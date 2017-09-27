import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SenderLogic {
    private static SenderLogic instance;
    public SenderLogic(){
		String broker;
		String clientID;
		MemoryPersistence persistence;

		MqttClient Client;
		MqttConnectOptions connOpts;
		
		ConnectToBroker();
    }  
    
	String broker = "tcp://192.168.56.104:1883";
	String clientID = "server";
	MemoryPersistence persistence = new MemoryPersistence();

	MqttClient Client = null;
	MqttConnectOptions connOpts = new MqttConnectOptions();

    public void reConnectToBroker() {
        connOpts.setCleanSession(false);
        connOpts.isAutomaticReconnect();
        
        
        try {
			Client.connect(connOpts);
		} catch (MqttSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void ConnectToBroker() {
    	connOpts.setCleanSession(false);
        connOpts.isAutomaticReconnect();
    	
		try {
			Client = new MqttClient(broker, clientID, persistence);
			Client.connect(connOpts);
			Client.subscribe("transaction/request/#");
			Client.subscribe("transfer/request/#");
			Client.subscribe("verification/request/#");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	
    }
    
	public void sendMessage(String Topic, String message) {
		if(!Client.isConnected()) {
			reConnectToBroker();
		}
		
		System.out.println();
		System.out.println("Publishing Begin");
		
		MqttMessage Message = new MqttMessage(message.getBytes());
		Message. setQos(1);                  
		Message. setRetained(true);
		
		System.out.println("Message Configured");
		
		
		try {
			System.out.println();
			System.out.println();
			//Client.subscribe(Topic);
			//System.out.println("Topic Subscribed");
			Client.publish(Topic, Message);
			System.out.println("Message Sent");
			//Client.unsubscribe(Topic);
			System.out.println("Message Delivered");
		} catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			System.out.println();
			System.out.println("Persistence Error");
			System.out.println(e.getCause());
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			System.out.println("I have no idea what happened Error");
			e.printStackTrace();
		} 
		
	}
    
    
	public static SenderLogic getInstance(){
        if(instance == null){
            instance = new SenderLogic();
        }
        return instance;
    }

}
