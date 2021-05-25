package fr.sgo.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.sgo.controller.ChatController;
import fr.sgo.controller.RequestPairingController;
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
		super(app.getProfileInfo().getUserName());
		this.app = app;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		contentPane.setSize(new Dimension(400, 300));
		JLabel pairedCorrespondentsPanelTitle = new JLabel("       Mes contacts       ");
		pairedCorrespondentsPanel = new JPanel();
		pairedCorrespondentsPanel.setLayout(new BoxLayout(pairedCorrespondentsPanel, BoxLayout.Y_AXIS));
		pairedCorrespondentsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		JLabel unpairedCorrespondentsPanelTitle = new JLabel("Autres correspondants     ");
		unpairedCorrespondentsPanel = new JPanel();
		unpairedCorrespondentsPanel.setLayout(new BoxLayout(unpairedCorrespondentsPanel, BoxLayout.Y_AXIS));
		unpairedCorrespondentsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		contentPane.add(pairedCorrespondentsPanelTitle, null);
		contentPane.add(pairedCorrespondentsPanel, null);
		contentPane.add(unpairedCorrespondentsPanelTitle, null);
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
					new ChatController(app, "Discuter", correspondent)));
		}
		for (Component component : unpairedCorrespondentsPanel.getComponents()) {
			((CorrespondentPanel) component).getCorrespondent().deleteObserver((CorrespondentPanel) component);
		}
		unpairedCorrespondentsPanel.removeAll();
		for (Correspondent correspondent : correspondentManager.getUnpairedCorrespondents()) {
			unpairedCorrespondentsPanel.add(new CorrespondentPanel(app, correspondent,
					new RequestPairingController(app, "Inviter", correspondent)));
		}
		pack();
		repaint();
	}

	private synchronized void refreshView(Correspondent correspondent) {
		buildView();
		CorrespondentManager correspondentManager = app.getCorrespondentManager();
		boolean correspondentExists = correspondentManager.getCorrespondents().contains(correspondent);
		boolean correspondentIsPaired = correspondent.isPaired();
		boolean viewContentsChange = false;
		boolean correspondentFound = false;
		for (Component component : pairedCorrespondentsPanel.getComponents()) {
			CorrespondentPanel panel = (CorrespondentPanel) component;
			if (panel.getCorrespondent().equals(correspondent)) {
				if (correspondentFound) {
					pairedCorrespondentsPanel.remove(component);
					viewContentsChange = true;
				} else {
					correspondentFound = true;
					if (correspondentExists && correspondentIsPaired) {
						panel.refresh();
					} else {
						pairedCorrespondentsPanel.remove(component);
						viewContentsChange = true;
					}
				}
			}
		}
		if (!correspondentFound & correspondentExists && correspondentIsPaired) {
			pairedCorrespondentsPanel.add(new CorrespondentPanel(app, correspondent,
					new ChatController(app, "Discuter", correspondent)));
		}
		correspondentFound = false;
		for (Component component : unpairedCorrespondentsPanel.getComponents()) {
			CorrespondentPanel panel = (CorrespondentPanel) component;
			if (panel.getCorrespondent().equals(correspondent)) {
				if (correspondentFound) {
					unpairedCorrespondentsPanel.remove(component);
					viewContentsChange = true;
				} else {
					correspondentFound = true;
					if (correspondentExists && !correspondentIsPaired) {
						panel.refresh();
					} else {
						unpairedCorrespondentsPanel.remove(component);
						viewContentsChange = true;
					}
				}
			}
		}
		if (!correspondentFound & correspondentExists && !correspondentIsPaired) {
			unpairedCorrespondentsPanel.add(new CorrespondentPanel(app, correspondent, 
					new RequestPairingController(app, "Inviter", correspondent)));
		}
		if (viewContentsChange) {
			pack();
			repaint();
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
