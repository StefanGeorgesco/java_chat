package fr.sgo.app;

import java.rmi.RemoteException;
import java.util.Observable;
import java.util.Observer;

import fr.sgo.controller.MainController;
import fr.sgo.model.CorrespondentManager;
import fr.sgo.service.CorrespondentServiceLocator;
import fr.sgo.service.ProfileInfo;
import fr.sgo.service.ServiceAgent;
import fr.sgo.view.MainView;

/**
 * Class App
 * 
 * The application
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class App {
	private final static boolean T = true; //
	private static App instance = null;
	private ProfileInfo profileInfo;
	private CorrespondentServiceLocator correspondentServiceLocator;
	private MainController mainController;
	private CorrespondentManager correspondentManager;
	private ServiceAgent serviceAgent;
	private MainView mainView;

	private App() {
		profileInfo = ProfileInfo.getInstance();
		correspondentServiceLocator = CorrespondentServiceLocator.getInstance();
		try {
			mainController = MainController.getInstance(this);
		} catch (RemoteException e) {
			if (T)
				e.printStackTrace();
			System.exit(1);
		}
		correspondentManager = CorrespondentManager.getInstance(this);
		serviceAgent = ServiceAgent.getInstance(this);
		mainView = MainView.getInstance(this);
	}

	public static App getInstance() {
		if (instance == null) {
			instance = new App();
		}
		return instance;
	}

	public void start() {
		if (T)
			System.out.println("profil : " + profileInfo.getUserName());
		if (T)
			System.out.println("correspondants : " + correspondentManager.getCorrespondents());
		if (T)
			correspondentManager.addObserver(new Observer() {
				public void update(Observable observable, Object arg) {
					System.out.println("correspondants : " + correspondentManager.getCorrespondents());
				}
			});
		correspondentServiceLocator.addObserver(correspondentManager);
		correspondentServiceLocator.open();
		serviceAgent.publishServices(3000);
		if (T)
			System.out.println("application démarrée, en attente...");
	}

	public MainController getMainController() {
		return mainController;
	}

	public ProfileInfo getProfileInfo() {
		return profileInfo;
	}

	public CorrespondentServiceLocator getCorrespondentServiceLocator() {
		return correspondentServiceLocator;
	}

	public CorrespondentManager getCorrespondentManager() {
		return correspondentManager;
	}

	public MainView getMainView() {
		return mainView;
	}

	public static void main(String[] args) throws RemoteException {
		App.getInstance().start();
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (java.lang.InterruptedException ie) {
			ie.printStackTrace();
		}
	}
}
