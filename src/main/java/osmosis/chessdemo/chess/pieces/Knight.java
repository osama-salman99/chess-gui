package osmosis.chessdemo.chess.pieces;

import javafx.scene.image.Image;
import osmosis.chessdemo.chess.helper.ChessPosition;
import osmosis.chessdemo.functionailties.DraggableImageView;

import java.util.Objects;

public class Knight extends Piece {
	private static final Image BLACK_SYMBOL;
	private static final Image WHITE_SYMBOL;

	static {
		BLACK_SYMBOL = new Image(Objects.requireNonNull(Rook.class.getResource("/osmosis/chessdemo/images/pieces/black_knight.png")).toExternalForm());
		WHITE_SYMBOL = new Image(Objects.requireNonNull(Rook.class.getResource("/osmosis/chessdemo/images/pieces/white_knight.png")).toExternalForm());
	}

	private DraggableImageView symbol;

	public Knight(PieceColor color, ChessPosition position) {
		super(color, position);
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
		int rankDifference = Math.abs(destinationPosition.getRank() - position.getRank());
		return (fileDifference == 2 && rankDifference == 1) || (fileDifference == 1 && rankDifference == 2);
	}

	@Override
	public Piece copy() {
		return new Knight(color, position);
	}
}
