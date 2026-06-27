package osmosis.chessdemo.functionailties;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class MouseEventHandler implements EventHandler<MouseEvent> {
	private final Node node;
	private double lastMouseX;
	private double lastMouseY;
	private boolean dragging;
	private DragListener dragListener;

	public MouseEventHandler(Node node) {
		this.node = node;
		this.lastMouseX = 0;
		this.lastMouseY = 0;
		this.dragging = false;
		this.node.addEventHandler(MouseEvent.ANY, this);
	}

	public DragListener getDragListener() {
		return dragListener;
	}

	public void setDragListener(DragListener dragListener) {
		this.dragListener = dragListener;
	}

	@Override
	public void handle(MouseEvent event) {
		if (dragListener == null) {
			return;
		}
		node.toFront();
		if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
			node.setCursor(Cursor.OPEN_HAND);
		} else if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
			node.setCursor(Cursor.CROSSHAIR);
		} else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
			if (node.contains(event.getX(), event.getY())) {
				lastMouseX = event.getSceneX();
				lastMouseY = event.getSceneY();
				event.consume();
			}
		} else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
			node.setCursor(Cursor.CLOSED_HAND);
			if (dragging) {
				double deltaX = event.getSceneX() - this.lastMouseX;
				double deltaY = event.getSceneY() - this.lastMouseY;
				node.setTranslateX(node.getTranslateX() + deltaX);
				node.setTranslateY(node.getTranslateY() + deltaY);

				this.lastMouseX = event.getSceneX();
				this.lastMouseY = event.getSceneY();
				event.consume();
				dragListener.accept(node, DragEvent.Drag);
			} else {
				dragging = true;
				dragListener.accept(node, DragEvent.DragStart);
			}
		} else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
			node.setCursor(Cursor.OPEN_HAND);
			if (dragging) {
				dragging = false;
				event.consume();
				dragListener.accept(node, DragEvent.DragEnd);
			}
		}
	}

	public enum DragEvent {
		DragStart, Drag, DragEnd
	}
}
