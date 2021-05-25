package fr.sgo.controller;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.OutMessage;

/**
 * Class ChatController
 * 
 * Begins a discussion with a correspondent.
 *
 * @author Stéfan Gerogesco
 * @version 1.0
 */
public class ChatController extends Controller {
	private Correspondent correspondent;

	public ChatController(App app, String actionName, Correspondent correspondent) {
		super(app, actionName);
		this.correspondent = correspondent;
	}

	public void run() {
		System.out.println("Discussion avec " + correspondent.getUserName() + "...");
		// TEST
		app.getMessageManager().sendMessage(correspondent, new OutMessage("ceci est un test"));
	}

}
