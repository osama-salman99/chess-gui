package osmosis.chessdemo.chess.pieces;


import osmosis.chessdemo.chess.helper.ChessPosition;
import osmosis.chessdemo.chess.helper.PieceDragListener;
import osmosis.chessdemo.functionailties.DragListener;
import osmosis.chessdemo.functionailties.DraggableImageView;

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

	public abstract boolean validMovement(ChessPosition destinationPosition);

	public abstract DraggableImageView getImageView();

	public DragListener getDragListener() {
		return getImageView().getDragListener();
	}

	public void setDragListener(PieceDragListener dragListener) {
		getImageView().setDragListener(dragListener);
	}

	public abstract Piece copy();

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" +
				"color=" + (color == PieceColor.BLACK ? "Black" : "White") +
				", position=" + position +
				'}';
	}

	public enum PieceColor {
		BLACK, WHITE
	}
}
