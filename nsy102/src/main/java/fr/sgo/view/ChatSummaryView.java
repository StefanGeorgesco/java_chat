package fr.sgo.view;

import java.util.Observable;

import fr.sgo.controller.ActionHandler;
import fr.sgo.entity.GroupChat;
import fr.sgo.entity.HostedGroupChat;
import fr.sgo.entity.RemoteGroupChat;

/**
 * Class ChatSummaryView
 * 
 * A panel representing a chat with action button
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class ChatSummaryView extends SummaryView {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2566319621500711361L;
	private GroupChat chat;

	public ChatSummaryView(ActionHandler actionHandler, GroupChat chat) {
		super(actionHandler);
		this.chat = chat;
		this.chat.addObserver(this);
		refresh();
	}

	public GroupChat getGroupChat() {
		return chat;
	}

	@Override
	public void refresh() {
		boolean online = chat instanceof HostedGroupChat
				|| chat instanceof RemoteGroupChat && ((RemoteGroupChat) chat).getCorrespondent().isOnline();
		refresh(chat.getName(), online);
	}

	@Override
	public void update(Observable observable, Object arg) {
		if (observable instanceof GroupChat) {
			GroupChat c = (GroupChat) observable;
			if (c.equals(chat))
				refresh();
		}
	}
}
