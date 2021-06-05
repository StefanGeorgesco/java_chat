package fr.sgo.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.sgo.controller.ActionHandler;
import fr.sgo.entity.GroupChat;

/**
 * Abstract class CorrespondentSummaryView
 * 
 * A panel representing a correspondent with action button
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class ChatSummaryView extends JPanel implements Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2566319621500711361L;
	private GroupChat chat;
	private JPanel namePanel;
	private JLabel nameField;
	private JButton actionButton1;

	public ChatSummaryView(GroupChat chat, ActionHandler actionHandler) {
		super();
		this.chat = chat;
		setSize(new Dimension(100, 20));
		namePanel = new JPanel();
		namePanel.setSize(new Dimension(54, 14));
		nameField = new JLabel();
		nameField.setSize(new Dimension(50, 10));
		namePanel.add(nameField);
		actionButton1 = new JButton();
		actionButton1.setSize(new Dimension(30, 10));
		add(namePanel);
		add(actionButton1);
		refresh();
		setActionButton1Controller(actionHandler);
		this.chat.addObserver(this);
	}

	public GroupChat getGroupChat() {
		return chat;
	}

	public void setActionButton1Controller(ActionHandler actionHandler) {
		final ActionHandler handler = actionHandler;
		actionButton1.setText(actionHandler.getActionName());
		actionButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				handler.execute();
			}
		});
	}

	public void refresh() {
		nameField.setText(chat.getName());
		repaint();
	}

	public void update(Observable observable, Object arg) {
		if (observable instanceof GroupChat) {
			GroupChat c = (GroupChat) observable;
			if (c.equals(chat))
				refresh();
		}
	}
}
