package fr.sgo.entity;

public abstract class Message implements Comparable<Message> {
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
