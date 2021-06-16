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

	public synchronized ChatView getChatView(Chat chat) {
		ChatView chatView = chatViews.get(chat);
		if (chatView == null) {
			chatView = new ChatView(chat);
			chatViews.put(chat, chatView);
		}
		return chatView;
	}

	private void removeChatView(Chat chat) {
		chatViews.remove(chat);
	}

	@Override
	public void update(Observable o, Object arg) {
		Chat chat = (Chat) arg;
		ChatView chatView = getChatView(chat);
		if (!ChatManager.getInstance().existsChat(chat)) {
			removeChatView(chat);
			chatView.dispose();
		}
	}

}
