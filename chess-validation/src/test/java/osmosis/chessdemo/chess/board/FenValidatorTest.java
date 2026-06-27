package osmosis.chessdemo.chess.board;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import osmosis.chessdemo.chess.exceptions.InvalidFenException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static osmosis.chessdemo.chess.fen.FenValidator.validate;

class FenValidatorTest {

	public static Stream<Arguments> validFen() {
		return Stream.of(Arguments.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"), Arguments.of("rnbqkbnr/ppppppp1/7p/8/8/8/PPPPPPPP/RNBQKBNR"), Arguments.of("r1b1k1nr/p2p1pNp/n2B4/1p1NP2P/6P1/3P1Q2/P1P1K3/q5b1"), Arguments.of("8/8/8/4p1K1/2k1P3/8/8/8"), Arguments.of("4k2r/6r1/8/8/8/8/3R4/R3K3"), Arguments.of("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR"), Arguments.of("8/5k2/3p4/1p1Pp2p/pP2Pp1P/P4P1K/8/8"));
	}

	public static Stream<Arguments> invalidFenPiecePlacement() {
		return Stream.of(Arguments.of("rnbqkbnp/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"), Arguments.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNP"), Arguments.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNp"), Arguments.of("rnbqkbnP/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"));
	}

	public static Stream<Arguments> invalidFenNumberOfPieces() {
		return Stream.of(Arguments.of("rnbqkbnr/ppppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"), Arguments.of("rnbqkbnr/pppppppp/7/8/8/8/PPPPPPPP/RNBQKBNR"), Arguments.of("rnbqkbnr/ppppppppp/p7/8/8/8/PPPPPPPP/RNBQKBNR"), Arguments.of("rnbqkbnr/pppppppp/p7/7/8/8/PPPPPPPP/RNBQKBNR"), Arguments.of("rnbqkbnr/pppppppp/p5ppp/7/8/8/PPPPPPPP/RNBQKBNR"));
	}

	public static Stream<Arguments> invalidFenFormat() {
		return Stream.of(Arguments.of("invalid"), Arguments.of("rnbqkbnrpppppppp8888PPPPPPPPRNBQKBNR"), Arguments.of("///////"), Arguments.of("rnbqkbnr/pppppppp//8/8/8/PPPPPPPP/RNBQKBNR"), Arguments.of("/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"), Arguments.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/"), Arguments.of("rnbqkbnr/pppppppp/s7/8/8/8/PPPPPPPP/RNBQKBNR"));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@MethodSource("invalidFenFormat")
	void givenInvalidFenFormatWhenValidateThenThrowException(String invalidFen) {
		InvalidFenException exception = assertThrows(InvalidFenException.class, () -> validate(invalidFen));
		assertEquals("Invalid FEN format", exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("invalidFenNumberOfPieces")
	void givenInvalidNumberOfPiecesInRowFenWhenValidateThenThrowException(String invalidFen) {
		InvalidFenException exception = assertThrows(InvalidFenException.class, () -> validate(invalidFen));
		assertEquals("Invalid number of pieces in rank", exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("invalidFenPiecePlacement")
	void givenInvalidPiecePlacementFenWhenValidateThenThrowException(String invalidFen) {
		InvalidFenException exception = assertThrows(InvalidFenException.class, () -> validate(invalidFen));
		assertEquals("Invalid piece placement", exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("validFen")
	void givenValidFenWhenValidateThenValidationSucceeds(String validFen) {
		assertDoesNotThrow(() -> validate(validFen));
	}
}
