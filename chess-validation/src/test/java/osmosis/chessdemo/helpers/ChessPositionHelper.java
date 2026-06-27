package osmosis.chessdemo.helpers;

import osmosis.chessdemo.chess.position.ChessPosition;

import static osmosis.chessdemo.helpers.FileHelper.getRandomFile;
import static osmosis.chessdemo.helpers.RankHelper.getRandomRank;

public class ChessPositionHelper {
	public static ChessPosition createRandomPosition() {
		return new ChessPosition(getRandomFile(), getRandomRank());
	}


}
