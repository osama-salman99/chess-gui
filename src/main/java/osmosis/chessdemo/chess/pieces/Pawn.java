package osmosis.chessdemo.chess.pieces;

import javafx.scene.image.Image;
import osmosis.chessdemo.chess.helper.ChessPosition;
import osmosis.chessdemo.chess.helper.PieceDragListener;
import osmosis.chessdemo.functionailties.DraggableImageView;

import java.util.Objects;

public class Pawn extends Piece {
	private static final Image BLACK_SYMBOL;
	private static final Image WHITE_SYMBOL;

	static {
		BLACK_SYMBOL = new Image(Objects.requireNonNull(Rook.class.getResource("/osmosis/chessdemo/images/pieces/black_pawn.png")).toExternalForm());
		WHITE_SYMBOL = new Image(Objects.requireNonNull(Rook.class.getResource("/osmosis/chessdemo/images/pieces/white_pawn.png")).toExternalForm());
	}

	private DraggableImageView symbol;

	public Pawn(PieceColor color, ChessPosition chessPosition) {
		super(color, chessPosition);
		if (color == PieceColor.BLACK) {
			symbol = new DraggableImageView(BLACK_SYMBOL);
		} else {
			symbol = new DraggableImageView(WHITE_SYMBOL);
		}
	}

	public DraggableImageView getImageView() {
		return symbol = symbol.copy();
	}

	@Override
	public boolean validMovement(ChessPosition destinationPosition) {
		int fileDifference = Math.abs(destinationPosition.getFile() - position.getFile());
		int rankDifference = destinationPosition.getRank() - position.getRank();
		if (color == PieceColor.BLACK) {
			rankDifference *= -1;
		}
		if (fileDifference == 0 && rankDifference == 2) {
			return true;
		}
		return fileDifference >= 0 && fileDifference <= 1
				&& (rankDifference == 1);
	}

	public Piece promote(Promotion promotion, ChessPosition position) {
		Piece piece;
		switch (promotion) {
			case Queen:
				piece = new Queen(color, position);
				break;
			case Rook:
				piece = new Rook(color, position);
				break;
			case Knight:
				piece = new Knight(color, position);
				break;
			case Bishop:
				piece = new Bishop(color, position);
				break;
			default:
				throw new IllegalArgumentException("Unknown argument " + promotion);
		}
		PieceDragListener dragListener = (PieceDragListener) getDragListener();
		dragListener.setPiece(piece);
		piece.setDragListener(dragListener);
		return piece;
	}

	@Override
	public Piece copy() {
		return new Pawn(color, position);
	}

	public enum Promotion {
		Queen, Rook, Knight, Bishop
	}
}
