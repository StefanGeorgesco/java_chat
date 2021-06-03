package fr.sgo.controller;

import fr.sgo.entity.Chat;
import fr.sgo.view.ChatViewContainer;

/**
 * Class ChatController_
 * 
 * Begins a discussion with a correspondent.
 *
 * @author Stéfan Gerogesco
 * @version 1.0
 */
public class ChatController_ extends Controller {
	private Chat chat;

	public ChatController_(String actionName, Chat chat) {
		super(actionName);
		this.chat = chat;
	}

	public void run() {
		ChatViewContainer.getInstance().getChatView(chat);
	}

}
