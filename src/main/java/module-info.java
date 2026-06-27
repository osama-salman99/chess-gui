module osmosis.chessdemo {
	requires javafx.controls;
	requires javafx.fxml;
	requires lombok;
	requires org.slf4j;

	opens osmosis.chessdemo.controllers to javafx.fxml;
	exports osmosis.chessdemo;
}