package osmosis.chessdemo.chess.move.validator;

import osmosis.chessdemo.chess.board.BoardSquares;
import osmosis.chessdemo.chess.position.ChessPosition;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static osmosis.chessdemo.chess.position.File.getFile;
import static osmosis.chessdemo.chess.position.Rank.getRank;

public class MoveValidator {
	public static boolean isEmptyPath(ChessPosition startingPosition, ChessPosition destinationPosition, BoardSquares pieces) {
		int rankDifference = destinationPosition.getRank().getRankNumber() - startingPosition.getRank().getRankNumber();
		int fileDifference = destinationPosition.getFile().getFileNumber() - startingPosition.getFile().getFileNumber();
		if (rankDifference == 0) {
			return isEmptyPathHorizontal(startingPosition, destinationPosition, pieces);
		} else if (fileDifference == 0) {
			return isEmptyPathVertical(startingPosition, destinationPosition, pieces);
		} else {
			return isEmptyPathDiagonal(startingPosition, pieces, rankDifference, fileDifference);
		}
	}

	private static boolean isEmptyPathDiagonal(ChessPosition startingPosition, BoardSquares pieces, int rankDifference, int fileDifference) {
		int difference = Math.abs(rankDifference);
		int rankIncrement = rankDifference > 0 ? 1 : -1;
		int fileIncrement = fileDifference > 0 ? 1 : -1;
		for (int i = 1, file = startingPosition.getFile().getFileNumber() + fileIncrement, rank = startingPosition.getRank().getRankNumber() + rankIncrement; i < difference; i++, file += fileIncrement, rank += rankIncrement) {
			if (pieces.get(new ChessPosition(getFile(file), getRank(rank))).isPresent()) {
				return false;
			}
		}
		return true;
	}

	private static boolean isEmptyPathVertical(ChessPosition startingPosition, ChessPosition destinationPosition, BoardSquares pieces) {
		int file = startingPosition.getFile().getFileNumber();
		int minRank = min(startingPosition.getRank().getRankNumber(), destinationPosition.getRank().getRankNumber());
		int maxRank = max(startingPosition.getRank().getRankNumber(), destinationPosition.getRank().getRankNumber());
		for (int rank = minRank + 1; rank < maxRank; rank++) {
			if (pieces.get(new ChessPosition(getFile(file), getRank(rank))).isPresent()) {
				return false;
			}
		}
		return true;
	}

	private static boolean isEmptyPathHorizontal(ChessPosition startingPosition, ChessPosition destinationPosition, BoardSquares pieces) {
		int rank = startingPosition.getRank().getRankNumber();
		int minFile = min(startingPosition.getFile().getFileNumber(), destinationPosition.getFile().getFileNumber());
		int maxFile = max(startingPosition.getFile().getFileNumber(), destinationPosition.getFile().getFileNumber());
		for (int file = minFile + 1; file < maxFile; file++) {
			if (pieces.get(new ChessPosition(getFile(file), getRank(rank))).isPresent()) {
				return false;
			}
		}
		return true;
	}
}
