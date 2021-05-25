package fr.sgo.entity;

public class InMessage extends Message {
	private Correspondent correspondent;

	public InMessage(String contents, long timeWritten) {
		super(contents);
		this.timeWritten = timeWritten;
	}
	
	public Correspondent getCorrespondent() {
		return correspondent;
	}
	
	public void setCorrespondent(Correspondent correspondent) {
		this.correspondent = correspondent;
	}
	
}
