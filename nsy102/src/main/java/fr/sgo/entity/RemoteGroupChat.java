package fr.sgo.entity;

import fr.sgo.service.ProfileInfo;

public class RemoteGroupChat extends GroupChat {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3659564476019099765L;
	private Correspondent correspondent;
	private String id;
	

	public RemoteGroupChat(Correspondent correspondent, String id, String name) {
		super(name);
		this.correspondent = correspondent;
		this.id = id;
		correspondents.add(correspondent);
		correspondents.add(new Correspondent(ProfileInfo.getInstance().getUserId(),
				ProfileInfo.getInstance().getUserName(), true));
	}

	public Correspondent getCorrespondent() {
		return correspondent;
	}

	public void setCorrespondent(Correspondent correspondent) {
		this.correspondent = correspondent;
	}

	@Override
	public String getId() {
		return id;
	}

}
