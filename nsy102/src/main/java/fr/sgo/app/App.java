package fr.sgo.app;

import java.rmi.RemoteException;

import fr.sgo.controller.CorrespondentController;
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
	private RMIController rmiController;
	private CorrespondentController correspondentController;
	private CorrespondentManager correspondentManager;
	private ServiceAgent serviceAgent;
	private MessagingService messagingService;
	private MainView mainView;
	private ChatViewContainer chatViewContainer;

	private App() {
		profileInfo = ProfileInfo.getInstance();
		assert !profileInfo.getUserId().equals("Toto"); // DEBUG
		rmiController = RMIController.getInstance();
		correspondentController = CorrespondentController.getInstance();
		correspondentServiceLocator = CorrespondentServiceLocator.getInstance();
		correspondentManager = CorrespondentManager.getInstance();
		serviceAgent = ServiceAgent.getInstance();
		messagingService = MessagingService.getInstance();
		mainView = MainView.getInstance();
		chatViewContainer = ChatViewContainer.getInstance();
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
		correspondentServiceLocator.addObserver(correspondentManager);
		messagingService.open();
		chatViewContainer.start();
		correspondentServiceLocator.open();
		serviceAgent.publishServices(2000);
		if (T)
			System.out.println("application démarrée, en attente...");
	}

	public ProfileInfo getProfileInfo() {
		return profileInfo;
	}

	public CorrespondentServiceLocator getCorrespondentServiceLocator() {
		return correspondentServiceLocator;
	}

	public RMIController getRmiController() {
		return rmiController;
	}
	
	public CorrespondentController getCorrespondentController() {
		return correspondentController;
	}

	public CorrespondentManager getCorrespondentManager() {
		return correspondentManager;
	}

	public ServiceAgent getServiceAgent() {
		return serviceAgent;
	}

	public MessagingService getMessagingService() {
		return messagingService;
	}

	public MainView getMainView() {
		return mainView;
	}

	public ChatViewContainer getChatViewContainer() {
		return chatViewContainer;
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
