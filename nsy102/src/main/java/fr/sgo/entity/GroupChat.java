package fr.sgo.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import fr.sgo.service.IDGenerator;

public abstract class GroupChat extends Chat {

	/**
	 * 
	 */
	private static final long serialVersionUID = 279792385811091040L;
	protected String name;
	protected String subscriberName;
	protected Set<Correspondent> correspondents;

	public GroupChat(String name) {
		super();
		this.name = name;
		this.subscriberName = IDGenerator.newId();
		this.correspondents = Collections.synchronizedSet(new TreeSet<Correspondent>());
	}

	public String getName() {
		return name;
	}

	@Override
	public String getSubscriberName() {
		return subscriberName;
	}

	public Collection<Correspondent> getCorrespondents() {
		return correspondents;
	}

	@SuppressWarnings("deprecation")
	public void addCorrespondent(Correspondent correspondent) {
		if (correspondents.add(correspondent)) {
			setChanged();
			notifyObservers();
		}
	}

	@SuppressWarnings("deprecation")
	public void addCorrespondents(Collection<Correspondent> correspondents) {
		if (this.correspondents.addAll(correspondents)) {
			setChanged();
			notifyObservers();
		}
	}

	@SuppressWarnings("deprecation")
	public void removeCorrespondent(Correspondent correspondent) {
		if (correspondents.remove(correspondent)) {
			setChanged();
			notifyObservers();
		}
	}

	public void replaceCorrespondent(Correspondent correspondentToRemove, Correspondent correspondentToAdd) {
		correspondents.remove(correspondentToRemove);
		correspondents.add(correspondentToAdd);
	}

}
