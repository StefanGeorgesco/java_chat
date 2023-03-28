package fr.sgo.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

import fr.sgo.service.IDGenerator;
import fr.sgo.service.MessagingService;

/**
 * Abstract class Chat
 * 
 * A generic chat object
 *
 * @author Stefan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public abstract class Chat extends Observable implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5801761275943085099L;
	protected Set<Message> messages;
	protected String subscriberName;

	public Chat() {
		this.messages = Collections.synchronizedSet(new TreeSet<Message>());
		this.subscriberName = IDGenerator.newId();
	}

	public Collection<Message> getMessages() {
		return messages;
	}
	
	public String getSubscriberName() {
		return subscriberName;
	}

	public void reportChange() {
		setChanged();
		notifyObservers();
	}

	private void reportChange(Message message) {
		setChanged();
		notifyObservers(message);
	}

	public void addMessage(Message message) {
		messages.add(message);
		reportChange(message);
	}

	public abstract String getId();

	public void sendMessage(OutMessage message) {
		final OutMessage m = message;
		addMessage(m);
		new Thread() {
			@Override
			public void run() {
				MessagingService.getInstance().sendMessage(Chat.this, m);
			}
		}.start();
	}

}
