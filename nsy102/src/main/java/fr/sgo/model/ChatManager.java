package fr.sgo.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import fr.sgo.entity.Chat;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.CorrespondentChat;

@SuppressWarnings("deprecation")
public class ChatManager extends Observable {
	private static ChatManager instance = null;
	private Set<Chat> chats;

	private ChatManager() {
		this.chats = Collections.synchronizedSet(new HashSet<Chat>());
	}

	public static synchronized ChatManager getInstance() {
		if (instance == null)
			instance = new ChatManager();
		return instance;
	}

	public Collection<Chat> getChats() {
		return chats;
	}

	public CorrespondentChat getCorrespondentChat(Correspondent correspondent) {
		CorrespondentChat correspondentChat = null;
		if (correspondent.isPaired()) {
			for (Chat chat : chats) {
				if (chat instanceof CorrespondentChat
						&& ((CorrespondentChat) chat).getCorrespondent().equals(correspondent)) {
					correspondentChat = (CorrespondentChat) chat;
					break;
				}
			}
			if (correspondentChat == null) {
				correspondentChat = new CorrespondentChat(correspondent);
				addChat(correspondentChat);
			}
		}
		return correspondentChat;
	}

	public void addChat(Chat chat) {
		chats.add(chat);
		setChanged();
		notifyObservers(chat);
	}

}
