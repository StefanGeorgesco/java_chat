package fr.sgo.entity;

import java.io.Serializable;

public abstract class Message implements Comparable<Message>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8696923613037318840L;
	protected String contents;
	protected long timeWritten;

	public Message(String contents, long timeWritten) {
		this.contents = contents;
		this.timeWritten = timeWritten;
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String getContents() {
		return contents;
	}

	public long getTimeWritten() {
		return timeWritten;
	}

	@SuppressWarnings("deprecation")
	public int compareTo(Message m) {
		return new Long(timeWritten).compareTo(new Long(m.getTimeWritten()));
	}

	@Override
	public String toString() {
		return getContents();
	}
}
