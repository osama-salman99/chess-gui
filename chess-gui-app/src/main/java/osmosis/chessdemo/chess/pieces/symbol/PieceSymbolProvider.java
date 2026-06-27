package osmosis.chessdemo.chess.pieces.symbol;

import javafx.scene.image.Image;

import java.util.Objects;

public class PieceSymbolProvider {
	public static Image getBlackPieceSymbol(String pieceName) {
		return new Image(Objects.requireNonNull(PieceSymbolProvider.class.getResource(String.format("/osmosis/chessdemo/images/pieces/black_%s.png", pieceName))).toExternalForm());
	}

	public static Image getWhitePieceSymbol(String pieceName) {
		return new Image(Objects.requireNonNull(PieceSymbolProvider.class.getResource(String.format("/osmosis/chessdemo/images/pieces/white_%s.png", pieceName))).toExternalForm());
	}
}
