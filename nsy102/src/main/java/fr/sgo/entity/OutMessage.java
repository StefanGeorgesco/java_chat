package fr.sgo.entity;

import fr.sgo.service.ProfileInfo;

public class OutMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6257443243989387915L;
	String userId;

	public OutMessage(String contents) {
		super(contents, System.currentTimeMillis());
		this.userId = ProfileInfo.getInstance().getUserId();
	}
	
	public String getUserId() {
		return userId;
	}

}
