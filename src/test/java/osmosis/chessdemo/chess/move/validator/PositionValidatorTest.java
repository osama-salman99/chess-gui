package osmosis.chessdemo.chess.move.validator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import osmosis.chessdemo.chess.board.BoardSquares;
import osmosis.chessdemo.chess.exceptions.InvalidMoveException;
import osmosis.chessdemo.chess.exceptions.KingInCheckException;
import osmosis.chessdemo.chess.exceptions.WrongTurnException;
import osmosis.chessdemo.chess.pieces.*;
import osmosis.chessdemo.chess.pieces.symbol.PieceSymbolProvider;
import osmosis.chessdemo.chess.position.ChessPosition;
import osmosis.chessdemo.chess.position.File;
import osmosis.chessdemo.chess.position.Rank;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PositionValidatorTest {
	private MockedStatic<PieceSymbolProvider> pieceSymbolProviderMockedStatic;

	@BeforeEach
	void setUp() {
		pieceSymbolProviderMockedStatic = Mockito.mockStatic(PieceSymbolProvider.class);
	}

	@AfterEach
	void tearDown() {
		pieceSymbolProviderMockedStatic.close();
	}

	// ── Turn ─────────────────────────────────────────────────────────────────

	@Test
	void givenBlackPieceOnWhiteTurnWhenValidateThenThrowWrongTurn() {
		Piece blackPawn = new Pawn(PieceColor.BLACK, pos(File.E, Rank.SEVENTH));
		PositionValidator v = validatorWith(List.of(blackPawn, whiteKing(), blackKing()), PieceColor.WHITE);

		assertThrows(WrongTurnException.class,
				() -> v.validateMove(blackPawn, pos(File.E, Rank.SIXTH)));
	}

	// ── Piece movement ────────────────────────────────────────────────────────

	@Test
	void givenRookMovingDiagonallyWhenValidateThenThrowInvalidMove() {
		Piece rook = new Rook(PieceColor.WHITE, pos(File.A, Rank.FIRST));
		PositionValidator v = validatorWith(List.of(rook, whiteKing(), blackKing()), PieceColor.WHITE);

		assertThrows(InvalidMoveException.class,
				() -> v.validateMove(rook, pos(File.B, Rank.SECOND)));
	}

	@Test
	void givenBishopMovingDiagonallyWhenValidateThenNoException() {
		Piece bishop = new Bishop(PieceColor.WHITE, pos(File.C, Rank.FIRST));
		PositionValidator v = validatorWith(List.of(bishop, whiteKing(), blackKing()), PieceColor.WHITE);

		assertDoesNotThrow(() -> v.validateMove(bishop, pos(File.E, Rank.THIRD)));
	}

	// ── Own-piece capture ─────────────────────────────────────────────────────

	@Test
	void givenOccupiedByOwnPieceWhenValidateThenThrowInvalidMove() {
		Piece rook = new Rook(PieceColor.WHITE, pos(File.A, Rank.FIRST));
		Piece knight = new Knight(PieceColor.WHITE, pos(File.A, Rank.THIRD));
		PositionValidator v = validatorWith(List.of(rook, knight, whiteKing(), blackKing()), PieceColor.WHITE);

		assertThrows(InvalidMoveException.class,
				() -> v.validateMove(rook, pos(File.A, Rank.THIRD)));
	}

	// ── Blocked path ─────────────────────────────────────────────────────────

	@Test
	void givenPieceBlockingRookPathWhenValidateThenThrowInvalidMove() {
		Piece rook = new Rook(PieceColor.WHITE, pos(File.A, Rank.FIRST));
		Piece blocker = new Pawn(PieceColor.WHITE, pos(File.A, Rank.THIRD));
		PositionValidator v = validatorWith(List.of(rook, blocker, whiteKing(), blackKing()), PieceColor.WHITE);

		assertThrows(InvalidMoveException.class,
				() -> v.validateMove(rook, pos(File.A, Rank.FIFTH)));
	}

	@Test
	void givenKnightJumpingOverPiecesWhenValidateThenNoException() {
		Piece knight = new Knight(PieceColor.WHITE, pos(File.B, Rank.FIRST));
		Piece blocker1 = new Pawn(PieceColor.WHITE, pos(File.B, Rank.SECOND));
		Piece blocker2 = new Pawn(PieceColor.WHITE, pos(File.C, Rank.SECOND));
		PositionValidator v = validatorWith(List.of(knight, blocker1, blocker2, whiteKing(), blackKing()), PieceColor.WHITE);

		assertDoesNotThrow(() -> v.validateMove(knight, pos(File.C, Rank.THIRD)));
	}

	// ── King in check ─────────────────────────────────────────────────────────

	@Test
	void givenMoveLeavesKingInCheckWhenValidateThenThrowKingInCheck() {
		// White king on e1, white rook on e4 (pinned by black rook on e8)
		Piece whiteRook = new Rook(PieceColor.WHITE, pos(File.E, Rank.FOURTH));
		Piece blackRook = new Rook(PieceColor.BLACK, pos(File.E, Rank.EIGHTH));
		Piece wk = new King(PieceColor.WHITE, pos(File.E, Rank.FIRST));
		Piece bk = new King(PieceColor.BLACK, pos(File.A, Rank.EIGHTH));
		PositionValidator v = validatorWith(List.of(whiteRook, blackRook, wk, bk), PieceColor.WHITE);

		// Moving the rook off the e-file would expose the king to the black rook
		assertThrows(KingInCheckException.class,
				() -> v.validateMove(whiteRook, pos(File.D, Rank.FOURTH)));
	}

	// ── En passant ────────────────────────────────────────────────────────────

	@Test
	void givenEnPassantTargetWhenPawnMovesToTargetThenNoException() {
		Piece whitePawn = new Pawn(PieceColor.WHITE, pos(File.D, Rank.FIFTH));
		ChessPosition epTarget = pos(File.E, Rank.SIXTH);
		PositionValidator v = validatorWith(List.of(whitePawn, whiteKing(), blackKing()),
				PieceColor.WHITE, Optional.of(epTarget));

		assertDoesNotThrow(() -> v.validateMove(whitePawn, epTarget));
	}

	@Test
	void givenNoEnPassantTargetWhenPawnCapturesDiagonallyToEmptySquareThenThrowInvalidMove() {
		Piece whitePawn = new Pawn(PieceColor.WHITE, pos(File.D, Rank.FIFTH));
		PositionValidator v = validatorWith(List.of(whitePawn, whiteKing(), blackKing()), PieceColor.WHITE);

		assertThrows(InvalidMoveException.class,
				() -> v.validateMove(whitePawn, pos(File.E, Rank.SIXTH)));
	}

	// ── Castling detection ────────────────────────────────────────────────────

	@Test
	void givenKingMovingTwoSquaresHorizontallyThenIsCastlingAttempt() {
		Piece king = new King(PieceColor.WHITE, pos(File.E, Rank.FIRST));
		PositionValidator v = validatorWith(List.of(king, blackKing()), PieceColor.WHITE);

		assertTrue(v.isCastlingAttempt(king, pos(File.G, Rank.FIRST)));
		assertTrue(v.isCastlingAttempt(king, pos(File.C, Rank.FIRST)));
	}

	@Test
	void givenKingMovingOneSquareThenIsNotCastlingAttempt() {
		Piece king = new King(PieceColor.WHITE, pos(File.E, Rank.FIRST));
		PositionValidator v = validatorWith(List.of(king, blackKing()), PieceColor.WHITE);

		assertFalse(v.isCastlingAttempt(king, pos(File.F, Rank.FIRST)));
	}

	// ── King-in-check detection ───────────────────────────────────────────────

	@Test
	void givenKingAttackedByRookWhenIsKingCurrentlyInCheckThenReturnTrue() {
		Piece wk = new King(PieceColor.WHITE, pos(File.E, Rank.FIRST));
		Piece blackRook = new Rook(PieceColor.BLACK, pos(File.E, Rank.EIGHTH));
		Piece bk = new King(PieceColor.BLACK, pos(File.A, Rank.EIGHTH));
		PositionValidator v = validatorWith(List.of(wk, blackRook, bk), PieceColor.WHITE);

		assertTrue(v.isKingCurrentlyInCheck());
	}

	@Test
	void givenKingNotAttackedWhenIsKingCurrentlyInCheckThenReturnFalse() {
		PositionValidator v = validatorWith(List.of(whiteKing(), blackKing()), PieceColor.WHITE);

		assertFalse(v.isKingCurrentlyInCheck());
	}

	// ── Helpers ───────────────────────────────────────────────────────────────

	private static ChessPosition pos(File file, Rank rank) {
		return new ChessPosition(file, rank);
	}

	private static Piece whiteKing() {
		return new King(PieceColor.WHITE, pos(File.E, Rank.FIRST));
	}

	private static Piece blackKing() {
		return new King(PieceColor.BLACK, pos(File.E, Rank.EIGHTH));
	}

	private static PositionValidator validatorWith(List<Piece> pieces, PieceColor turn) {
		return validatorWith(pieces, turn, Optional.empty());
	}

	private static PositionValidator validatorWith(List<Piece> pieces, PieceColor turn,
	                                                Optional<ChessPosition> enPassantTarget) {
		return new PositionValidator(new ValidationContext(
				new BoardSquares(pieces), turn, enPassantTarget,
				true, true, true, true));
	}
}
