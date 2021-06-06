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
			setChanged();
			notifyObservers(chat);
		}
		if (correspondent.isOnline()) {
			MessagingService.getInstance().setMessagingHandlers(chat);
		} else {
			MessagingService.getInstance().unsetInMessagingHandlers(chat);
		}
	}

	private void removeCorrespondentChat(Correspondent correspondent) {
		CorrespondentChat chat = correspondentChats.remove(correspondent);
		if (chat != null) {
			if (App.T)
				System.out.println("chat retiré pour " + correspondent.getUserName());
			MessagingService.getInstance().unsetInMessagingHandlers(chat);
			setChanged();
			notifyObservers(chat);
		}
	}

	public void addGroupChat(GroupChat chat) {
		if (groupChats.add(chat)) {
			MessagingService.getInstance().setMessagingHandlers(chat);
			setChanged();
			notifyObservers(chat);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		Correspondent correspondent = (Correspondent) arg;
		if (CorrespondentManager.getInstance().existsCorrespondent(correspondent) && correspondent.isPaired()) {
			addCorrespondentChatIfNone(correspondent);
		} else {
			removeCorrespondentChat(correspondent);
			new Thread() {
				@Override
				public void run() {
					for (GroupChat chat: getGroupChats()) {
						chat.removeCorrespondent(correspondent);
					}
				}
			}.start();
		}
	}

}
