package fr.sgo.model;

import fr.sgo.entity.*;
import fr.sgo.service.*;
import java.util.*;

public class Alimenter {
	private static Map<String, Correspondent> pairedCorrespondents;
	private static final String objectName = "pairedcorrespondents";

	public static void main(String[] args) {
		pairedCorrespondents = Collections.synchronizedMap(new HashMap<String, Correspondent>());
		String[] userNames = new String[] { "Mark", "Alfred", "Mary", "Emma" };
		String[] userIds = new String[] { "C57EF90C28304A919C730B366CECA02C", "39C29A6420804F15A27111EAE9ED3EAE",
				"C6D118E8F480488997FA8963D4E73876", "9A1A2D1FF1514E1BBC4CD668D1098F27" };
		for (int i = 0; i < 4; i++) {
			pairedCorrespondents.put(userIds[i], new Correspondent(userIds[i], userNames[i], false));
		}
		Storage.save(pairedCorrespondents, objectName);
		System.out.println(pairedCorrespondents);
	}
}
