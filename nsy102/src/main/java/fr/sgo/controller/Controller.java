package fr.sgo.controller;

import fr.sgo.app.App;
import fr.sgo.view.MainView;

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
	protected MainView mainView;

	public Controller(App app, String actionName, MainView mainView) {
		this.app = app;
		this.actionName = actionName;
		this.mainView = mainView;
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
