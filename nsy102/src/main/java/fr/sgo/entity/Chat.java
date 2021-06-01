package fr.sgo.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

import fr.sgo.app.App;

@SuppressWarnings("deprecation")
public abstract class Chat extends Observable implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5801761275943085099L;
	protected Set<Message> messages;
	protected App app;

	public Chat(App app) {
		this.app = app;
		this.messages = Collections.synchronizedSet(new TreeSet<Message>());
	}

	public Collection<Message> getMessages() {
		return messages;
	}

	public void addMessage(Message message) {
		messages.add(message);
		setChanged();
		notifyObservers(message);
	}

	public abstract String getId();

	public void sendMessage(OutMessage message) {
		app.getMessagingService().sendMessage(this.getId(), message);
		addMessage(message);
	}

}
