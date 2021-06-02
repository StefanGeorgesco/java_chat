package fr.sgo.app;

import java.rmi.RemoteException;

import fr.sgo.controller.RMIController;
import fr.sgo.model.CorrespondentManager;
import fr.sgo.service.CorrespondentServiceLocator;
import fr.sgo.service.MessagingService;
import fr.sgo.service.ProfileInfo;
import fr.sgo.service.ServiceAgent;
import fr.sgo.view.ChatViewContainer;
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
	public static final boolean T = true; //
	private static App instance = null;
	private ProfileInfo profileInfo;
	private CorrespondentServiceLocator correspondentServiceLocator;
	private CorrespondentManager correspondentManager;
	private ServiceAgent serviceAgent;
	private MessagingService messagingService;
	private ChatViewContainer chatViewContainer;

	private App() {
		profileInfo = ProfileInfo.getInstance();
		assert !profileInfo.getUserId().equals("Toto"); // DEBUG
		correspondentServiceLocator = CorrespondentServiceLocator.getInstance();
		RMIController.getInstance();
		correspondentManager = CorrespondentManager.getInstance();
		serviceAgent = ServiceAgent.getInstance();
		messagingService = MessagingService.getInstance();
		MainView.getInstance();
		chatViewContainer = ChatViewContainer.getInstance();
	}

	public static synchronized App getInstance() {
		if (instance == null) {
			instance = new App();
		}
		return instance;
	}

	public void start() {
		if (T)
			System.out.println("profil : " + profileInfo.getUserName());
		correspondentServiceLocator.addObserver(correspondentManager);
		messagingService.open();
		correspondentServiceLocator.open();
		chatViewContainer.start();
		serviceAgent.publishServices(3000);
		if (T)
			System.out.println("application démarrée, en attente...");
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
