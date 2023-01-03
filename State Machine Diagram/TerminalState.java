import java.util.ArrayList;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class TerminalState extends State {
	public TerminalState(String id) {
		super();
		setId(id);
		transitions = new ArrayList<Transition>();		
		Circle backCircle = new Circle(10, Color.WHITE); //initial state is a plain black circle
		backCircle.setStroke(Color.BLACK);
		Circle frontCircle = new Circle(7,Color.BLACK);
		StackPane circles = new StackPane(backCircle,frontCircle);
		
		getChildren().add(circles);
	}
	
	public void highlight() {
		StackPane circles = (StackPane) getChildren().get(0);
		Circle c = (Circle) circles.getChildren().get(1);
		c.setFill(Color.LAWNGREEN);
	}
	
	public void stopHighlight() {
		StackPane circles = (StackPane) getChildren().get(0);
		Circle c = (Circle) circles.getChildren().get(1);
		c.setFill(Color.BLACK);
	}
	
	public void rename(String name) {
		setId(name); //no displayed name to change
	}
}

