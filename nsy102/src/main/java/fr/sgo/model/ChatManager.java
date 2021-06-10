package fr.sgo.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import fr.sgo.app.App;
import fr.sgo.entity.Chat;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.CorrespondentChat;
import fr.sgo.entity.GroupChat;
import fr.sgo.entity.HostedGroupChat;
import fr.sgo.entity.RemoteGroupChat;
import fr.sgo.service.MessagingService;

@SuppressWarnings("deprecation")
public class ChatManager extends Observable implements Observer {
	private static ChatManager instance = null;
	private Map<Correspondent, CorrespondentChat> correspondentChats;
	private Set<GroupChat> groupChats;

	private ChatManager() {
		this.correspondentChats = Collections.synchronizedMap(new HashMap<Correspondent, CorrespondentChat>());
		this.groupChats = Collections.synchronizedSet(new HashSet<GroupChat>());
	}

	public static synchronized ChatManager getInstance() {
		if (instance == null)
			instance = new ChatManager();
		return instance;
	}

	public Collection<CorrespondentChat> getCorrespondentChats() {
		return correspondentChats.values();
	}

	public Collection<GroupChat> getGroupChats() {
		return groupChats;
	}
	
	public Collection<GroupChat> getGroupChats(Correspondent correspondent) {
		Set<GroupChat> set = new HashSet<GroupChat>();
		for (GroupChat chat: getGroupChats()) {
			if (chat instanceof RemoteGroupChat && ((RemoteGroupChat) chat).getCorrespondent().equals(correspondent)) {
				set.add(chat);
			}
		}
		return set;
		
	}

	public Collection<Chat> getChats() {
		Set<Chat> set = new HashSet<Chat>(groupChats);
		set.addAll(correspondentChats.values());
		return set;
	}

	public CorrespondentChat getCorrespondentChat(Correspondent correspondent) {
		return correspondentChats.get(correspondent);
	}

	public boolean existsChat(Chat chat) {
		return correspondentChats.values().contains(chat) || groupChats.contains(chat);
	}

	private void addCorrespondentChatIfNone(Correspondent correspondent) {
		CorrespondentChat chat = correspondentChats.get(correspondent);
		if (chat == null) {
			chat = new CorrespondentChat(correspondent);
			correspondentChats.put(correspondent, chat);
			if (App.T)
				System.out.println("chat ajouté pour " + correspondent.getUserName());
			MessagingService.getInstance().setOutMessagingHandler(chat);
			if (App.T)
				System.out.println("messagerie installée en sortie pour " + correspondent.getUserName());
			setChanged();
			notifyObservers(chat);
		}
		if (correspondent.isOnline()) {
			MessagingService.getInstance().setInMessagingHandler(chat);
			if (App.T)
				System.out.println("messagerie installée en entrée pour "
						+ chat.getCorrespondent().getUserName());
		} else {
			MessagingService.getInstance().unsetInMessagingHandler(chat);
			if (App.T)
				System.out.println("messagerie retirée en entrée pour "
						+ chat.getCorrespondent().getUserName());
		}
	}

	private void removeCorrespondentChat(Correspondent correspondent) {
		CorrespondentChat chat = correspondentChats.remove(correspondent);
		if (chat != null) {
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
		if (groupChats.add(chat)) {
			if (chat instanceof HostedGroupChat || ((RemoteGroupChat) chat).getCorrespondent().isOnline()) {
				setMessagingHandlers(chat);
			}
		}
	}
	
	@Override
	public void update(Observable o, Object arg) {
		Correspondent correspondent = (Correspondent) arg;
		if (CorrespondentManager.getInstance().existsCorrespondent(correspondent) && correspondent.isPaired()) {
			addCorrespondentChatIfNone(correspondent);
			if (correspondent.isOnline())
				for (GroupChat chat: getGroupChats(correspondent)) {
					setMessagingHandlers(chat);
				}
			else
				for (GroupChat chat: getGroupChats(correspondent)) {
					unsetMessagingHandlers(chat);
				}
		} else {
			removeCorrespondentChat(correspondent);
			for (GroupChat chat: getGroupChats(correspondent)) {
				unsetMessagingHandlers(chat);
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
