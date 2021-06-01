package fr.sgo.entity;

import fr.sgo.app.App;

public class RemoteGroupChat extends GroupChat {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3659564476019099765L;
	private Correspondent correspondent;
	private String id;
	

	public RemoteGroupChat(App app, Correspondent correspondent, String id, String name) {
		super(app, name);
		this.correspondent = correspondent;
		this.id = id;
	}

	public Correspondent getCorrespondent() {
		return correspondent;
	}

	@Override
	public String getId() {
		return id;
	}

}
