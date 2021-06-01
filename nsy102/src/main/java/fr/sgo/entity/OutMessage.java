package fr.sgo.entity;

public class OutMessage extends Message {
	String userId;

	public OutMessage(String contents, String userId) {
		super(contents, System.currentTimeMillis());
		this.userId = userId;
	}
	
	public String getUserId() {
		return userId;
	}

}
