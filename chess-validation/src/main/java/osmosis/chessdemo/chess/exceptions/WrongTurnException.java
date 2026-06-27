package osmosis.chessdemo.chess.exceptions;

import osmosis.chessdemo.chess.pieces.PieceColor;

public class WrongTurnException extends InvalidMoveException {
	public WrongTurnException(PieceColor currentTurn) {
		super(String.format("Piece color does not match current turn (%s)", currentTurn));
	}
}
