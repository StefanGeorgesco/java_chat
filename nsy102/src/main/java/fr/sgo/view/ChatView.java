package fr.sgo.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Date;
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

import fr.sgo.app.App;
import fr.sgo.entity.Chat;
import fr.sgo.entity.CorrespondentChat;
import fr.sgo.entity.GroupChat;
import fr.sgo.entity.InMessage;
import fr.sgo.entity.Message;
import fr.sgo.entity.OutMessage;

@SuppressWarnings("deprecation")
public class ChatView extends JFrame implements ActionListener, Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4272327756479515057L;
	private Chat chat;
	private JTextArea messagesHistory;
	private JTextField messageField;
	private JScrollPane jScrollPane;
	private App app;

	public ChatView(App app, Chat chat) {
		this.app = app;
		this.chat = chat;
		if (chat instanceof GroupChat)
			this.setTitle("Groupe de discussion " + ((GroupChat) chat).getName());
		else
			this.setTitle("Discussion avec " + ((CorrespondentChat) chat).getCorrespondent().getUserName());
		JPanel panelNorth = new JPanel();
		messagesHistory = new JTextArea(15, 40);
		messagesHistory.setEditable(false);
		messagesHistory.setLineWrap(true);
		messagesHistory.setWrapStyleWord(true);
		jScrollPane = new JScrollPane();
		jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane.setViewportView(messagesHistory);
		panelNorth.add(jScrollPane, BorderLayout.CENTER);
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
		refreshMessagesHistoryView();
		Container container = this.getContentPane();
		container.setLayout(new BorderLayout());
		container.add(panelNorth, BorderLayout.NORTH);
		container.add(panelSouth, BorderLayout.SOUTH);
		pack();
		setVisible(false);
		this.chat.addObserver(this);
	}

	public void refreshMessagesHistoryView() {
		DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		String contents = "";
		for (Message m : chat.getMessages()) {
			if (m instanceof InMessage)
				contents += ((InMessage) m).getAuthor().getUserName();
			else
				contents += "moi";
			contents += " (" + shortDateFormat.format(new Date(m.getTimeWritten())) + ") : ";
			contents += m.getContents();
			contents += "\n";
		}
		messagesHistory.setText(contents);
		JScrollBar vertical = jScrollPane.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new Thread(new Runnable() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String text = messageField.getText();
						if (text.length() > 0) {
							chat.sendMessage(new OutMessage(text, app.getProfileInfo().getUserId()));
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
				setVisible(true);
				toFront();
			}
		}.start();

	}

}
