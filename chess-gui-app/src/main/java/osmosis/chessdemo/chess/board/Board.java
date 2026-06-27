package osmosis.chessdemo.chess.board;

import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmosis.chessdemo.chess.exceptions.*;
import osmosis.chessdemo.chess.fen.FenParser;
import osmosis.chessdemo.chess.helper.PieceDragListener;
import osmosis.chessdemo.chess.move.validator.PositionValidator;
import osmosis.chessdemo.chess.move.validator.ValidationContext;
import osmosis.chessdemo.chess.pieces.*;
import osmosis.chessdemo.chess.pieces.symbol.PieceSymbolProvider;
import osmosis.chessdemo.chess.position.ChessPosition;
import osmosis.chessdemo.chess.position.File;
import osmosis.chessdemo.chess.position.Rank;
import osmosis.chessdemo.functionailties.DraggableImageView;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Board {
	private static final Logger log = LoggerFactory.getLogger(Board.class);
	private static final String NEW_GAME_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

	private static final String CELL_HIGHLIGHT_LAST_MOVE = "-fx-background-color: rgba(255, 255, 0, 0.45);";
	private static final String CELL_HIGHLIGHT_CHECK = "-fx-background-color: rgba(220, 50, 50, 0.55);";

	private final GridPane boardGridPane;
	private final BoardSquares boardSquares;
	private final Map<Piece, DraggableImageView> pieceViews = new IdentityHashMap<>();
	private PieceColor currentTurn;

	private Optional<ChessPosition> enPassantTarget = Optional.empty();

	private boolean whiteKingSideCastle = true;
	private boolean whiteQueenSideCastle = true;
	private boolean blackKingSideCastle = true;
	private boolean blackQueenSideCastle = true;

	private int halfMoveClock = 0;

	private Optional<ChessPosition> lastMoveFrom = Optional.empty();
	private Optional<ChessPosition> lastMoveTo = Optional.empty();
	private boolean currentPlayerKingInCheck = false;

	private Function<PieceColor, Pawn.PromotionPiece> promotionChooser = color -> Pawn.PromotionPiece.Queen;
	private Consumer<String> gameOverHandler = message -> log.info("Game over: {}", message);

	private Board(GridPane boardGridPane, BoardSquares boardSquares) {
		this.boardGridPane = boardGridPane;
		this.boardSquares = boardSquares;
		this.currentTurn = PieceColor.WHITE;
		boardSquares.getAll().forEach(piece -> pieceViews.put(piece, createPieceView(piece)));
		log.info("Board created — white to move");
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

	// ── Public move entry point ───────────────────────────────────────────────

	public void makeMove(Piece piece, ChessPosition destinationPosition) {
		boolean moveSuccessful = false;
		try {
			validateMove(piece, destinationPosition);
			executeMove(piece, destinationPosition);
			moveSuccessful = true;
		} catch (InvalidMoveException e) {
			log.warn("Invalid move {} → {}: {}", piece.getPosition(), destinationPosition, e.getMessage());
			throw e;
		} finally {
			refreshBoard();
		}
		if (moveSuccessful) {
			checkForGameEnd();
		}
	}

	public void validateMove(Piece piece, ChessPosition destinationPosition) {
		validator().validateMove(piece, destinationPosition);
	}

	// ── Execution ────────────────────────────────────────────────────────────

	private void executeMove(Piece piece, ChessPosition destinationPosition) {
		ChessPosition origin = piece.getPosition();
		PositionValidator v = validator();

		if (v.isCastlingAttempt(piece, destinationPosition)) {
			log.info("{} castles {}", colorName(piece.getColor()),
					destinationPosition.getFile().getFileNumber() > origin.getFile().getFileNumber() ? "kingside" : "queenside");
			executeCastling((King) piece, destinationPosition, origin);
			currentTurn = flipTurn(currentTurn);
			updateCastlingRights(piece, origin);
			enPassantTarget = Optional.empty();
			halfMoveClock++;
			lastMoveFrom = Optional.of(origin);
			lastMoveTo = Optional.of(destinationPosition);
			currentPlayerKingInCheck = validator().isKingCurrentlyInCheck();
			if (currentPlayerKingInCheck) log.info("{} king is in check", colorName(currentTurn));
			return;
		}

		boolean isEnPassant = isEnPassantCapture(piece, destinationPosition);
		boolean isCapture = boardSquares.get(destinationPosition).isPresent() || isEnPassant;

		if (isEnPassant) {
			ChessPosition capturedPawnPos = new ChessPosition(destinationPosition.getFile(), origin.getRank());
			boardSquares.get(capturedPawnPos).ifPresent(captured -> {
				log.info("{} captures {} en passant on {}", piece, captured, destinationPosition);
				pieceViews.remove(captured);
			});
			boardSquares.removePieceOn(capturedPawnPos);
		} else {
			boardSquares.get(destinationPosition).ifPresent(captured -> {
				log.info("{} captures {}", piece, captured);
				pieceViews.remove(captured);
			});
			revokeCapturedRookRights(destinationPosition);
			boardSquares.removePieceOn(destinationPosition);
		}

		boardSquares.removePieceOn(origin);
		piece.setPosition(destinationPosition);
		boardSquares.put(piece);
		currentTurn = flipTurn(currentTurn);

		updateCastlingRights(piece, origin);
		updateEnPassantTarget(piece, origin, destinationPosition);
		halfMoveClock = (piece instanceof Pawn || isCapture) ? 0 : halfMoveClock + 1;
		handlePromotion(piece, origin, destinationPosition);

		lastMoveFrom = Optional.of(origin);
		lastMoveTo = Optional.of(destinationPosition);
		currentPlayerKingInCheck = validator().isKingCurrentlyInCheck();

		log.info("{} {} → {}{}", piece.getClass().getSimpleName(), origin, destinationPosition,
				currentPlayerKingInCheck ? " (check!)" : "");
		if (currentPlayerKingInCheck) log.info("{} king is in check", colorName(currentTurn));
	}

	// ── En passant ───────────────────────────────────────────────────────────

	private boolean isEnPassantCapture(Piece piece, ChessPosition destination) {
		return piece instanceof Pawn
				&& enPassantTarget.map(destination::equals).orElse(false)
				&& piece.getPosition().getFile().absoluteDifference(destination.getFile()) == 1;
	}

	private void updateEnPassantTarget(Piece piece, ChessPosition origin, ChessPosition destination) {
		if (piece instanceof Pawn
				&& Math.abs(destination.getRank().getRankNumber() - origin.getRank().getRankNumber()) == 2) {
			int skippedRank = (origin.getRank().getRankNumber() + destination.getRank().getRankNumber()) / 2;
			ChessPosition target = new ChessPosition(origin.getFile(), Rank.getRank(skippedRank));
			enPassantTarget = Optional.of(target);
			log.debug("En passant target set to {}", target);
		} else {
			enPassantTarget = Optional.empty();
		}
	}

	// ── Castling ─────────────────────────────────────────────────────────────

	private void executeCastling(King king, ChessPosition destination, ChessPosition origin) {
		boolean kingside = destination.getFile().getFileNumber() > origin.getFile().getFileNumber();
		Rank castleRank = origin.getRank();

		boardSquares.removePieceOn(origin);
		king.setPosition(destination);
		boardSquares.put(king);

		ChessPosition rookOrigin = new ChessPosition(kingside ? File.H : File.A, castleRank);
		ChessPosition rookDest = new ChessPosition(kingside ? File.F : File.D, castleRank);
		Piece rook = boardSquares.get(rookOrigin)
				.orElseThrow(() -> new InvalidMoveException("Rook not found for castling"));
		boardSquares.removePieceOn(rookOrigin);
		rook.setPosition(rookDest);
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

	private void revokeCastlingRightForCorner(ChessPosition pos) {
		if (File.A.equals(pos.getFile()) && Rank.FIRST.equals(pos.getRank())) whiteQueenSideCastle = false;
		if (File.H.equals(pos.getFile()) && Rank.FIRST.equals(pos.getRank())) whiteKingSideCastle = false;
		if (File.A.equals(pos.getFile()) && Rank.EIGHTH.equals(pos.getRank())) blackQueenSideCastle = false;
		if (File.H.equals(pos.getFile()) && Rank.EIGHTH.equals(pos.getRank())) blackKingSideCastle = false;
	}

	// ── Promotion ────────────────────────────────────────────────────────────

	private void handlePromotion(Piece piece, ChessPosition origin, ChessPosition destination) {
		if (!(piece instanceof Pawn)) return;
		Rank destRank = destination.getRank();
		if (!Rank.FIRST.equals(destRank) && !Rank.EIGHTH.equals(destRank)) return;

		boardSquares.removePieceOn(piece.getPosition());
		piece.setPosition(origin);
		Pawn.PromotionPiece choice = promotionChooser.apply(piece.getColor());
		Piece promotionPiece = ((Pawn) piece).promote(choice, destination.getFile());
		boardSquares.put(promotionPiece);

		pieceViews.remove(piece);
		pieceViews.put(promotionPiece, createPieceView(promotionPiece));

		log.info("{} pawn promotes to {} on {}", colorName(piece.getColor()), choice, destination);
	}

	// ── Game-end detection ───────────────────────────────────────────────────

	private void checkForGameEnd() {
		if (halfMoveClock >= 100) {
			log.info("Draw by 50-move rule (half-move clock: {})", halfMoveClock);
			gameOverHandler.accept("Draw! 50-move rule.");
			return;
		}
		if (isInsufficientMaterial()) {
			log.info("Draw by insufficient material");
			gameOverHandler.accept("Draw! Insufficient material.");
			return;
		}
		if (hasAnyLegalMove()) return;
		boolean inCheck = validator().isKingCurrentlyInCheck();
		String winner = PieceColor.WHITE.equals(currentTurn) ? "Black" : "White";
		String message = inCheck ? winner + " wins! Checkmate." : "Draw! Stalemate.";
		log.info(message);
		gameOverHandler.accept(message);
	}

	public boolean hasAnyLegalMove() {
		PositionValidator v = validator();
		List<Piece> currentPieces = new ArrayList<>(boardSquares.getAll());
		for (Piece piece : currentPieces) {
			if (!currentTurn.equals(piece.getColor())) continue;
			for (int file = 1; file <= 8; file++) {
				for (int rank = 1; rank <= 8; rank++) {
					ChessPosition dest = new ChessPosition(File.getFile(file), Rank.getRank(rank));
					try {
						v.validateMove(piece, dest);
						return true;
					} catch (Exception ignored) {
					}
				}
			}
		}
		return false;
	}

	public boolean isKingCurrentlyInCheck() {
		return validator().isKingCurrentlyInCheck();
	}

	public boolean kingInCheck(Piece piece, ChessPosition destinationPosition) {
		return validator().kingInCheck(piece, destinationPosition);
	}

	private boolean isInsufficientMaterial() {
		List<Piece> pieces = new ArrayList<>(boardSquares.getAll());
		int total = pieces.size();
		if (total == 2) return true;
		if (total == 3) return pieces.stream().anyMatch(p -> p instanceof Knight || p instanceof Bishop);
		if (total == 4) {
			List<Piece> bishops = pieces.stream().filter(p -> p instanceof Bishop).collect(Collectors.toList());
			if (bishops.size() == 2) {
				int p1 = (bishops.get(0).getPosition().getFile().getFileNumber()
						+ bishops.get(0).getPosition().getRank().getRankNumber()) % 2;
				int p2 = (bishops.get(1).getPosition().getFile().getFileNumber()
						+ bishops.get(1).getPosition().getRank().getRankNumber()) % 2;
				return p1 == p2;
			}
		}
		return false;
	}

	// ── Reset ────────────────────────────────────────────────────────────────

	public void reset() {
		try {
			BoardSquares startingSquares = FenParser.parse(NEW_GAME_FEN);
			boardSquares.clear();
			pieceViews.clear();
			startingSquares.getAll().forEach(piece -> {
				boardSquares.put(piece);
				pieceViews.put(piece, createPieceView(piece));
			});
		} catch (InvalidFenException e) {
			throw new RuntimeException("Internal new-game FEN is invalid", e);
		}
		currentTurn = PieceColor.WHITE;
		enPassantTarget = Optional.empty();
		whiteKingSideCastle = whiteQueenSideCastle = blackKingSideCastle = blackQueenSideCastle = true;
		halfMoveClock = 0;
		lastMoveFrom = Optional.empty();
		lastMoveTo = Optional.empty();
		currentPlayerKingInCheck = false;
		log.info("Board reset — white to move");
		refreshBoard();
	}

	// ── Validator factory ─────────────────────────────────────────────────────

	private PositionValidator validator() {
		return new PositionValidator(new ValidationContext(
				boardSquares, currentTurn, enPassantTarget,
				whiteKingSideCastle, whiteQueenSideCastle,
				blackKingSideCastle, blackQueenSideCastle));
	}

	// ── Piece view factory ────────────────────────────────────────────────────

	private DraggableImageView createPieceView(Piece piece) {
		String pieceName = piece.getClass().getSimpleName().toLowerCase();
		Image image = PieceColor.WHITE.equals(piece.getColor())
				? PieceSymbolProvider.getWhitePieceSymbol(pieceName)
				: PieceSymbolProvider.getBlackPieceSymbol(pieceName);
		DraggableImageView view = new DraggableImageView(image);
		view.setDragListener(new PieceDragListener(piece));
		return view;
	}

	// ── Board rendering ───────────────────────────────────────────────────────

	private void refreshBoard() {
		boardGridPane.getChildren().clear();
		double sideLength = boardGridPane.getPrefHeight() / 8;

		Optional<ChessPosition> checkKingPos = currentPlayerKingInCheck
				? boardSquares.getAll().stream()
						.filter(p -> currentTurn.equals(p.getColor()) && p instanceof King)
						.map(Piece::getPosition).findAny()
				: Optional.empty();

		for (int col = 0; col < 8; col++) {
			for (int row = 0; row < 8; row++) {
				ChessPosition cellPos = gridToChessPosition(col, row);
				Pane cell = createEmptyCell(sideLength);
				if (checkKingPos.map(cellPos::equals).orElse(false)) {
					cell.setStyle(CELL_HIGHLIGHT_CHECK);
				} else if (isLastMoveSquare(cellPos)) {
					cell.setStyle(CELL_HIGHLIGHT_LAST_MOVE);
				}
				boardGridPane.add(cell, col, row);
			}
		}

		for (Piece piece : boardSquares.getAll()) {
			DraggableImageView view = pieceViews.get(piece);
			if (view == null) continue;
			view.setFitWidth(sideLength);
			view.setFitHeight(sideLength);
			ChessPosition pos = piece.getPosition();
			boardGridPane.add(view, pos.getFile().getFileNumber() - 1,
					pos.getRank().getInverse().getRankNumber() - 1);
		}
	}

	private static ChessPosition gridToChessPosition(int col, int row) {
		return new ChessPosition(File.getFile(col + 1), Rank.getRank(8 - row));
	}

	private boolean isLastMoveSquare(ChessPosition pos) {
		return lastMoveFrom.map(pos::equals).orElse(false)
				|| lastMoveTo.map(pos::equals).orElse(false);
	}

	private static Pane createEmptyCell(double sideLength) {
		Pane cell = new Pane();
		cell.minHeightProperty().set(sideLength);
		cell.minWidthProperty().set(sideLength);
		return cell;
	}

	private static PieceColor flipTurn(PieceColor color) {
		return PieceColor.WHITE.equals(color) ? PieceColor.BLACK : PieceColor.WHITE;
	}

	private static String colorName(PieceColor color) {
		return PieceColor.WHITE.equals(color) ? "White" : "Black";
	}
}
