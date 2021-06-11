package fr.sgo.entity;

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
	
}
