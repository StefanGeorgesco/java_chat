package fr.sgo.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.service.CorrespondentServiceInfo;
import fr.sgo.service.CorrespondentServiceLocator;
import fr.sgo.service.MessagingService;
import fr.sgo.service.Storage;

/**
 * Class CorrespondentManager
 * 
 * Manages correspondents
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class CorrespondentManager extends Observable implements Observer {
	private static CorrespondentManager instance = null;
	private Map<String, Correspondent> correspondents;
	private static String objectName;
	private App app;

	@SuppressWarnings("unchecked")
	private CorrespondentManager(App app) {
		this.app = app;
		this.correspondents = Collections.synchronizedMap(new HashMap<String, Correspondent>());
		objectName = app.getProfileInfo().getUserId();
		Collection<Correspondent> pairedCorrespondents = (Collection<Correspondent>) Storage.restore(objectName);
		if (pairedCorrespondents != null) {
			for (Correspondent c : pairedCorrespondents) {
				c.setOnline(false);
				correspondents.put(c.getUserId(), c);
			}
		}
	}

	public static CorrespondentManager getInstance(App app) {
		if (instance == null)
			instance = new CorrespondentManager(app);
		return instance;
	}

	public Collection<Correspondent> getCorrespondents() {
		return correspondents.values();
	}

	public Collection<Correspondent> getPairedCorrespondents() {
		Collection<Correspondent> collection = new HashSet<Correspondent>();
		for (Correspondent c : correspondents.values()) {
			if (c.isPaired())
				collection.add(c);
		}
		return collection;
	}

	public Collection<Correspondent> getUnpairedCorrespondents() {
		Collection<Correspondent> collection = new HashSet<Correspondent>();
		for (Correspondent c : correspondents.values()) {
			if (!c.isPaired())
				collection.add(c);
		}
		return collection;
	}

	public Collection<String> getCorrespondentsUserIds() {
		return correspondents.keySet();
	}

	public Collection<String> getPairedCorrespondentsUserIds() {
		Collection<String> collection = new HashSet<String>();
		for (Correspondent c : correspondents.values()) {
			if (c.isPaired())
				collection.add(c.getUserId());
		}
		return collection;
	}

	public Collection<String> getUnpairedCorrespondentsUserIds() {
		Collection<String> collection = new HashSet<String>();
		for (Correspondent c : correspondents.values()) {
			if (!c.isPaired())
				collection.add(c.getUserId());
		}
		return collection;
	}

	public Correspondent getCorrespondent(String userId) {
		return correspondents.get(userId);
	}

	public void add(Correspondent correspondent) {
		correspondents.put(correspondent.getUserId(), correspondent);
		reportChange(correspondent);
	}
	
	public void reportChange(Correspondent correspondent) {
		if (correspondent.isPaired()) {
			final MessagingService messagingService = CorrespondentManager.this.app.getMessagingService();
			messagingService.setInMessagingHandler(correspondent);
			Storage.save(getPairedCorrespondents(), objectName);
		}
		setChanged();
		notifyObservers(correspondent);
	}

	public void update(Observable observable, Object arg) {
		final CorrespondentServiceLocator correspondentServiceLocator = (CorrespondentServiceLocator) observable;
		final CorrespondentServiceInfo correspondentServiceInfo = (CorrespondentServiceInfo) arg;
		final MessagingService messagingService = CorrespondentManager.this.app.getMessagingService();
		new Thread() {
			@Override
			public void run() {
				if (correspondentServiceInfo != null) { // got info
					String userId = correspondentServiceInfo.getUserId();
					assert !userId.equals("Toto"); // DEBUG
					String myUserId = CorrespondentManager.this.app.getProfileInfo().getUserId();
					if (!userId.equals(myUserId)) { // not me
						Correspondent correspondent = correspondents.get(userId); // get correspondent if present
						if (correspondentServiceLocator.lookup(userId) == null) { // service removed
							if (correspondent != null) { // present
								if (correspondent.isPaired()) { // paired
									correspondent.setOnline(false); // set offline
									messagingService.unsetInMessagingHandler(correspondent);
								} else { // unpaired
									correspondents.remove(correspondent.getUserId()); // remove
								}
							}
						} else { // service resolved
							if (correspondent != null) { // present
								correspondent.setUserName(correspondentServiceInfo.getUserName()); // update name
								correspondent.setOnline(true); // set online
								if (correspondent.isPaired()) // paired
									messagingService.setInMessagingHandler(correspondent);
							} else { // absent
								// new online unpaired correspondent
								correspondent = new Correspondent(userId, correspondentServiceInfo.getUserName(), true);
								correspondents.put(userId, correspondent);
							}
						}
						setChanged();
						notifyObservers(correspondent);
					}
				}
			}
		}.start();
	}

}
