package fr.sgo.controller;

import fr.sgo.app.App;

/**
 * Abstract class Controller.
 * 
 * Represents a controller
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
public abstract class Controller implements Runnable {
	protected App app;
	protected String actionName;

	public Controller(App app, String actionName) {
		this.app = app;
		this.actionName = actionName;
	}

	public App getApp() {
		return app;
	}

	public String getActionName() {
		return actionName;
	}

	public void execute() {
		new Thread(this).start();
	}
	
}
