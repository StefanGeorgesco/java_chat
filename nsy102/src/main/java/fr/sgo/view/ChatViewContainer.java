package fr.sgo.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;

public class ChatViewContainer {
	private static ChatViewContainer instance = null;
	private Map<String,ChatView> chatViews;
	private App app;

	private ChatViewContainer(App app) {
		this.app = app;
		this.chatViews = Collections.synchronizedMap(new HashMap<String,ChatView>());
	}

	public static ChatViewContainer getInstance(App app) {
		if (instance == null)
			instance = new ChatViewContainer(app);
		return instance;
	}
	
	public void open() {
		for (Correspondent correspondent: app.getCorrespondentManager().getPairedCorrespondents()) {
			String userId = correspondent.getUserId();
			if (chatViews.get(userId) == null)
				chatViews.put(userId, new ChatView(app, correspondent));
		}
	}
	
	public ChatView getChatView(Correspondent correspondent) throws Exception {
		if (!correspondent.isPaired()) {
			throw new Exception("correspondent is not paired, cannot get ChatView");
		}
		String userId = correspondent.getUserId();
		ChatView chatView = chatViews.get(userId);
		if (chatView == null) {
			chatView = new ChatView(app, correspondent);
			chatViews.put(userId, chatView);
		}
		chatView.setVisible(true);
		return chatView;
	}

}
