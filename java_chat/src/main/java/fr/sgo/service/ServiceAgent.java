package fr.sgo.service;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import fr.sgo.app.App;
import fr.sgo.app.AppMBean;
import fr.sgo.controller.RMIController;

/**
 * Class ServiceAgent
 * 
 * Publishes services (RMI, MBean & JmDNS)
 *
 * @author Stefan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class ServiceAgent {
	private static ServiceAgent instance = null;
	public static final String SERVICE_TYPE = "_CorrespondentService._tcp.local.";
	private static String serviceName;
	private static Registry registry;
	private static JmDNS jmdns = null;

	private ServiceAgent() {
		serviceName = ProfileInfo.getInstance().getUserId();
		java.util.Properties p = System.getProperties();
		p.put("java.security.policy", "policy.all");
		System.setProperties(p);
		System.setSecurityManager(new RMISecurityManager());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					if (App.T)
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
		ProfileInfo profileInfo = ProfileInfo.getInstance();

		try {
			registry = LocateRegistry.createRegistry(profileInfo.getRMIPort());
			if (App.T)
				System.out.println("registre RMI créé");
		} catch (RemoteException re1) {
			try {
				registry = LocateRegistry.getRegistry(profileInfo.getRMIPort());
				if (App.T)
					System.out.println("registre RMI lié");
			} catch (RemoteException re2) {
				re2.printStackTrace();
				System.exit(1);
			}
		}
		try {
			registry.rebind(serviceName, RMIController.getInstance());
			if (App.T)
				System.out.println("service enregistré dans le registre RMI");
		} catch (RemoteException re3) {
			re3.printStackTrace();
			System.exit(1);
		}

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		AppMBean bean = App.getInstance();
		ObjectName name = null;
		try {
			name = new ObjectName("app.App:name=AppAgent");
			mbs.registerMBean(bean, name);
			if (App.T)
				System.out.println("service JMX (MBean) publié");
		} catch (Exception e) {
			e.printStackTrace();
		}

		new DNSServicePublisher(delay, profileInfo); // mDNS service
	}

	private class DNSServicePublisher extends Thread {
		private int delay;
		private ProfileInfo profileInfo;

		public DNSServicePublisher(int delay, ProfileInfo profileInfo) {
			this.delay = delay;
			this.profileInfo = profileInfo;
			start();
		}

		@Override
		public void run() {
			try {
				Thread.sleep(delay);
			} catch (java.lang.InterruptedException ie) {
				ie.printStackTrace();
			}
			Map<String, String> props = new HashMap<String, String>();
			props.put("userId", profileInfo.getUserId());
			props.put("userName", profileInfo.getUserName());
			try {
				ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, serviceName, profileInfo.getRMIPort(), 0, 0,
						true, props);
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
