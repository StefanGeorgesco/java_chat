package fr.sgo.model;

import fr.sgo.entity.*;
import fr.sgo.service.*;
import java.util.*;

public class Alimenter {
	private static List<Correspondent> pairedCorrespondents;
	private static final String objectName = "pairedcorrespondents";

	public static void main(String[] args) {
		pairedCorrespondents = new ArrayList<Correspondent>();
		String[] userNames = new String[] { "Mark", "Alfred", "Mary", "Emma" };
		String[] userIds = new String[] { "A802C2E1A905411C8046F06339E6DEBC", "33F05A9CDE5B4C25AD9383A118635651",
				"9698B3B3584C4F6DB4F7D02DEEF9B481", "5C11DC83485C4C34BB65219B7192989C" };
		for (int i = 0; i < 4; i++) {
			Correspondent correspondent = new Correspondent(userIds[i], userNames[i], false);
			correspondent.getPairingInfo().setPairingStatus(Correspondent.PAIRED);
			pairedCorrespondents.add(correspondent);
		}
		Storage.save(pairedCorrespondents, objectName);
		System.out.println(pairedCorrespondents);
	}
}
