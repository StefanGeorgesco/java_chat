package fr.sgo.entity;

import fr.sgo.service.IDGenerator;
import fr.sgo.service.ProfileInfo;

public class HostedGroupChat extends GroupChat {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6948740240113777937L;
	private String id;

	public HostedGroupChat(String name) {
		super(name);
		this.id = IDGenerator.newId();
		correspondents.add(new Correspondent("", ProfileInfo.getInstance().getUserName(), true));
	}

	@Override
	public String getId() {
		return id;
	}

}
