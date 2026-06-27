package osmosis.chessdemo.chess.position;

import osmosis.chessdemo.chess.exceptions.InvalidNumberException;
import osmosis.chessdemo.chess.exceptions.RankCalculationException;

public enum Rank {
	FIRST(1), SECOND(2), THIRD(3), FOURTH(4), FIFTH(5), SIXTH(6), SEVENTH(7), EIGHTH(8);
	private static final Rank[] RANKS = new Rank[]{FIRST, SECOND, THIRD, FOURTH, FIFTH, SIXTH, SEVENTH, EIGHTH};

	private final int rankNumber;

	Rank(int rankNumber) {
		this.rankNumber = rankNumber;
	}

	public static Rank getRank(int rankNumber) {
		if (rankNumber < 1 || rankNumber > 8) {
			throw new InvalidNumberException(String.format("Invalid rank number [%s]", rankNumber));
		}
		return RANKS[rankNumber - 1];
	}

	public int getRankNumber() {
		return rankNumber;
	}

	public Rank nextRank() {
		return add(1);
	}

	public Rank add(int ranks) {
		int newRank = getRankNumber() + ranks;
		try {
			return getRank(newRank);
		} catch (InvalidNumberException e) {
			throw new RankCalculationException(String.format("Rank calculation result out of bounds [%d + %d = %s]", getRankNumber(), ranks, newRank));
		}
	}

	public Rank subtract(int ranks) {
		return add(-ranks);
	}

	public Rank getInverse() {
		return getRank(9 - getRankNumber());
	}

	public String getNotation() {
		return String.valueOf(rankNumber);
	}
}
