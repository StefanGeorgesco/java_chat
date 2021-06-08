package fr.sgo.view;

import java.awt.BorderLayout;
import java.awt.Container;
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
//import javax.swing.SwingUtilities;
import javax.swing.SwingUtilities;

import fr.sgo.controller.ChatController;
import fr.sgo.entity.Chat;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.CorrespondentChat;
import fr.sgo.entity.GroupChat;
import fr.sgo.entity.HostedGroupChat;
import fr.sgo.entity.InMessage;
import fr.sgo.entity.Message;
import fr.sgo.entity.OutMessage;
import fr.sgo.service.ProfileInfo;

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
		JButton sendButton = new JButton("Envoyer");
		sendButton.addActionListener(this);
		getRootPane().setDefaultButton(sendButton);
		panelSouth.add(messageField);
		panelSouth.add(sendButton);

		Container container = this.getContentPane();
		container.setLayout(new BorderLayout());
		container.add(panelNorth, BorderLayout.NORTH);
		container.add(panelSouth, BorderLayout.SOUTH);
		if (this.chat instanceof GroupChat) {
			if (this.chat instanceof HostedGroupChat) {
				JButton addCorrespondent = new JButton("Ajouter un participant");
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

	@Override
	public void actionPerformed(ActionEvent e) {
		new Thread(new Runnable() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String text = messageField.getText();
						if (text.length() > 0) {
							chat.sendMessage(new OutMessage(text, ProfileInfo.getInstance().getUserId()));
							messageField.setText("");
						}
					}
				});
			}
		}).start();
	}

	@Override
	public void update(Observable o, Object arg) {
		new Thread() {
			@Override
			public void run() {
				refreshMessagesHistoryView();
				refreshcorrespondentsListView();
				setVisible(true);
				toFront();
			}
		}.start();

	}

}
