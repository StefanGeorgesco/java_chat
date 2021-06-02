package fr.sgo.controller;

import fr.sgo.entity.Chat;
import fr.sgo.view.ChatViewContainer;

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

	public ChatController(String actionName, Chat chat) {
		super(actionName);
		this.chat = chat;
	}

	public void run() {
		try {
			ChatViewContainer.getInstance().getChatView(chat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
