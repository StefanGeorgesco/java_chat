package fr.sgo.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import fr.sgo.controller.DiscussionController;
import fr.sgo.controller.InvitationController;
import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.model.CorrespondentManager;

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
	private App app;
	private JPanel pairedCorrespondentsPanel;
	private JPanel unpairedCorrespondentsPanel;

	private MainView(App app) {
		super("Ma messagerie - " + app.getProfileInfo().getUserName());
		this.app = app;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridLayout(1, 1));
		contentPane.setSize(new Dimension(400, 300));
		pairedCorrespondentsPanel = new JPanel();
		pairedCorrespondentsPanel.setLayout(new GridLayout(0, 1));
		pairedCorrespondentsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		unpairedCorrespondentsPanel = new JPanel();
		unpairedCorrespondentsPanel.setLayout(new GridLayout(0, 1));
		unpairedCorrespondentsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		contentPane.add(pairedCorrespondentsPanel, null);
		contentPane.add(unpairedCorrespondentsPanel, null);
		setVisible(true);
		buildView();
		this.app.getCorrespondentManager().addObserver(this);
		// this.correspondentManager.notifyObservers();
	}

	public static MainView getInstance(App app) {
		if (instance == null)
			instance = new MainView(app);
		return instance;
	}

	private synchronized void buildView() {
		CorrespondentManager correspondentManager = app.getCorrespondentManager();
		for (Component component : pairedCorrespondentsPanel.getComponents()) {
			((CorrespondentPanel) component).getCorrespondent().deleteObserver((CorrespondentPanel) component);
		}
		pairedCorrespondentsPanel.removeAll();
		for (Correspondent correspondent : correspondentManager.getPairedCorrespondents()) {
			pairedCorrespondentsPanel.add(new CorrespondentPanel(app, correspondent, 
					new DiscussionController(app, "Discuter", correspondent)));
		}
		for (Component component : unpairedCorrespondentsPanel.getComponents()) {
			((CorrespondentPanel) component).getCorrespondent().deleteObserver((CorrespondentPanel) component);
		}
		unpairedCorrespondentsPanel.removeAll();
		for (Correspondent correspondent : correspondentManager.getUnpairedCorrespondents()) {
			unpairedCorrespondentsPanel.add(new CorrespondentPanel(app,correspondent, 
					new InvitationController(app, "Inviter", correspondent)));
		}
		pack();
		repaint();
	}

	private synchronized void refreshView(Correspondent correspondent) {
		CorrespondentManager correspondentManager = app.getCorrespondentManager();
		if (correspondentManager.getPairedCorrespondents().contains(correspondent)) { // correspondent is paired
			for (Component component : pairedCorrespondentsPanel.getComponents()) {
				CorrespondentPanel panel = (CorrespondentPanel) component;
				if (panel.getCorrespondent().equals(correspondent)) {
					panel.refresh();
					break;
				}
			}
		} else if (correspondentManager.getUnpairedCorrespondents().contains(correspondent)) { // correspondent is
																								// unpaired
			// and resolved
			unpairedCorrespondentsPanel.add(new CorrespondentPanel(app, correspondent, 
					new InvitationController(app, "Inviter", correspondent)));
			pack();
			repaint();
		} else { // correspondent was unpaired and is removed
			for (Component component : unpairedCorrespondentsPanel.getComponents()) {
				CorrespondentPanel panel = (CorrespondentPanel) component;
				if (panel.getCorrespondent().equals(correspondent)) {
					panel.getCorrespondent().deleteObserver(panel);
					unpairedCorrespondentsPanel.remove(component);
					pack();
					repaint();
					break;
				}
			}
		}
	}

	public void update(Observable observable, Object arg) {
		final Correspondent correspondent = (Correspondent) arg;
		new Thread() {
			@Override
			public void run() {
				refreshView(correspondent);
			}
		}.start();
	}
}
