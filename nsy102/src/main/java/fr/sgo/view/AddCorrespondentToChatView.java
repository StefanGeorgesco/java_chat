package fr.sgo.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fr.sgo.controller.ChatController;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.HostedGroupChat;

public class AddCorrespondentToChatView {
	private HostedGroupChat chat;
	private Set<Correspondent> correspondents;
	private JFrame frame;
	private JList<String> list;
	private JButton validate;
	private JButton cancel;
	private int[] selected;

	public AddCorrespondentToChatView(HostedGroupChat chat, Collection<Correspondent> correspondents) {
		this.chat = chat;
		this.correspondents = new TreeSet<Correspondent>(correspondents);
		frame = new JFrame("Ajouter des participants");
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Sélectionnez des participants : ");
		list = new JList<String>();
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				selected = list.getSelectedIndices();
			}
		});
		validate = new JButton("OK");
		cancel = new JButton("Annuler");
		panel.add(label);
		panel.add(list);
		panel.add(validate);
		panel.add(cancel);
		frame.add(panel);
		Set<Correspondent> selectedCorrespondents = new HashSet<Correspondent>();
		String[] userNamesTable = new String[this.correspondents.size()];
		Correspondent[] correspondentsTable = new Correspondent[this.correspondents.size()];
		int i = 0;
		for (Correspondent correspondent : this.correspondents) {
			userNamesTable[i] = correspondent.getUserName();
			correspondentsTable[i++] = correspondent;
		}
		list.setListData(userNamesTable);
		validate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selected != null && selected.length > 0) {
					for (int i : selected) {
						selectedCorrespondents.add(correspondentsTable[i]);
					}
					ChatController.getInstance().addCorrespondentsToChat(AddCorrespondentToChatView.this.chat,
							selectedCorrespondents);
					frame.dispose();
				}
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		frame.pack();
		frame.setVisible(true);
	}

}
