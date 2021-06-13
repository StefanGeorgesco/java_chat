package fr.sgo.controller;

import java.awt.Component;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JOptionPane;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.GroupChat;
import fr.sgo.entity.HostedGroupChat;
import fr.sgo.model.ChatManager;
import fr.sgo.model.CorrespondentManager;
import fr.sgo.service.CorrespondentServiceLocator;
import fr.sgo.view.AddCorrespondentToChatView;
import fr.sgo.view.InformationView;
import fr.sgo.view.MainView;

public class ChatController {
	private static ChatController instance = null;

	private ChatController() {
	}

	public static synchronized ChatController getInstance() {
		if (instance == null) {
			instance = new ChatController();
		}
		return instance;
	}

	public void createGroupChat() {
		String name = "";
		while (name != null && name.length() == 0) {
			name = JOptionPane.showInputDialog(MainView.getInstance(), "Nom du groupe de discussion",
					"Créer un groupe de discussion", JOptionPane.PLAIN_MESSAGE);
		}
		if (name != null) {
			ChatManager.getInstance().addGroupChat(new HostedGroupChat(name));
		}
	}

	public void showAddCorrespondentsView(Component component, HostedGroupChat chat) {
		if (chat instanceof HostedGroupChat) {
			Collection<Correspondent> correspondents = new HashSet<Correspondent>();
			for (Correspondent correspondent : CorrespondentManager.getInstance().getPairedCorrespondents()) {
				if (correspondent.isOnline())
					correspondents.add(correspondent);
			}
			correspondents.removeAll(((HostedGroupChat) chat).getCorrespondents());
			if (correspondents.size() > 0)
				new AddCorrespondentToChatView(chat, correspondents);
			else
				new InformationView(component, "Aucun correspondant disponible");
		}
	}

	public void addCorrespondentsToChat(HostedGroupChat chat, Collection<Correspondent> correspondents) {
		if (App.T)
			System.out.println(
					"ajout de participants au chat" + ((GroupChat) chat).getName() + " : " + correspondents.toString());
		final CorrespondentServiceLocator correspondentServiceLocator = CorrespondentServiceLocator.getInstance();
		new Thread() {
			@Override
			public void run() {
				for (Correspondent correspondent : correspondents) {
					RMIService service = correspondentServiceLocator.lookup(correspondent.getUserId()).getServiceRMI();
					if (service != null)
						try {
							if (service.inviteToGroupChat(RMIController.getInstance(),
									correspondent.getPairingInfo().getOutId(), chat))
								chat.addCorrespondent(correspondent);
							else
								new InformationView(null, correspondent.getUserName() + " n'est pas disponible");
						} catch (Exception e) {
							new InformationView(null, correspondent.getUserName() + " n'est pas disponible");
						}
				}
			}
		}.start();
	}

}
