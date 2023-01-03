import java.util.ArrayList;
import java.util.List;

import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class State extends Group {
	
	protected List<Transition> transitions;
	protected boolean deleting;
	
	protected State() {
		super();
	}
	
	public State(String id) {
		super();
		setId(id); //allows lookup within an entire scene--useful for reconstructing a graph after saving
		transitions = new ArrayList<Transition>();		
		deleting = false; //necessary to prevent structural problems from deleting from a list
		
    	Text text = new Text(id);
		Rectangle stateBG = new Rectangle(text.getLayoutBounds().getWidth()+50, text.getLayoutBounds().getHeight()+50);
    	stateBG.setFill(Color.WHITESMOKE);
    	stateBG.setStroke(Color.BLACK);
    	StackPane stateContents = new StackPane(stateBG,text); //the "top" half of the state: its ID
    	
    	getChildren().add(stateContents); 
	}

	/*
	 * Turns box green to highlight the state
	 */
	public void highlight() {
		StackPane stateContents = (StackPane) getChildren().get(0); //know first child is a stackpane
		Rectangle stateBG = (Rectangle) stateContents.getChildren().get(0); //know first child is a rectangle
		stateBG.setFill(Color.LAWNGREEN);
	}

	/*
	 * Returns normal colour
	 */
	public void stopHighlight() {		
		StackPane stateContents = (StackPane) getChildren().get(0); //know first child is a stackpane
		Rectangle stateBG = (Rectangle) stateContents.getChildren().get(0); //know first child is a rectangle
		stateBG.setFill(Color.WHITESMOKE);			
	}

	/*
	 * Called by a new transition
	 */
	public void registerTransition(Transition transition) {
		transitions.add(transition);
	}
	
	/*
	 * Called by a transition being deleted
	 */
	public void unregisterTransition(Transition transition) {
		if(!deleting) 
			transitions.remove(transition);
		// When deleting, a state iterates through all connected transitions
		// modifying this list at that point would throw an exception
		// however, as the state is about to be removed, it has no need to change the list
	}
	
	/*
	 * Changes ID and text
	 */
	public void rename(String name) {
		setId(name);
		StackPane stateContents = (StackPane) getChildren().get(0);
		Text text = (Text) stateContents.getChildren().get(1);
		text.setText(name); //change text to match
		Rectangle stateBG = (Rectangle) stateContents.getChildren().get(0);
		stateBG.setWidth(text.getLayoutBounds().getWidth()+50); //resize box
	}
	
	public List<Transition> getTransitions() {
		return transitions;
	}
	
	public void delete() {
		deleting = true;
		for(Transition t: transitions) {
			t.delete();
		}
		Pane p = (Pane) getParent(); 
		p.getChildren().remove(this); //remove state
	}
}
