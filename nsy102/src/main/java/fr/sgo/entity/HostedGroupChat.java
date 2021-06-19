package fr.sgo.entity;

import fr.sgo.service.IDGenerator;

/**
 * Class HostedGroupChat
 * 
 * Represents a local (hosted) group chat
 *
 * @author Stefan Georgesco
 * @version 1.0
 */
public class HostedGroupChat extends GroupChat {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6948740240113777937L;

	public HostedGroupChat(String name) {
		super(name);
		this.id = IDGenerator.newId();
	}

	@Override
	public String toString() {
		return getName() + " , hosted, chatId=" + getId();
	}

}
