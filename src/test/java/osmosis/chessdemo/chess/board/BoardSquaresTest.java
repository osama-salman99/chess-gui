package osmosis.chessdemo.chess.board;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import osmosis.chessdemo.chess.pieces.Piece;
import osmosis.chessdemo.chess.pieces.symbol.PieceSymbolProvider;
import osmosis.chessdemo.chess.position.ChessPosition;
import osmosis.chessdemo.chess.position.File;
import osmosis.chessdemo.chess.position.Rank;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static osmosis.chessdemo.helpers.PieceHelper.createRandomPiece;

class BoardSquaresTest {
	private MockedStatic<PieceSymbolProvider> pieceSymbolProviderMockedStatic;

	@BeforeEach
	void setUp() {
		pieceSymbolProviderMockedStatic = Mockito.mockStatic(PieceSymbolProvider.class);
	}

	@AfterEach
	void tearDown() {
		pieceSymbolProviderMockedStatic.close();
	}

	@Test
	void givenPiecesWhenCopyThenReturnNewInstance() {
		Piece piece = createRandomPiece();
		BoardSquares boardSquares = new BoardSquares(List.of(piece));

		BoardSquares copy = boardSquares.copy();

		assertNotSame(boardSquares, copy);
	}

	@Test
	void givenPiecesWhenCopyThenCopyHasSameSize() {
		Piece piece1 = createRandomPiece();
		Piece piece2 = createRandomPiece();
		BoardSquares boardSquares = new BoardSquares(List.of(piece1, piece2));

		BoardSquares copy = boardSquares.copy();

		assertEquals(boardSquares.getAll().size(), copy.getAll().size());
	}

	@Test
	void givenPieceWhenPutThenPieceIsRetrievable() {
		Piece piece = createRandomPiece();
		BoardSquares boardSquares = new BoardSquares(List.of());

		boardSquares.put(piece);

		assertTrue(boardSquares.get(piece.getPosition()).isPresent());
		assertEquals(piece, boardSquares.get(piece.getPosition()).get());
	}

	@Test
	void givenPieceWhenRemovePieceOnThenPieceIsAbsent() {
		Piece piece = createRandomPiece();
		BoardSquares boardSquares = new BoardSquares(List.of(piece));

		boardSquares.removePieceOn(piece.getPosition());

		assertFalse(boardSquares.get(piece.getPosition()).isPresent());
	}

	@Test
	void givenPiecesWhenClearThenGetAllIsEmpty() {
		Piece piece1 = createRandomPiece();
		Piece piece2 = createRandomPiece();
		BoardSquares boardSquares = new BoardSquares(List.of(piece1, piece2));

		boardSquares.clear();

		assertTrue(boardSquares.getAll().isEmpty());
	}

	@Test
	void givenEmptyPositionWhenGetThenReturnEmpty() {
		BoardSquares boardSquares = new BoardSquares(List.of());

		assertFalse(boardSquares.get(new ChessPosition(File.E, Rank.FOURTH)).isPresent());
	}
}
