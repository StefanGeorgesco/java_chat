package fr.sgo.controller;

import javax.swing.JOptionPane;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.view.MainView;

/**
 * Class DiscussionController
 * 
 * Begins a discussion with a correspondent.
 *
 * @author Stéfan Gerogesco
 * @version 1.0
 */
public class DiscussionController extends Controller {
	private Correspondent correspondent;

	public DiscussionController(App app, String actionName, MainView mainView, Correspondent correspondent) {
		super(app, actionName, mainView);
		this.correspondent = correspondent;
	}

	public void run() {
		System.out.println("Discussion avec " + correspondent.getUserName() + "...");
		JOptionPane.showMessageDialog(mainView, "Discussion avec " + correspondent.getUserName() + "...");
	}

}
