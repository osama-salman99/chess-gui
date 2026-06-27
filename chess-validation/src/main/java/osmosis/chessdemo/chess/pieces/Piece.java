package osmosis.chessdemo.chess.pieces;

import osmosis.chessdemo.chess.position.ChessPosition;

public abstract class Piece {
	protected final PieceColor color;
	protected ChessPosition position;

	protected Piece(PieceColor color, ChessPosition position) {
		this.color = color;
		this.position = position;
	}

	public PieceColor getColor() {
		return color;
	}

	public ChessPosition getPosition() {
		return position;
	}

	public void setPosition(ChessPosition chessPosition) {
		this.position = chessPosition;
	}

	public abstract boolean isMovementValid(ChessPosition destinationPosition);

	public abstract Piece copy();

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{color=" + (color == PieceColor.BLACK ? "Black" : "White") + ", position=" + position + '}';
	}
}
