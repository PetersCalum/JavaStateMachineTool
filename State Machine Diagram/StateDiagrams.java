import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class StateDiagrams extends Application {

	List<Button> buttons; //accessible list of buttons
	
	boolean addState = false; //controls if you can add a state
	boolean addInitial = false; //controls if you can add an initial state
	boolean addTerminal = false; //controls if you can add a terminal state
	boolean addTransition = false; //controls if you can add transitions
	
	InitialState initial; //initial state
	TerminalState terminal; //terminal state
	State transitionStart; //first state selected in a transition 
	//must be set here because otherwise local variable in method declaration--must be final or effectively final
	//editing global values is acceptable, however
	Stage stage;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("State Diagram Creator");

		BorderPane borders = new BorderPane();
		
		Scene scene = new Scene(borders, 1152, 864);
		primaryStage.setScene(scene);

		//create buttons and set ID
		buttons = new ArrayList<Button>();
		//add button for adding states
		Button stateButton = new Button("Add State");
		stateButton.setId("stateButton");
		buttons.add(stateButton);
		//add button for adding initial state
		Button initialButton = new Button("Add Initial State");
		initialButton.setId("initialButton");
		buttons.add(initialButton);
		//add button for adding terminal state
		Button terminalButton = new Button("Add Terminal State");
		terminalButton.setId("terminalButton");
		buttons.add(terminalButton);
		//add button for adding lines 
		Button transitionButton = new Button("Add transition");
		transitionButton.setId("transitionButton");
		buttons.add(transitionButton);
		
		FlowPane top = new FlowPane(stateButton, initialButton, terminalButton, transitionButton);
		top.setPrefWidth(1024);
		top.setAlignment(Pos.CENTER);
		top.setHgap(10);
		borders.setTop(top);
		BorderPane.setAlignment(top, Pos.CENTER);
		
		//create centre nodes
		Group root = new Group(); 
		Canvas canvas = new Canvas(1024,768); //diagram bg
		Pane overlay = new Pane(); //diagram contents
		overlay.setPrefSize(1024, 768);
		overlay.setMaxSize(1024, 768); //otherwise additions near the boundary stretch it
		
		//get the drawing context
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		//draw canvas 
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, 1024, 768);
		gc.setStroke(Color.BLACK);
		gc.strokeRect(0, 0, 1024, 768);
		
		//set up state-drag handler and context-menu handler creators
		MouseDragHandlers mouseDrag = new MouseDragHandlers();

		//create the right-click menus and handlers 
		StateMenus stateMenus = new StateMenus();
		TransitionMenus transitionMenus = new TransitionMenus();
		
		//implement button functionality
		//disable other buttons on click
		stateButton.setOnAction(e -> {
			addState = true;
			disableButtons();
		});
		
		initialButton.setOnAction(e -> {
			addState = true;
			addInitial = true;
			disableButtons();
		});
		
		terminalButton.setOnAction(e -> {
			addState = true;
			addTerminal = true;
			disableButtons();
		});
		
		transitionButton.setOnAction(e -> {
			addTransition = true;
			disableButtons();
			overlay.getChildrenUnmodifiable();
			for(Node n: overlay.getChildrenUnmodifiable()) {
				if(n instanceof State && !(n instanceof TerminalState)) {
					State s = (State) n;
					s.highlight();
				}
			}
		});
		
		//set up the "add state" and "add transition" functionality
		addOverlayHandlers(overlay, scene, mouseDrag, stateMenus, transitionMenus);
		
		//attach listeners to automatically limit and free up the single initial or terminal node allowed
		addOverlayListeners(overlay, initialButton, terminalButton);
		
		//add a key listener to the scene for cancelling an addition
		scene.setOnKeyPressed(e -> {
			if(e.getCode().equals(KeyCode.ESCAPE)) {
				//reset all active addition info
				transitionStart = null;
				addState = false;
				addTransition = false;
				addInitial = false;
				addTerminal = false; 
				//disable any higlighted states
				for(Node n : overlay.getChildren()) {
					if (n instanceof State) {
						State s = (State) n;
						s.stopHighlight();
					}
				} 
				//re-enable appropriate buttons
				enableButtons();
			}
		});
		
		//add set-up centre to the group root and then add THAT to the centre
		root.getChildren().addAll(canvas, overlay);
		borders.setCenter(root);
		
		//add buttons to save and load the overlay as JSON
		
		//contain buttons inside a flowpane
		FlowPane bottom = new FlowPane();
		bottom.setPrefWidth(1024);
		bottom.setAlignment(Pos.CENTER);
		bottom.setHgap(10);
		Button saveButton = new Button("Save");
		Button loadButton = new Button("Load");
		Button exportButton = new Button("Export to Java");
		Button clearButton = new Button("Clear");
		bottom.getChildren().add(saveButton);
		bottom.getChildren().add(loadButton);
		bottom.getChildren().add(exportButton);		
		bottom.getChildren().add(clearButton);		
		borders.setBottom(bottom);
		
		//add save button functionality
		saveButton.setOnAction(e -> {
			saveOverlay(overlay);
		});
		
		//add load button functionality
		loadButton.setOnAction(e -> {
			loadOverlay(overlay);
		});
		
		//add export button functionality
		exportButton.setOnAction(e -> {
			try {
	    		Optional<String> className = null;
		    	TextInputDialog userText = new TextInputDialog();
			    userText.setHeaderText("Enter Class Name");
			    className = userText.showAndWait();
			    JavaExporter exporter = new JavaExporter();
			    exporter.exportDiagram(overlay, className.get());
			} catch (NoSuchElementException r) {
				//user cancelled selection
			}
		});
		
		clearButton.setOnAction(e -> {
			overlay.getChildren().clear();
		});
		
		
		stage = primaryStage; //for referencing 
		primaryStage.show();
	}
	
	/*
	 * Go through list of buttons and disable
	 */
	private void disableButtons() {
		for(Button b : buttons) {
			b.setDisable(true);
		}
	}
	
	/*
	 * Go through list of buttons and enable all that need to be enabled
	 */
	private void enableButtons() {
		for(Button b : buttons) {
			if(b.getId().equals("initialButton"))	{
				if(initial == null)
					b.setDisable(false);
			}
			else if(b.getId().equals("terminalButton")) {
				if (terminal == null)
					b.setDisable(false);
			}
			else {
				b.setDisable(false);
			}
		}
	}
	
	/*
	 * Attaches the on-click event handlers to the overlay
	 * Added via method to allow easily replacing the current
	 * overlay with a fresh one
	 */
	private void addOverlayHandlers(Pane overlay, Scene scene, MouseDragHandlers mouseDrag, StateMenus stateMenus, 
			TransitionMenus transitionMenus) {
		overlay.setOnMouseClicked(e -> {
		    if(addState && e.getX() < 1024 && e.getY() < 768) {
		    	if(addInitial) { //if initial state
		    		State initial = new InitialState("initial");
		    		initial.relocate(e.getX(),e.getY());
		    		mouseDrag.addHandlers(initial);
		    		stateMenus.addMenu(initial);
		    		this.initial = (InitialState) initial;
		    		overlay.getChildren().add(initial);
		    	}
		    	else if (addTerminal) {
		    		State terminal = new TerminalState("terminal");
		    		terminal.relocate(e.getX(),e.getY());
		    		mouseDrag.addHandlers(terminal);
		    		stateMenus.addMenu(terminal);
		    		this.terminal = (TerminalState) terminal;
		    		overlay.getChildren().add(terminal);
		    	}		    	
		    	else {
		    		try {
			    		//get user input, i.e. state name
			    		Optional<String> result = null;
				    	boolean unique = false;
				    	while(!unique) {
				    		TextInputDialog userText = new TextInputDialog();
					    	userText.setHeaderText("Enter Unique State Name");
					    	result = userText.showAndWait();
					    	if(scene.lookup("#"+result.get())==null) {
					    		unique = true;
					    	}
					    }
			    		State state = new State(result.get()); //create new state
			    		state.relocate(e.getX(), e.getY()); //place state where mouse clicked
				    	mouseDrag.addHandlers(state);
				    	stateMenus.addMenu(state);
				    	overlay.getChildren().add(state);
			    	} catch (NoSuchElementException r) {
			    		//harmless--it means that the user cancelled out of the dialog box
			    		//catching to still disable buttons
			    	}
		    	}
		    	addState = false;
		    	addInitial = false;
		    	addTerminal = false;
		    	enableButtons();
		    }
		    else if(addTransition && e.getX() < 1024 && e.getY() < 768) {
			    try {
					if(transitionStart == null) {
						Node n = e.getPickResult().getIntersectedNode();
						while(!(n instanceof State)) {
							if(n.getParent()!=null) {
								n = n.getParent();
							}
							else if(n instanceof TerminalState) {
								return; //cannot transition from terminal state
							}
							else {
								return; //invalid target clicked
							}
						}
						State s = (State) n;
						transitionStart = s;
						if(initial != null) {
							initial.stopHighlight(); //stop highlighting as it's now an invalid selection
						}
						if(terminal != null) {
							terminal.highlight(); //highlight to show it can be changed to
						}
					}
					else {
						Node n = e.getPickResult().getIntersectedNode();
						while(!(n instanceof State)) {
							if(n.getParent()!=null) {
								n = n.getParent();
							}
							else {
								return; //invalid target clicked
							}
						}
						if(!(n instanceof InitialState)) { //you can't transition TO an initial state
							//get user input--method name
				    		TextInputDialog userText = new TextInputDialog();
					    	userText.setHeaderText("Enter Method Name");
					    	Optional<String> result = userText.showAndWait();
					    	
							//create a transition between the previously selected state and the second selected state
							ObservableList<Node> o = overlay.getChildren();
							Transition transition;
							if(n.equals(transitionStart)) {
								transition = new SelfReferentialTransition(transitionStart, result.get());
							} 
							else {
								transition = new Transition(transitionStart, (State) n, result.get());
							}
							transitionMenus.addMenu(transition);
							o.add(transition);
							//stop highlighting nodes
							for(Node n2: o) {
								if(n2 instanceof State) {
									State s = (State) n2;
									s.stopHighlight();
									if(!(initial == null))
										initial.stopHighlight();
								}
							}
							transitionStart = null;
							addTransition = false;
							enableButtons();
						}
					}
				} catch (NoSuchElementException r) {
					//user cancelled selection
					//slightly more involved								
					ObservableList<Node> o = overlay.getChildren();
					for(Node n: o) {
						if(n instanceof State) {
							State s = (State) n;
							s.stopHighlight();
						}
					}
					enableButtons();
				}
		    }
		});
	}
	
	/*
	 * Adds necessary on-change listeners to the overlay
	 */
	private void addOverlayListeners(Pane overlay, Button initialButton, Button terminalButton) {
		ListChangeListener<Node> listener = o -> {
			if(!overlay.getChildrenUnmodifiable().contains(initial)) {
				initial = null; //set to null if initial deleted
				initialButton.setDisable(false); //allow adding a new one
			}			
			if(!overlay.getChildrenUnmodifiable().contains(terminal)) {
				terminal = null; //set to null if terminal deleted
				terminalButton.setDisable(false); //allow adding a new one
			}
		};
		
		overlay.getChildren().addListener(listener);
	}
	
	/*
	 * Saves the current contents of the overlay into a JSON Object and
	 * writes it out to a file.
	 */
	@SuppressWarnings("unchecked")
	public void saveOverlay(Pane overlay) {
		JSONObject overlayChildren = new JSONObject();
		JSONArray childrenArray = new JSONArray();
		ObservableList<Node> children = overlay.getChildrenUnmodifiable();
		
		//iterate through list, creating new objects to add to the array
		for(Node node : children) {
			JSONObject child = new JSONObject();
			if (node instanceof State) {
				if(node instanceof InitialState) 
					child.put("Type", "Initial");
				else if(node instanceof TerminalState)
					child.put("Type", "Terminal");
				else
					child.put("Type", "State");
				child.put("ID", node.getId());
				//contains the node's X/Y co-ords and size preferences with reference to its parent
				//e.g. its current X/Y position 
				Bounds bounds = node.getBoundsInParent();
				child.put("XCoord", bounds.getMinX());
				child.put("YCoord", bounds.getMinY()); //save its X and Y co-ordinates.
			}
			else if (node instanceof Transition) {
				child.put("Type", "Transition");
				Transition transition = (Transition) node;
				child.put("Start", transition.getStartId());
				child.put("End", transition.getEndId());
				child.put("Label", transition.getMethodName());//save the two nodes that this is connected to and its method
				JSONArray guards = new JSONArray();
				for(String s : transition.getGuards()) {
					guards.add(s);
				}
				child.put("Guards", guards);
			}
			childrenArray.add(child);
		}
		overlayChildren.put("Contents",childrenArray);
		
		//finally, write to file
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialFileName("export.json");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
		File file = fileChooser.showSaveDialog(stage);
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(overlayChildren.toJSONString());
			fileWriter.flush();
			fileWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(NullPointerException e) {
			e.printStackTrace(); //most likely quit out of window, no file to choose
		}
	}
	
	public void loadOverlay(Pane overlay) {
		MouseDragHandlers mouseDrag = new MouseDragHandlers(); //will be creating states, need the handler adder
		StateMenus stateMenus = new StateMenus();
		TransitionMenus transitionMenus = new TransitionMenus();
		JSONParser parser = new JSONParser();
		
		try {
			//select file
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialFileName("export.json");
			fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
			File file = fileChooser.showOpenDialog(stage);
			
			//read in object
			JSONObject overlayChildren = (JSONObject) parser.parse(new FileReader(file));
			JSONArray childrenArray = (JSONArray) overlayChildren.get("Contents"); //get array of nodes
			JSONObject[] transitions = new JSONObject[childrenArray.size()];//make array as long as all objects in case somehow all objects
			//are transitions
			int transitionsCount = 0;
			
			overlay.getChildren().clear(); //remove all old children now
			initial = null;
			terminal = null; //reset values
			disableButtons(); //disable buttons
			
			@SuppressWarnings("rawtypes")
			Iterator i = childrenArray.iterator();
			while (i.hasNext()) {
				JSONObject child = (JSONObject) i.next();
				String type = (String) child.get("Type");
				if(type.equals("State") 
						|| type.equals("Initial") 
						|| type.equals("Terminal")) {
					//same creation code as in the main body, mostly
					State state;
					if(type.equals("State")) {
						state = new State((String) child.get("ID"));
					}
					else if(type.equals("Initial")) {
						state = new InitialState("initial");
						initial = (InitialState) state;
					}
					else {
						state = new TerminalState("terminal");
						terminal = (TerminalState) state;
					}
		    		overlay.getChildren().add(state); 
		    		mouseDrag.addHandlers(state);
		    		stateMenus.addMenu(state);
		    		double x = (double) child.get("XCoord");
		    		double y = (double) child.get("YCoord");
		    		state.relocate(x, y);
				}
				else if(child.get("Type").equals("Transition")) {
					transitions[transitionsCount] = child;
					transitionsCount++;
					//deal with transitions after
				}
			}
			Scene scene = overlay.getScene();
			for(int j = 0; j<transitionsCount; j++) {
				State start = (State) scene.lookup("#"+transitions[j].get("Start")); //transitions can only attach to states
				State end = (State) scene.lookup("#"+transitions[j].get("End"));
				String label = (String) transitions[j].get("Label");
				Transition transition;
				if(start.equals(end)) {
					transition = new SelfReferentialTransition(start, label);
				}
				else {
					transition = new Transition(start, end, label); 
				}
				transitionMenus.addMenu(transition);
				JSONArray guards = (JSONArray) transitions[j].get("Guards");
				for(int k = 0; k < guards.size(); k++) {
					String s = (String) guards.get(k);
					transition.addGuard(s);
				}
				overlay.getChildren().add(transition);//find relevant nodes and reattach transitions
			}
			
			enableButtons(); //finally enable buttons
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(ParseException e) {
			e.printStackTrace();
		} catch(NullPointerException e) {
			e.printStackTrace(); //most likely quit out of window, no file to choose
		}
	}
	
}
