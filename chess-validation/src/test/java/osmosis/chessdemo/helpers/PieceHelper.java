package osmosis.chessdemo.helpers;

import osmosis.chessdemo.chess.pieces.*;
import osmosis.chessdemo.chess.position.ChessPosition;

import java.util.List;
import java.util.function.BiFunction;

import static osmosis.chessdemo.helpers.ChessPositionHelper.createRandomPosition;
import static osmosis.chessdemo.helpers.PieceColorHelper.createRandomPieceColor;
import static osmosis.chessdemo.helpers.Random.getRandomNumberBetween;

public class PieceHelper {
	private static final List<BiFunction<PieceColor, ChessPosition, Piece>> pieceCreators = List.of(Bishop::new, King::new, Knight::new, Pawn::new, Queen::new, Rook::new);

	public static Piece createRandomPiece() {
		return getRandomPieceCreator().apply(createRandomPieceColor(), createRandomPosition());
	}

	private static BiFunction<PieceColor, ChessPosition, Piece> getRandomPieceCreator() {
		return pieceCreators.get(getRandomNumberBetween(0, pieceCreators.size() - 1));
	}
}
