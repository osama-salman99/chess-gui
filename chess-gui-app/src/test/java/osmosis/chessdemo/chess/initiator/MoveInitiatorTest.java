package osmosis.chessdemo.chess.initiator;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import osmosis.chessdemo.chess.board.Board;
import osmosis.chessdemo.chess.move.initiator.MoveInitiator;
import osmosis.chessdemo.chess.pieces.Piece;
import osmosis.chessdemo.chess.position.ChessPosition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static osmosis.chessdemo.helpers.ChessPositionHelper.createRandomPosition;
import static osmosis.chessdemo.helpers.PieceHelper.createRandomPiece;

class MoveInitiatorTest {

	@Test
	void whenGetInstanceMultipleTimesThenReturnSameInstance() {
		MoveInitiator instance = MoveInitiator.getInstance();
		for (int i = 0; i < 10; i++) {
			assertEquals(instance, MoveInitiator.getInstance());
		}
	}

	@Test
	void givenBoardsWhenInitiateMoveThenCallMakeMoveOnRegisteredAllBoards() {
		Board board1 = Mockito.mock(Board.class);
		Board board2 = Mockito.mock(Board.class);
		MoveInitiator instance = MoveInitiator.getInstance();
		instance.registerBoard(board1);
		instance.registerBoard(board2);

		Piece piece = createRandomPiece();
		ChessPosition destinationPosition = createRandomPosition();
		instance.initiateMove(piece, destinationPosition);
		verify(board1, times(1)).makeMove(piece, destinationPosition);
		verify(board2, times(1)).makeMove(piece, destinationPosition);
	}

	@Test
	void givenOneBoardThrowsExceptionWhenInitiateMoveThenCallMakeMoveOnRegisteredAllRemainingBoards() {
		Board board1 = Mockito.mock(Board.class);
		Board board2 = Mockito.mock(Board.class);

		doThrow(new RuntimeException()).when(board1).makeMove(any(), any());

		MoveInitiator instance = MoveInitiator.getInstance();
		instance.registerBoard(board1);
		instance.registerBoard(board2);

		Piece piece = createRandomPiece();
		ChessPosition destinationPosition = createRandomPosition();
		instance.initiateMove(piece, destinationPosition);
		verify(board1, times(1)).makeMove(piece, destinationPosition);
		verify(board2, times(1)).makeMove(piece, destinationPosition);
	}
}