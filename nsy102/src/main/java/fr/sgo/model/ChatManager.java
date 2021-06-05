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
		boolean none = correspondentChats.get(correspondent) == null;
		if (none) {
			CorrespondentChat chat = new CorrespondentChat(correspondent);
			correspondentChats.put(correspondent, chat);
			setChanged();
			notifyObservers(chat);
		}
		if (correspondent.isOnline()) {
			MessagingService.getInstance().setInMessagingHandler(correspondent);
		} else {
			MessagingService.getInstance().unsetInMessagingHandler(correspondent);
		}
	}

	private void removeCorrespondentChat(Correspondent correspondent) {
		MessagingService.getInstance().unsetInMessagingHandler(correspondent);
		CorrespondentChat chat = correspondentChats.remove(correspondent);
		if (chat != null) {
			setChanged();
			notifyObservers(chat);
		}
	}

	public void addGroupChat(GroupChat chat) {
		if (groupChats.add(chat)) {
			setChanged();
			notifyObservers(chat);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		Correspondent correspondent = (Correspondent) arg;
		if (CorrespondentManager.getInstance().existsCorrespondent(correspondent)) {
			if (correspondent.isPaired()) {
				addCorrespondentChatIfNone(correspondent);
				if (App.T)
					System.out.println("chat ajouté pour " + correspondent.getUserName());
			}
		} else {
			removeCorrespondentChat(correspondent);
		}
	}

}
