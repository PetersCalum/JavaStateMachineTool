import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

public class JavaExporter {
	
	//variables extracted from the diagram
	private List<String> states;
	private List<String[]> methods;
	private Map<String,String[]> variables;
	
	//export values
	private List<String> guardVariables;
	
	public JavaExporter() {
		states = new ArrayList<String>();
		methods = new ArrayList<String[]>();
		variables = new HashMap<String,String[]>(); 
		guardVariables = new ArrayList<String>();
	}
	
	public void exportDiagram(Pane overlay, String className) {
		//extract data
		getStates(overlay);
		getMethods(overlay);
		getVariables(overlay);
		
		//gather all variables
		formatVariables();
		
		//export to file
		writeFile(className);
	}

	/*
	 * Gathers all state ids
	 * When exported, these will become enum components
	 */
	private void getStates(Pane overlay) {
		for(Node n : overlay.getChildren()) {
			if(n instanceof State) {
				State s = (State) n;
				states.add(s.getId());
			}
		}
	}
	

	/*
	 * Gathers all transition namnes and their start/end states
	 * When exported, these become methods
	 */
	private void getMethods(Pane overlay) {
		for(Node n : overlay.getChildren()) {
			if(n instanceof Transition) {
				Transition t = (Transition) n;
				String methodName = t.getMethodName();
				String lastTwoChars = methodName.length() > 2 ? methodName.substring(methodName.length() - 2) : methodName; //get last two characters
				if(lastTwoChars.equals("()"))
					methodName = methodName.substring(0, methodName.length()-2); //strip last two characters if ()
				String[] s = {methodName, t.getStartId(), t.getEndId()};
				methods.add(s);
 			}
		}
	}
	
	/*
	 * Gets all guard conditions from transitions, splits them into three parts (e.g. "x", ">", "y"),
	 * Then maps these conditions with the appropriate method.
	 * When exported, these form part of the method body
	 */
	private void getVariables(Pane overlay) {
		for(Node n : overlay.getChildren()) {
			if(n instanceof Transition) {
				Transition t = (Transition) n;
				for(String s : t.getGuards()) {
					String[] splitGuards = parseGuard(s);
					if (splitGuards != null) {
						String methodName = t.getMethodName();
						String lastTwoChars = methodName.length() > 2 ? methodName.substring(methodName.length() - 2) : methodName; //get last two characters
						if(lastTwoChars.equals("()"))
							methodName = methodName.substring(0, methodName.length()-2); //strip last two characters if ()
						variables.put(methodName, splitGuards);
					}
				}
			}
		}
	}
	
	/*
	 * Takes a guard statement and splits it up if formatted as a boolean condition
	 */
	private String[] parseGuard(String guard) {
		// wouldn't be able to format as a valid condition otherwise
		if(guard.contains("<") || guard.contains(">") || guard.contains("=")) {
			String[] splitGuard = new String[3];
			//use the operator to separate the condition into its initial value
			String[] split = guard.split("(<=)|(>=)|[<>=]", 2); //match <= or >= or finally individual tokens, split into two groups only
			splitGuard[0] = split[0].trim();
			splitGuard[2] = split[1].trim();
			if(guard.contains("<="))
				splitGuard[1] = "<=";
			else if (guard.contains(">="))
					splitGuard[1] = ">=";
			else if (guard.contains("<"))
				splitGuard[1] = ">";
			else if (guard.contains(">"))
				splitGuard[1] = ">";
			else 
				splitGuard[1] = "==";
			return splitGuard;
		}
		else
			return null;
	}
	
	/*
	 * Takes the collected variables and turns them to a printable form
	 * Makes an attempt to determine what the correct type is for each
	 * variable
	 */
	private void formatVariables() {
		//examine all the guard variables
		for(String[] s : variables.values()) {
			try {
				Integer.parseInt(s[2]);
				//we're comparing to an integer, therefore
				guardVariables.add("private int " + s[0] + ";");
			}
			catch (NumberFormatException e) {
				try {
					Double.parseDouble(s[2]);
					//if here, it's a double
					guardVariables.add("private double " + s[0] + ";");
				}
				catch (NumberFormatException e2) {
					if(s[2].toLowerCase().equals("true") || s[2].toLowerCase().equals("false")) { //is it formatted to be a boolean?
						guardVariables.add("private boolean " + s[0] + ";");
					}
					else {
						guardVariables.add("private Object " + s[0] + ";"); //unclear what it's compared to--another variable, a string?
					}
				}
			}
		}
	}
	

	private void writeFile(String className) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialFileName(className+".java");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
		File file = fileChooser.showSaveDialog(null);
		
		try {
			FileWriter writer = new FileWriter(file);
			writer.write("public class " + className + " {\n"); //write class name
			
			//write states and state enum
			writer.write("\tpublic enum State {\n\t\t");
			int size = states.size();
			for(int i = 0; i < size; i++) {
				writer.write(states.get(i).toUpperCase());
				if(i+1<size)
					writer.write(", "); //every state except the last, print separating comma
			}
			writer.write("\n\t}\n\n");
			writer.write("\tprivate State state;\n"); //state enum
			
			for(String s : guardVariables) {
				writer.write("\t" + s + "\n"); //write out all guard variables
			}
			
			//constructor
			writer.write("\n\tpublic " + className +"() {\n"); //begin constructor
			if(states.contains("initial"))
				writer.write("\t\tstate = INITIAL;\n");
			writer.write("\t}\n"); //end constructor
			
			//methods
			for(String[] s : methods) {
				writer.write("\n\tpublic void " + s[0] + "(){\n"); //method header
				writer.write("\t\tif(" + s[1] +"==true"); //if in the correct state
				variables.forEach((method, guards) -> { //add guards if relevant
					if(s[0].equals(method)) {
						try {
							writer.write(" && " + guards[0] + guards[1] + guards[2]);
							//unfortunately will not syntax correctly for object comparisons -- still prints == not .equals()
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				writer.write(") {"); //close if statement header
				writer.write("\n\t\t\t state = " + s[2].toUpperCase());//change state
				writer.write("\n\t\t}\n\t}\n"); //close statement and method.
			}
			writer.write("}"); //close class
			writer.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
