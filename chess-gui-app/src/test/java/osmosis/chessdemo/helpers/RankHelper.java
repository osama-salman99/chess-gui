package osmosis.chessdemo.helpers;

import osmosis.chessdemo.chess.position.Rank;

import static osmosis.chessdemo.helpers.Random.getRandomNumberBetween;

public class RankHelper {
	public static Rank getRandomRank() {
		return Rank.getRank(getRandomNumberBetween(1, 8));
	}
}
