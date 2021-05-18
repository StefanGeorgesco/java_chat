package fr.sgo.service;

import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import fr.sgo.app.App;

/**
 * Class ServiceAgent
 * 
 * Publishes services (RMI & JmDNS)
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class ServiceAgent
{
    private final static boolean T = true; //
    private static ServiceAgent instance = null;
    private App app;
    public static final String SERVICE_TYPE = "_monserviceRMI._tcp.local.";
    private static final String serviceName = IDGenerator.newId();
    private static Registry registry;
    private static JmDNS jmdns = null;

    private ServiceAgent(App app) {
    	this.app = app;
        java.util.Properties p = System.getProperties();
        p.put("java.security.policy","policy.all");
        System.setProperties(p);
        System.setSecurityManager(new RMISecurityManager());
        try
        {
            jmdns = JmDNS.create();
        }
        catch (java.io.IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    public static ServiceAgent getInstance(App app) {
        if (instance == null)
            instance = new ServiceAgent(app);
        return instance;
    }

    public void publishServices(int delay) {
        new ServicePublisher(delay);
    }

    private class ServicePublisher extends Thread {
        private int delay;

        public ServicePublisher(int delay) {
            this.delay = delay;
            start();
        }

        @Override
        public void run() {
            try
            {
                Thread.sleep(delay);
            }
            catch (java.lang.InterruptedException ie)
            {
                ie.printStackTrace();
            }
            try
            {
                registry = LocateRegistry.createRegistry(app.getProfileInfo().getRMIPort());
            }
            catch (RemoteException re1)
            {
                try
                {
                    registry = LocateRegistry.getRegistry(app.getProfileInfo().getRMIPort());
                }
                catch (RemoteException re2)
                {
                    re2.printStackTrace();
                    System.exit(1);
                }
            }
            if (T) System.out.println("registre RMI créé ou lié");
            try
            {
                registry.rebind(serviceName, app.getMainController());
            }
            catch (RemoteException re3)
            {
                re3.printStackTrace();
                System.exit(1);
            }
            if (T) System.out.println("service enregistré dans le registre RMI");
            Map<String, String> props = new HashMap<String, String>();
            props.put("userId", app.getProfileInfo().getUserId());
            props.put("userName", app.getProfileInfo().getUserName());
            try
            {
                ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, serviceName, app.getProfileInfo().getRMIPort(), 0, 0, props);
                jmdns.registerService(serviceInfo);
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
            if (T) System.out.println("service mDNS publié");
            Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        try{
                            System.out.println("retrait des services RMI et mDNS...");
                            registry.unbind(serviceName);
                            jmdns.unregisterAllServices();
                            jmdns.close();
                        }catch(Exception e){}
                    }
                });
        }
    }
}
