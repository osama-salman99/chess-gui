package osmosis.chessdemo.chess.board;

import osmosis.chessdemo.chess.pieces.Piece;
import osmosis.chessdemo.chess.position.ChessPosition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static osmosis.chessdemo.chess.position.ChessNotationProvider.getChessNotation;

public class BoardSquares {
	private final Map<String, Piece> pieces;

	public BoardSquares(Collection<Piece> pieces) {
		this.pieces = new HashMap<>();
		pieces.forEach(this::put);
	}

	public void put(Piece piece) {
		pieces.put(getChessNotation(piece.getPosition()), piece);
	}

	public Optional<Piece> get(ChessPosition position) {
		return Optional.ofNullable(pieces.get(getChessNotation(position)));
	}

	public void removePieceOn(ChessPosition chessPosition) {
		pieces.remove(getChessNotation(chessPosition));
	}

	public Collection<Piece> getAll() {
		return pieces.values();
	}

	public void clear() {
		pieces.clear();
	}

	public BoardSquares copy() {
		return new BoardSquares(pieces.values());
	}
}
