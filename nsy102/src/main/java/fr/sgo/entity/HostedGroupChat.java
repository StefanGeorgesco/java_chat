package fr.sgo.entity;

import fr.sgo.app.App;
import fr.sgo.service.IDGenerator;

public class HostedGroupChat extends GroupChat {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6948740240113777937L;
	private String id;

	public HostedGroupChat(App app, String name) {
		super(app, name);
		this.id = IDGenerator.newId();
	}

	@Override
	public String getId() {
		return id;
	}

}
