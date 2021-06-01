package fr.sgo.entity;

import fr.sgo.app.App;

public class CorrespondentChat extends Chat {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5022978275100350854L;
	private Correspondent correspondent;

	public CorrespondentChat(App app, Correspondent correspondent) {
		super(app);
		this.correspondent = correspondent;
	}
	
	public Correspondent getCorrespondent() {
		return correspondent;
	}

	@Override
	public String getId() {
		return correspondent.getPairingInfo().getOutId();
	}

}
