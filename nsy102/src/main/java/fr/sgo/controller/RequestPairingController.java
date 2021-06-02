package fr.sgo.controller;

import java.rmi.RemoteException;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.service.CorrespondentServiceLocator;
import fr.sgo.service.RMIService;
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
	private Correspondent correspondent;

	public RequestPairingController(String actionName, Correspondent correspondent) {
		super(actionName);
		this.correspondent = correspondent;
	}

	public void run() {
		String userId = correspondent.getUserId();
		String userName = correspondent.getUserName();
		Correspondent.PairingInfo pairingInfo = correspondent.getPairingInfo();
		int pairingStatus = pairingInfo.getPairingStatus();
		CorrespondentServiceLocator correspondentServiceLocator = CorrespondentServiceLocator.getInstance();
		RMIService correspondentRmiService = correspondentServiceLocator.lookup(userId).getServiceRMI();

		switch (pairingStatus) {
		case Correspondent.PAIRED:
			new InformationMessage("Invitation déjà acceptée par " + userName + "...");
			break;
		case Correspondent.PAIRING_REQUEST_SENT:
			new InformationMessage("Invitation déjà lancée pour " + userName + "...");
			break;
		case Correspondent.PAIRING_REQUEST_RECEIVED:
			try {
				correspondentRmiService.acceptPairingRequest(RMIController.getInstance(), pairingInfo.getInId(),
						pairingInfo.getOutId());
				new InformationMessage("Invitation acceptée par " + userName + "...");
				pairingInfo.setPairingStatus(Correspondent.PAIRED);
			} catch (RemoteException e1) {
				if (App.T)
					e1.printStackTrace();
				new InformationMessage("Une erreur s'est produite...");
			}
			break;
		case Correspondent.UNPAIRED:
			try {
				correspondentRmiService.requestPairing(RMIController.getInstance(),
						pairingInfo.getOutId());
				new InformationMessage("Invitation lancée pour " + userName + "...");
				pairingInfo.setPairingStatus(Correspondent.PAIRING_REQUEST_SENT);
				if (App.T)
					System.out.println("Invitation lancée pour " + userName + "...");
			} catch (RemoteException e2) {
				if (App.T)
					e2.printStackTrace();
				new InformationMessage("Une erreur s'est produite...");
			}
			break;
		default:
		}
	}
}
