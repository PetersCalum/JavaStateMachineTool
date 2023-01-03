import java.util.NoSuchElementException;
import java.util.Optional;

import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;

public class TransitionMenus {
	public void addMenu(Transition transition) {
		ContextMenu transitionMenu = new ContextMenu();
		MenuItem deleteTransition = new MenuItem("Delete Transition");
		MenuItem editName = new MenuItem("Edit Name");
		MenuItem addGuard = new MenuItem("Add Guard");
		MenuItem removeGuard = new MenuItem("Remove Guard");
		transitionMenu.getItems().add(deleteTransition);
		transitionMenu.getItems().add(editName);
		transitionMenu.getItems().add(addGuard);
		transitionMenu.getItems().add(removeGuard);
		
		//handler to delete transition
		deleteTransition.setOnAction(e -> transition.delete());		
		
		//handler for editing the transition name
		editName.setOnAction(e -> {
			try {
				Optional<String> result = null;
		    	TextInputDialog userText = new TextInputDialog();
			    userText.setHeaderText("Enter Method Name");
			    result = userText.showAndWait();
		    	transition.rename(result.get());
			}
			catch (NoSuchElementException r) {
				//user just cancelled selection
			}
		});
		
		/*
		 * Add a guard condition to the transition
		 */
		addGuard.setOnAction(e -> {
			try {
				Optional<String> result = null;
				TextInputDialog userText = new TextInputDialog();
				userText.setHeaderText("Enter Guard");
				result = userText.showAndWait();	
				String guard = new String(result.get());
				transition.addGuard(guard);
			}
			catch (NoSuchElementException r) {
				//user cancelled 
			}
		});
		
		/*
		 * Remove a guard condition from the transition
		 */
		removeGuard.setOnAction(e -> {
			try {
				Optional<String> selection = null;
				ChoiceDialog<String> guardSelect = new ChoiceDialog<String>(null, transition.getGuards());
				guardSelect.setHeaderText("Select Guard to Remove");
				selection = guardSelect.showAndWait();
				transition.removeGuard(selection.get());
			}
			catch (NoSuchElementException r) {
				//user cancelled
			}
		});
		
		
		//finally, add the handler to show the context menu on a right click
		transition.setOnContextMenuRequested(e -> transitionMenu.show(transition, e.getScreenX(), e.getScreenY()));
		
	}
}
