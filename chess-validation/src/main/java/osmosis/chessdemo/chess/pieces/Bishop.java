package osmosis.chessdemo.chess.pieces;

import osmosis.chessdemo.chess.position.ChessPosition;

public class Bishop extends Piece {
	public Bishop(PieceColor color, ChessPosition position) {
		super(color, position);
	}

	@Override
	public boolean isMovementValid(ChessPosition destinationPosition) {
		int fileDifference = Math.abs(destinationPosition.getFile().getFileNumber() - position.getFile().getFileNumber());
		int rankDifference = Math.abs(destinationPosition.getRank().getRankNumber() - position.getRank().getRankNumber());
		if (fileDifference == 0 || rankDifference == 0) {
			return false;
		}
		return rankDifference == fileDifference;
	}

	@Override
	public Piece copy() {
		return new Bishop(color, position);
	}
}
