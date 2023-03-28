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
import javax.swing.JPanel;
import javax.swing.JTextArea;

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
 * @author Stefan Georgesco
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
		JTextArea pairedCorrespondentsPanelTitle = new JTextArea("Mes contacts", 1, 25);
		pairedCorrespondentsPanelTitle.setEditable(false);
		correspondentChatsPanel = new JPanel();
		correspondentChatsPanel.setLayout(new BoxLayout(correspondentChatsPanel, BoxLayout.Y_AXIS));
		correspondentChatsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		JTextArea unpairedCorrespondentsPanelTitle = new JTextArea("Autres correspondants", 1, 25);
		unpairedCorrespondentsPanelTitle.setEditable(false);
		unpairedCorrespondentsPanel = new JPanel();
		unpairedCorrespondentsPanel.setLayout(new BoxLayout(unpairedCorrespondentsPanel, BoxLayout.Y_AXIS));
		unpairedCorrespondentsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		JTextArea groupChatPanelTitle = new JTextArea("Conversations de groupe", 1, 25);
		groupChatPanelTitle.setEditable(false);
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
		final Correspondent corr = correspondent;
		boolean correspondentExists = correspondentManager.existsCorrespondent(corr);
		boolean correspondentIsPaired = corr.isPaired();
		boolean viewContentsChange = false;
		boolean correspondentFound = false;
		for (Component component : unpairedCorrespondentsPanel.getComponents()) {
			CorrespondentSummaryView panel = (CorrespondentSummaryView) component;
			if (panel.getCorrespondent().equals(corr)) {
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
			unpairedCorrespondentsPanel.add(new CorrespondentSummaryView(new ActionHandler("Inviter") {
				@Override
				public void run() {
					CorrespondentController.getInstance().requestPairing(corr);
				}
			}, corr));
			viewContentsChange = true;
		}
		if (viewContentsChange) {
			pack();
			repaint();
		}
	}

	private synchronized void updateView(Chat chat) {
		final Chat ch = chat;
		if (ch instanceof GroupChat) {
			GroupChat groupChat = (GroupChat) ch;
			boolean summaryViewMustAppear = chatManager.existsChat(ch);
			boolean summaryViewFound = false;
			boolean viewContentsChange = false;
			for (Component component : groupChatsPanel.getComponents()) {
				ChatSummaryView panel = (ChatSummaryView) component;
				if (panel.getGroupChat().equals(ch)) {
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
				groupChatsPanel.add(new ChatSummaryView(new ActionHandler("Ouvrir") {
					@Override
					public void run() {
						ChatViewContainer.getInstance().getChatView(ch).update(ch, new Object());
					}
				}, groupChat));
				viewContentsChange = true;
			}
			if (viewContentsChange) {
				pack();
				repaint();
			}
		} else { // CorrespondentChat -> updates correspondentChatsPanel
			Correspondent correspondent = ((CorrespondentChat) ch).getCorrespondent();
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
				correspondentChatsPanel.add(new CorrespondentSummaryView(new ActionHandler("Discuter") {
					@Override
					public void run() {
						ChatViewContainer.getInstance().getChatView(ch).update(ch, new Object());
					}

				}, correspondent));
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
		} else { // from ChatManager
			final Chat chat = (Chat) arg;
			new Thread() {
				@Override
				public void run() {
					updateView(chat);
				}
			}.start();
		}
	}
}
