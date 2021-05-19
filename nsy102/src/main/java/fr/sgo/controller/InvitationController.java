package fr.sgo.controller;

import javax.swing.JOptionPane;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.view.MainView;

/**
 * Class InvitationController
 * 
 * Manages an invitation to a correspondent.
 *
 * @author Stéfan Gerogesco
 * @version 1.0
 */
public class InvitationController extends Controller {
	private final static boolean T = true;
	private Correspondent correspondent;

	public InvitationController(App app, String actionName, MainView mainView, Correspondent correspondent) {
		super(app, actionName, mainView);
		this.correspondent = correspondent;
	}

	public void run() {
		new Thread() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(mainView,
						"Invitation lancée pour " + correspondent.getUserName() + "...");
			}
		}.start();
		if (T)
			System.out.println("Invitation lancée pour " + correspondent.getUserName() + "...");
		InvitationController.this.app.getMainController().requestCorrespondentPairing(correspondent);
	}
}
