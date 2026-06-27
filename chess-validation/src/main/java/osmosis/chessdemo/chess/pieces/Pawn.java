package osmosis.chessdemo.chess.pieces;

import osmosis.chessdemo.chess.exceptions.InvalidPromotionMoveException;
import osmosis.chessdemo.chess.position.ChessPosition;
import osmosis.chessdemo.chess.position.File;
import osmosis.chessdemo.chess.position.Rank;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Pawn extends Piece {
	private static final Map<PromotionPiece, BiFunction<PieceColor, ChessPosition, Piece>> PROMOTION_MAP = new HashMap<>();

	static {
		PROMOTION_MAP.put(PromotionPiece.Queen, Queen::new);
		PROMOTION_MAP.put(PromotionPiece.Rook, Rook::new);
		PROMOTION_MAP.put(PromotionPiece.Knight, Knight::new);
		PROMOTION_MAP.put(PromotionPiece.Bishop, Bishop::new);
	}

	public Pawn(PieceColor color, ChessPosition position) {
		super(color, position);
	}

	@Override
	public boolean isMovementValid(ChessPosition destinationPosition) {
		int fileDifference = Math.abs(destinationPosition.getFile().getFileNumber() - position.getFile().getFileNumber());
		int rankDifference = destinationPosition.getRank().getRankNumber() - position.getRank().getRankNumber();
		if (color == PieceColor.BLACK) {
			rankDifference *= -1;
		}
		return isAdvance(rankDifference, fileDifference) || isTake(rankDifference, fileDifference) || isTwoSquareMove(rankDifference, fileDifference);
	}

	private boolean isAdvance(int rankDifference, int fileDifference) {
		return rankDifference == 1 && fileDifference == 0;
	}

	private boolean isTake(int rankDifference, int fileDifference) {
		return rankDifference == 1 && fileDifference == 1;
	}

	private boolean isTwoSquareMove(int rankDifference, int fileDifference) {
		return fileDifference == 0 && rankDifference == 2 && isRankValidForTwoSquareMove();
	}

	private boolean isRankValidForTwoSquareMove() {
		return (color == PieceColor.BLACK && Rank.SEVENTH.equals(position.getRank())) || (color == PieceColor.WHITE && Rank.SECOND.equals(position.getRank()));
	}

	public Piece promote(PromotionPiece promotionPiece, File file) {
		return getPromotionPiece(promotionPiece, file);
	}

	private Piece getPromotionPiece(PromotionPiece promotionPiece, File file) {
		ChessPosition promotionPosition = getPromotionPosition(file);
		if (!isMovementValid(promotionPosition)) {
			throw new InvalidPromotionMoveException();
		}
		return PROMOTION_MAP.get(promotionPiece).apply(color, promotionPosition);
	}

	private ChessPosition getPromotionPosition(File file) {
		return new ChessPosition(file, PieceColor.WHITE.equals(color) ? Rank.EIGHTH : Rank.FIRST);
	}

	@Override
	public Piece copy() {
		return new Pawn(color, position);
	}

	public enum PromotionPiece {
		Queen, Rook, Knight, Bishop
	}
}
