package fr.sgo.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.sgo.controller.ActionHandler;
import fr.sgo.controller.ChatController;
import fr.sgo.controller.CorrespondentController;
import fr.sgo.entity.Chat;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.CorrespondentChat;
import fr.sgo.entity.GroupChat;
import fr.sgo.model.ChatManager;
import fr.sgo.model.CorrespondentManager;
import fr.sgo.service.ProfileInfo;

/**
 * Class MainView
 * 
 * Main application view. Application terminates when closed.
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class MainView extends JFrame implements Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5146613134203799839L;
	private static MainView instance = null;
	private JPanel correspondentChatsPanel;
	private JPanel unpairedCorrespondentsPanel;
	private JPanel groupChatsPanel;
	private CorrespondentManager correspondentManager;
	private ChatManager chatManager;

	private MainView() {
		super(ProfileInfo.getInstance().getUserName());
		correspondentManager = CorrespondentManager.getInstance();
		chatManager = ChatManager.getInstance();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		JLabel pairedCorrespondentsPanelTitle = new JLabel("Mes contacts");
		correspondentChatsPanel = new JPanel();
		correspondentChatsPanel.setLayout(new BoxLayout(correspondentChatsPanel, BoxLayout.Y_AXIS));
		correspondentChatsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		JLabel unpairedCorrespondentsPanelTitle = new JLabel("Autres correspondants");
		unpairedCorrespondentsPanel = new JPanel();
		unpairedCorrespondentsPanel.setLayout(new BoxLayout(unpairedCorrespondentsPanel, BoxLayout.Y_AXIS));
		unpairedCorrespondentsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		JLabel groupChatPanelTitle = new JLabel("Conversations de groupe");
		groupChatsPanel = new JPanel();
		groupChatsPanel.setLayout(new BoxLayout(groupChatsPanel, BoxLayout.Y_AXIS));
		groupChatsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		JButton addGroupChat = new JButton("Nouveau groupe");
		addGroupChat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ChatController.getInstance().createGroupChat();
			}
		});
		contentPane.add(pairedCorrespondentsPanelTitle, null);
		contentPane.add(correspondentChatsPanel, null);
		contentPane.add(unpairedCorrespondentsPanelTitle, null);
		contentPane.add(unpairedCorrespondentsPanel, null);
		contentPane.add(groupChatPanelTitle, null);
		contentPane.add(groupChatsPanel, null);
		contentPane.add(addGroupChat, null);
		pack();
		setVisible(true);
	}

	public static synchronized MainView getInstance() {
		if (instance == null)
			instance = new MainView();
		return instance;
	}

	private synchronized void updateView(Correspondent correspondent) { // updates unpairedCorrespondentsPanel
		boolean correspondentExists = correspondentManager.existsCorrespondent(correspondent);
		boolean correspondentIsPaired = correspondent.isPaired();
		boolean viewContentsChange = false;
		boolean correspondentFound = false;
		for (Component component : unpairedCorrespondentsPanel.getComponents()) {
			CorrespondentSummaryView panel = (CorrespondentSummaryView) component;
			if (panel.getCorrespondent().equals(correspondent)) {
				if (correspondentFound) {
					panel.getCorrespondent().deleteObserver(panel);
					unpairedCorrespondentsPanel.remove(component);
					viewContentsChange = true;
				} else {
					correspondentFound = true;
					if (correspondentExists && !correspondentIsPaired) {
						panel.refresh();
					} else {
						panel.getCorrespondent().deleteObserver(panel);
						unpairedCorrespondentsPanel.remove(component);
						viewContentsChange = true;
					}
				}
			}
		}
		if (!correspondentFound & correspondentExists && !correspondentIsPaired) {
			unpairedCorrespondentsPanel.add(new CorrespondentSummaryView(correspondent, new ActionHandler("Inviter") {
				@Override
				public void run() {
					CorrespondentController.getInstance().requestPairing(correspondent);
				}
			}));
			viewContentsChange = true;
		}
		if (viewContentsChange) {
			pack();
			repaint();
		}
	}

	private synchronized void updateView(Chat chat) {
		if (chat instanceof GroupChat) {
			GroupChat groupChat = (GroupChat) chat;
			boolean summaryViewMustAppear = chatManager.existsChat(chat);
			boolean summaryViewFound = false;
			boolean viewContentsChange = false;
			for (Component component : groupChatsPanel.getComponents()) {
				ChatSummaryView panel = (ChatSummaryView) component;
				if (panel.getGroupChat().equals(chat)) {
					if (summaryViewFound) {
						panel.getGroupChat().deleteObserver(panel);
						groupChatsPanel.remove(component);
						viewContentsChange = true;
					} else {
						summaryViewFound = true;
						if (summaryViewMustAppear) {
							panel.refresh();
						} else {
							panel.getGroupChat().deleteObserver(panel);
							groupChatsPanel.remove(component);
							viewContentsChange = true;
						}
					}
				}
			}
			if (!summaryViewFound && summaryViewMustAppear) {
				groupChatsPanel.add(new ChatSummaryView(groupChat, new ActionHandler("Ouvrir") {
					@Override
					public void run() {
						ChatViewContainer.getInstance().getChatView(chat).update(null, null);
					}
				}));
				viewContentsChange = true;
			}
			if (viewContentsChange) {
				pack();
				repaint();
			}
		} else { // CorrespondentChat -> updates correspondentChatsPanel
			Correspondent correspondent = ((CorrespondentChat) chat).getCorrespondent();
			boolean correspondentExists = correspondentManager.existsCorrespondent(correspondent);
			boolean correspondentIsPaired = correspondent.isPaired();
			boolean viewContentsChange = false;
			boolean correspondentViewFound = false;
			for (Component component : correspondentChatsPanel.getComponents()) {
				CorrespondentSummaryView panel = (CorrespondentSummaryView) component;
				if (panel.getCorrespondent().equals(correspondent)) {
					if (correspondentViewFound) {
						panel.getCorrespondent().deleteObserver(panel);
						correspondentChatsPanel.remove(component);
						viewContentsChange = true;
					} else {
						correspondentViewFound = true;
						if (correspondentExists && correspondentIsPaired) {
							panel.refresh();
						} else {
							panel.getCorrespondent().deleteObserver(panel);
							correspondentChatsPanel.remove(component);
							viewContentsChange = true;
						}
					}
				}
			}
			if (!correspondentViewFound & correspondentExists && correspondentIsPaired) {
				correspondentChatsPanel.add(new CorrespondentSummaryView(correspondent, new ActionHandler("Discuter") {
					@Override
					public void run() {
						ChatViewContainer.getInstance().getChatView(chat).update(null, null);
					}

				}));
				viewContentsChange = true;
			}
			if (viewContentsChange) {
				pack();
				repaint();
			}
		}
	}

	public void update(Observable observable, Object arg) {
		if (observable instanceof CorrespondentManager) { // from CorrespondentManager
			final Correspondent correspondent = (Correspondent) arg;
			new Thread() {
				@Override
				public void run() {
					updateView(correspondent);
				}
			}.start();
		} else if (arg instanceof GroupChat) { // from ChatManager - GroupChat
			final GroupChat chat = (GroupChat) arg;
			new Thread() {
				@Override
				public void run() {
					updateView(chat);
				}
			}.start();
		} else { // from ChatManager - CorrespondentChat
			final CorrespondentChat chat = (CorrespondentChat) arg;
			new Thread() {
				@Override
				public void run() {
					updateView(chat);
				}
			}.start();
		}
	}
}
