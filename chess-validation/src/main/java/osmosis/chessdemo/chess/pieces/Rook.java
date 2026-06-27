package osmosis.chessdemo.chess.pieces;

import osmosis.chessdemo.chess.position.ChessPosition;

public class Rook extends Piece {
	public Rook(PieceColor color, ChessPosition position) {
		super(color, position);
	}

	@Override
	public boolean isMovementValid(ChessPosition destinationPosition) {
		int fileDifference = Math.abs(destinationPosition.getFile().getFileNumber() - position.getFile().getFileNumber());
		int rankDifference = Math.abs(destinationPosition.getRank().getRankNumber() - position.getRank().getRankNumber());
		return (fileDifference == 0) ^ (rankDifference == 0);
	}

	@Override
	public Piece copy() {
		return new Rook(color, position);
	}
}
