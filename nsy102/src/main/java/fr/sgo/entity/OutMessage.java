package fr.sgo.entity;

public class OutMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6257443243989387915L;
	String userId;

	public OutMessage(String contents, String userId) {
		super(contents, System.currentTimeMillis());
		this.userId = userId;
	}
	
	public String getUserId() {
		return userId;
	}

}
