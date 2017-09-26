import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public class ConnectionLogic implements MqttCallback {
	Data data = Data.getInstance();
	
	// initiator
	public ConnectionLogic() {
		String broker = "tcp://192.168.56.104:1883";
		String clientID = "server";
		MemoryPersistence persistence = new MemoryPersistence();

		MqttClient Client = null;
		try {
			Client = new MqttClient(broker, clientID, persistence);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MqttConnectOptions connOpts = new MqttConnectOptions();

		connOpts.setCleanSession(false);
		connOpts.isAutomaticReconnect();
		try {
			Client.connect(connOpts);
			Client.subscribe("Transaction/request/#");
			Client.subscribe("Transfer/request/#");
			Client.subscribe("Verification/request/#");
		} catch (MqttSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	String broker = "tcp://192.168.56.104:1883";
	String clientID = "server";
	MemoryPersistence persistence = new MemoryPersistence();

	MqttClient Client = null;
	
	
	

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String Topic, MqttMessage Message) throws Exception {

		// TODO Auto-generated method stub

		//Split the Topic
		String[] topic = Topic.split("~");
		String topicName = topic[0];
		String topicType = topic[1];
		String topicUser = topic[2];

		//Transaction Topic
		if (topicName.compareTo("transaction") == 0) {
			if (topicType.compareTo("request") == 0) {
				//Create Predicate if contain the user in Sender or Receiver
				Predicate<objList> bySender = p -> p.Account.compareTo(topicUser) == 0;
				Predicate<objList> byReceiver = p -> p.Recipient.compareTo(topicUser) == 0;

				//Create a List containing the user either as a Sender or a Receiver 
				List<objList> Result1 = FluentIterable.from(data.transList).filter(bySender).toList();
				List<objList> Result2 = FluentIterable.from(data.transList).filter(byReceiver).toList();

				// Combine both List
				Result1.addAll(Result2);

				//Sort the Data based on Date
				Collections.sort(Result1, new Comparator<objList>() {
					DateFormat f = new SimpleDateFormat("dd/MM/yy hh:mm");
					@Override
					public int compare(objList o1, objList o2) {
						// TODO Auto-generated method stub
						try {
							return f.parse(o1.getDateTime()).compareTo(f.parse(o2.getDateTime()));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return 0;
					}
				});
				
				//Send the data to User
				MqttMessage messageList = new MqttMessage(Result1.toString().getBytes());
				messageList. setQos(1);                  
				messageList. setRetained(true); 
				
				Client.publish(topicName+"/list/"+topicUser, messageList); 
				
				//Get User money
				List<Money> accountMoney = data.accMoney.stream().filter(p -> p.Account.equals(topicUser)).collect(Collectors.toList());
				
				//Send the info to User
				MqttMessage messageMoney = new MqttMessage(accountMoney.get(0).getMoney().toString().getBytes());
				messageMoney. setQos(1);                  
				messageMoney. setRetained(true); 
				
				Client.publish(topicName+"/money/"+topicUser, messageMoney); 

			}
		}
		
		//Transfer Request
		if (topicName.compareTo("transfer") == 0) {
			if (topicType.compareTo("request") == 0) {
				
				//decode the message
				String[] message = Message.toString().split("~");
				String messageDate = message[0];
				String messageSender = message[1];
				String messageReceiver = message[2];
				String messageAmountRaw = message[3];
				Long messageAmount = Long.valueOf(messageAmountRaw).longValue();
				
				//check Sender name
				List<Money> accountSender = data.accMoney.stream().filter(p -> p.Account.equals(messageSender))
						.collect(Collectors.toList());
				
				//heck Receiver Name
				List<Money> accountReceiver = data.accMoney.stream().filter(p -> p.Account.equals(messageReceiver))
						.collect(Collectors.toList());
					

				//if Sender is not false
				if (accountSender != null) {
					//if Receiver is not false
					if(accountReceiver != null) {
						
						//if Money is available
						if((accountSender.get(0).getMoney() - messageAmount) <=0) {
							//checks if Money is enough to transfer
							String response = messageDate+"~confirmed";
							
							MqttMessage messageVerification = new MqttMessage(response.getBytes());
							messageVerification. setQos(1);                  
							messageVerification. setRetained(true); 
							
							Client.publish(topicName+"/response/"+topicUser, messageVerification);
							
							//new TransferData
							objList newTransList = new objList();
							newTransList.setDateTime(messageDate);
							newTransList.setAccount(messageSender);
							newTransList.setRecipient(messageReceiver);
							newTransList.setAmount(messageAmountRaw);
							
							data.transList.add(newTransList);

							//Change Sender Money
							int senderMoney = data.accMoney.indexOf(accountSender);
							long newSenderMoney = data.accMoney.get(senderMoney).Money - messageAmount;
							data.accMoney.get(senderMoney).setMoney(newSenderMoney);
							
							//Change Receiver Money
							int receiverMoney = data.accMoney.indexOf(accountReceiver);
							long newReceiverMoney = data.accMoney.get(receiverMoney).Money - messageAmount;
							data.accMoney.get(receiverMoney).setMoney(newReceiverMoney);							
							
							
						}else {
							//failure
							String response = messageDate+"~failed";
							
							MqttMessage messageVerification = new MqttMessage(response.getBytes());
							messageVerification. setQos(1);                  
							messageVerification. setRetained(true); 
							
							Client.publish(topicName+"/response/"+topicUser, messageVerification);
						}
					}else {
						//failure
						String response = messageDate+"~failed";
						
						MqttMessage messageVerification = new MqttMessage(response.getBytes());
						messageVerification. setQos(1);                  
						messageVerification. setRetained(true); 
						
						Client.publish(topicName+"/response/"+topicUser, messageVerification);
					}

				}else {
					//failure
					String response = messageDate+"~failed";
					
					MqttMessage messageVerification = new MqttMessage(response.getBytes());
					messageVerification. setQos(1);                  
					messageVerification. setRetained(true); 
					
					Client.publish(topicName+"/response/"+topicUser, messageVerification);
				}

			}
		}
		
		//Verification Response
		if (topicName.compareTo("verification") == 0) {
			if (topicType.compareTo("request") == 0) {
				
				//search for the clientID
				Predicate<Detail> username = p -> p.Account.equals(topicUser);
				List<Detail> accountName = data.accDetail.stream().filter(p -> p.Account.equals(topicUser))
						.collect(Collectors.toList());
				
				//Split The message
				String[] message = Message.toString().split("~");
				String messageDate = message[0];
				String messageUsername = message[1];
				String messagePassword = message[2];

				if (!accountName.isEmpty()) {
					if(accountName.get(0).getPassword().compareTo(messagePassword)==0) {
						
						//Send Date and Message
						String response = messageDate+"~confirmed";
						
						MqttMessage messageVerification = new MqttMessage(response.getBytes());
						messageVerification. setQos(1);                  
						messageVerification. setRetained(true); 
						
						Client.publish(topicName+"/response/"+topicUser, messageVerification); 
						
					}else {
						//Send Date and Message						
						String response = messageDate+"~failed";
						
						MqttMessage messageVerification = new MqttMessage(response.getBytes());
						messageVerification. setQos(1);                  
						messageVerification. setRetained(true); 
						
						Client.publish(topicName+"/response/"+topicUser, messageVerification); 
						
					}


				} else{
					//Send Date and Message					
					String response = messageDate+"~failed";
					
					MqttMessage messageVerification = new MqttMessage(response.getBytes());
					messageVerification. setQos(1);                  
					messageVerification. setRetained(true); 
					
					Client.publish(topicName+"/response/"+topicUser, messageVerification); 
				}
			}
		}

	}


}
