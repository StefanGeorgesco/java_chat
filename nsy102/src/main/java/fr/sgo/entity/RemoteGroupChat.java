package fr.sgo.entity;

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
	}

	public Correspondent getCorrespondent() {
		return correspondent;
	}

	@Override
	public String getId() {
		return id;
	}

}
