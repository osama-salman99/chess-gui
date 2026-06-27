package osmosis.chessdemo.chess.fen;

import osmosis.chessdemo.chess.exceptions.InvalidFenException;

import java.util.Arrays;
import java.util.Objects;

import static java.lang.Character.getNumericValue;
import static java.lang.Character.isLetter;

public class FenValidator {
	private static final String FEN_FORMAT_REGEX = "([rnbqkpRNBQKP1-8]+/){7}[rnbqkpRNBQKP1-8]+";

	public static void validate(String fen) throws InvalidFenException {
		validateFormat(fen);
		String[] ranks = fen.split("/");
		validateNumberOfPieces(ranks);
		validatePiecesPlacement(ranks);
	}

	private static void validateFormat(String fen) throws InvalidFenException {
		if (Objects.isNull(fen) || !fen.matches(FEN_FORMAT_REGEX)) {
			throw new InvalidFenException("Invalid FEN format");
		}
	}

	private static void validateNumberOfPieces(String[] ranks) throws InvalidFenException {
		if (Arrays.stream(ranks).anyMatch(rank -> getNumberOfPieces(rank) != 8)) {
			throw new InvalidFenException("Invalid number of pieces in rank");
		}
	}

	private static void validatePiecesPlacement(String[] ranks) throws InvalidFenException {
		validateNoPawns(ranks[0]);
		validateNoPawns(ranks[7]);
	}

	private static void validateNoPawns(String rank) throws InvalidFenException {
		if (rank.contains("p") || rank.contains("P")) {
			throw new InvalidFenException("Invalid piece placement");
		}
	}

	private static int getNumberOfPieces(String rank) {
		return rank.chars().map(c -> (isLetter(c)) ? 1 : getNumericValue(c)).sum();
	}
}
