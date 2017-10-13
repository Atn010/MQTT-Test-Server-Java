/**
 * @author Antonius George Sunggeriwan <atn010g@gmail.com>
 *
 */
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SenderLogic {
    private static SenderLogic instance;
    public static SenderLogic getInstance(){
        if(instance == null){
            instance = new SenderLogic();
        }
        return instance;
    }  
    
	String broker = "tcp://192.168.56.101:1883";
	MqttClient Client = null;
	String clientID = "server";

	MqttConnectOptions connOpts = new MqttConnectOptions();
	MemoryPersistence persistence = new MemoryPersistence();

    public SenderLogic(){
		String broker;
		String clientID;
		MemoryPersistence persistence;

		MqttClient Client;
		MqttConnectOptions connOpts;
		
		ConnectToBroker();
    }
    
    /**
     * Connect to the Broker.
     */
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
    
    /**
     * This Method attempts to reconnect to the broker
     */
	public void reConnectToBroker() {
        connOpts.setCleanSession(false);
        connOpts.setAutomaticReconnect(true);
        
        try {
			Client.connect(connOpts);
		} catch (MqttSecurityException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
    }
    
    
	/**	
	 * Check if the client is connected. If not, attempt to reconnect to broker.
	 * 
	 * Send the Message to the corresponding Topic by configuring the message for retained and quality of service and the send message.
	 * 
	 * @param Topic The Topic to Publish
	 * @param message The Message to Publish

	 */
	public void sendMessage(String Topic, String message) {
		//configure and send the appropriate message to the specified topic
		
		if(!Client.isConnected()) {
			reConnectToBroker();
		}
		
		MqttMessage Message = new MqttMessage(message.getBytes());
		Message. setQos(1);                  
		Message. setRetained(true);
		
		
		try {
			Client.publish(Topic, Message);
		} catch (MqttPersistenceException e) {
			System.out.println(e.getCause());
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			e.printStackTrace();
		} catch (MqttException e) {
			System.out.println(e.getCause());
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			e.printStackTrace();
		} 
		
	}

}
