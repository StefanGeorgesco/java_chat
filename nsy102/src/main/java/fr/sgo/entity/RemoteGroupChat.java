package fr.sgo.entity;

public class RemoteGroupChat extends GroupChat {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3659564476019099765L;
	private Correspondent correspondent;
	

	public RemoteGroupChat(String name, String id, Correspondent correspondent) {
		super(name);
		this.id = id;
		this.correspondent = correspondent;
		correspondents.add(correspondent);
	}

	public Correspondent getCorrespondent() {
		return correspondent;
	}

	public void setCorrespondent(Correspondent correspondent) {
		this.correspondent = correspondent;
	}

}
