package fr.sgo.controller;

import javax.swing.JOptionPane;

import fr.sgo.entity.HostedGroupChat;
import fr.sgo.model.ChatManager;

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

	public void createGroupChat() {
        String name = "";
        while (name != null && name.length() == 0) {
            name = JOptionPane.showInputDialog("Nom du groupe de discussion");
        }
        if (name != null) {
        	ChatManager.getInstance().addGroupChat(new HostedGroupChat(name));
        }
	}
	
}
