package fr.sgo.view;

import javax.swing.JOptionPane;

import fr.sgo.app.App;

public class InformationView extends Thread {
	App app;
	String message;
	
	public InformationView(String message) {
		this.message = message;
		start();
	}
	
	@Override
	public void run() {
		JOptionPane.showMessageDialog(MainView.getInstance(), message);
	}
}
