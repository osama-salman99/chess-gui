package osmosis.chessdemo.chess.position;

public class ChessNotationProvider {
	public static String getChessNotation(ChessPosition chessPosition) {
		return getChessNotation(chessPosition.getFile(), chessPosition.getRank());
	}

	public static String getChessNotation(File file, Rank rank) {
		return file.getNotation() + rank.getNotation();
	}
}
