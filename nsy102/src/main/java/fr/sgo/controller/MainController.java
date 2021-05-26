package fr.sgo.controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import fr.sgo.service.ProfileInfo;
import fr.sgo.service.ServiceRMI;
import fr.sgo.view.InformationMessage;
import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;

/**
 * Class MainController
 * 
 * Main application controller.
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
public class MainController extends UnicastRemoteObject implements ServiceRMI {
	private static final long serialVersionUID = 8194586220342790039L;
	private static MainController instance = null;
	private App app;

	private MainController(App app) throws RemoteException {
		this.app = app;
	}

	public static MainController getInstance(App app) throws RemoteException {
		if (instance == null)
			instance = new MainController(app);
		return instance;
	}
	
	public App getApp() {
		return app;
	}
	
	public boolean isActive() throws RemoteException {
		return true;
	}
	
	public ProfileInfo getProfileInfo() throws RemoteException {
		return app.getProfileInfo();
	}
	
	public void requestPairing(ServiceRMI service, String inId) throws RemoteException {
		String userId = service.getProfileInfo().getUserId();
		String userName = service.getProfileInfo().getUserName();
		if (app.T)
			System.out.println("Connection required from " + userName
					+ " with id " + inId);
		Correspondent correspondent = app.getCorrespondentManager().getCorrespondent(userId);
		if (correspondent == null) {
			correspondent = new Correspondent(userId, userName, true);
			app.getCorrespondentManager().add(correspondent);
		}
		Correspondent.PairingInfo pairingInfo = correspondent.getPairingInfo();
		int pairingStatus = pairingInfo.getPairingStatus();		
		pairingInfo.setInId(inId);

		switch (pairingStatus) {
		case Correspondent.UNPAIRED:
			pairingInfo.setPairingStatus(Correspondent.PAIRING_REQUEST_RECEIVED);
			new PairingRequestController(app, "", correspondent).execute();
			break;
		case Correspondent.PAIRING_REQUEST_RECEIVED:
			break;
		case Correspondent.PAIRING_REQUEST_SENT:
		case Correspondent.PAIRED:
			try {
				service.acceptPairingRequest(app.getMainController(), inId, pairingInfo.getOutId());
				pairingInfo.setPairingStatus(Correspondent.PAIRED);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			break;
		default:
		}
	}
	
	public void acceptPairingRequest(ServiceRMI service, String inId, String outId) throws RemoteException {
		String userId = service.getProfileInfo().getUserId();
		String userName = service.getProfileInfo().getUserName();
		Correspondent correspondent = app.getCorrespondentManager().getCorrespondent(userId);
		if (correspondent != null) {
			Correspondent.PairingInfo pairingInfo = correspondent.getPairingInfo();
			int pairingStatus = pairingInfo.getPairingStatus();	
			
			switch (pairingStatus) {
			case Correspondent.PAIRING_REQUEST_SENT:
				if (inId.equals(pairingInfo.getOutId())) {
					if (app.T)
						System.out.println("Connection accepted from " + userName
								+ " with id " + outId);
					new InformationMessage(app, userName +
							" a accepté votre invitation. Il (elle) fait maintenant partie de vos contacts.");
					pairingInfo.setInId(outId);
					pairingInfo.setPairingStatus(Correspondent.PAIRED);
					app.getCorrespondentManager().reportChange(correspondent);
				}
				break;
			default:
			}
		}
	}
	
	public void refusePairing(ServiceRMI service, String inId)  throws RemoteException {
		String userId = service.getProfileInfo().getUserId();
		String userName = service.getProfileInfo().getUserName();
		Correspondent correspondent = app.getCorrespondentManager().getCorrespondent(userId);
		if (correspondent != null) {
			Correspondent.PairingInfo pairingInfo = correspondent.getPairingInfo();
			int pairingStatus = pairingInfo.getPairingStatus();	
			
			switch (pairingStatus) {
			case Correspondent.PAIRING_REQUEST_SENT:
				if (inId.equals(pairingInfo.getOutId())) {
					if (app.T)
						System.out.println("Connection refused from " + userName);
					new InformationMessage(app, userName +
							" a refusé votre invitation.");
					pairingInfo.setPairingStatus(Correspondent.UNPAIRED);
					app.getCorrespondentManager().reportChange(correspondent);
				}
				break;
			default:
			}
		}
	}
	
	public String getDestinationName(ServiceRMI service, String outId) throws RemoteException {
		String userId = service.getProfileInfo().getUserId();
		Correspondent correspondent = app.getCorrespondentManager().getCorrespondent(userId);
		String destinationName = "refused";
		if (outId.equals(correspondent.getPairingInfo().getInId()))
			destinationName = app.getMessagingService().getDestinationName();
		return destinationName;
	}

}
