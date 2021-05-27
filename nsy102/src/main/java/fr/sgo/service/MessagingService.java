package fr.sgo.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.Topic;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TopicSession;
import javax.jms.JMSException;

import org.exolab.jms.message.MapMessageImpl;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.InMessage;
import fr.sgo.entity.OutMessage;
import fr.sgo.model.MessageManager;
import fr.sgo.view.InformationMessage;

public class MessagingService {
	private static MessagingService instance = null;
	private Context context;
	private TopicConnectionFactory factory;
	private TopicConnection connection;
	private static final String factoryName = "JmsTopicConnectionFactory";
	private static final String topicName = "topic1";
	private TopicSession session;
	private TopicPublisher sender;
	private Map<String, InMessagingInfo> inMessagingInfoRecords;
	private App app;

	private MessagingService(App app) {
		this.app = app;
		this.inMessagingInfoRecords = Collections.synchronizedMap(new HashMap<String, InMessagingInfo>());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (context != null) {
					try {
						context.close();
					} catch (NamingException ne) {
						ne.printStackTrace();
					}
				}
				if (connection != null) {
					try {
						connection.close();
					} catch (JMSException jmse) {
						jmse.printStackTrace();
					}
				}
			}
		});
	}

	public static MessagingService getInstance(App app) {
		if (instance == null)
			instance = new MessagingService(app);
		return instance;
	}

	public String getDestinationName() {
		return topicName;
	}

	public void open() {
		ProfileInfo profileInfo = app.getProfileInfo();
		try {
			Hashtable<String, String> props = new Hashtable<String, String>();
			props.put(Context.PROVIDER_URL,
					"rmi://" + profileInfo.getHost() + ":" + Integer.toString(profileInfo.getJMSPort()) + "/");
			props.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
			props.put(Context.SECURITY_PRINCIPAL, "admin");
			props.put(Context.SECURITY_CREDENTIALS, "openjms");
			context = new InitialContext(props);
			factory = (TopicConnectionFactory) context.lookup(factoryName);
			connection = factory.createTopicConnection();
			session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			Topic topic = (Topic) context.lookup(topicName);
			sender = session.createPublisher(topic);
			connection.start();
			if (app.T)
				System.out.println("service jms local installé");
		} catch (NamingException ne) {
			ne.printStackTrace();
			System.exit(1);
		} catch (JMSException jmse) {
			jmse.printStackTrace();
			System.exit(1);
		}
	}

	public void sendMessage(Correspondent correspondent, OutMessage message) {
		javax.jms.Message jmsMessage = translateMessage(message);
		try {
			jmsMessage.setStringProperty("InId", correspondent.getPairingInfo().getOutId());
			sender.send(jmsMessage);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void setInMessagingHandler(Correspondent correspondent) {
		if (correspondent.isPaired()) {
			String userId = correspondent.getUserId();
			CorrespondentServiceInfo correspondentServiceInfo = MessagingService.this.app
					.getCorrespondentServiceLocator().lookup(userId);
			if (correspondentServiceInfo != null) {
				String host = correspondentServiceInfo.getHost();
				ServiceRMI service = correspondentServiceInfo.getServiceRMI();
				Hashtable<String, String> props = new Hashtable<String, String>();
				props.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
				props.put(Context.SECURITY_PRINCIPAL, "admin");
				props.put(Context.SECURITY_CREDENTIALS, "openjms");
				int port;
				String topicName;
				Context context = null;
				TopicConnection connection = null;
				MessageConsumer receiver = null;
				try {
					port = service.getProfileInfo().getJMSPort();
					props.put(Context.PROVIDER_URL, "rmi://" + host + ":" + Integer.toString(port) + "/");
					context = new InitialContext(props);
					TopicConnectionFactory factory = (TopicConnectionFactory) context.lookup(factoryName);
					connection = factory.createTopicConnection();
					TopicSession session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
					topicName = service.getDestinationName(app.getMainController(),
							correspondent.getPairingInfo().getOutId());
					Topic topic = (Topic) context.lookup(topicName);
//					receiver = session.createDurableSubscriber(topic, correspondent.getPairingInfo().getInId(),
//							"InId = '" + correspondent.getPairingInfo().getInId() + "'", true);
					receiver = session.createConsumer(topic, "InId = '" + correspondent.getPairingInfo().getInId() + "'", true);
					receiver.setMessageListener(
							new InMessageHandler(correspondent, MessagingService.this.app.getMessageManager()));
					connection.start();
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NamingException e) {
					e.printStackTrace();
				} catch (JMSException e) {
					e.printStackTrace();
				}

				if (context != null && connection != null && receiver != null) {
					inMessagingInfoRecords.put(userId, new InMessagingInfo(context, connection, receiver));
				}
			}
		}
	}

	public void unsetInMessagingHandler(Correspondent correspondent) {
		String userId = correspondent.getUserId();
		InMessagingInfo messagingInfo = inMessagingInfoRecords.get(userId);
		if (messagingInfo != null) {
			messagingInfo.close();
			inMessagingInfoRecords.remove(userId);
		}
	}

	private javax.jms.Message translateMessage(OutMessage applicationMessage) {
		javax.jms.Message jmsMessage = null;
		try {
			jmsMessage = new MapMessageImpl();
			jmsMessage.setStringProperty("contents", applicationMessage.getContents());
			jmsMessage.setLongProperty("timeWritten", applicationMessage.getTimeWritten());
		} catch (JMSException e) {
			e.printStackTrace();
			new InformationMessage(app, "Le message jms " + applicationMessage.getContents() + " n'a pas pu être créé");
		}
		return jmsMessage;
	}

	private InMessage translateMessage(javax.jms.Message jmsMessage) {
		InMessage applicationMessage = null;
		try {
			applicationMessage = new InMessage(jmsMessage.getStringProperty("contents"),
					jmsMessage.getLongProperty("timeWritten"));
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return applicationMessage;
	}

	private class InMessagingInfo {
		private Context context;
		private TopicConnection connection;
		private MessageConsumer receiver;

		public InMessagingInfo(Context context, TopicConnection connection, MessageConsumer receiver) {
			this.context = context;
			this.connection = connection;
			this.receiver = receiver;
		}

		public void close() {
			if (receiver != null) {
				try {
					receiver.setMessageListener(null);
				} catch (JMSException jmse1) {
					jmse1.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException jmse2) {
					jmse2.printStackTrace();
				}
			}
			if (context != null) {
				try {
					context.close();
				} catch (NamingException ne) {
					ne.printStackTrace();
				}
			}
		}
	}

	private class InMessageHandler implements MessageListener {
		Correspondent correspondent;
		MessageManager messageManager;

		public InMessageHandler(Correspondent correspondent, MessageManager messageManager) {
			this.correspondent = correspondent;
			this.messageManager = messageManager;
		}

		@Override
		public void onMessage(javax.jms.Message jmsmessage) {
			InMessage applicationMessage = translateMessage(jmsmessage);
			applicationMessage.setCorrespondent(correspondent);
			if (app.T)
				System.out.println(
						"message reçu de " + correspondent.getUserName() + " : " + applicationMessage.getContents());
			messageManager.addMessage(correspondent.getUserId(), applicationMessage);
		}

	}

}
