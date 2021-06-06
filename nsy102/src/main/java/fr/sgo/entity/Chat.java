package fr.sgo.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

import fr.sgo.service.MessagingService;

@SuppressWarnings("deprecation")
public abstract class Chat extends Observable implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5801761275943085099L;
	protected Set<Message> messages;

	public Chat() {
		this.messages = Collections.synchronizedSet(new TreeSet<Message>());
	}

	public Collection<Message> getMessages() {
		return messages;
	}

	public void addMessage(Message message) {
		messages.add(message);
		setChanged();
		notifyObservers();
	}

	public abstract String getId();

	public abstract String getSubscriberName();
	
	public void sendMessage(OutMessage message) {
		addMessage(message);
		final OutMessage m = message;
		new Thread() {
			@Override
			public void run() {
				MessagingService.getInstance().sendMessage(Chat.this, m);
			}
		}.start();
	}

}
