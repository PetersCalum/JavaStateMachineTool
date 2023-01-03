import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class InitialState extends State {

	public InitialState(String id) {
		super();
		setId(id);
		transitions = new ArrayList<Transition>();		
		getChildren().add((new Circle(10, Color.BLACK))); //initial state is a plain black circle
	}
	
	public void highlight() {
		Circle c = (Circle) getChildren().get(0);
		c.setFill(Color.LAWNGREEN);
	}
	
	public void stopHighlight() {
		Circle c = (Circle) getChildren().get(0);
		c.setFill(Color.BLACK);
	}
	
	public void rename(String name) {
		setId(name); //no displayed name to change
	}
}
