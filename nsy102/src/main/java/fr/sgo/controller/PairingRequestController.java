package fr.sgo.controller;

import java.rmi.RemoteException;

import javax.swing.JOptionPane;
import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.service.CorrespondentServiceLocator;
import fr.sgo.service.ServiceRMI;

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

	public PairingRequestController(App app, String actionName, Correspondent correspondent) {
		super(app, actionName);
		this.correspondent = correspondent;
	}

	public void run() {
		String userId = correspondent.getUserId();
		String userName = correspondent.getUserName();
		Correspondent.PairingInfo pairingInfo = correspondent.getPairingInfo();
		String inId = pairingInfo.getInId();
		int pairingStatus = pairingInfo.getPairingStatus();
		CorrespondentServiceLocator correspondentServiceLocator = this.app.getCorrespondentServiceLocator();
		ServiceRMI correspondentServiceRMI = correspondentServiceLocator.lookup(userId).getServiceRMI();

		boolean accept = false;
		switch (pairingStatus) {
		case Correspondent.PAIRED:
		case Correspondent.PAIRING_REQUEST_SENT:
			accept = true;
			break;
		case Correspondent.PAIRING_REQUEST_RECEIVED:
		case Correspondent.UNPAIRED:
			int response = JOptionPane.showConfirmDialog(app.getMainView(),
					userName + " souhaite vous inviter. Acceptez-vous ?");
			if (response == 0)
				accept = true;
			break;
		default:
		}
		if (accept) {
			try {
				correspondentServiceRMI.acceptPairingRequest(app.getMainController(), inId, pairingInfo.getOutId());
				pairingInfo.setPairingStatus(Correspondent.PAIRED);
				app.getCorrespondentManager().reportChange(correspondent);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		} else {
			try {
				correspondentServiceRMI.refusePairing(app.getMainController(), inId);
				pairingInfo.setPairingStatus(Correspondent.UNPAIRED);
			} catch (RemoteException e2) {
				e2.printStackTrace();
			}
		}
	}
}
