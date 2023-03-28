package fr.sgo.entity;

import java.text.DateFormat;
import java.util.Date;

/**
 * Class InMessage
 * 
 * Represents a message received from a chat
 *
 * @author Stefan Georgesco
 * @version 1.0
 */
public class InMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2562123831014089572L;
	private Correspondent author;

	public InMessage(String contents, long timeWritten, Correspondent author) {
		super(contents, timeWritten);
		this.author = author;
	}

	public Correspondent getAuthor() {
		return author;
	}

	@Override
	public String toString() {
		DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		return getAuthor().getUserName() + " (" + shortDateFormat.format(new Date(getTimeWritten())) + ") : "
				+ getContents();
	}

}
