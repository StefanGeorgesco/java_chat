package fr.sgo.entity;

public class InMessage extends Message {
	private Correspondent author;

	public InMessage(String contents, long timeWritten, Correspondent author) {
		super(contents, timeWritten);
	}
	
	public Correspondent getAuthor() {
		return author;
	}
	
}
