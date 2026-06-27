package osmosis.chessdemo.controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import osmosis.chessdemo.chess.board.Board;
import osmosis.chessdemo.chess.move.initiator.MoveInitiator;
import osmosis.chessdemo.chess.pieces.Pawn;
import osmosis.chessdemo.chess.pieces.PieceColor;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainController extends Controller {
	public GridPane boardGridPane;
	private Board board;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		BackgroundSize backgroundSize = new BackgroundSize(boardGridPane.getPrefWidth(), boardGridPane.getPrefHeight(), true, true, true, true);
		Image backgroundImage = new Image(Objects.requireNonNull(getClass().getResource("/osmosis/chessdemo/images/boards/board_background.png")).toExternalForm());
		boardGridPane.setBackground(new Background(new BackgroundImage(backgroundImage, null, null, null, backgroundSize)));

		board = Board.createChessBoard(boardGridPane);
		MoveInitiator.getInstance().registerBoard(board);

		board.setPromotionChooser(this::showPromotionDialog);
		board.setGameOverHandler(this::handleGameOver);
	}

	private Pawn.PromotionPiece showPromotionDialog(PieceColor color) {
		List<Pawn.PromotionPiece> choices = Arrays.asList(Pawn.PromotionPiece.values());
		ChoiceDialog<Pawn.PromotionPiece> dialog = new ChoiceDialog<>(Pawn.PromotionPiece.Queen, choices);
		dialog.setTitle("Pawn Promotion");
		dialog.setHeaderText(null);
		dialog.setContentText("Choose a piece:");
		return dialog.showAndWait().orElse(Pawn.PromotionPiece.Queen);
	}

	private void handleGameOver(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Game Over");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
		board.reset();
	}
}
