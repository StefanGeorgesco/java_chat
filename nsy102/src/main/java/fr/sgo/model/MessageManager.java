package fr.sgo.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.Message;
import fr.sgo.entity.OutMessage;

@SuppressWarnings("deprecation")
public class MessageManager extends Observable {
	private static MessageManager instance = null;
	private Map<String, Set<Message>> messages;
	private App app;

	private MessageManager(App app) {
		this.app = app;
		this.messages = Collections.synchronizedMap(new HashMap<String, Set<Message>>());
	}
	
	public static MessageManager getInstance(App app) {
		if (instance == null)
			instance = new MessageManager(app);
		return instance;
	}
	
	public Collection<Message> getMessages(String userId) {
		Set<Message> userMessages = this.messages.get(userId);
		if (userMessages == null) {
			userMessages = Collections.synchronizedSet(new HashSet<Message>());
			this.messages.put(userId, userMessages);
		}
		return userMessages;
	}
	
	public void addMessage(String userId, Message message) {
		Set<Message> userMessages = this.messages.get(userId);
		if (userMessages == null) {
			userMessages = Collections.synchronizedSet(new HashSet<Message>());
			this.messages.put(userId, userMessages);
		}
		userMessages.add(message);
		setChanged();
		notifyObservers(userId);
	}
	
	public void sendMessage(Correspondent correspondent, OutMessage message) {
		String userId = correspondent.getUserId();
		app.getMessagingService().sendMessage(correspondent, message);
		addMessage(userId, message);
	}

}
