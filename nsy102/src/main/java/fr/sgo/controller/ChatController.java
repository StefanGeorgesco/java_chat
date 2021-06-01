package fr.sgo.controller;

import fr.sgo.app.App;
import fr.sgo.entity.Chat;

/**
 * Class ChatController
 * 
 * Begins a discussion with a correspondent.
 *
 * @author Stéfan Gerogesco
 * @version 1.0
 */
public class ChatController extends Controller {
	private Chat chat;

	public ChatController(App app, String actionName, Chat chat) {
		super(app, actionName);
		this.chat = chat;
	}

	public void run() {
		try {
			app.getChatViewContainer().getChatView(chat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
