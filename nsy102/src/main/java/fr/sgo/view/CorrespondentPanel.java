package fr.sgo.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.sgo.controller.Controller;
import fr.sgo.entity.Correspondent;

/**
 * Abstract class CorrespondentPanel
 * 
 * A panel representing a correspondent with action button
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class CorrespondentPanel extends JPanel implements Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2046950170711645517L;
	private Correspondent correspondent;
	private JPanel namePanel;
	private JLabel nameField;
	private JPanel onlinePanel;
	private JButton actionButton1;

	public CorrespondentPanel(Correspondent correspondent, Controller controller) {
		super();
		this.correspondent = correspondent;
		setSize(new Dimension(100, 20));
		namePanel = new JPanel();
		namePanel.setSize(new Dimension(54, 14));
		nameField = new JLabel();
		nameField.setSize(new Dimension(50, 10));
		namePanel.add(nameField);
		onlinePanel = new JPanel();
		onlinePanel.setSize(new Dimension(10, 10));
		actionButton1 = new JButton();
		actionButton1.setSize(new Dimension(30, 10));
		add(namePanel);
		add(onlinePanel);
		add(actionButton1);
		refresh();
		setActionButton1Controller(controller);
		this.correspondent.addObserver(this);
	}

	public Correspondent getCorrespondent() {
		return correspondent;
	}

	public void setActionButton1Controller(Controller controller) {
		final Controller ctrl = controller;
		actionButton1.setText(controller.getActionName());
		actionButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ctrl.execute();
			}
		});
	}

	public void refresh() {
		nameField.setText(correspondent.getUserName());
		if (correspondent.isOnline())
			onlinePanel.setBackground(Color.GREEN);
		else
			onlinePanel.setBackground(Color.GRAY);
		try {
			repaint();
		} catch (Exception e) {
		}
	}

	public void update(Observable observable, Object args) {
		Correspondent c = (Correspondent) observable;
		if (c.equals(correspondent))
			refresh();
	}
}
