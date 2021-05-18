package fr.sgo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.service.CorrespondentServiceInfo;
import fr.sgo.service.CorrespondentServiceLocator;
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
	private Map<String, Correspondent> pairedCorrespondents;
	private Map<String, Correspondent> unpairedCorrespondents;
	private static final String objectName = "pairedcorrespondents";
	private App app;

	@SuppressWarnings("unchecked")
	private CorrespondentManager(App app) {
		this.app = app;
		pairedCorrespondents = (Map<String, Correspondent>) Storage.restore(objectName);
		if (pairedCorrespondents == null)
			pairedCorrespondents = Collections.synchronizedMap(new HashMap<String, Correspondent>());
		for (Correspondent c : pairedCorrespondents.values()) {
			c.setOnline(false);
		}
		unpairedCorrespondents = Collections.synchronizedMap(new HashMap<String, Correspondent>());
	}

	public static CorrespondentManager getInstance(App app) {
		if (instance == null)
			instance = new CorrespondentManager(app);
		return instance;
	}

	public List<Correspondent> getCorrespondents() {
		List<Correspondent> liste = new ArrayList<Correspondent>(pairedCorrespondents.values());
		liste.addAll(unpairedCorrespondents.values());
		return liste;
	}

	public List<Correspondent> getPairedCorrespondents() {
		return new ArrayList<Correspondent>(pairedCorrespondents.values());
	}

	public List<Correspondent> getUnpairedCorrespondents() {
		return new ArrayList<Correspondent>(unpairedCorrespondents.values());
	}

	public List<String> getCorrespondentsUserIds() {
		List<String> liste = new ArrayList<String>(pairedCorrespondents.keySet());
		liste.addAll(unpairedCorrespondents.keySet());
		return new ArrayList<String>(liste);
	}

	public List<String> getPairedCorrespondentsUserIds() {
		return new ArrayList<String>(pairedCorrespondents.keySet());
	}

	public List<String> getUnpairedCorrespondentsUserIds() {
		return new ArrayList<String>(unpairedCorrespondents.keySet());
	}

	public Correspondent getCorrespondent(String userId) {
		Correspondent correspondent = pairedCorrespondents.get(userId);
		if (correspondent != null)
			return correspondent;
		else
			return unpairedCorrespondents.get(userId);
	}

	public void update(Observable observable, Object arg) {
		final CorrespondentServiceLocator correspondentServiceLocator = (CorrespondentServiceLocator) observable;
		final CorrespondentServiceInfo correspondentServiceInfo = (CorrespondentServiceInfo) arg;
		new Thread() {
			@Override
			public void run() {
				if (correspondentServiceInfo != null) {
					String userId = correspondentServiceInfo.getUserId();
					String myUserId = CorrespondentManager.this.app.getProfileInfo().getUserId();
					if (!userId.equals(myUserId)) {
						Correspondent correspondent = null;
						if (correspondentServiceLocator.lookup(userId) == null) // service removed
						{
							correspondent = unpairedCorrespondents.remove(userId); // in case service was registered as
																					// unpaired
							if (correspondent == null)
							{
								correspondent = pairedCorrespondents.get(userId);
								if (correspondent != null) // if service was registered as paired
									correspondent.setOnline(false); // turns offline
							}
						} else // service resolved
						{
							if (pairedCorrespondents.containsKey(userId)) // service already paired
							{
								correspondent = pairedCorrespondents.get(userId);
								correspondent.setUserName(correspondentServiceInfo.getUserName()); // in case name
																									// has
																									// changed
								correspondent.setOnline(true); // turns online
							} else // service unpaired
							{
								 // new online unpaired correspondent
								correspondent = new Correspondent(userId, correspondentServiceInfo.getUserName(), true);
								unpairedCorrespondents.put(userId, correspondent);
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
