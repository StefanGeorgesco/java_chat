package fr.sgo.entity;

import fr.sgo.app.App;

public abstract class GroupChat extends Chat {

	/**
	 * 
	 */
	private static final long serialVersionUID = 279792385811091040L;
	String name;

	public GroupChat(App app, String name) {
		super(app);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
