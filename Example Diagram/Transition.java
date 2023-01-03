import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;

public class Transition extends Group {
	protected String startID, endID, methodName;
	protected List<String> guardConditions; //list of guard conditions
	protected State start, end;
	protected static int numTransitions = 0; //number of transitions instantiated
	protected final int thisTransition; //what number is THIS transition
	
	public Transition() {
		super();
		thisTransition = numTransitions;
		numTransitions++; //update class variables
	}
	
	public Transition(State start, State end, String methodName) {
		super();
		thisTransition = numTransitions;
		numTransitions++; //update class variables
		guardConditions = new ArrayList<String>();
		
		startID = start.getId();
		this.start = start;
		start.registerTransition(this);
		endID = end.getId();
		this.end = end;
		end.registerTransition(this);
		this.methodName = methodName;		
		
		getChildren().add(new Line(0,0,0,0)); //main connector line
		getChildren().add(new Line(0,0,0,0));
		getChildren().add(new Line(0,0,0,0)); //arrowheads
		getChildren().get(1).getTransforms().add(new Rotate(0,0,0));
		getChildren().get(2).getTransforms().add(new Rotate(0,0,0)); //add dummy rotates
		getChildren().add(new Label(methodName));
		
		//implement listener to connect line to nodes and keep it attached when moved
		
		//create listener
		InvalidationListener listener = o -> { //when observable o is invalidated
			
			boolean overlap = false;
			List<Transition> overlaps = new ArrayList<Transition>();
			for (Transition t : start.getTransitions()) {
				if(end.getTransitions().contains(t) && !this.equals(t)) {
					overlaps.add(t);
					overlap = true;
				}
			}
			if(!overlap) //no overlap, calculate where to attach and update
				nonOverlapUpdate(start.getBoundsInParent(), end.getBoundsInParent());
			else 
				overlapUpdate(start.getBoundsInParent(), end.getBoundsInParent(), overlaps);
			
			updateArrowhead(); //update the arrowhead positions
			
			//attach label
			Bounds lineBounds = getChildren().get(0).getBoundsInLocal();
			Label arrowLabel = (Label) getChildren().get(3);
			arrowLabel.setLayoutX((lineBounds.getMinX()+lineBounds.getMaxX())/2+5);//offset a little from the line when vertical
			arrowLabel.setLayoutY((lineBounds.getMinY()+lineBounds.getMaxY())/2);
		};
		
		//listen to changes to node positions
		start.boundsInParentProperty().addListener(listener);
		end.boundsInParentProperty().addListener(listener);
		
		//refresh listener to get initial values
		listener.invalidated(null);
		
		//force all overlaps to update
		for (Transition t : start.getTransitions()) {
			if(end.getTransitions().contains(t) && !this.equals(t)) {
				t.refresh();
			}
		}
	}
	
	public String getStartId() {
		return startID;
	}
	
	public String getEndId() {
		return endID;
	}
	
	public String getMethodName() {
		return methodName;
	}

	public int getNumber() {
		return thisTransition;
	}
	
	/*
	 * Deletes this node
	 */
	public void delete() {
		start.unregisterTransition(this);
		end.unregisterTransition(this); //unregister from both states
		Pane p = (Pane) getParent();
		p.getChildren().remove(this); //remove transition
	}
	
	/*
	 * Renames transition
	 */
	public void rename(String methodName) {
		this.methodName = methodName;
		Label label = (Label) getChildren().get(3);
		StringBuilder labelText = new StringBuilder();
		for(String s : guardConditions) {
			labelText.append("[ ");
			labelText.append(s);
			labelText.append(" ]");
			labelText.append("\n");
		}
		labelText.append(methodName);
		label.setText(labelText.toString());
	}
	
	/*
	 * Add a guard condition
	 */
	public void addGuard(String guard) {
		guardConditions.add(guard);
		Label label = (Label) getChildren().get(3);
		StringBuilder labelText = new StringBuilder();
		for(String s : guardConditions) {
			labelText.append("[ ");
			labelText.append(s);
			labelText.append(" ]");
			labelText.append("\n");
		}
		labelText.append(methodName);
		label.setText(labelText.toString());
	}
	
	/*
	 * Remove a guard condition
	 */
	public void removeGuard(String guard) {
		if(!guard.equals(null)) {
			guardConditions.remove(guard);		
			Label label = (Label) getChildren().get(3);
			StringBuilder labelText = new StringBuilder();
			for(String s : guardConditions) {
				labelText.append(s);
				labelText.append("\n");
			}
			labelText.append(methodName);
			label.setText(labelText.toString());
		}
	}
	
	/*
	 * Get the list of guard conditions
	 */
	
	public List<String> getGuards() {
		return guardConditions;
	}
	
	/*
	 * Recalculates position when called
	 */
	public void refresh() {
		List<Transition> overlaps = new ArrayList<Transition>();
		for (Transition t : start.getTransitions()) {
			if(end.getTransitions().contains(t) && !this.equals(t)) {
				overlaps.add(t);
			}
		}
		
		overlapUpdate(start.getBoundsInParent(), end.getBoundsInParent(), overlaps); //called BECAUSE of new overlapping
		updateArrowhead(); //update the arrowhead positions
		
		//attach label
		Bounds lineBounds = getChildren().get(0).getBoundsInLocal();
		Label arrowLabel = (Label) getChildren().get(3);
		arrowLabel.setLayoutX((lineBounds.getMinX()+lineBounds.getMaxX())/2);
		arrowLabel.setLayoutY((lineBounds.getMinY()+lineBounds.getMaxY())/2);
	}
	
	/*
	 * Called when only one transition runs between a given pair of states
	 */
	private void nonOverlapUpdate(Bounds startBounds, Bounds endBounds) {

		double startX, startY, endX, endY;
		double xDif, yDif;
		xDif = startBounds.getMinX() - endBounds.getMinX();
		yDif = startBounds.getMinY() - endBounds.getMinY();
		
		if(xDif < 0 && Math.abs(xDif) > Math.abs(yDif)) { //attach to the right side of start, left of end
			startX = startBounds.getMaxX();
			startY = (startBounds.getMinY()+startBounds.getMaxY())/2;
			endX = endBounds.getMinX();
			endY = (endBounds.getMinY()+endBounds.getMaxY())/2;	
		}
		else if(xDif > 0 && Math.abs(xDif) > Math.abs(yDif)) { //right side of end, left side of start
			startX = startBounds.getMinX();
			startY = (startBounds.getMinY()+startBounds.getMaxY())/2;
			endX = endBounds.getMaxX();
			endY = (endBounds.getMinY()+endBounds.getMaxY())/2;	
		} 
		else if(yDif < 0) { //bottom of start, top of end
			startX = (startBounds.getMinX()+startBounds.getMaxX())/2;
			startY = startBounds.getMaxY();
			endX = (endBounds.getMinX()+endBounds.getMaxX())/2;
			endY = endBounds.getMinY();	
		}
		else { //last remaining quadrant
			startX = (startBounds.getMinX()+startBounds.getMaxX())/2;
			startY = startBounds.getMinY();
			endX = (endBounds.getMinX()+endBounds.getMaxX())/2;
			endY = endBounds.getMaxY();	
		}
		
		Line mainLine = (Line) getChildren().get(0);
		mainLine.setStartX(startX);
		mainLine.setStartY(startY);
		mainLine.setEndX(endX);
		mainLine.setEndY(endY); //update the transition's X/Y positions
	}
	
	/*
	 * Called when multiple transitions run between given states.
	 */
	private void overlapUpdate(Bounds startBounds, Bounds endBounds, List<Transition> overlaps) {
		int numberOfOverlaps = overlaps.size(); //the total number of other overlapping transitions
		int position = 0; //What relative position does this have compared to the others? Default to being at the start
		for (Transition t : overlaps) {
			if(t.getNumber() < thisTransition)
				position++; //older transitions go to the top (as based on JavaFX co-ordinates, 0 0 is top left), newer transitions lower
		}
		//calculate position as before, with modifications to space out along the relevant axis
		
		double startX, startY, endX, endY;
		double xDif, yDif;
		xDif = startBounds.getMinX() - endBounds.getMinX();
		yDif = startBounds.getMinY() - endBounds.getMinY();
		
		if(xDif < 0 && Math.abs(xDif) > Math.abs(yDif)) { //attach to the right side of start, left of end
			double startYSpread = (startBounds.getMaxY()-startBounds.getMinY())/numberOfOverlaps;
			double endYSpread = (endBounds.getMaxY()-endBounds.getMinY())/numberOfOverlaps;
			startX = startBounds.getMaxX();
			startY = (startBounds.getMinY()+startYSpread*position);
			endX = endBounds.getMinX();
			endY = (endBounds.getMinY()+endYSpread*position);	
		}
		else if(xDif > 0 && Math.abs(xDif) > Math.abs(yDif)) { //right side of end, left side of start
			double startYSpread = (startBounds.getMaxY()-startBounds.getMinY())/numberOfOverlaps;
			double endYSpread = (endBounds.getMaxY()-endBounds.getMinY())/numberOfOverlaps;
			startX = startBounds.getMinX();
			startY = (startBounds.getMinY()+startYSpread*position);
			endX = endBounds.getMaxX();
			endY = (endBounds.getMinY()+endYSpread*position);	
		} 
		else if(yDif < 0) { //bottom of start, top of end
			double startXSpread = (startBounds.getMaxY()-startBounds.getMinY())/numberOfOverlaps;
			double endXSpread = (endBounds.getMaxY()-endBounds.getMinY())/numberOfOverlaps;
			startX = (startBounds.getMinX()+startXSpread*position);
			startY = startBounds.getMaxY();
			endX = (endBounds.getMinX()+endXSpread*position);
			endY = endBounds.getMinY();	
		}
		else { //last remaining quadrant
			double startXSpread = (startBounds.getMaxX()-startBounds.getMinX())/numberOfOverlaps;
			double endXSpread = (endBounds.getMaxX()-endBounds.getMinX())/numberOfOverlaps;
			startX = (startBounds.getMinX()+startXSpread*position);
			startY = startBounds.getMinY();
			endX = (endBounds.getMinX()+endXSpread*position);
			endY = endBounds.getMaxY();	
		}
		
		Line mainLine = (Line) getChildren().get(0);
		mainLine.setStartX(startX);
		mainLine.setStartY(startY);
		mainLine.setEndX(endX);
		mainLine.setEndY(endY); //update the transition's X/Y positions
	}
	
	/*
	 * Rotates arrow along with rest of line and keeps it in position
	 */
	private void updateArrowhead() {
		Line mainLine = (Line) getChildren().get(0);
		double endX = mainLine.getEndX();
		double endY = mainLine.getEndY();
		double startX = mainLine.getStartX();
		double startY = mainLine.getStartY();
		
		double angle = Math.atan((endY-startY)/(endX-startX))*180/Math.PI; //angle between the line and the vertical
		//converted to degrees to work with the Rotate transform
		
		Line arrow = (Line) getChildren().get(1);
		arrow.setStartX(endX);
		arrow.setStartY(endY); //arrows attach to the end of the transition
		arrow.setEndX(endX+20); //arrow starts off as a 20 pixel horizontal line
		arrow.setEndY(endY);
		arrow.getTransforms().remove(0);
		if(endX<startX)
			arrow.getTransforms().add(new Rotate(angle+30,endX,endY)); //remove old rotation, add new rotation pivoting around new endpoint
		else
			arrow.getTransforms().add(new Rotate(angle+210,endX,endY)); //mirror for other side of screen
		arrow = (Line) getChildren().get(2);
		arrow.setStartX(endX);
		arrow.setStartY(endY); 
		arrow.setEndX(endX+20);
		arrow.setEndY(endY);
		arrow.getTransforms().remove(0);
		if(endX<startX)
			arrow.getTransforms().add(new Rotate(angle-30,endX,endY)); //remove old rotation, add new rotation pivoting around new endpoint
		else
			arrow.getTransforms().add(new Rotate(angle-210,endX,endY)); //mirror for other side of screen
		
	}
}
