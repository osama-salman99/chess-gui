package osmosis.chessdemo.chess.move.initiator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmosis.chessdemo.chess.board.Board;
import osmosis.chessdemo.chess.pieces.Piece;
import osmosis.chessdemo.chess.position.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MoveInitiator {
	private static final Logger log = LoggerFactory.getLogger(MoveInitiator.class);
	private static final MoveInitiator instance = new MoveInitiator();
	private final Collection<Board> boards = new ArrayList<>();

	public static MoveInitiator getInstance() {
		return instance;
	}

	public void initiateMove(Piece piece, ChessPosition destinationPosition) {
		boards.forEach(board -> {
			try {
				board.makeMove(piece, destinationPosition);
			} catch (Exception exception) {
				log.debug("Move rejected by board: {}", exception.getMessage());
			}
		});
	}

	public void registerBoard(Board board) {
		boards.add(board);
	}
}
