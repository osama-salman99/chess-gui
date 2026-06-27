package osmosis.chessdemo.chess.exceptions;

public class InvalidFenException extends Exception {
	public InvalidFenException(String message) {
		super(message);
	}

	public InvalidFenException() {
		this("Invalid FEN format");
	}
}
