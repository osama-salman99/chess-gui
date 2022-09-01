package osmosis.chessdemo.chess.helper;

import osmosis.chessdemo.chess.pieces.Piece;
import osmosis.chessdemo.functionailties.DragListener;

public abstract class PieceDragListener implements DragListener {
	private Piece piece;

	public PieceDragListener(Piece piece) {
		this.piece = piece;
	}

	public Piece getPiece() {
		return piece;
	}

	public void setPiece(Piece piece) {
		this.piece = piece;
	}
}
