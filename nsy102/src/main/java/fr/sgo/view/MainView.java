package fr.sgo.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.sgo.controller.ChatController;
import fr.sgo.controller.ActionHandler;
import fr.sgo.controller.CorrespondentController;
import fr.sgo.entity.Chat;
import fr.sgo.entity.Correspondent;
import fr.sgo.model.ChatManager;
import fr.sgo.model.CorrespondentManager;
import fr.sgo.service.ProfileInfo;

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
	private JPanel pairedCorrespondentsPanel;
	private JPanel unpairedCorrespondentsPanel;
	private CorrespondentManager correspondentManager;
	private ChatManager chatManager;

	private MainView() {
		super(ProfileInfo.getInstance().getUserName());
		correspondentManager = CorrespondentManager.getInstance();
		chatManager = ChatManager.getInstance();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
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
		correspondentManager.addObserver(this);
	}

	public static synchronized MainView getInstance() {
		if (instance == null)
			instance = new MainView();
		return instance;
	}

	private synchronized void buildView() {
		for (Component component : pairedCorrespondentsPanel.getComponents()) {
			((CorrespondentView) component).getCorrespondent().deleteObserver((CorrespondentView) component);
		}
		pairedCorrespondentsPanel.removeAll();
		for (Correspondent correspondent : correspondentManager.getPairedCorrespondents()) {
			final Chat chat = chatManager.getCorrespondentChat(correspondent);
			pairedCorrespondentsPanel.add(new CorrespondentView(correspondent,
					new ActionHandler("Discuter") {
						@Override
						public void run() {
							ChatController.getInstance().showView(chat);
						}

					}));
		}
		for (Component component : unpairedCorrespondentsPanel.getComponents()) {
			((CorrespondentView) component).getCorrespondent().deleteObserver((CorrespondentView) component);
		}
		unpairedCorrespondentsPanel.removeAll();
		for (Correspondent correspondent : correspondentManager.getUnpairedCorrespondents()) {
			unpairedCorrespondentsPanel.add(new CorrespondentView(correspondent, new ActionHandler("Inviter") {
				@Override
				public void run() {
					CorrespondentController.getInstance().requestPairing(correspondent);
				}
			}));
		}
		pack();
		repaint();
	}

	private synchronized void refreshView(Correspondent correspondent) {
		buildView();
		boolean correspondentExists = correspondentManager.getCorrespondents().contains(correspondent);
		boolean correspondentIsPaired = correspondent.isPaired();
		boolean viewContentsChange = false;
		boolean correspondentFound = false;
		for (Component component : pairedCorrespondentsPanel.getComponents()) {
			CorrespondentView panel = (CorrespondentView) component;
			if (panel.getCorrespondent().equals(correspondent)) {
				if (correspondentFound) {
					panel.getCorrespondent().deleteObserver(panel);
					pairedCorrespondentsPanel.remove(component);
					viewContentsChange = true;
				} else {
					correspondentFound = true;
					if (correspondentExists && correspondentIsPaired) {
						panel.refresh();
					} else {
						panel.getCorrespondent().deleteObserver(panel);
						pairedCorrespondentsPanel.remove(component);
						viewContentsChange = true;
					}
				}
			}
		}
		if (!correspondentFound & correspondentExists && correspondentIsPaired) {
			final Chat chat = chatManager.getCorrespondentChat(correspondent);
			pairedCorrespondentsPanel.add(new CorrespondentView(correspondent, new ActionHandler("Discuter") {
				@Override
				public void run() {
					ChatController.getInstance().showView(chat);
				}

			}));
		}
		correspondentFound = false;
		for (Component component : unpairedCorrespondentsPanel.getComponents()) {
			CorrespondentView panel = (CorrespondentView) component;
			if (panel.getCorrespondent().equals(correspondent)) {
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
			unpairedCorrespondentsPanel.add(new CorrespondentView(correspondent,
					new ActionHandler("Inviter") {
						@Override
						public void run() {
							CorrespondentController.getInstance().requestPairing(correspondent);
						}
					}));
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
