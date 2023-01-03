import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/*
 * Creates and attaches event handlers for mouse drag events
 */
public class MouseDragHandlers {
	
	double previousSceneX, previousSceneY; //mouse position on previous mouseevent call
	double previousTranslateX, previousTranslateY; //node position on previous mouseevent call
	
	public void addHandlers (Node node) {
		node.setOnMousePressed(mousePressedHandler);
		node.setOnMouseDragged(mouseDraggedHandler);
	}
	
	EventHandler<MouseEvent> mousePressedHandler = e -> {
		previousSceneX = e.getSceneX();
		previousSceneY = e.getSceneY(); //get the position of the mouse relative to the window
		if (e.getSource() instanceof Node) {
			Node n = (Node) e.getSource();
			previousTranslateX = n.getTranslateX();
			previousTranslateY = n.getTranslateY(); //get the draggable object's current translation from its
			//original position
		}
	}; 
	
	EventHandler<MouseEvent> mouseDraggedHandler = e -> {
		double sceneX = e.getSceneX();
		double sceneY = e.getSceneY();
		
		double offsetX = sceneX - previousSceneX;
		double offsetY = sceneY - previousSceneY; //see how much the mouse has moved since the last update
		
		if (e.getSource() instanceof Node) {
			Node n = (Node) e.getSource(); 
			Bounds bounds = n.getBoundsInParent(); //gets the node position relative to its parent node--with this, it's easy to check
			//if the node is still within boundaries
			if (bounds.getMinX() + offsetX > 0 && bounds.getMinY() + offsetY > 0 && 
    			bounds.getMaxX() + offsetX < 1024 && bounds.getMaxY() + offsetY  < 768) { // do not move node beyond canvas boundaries
				n.setTranslateX(previousTranslateX+offsetX);
				n.setTranslateY(previousTranslateY+offsetY); //move node
				previousSceneX = sceneX;
				previousSceneY = sceneY; 
				previousTranslateX=n.getTranslateX();
				previousTranslateY=n.getTranslateY();
				//update the new previous positions
			}
		}				
	};
}