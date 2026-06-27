package osmosis.chessdemo.chess.exceptions;

public class KingInCheckException extends InvalidMoveException {
	public KingInCheckException() {
		super("One or more kings are in check");
	}
}
