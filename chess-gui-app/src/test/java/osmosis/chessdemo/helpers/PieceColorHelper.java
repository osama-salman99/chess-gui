package osmosis.chessdemo.helpers;

import osmosis.chessdemo.chess.pieces.PieceColor;

import static osmosis.chessdemo.chess.pieces.PieceColor.BLACK;
import static osmosis.chessdemo.chess.pieces.PieceColor.WHITE;

public class PieceColorHelper {
	public static PieceColor createRandomPieceColor() {
		return Math.random() >= 0.5 ? BLACK : WHITE;
	}
}
