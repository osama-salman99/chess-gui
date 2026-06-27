package osmosis.chessdemo.chess.exceptions;

public class InvalidMoveException extends RuntimeException {
	public InvalidMoveException(String message) {
		super(message);
	}
}
