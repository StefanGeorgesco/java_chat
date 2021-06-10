package fr.sgo.service;

import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import fr.sgo.app.App;
import fr.sgo.controller.RMIController;

/**
 * Class ServiceAgent
 * 
 * Publishes services (RMI & JmDNS)
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class ServiceAgent {
	private static ServiceAgent instance = null;
	public static final String SERVICE_TYPE = "_monserviceRMI._tcp.local.";
	private static final String serviceName = IDGenerator.newId();
	private static Registry registry;
	private static JmDNS jmdns = null;

	private ServiceAgent() {
		java.util.Properties p = System.getProperties();
		p.put("java.security.policy", "policy.all");
		System.setProperties(p);
		System.setSecurityManager(new RMISecurityManager());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					System.out.println("retrait des services RMI et mDNS...");
					registry.unbind(serviceName);
					jmdns.unregisterAllServices();
					jmdns.close();
				} catch (Exception e) {
				}
			}
		});
	}

	public static synchronized ServiceAgent getInstance() {
		if (instance == null)
			instance = new ServiceAgent();
		return instance;
	}

	public void publishServices(int delay) {
		new ServicePublisher(delay);
	}

	private class ServicePublisher extends Thread {
		private int delay;

		public ServicePublisher(int delay) {
			this.delay = delay;
			start();
		}

		@Override
		public void run() {
			ProfileInfo profileInfo = ProfileInfo.getInstance();
			try {
				Thread.sleep(delay);
			} catch (java.lang.InterruptedException ie) {
				ie.printStackTrace();
			}
			try {
				registry = LocateRegistry.createRegistry(profileInfo.getRMIPort());
			} catch (RemoteException re1) {
				try {
					registry = LocateRegistry.getRegistry(profileInfo.getRMIPort());
				} catch (RemoteException re2) {
					re2.printStackTrace();
					System.exit(1);
				}
			}
			if (App.T)
				System.out.println("registre RMI créé ou lié");
			try {
				registry.rebind(serviceName, RMIController.getInstance());
			} catch (RemoteException re3) {
				re3.printStackTrace();
				System.exit(1);
			}
			if (App.T)
				System.out.println("service enregistré dans le registre RMI");
			Map<String, String> props = new HashMap<String, String>();
			props.put("userId", profileInfo.getUserId());
			props.put("userName", profileInfo.getUserName());
			try {
				ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, serviceName,
						profileInfo.getRMIPort(), 0, 0, props);
				jmdns = JmDNS.create();
				jmdns.registerService(serviceInfo);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			if (App.T)
				System.out.println("service mDNS publié");
			
		}
	}
}
