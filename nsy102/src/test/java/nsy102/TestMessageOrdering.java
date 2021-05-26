package nsy102;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import fr.sgo.entity.InMessage;
import fr.sgo.entity.Message;
import fr.sgo.entity.OutMessage;

public class TestMessageOrdering {
	public static void main(String[] args) {
		Message m1 = new OutMessage("message 1");
		Message m2 = new InMessage("message 2", System.currentTimeMillis());
		Message m3 = new OutMessage("message 3");
		Message m4 = new InMessage("message 4", System.currentTimeMillis());
		Set<Message> set = Collections.synchronizedSet(new TreeSet<Message>());
		set.add(m4);
		set.add(m2);
		set.add(m1);
		set.add(m3);
		System.out.println(set);
	}
}
