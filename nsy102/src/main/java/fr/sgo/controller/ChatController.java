package fr.sgo.controller;

import fr.sgo.entity.Chat;
import fr.sgo.view.ChatViewContainer;

public class ChatController {
	private static ChatController instance = null;

	private ChatController() {
	}
	
	public static synchronized ChatController getInstance() {
		if (instance == null) {
			instance = new ChatController();
		}
		return instance;
	}
	
	public void showView(Chat chat) {
		ChatViewContainer.getInstance().getChatView(chat);
	}
	
}
