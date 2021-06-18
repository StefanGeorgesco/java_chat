package fr.sgo.app;

import java.rmi.RemoteException;

import fr.sgo.controller.ChatController;
import fr.sgo.controller.CorrespondentController;
import fr.sgo.controller.RMIController;
import fr.sgo.model.ChatManager;
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
 * Initializes the application
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class App {
	public static final boolean T = true; //
	public static final boolean JMS_MESSAGE_PERSISTENCE = false; //
	private static App instance = null;
	private ProfileInfo profileInfo;
	private CorrespondentServiceLocator correspondentServiceLocator;
	private CorrespondentManager correspondentManager;
	private ChatManager chatManager;
	private ServiceAgent serviceAgent;
	private MainView mainView;
	private ChatViewContainer chatViewContainer;

	private App() {
		profileInfo = ProfileInfo.getInstance();
		assert profileInfo.getUserId() != null; // DEBUG
		RMIController.getInstance();
		CorrespondentController.getInstance();
		ChatController.getInstance();
		correspondentServiceLocator = CorrespondentServiceLocator.getInstance();
		correspondentManager = CorrespondentManager.getInstance();
		chatManager = ChatManager.getInstance();
		serviceAgent = ServiceAgent.getInstance();
		MessagingService.getInstance();
		mainView = MainView.getInstance();
		chatViewContainer = ChatViewContainer.getInstance();
	}

	private static App getInstance() {
		if (instance == null) {
			instance = new App();
		}
		return instance;
	}

	public void start() {
		if (T)
			System.out.println("profil : " + profileInfo.getUserName());
		correspondentServiceLocator.addObserver(correspondentManager);
		correspondentManager.addObserver(chatManager);
		correspondentManager.addObserver(mainView);
		chatManager.addObserver(chatViewContainer);
		chatManager.addObserver(mainView);
		correspondentManager.start();
		chatManager.start();
		correspondentServiceLocator.open();
		serviceAgent.publishServices(2000);
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
