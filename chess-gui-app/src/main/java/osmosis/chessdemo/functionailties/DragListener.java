package osmosis.chessdemo.functionailties;

import javafx.scene.Node;
import osmosis.chessdemo.functionailties.MouseEventHandler.DragEvent;

public interface DragListener {
	void accept(Node node, DragEvent dragEvent);
}
