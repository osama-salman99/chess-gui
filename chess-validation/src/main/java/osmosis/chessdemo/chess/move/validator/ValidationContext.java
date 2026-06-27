package osmosis.chessdemo.chess.move.validator;

import osmosis.chessdemo.chess.board.BoardSquares;
import osmosis.chessdemo.chess.pieces.PieceColor;
import osmosis.chessdemo.chess.position.ChessPosition;

import java.util.Optional;

public record ValidationContext(
		BoardSquares boardSquares,
		PieceColor currentTurn,
		Optional<ChessPosition> enPassantTarget,
		boolean whiteKingSideCastle,
		boolean whiteQueenSideCastle,
		boolean blackKingSideCastle,
		boolean blackQueenSideCastle
) {}
