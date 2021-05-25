package fr.sgo.entity;

public class OutMessage extends Message {

	public OutMessage(String contents) {
		super(contents);
		this.timeWritten = System.currentTimeMillis();
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
