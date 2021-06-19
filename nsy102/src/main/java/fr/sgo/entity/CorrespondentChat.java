package fr.sgo.entity;

/**
 * Class CorrespondentChat
 * 
 * Represents a chat with one correspondent
 *
 * @author Stefan Georgesco
 * @version 1.0
 */
public class CorrespondentChat extends Chat {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5022978275100350854L;
	private Correspondent correspondent;

	public CorrespondentChat(Correspondent correspondent) {
		super();
		this.correspondent = correspondent;
	}
	
	public Correspondent getCorrespondent() {
		return correspondent;
	}
	
	public void setCorrespondent(Correspondent correspondent) {
		this.correspondent = correspondent;
	}

	@Override
	public String getId() {
		return correspondent.getPairingInfo().getOutId();
	}
	
	@Override
	public String toString() {
		return getCorrespondent().getUserName() + ", chatId=" + getId();
	}

}
