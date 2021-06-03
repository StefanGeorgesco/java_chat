package fr.sgo.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import fr.sgo.entity.Chat;
import fr.sgo.model.ChatManager;

@SuppressWarnings("deprecation")
public class ChatViewContainer implements Observer {
	private static ChatViewContainer instance = null;
	private Map<Chat, ChatView> chatViews;

	private ChatViewContainer() {
		this.chatViews = Collections.synchronizedMap(new HashMap<Chat, ChatView>());
	}

	public static synchronized ChatViewContainer getInstance() {
		if (instance == null)
			instance = new ChatViewContainer();
		return instance;
	}

	public void start() {
		ChatManager chatManager = ChatManager.getInstance();
		for (Chat chat : chatManager.getChats()) {
			addChatView(chat);
		}
		chatManager.addObserver(this);
	}

	public void addChatView(ChatView chatView) {
		chatViews.put(chatView.getChat(), chatView);
	}

	public void addChatView(Chat chat) {
		if (chatViews.get(chat) == null)
			chatViews.put(chat, new ChatView(chat));
	}

	public ChatView getChatView(Chat chat) throws Exception {
		ChatView chatView = chatViews.get(chat);
		if (chatView == null) {
			chatView = new ChatView(chat);
			chatViews.put(chat, chatView);
		}
		chatView.setVisible(true);
		return chatView;
	}

	@Override
	public void update(Observable o, Object arg) {
		new Thread() {
			@Override
			public void run() {
				Chat chat = (Chat) arg;
				addChatView(chat);
			}
		}.start();
	}

}
