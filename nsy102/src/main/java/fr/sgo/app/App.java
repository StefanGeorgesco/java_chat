package fr.sgo.app;

import java.rmi.RemoteException;
import fr.sgo.controller.MainController;
import fr.sgo.model.CorrespondentManager;
import fr.sgo.model.MessageManager;
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
	public final boolean T = true; //
	private static App instance = null;
	private ProfileInfo profileInfo;
	private CorrespondentServiceLocator correspondentServiceLocator;
	private MainController mainController;
	private CorrespondentManager correspondentManager;
	private ServiceAgent serviceAgent;
	private MessageManager messageManager;
	private MessagingService messagingService;
	private MainView mainView;
	private ChatViewContainer chatViewContainer;

	private App() {
		profileInfo = ProfileInfo.getInstance();
		correspondentServiceLocator = CorrespondentServiceLocator.getInstance();
		try {
			mainController = MainController.getInstance(this);
		} catch (RemoteException e) {
			e.printStackTrace();
			System.exit(1);
		}
		correspondentManager = CorrespondentManager.getInstance(this);
		serviceAgent = ServiceAgent.getInstance(this);
		messageManager = MessageManager.getInstance(this);
		messagingService = MessagingService.getInstance(this);
		mainView = MainView.getInstance(this);
		chatViewContainer = ChatViewContainer.getInstance(this);
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
		correspondentServiceLocator.open();
		chatViewContainer.open();
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

	public MessageManager getMessageManager() {
		return messageManager;
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
