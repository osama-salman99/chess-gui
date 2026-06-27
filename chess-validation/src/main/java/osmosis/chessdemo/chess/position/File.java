package osmosis.chessdemo.chess.position;

import osmosis.chessdemo.chess.exceptions.FileCalculationException;
import osmosis.chessdemo.chess.exceptions.InvalidNumberException;
import osmosis.chessdemo.chess.exceptions.UnexpectedException;

public enum File {
	A(1), B(2), C(3), D(4), E(5), F(6), G(7), H(8);
	private static final File[] FILES = new File[]{A, B, C, D, E, F, G, H};

	private final int fileNumber;

	File(int fileNumber) {
		this.fileNumber = fileNumber;
	}

	public static File getFile(int fileNumber) {
		if (fileNumber < 1 || fileNumber > 8) {
			throw new InvalidNumberException(String.format("Invalid file number [%s]", fileNumber));
		}
		return FILES[fileNumber - 1];
	}

	public int getFileNumber() {
		return fileNumber;
	}

	public File nextFile() {
		return add(1);
	}

	public File add(int files) {
		int newFile = getFileNumber() + files;
		try {
			return getFile(newFile);
		} catch (InvalidNumberException e) {
			throw new FileCalculationException(String.format("File calculation result out of bounds [%d + %d = %s]", getFileNumber(), files, newFile));
		}
	}

	public File subtract(int files) {
		return add(-files);
	}

	public File getInverse() {
		try {
			return getFile(9 - getFileNumber());
		} catch (InvalidNumberException e) {
			throw new UnexpectedException(e);
		}
	}

	public int difference(File file) {
		return this.getFileNumber() - file.getFileNumber();
	}

	public int absoluteDifference(File file) {
		return Math.abs(difference(file));
	}

	public String getNotation() {
		return name().toLowerCase();
	}
}
