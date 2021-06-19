package fr.sgo.service;

import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import fr.sgo.app.App;
import fr.sgo.controller.RMIService;

/**
 * Class CorrespondentServiceLocator
 * 
 * Keep a record of live Correspondents based on mDNS service
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings({ "deprecation" })
public class CorrespondentServiceLocator extends Observable {
	private static CorrespondentServiceLocator instance = null;
	private JmDNS jmdns;
	private CorrespondentServiceListener correspondentServiceListener;
	private Map<String, CorrespondentServiceInfo> map;

	private CorrespondentServiceLocator() {
		this.map = Collections.synchronizedMap(new HashMap<String, CorrespondentServiceInfo>());
	}

	public static synchronized CorrespondentServiceLocator getInstance() {
		if (instance == null)
			instance = new CorrespondentServiceLocator();
		return instance;
	}

	public void open() {
		try {
			correspondentServiceListener = new CorrespondentServiceListener();
			jmdns = JmDNS.create();
			jmdns.addServiceListener(ServiceAgent.SERVICE_TYPE, correspondentServiceListener);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public CorrespondentServiceInfo lookup(String userId) {
		return map.get(userId);
	}

	private class CorrespondentServiceListener implements ServiceListener {
		public void serviceAdded(ServiceEvent event) {
			if (App.T)
				System.out.println("service ajouté : " + event.getName());
			ServiceInfo info = event.getDNS().getServiceInfo(event.getType(), event.getName());
			new ResolvedServiceInfo(event.getName(), info).start();
		}

		public void serviceRemoved(ServiceEvent event) {
			if (App.T)
				System.out.println("service retiré : " + event.getName() + "("
						+ event.getInfo().getPropertyString("userName") + ")");
			final String userId = event.getName();
			final CorrespondentServiceInfo correspondentServiceInfo = map.get(userId);
			new Thread() {
				@Override
				public void run() {
					map.remove(userId);
					CorrespondentServiceLocator.this.setChanged();
					CorrespondentServiceLocator.this.notifyObservers(correspondentServiceInfo);
				}
			}.start();
		}

		public void serviceResolved(ServiceEvent event) {
			if (App.T)
				System.out.println("service résolu : " + event.getName() + "("
						+ event.getInfo().getPropertyString("userName") + ")");
			assert event.getInfo().getPropertyString("userId") != null; // DEBUG
		}
	}

	private class ResolvedServiceInfo extends Thread {
		private String userId;
		private String userName;
		private String urlString;
		private String serviceName;

		public ResolvedServiceInfo(String serviceName, ServiceInfo si) {
			this.userId = si.getPropertyString("userId");
			assert this.userId != null; // DEBUG
			this.userName = si.getPropertyString("userName");
			this.urlString = si.getURLs()[0];
			this.serviceName = serviceName;
		}

		public void run() {
			try {
				URL url = new URL(urlString);
				String host = url.getHost();
				int port = url.getPort();
				Registry registry = LocateRegistry.getRegistry(host, port);
				RMIService service = (RMIService) registry.lookup(serviceName);
				if (service.isOnline()) {
					if (App.T)
						System.out.println("service actif pour " + userName);
					assert userId != null; // DEBUG
					CorrespondentServiceInfo correspondentServiceInfo = new CorrespondentServiceInfo(userId, userName,
							host, service);
					map.put(userId, correspondentServiceInfo);
					CorrespondentServiceLocator.this.setChanged();
					assert correspondentServiceInfo.getUserId() != null; // DEBUG
					CorrespondentServiceLocator.this.notifyObservers(correspondentServiceInfo);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
