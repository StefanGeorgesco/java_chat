package fr.sgo.service;

import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

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
	private Map<String, String> match;

	private CorrespondentServiceLocator() {
		this.map = Collections.synchronizedMap(new HashMap<String, CorrespondentServiceInfo>());
		this.match = Collections.synchronizedMap(new HashMap<String, String>());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					jmdns.removeServiceListener(ServiceAgent.SERVICE_TYPE, correspondentServiceListener);
					correspondentServiceListener = null;
					jmdns.unregisterAllServices();
					jmdns.close();
				} catch (Exception e) {
				} finally {
					jmdns = null;
				}
				map.clear();
				match.clear();
				System.gc();
			}
		});
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

	public List<String> allUserIds() {
		return new LinkedList<String>(map.keySet());
	}

	private class CorrespondentServiceListener implements ServiceListener {
		public void serviceAdded(ServiceEvent event) {
		}

		public void serviceRemoved(ServiceEvent event) {
			final String serviceName = event.getName();
			final String userId = match.get(serviceName);
			final CorrespondentServiceInfo correspondentServiceInfo = map.get(userId);
			new Thread() {
				@Override
				public void run() {
					map.remove(userId);
					match.remove(serviceName);
					CorrespondentServiceLocator.this.setChanged();
					CorrespondentServiceLocator.this.notifyObservers(correspondentServiceInfo);
				}
			}.start();
		}

		public void serviceResolved(ServiceEvent event) {
			assert !event.getInfo().getPropertyString("userId").equals("Toto"); // DEBUG
			new ResolvedServiceInfo(event.getName(), event.getInfo()).start();
		}
	}

	private class ResolvedServiceInfo extends Thread {
		private String userId;
		private String userName;
		private String urlString;
		private String serviceName;

		public ResolvedServiceInfo(String serviceName, ServiceInfo si) {
			this.userId = si.getPropertyString("userId");
			assert !this.userId.equals("Toto"); // DEBUG
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
				if (service.isActive()) {
					assert !userId.equals("Toto"); // DEBUG
					CorrespondentServiceInfo correspondentServiceInfo = new CorrespondentServiceInfo(userId, userName,
							host, service);
					map.put(userId, correspondentServiceInfo);
					match.put(serviceName, userId);
					CorrespondentServiceLocator.this.setChanged();
					assert !correspondentServiceInfo.getUserId().equals("Toto"); // DEBUG
					CorrespondentServiceLocator.this.notifyObservers(correspondentServiceInfo);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
