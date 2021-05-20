package fr.sgo.controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import fr.sgo.service.ProfileInfo;
import fr.sgo.service.ServiceRMI;
import fr.sgo.app.App;

/**
 * Class MainController
 * 
 * Main application controller.
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
public class MainController extends UnicastRemoteObject implements ServiceRMI {
	private static final long serialVersionUID = 8194586220342790039L;
	private final static boolean T = true; //
	private static MainController instance = null;
	private App app;

	private MainController(App app) throws RemoteException {
		this.app = app;
	}

	public static MainController getInstance(App app) throws RemoteException {
		if (instance == null)
			instance = new MainController(app);
		return instance;
	}
	
	public App getApp() {
		return app;
	}
	
	public boolean isActive() throws RemoteException {
		if (T)
			System.out.println("service actif");
		return true;
	}
	
	public ProfileInfo getProfileInfo() throws RemoteException {
		return app.getProfileInfo();
	}
	
	public void requestPairing(ServiceRMI service, String inId) throws RemoteException {
		if (T)
			System.out.println("Connection required from " + service.getProfileInfo().getUserName()
					+ " with id " + inId);
	}
	
	public void acceptPairingRequest(ServiceRMI service, String inId, String outId) throws RemoteException {
		// TO DO
	}

}
