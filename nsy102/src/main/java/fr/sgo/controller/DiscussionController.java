package fr.sgo.controller;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;

/**
 * Class DiscussionController
 * 
 * Begins a discussion with a correspondent.
 *
 * @author Stéfan Gerogesco
 * @version 1.0
 */
public class DiscussionController extends Controller {
	private Correspondent correspondent;

	public DiscussionController(App app, String actionName, Correspondent correspondent) {
		super(app, actionName);
		this.correspondent = correspondent;
	}

	public void run() {
		System.out.println("Discussion avec " + correspondent.getUserName() + "...");
	}

}
