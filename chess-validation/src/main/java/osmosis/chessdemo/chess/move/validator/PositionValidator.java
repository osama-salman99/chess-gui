package osmosis.chessdemo.chess.move.validator;

import osmosis.chessdemo.chess.board.BoardSquares;
import osmosis.chessdemo.chess.exceptions.*;
import osmosis.chessdemo.chess.pieces.*;
import osmosis.chessdemo.chess.position.ChessPosition;
import osmosis.chessdemo.chess.position.File;
import osmosis.chessdemo.chess.position.Rank;

import java.util.Optional;

import static osmosis.chessdemo.chess.move.validator.MoveValidator.isEmptyPath;

public class PositionValidator {
	private final ValidationContext ctx;

	public PositionValidator(ValidationContext ctx) {
		this.ctx = ctx;
	}

	// ── Public entry point ────────────────────────────────────────────────────

	public void validateMove(Piece piece, ChessPosition destination) {
		validateTurn(piece);
		if (isCastlingAttempt(piece, destination)) {
			validateCastling((King) piece, destination);
			return;
		}
		validatePieceMovement(piece, destination);
		Optional<Piece> occupyingPiece = ctx.boardSquares().get(destination);
		validatePawnMovement(piece, destination, occupyingPiece.isPresent());
		occupyingPiece.ifPresent(this::validateTakingOwnPiece);
		validatePathEmpty(piece, destination);
		if (kingInCheck(piece, destination)) {
			throw new KingInCheckException();
		}
	}

	// ── Check simulation ──────────────────────────────────────────────────────

	public boolean kingInCheck(Piece piece, ChessPosition destination) {
		BoardSquares mock = ctx.boardSquares().copy();
		mock.removePieceOn(destination);
		mock.removePieceOn(piece.getPosition());
		Piece copy = piece.copy();
		copy.setPosition(destination);
		mock.put(copy);

		Piece king = mock.getAll().stream()
				.filter(p -> ctx.currentTurn().equals(p.getColor()) && p instanceof King)
				.findAny()
				.orElseThrow(() -> new IllegalStateException("No king found"));

		for (Piece attacker : mock.getAll()) {
			if (ctx.currentTurn().equals(attacker.getColor())) continue;
			if (attacker instanceof King || attacker instanceof Knight) {
				if (attacker.isMovementValid(king.getPosition())) return true;
			} else if (attacker instanceof Pawn) {
				if (isPawnAttackingKing((Pawn) attacker, king)) return true;
			} else {
				if (attacker.isMovementValid(king.getPosition())
						&& isEmptyPath(attacker.getPosition(), king.getPosition(), mock)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isKingCurrentlyInCheck() {
		return ctx.boardSquares().getAll().stream()
				.filter(p -> ctx.currentTurn().equals(p.getColor()) && p instanceof King)
				.findAny()
				.map(king -> kingInCheck(king, king.getPosition()))
				.orElse(false);
	}

	// ── Castling ─────────────────────────────────────────────────────────────

	public boolean isCastlingAttempt(Piece piece, ChessPosition destination) {
		if (!(piece instanceof King)) return false;
		int fileDiff = Math.abs(destination.getFile().getFileNumber() - piece.getPosition().getFile().getFileNumber());
		int rankDiff = Math.abs(destination.getRank().getRankNumber() - piece.getPosition().getRank().getRankNumber());
		return fileDiff == 2 && rankDiff == 0;
	}

	private void validateCastling(King king, ChessPosition destination) {
		boolean kingside = destination.getFile().getFileNumber() > king.getPosition().getFile().getFileNumber();
		if (!hasCastlingRight(king.getColor(), kingside)) {
			throw new InvalidMoveException("Castling right is not available");
		}
		Rank castleRank = king.getPosition().getRank();
		ChessPosition rookPos = new ChessPosition(kingside ? File.H : File.A, castleRank);
		if (!ctx.boardSquares().get(rookPos).filter(p -> p instanceof Rook).isPresent()) {
			throw new InvalidMoveException("Castling rook is not in place");
		}
		if (!isEmptyPath(king.getPosition(), rookPos, ctx.boardSquares())) {
			throw new InvalidMoveException("Castling path is not empty");
		}
		if (kingInCheck(king, king.getPosition())) {
			throw new KingInCheckException();
		}
		ChessPosition passThrough = new ChessPosition(kingside ? File.F : File.D, castleRank);
		if (kingInCheck(king, passThrough)) {
			throw new KingInCheckException();
		}
		if (kingInCheck(king, destination)) {
			throw new KingInCheckException();
		}
	}

	private boolean hasCastlingRight(PieceColor color, boolean kingside) {
		if (PieceColor.WHITE.equals(color)) {
			return kingside ? ctx.whiteKingSideCastle() : ctx.whiteQueenSideCastle();
		} else {
			return kingside ? ctx.blackKingSideCastle() : ctx.blackQueenSideCastle();
		}
	}

	// ── Per-piece rules ───────────────────────────────────────────────────────

	private void validateTurn(Piece piece) {
		if (!ctx.currentTurn().equals(piece.getColor())) {
			throw new WrongTurnException(ctx.currentTurn());
		}
	}

	private static void validatePieceMovement(Piece piece, ChessPosition destination) {
		if (!piece.isMovementValid(destination)) {
			throw new InvalidMoveException("Invalid piece movement");
		}
	}

	private void validatePawnMovement(Piece piece, ChessPosition destination, boolean occupied) {
		if (!(piece instanceof Pawn)) return;
		boolean advance = piece.getPosition().getFile().absoluteDifference(destination.getFile()) == 0;
		if (advance && occupied) {
			throw new InvalidMoveException("Pawn advancing destination is occupied");
		}
		boolean enPassant = !advance && !occupied
				&& ctx.enPassantTarget().map(destination::equals).orElse(false);
		if (!advance && !occupied && !enPassant) {
			throw new InvalidNumberException("Pawn is not taking any piece");
		}
	}

	private void validatePathEmpty(Piece piece, ChessPosition destination) {
		if (!(piece instanceof Knight) && !isEmptyPath(piece.getPosition(), destination, ctx.boardSquares())) {
			throw new InvalidMoveException("Path is not empty");
		}
	}

	private void validateTakingOwnPiece(Piece occupying) {
		if (ctx.currentTurn().equals(occupying.getColor())) {
			throw new InvalidMoveException("Player is taking their own piece");
		}
	}

	private static boolean isPawnAttackingKing(Pawn pawn, Piece king) {
		int right = pawn.getPosition().getFile().getFileNumber() + 1;
		int left = pawn.getPosition().getFile().getFileNumber() - 1;
		int kingFile = king.getPosition().getFile().getFileNumber();
		int attackRank = PieceColor.WHITE.equals(pawn.getColor())
				? pawn.getPosition().getRank().getRankNumber() + 1
				: pawn.getPosition().getRank().getRankNumber() - 1;
		return king.getPosition().getRank().getRankNumber() == attackRank
				&& (kingFile == right || kingFile == left);
	}
}
