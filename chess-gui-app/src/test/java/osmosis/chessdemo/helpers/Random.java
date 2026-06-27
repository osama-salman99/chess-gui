package osmosis.chessdemo.helpers;

public class Random {
	public static int getRandomNumberBetween(int min, int max) {
		return (int) ((max - min + 1) * Math.random() + min);
	}
}
