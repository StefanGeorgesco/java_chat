package fr.sgo.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JPanel;

import fr.sgo.controller.ActionHandler;

/**
 * Abstract class SummaryView
 * 
 * A panel representing a correspondent or a chat with action button
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public abstract class SummaryView extends JPanel implements Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4564807286808944179L;
	protected JPanel namePanel;
	protected JTextArea nameField;
	protected JPanel onlinePanel;
	protected JButton actionButton1;

	public SummaryView(ActionHandler actionHandler) {
		super();
		setSize(new Dimension(100, 20));
		namePanel = new JPanel();
		namePanel.setSize(new Dimension(54, 14));
		nameField = new JTextArea(1, 15);
		nameField.setEditable(false);
		namePanel.add(nameField);
		onlinePanel = new JPanel();
		onlinePanel.setSize(new Dimension(10, 10));
		actionButton1 = new JButton();
		actionButton1.setSize(new Dimension(30, 10));
		add(namePanel);
		add(actionButton1);
		add(onlinePanel);
		refresh();
		setActionButton1Controller(actionHandler);
	}

	protected void setActionButton1Controller(ActionHandler actionHandler) {
		final ActionHandler handler = actionHandler;
		actionButton1.setText(actionHandler.getActionName());
		actionButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				handler.execute();
			}
		});
	}
	
	protected void refresh(String name, boolean online) {
		nameField.setText(name);
		if (online)
			onlinePanel.setBackground(Color.GREEN);
		else
			onlinePanel.setBackground(Color.GRAY);
		repaint();
	}

	public abstract void refresh();

}
