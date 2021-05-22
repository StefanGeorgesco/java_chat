package fr.sgo.controller;

import java.rmi.RemoteException;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.service.CorrespondentServiceLocator;
import fr.sgo.service.ServiceRMI;
import fr.sgo.view.InformationMessage;

/**
 * Class RequestPairingController
 * 
 * Manages a pairinq request to a correspondent.
 *
 * @author Stéfan Gerogesco
 * @version 1.0
 */
public class RequestPairingController extends Controller {
	private final static boolean T = true;
	private Correspondent correspondent;

	public RequestPairingController(App app, String actionName, Correspondent correspondent) {
		super(app, actionName);
		this.correspondent = correspondent;
	}

	public void run() {
		String userId = correspondent.getUserId();
		String userName = correspondent.getUserName();
		Correspondent.PairingInfo pairingInfo = correspondent.getPairingInfo();
		int pairingStatus = pairingInfo.getPairingStatus();
		CorrespondentServiceLocator correspondentServiceLocator = this.app.getCorrespondentServiceLocator();
		ServiceRMI correspondentServiceRMI = correspondentServiceLocator.lookup(userId).getServiceRMI();

		switch (pairingStatus) {
		case Correspondent.PAIRED:
			new InformationMessage(app, "Invitation déjà acceptée par " + userName + "...");
			break;
		case Correspondent.PAIRING_REQUEST_SENT:
			new InformationMessage(app, "Invitation déjà lancée pour " + userName + "...");
			break;
		case Correspondent.PAIRING_REQUEST_RECEIVED:
			try {
				correspondentServiceRMI.acceptPairingRequest(this.app.getMainController(), pairingInfo.getInId(),
						pairingInfo.getOutId());
				new InformationMessage(app, "Invitation acceptée par " + userName + "...");
				pairingInfo.setPairingStatus(Correspondent.PAIRED);
			} catch (RemoteException e1) {
				if (T)
					e1.printStackTrace();
				new InformationMessage(app, "Une erreur s'est produite...");
			}
			break;
		case Correspondent.UNPAIRED:
			try {
				correspondentServiceRMI.requestPairing(this.app.getMainController(),
						pairingInfo.getOutId());
				new InformationMessage(app, "Invitation lancée pour " + userName + "...");
				pairingInfo.setPairingStatus(Correspondent.PAIRING_REQUEST_SENT);
				if (T)
					System.out.println("Invitation lancée pour " + userName + "...");
			} catch (RemoteException e2) {
				if (T)
					e2.printStackTrace();
				new InformationMessage(app, "Une erreur s'est produite...");
			}
			break;
		default:
		}
	}
}
