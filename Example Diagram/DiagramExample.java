import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DiagramExample extends Application {
	private int fine;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		//set up the drawing window
		Group root = new Group();
		Scene drawingScene = new Scene(root,800,600);
		primaryStage.setScene(drawingScene);
		
		//add the blank canvas background and an overlay to place the diagram in
		Canvas canvas = new Canvas(800,600);
		
		//get the drawing context
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		//draw canvas 
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, 800, 600);
		
		//set up overlay
		Pane overlay = new Pane();
		overlay.setPrefSize(800, 600);
		overlay.setMaxSize(800, 600); 
		
		root.getChildren().addAll(canvas,overlay); //add to the main container
		
		//set up and place states
		State initial = new InitialState("initial");
		overlay.getChildren().add(initial);
		initial.relocate(50, 190);
		
		//onshelf
		State onShelf = new State("onShelf");
		overlay.getChildren().add(onShelf);
		onShelf.relocate(270, 165);
		
		//onloan
		State onLoan = new State("onLoan");
		overlay.getChildren().add(onLoan);
		onLoan.relocate(560, 165);
		
		//terminal
		State terminal = new TerminalState("terminal");
		overlay.getChildren().add(terminal);
		terminal.relocate(595, 500);
		
		//add all transitions between nodes
		Transition currentTransition = new Transition(initial, onShelf, "bought()");
		overlay.getChildren().add(currentTransition);
		currentTransition = new Transition(onShelf, onLoan, "borrowed()");
		overlay.getChildren().add(currentTransition);
		currentTransition = new Transition(onLoan, onShelf, "returned()");
		overlay.getChildren().add(currentTransition);
		currentTransition = new Transition(onShelf, terminal, "disposed()");
		overlay.getChildren().add(currentTransition);
		currentTransition = new Transition(onLoan, terminal, "lost()");
		overlay.getChildren().add(currentTransition);
		currentTransition = new SelfReferentialTransition(onLoan, "renew()");
		currentTransition.addGuard("fine < 5");
		overlay.getChildren().add(currentTransition);
		
		primaryStage.setTitle("Book Diagram");
		primaryStage.setResizable(false);
		
		//set up the UI control stage		
		Stage secondaryStage = new Stage();
		GridPane uiGrid = new GridPane();
		Scene uiScene = new Scene(uiGrid);
		secondaryStage.setScene(uiScene);
		
		//add buttons to the UI Pane
		//state transition buttons follow the same pattern as java exported from the diagram drawing program
		
		//book creation
		Button newBook = new Button("Create Book");
		newBook.setMinWidth(110);
		newBook.setMinHeight(40);
		uiGrid.add(newBook, 0, 0);
		
		//book purchase
		Button buyBook = new Button("Buy Book");
		buyBook.setMinWidth(110);
		buyBook.setMinHeight(40);
		uiGrid.add(buyBook, 1, 0);
		buyBook.setDisable(true);
		
		//book on loan
		Button loanBook = new Button("Loan Book");
		loanBook.setMinWidth(110);
		loanBook.setMinHeight(40);
		uiGrid.add(loanBook, 0, 1);
		loanBook.setDisable(true);
		
		//return loaned book
		Button returnBook = new Button("Return Book");
		returnBook.setMinWidth(110);
		returnBook.setMinHeight(40);
		uiGrid.add(returnBook, 1, 1);
		returnBook.setDisable(true);
		
		//renew book
		Button renewBook = new Button("Renew Book");
		renewBook.setMinWidth(110);
		renewBook.setMinHeight(40);
		uiGrid.add(renewBook, 0, 2);
		renewBook.setDisable(true);
		
		//dispose of book
		Button disposeBook = new Button ("Dispose of Book");
		disposeBook.setMinWidth(110);
		disposeBook.setMinHeight(40);
		uiGrid.add(disposeBook, 0, 3);
		disposeBook.setDisable(true);
		
		//lose book
		Button loseBook = new Button ("Lose Book");
		loseBook.setMinWidth(110);
		loseBook.setMinHeight(40);
		uiGrid.add(loseBook, 1, 3);
		loseBook.setDisable(true);
		
		//clear and start again
		Button clearBook = new Button("Clear");
		clearBook.setMinWidth(110);
		clearBook.setMinHeight(40);
		uiGrid.add(clearBook, 0, 4);
		clearBook.setDisable(true);
		
		//the following buttons provide extra methods that don't affect state
		//add fine
		
		Button addFine = new Button ("Increase Fine");
		addFine.setMinWidth(110);
		addFine.setMinHeight(40);
		uiGrid.add(addFine, 0, 5);
		addFine.setDisable(true);
		
		//pay fine
		Button payFine = new Button ("Pay Fine");
		payFine.setMinWidth(110);
		payFine.setMinHeight(40);
		uiGrid.add(payFine, 1, 5);
		payFine.setDisable(true);
		
		//fine display:
		Text fineLabel = new Text("Fine: 0");
		fineLabel.setFont(new Font(28));
		uiGrid.add(fineLabel, 0, 6); 
		
		//get book info
		Button getInfo = new Button ("Get Book Info");
		getInfo.setMinWidth(110);
		getInfo.setMinHeight(40);
		uiGrid.add(getInfo, 1, 6);
		getInfo.setDisable(true);
		
		//unlike in the generated example code, state isn't determined by a separate variable
		//but by which buttons are activated and de-activated
		
		//creation allows purchase and info inspection
		newBook.setOnAction(e -> {
			initial.highlight();
			newBook.setDisable(true);
			buyBook.setDisable(false);
			getInfo.setDisable(false);
		});
		
		//purchase allows the book to be loaned or disposed
		buyBook.setOnAction(e -> {
			initial.stopHighlight();
			onShelf.highlight();
			buyBook.setDisable(true);
			loanBook.setDisable(false);
			disposeBook.setDisable(false);
		});
		
		//when on loan the book can accumulate fines, be renewed, be lost, or be returned
		loanBook.setOnAction(e -> {
			onShelf.stopHighlight();
			onLoan.highlight();
			loanBook.setDisable(true);
			disposeBook.setDisable(true);
			renewBook.setDisable(false);
			loseBook.setDisable(false);
			returnBook.setDisable(false);
			addFine.setDisable(false);
		});
		
		//no handler for renewing, if there was some sort of timer, however, it would reset it
		
		//when returned, same as if the book has just been purchased
		returnBook.setOnAction(e -> {
			onLoan.stopHighlight();
			onShelf.highlight();
			renewBook.setDisable(true);
			loseBook.setDisable(true);
			returnBook.setDisable(true);
			addFine.setDisable(true);
			payFine.setDisable(true); //we can assume fines are paid when the book is returned
			fine = 0;
			fineLabel.setText("Fine: 0");
			loanBook.setDisable(false);
			disposeBook.setDisable(false);
		});
		
		//disposal disables anything but starting again and accessing info
		disposeBook.setOnAction(e -> {
			onShelf.stopHighlight();
			terminal.highlight();
			loanBook.setDisable(true);
			disposeBook.setDisable(true);
			clearBook.setDisable(false);
		});
		
		//losing the book is the same
		loseBook.setOnAction(e -> {
			onLoan.stopHighlight();
			terminal.highlight();
			renewBook.setDisable(true);
			loseBook.setDisable(true);
			returnBook.setDisable(true);
			addFine.setDisable(true);
			payFine.setDisable(true);
			fine = 0;
			fineLabel.setText("Fine: 0");
			clearBook.setDisable(false);
		});
		
		//finally, clear resets to a new book object (effectively)
		clearBook.setOnAction(e -> {
			terminal.stopHighlight();
			clearBook.setDisable(true);
			getInfo.setDisable(true);
			newBook.setDisable(false);
		});
		
		//adds to the fine
		addFine.setOnAction(e -> {
			fine++;
			fineLabel.setText("Fine: " + fine);
			payFine.setDisable(false);
			if(fine >= 5)
				renewBook.setDisable(true);
		});
		
		//pays off the fine
		payFine.setOnAction(e -> {
			fine = 0;
			fineLabel.setText("Fine: 0");
			payFine.setDisable(true);
			renewBook.setDisable(false);
		});
		
		getInfo.setOnAction(e -> {
			Alert info = new Alert(AlertType.INFORMATION);
			info.setHeaderText(null);
			info.setContentText("Title: The Square Root of 4 to a Million Places\nAuthor: Norman L. De Forest");
			info.show();
		});
		
		//close entire program if one window is closed
		EventHandler<WindowEvent> closer = e -> {
			Platform.exit();
			System.exit(0);
		};
		primaryStage.setOnCloseRequest(closer);
		secondaryStage.setOnCloseRequest(closer);
		
		//position them together
		primaryStage.setX(0);
		secondaryStage.setX(800);
		
		primaryStage.show();
		secondaryStage.show();
	}
	
}
