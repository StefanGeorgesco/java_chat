package fr.sgo.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import fr.sgo.app.App;
import fr.sgo.entity.Chat;

@SuppressWarnings("deprecation")
public class ChatViewContainer implements Observer {
	private static ChatViewContainer instance = null;
	private Map<Chat, ChatView> chatViews;
	private App app;

	private ChatViewContainer(App app) {
		this.app = app;
		this.chatViews = Collections.synchronizedMap(new HashMap<Chat, ChatView>());
	}

	public static ChatViewContainer getInstance(App app) {
		if (instance == null)
			instance = new ChatViewContainer(app);
		return instance;
	}

	public void open() {
		for (Chat chat : app.getChatManager().getChats()) {
			addChatView(chat);
		}
		app.getChatManager().addObserver(this);
	}

	public void addChatView(ChatView chatView) {
		chatViews.put(chatView.getChat(), chatView);
	}

	public void addChatView(Chat chat) {
		if (chatViews.get(chat) == null)
			chatViews.put(chat, new ChatView(app, chat));
	}

	public ChatView getChatView(Chat chat) throws Exception {
		ChatView chatView = chatViews.get(chat);
		if (chatView == null) {
			chatView = new ChatView(app, chat);
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
