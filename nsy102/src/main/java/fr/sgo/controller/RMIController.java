package fr.sgo.controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import fr.sgo.service.MessagingService;
import fr.sgo.service.ProfileInfo;
import fr.sgo.service.RMIService;
import fr.sgo.view.InformationMessage;
import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.model.CorrespondentManager;

/**
 * Class RMIController
 * 
 * Main application controller.
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
public class RMIController extends UnicastRemoteObject implements RMIService {
	private static final long serialVersionUID = 8194586220342790039L;
	private static RMIController instance = null;

	private RMIController() throws RemoteException {
	}

	public static synchronized RMIController getInstance() {
		if (instance == null)
			try {
				instance = new RMIController();
			} catch (RemoteException e) {
				e.printStackTrace();
				System.exit(1);
			}
		return instance;
	}
	
	public boolean isActive() throws RemoteException {
		return true;
	}
	
	public ProfileInfo getProfileInfo() throws RemoteException {
		return ProfileInfo.getInstance();
	}
	
	public void requestPairing(RMIService service, String inId) throws RemoteException {
		String userId = service.getProfileInfo().getUserId();
		String userName = service.getProfileInfo().getUserName();
		if (App.T)
			System.out.println("Connection required from " + userName
					+ " with id " + inId);
		Correspondent correspondent = CorrespondentManager.getInstance().getCorrespondent(userId);
		if (correspondent == null) {
			correspondent = new Correspondent(userId, userName, true);
			CorrespondentManager.getInstance().add(correspondent);
		}
		Correspondent.PairingInfo pairingInfo = correspondent.getPairingInfo();
		int pairingStatus = pairingInfo.getPairingStatus();		
		pairingInfo.setInId(inId);

		switch (pairingStatus) {
		case Correspondent.UNPAIRED:
			pairingInfo.setPairingStatus(Correspondent.PAIRING_REQUEST_RECEIVED);
			new PairingRequestController("", correspondent).execute();
			break;
		case Correspondent.PAIRING_REQUEST_RECEIVED:
			break;
		case Correspondent.PAIRING_REQUEST_SENT:
		case Correspondent.PAIRED:
			try {
				service.acceptPairingRequest(RMIController.getInstance(), inId, pairingInfo.getOutId());
				pairingInfo.setPairingStatus(Correspondent.PAIRED);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			break;
		default:
		}
	}
	
	public void acceptPairingRequest(RMIService service, String inId, String outId) throws RemoteException {
		String userId = service.getProfileInfo().getUserId();
		String userName = service.getProfileInfo().getUserName();
		Correspondent correspondent = CorrespondentManager.getInstance().getCorrespondent(userId);
		if (correspondent != null) {
			Correspondent.PairingInfo pairingInfo = correspondent.getPairingInfo();
			int pairingStatus = pairingInfo.getPairingStatus();	
			
			switch (pairingStatus) {
			case Correspondent.PAIRING_REQUEST_SENT:
				if (inId.equals(pairingInfo.getOutId())) {
					if (App.T)
						System.out.println("Connection accepted from " + userName
								+ " with id " + outId);
					new InformationMessage(userName +
							" a accepté votre invitation. Il (elle) fait maintenant partie de vos contacts.");
					pairingInfo.setInId(outId);
					pairingInfo.setPairingStatus(Correspondent.PAIRED);
					CorrespondentManager.getInstance().reportChange(correspondent);
				}
				break;
			default:
			}
		}
	}
	
	public void refusePairing(RMIService service, String inId)  throws RemoteException {
		String userId = service.getProfileInfo().getUserId();
		String userName = service.getProfileInfo().getUserName();
		Correspondent correspondent = CorrespondentManager.getInstance().getCorrespondent(userId);
		if (correspondent != null) {
			Correspondent.PairingInfo pairingInfo = correspondent.getPairingInfo();
			int pairingStatus = pairingInfo.getPairingStatus();	
			
			switch (pairingStatus) {
			case Correspondent.PAIRING_REQUEST_SENT:
				if (inId.equals(pairingInfo.getOutId())) {
					if (App.T)
						System.out.println("Connection refused from " + userName);
					new InformationMessage(userName +
							" a refusé votre invitation.");
					pairingInfo.setPairingStatus(Correspondent.UNPAIRED);
					CorrespondentManager.getInstance().reportChange(correspondent);
				}
				break;
			default:
			}
		}
	}
	
	public String getDestinationName(RMIService service, String outId) throws RemoteException {
		String userId = service.getProfileInfo().getUserId();
		Correspondent correspondent = CorrespondentManager.getInstance().getCorrespondent(userId);
		String destinationName = "refused";
		if (outId.equals(correspondent.getPairingInfo().getInId()))
			destinationName = MessagingService.getInstance().getDestinationName();
		return destinationName;
	}

}
