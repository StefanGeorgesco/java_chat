package fr.sgo.view;

import javax.swing.JOptionPane;

import fr.sgo.app.App;

public class InformationMessage extends Thread {
	App app;
	String message;
	
	public InformationMessage(App app, String message) {
		this.app = app;
		this.message = message;
		start();
	}
	
	@Override
	public void run() {
		JOptionPane.showMessageDialog(app.getMainView(), message);
	}
}
