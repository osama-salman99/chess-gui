package osmosis.chessdemo.chess.board;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import osmosis.chessdemo.chess.exceptions.*;
import osmosis.chessdemo.chess.fen.FenParser;
import osmosis.chessdemo.chess.pieces.*;
import osmosis.chessdemo.chess.position.ChessPosition;
import osmosis.chessdemo.chess.position.File;
import osmosis.chessdemo.chess.position.Rank;
import osmosis.chessdemo.functionailties.DraggableImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static osmosis.chessdemo.chess.move.validator.MoveValidator.isEmptyPath;

public class Board {
	private static final String NEW_GAME_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
	private final GridPane boardGridPane;
	private final BoardSquares boardSquares;
	private PieceColor currentTurn;

	// En passant: square a capturing pawn moves TO after opponent double-advance
	private Optional<ChessPosition> enPassantTarget = Optional.empty();

	// Castling rights
	private boolean whiteKingSideCastle = true;
	private boolean whiteQueenSideCastle = true;
	private boolean blackKingSideCastle = true;
	private boolean blackQueenSideCastle = true;

	// Callbacks set by the controller
	private Function<PieceColor, Pawn.PromotionPiece> promotionChooser = color -> Pawn.PromotionPiece.Queen;
	private Consumer<String> gameOverHandler = message -> System.out.println("Game over: " + message);

	private Board(GridPane boardGridPane, BoardSquares boardSquares) {
		this.boardGridPane = boardGridPane;
		this.boardSquares = boardSquares;
		this.currentTurn = PieceColor.WHITE;
		refreshBoard();
	}

	public static Board createChessBoard(GridPane boardGridPane) {
		try {
			return createChessBoard(NEW_GAME_FEN, boardGridPane);
		} catch (InvalidFenException e) {
			throw new RuntimeException("Internal new-game FEN is invalid or is not being parsed correctly");
		}
	}

	public static Board createChessBoard(String fen, GridPane boardGridPane) throws InvalidFenException {
		return new Board(boardGridPane, FenParser.parse(fen));
	}

	public Optional<Piece> getPieceAt(ChessPosition position) {
		return boardSquares.get(position);
	}

	public void setPromotionChooser(Function<PieceColor, Pawn.PromotionPiece> promotionChooser) {
		this.promotionChooser = promotionChooser;
	}

	public void setGameOverHandler(Consumer<String> gameOverHandler) {
		this.gameOverHandler = gameOverHandler;
	}

	public void makeMove(Piece piece, ChessPosition destinationPosition) {
		boolean moveSuccessful = false;
		try {
			validateMove(piece, destinationPosition);
			executeMove(piece, destinationPosition);
			moveSuccessful = true;
		} finally {
			refreshBoard();
		}
		if (moveSuccessful) {
			checkForGameEnd();
		}
	}

	public void validateMove(Piece piece, ChessPosition destinationPosition) {
		validateTurn(piece);
		if (isCastlingAttempt(piece, destinationPosition)) {
			validateCastling((King) piece, destinationPosition);
			return;
		}
		validatePieceMovement(piece, destinationPosition);
		Optional<Piece> occupyingPiece = boardSquares.get(destinationPosition);
		validatePawnMovement(piece, destinationPosition, occupyingPiece.isPresent());
		occupyingPiece.ifPresent(this::validateTakingOwnPiece);
		validatePathEmpty(piece, destinationPosition);
		validateKingCheck(piece, destinationPosition);
	}

	private void executeMove(Piece piece, ChessPosition destinationPosition) {
		ChessPosition origin = piece.getPosition();

		if (isCastlingAttempt(piece, destinationPosition)) {
			executeCastling((King) piece, destinationPosition, origin);
			currentTurn = flipTurn(currentTurn);
			updateCastlingRights(piece, origin);
			enPassantTarget = Optional.empty();
			return;
		}

		boolean isEnPassant = isEnPassantCapture(piece, destinationPosition);

		if (isEnPassant) {
			boardSquares.removePieceOn(new ChessPosition(destinationPosition.getFile(), origin.getRank()));
		} else {
			revokeCapturedRookRights(destinationPosition);
			boardSquares.removePieceOn(destinationPosition);
		}

		boardSquares.removePieceOn(origin);
		piece.setPosition(destinationPosition);
		boardSquares.put(piece);
		currentTurn = flipTurn(currentTurn);

		updateCastlingRights(piece, origin);
		updateEnPassantTarget(piece, origin, destinationPosition);
		handlePromotion(piece, origin, destinationPosition);
	}

	// ── En passant ───────────────────────────────────────────────────────────

	private boolean isEnPassantCapture(Piece piece, ChessPosition destination) {
		return piece instanceof Pawn
				&& enPassantTarget.map(destination::equals).orElse(false)
				&& isPawnDiagonalMove(piece.getPosition(), destination);
	}

	private static boolean isPawnDiagonalMove(ChessPosition origin, ChessPosition destination) {
		return origin.getFile().absoluteDifference(destination.getFile()) == 1;
	}

	private void updateEnPassantTarget(Piece piece, ChessPosition origin, ChessPosition destination) {
		if (piece instanceof Pawn
				&& Math.abs(destination.getRank().getRankNumber() - origin.getRank().getRankNumber()) == 2) {
			int skippedRank = (origin.getRank().getRankNumber() + destination.getRank().getRankNumber()) / 2;
			enPassantTarget = Optional.of(new ChessPosition(origin.getFile(), Rank.getRank(skippedRank)));
		} else {
			enPassantTarget = Optional.empty();
		}
	}

	// ── Castling ─────────────────────────────────────────────────────────────

	private boolean isCastlingAttempt(Piece piece, ChessPosition destination) {
		if (!(piece instanceof King)) return false;
		int fileDiff = Math.abs(destination.getFile().getFileNumber() - piece.getPosition().getFile().getFileNumber());
		int rankDiff = Math.abs(destination.getRank().getRankNumber() - piece.getPosition().getRank().getRankNumber());
		return fileDiff == 2 && rankDiff == 0;
	}

	private void validateCastling(King king, ChessPosition destination) {
		boolean kingside = destination.getFile().getFileNumber() > king.getPosition().getFile().getFileNumber();
		boolean hasRight = hasCastlingRight(king.getColor(), kingside);
		if (!hasRight) {
			throw new InvalidMoveException("Castling right is not available");
		}
		Rank castleRank = king.getPosition().getRank();
		ChessPosition rookPosition = new ChessPosition(kingside ? File.H : File.A, castleRank);
		if (!boardSquares.get(rookPosition).filter(p -> p instanceof Rook).isPresent()) {
			throw new InvalidMoveException("Castling rook is not in place");
		}
		if (!isEmptyPath(king.getPosition(), rookPosition, boardSquares)) {
			throw new InvalidMoveException("Castling path is not empty");
		}
		if (kingInCheck(king, king.getPosition())) {
			throw new KingInCheckException();
		}
		ChessPosition passThrough = new ChessPosition(kingside ? File.F : File.D, castleRank);
		if (kingInCheck(king, passThrough)) {
			throw new KingInCheckException();
		}
	}

	private boolean hasCastlingRight(PieceColor color, boolean kingside) {
		if (PieceColor.WHITE.equals(color)) {
			return kingside ? whiteKingSideCastle : whiteQueenSideCastle;
		} else {
			return kingside ? blackKingSideCastle : blackQueenSideCastle;
		}
	}

	private void executeCastling(King king, ChessPosition destination, ChessPosition origin) {
		boolean kingside = destination.getFile().getFileNumber() > origin.getFile().getFileNumber();
		Rank castleRank = origin.getRank();

		boardSquares.removePieceOn(origin);
		king.setPosition(destination);
		boardSquares.put(king);

		ChessPosition rookOrigin = new ChessPosition(kingside ? File.H : File.A, castleRank);
		ChessPosition rookDestination = new ChessPosition(kingside ? File.F : File.D, castleRank);
		Piece rook = boardSquares.get(rookOrigin).orElseThrow(() -> new InvalidMoveException("Rook not found for castling"));
		boardSquares.removePieceOn(rookOrigin);
		rook.setPosition(rookDestination);
		boardSquares.put(rook);
	}

	private void updateCastlingRights(Piece piece, ChessPosition origin) {
		if (piece instanceof King) {
			if (PieceColor.WHITE.equals(piece.getColor())) {
				whiteKingSideCastle = false;
				whiteQueenSideCastle = false;
			} else {
				blackKingSideCastle = false;
				blackQueenSideCastle = false;
			}
		} else if (piece instanceof Rook) {
			revokeCastlingRightForCorner(origin);
		}
	}

	private void revokeCapturedRookRights(ChessPosition capturePosition) {
		boardSquares.get(capturePosition)
				.filter(p -> p instanceof Rook)
				.ifPresent(rook -> revokeCastlingRightForCorner(capturePosition));
	}

	private void revokeCastlingRightForCorner(ChessPosition position) {
		if (File.A.equals(position.getFile()) && Rank.FIRST.equals(position.getRank())) whiteQueenSideCastle = false;
		if (File.H.equals(position.getFile()) && Rank.FIRST.equals(position.getRank())) whiteKingSideCastle = false;
		if (File.A.equals(position.getFile()) && Rank.EIGHTH.equals(position.getRank())) blackQueenSideCastle = false;
		if (File.H.equals(position.getFile()) && Rank.EIGHTH.equals(position.getRank())) blackKingSideCastle = false;
	}

	// ── Promotion ────────────────────────────────────────────────────────────

	private void handlePromotion(Piece piece, ChessPosition origin, ChessPosition destinationPosition) {
		if (!(piece instanceof Pawn)) return;
		Rank destinationRank = destinationPosition.getRank();
		if (!Rank.FIRST.equals(destinationRank) && !Rank.EIGHTH.equals(destinationRank)) return;

		boardSquares.removePieceOn(piece.getPosition());
		// promote() validates movement from the pawn's pre-move rank, so restore origin temporarily
		piece.setPosition(origin);
		Pawn.PromotionPiece choice = promotionChooser.apply(piece.getColor());
		Piece promotionPiece = ((Pawn) piece).promote(choice, destinationPosition.getFile());
		boardSquares.put(promotionPiece);
	}

	// ── Checkmate / Stalemate ────────────────────────────────────────────────

	private void checkForGameEnd() {
		if (hasAnyLegalMove()) return;
		boolean inCheck = isKingCurrentlyInCheck();
		String winner = PieceColor.WHITE.equals(currentTurn) ? "Black" : "White";
		String message = inCheck
				? winner + " wins! Checkmate."
				: "Draw! Stalemate.";
		gameOverHandler.accept(message);
	}

	public boolean hasAnyLegalMove() {
		List<Piece> currentPieces = new ArrayList<>(boardSquares.getAll());
		for (Piece piece : currentPieces) {
			if (!currentTurn.equals(piece.getColor())) continue;
			for (int file = 1; file <= 8; file++) {
				for (int rank = 1; rank <= 8; rank++) {
					ChessPosition dest = new ChessPosition(File.getFile(file), Rank.getRank(rank));
					try {
						validateMove(piece, dest);
						return true;
					} catch (Exception ignored) {
					}
				}
			}
		}
		return false;
	}

	public boolean isKingCurrentlyInCheck() {
		Piece king = boardSquares.getAll().stream()
				.filter(this::isCurrentKing)
				.findAny()
				.orElseThrow(() -> new IllegalStateException("No king found"));
		return kingInCheck(king, king.getPosition());
	}

	// ── Reset ────────────────────────────────────────────────────────────────

	public void reset() {
		try {
			BoardSquares startingSquares = FenParser.parse(NEW_GAME_FEN);
			boardSquares.clear();
			startingSquares.getAll().forEach(boardSquares::put);
		} catch (InvalidFenException e) {
			throw new RuntimeException("Internal new-game FEN is invalid", e);
		}
		currentTurn = PieceColor.WHITE;
		enPassantTarget = Optional.empty();
		whiteKingSideCastle = whiteQueenSideCastle = blackKingSideCastle = blackQueenSideCastle = true;
		refreshBoard();
	}

	// ── Existing validation helpers ──────────────────────────────────────────

	private void validatePawnMovement(Piece piece, ChessPosition destinationPosition, boolean occupyingPiecePresent) {
		if (!(piece instanceof Pawn)) return;
		boolean pawnAdvance = isPawnAdvance(piece.getPosition().getFile(), destinationPosition.getFile());
		if (pawnAdvance && occupyingPiecePresent) {
			throw new InvalidMoveException("Pawn advancing destination is occupied");
		}
		boolean isEnPassant = !pawnAdvance && !occupyingPiecePresent
				&& enPassantTarget.map(destinationPosition::equals).orElse(false);
		if (!pawnAdvance && !occupyingPiecePresent && !isEnPassant) {
			throw new InvalidNumberException("Pawn is not taking any piece");
		}
	}

	private static boolean isPawnAdvance(File currentFile, File destinationFile) {
		return currentFile.absoluteDifference(destinationFile) == 0;
	}

	private void validateTurn(Piece piece) {
		if (!currentTurn.equals(piece.getColor())) {
			throw new WrongTurnException(currentTurn);
		}
	}

	private void validatePathEmpty(Piece piece, ChessPosition destinationPosition) {
		if (!(piece instanceof Knight) && !isEmptyPath(piece.getPosition(), destinationPosition, boardSquares)) {
			throw new InvalidMoveException("Path is not empty");
		}
	}

	private void validateTakingOwnPiece(Piece occupyingPiece) {
		if (currentTurn.equals(occupyingPiece.getColor())) {
			throw new InvalidMoveException("Player is taking their own piece");
		}
	}

	private void validateKingCheck(Piece piece, ChessPosition destinationPosition) {
		if (kingInCheck(piece, destinationPosition)) {
			throw new KingInCheckException();
		}
	}

	private static void validatePieceMovement(Piece piece, ChessPosition destinationPosition) {
		if (!piece.isMovementValid(destinationPosition)) {
			throw new InvalidMoveException("Invalid piece movement");
		}
	}

	// ── King-in-check simulation ─────────────────────────────────────────────

	public boolean kingInCheck(Piece piece, ChessPosition destinationPosition) {
		BoardSquares mockPieces = boardSquares.copy();
		mockPieces.removePieceOn(destinationPosition);
		mockPieces.removePieceOn(piece.getPosition());
		Piece copyPiece = piece.copy();
		copyPiece.setPosition(destinationPosition);
		mockPieces.put(copyPiece);
		Piece king = mockPieces.getAll().stream().filter(this::isCurrentKing).findAny()
				.orElseThrow(() -> new IllegalStateException("No king found"));
		for (Piece mockPiece : mockPieces.getAll()) {
			if (currentTurn.equals(mockPiece.getColor())) continue;
			if (mockPiece instanceof King || mockPiece instanceof Knight) {
				if (mockPiece.isMovementValid(king.getPosition())) return true;
			} else if (mockPiece instanceof Pawn) {
				if (isPawnAttackingKing((Pawn) mockPiece, king)) return true;
			} else {
				if (mockPiece.isMovementValid(king.getPosition())
						&& isEmptyPath(mockPiece.getPosition(), king.getPosition(), mockPieces)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isPawnAttackingKing(Pawn pawn, Piece king) {
		int takeFileRight = pawn.getPosition().getFile().getFileNumber() + 1;
		int takeFileLeft = pawn.getPosition().getFile().getFileNumber() - 1;
		int kingFile = king.getPosition().getFile().getFileNumber();
		int takeRank = PieceColor.WHITE.equals(pawn.getColor())
				? pawn.getPosition().getRank().getRankNumber() + 1
				: pawn.getPosition().getRank().getRankNumber() - 1;
		return king.getPosition().getRank().getRankNumber() == takeRank
				&& (kingFile == takeFileRight || kingFile == takeFileLeft);
	}

	private boolean isCurrentKing(Piece piece) {
		return currentTurn.equals(piece.getColor()) && piece instanceof King;
	}

	// ── Board rendering ──────────────────────────────────────────────────────

	private void refreshBoard() {
		boardGridPane.getChildren().clear();
		double sideLength = boardGridPane.getPrefHeight() / 8;
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				boardGridPane.add(createEmptyCell(sideLength), x, y);
			}
		}
		for (Piece piece : boardSquares.getAll()) {
			DraggableImageView pieceImageView = piece.getImageView();
			pieceImageView.setFitWidth(sideLength);
			pieceImageView.setFitHeight(sideLength);
			ChessPosition position = piece.getPosition();
			boardGridPane.add(pieceImageView, position.getFile().getFileNumber() - 1,
					position.getRank().getInverse().getRankNumber() - 1);
		}
	}

	private static Pane createEmptyCell(double sideLength) {
		Pane emptyCell = new Pane();
		emptyCell.minHeightProperty().set(sideLength);
		emptyCell.minWidthProperty().set(sideLength);
		return emptyCell;
	}

	private static PieceColor flipTurn(PieceColor color) {
		return PieceColor.WHITE.equals(color) ? PieceColor.BLACK : PieceColor.WHITE;
	}
}
