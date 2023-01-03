import java.util.NoSuchElementException;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;

/*
 * Creates and attaches right-clickmenus for states
 */
public class StateMenus {
	public void addMenu(State state) {
		//initialise menu and menu options
		ContextMenu stateMenu = new ContextMenu();
		MenuItem deleteState = new MenuItem("Delete State");
		MenuItem editName = new MenuItem("Edit Name");
		stateMenu.getItems().add(deleteState);
		stateMenu.getItems().add(editName);
		
		//handler to delete state
		deleteState.setOnAction(e -> state.delete());
		
		//handler for editing the name of a state 
		editName.setOnAction(e -> {
			if(!(state instanceof InitialState)) {
				try {
					Optional<String> result = null;
					boolean unique = false;
			    	while(!unique) {
			    		TextInputDialog userText = new TextInputDialog();
				    	userText.setHeaderText("Enter Unique State Name");
				    	result = userText.showAndWait();
				    	if(state.getScene().lookup("#"+result.get())==null) {
				    		unique = true;
				    	}
				    }
			    	state.rename(result.get());
				}
				catch (NoSuchElementException r) {
					//user just cancelled selection
				}
			}
			else {
				Alert alertDialog = new Alert(AlertType.INFORMATION);
				alertDialog.setHeaderText("Can't rename initial state");
				alertDialog.show();
			}
		});
		
		//finally, add the handler to show the context menu on a right click
		state.setOnContextMenuRequested(e -> stateMenu.show(state, e.getScreenX(), e.getScreenY()));
	}
}