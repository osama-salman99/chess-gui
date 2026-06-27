package osmosis.chessdemo.chess.helper;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import lombok.AllArgsConstructor;
import lombok.Getter;
import osmosis.chessdemo.chess.move.initiator.MoveInitiator;
import osmosis.chessdemo.chess.pieces.Piece;
import osmosis.chessdemo.chess.position.ChessPosition;
import osmosis.chessdemo.functionailties.DragListener;
import osmosis.chessdemo.functionailties.MouseEventHandler;

import static osmosis.chessdemo.chess.position.File.getFile;
import static osmosis.chessdemo.chess.position.Rank.getRank;

public class PieceDragListener implements DragListener {
	private Piece piece;

	public PieceDragListener(Piece piece) {
		this.piece = piece;
	}

	private static Point2D getCenterPoint(Bounds bounds) {
		return new Point2D((bounds.getMinX() + bounds.getMaxX()) / 2, (bounds.getMinY() + bounds.getMaxY()) / 2);
	}

	private static Location getLocation(Node node) {
		Bounds bounds = node.localToScene(node.getBoundsInLocal());
		Point2D center = getCenterPoint(bounds);
		double sideLength = node.getBoundsInLocal().getHeight();
		int x = getIndex(center.getX(), sideLength);
		int y = getIndex(center.getY(), sideLength);
		return new Location(x, y);
	}

	private static int getIndex(double variable, double sideLength) {
		return (int) (variable / sideLength);
	}

	public Piece getPiece() {
		return piece;
	}

	public void setPiece(Piece piece) {
		this.piece = piece;
	}

	@Override
	public void accept(Node node, MouseEventHandler.DragEvent dragEvent) {
		if (!dragEvent.equals(MouseEventHandler.DragEvent.DragEnd)) {
			return;
		}
		Location location = getLocation(node);
		ChessPosition destinationPosition = new ChessPosition(getFile(location.getX() + 1), getRank(location.getY() + 1).getInverse());
		MoveInitiator.getInstance().initiateMove(getPiece(), destinationPosition);
	}

	@Getter
	@AllArgsConstructor
	private static class Location {
		private final int x;
		private final int y;
	}
}
