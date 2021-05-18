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
		String[] userIds = new String[] { "07EE366420AF4EEC9833F2B1843D2920", "9055B0018E6A4BB9A79FC92CBEBD3E66",
				"0BEB9C851E5A47AA95588C66EE7CADDD", "DB30CBFCD79449D1BA1D8CB9220FEC96" };
		for (int i = 0; i < 4; i++) {
			pairedCorrespondents.put(userIds[i], new Correspondent(userIds[i], userNames[i], false));
		}
		Storage.save(pairedCorrespondents, objectName);
		System.out.println(pairedCorrespondents);
	}
}
