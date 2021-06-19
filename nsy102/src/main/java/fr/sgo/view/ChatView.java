package fr.sgo.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import fr.sgo.controller.ChatController;
import fr.sgo.entity.Chat;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.CorrespondentChat;
import fr.sgo.entity.GroupChat;
import fr.sgo.entity.HostedGroupChat;
import fr.sgo.entity.InMessage;
import fr.sgo.entity.Message;
import fr.sgo.entity.RemoteGroupChat;
import fr.sgo.service.ProfileInfo;

/**
 * Class ChatView
 * 
 * Detailed view of a chat
 *
 * @author Stefan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class ChatView extends JFrame implements ActionListener, Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4272327756479515057L;
	private Chat chat;
	private JTextArea messagesHistory;
	private JScrollPane messagesHistoryScrollPane;
	private JTextArea correspondentsList;
	private JScrollPane correspondentsListScrollPane;
	private JTextField messageField;
	private JButton sendButton;
	private JPanel onlinePanel;

	public ChatView(Chat chat) {
		this.chat = chat;
		if (this.chat instanceof GroupChat)
			this.setTitle(ProfileInfo.getInstance().getUserName() + " - Groupe de discussion "
					+ ((GroupChat) this.chat).getName());
		else
			this.setTitle(ProfileInfo.getInstance().getUserName() + " - Discussion avec "
					+ ((CorrespondentChat) this.chat).getCorrespondent().getUserName());
		JPanel panelNorth = new JPanel();
		messagesHistory = new JTextArea(15, 40);
		messagesHistory.setEditable(false);
		messagesHistory.setLineWrap(true);
		messagesHistory.setWrapStyleWord(true);
		messagesHistoryScrollPane = new JScrollPane();
		messagesHistoryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		messagesHistoryScrollPane.setViewportView(messagesHistory);
		panelNorth.add(messagesHistoryScrollPane, BorderLayout.CENTER);
		refreshMessagesHistoryView();

		JPanel panelCenter = new JPanel();
		panelCenter.setLayout(new GridLayout(2, 1));
		correspondentsList = new JTextArea(1, 40);
		correspondentsList.setEditable(false);
		correspondentsList.setLineWrap(true);
		correspondentsList.setWrapStyleWord(true);
		correspondentsListScrollPane = new JScrollPane();
		correspondentsListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		correspondentsListScrollPane.setViewportView(correspondentsList);
		panelCenter.add(correspondentsListScrollPane, BorderLayout.CENTER);
		refreshcorrespondentsListView();

		JPanel panelSouth = new JPanel(new FlowLayout());
		messageField = new JTextField(30);
		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				messageField.requestFocus();
			}
		});
		sendButton = new JButton("Envoyer");
		sendButton.addActionListener(this);
		getRootPane().setDefaultButton(sendButton);
		onlinePanel = new JPanel();
		onlinePanel.setSize(new Dimension(10, 10));
		panelSouth.add(messageField);
		panelSouth.add(sendButton);
		panelSouth.add(onlinePanel);
		refreshConnectionStatusView();

		Container container = this.getContentPane();
		container.setLayout(new BorderLayout());
		container.add(panelNorth, BorderLayout.NORTH);
		container.add(panelSouth, BorderLayout.SOUTH);
		if (this.chat instanceof GroupChat) {
			if (this.chat instanceof HostedGroupChat) {
				JButton addCorrespondent = new JButton("Ajouter des participants");
				addCorrespondent.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ChatController.getInstance().showAddCorrespondentsView(ChatView.this,
								(HostedGroupChat) ChatView.this.chat);
					}
				});
				panelCenter.add(addCorrespondent);
			}
			container.add(panelCenter, BorderLayout.CENTER);
		}
		pack();
		setVisible(false);
		this.chat.addObserver(this);
	}

	public Chat getChat() {
		return chat;
	}

	private void refreshMessagesHistoryView() {
		DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		String contents = "";
		for (Message message : chat.getMessages()) {
			if (message instanceof InMessage)
				contents += ((InMessage) message).getAuthor().getUserName();
			else
				contents += "moi";
			contents += " (" + shortDateFormat.format(new Date(message.getTimeWritten())) + ") : ";
			contents += message.getContents();
			contents += "\n";
		}
		messagesHistory.setText(contents);
		JScrollBar vertical = messagesHistoryScrollPane.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}

	private void refreshcorrespondentsListView() {
		if (chat instanceof GroupChat) {
			String contents = "Patricipants : ";
			Iterator<Correspondent> it = ((GroupChat) chat).getCorrespondents().iterator();
			while (it.hasNext()) {
				contents += it.next().getUserName();
				if (it.hasNext())
					contents += ", ";
			}
			correspondentsList.setText(contents);
			JScrollBar vertical = correspondentsListScrollPane.getVerticalScrollBar();
			vertical.setValue(vertical.getMaximum());
		}
	}

	private void refreshConnectionStatusView() {
		if (chat instanceof HostedGroupChat
				|| chat instanceof RemoteGroupChat && ((RemoteGroupChat) chat).getCorrespondent().isOnline()
				|| chat instanceof CorrespondentChat && ((CorrespondentChat) chat).getCorrespondent().isOnline())
			onlinePanel.setBackground(Color.GREEN);
		else
			onlinePanel.setBackground(Color.GRAY);
		repaint();
		if (chat instanceof RemoteGroupChat) {
			messageField.setEnabled(((RemoteGroupChat) chat).getCorrespondent().isOnline());
			sendButton.setEnabled(((RemoteGroupChat) chat).getCorrespondent().isOnline());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new Thread(new Runnable() {
			public void run() {
				String text = messageField.getText();
				if (text.length() > 0) {
					ChatController.getInstance().sendMessage(chat, text);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							messageField.setText("");
						}
					});
				}
			}
		}).start();
	}

	@Override
	public void update(Observable o, Object arg) {
		final Object obj = arg;
		new Thread() {
			@Override
			public void run() {
				refreshMessagesHistoryView();
				refreshcorrespondentsListView();
				refreshConnectionStatusView();
				if (obj != null) {
					setVisible(true);
					toFront();
				}
			}
		}.start();

	}

}
