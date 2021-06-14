package fr.sgo.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import fr.sgo.app.App;
import fr.sgo.entity.Chat;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.CorrespondentChat;
import fr.sgo.entity.GroupChat;
import fr.sgo.entity.HostedGroupChat;
import fr.sgo.entity.OutMessage;
import fr.sgo.entity.RemoteGroupChat;
import fr.sgo.service.MessagingService;
import fr.sgo.service.ProfileInfo;
import fr.sgo.service.Storage;

@SuppressWarnings("deprecation")
public class ChatManager extends Observable implements Observer {
	private static ChatManager instance = null;
	private Set<Chat> chats;
	private static String objectName;

	@SuppressWarnings("unchecked")
	private ChatManager() {
		objectName = "chat_" + ProfileInfo.getInstance().getUserId();
		Set<Chat> restoredChats = (Set<Chat>) Storage.restore(objectName);
		if (restoredChats == null)
			this.chats = Collections.synchronizedSet(new HashSet<Chat>());
		else
			this.chats = Collections.synchronizedSet(restoredChats);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Storage.save(ChatManager.this.chats, objectName);
			}
		});
	}

	public static synchronized ChatManager getInstance() {
		if (instance == null)
			instance = new ChatManager();
		return instance;
	}

	public void start() {
		for (Chat chat : getChats()) {
			if (chat instanceof GroupChat)
				if (chat instanceof HostedGroupChat || ((RemoteGroupChat) chat).getCorrespondent().isOnline())
					setMessagingHandlers(chat);
			setChanged();
			notifyObservers(chat);
		}
	}

	public Collection<Chat> getChats() {
		return chats;
	}

	private Collection<CorrespondentChat> getCorrespondentChats() {
		Set<CorrespondentChat> chats = new HashSet<CorrespondentChat>();
		for (Chat chat : getChats()) {
			if (chat instanceof CorrespondentChat)
				chats.add((CorrespondentChat) chat);
		}
		return chats;
	}

	private Collection<GroupChat> getGroupChats() {
		Set<GroupChat> chats = new HashSet<GroupChat>();
		for (Chat chat : getChats()) {
			if (chat instanceof GroupChat)
				chats.add((GroupChat) chat);
		}
		return chats;
	}

	private Collection<GroupChat> getGroupChats(Correspondent correspondent) {
		Set<GroupChat> set = new HashSet<GroupChat>();
		for (GroupChat chat : getGroupChats()) {
			if (chat instanceof RemoteGroupChat && ((RemoteGroupChat) chat).getCorrespondent().equals(correspondent)) {
				set.add(chat);
			}
		}
		return set;

	}

	private CorrespondentChat getCorrespondentChat(Correspondent correspondent) {
		CorrespondentChat chat = null;
		for (CorrespondentChat c : getCorrespondentChats()) {
			if (c.getCorrespondent().equals(correspondent)) {
				chat = c;
				break;
			}
		}
		return chat;
	}

	public boolean existsChat(Chat chat) {
		return chats.contains(chat);
	}

	private void setCorrespondentChat(Correspondent correspondent) {
		CorrespondentChat chat = getCorrespondentChat(correspondent);
		if (chat == null) {
			chat = new CorrespondentChat(correspondent);
			chats.add(chat);
			if (App.T)
				System.out.println("chat ajouté pour " + correspondent.getUserName());
		}
		MessagingService.getInstance().setOutMessagingHandler(chat);
		if (App.T)
			System.out.println("messagerie installée en sortie pour " + correspondent.getUserName());
		setChanged();
		notifyObservers(chat);
		if (correspondent.isOnline()) {
			MessagingService.getInstance().setInMessagingHandler(chat);
			if (App.T)
				System.out.println("messagerie installée en entrée pour " + chat.getCorrespondent().getUserName());
		} else {
			MessagingService.getInstance().unsetInMessagingHandler(chat);
			if (App.T)
				System.out.println("messagerie retirée en entrée pour " + chat.getCorrespondent().getUserName());
		}
	}

	private void removeCorrespondentChat(Correspondent correspondent) {
		CorrespondentChat chat = getCorrespondentChat(correspondent);
		if (chat != null && chats.remove(chat)) {
			if (App.T)
				System.out.println("chat retiré pour " + correspondent.getUserName());
			MessagingService.getInstance().unsetOutMessagingHandler(chat);
			MessagingService.getInstance().unsetInMessagingHandler(chat);
			if (App.T)
				System.out.println("messagerie retirée pour " + correspondent.getUserName());
			setChanged();
			notifyObservers(chat);
		}
	}

	private void setMessagingHandlers(Chat chat) {
		MessagingService.getInstance().setOutMessagingHandler(chat);
		MessagingService.getInstance().setInMessagingHandler(chat);
		setChanged();
		notifyObservers(chat);
	}

	private void unsetMessagingHandlers(Chat chat) {
		MessagingService.getInstance().unsetOutMessagingHandler(chat);
		MessagingService.getInstance().unsetInMessagingHandler(chat);
		setChanged();
		notifyObservers(chat);
	}

	public void addGroupChat(GroupChat chat) {
		if (chats.add(chat)) {
			if (chat instanceof HostedGroupChat || ((RemoteGroupChat) chat).getCorrespondent().isOnline()) {
				setMessagingHandlers(chat);
			}
		}
	}

	public void acceptRemoteGroupChat(GroupChat chat, Correspondent correspondent) {
		CorrespondentManager correspondentManager = CorrespondentManager.getInstance();
		RemoteGroupChat newChat = new RemoteGroupChat(chat.getName(), chat.getId(), correspondent);
		for (Correspondent remoteCorrespondent : chat.getCorrespondents()) {
			Correspondent localCorrespondent = correspondentManager.getCorrespondent(remoteCorrespondent.getUserId());
			if (localCorrespondent != null)
				newChat.addCorrespondent(localCorrespondent);
			else
				newChat.addCorrespondent(
						new Correspondent(remoteCorrespondent.getUserId(), remoteCorrespondent.getUserName()));
		}
		addGroupChat(newChat);
	}
	
	public void sendMessage(Chat chat, String text) {
		chat.sendMessage(new OutMessage(text, ProfileInfo.getInstance().getUserId()));
	}

	@Override
	public void update(Observable o, Object arg) {
		Correspondent correspondent = (Correspondent) arg;
		if (CorrespondentManager.getInstance().existsCorrespondent(correspondent) && correspondent.isPaired()) {
			setCorrespondentChat(correspondent);
			if (correspondent.isOnline())
				for (GroupChat chat : getGroupChats(correspondent)) {
					setMessagingHandlers(chat);
					chat.reportChange();
				}
			else
				for (GroupChat chat : getGroupChats(correspondent)) {
					unsetMessagingHandlers(chat);
					chat.reportChange();
				}
		} else {
			removeCorrespondentChat(correspondent);
			for (GroupChat chat : getGroupChats(correspondent)) {
				unsetMessagingHandlers(chat);
				chat.reportChange();
			}
			new Thread() {
				@Override
				public void run() {
					for (GroupChat chat : getGroupChats()) {
						chat.removeCorrespondent(correspondent);
					}
				}
			}.start();
		}
	}

}
