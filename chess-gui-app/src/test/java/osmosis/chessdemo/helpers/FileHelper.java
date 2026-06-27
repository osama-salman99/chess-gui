package osmosis.chessdemo.helpers;

import osmosis.chessdemo.chess.position.File;

import static osmosis.chessdemo.helpers.Random.getRandomNumberBetween;

public class FileHelper {
	public static File getRandomFile() {
		return File.getFile(getRandomNumberBetween(1, 8));
	}
}
