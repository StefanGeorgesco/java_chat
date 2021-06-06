package fr.sgo.view;

import java.awt.Component;

import javax.swing.JOptionPane;

public class InformationView extends Thread {
	private Component parent;
	private String message;
	
	public InformationView(Component parent, String message) {
		this.parent = parent;
		this.message = message;
		start();
	}
	
	public InformationView(String message) {
		this(MainView.getInstance(), message);
	}
	
	@Override
	public void run() {
		JOptionPane.showMessageDialog(parent, message);
	}
}
