package fr.sgo.entity;

public class OutMessage extends Message {

	public OutMessage(String contents) {
		super(contents, System.currentTimeMillis());
	}

}
