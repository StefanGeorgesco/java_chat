package fr.sgo.entity;

import fr.sgo.service.IDGenerator;

public abstract class GroupChat extends Chat {

	/**
	 * 
	 */
	private static final long serialVersionUID = 279792385811091040L;
	String name;
	String subscriberName;

	public GroupChat(String name) {
		super();
		this.name = name;
		this.subscriberName = IDGenerator.newId();
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String getSubscriberName() {
		return subscriberName;
	}

}
