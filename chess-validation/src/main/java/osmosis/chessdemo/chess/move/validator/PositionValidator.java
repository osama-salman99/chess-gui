package osmosis.chessdemo.chess.move.validator;

import osmosis.chessdemo.chess.board.BoardSquares;
import osmosis.chessdemo.chess.exceptions.*;
import osmosis.chessdemo.chess.pieces.*;
import osmosis.chessdemo.chess.position.ChessPosition;
import osmosis.chessdemo.chess.position.File;
import osmosis.chessdemo.chess.position.Rank;

import java.util.ArrayList;
import java.util.List;
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

	// ── Candidate generation ──────────────────────────────────────────────────

	public List<ChessPosition> candidateDestinations(Piece piece) {
		if (piece instanceof Pawn) return pawnCandidates((Pawn) piece);
		if (piece instanceof Knight) return knightCandidates(piece.getPosition());
		if (piece instanceof Bishop) return slidingCandidates(piece.getPosition(), DIAGONAL_DIRS);
		if (piece instanceof Rook) return slidingCandidates(piece.getPosition(), STRAIGHT_DIRS);
		if (piece instanceof Queen) return slidingCandidates(piece.getPosition(), ALL_DIRS);
		if (piece instanceof King) return kingCandidates(piece);
		return List.of();
	}

	private static final int[][] DIAGONAL_DIRS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
	private static final int[][] STRAIGHT_DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
	private static final int[][] ALL_DIRS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
	private static final int[][] KNIGHT_OFFSETS = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};

	private List<ChessPosition> slidingCandidates(ChessPosition from, int[][] dirs) {
		List<ChessPosition> candidates = new ArrayList<>();
		for (int[] dir : dirs) {
			for (int f = from.getFile().getFileNumber() + dir[0], r = from.getRank().getRankNumber() + dir[1];
				 f >= 1 && f <= 8 && r >= 1 && r <= 8;
				 f += dir[0], r += dir[1]) {
				candidates.add(new ChessPosition(File.getFile(f), Rank.getRank(r)));
				if (ctx.boardSquares().get(new ChessPosition(File.getFile(f), Rank.getRank(r))).isPresent()) break;
			}
		}
		return candidates;
	}

	private static List<ChessPosition> knightCandidates(ChessPosition from) {
		List<ChessPosition> candidates = new ArrayList<>();
		for (int[] off : KNIGHT_OFFSETS) {
			int f = from.getFile().getFileNumber() + off[0];
			int r = from.getRank().getRankNumber() + off[1];
			if (f >= 1 && f <= 8 && r >= 1 && r <= 8) {
				candidates.add(new ChessPosition(File.getFile(f), Rank.getRank(r)));
			}
		}
		return candidates;
	}

	private static List<ChessPosition> pawnCandidates(Pawn pawn) {
		List<ChessPosition> candidates = new ArrayList<>();
		ChessPosition pos = pawn.getPosition();
		int dir = PieceColor.WHITE.equals(pawn.getColor()) ? 1 : -1;
		int rank = pos.getRank().getRankNumber();
		int file = pos.getFile().getFileNumber();
		if (rank + dir >= 1 && rank + dir <= 8) {
			candidates.add(new ChessPosition(pos.getFile(), Rank.getRank(rank + dir)));
			boolean onStart = (PieceColor.WHITE.equals(pawn.getColor()) && rank == 2)
					|| (PieceColor.BLACK.equals(pawn.getColor()) && rank == 7);
			if (onStart) candidates.add(new ChessPosition(pos.getFile(), Rank.getRank(rank + 2 * dir)));
			if (file > 1) candidates.add(new ChessPosition(File.getFile(file - 1), Rank.getRank(rank + dir)));
			if (file < 8) candidates.add(new ChessPosition(File.getFile(file + 1), Rank.getRank(rank + dir)));
		}
		return candidates;
	}

	private List<ChessPosition> kingCandidates(Piece king) {
		List<ChessPosition> candidates = new ArrayList<>();
		ChessPosition pos = king.getPosition();
		for (int[] dir : ALL_DIRS) {
			int f = pos.getFile().getFileNumber() + dir[0];
			int r = pos.getRank().getRankNumber() + dir[1];
			if (f >= 1 && f <= 8 && r >= 1 && r <= 8) {
				candidates.add(new ChessPosition(File.getFile(f), Rank.getRank(r)));
			}
		}
		candidates.add(new ChessPosition(File.G, pos.getRank()));
		candidates.add(new ChessPosition(File.C, pos.getRank()));
		return candidates;
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
