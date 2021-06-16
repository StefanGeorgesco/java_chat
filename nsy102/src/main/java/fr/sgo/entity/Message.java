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
	}

	public String getContents() {
		return contents;
	}

	public long getTimeWritten() {
		return timeWritten;
	}

	@SuppressWarnings("deprecation")
	public int compareTo(Message m) {
		int res;
		Long time1 = new Long(timeWritten);
		Long time2 = new Long(m.getTimeWritten());
		if (!time1.equals(time2))
			res = time1.compareTo(time2);
		else if (contents.equals(m.getContents()) && (this instanceof OutMessage && m instanceof OutMessage
				|| this instanceof InMessage && m instanceof InMessage
						&& ((InMessage) this).getAuthor().equals(((InMessage) m).getAuthor())))
			res = 0;
		else
			res = -1;
		return res;
	}

	@Override
	public String toString() {
		return getContents();
	}
}
