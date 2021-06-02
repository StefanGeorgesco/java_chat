package fr.sgo.entity;

public abstract class GroupChat extends Chat {

	/**
	 * 
	 */
	private static final long serialVersionUID = 279792385811091040L;
	String name;

	public GroupChat(String name) {
		super();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
