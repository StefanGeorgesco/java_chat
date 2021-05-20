package fr.sgo.controller;

import java.rmi.RemoteException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
	private String inId;

	public PairingRequestController(App app, String actionName, Correspondent correspondent, String inId) {
		super(app, actionName);
		this.correspondent = correspondent;
		this.inId = inId;
	}

	public void run() {
		String userId = correspondent.getUserId();
		String userName = correspondent.getUserName();
		Correspondent.PairingInfo pairingInfo = correspondent.getPairingInfo();
		int pairingStatus = pairingInfo.getPairingStatus();
		CorrespondentServiceLocator correspondentServiceLocator = this.app.getCorrespondentServiceLocator();
		ServiceRMI correspondentServiceRMI = correspondentServiceLocator.lookup(userId).getServiceRMI();

		boolean accept = false;
		switch (pairingStatus) {
			case Correspondent.PAIRED:
			case Correspondent.PAIRING_REQUEST_SENT:
				accept = true;
				break;
			case Correspondent.PAIRING_REFUSED:
			case Correspondent.PAIRING_REQUEST_RECEIVED:
			case Correspondent.UNPAIRED:
				int response = JOptionPane.showConfirmDialog(new JPanel(), userName + " souhaite vous inviter. OK ?");
				if (response == 0)
					accept = true;
				break;
			default:
		}
		if (accept) {
			try {
				correspondentServiceRMI.acceptPairingRequest(app.getMainController(), inId, pairingInfo.getOutId());
				pairingInfo.setInId(inId);
				pairingInfo.setPairingStatus(Correspondent.PAIRED);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			try {
				correspondentServiceRMI.refusePairing(app.getMainController(), inId);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}
