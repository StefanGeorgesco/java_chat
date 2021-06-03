package fr.sgo.controller;

import java.rmi.RemoteException;

import javax.swing.JOptionPane;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.model.CorrespondentManager;
import fr.sgo.service.CorrespondentServiceLocator;
import fr.sgo.service.RMIService;
import fr.sgo.view.InformationView;
import fr.sgo.view.MainView;

public class CorrespondentController {
	private static CorrespondentController instance = null;

	private CorrespondentController() {
	}

	public static synchronized CorrespondentController getInstance() {
		if (instance == null) {
			instance = new CorrespondentController();
		}
		return instance;
	}

	public void requestPairing(Correspondent correspondent) {
		String userId = correspondent.getUserId();
		String userName = correspondent.getUserName();
		Correspondent.PairingInfo pairingInfo = correspondent.getPairingInfo();
		int pairingStatus = pairingInfo.getPairingStatus();
		CorrespondentServiceLocator correspondentServiceLocator = CorrespondentServiceLocator.getInstance();
		RMIService correspondentRmiService = correspondentServiceLocator.lookup(userId).getServiceRMI();

		switch (pairingStatus) {
		case Correspondent.PAIRED:
			new InformationView("Invitation déjà acceptée par " + userName + "...");
			break;
		case Correspondent.PAIRING_REQUEST_SENT:
			new InformationView("Invitation déjà lancée pour " + userName + "...");
			break;
		case Correspondent.PAIRING_REQUEST_RECEIVED:
			try {
				correspondentRmiService.acceptPairingRequest(RMIController.getInstance(), pairingInfo.getInId(),
						pairingInfo.getOutId());
				new InformationView("Invitation acceptée par " + userName + "...");
				pairingInfo.setPairingStatus(Correspondent.PAIRED);
			} catch (RemoteException e1) {
				if (App.T)
					e1.printStackTrace();
				new InformationView("Une erreur s'est produite...");
			}
			break;
		case Correspondent.UNPAIRED:
			try {
				correspondentRmiService.requestPairing(RMIController.getInstance(), pairingInfo.getOutId());
				new InformationView("Invitation lancée pour " + userName + "...");
				pairingInfo.setPairingStatus(Correspondent.PAIRING_REQUEST_SENT);
				if (App.T)
					System.out.println("Invitation lancée pour " + userName + "...");
			} catch (RemoteException e2) {
				if (App.T)
					e2.printStackTrace();
				new InformationView("Une erreur s'est produite...");
			}
			break;
		default:
		}
	}

	public void handlePairingRequest(Correspondent correspondent) {
		new Thread(new PairingRequestHandler(correspondent)).start();
	}

	private class PairingRequestHandler implements Runnable {
		private Correspondent correspondent;

		public PairingRequestHandler(Correspondent correspondent) {
			this.correspondent = correspondent;
		}

		@Override
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
					correspondentRmiService.acceptPairingRequest(RMIController.getInstance(), inId,
							pairingInfo.getOutId());
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

}
