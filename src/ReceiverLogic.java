

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ReceiverLogic implements MqttCallback {
	Data data = Data.getInstance();
	SenderLogic sender = SenderLogic.getInstance();
	
	// initiator
	public ReceiverLogic() {
		sender.Client.setCallback(this);
	}

	

	@Override
	public void connectionLost(Throwable arg0) {
		
	}
	

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		
	}

	@Override
	public void messageArrived(String Topic, MqttMessage Message) throws Exception {
		TimeUnit.MILLISECONDS.sleep(10000);


		//Split the Topic
		String[] topic = Topic.split("/");
		String topicName = topic[0];
		String topicType = topic[1];
		String topicUser = topic[2];
		

		//Transaction Topic
		if (topicName.compareTo("transaction") == 0) {
			if (topicType.compareTo("request") == 0) {
				processTransactionRequest(topicName, topicUser); 
			}
		}
		
		//Transfer Request
		if (topicName.compareTo("transfer") == 0) {
			if (topicType.compareTo("request") == 0) {
				processTransferRequest(Message, topicName, topicUser);
			}
		}
		
		//Verification Response
		if (topicName.equals("verification")) {
			if (topicType.equals("request")) {
				processVerificationRequest(Message, topicName, topicUser);
			}
		}

	}
	
	private void addNewTransactionInformation(String messageDate, String messageSender, String messageReceiver,
			String messageAmountRaw) {
		objList newTransList = new objList();
		newTransList.setDateTime(messageDate);
		newTransList.setAccount(messageSender);
		newTransList.setRecipient(messageReceiver);
		newTransList.setAmount(messageAmountRaw);
		
		data.transList.add(newTransList);
	}

	private void processTransactionRequest(String topicName, String topicUser) {
		List<objList> Result1 = data.transList.stream().filter(p -> p.AccountName.equals(topicUser))
				.collect(Collectors.toList());
		List<objList> Result2 = data.transList.stream().filter(p -> p.Recipient.equals(topicUser))
				.collect(Collectors.toList());
		
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
		
		sender.sendMessage(topicName+"/list/"+topicUser, Result1.toString()); 
		
		//Get User money
		List<Money> accountMoney = data.accMoney.stream().filter(p -> p.AccountName.equals(topicUser)).collect(Collectors.toList());
		
		//Send the info to User
		
		
		sender.sendMessage(topicName+"/money/"+topicUser, accountMoney.get(0).getMoney().toString());
	}

	private void processTransferRequest(MqttMessage Message, String topicName, String topicUser) {
		//decode the message
		String[] message = Message.toString().split("~");
		String messageDate = message[0];
		String messageSender = message[1];
		String messageReceiver = message[2];
		String messageAmountRaw = message[3];
		Long messageAmount = Long.valueOf(messageAmountRaw).longValue();
		
		//check Sender name
		List<Money> accountSender = data.accMoney.stream().filter(p -> p.AccountName.equals(messageSender))
				.collect(Collectors.toList());
		
		//check Receiver Name
		List<Money> accountReceiver = data.accMoney.stream().filter(p -> p.AccountName.equals(messageReceiver))
				.collect(Collectors.toList());
			

		//if Sender is not false
		if (!accountSender.isEmpty()) {
			//if Receiver is not false
			if(!accountReceiver.isEmpty()) {
				//if Money is available
				if((accountSender.get(0).getMoney() - messageAmount) >=0) {
					//checks if Money is enough to transfer
					String response = messageDate+"~confirmed";
					
					sender.sendMessage(topicName+"/response/"+topicUser, response);
					
					//new TransferData
					addNewTransactionInformation(messageDate, messageSender, messageReceiver, messageAmountRaw);
					
					//find and replace data
					updateAccountMoney(messageSender, messageReceiver, messageAmount);						
					
					
				}else {
					//failure
					String response = messageDate+"~failed";
					sender.sendMessage(topicName+"/response/"+topicUser, response);
				}
			}else {
				//failure
				String response = messageDate+"~failed";	
				sender.sendMessage(topicName+"/response/"+topicUser, response);
			}

		}else {
			//failure
			String response = messageDate+"~failed";
			sender.sendMessage(topicName+"/response/"+topicUser, response);
		}
	}

	private void processVerificationRequest(MqttMessage Message, String topicName, String topicUser) {
		//search for the clientID
		//Predicate<Detail> username = p -> p.Account.equals(topicUser);
		List<Detail> accountName = data.accDetail.stream().filter(p -> p.AccountName.equals(topicUser))
				.collect(Collectors.toList());
		
		//Split The message
		String[] message = Message.toString().split("~");
		String messageDate = message[0];
		String messageUsername = message[1];
		String messagePassword = message[2];


		if (!accountName.isEmpty()) {
			if(accountName.get(0).Password.equals(messagePassword)) {
				
				//Send Date and Message
				String response = messageDate+"~confirmed";
				
				sender.sendMessage(topicName+"/response/"+topicUser, response); 
				
			}else {
				//Send Date and Message						
				String response = messageDate+"~failed";
				System.out.println("Failed");

				sender.sendMessage(topicName+"/response/"+topicUser, response); 
				
			}


		} else{
			//Send Date and Message					
			String response = messageDate+"~failed";
		
			
			sender.sendMessage(topicName+"/response/"+topicUser, response); 
		}
	}

	private void updateAccountMoney(String messageSender, String messageReceiver, Long messageAmount) {
		int loopBreaker = 0;
		for(int i=0;i < data.accMoney.size();i++) {
			if(data.accMoney.get(i).AccountName.equals(messageSender)) {
				System.out.println("Begin Change on Sender");
				long newSenderMoney = data.accMoney.get(i).Money - messageAmount;
				System.out.println("New Sender Money: "+newSenderMoney);
				data.accMoney.get(i).setMoney(newSenderMoney);
				System.out.println("Now Sender Money: "+data.accMoney.get(i).Money);
				loopBreaker++;
			}
			if(data.accMoney.get(i).AccountName.equals(messageReceiver)) {
				System.out.println("Begin Change on Receiver");
				long newReceiverMoney = data.accMoney.get(i).Money + messageAmount;
				System.out.println("New Receiver Money: "+newReceiverMoney);
				data.accMoney.get(i).setMoney(newReceiverMoney);
				System.out.println("Now Receiver Money: "+data.accMoney.get(i).Money);
				loopBreaker++;
				
			}
			if(loopBreaker >=2) {
				System.out.println("The End");
				break;
			}
		}
	}


}
