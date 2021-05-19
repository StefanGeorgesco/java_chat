package fr.sgo.controller;

import java.rmi.RemoteException;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.service.CorrespondentServiceLocator;
import fr.sgo.service.ServiceRMI;
import fr.sgo.view.InformationMessage;

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

	public InvitationController(App app, String actionName, Correspondent correspondent) {
		super(app, actionName);
		this.correspondent = correspondent;
	}

	public void run() {
		String userName = correspondent.getUserName();
		switch (correspondent.getPairingInfo().getPairingStatus()) {
		case Correspondent.PAIRING_GRANTED:
			new InformationMessage(app, "Invitation déjà acceptée par " + userName + "...");
			break;
		case Correspondent.PAIRING_REFUSED:
			new InformationMessage(app, "Invitation déjà refusée par " + userName + "...");
			break;
		case Correspondent.PAIRING_REQUIRED:
			new InformationMessage(app, "Invitation déjà lancée pour " + userName + "...");
			break;
		case Correspondent.UNPAIRED:
			CorrespondentServiceLocator correspondentServiceLocator = this.app.getCorrespondentServiceLocator();
			String userId = correspondent.getUserId();
			ServiceRMI correspondentServiceRMI = correspondentServiceLocator.lookup(userId).getServiceRMI();
			try {
				correspondentServiceRMI.requestPairing(this.app.getMainController(),
						this.correspondent.getPairingInfo().getOutId());
				new InformationMessage(app, "Invitation lancée pour " + userName + "...");
				if (T)
					System.out.println("Invitation lancée pour " + correspondent.getUserName() + "...");
			} catch (RemoteException e) {
				if (T)
					e.printStackTrace();
				new InformationMessage(app, "Une erreur s'est produite...");
			}
			break;
		default:
		}
	}
}
