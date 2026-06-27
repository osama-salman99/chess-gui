package osmosis.chessdemo.chess.board;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import osmosis.chessdemo.chess.exceptions.InvalidMoveException;
import osmosis.chessdemo.chess.exceptions.KingInCheckException;
import osmosis.chessdemo.chess.pieces.*;
import osmosis.chessdemo.chess.pieces.symbol.PieceSymbolProvider;
import osmosis.chessdemo.chess.position.ChessPosition;
import osmosis.chessdemo.chess.position.File;
import osmosis.chessdemo.chess.position.Rank;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BoardTest {
	private MockedStatic<PieceSymbolProvider> pieceSymbolProviderMockedStatic;
	private GridPane gridPane;

	@BeforeEach
	void setUp() {
		pieceSymbolProviderMockedStatic = Mockito.mockStatic(PieceSymbolProvider.class);
		gridPane = mock(GridPane.class);
		ObservableList<Node> children = FXCollections.observableArrayList();
		when(gridPane.getChildren()).thenReturn(children);
		when(gridPane.getPrefHeight()).thenReturn(500.0);
	}

	@AfterEach
	void tearDown() {
		pieceSymbolProviderMockedStatic.close();
	}

	// ── En passant ───────────────────────────────────────────────────────────

	@Test
	void givenWhitePawnDoubleAdvanceWhenBlackPawnCapturesEnPassantThenCapturedPawnIsRemoved() throws Exception {
		// White: e2->e4. Black pawn on d4. Black captures e.p. on e3 removing white pawn on e4.
		String fen = "4k3/8/8/8/3p4/8/4P3/4K3";
		Board board = Board.createChessBoard(fen, gridPane);

		// White pawn e2 double advance to e4
		Piece whitePawn = findPiece(board, File.E, Rank.SECOND);
		board.makeMove(whitePawn, new ChessPosition(File.E, Rank.FOURTH));

		// Black pawn d4 captures en passant to e3
		Piece blackPawn = findPiece(board, File.D, Rank.FOURTH);
		board.makeMove(blackPawn, new ChessPosition(File.E, Rank.THIRD));

		// White pawn on e4 must be gone
		assertFalse(board.getPieceAt(new ChessPosition(File.E, Rank.FOURTH)).isPresent());
		// Black pawn is now on e3
		assertTrue(board.getPieceAt(new ChessPosition(File.E, Rank.THIRD)).isPresent());
	}

	@Test
	void givenEnPassantOpportunityNotTakenImmediatelyWhenNextMovePlayedThenEnPassantNoLongerValid() throws Exception {
		String fen = "4k3/8/8/8/3p4/8/4P3/4K3";
		Board board = Board.createChessBoard(fen, gridPane);

		// White pawn e2 -> e4 (creates en passant target e3)
		Piece whitePawn = findPiece(board, File.E, Rank.SECOND);
		board.makeMove(whitePawn, new ChessPosition(File.E, Rank.FOURTH));

		// Black king moves (forfeits en passant right)
		Piece blackKing = findPiece(board, File.E, Rank.EIGHTH);
		board.makeMove(blackKing, new ChessPosition(File.D, Rank.EIGHTH));

		// White king moves
		Piece whiteKing = findPiece(board, File.E, Rank.FIRST);
		board.makeMove(whiteKing, new ChessPosition(File.F, Rank.FIRST));

		// Black pawn tries en passant - should fail now
		Piece blackPawn = findPiece(board, File.D, Rank.FOURTH);
		assertThrows(InvalidMoveException.class, () -> board.makeMove(blackPawn, new ChessPosition(File.E, Rank.THIRD)));
	}

	// ── Castling ─────────────────────────────────────────────────────────────

	@Test
	void givenClearPathWhenWhiteKingCastlesKingsideThenKingAndRookMoved() throws Exception {
		// Back rank clear between king and h-rook
		String fen = "4k3/8/8/8/8/8/8/4K2R";
		Board board = Board.createChessBoard(fen, gridPane);

		Piece king = findPiece(board, File.E, Rank.FIRST);
		board.makeMove(king, new ChessPosition(File.G, Rank.FIRST));

		assertTrue(board.getPieceAt(new ChessPosition(File.G, Rank.FIRST)).filter(p -> p instanceof King).isPresent());
		assertTrue(board.getPieceAt(new ChessPosition(File.F, Rank.FIRST)).filter(p -> p instanceof Rook).isPresent());
		assertFalse(board.getPieceAt(new ChessPosition(File.E, Rank.FIRST)).isPresent());
		assertFalse(board.getPieceAt(new ChessPosition(File.H, Rank.FIRST)).isPresent());
	}

	@Test
	void givenClearPathWhenWhiteKingCastlesQueensideThenKingAndRookMoved() throws Exception {
		String fen = "4k3/8/8/8/8/8/8/R3K3";
		Board board = Board.createChessBoard(fen, gridPane);

		Piece king = findPiece(board, File.E, Rank.FIRST);
		board.makeMove(king, new ChessPosition(File.C, Rank.FIRST));

		assertTrue(board.getPieceAt(new ChessPosition(File.C, Rank.FIRST)).filter(p -> p instanceof King).isPresent());
		assertTrue(board.getPieceAt(new ChessPosition(File.D, Rank.FIRST)).filter(p -> p instanceof Rook).isPresent());
		assertFalse(board.getPieceAt(new ChessPosition(File.E, Rank.FIRST)).isPresent());
		assertFalse(board.getPieceAt(new ChessPosition(File.A, Rank.FIRST)).isPresent());
	}

	@Test
	void givenClearPathWhenBlackKingCastlesKingsideThenKingAndRookMoved() throws Exception {
		String fen = "4k2r/8/8/8/8/8/8/4K3";
		Board board = Board.createChessBoard(fen, gridPane);

		// White moves first (dummy king move)
		Piece whiteKing = findPiece(board, File.E, Rank.FIRST);
		board.makeMove(whiteKing, new ChessPosition(File.D, Rank.FIRST));

		Piece blackKing = findPiece(board, File.E, Rank.EIGHTH);
		board.makeMove(blackKing, new ChessPosition(File.G, Rank.EIGHTH));

		assertTrue(board.getPieceAt(new ChessPosition(File.G, Rank.EIGHTH)).filter(p -> p instanceof King).isPresent());
		assertTrue(board.getPieceAt(new ChessPosition(File.F, Rank.EIGHTH)).filter(p -> p instanceof Rook).isPresent());
	}

	@Test
	void givenKingAlreadyMovedWhenCastlingAttemptedThenThrowException() throws Exception {
		String fen = "4k3/8/8/8/8/8/8/4K2R";
		Board board = Board.createChessBoard(fen, gridPane);

		// Move king and back
		Piece king = findPiece(board, File.E, Rank.FIRST);
		board.makeMove(king, new ChessPosition(File.F, Rank.FIRST));

		// Black moves
		Piece blackKing = findPiece(board, File.E, Rank.EIGHTH);
		board.makeMove(blackKing, new ChessPosition(File.D, Rank.EIGHTH));

		// King back to e1
		king = findPiece(board, File.F, Rank.FIRST);
		board.makeMove(king, new ChessPosition(File.E, Rank.FIRST));

		// Black moves again
		blackKing = findPiece(board, File.D, Rank.EIGHTH);
		board.makeMove(blackKing, new ChessPosition(File.E, Rank.EIGHTH));

		// Try to castle - should fail
		king = findPiece(board, File.E, Rank.FIRST);
		Piece finalKing = king;
		assertThrows(InvalidMoveException.class,
				() -> board.makeMove(finalKing, new ChessPosition(File.G, Rank.FIRST)));
	}

	@Test
	void givenKingInCheckWhenCastlingAttemptedThenThrowException() throws Exception {
		// White king on e1, black rook attacking e-file, rook on h1
		String fen = "4k3/4r3/8/8/8/8/8/4K2R";
		Board board = Board.createChessBoard(fen, gridPane);

		Piece king = findPiece(board, File.E, Rank.FIRST);
		assertThrows(KingInCheckException.class,
				() -> board.makeMove(king, new ChessPosition(File.G, Rank.FIRST)));
	}

	@Test
	void givenPathBlockedByPieceWhenCastlingAttemptedThenThrowException() throws Exception {
		// Knight on f1 blocks king-side castling path
		String fen = "4k3/8/8/8/8/8/8/4KN1R";
		Board board = Board.createChessBoard(fen, gridPane);

		Piece king = findPiece(board, File.E, Rank.FIRST);
		assertThrows(InvalidMoveException.class,
				() -> board.makeMove(king, new ChessPosition(File.G, Rank.FIRST)));
	}

	// ── Promotion ────────────────────────────────────────────────────────────

	@Test
	void givenDefaultPromotionChooserWhenPawnReachesBackRankThenPromotesToQueen() throws Exception {
		// White pawn on e7, black king on h8 (not blocking e8)
		String fen = "7k/4P3/8/8/8/8/8/4K3";
		Board board = Board.createChessBoard(fen, gridPane);

		Piece pawn = findPiece(board, File.E, Rank.SEVENTH);
		board.makeMove(pawn, new ChessPosition(File.E, Rank.EIGHTH));

		assertTrue(board.getPieceAt(new ChessPosition(File.E, Rank.EIGHTH)).filter(p -> p instanceof Queen).isPresent());
		assertFalse(board.getPieceAt(new ChessPosition(File.E, Rank.SEVENTH)).isPresent());
	}

	@Test
	void givenCustomPromotionChooserWhenPawnPromotesThenCustomPieceIsPlaced() throws Exception {
		String fen = "7k/4P3/8/8/8/8/8/4K3";
		Board board = Board.createChessBoard(fen, gridPane);
		board.setPromotionChooser(color -> Pawn.PromotionPiece.Knight);

		Piece pawn = findPiece(board, File.E, Rank.SEVENTH);
		board.makeMove(pawn, new ChessPosition(File.E, Rank.EIGHTH));

		assertTrue(board.getPieceAt(new ChessPosition(File.E, Rank.EIGHTH)).filter(p -> p instanceof Knight).isPresent());
	}

	// ── Checkmate / Stalemate ────────────────────────────────────────────────

	@Test
	void givenCheckmatePositionWhenFinalMovePlayedThenGameOverHandlerCalledWithCheckmate() throws Exception {
		// Scholar's mate setup: white to deliver checkmate on f7 with queen
		// Black king on e8, white queen on h5, white bishop on c4, black pieces block escape
		// FEN: r1bqkbnr/pppp1ppp/2n5/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR (after 1.e4 e5 2.Bc4 Nc6 3.Qh5)
		// Next move: Qxf7#
		String fen = "r1bqkbnr/pppp1ppp/2n5/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR";
		Board board = Board.createChessBoard(fen, gridPane);

		AtomicReference<String> message = new AtomicReference<>();
		board.setGameOverHandler(message::set);

		Piece queen = findPiece(board, File.H, Rank.FIFTH);
		board.makeMove(queen, new ChessPosition(File.F, Rank.SEVENTH));

		assertNotNull(message.get());
		assertTrue(message.get().contains("Checkmate"), "Expected checkmate message, got: " + message.get());
	}

	@Test
	void givenNoCheckmateThenGameOverHandlerNotCalled() throws Exception {
		String fen = "4k3/8/8/8/8/8/4P3/4K3";
		Board board = Board.createChessBoard(fen, gridPane);

		AtomicReference<String> message = new AtomicReference<>();
		board.setGameOverHandler(message::set);

		Piece pawn = findPiece(board, File.E, Rank.SECOND);
		board.makeMove(pawn, new ChessPosition(File.E, Rank.FOURTH));

		assertNull(message.get());
	}

	@Test
	void givenStalematePositionWhenFinalMovePlayedThenGameOverHandlerCalledWithStalemate() throws Exception {
		// Black king on a8, white queen on c5. Queen moves c5->c7.
		// c7 covers a7 (horizontal), b7 (horizontal), b8 (diagonal) — king not in check, no moves = stalemate.
		String fen = "k7/8/8/2Q5/8/8/8/4K3";
		Board board = Board.createChessBoard(fen, gridPane);

		AtomicReference<String> message = new AtomicReference<>();
		board.setGameOverHandler(message::set);

		Piece queen = findPiece(board, File.C, Rank.FIFTH);
		board.makeMove(queen, new ChessPosition(File.C, Rank.SEVENTH));

		assertNotNull(message.get());
		assertTrue(message.get().contains("Stalemate"), "Expected stalemate message, got: " + message.get());
	}

	// ── Reset ────────────────────────────────────────────────────────────────

	@Test
	void whenBoardResetThenPiecesAreBackToStartingPosition() throws Exception {
		String fen = "4k3/8/8/8/8/8/8/4K3";
		Board board = Board.createChessBoard(fen, gridPane);

		board.reset();

		// After reset, standard starting pieces should be present
		assertTrue(board.getPieceAt(new ChessPosition(File.E, Rank.FIRST)).filter(p -> p instanceof King && PieceColor.WHITE.equals(p.getColor())).isPresent());
		assertTrue(board.getPieceAt(new ChessPosition(File.E, Rank.EIGHTH)).filter(p -> p instanceof King && PieceColor.BLACK.equals(p.getColor())).isPresent());
		assertTrue(board.getPieceAt(new ChessPosition(File.E, Rank.SECOND)).filter(p -> p instanceof Pawn).isPresent());
	}

	// ── 50-move rule ─────────────────────────────────────────────────────────

	@Test
	void given50MoveRuleExceededWhenNextMovePlayedThenDrawReported() throws Exception {
		// Two kings and two rooks — no pawns, every rook move increments the clock
		// We just need the clock to hit 100 (50 full moves without pawn or capture)
		String fen = "k6r/8/8/8/8/8/8/K6R";
		Board board = Board.createChessBoard(fen, gridPane);

		AtomicReference<String> message = new AtomicReference<>();
		board.setGameOverHandler(message::set);

		// Play rook shuffles until the clock trips 100 half-moves
		// White rook h1↔g1, Black rook h8↔g8, repeat
		for (int i = 0; i < 50; i++) {
			File whiteRookFile = (i % 2 == 0) ? File.H : File.G;
			File whiteRookTarget = (i % 2 == 0) ? File.G : File.H;
			File blackRookFile = (i % 2 == 0) ? File.H : File.G;
			File blackRookTarget = (i % 2 == 0) ? File.G : File.H;

			Piece whiteRook = findPiece(board, whiteRookFile, Rank.FIRST);
			board.makeMove(whiteRook, new ChessPosition(whiteRookTarget, Rank.FIRST));

			if (message.get() != null) break;

			Piece blackRook = findPiece(board, blackRookFile, Rank.EIGHTH);
			board.makeMove(blackRook, new ChessPosition(blackRookTarget, Rank.EIGHTH));

			if (message.get() != null) break;
		}

		assertNotNull(message.get(), "Expected game-over message");
		assertTrue(message.get().contains("50-move"), "Expected 50-move draw, got: " + message.get());
	}

	// ── Insufficient material ────────────────────────────────────────────────

	@Test
	void givenKingVsKingWhenMovePlayedThenInsufficientMaterialReported() throws Exception {
		// White king captures the last black piece (a pawn on b2), leaving King vs King
		String fen = "7k/8/8/8/8/8/1p6/K7";
		Board board = Board.createChessBoard(fen, gridPane);

		AtomicReference<String> message = new AtomicReference<>();
		board.setGameOverHandler(message::set);

		Piece whiteKing = findPiece(board, File.A, Rank.FIRST);
		board.makeMove(whiteKing, new ChessPosition(File.B, Rank.SECOND));

		assertNotNull(message.get());
		assertTrue(message.get().contains("Insufficient material"), "Got: " + message.get());
	}

	@Test
	void givenKingAndBishopVsKingWhenMovePlayedThenInsufficientMaterialReported() throws Exception {
		// White king captures last pawn, leaving K+B vs K
		String fen = "7k/8/8/8/8/8/1p6/KB6";
		Board board = Board.createChessBoard(fen, gridPane);

		AtomicReference<String> message = new AtomicReference<>();
		board.setGameOverHandler(message::set);

		Piece whiteKing = findPiece(board, File.A, Rank.FIRST);
		board.makeMove(whiteKing, new ChessPosition(File.B, Rank.SECOND));

		assertNotNull(message.get());
		assertTrue(message.get().contains("Insufficient material"), "Got: " + message.get());
	}

	// ── Helpers ──────────────────────────────────────────────────────────────

	private Piece findPiece(Board board, File file, Rank rank) {
		return board.getPieceAt(new ChessPosition(file, rank))
				.orElseThrow(() -> new AssertionError("No piece at " + file + rank));
	}
}
