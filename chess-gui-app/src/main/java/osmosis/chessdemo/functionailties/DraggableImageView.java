package osmosis.chessdemo.functionailties;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import osmosis.chessdemo.chess.helper.PieceDragListener;

public class DraggableImageView extends ImageView {
	private final MouseEventHandler mouseEventHandler;
	private final Image image;

	public DraggableImageView(Image image) {
		super(image);
		this.image = image;
		this.mouseEventHandler = new MouseEventHandler(this);
	}

	public PieceDragListener getDragListener() {
		return (PieceDragListener) mouseEventHandler.getDragListener();
	}

	public void setDragListener(PieceDragListener dragListener) {
		mouseEventHandler.setDragListener(dragListener);
	}

	public DraggableImageView copy() {
		DraggableImageView draggableImageView = new DraggableImageView(image);
		draggableImageView.setDragListener(getDragListener());
		return draggableImageView;
	}
}
