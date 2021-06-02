package fr.sgo.controller;

/**
 * Abstract class Controller.
 * 
 * Represents a controller
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
public abstract class Controller implements Runnable {
	protected String actionName;

	public Controller(String actionName) {
		this.actionName = actionName;
	}

	public String getActionName() {
		return actionName;
	}

	public void execute() {
		new Thread(this).start();
	}
	
}
