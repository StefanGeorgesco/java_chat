package fr.sgo.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JPanel;

import fr.sgo.controller.ActionHandler;
import fr.sgo.entity.Correspondent;

/**
 * Abstract class CorrespondentSummaryView
 * 
 * A panel representing a correspondent with action button
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class CorrespondentSummaryView extends JPanel implements Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2046950170711645517L;
	private Correspondent correspondent;
	private JPanel namePanel;
	private JTextArea nameField;
	private JPanel onlinePanel;
	private JButton actionButton1;

	public CorrespondentSummaryView(Correspondent correspondent, ActionHandler actionHandler) {
		super();
		this.correspondent = correspondent;
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
		this.correspondent.addObserver(this);
	}

	public Correspondent getCorrespondent() {
		return correspondent;
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
		nameField.setText(correspondent.getUserName());
		if (correspondent.isOnline())
			onlinePanel.setBackground(Color.GREEN);
		else
			onlinePanel.setBackground(Color.GRAY);
		repaint();
	}

	public void update(Observable observable, Object arg) {
		if (observable instanceof Correspondent) {
			Correspondent c = (Correspondent) observable;
			if (c.equals(correspondent))
				refresh();
		}
	}
}
