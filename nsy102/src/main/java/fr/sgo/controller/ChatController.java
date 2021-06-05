package fr.sgo.controller;

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
	
}
