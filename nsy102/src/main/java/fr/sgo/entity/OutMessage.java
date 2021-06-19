package fr.sgo.entity;

import java.text.DateFormat;
import java.util.Date;

import fr.sgo.service.ProfileInfo;

/**
 * Class OutMessage
 * 
 * Represents a message sent to a chat
 *
 * @author Stefan Georgesco
 * @version 1.0
 */
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

	@Override
	public String toString() {
		DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		return "Me (" + shortDateFormat.format(new Date(getTimeWritten())) + ") : "
				+ getContents();
	}

}
