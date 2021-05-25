package nsy102;

import java.util.Set;
import java.util.TreeSet;

import fr.sgo.entity.Correspondent;
import fr.sgo.entity.InMessage;
import fr.sgo.entity.Message;
import fr.sgo.entity.OutMessage;

public class TestMessageOrdering {
	public static void main(String[] args) {
		Message m1 = new OutMessage("message 1");
		Message m2 = new InMessage("message 2", new Correspondent(null, null));
		Message m3 = new OutMessage("message 3");
		Set<Message> set = new TreeSet<Message>();
		set.add(m2);
		set.add(m1);
		set.add(m3);
		System.out.println(set);
	}
}
