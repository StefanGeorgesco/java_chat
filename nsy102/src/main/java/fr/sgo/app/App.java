package fr.sgo.app;

import java.rmi.RemoteException;
import java.util.Observable;
import java.util.Observer;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import fr.sgo.controller.ChatController;
import fr.sgo.controller.CorrespondentController;
import fr.sgo.controller.RMIController;
import fr.sgo.entity.Chat;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.CorrespondentChat;
import fr.sgo.entity.HostedGroupChat;
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
 * Main class. Initializes the application and supports MBean.
 *
 * @author Stefan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class App extends NotificationBroadcasterSupport implements AppMBean, Observer {
	public static final boolean T = false; //
	public static final boolean JMS_MESSAGE_PERSISTENCE = false; //
	private static App instance = null;
	private ProfileInfo profileInfo;
	private CorrespondentServiceLocator correspondentServiceLocator;
	private CorrespondentManager correspondentManager;
	private ChatManager chatManager;
	private ServiceAgent serviceAgent;
	private MainView mainView;
	private ChatViewContainer chatViewContainer;
	private long sequenceNumber = 0;

	private App() {
		profileInfo = ProfileInfo.getInstance();
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
		correspondentManager.addObserver(chatManager);
		correspondentManager.addObserver(mainView);
		correspondentManager.addObserver(this);
		chatManager.addObserver(chatViewContainer);
		chatManager.addObserver(mainView);
		chatManager.addObserver(this);
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

	@Override
	public String userName() {
		return profileInfo.getUserName();
	}

	@Override
	public int numberOfCorrespondents() {
		return correspondentManager.getCorrespondents().size();
	}

	@Override
	public int numberOfPairedCorrespondents() {
		return correspondentManager.getPairedCorrespondents().size();
	}

	@Override
	public int numberOfUnpairedCorrespondents() {
		return correspondentManager.getUnpairedCorrespondents().size();
	}

	@Override
	public int numberOfOnlineCorrespondents() {
		int number = 0;
		for (Correspondent c : correspondentManager.getCorrespondents())
			if (c.isOnline())
				number++;
		return number;
	}

	@Override
	public int numberOfChats() {
		return chatManager.getChats().size();
	}

	@Override
	public int numberOfCorrespondentChats() {
		int number = 0;
		for (Chat c : chatManager.getChats())
			if (c instanceof CorrespondentChat)
				number++;
		return number;
	}

	@Override
	public int numberOfHostedGroupChats() {
		int number = 0;
		for (Chat c : chatManager.getChats())
			if (c instanceof HostedGroupChat)
				number++;
		return number;
	}

	@Override
	public int numberOfRemoteGroupChats() {
		int number = 0;
		for (Chat c : chatManager.getChats())
			if (c instanceof HostedGroupChat)
				number++;
		return number;
	}

	@Override
	public String correspondents() {
		return correspondentManager.getCorrespondents().toString();
	}

	@Override
	public String chats() {
		return chatManager.getChats().toString();
	}

	@Override
	public String messages(String chatId) {
		String messages = "";
		for (Chat c : chatManager.getChats())
			if (c.getId().equals(chatId)) {
				messages = c.getMessages().toString();
				break;
			}
		return messages;
	}

	@Override
	public void update(Observable o, Object arg) {
		String type = null;
		String message = null;
		if (arg instanceof Correspondent) {
			type = "Correspondent";
			message = ((Correspondent) arg).toString();
		} else {
			type = "Chat";
			message = ((Chat) arg).toString();
		}
		sequenceNumber++;
		sendNotification(new Notification(type, this, sequenceNumber,
				System.currentTimeMillis(), message));
	}

}
