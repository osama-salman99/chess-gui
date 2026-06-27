package osmosis.chessdemo.chess.pieces;

import osmosis.chessdemo.chess.position.ChessPosition;

public class Knight extends Piece {
	public Knight(PieceColor color, ChessPosition position) {
		super(color, position);
	}

	@Override
	public boolean isMovementValid(ChessPosition destinationPosition) {
		int fileDifference = Math.abs(destinationPosition.getFile().getFileNumber() - position.getFile().getFileNumber());
		int rankDifference = Math.abs(destinationPosition.getRank().getRankNumber() - position.getRank().getRankNumber());
		return (fileDifference == 2 && rankDifference == 1) || (fileDifference == 1 && rankDifference == 2);
	}

	@Override
	public Piece copy() {
		return new Knight(color, position);
	}
}
