package fr.sgo.controller;

/**
 * Abstract class ActionHandler.
 * 
 * Subclasses Handle button-attached specific actions
 *
 * @author Stefan Georgesco
 * @version 1.0
 */
public abstract class ActionHandler implements Runnable {
	protected String actionName;

	public ActionHandler(String actionName) {
		this.actionName = actionName;
	}

	public String getActionName() {
		return actionName;
	}

	public void execute() {
		new Thread(this).start();
	}
	
}
