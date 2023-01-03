import java.util.ArrayList;

import javafx.beans.InvalidationListener;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;

/*
 * Extension of the transition class to implement transitions that point at the same node
 */
public class SelfReferentialTransition extends Transition {
	
	public SelfReferentialTransition(State state, String methodName) {
		super();
		guardConditions = new ArrayList<String>();
		start = state;
		end = state;
		
		startID = state.getId();
		endID = startID;
		start.registerTransition(this); 
		end.registerTransition(this); 
		this.methodName = methodName;
		
		getChildren().add(new Line(0,0,0,0)); 
		getChildren().add(new Line(0,0,0,0));
		getChildren().add(new Line(0,0,0,0)); //final connector line--the last part that reconnects
		getChildren().add(new Label(methodName)); //label, maintaining same position in the child structure as normal transitions
		getChildren().add(new Line(0,0,0,0)); 
		getChildren().add(new Line(0,0,0,0)); //arrowheads
		getChildren().get(4).getTransforms().add(new Rotate(0,0,0));
		getChildren().get(5).getTransforms().add(new Rotate(0,0,0)); //add dummy rotates
		
		//create invalidation listener
		InvalidationListener listener = o -> {
			Bounds bounds = state.getBoundsInParent();
			double x = bounds.getMaxX(); //attaching to right-hand side
			double minY = bounds.getMinY();
			double maxY = bounds.getMaxY();
			
			Line line = (Line) getChildren().get(0);
			line.setStartX(x);
			line.setEndX(x+50);
			line.setStartY(minY);
			line.setEndY(minY); //first line going out
			
			line = (Line) getChildren().get(1);
			line.setStartX(x+50);
			line.setEndX(x+50);
			line.setStartY(minY);
			line.setEndY(maxY); //second line going up
			
			line = (Line) getChildren().get(2);
			line.setStartX(x+50);
			line.setEndX(x);
			line.setStartY(maxY);
			line.setEndY(maxY); //final line going back
			
			line = (Line) getChildren().get(4); //first arrowhead line
			line.getTransforms().remove(0); //remove old transform
			line.setStartX(x); 
			line.setEndX(x+20);
			line.setStartY(maxY);
			line.setEndY(maxY);
			line.getTransforms().add(new Rotate(30, x, maxY)); //rotate around new endpoint
			
			line = (Line) getChildren().get(5); //second arrowhead line
			line.getTransforms().remove(0); //remove old transform
			line.setStartX(x); 
			line.setEndX(x+20);
			line.setStartY(maxY);
			line.setEndY(maxY);
			line.getTransforms().add(new Rotate(330, x, maxY)); //rotate around new endpoint
			
			//attach label
			Bounds lineBounds = getChildren().get(1).getBoundsInLocal(); //have label by right-hand line
			Label arrowLabel = (Label) getChildren().get(3);
			arrowLabel.setLayoutX((lineBounds.getMinX()+lineBounds.getMaxX())/2+5);
			arrowLabel.setLayoutY((lineBounds.getMinY()+lineBounds.getMaxY())/2);
		};
		
		//listen to changes to node positions
		start.boundsInParentProperty().addListener(listener);
		
		//refresh listener to get initial values
		listener.invalidated(null);
	}
	
}
