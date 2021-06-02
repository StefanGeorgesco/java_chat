package fr.sgo.controller;

import java.rmi.RemoteException;

import javax.swing.JOptionPane;
import fr.sgo.entity.Correspondent;
import fr.sgo.model.CorrespondentManager;
import fr.sgo.service.CorrespondentServiceLocator;
import fr.sgo.service.RMIService;
import fr.sgo.view.MainView;

/**
 * Class PairingRequestController
 * 
 * Manages a pairinq request from a correspondent.
 *
 * @author Stéfan Gerogesco
 * @version 1.0
 */
public class PairingRequestController extends Controller {
	private Correspondent correspondent;

	public PairingRequestController(String actionName, Correspondent correspondent) {
		super(actionName);
		this.correspondent = correspondent;
	}

	public void run() {
		String userId = correspondent.getUserId();
		String userName = correspondent.getUserName();
		Correspondent.PairingInfo pairingInfo = correspondent.getPairingInfo();
		String inId = pairingInfo.getInId();
		int pairingStatus = pairingInfo.getPairingStatus();
		CorrespondentServiceLocator correspondentServiceLocator = CorrespondentServiceLocator.getInstance();
		RMIService correspondentRmiService = correspondentServiceLocator.lookup(userId).getServiceRMI();

		boolean accept = false;
		switch (pairingStatus) {
		case Correspondent.PAIRED:
		case Correspondent.PAIRING_REQUEST_SENT:
			accept = true;
			break;
		case Correspondent.PAIRING_REQUEST_RECEIVED:
		case Correspondent.UNPAIRED:
			int response = JOptionPane.showConfirmDialog(MainView.getInstance(),
					userName + " souhaite vous inviter. Acceptez-vous ?");
			if (response == 0)
				accept = true;
			break;
		default:
		}
		if (accept) {
			try {
				correspondentRmiService.acceptPairingRequest(RMIController.getInstance(), inId, pairingInfo.getOutId());
				pairingInfo.setPairingStatus(Correspondent.PAIRED);
				CorrespondentManager.getInstance().reportChange(correspondent);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		} else {
			try {
				correspondentRmiService.refusePairing(RMIController.getInstance(), inId);
				pairingInfo.setPairingStatus(Correspondent.UNPAIRED);
			} catch (RemoteException e2) {
				e2.printStackTrace();
			}
		}
	}
}
