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

/**
 * 
 * @author Antonius George Sunggeriwan <atn010g@gmail.com>
 *
 * This class is responsible for receiving Message from the Clients thru the Broker.
 * The message is retrieve in messageArrived method and redirected to the an appropriate method for the type of message from different topic.
 * 
 */
public class ReceiverLogic implements MqttCallback {
	Data data = Data.getInstance();
	SenderLogic sender = SenderLogic.getInstance();

	public ReceiverLogic() {
		sender.Client.setCallback(this);
	}

	@Override
	public void connectionLost(Throwable arg0) {

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {

	}

	/**
	 * The messageArrived method is provided by the library to receive message from the broker.
	 * The method included the corresponding Topic.
	 * that is previously subscribed to and Message.
	 * 
	 */
	public void messageArrived(String Topic, MqttMessage Message) throws Exception {
		TimeUnit.MILLISECONDS.sleep(10000); // This is to simulate Network Lag

		/// Split the Topic into parts and store it in a variable
		String[] topic = Topic.split("/");

		String topicName = topic[0];///< This variable store the name of the main topic
		String topicType = topic[1];///< This variable store the type of the Topic
		String topicUser = topic[2];///< This variable store the receiver of the

		filterAndDelegateTopic(Message, topicName, topicType, topicUser);/// < This method separates the topic and run
																			/// the appropriate method

	}

	/**
	 * This Method check if the message is a Transaction Request or a Transfer Request or, a Verification Request.
	 * After checking, The method will send the message into the appropriate function
	 * 
	 * @param Message The Message retrieve from the broker sent by the Client
	 * @param topicName The Name of the Topic
	 * @param topicType The Type of Topic
	 * @param topicUser The ClientID of the Topic
	 * 
	 */
	private void filterAndDelegateTopic(MqttMessage Message, String topicName, String topicType, String topicUser) {
		/// Transaction Topic
		if (topicName.compareTo("transaction") == 0) {
			if (topicType.compareTo("request") == 0) {
				processTransactionRequest(topicName, topicUser);/// < This method respond to the Transaction Request
			}
		}

		/// Transfer Request
		if (topicName.compareTo("transfer") == 0) {
			if (topicType.compareTo("request") == 0) {
				processTransferRequest(Message, topicName, topicUser);/// < This method respond to the Transfer Request
			}
		}

		/// Verification Response
		if (topicName.equals("verification")) {
			if (topicType.equals("request")) {
				processVerificationRequest(Message, topicName, topicUser);/// < This method respond to the Verification
																			/// Request
			}
		}
	}

	/**
	 * This Method Add the new information into the transaction list.
	 * 
	 * @param messageDate The Date of the message
	 * @param messageSender The Client that sends the transfer request
	 * @param messageReceiver The Target of the transfer request
	 * @param messageAmountRaw The amount of money transfered
	 */
	private void addNewTransactionInformation(String messageDate, String messageSender, String messageReceiver,
			String messageAmountRaw) {	
		objList newTransList = new objList();
		
		newTransList.setDateTime(messageDate);
		newTransList.setAccount(messageSender);
		newTransList.setRecipient(messageReceiver);
		newTransList.setAmount(messageAmountRaw);

		data.transList.add(newTransList);
	}

	/**
	 * Process the Transaction Request By.
	 * Get all the Requester in the Sender Field in the Transaction List into a list.
	 * Get all the Requester in the Receiver Field in the Transaction List into a list.
	 * Combine both list into one and Send the list into the Requester.
	 * And send The Amount of Money to the Requester.
	 * 
	 * @param topicName The name of the topic 
	 * @param topicUser The Client of the topic
	 */
	private void processTransactionRequest(String topicName, String topicUser) {
		
		List<objList> Result1 = data.transList.stream().filter(p -> p.AccountName.equals(topicUser))
				.collect(Collectors.toList()); ///< This method find all the Requester in the Sender list
		List<objList> Result2 = data.transList.stream().filter(p -> p.Recipient.equals(topicUser))
				.collect(Collectors.toList()); ///< This method find all the Requester in the Recipient list

		Result1.addAll(Result2);///< This combines both of the result from the Requester in Sender list and Recipient list into one List

		/**
		 * The method below sorts the Result from latest to oldest.
		 * The method parse the variable into the "day/month/year hour:minute" format and compare which are higher.
		 */
		Collections.sort(Result1, new Comparator<objList>() {
			DateFormat f = new SimpleDateFormat("dd/MM/yy hh:mm");

			@Override
			public int compare(objList o1, objList o2) {
				try {
					return f.parse(o1.getDateTime()).compareTo(f.parse(o2.getDateTime()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return 0;
			}
		});

		/// Send the resulting list into the expected response topic
		sender.sendMessage(topicName + "/list/" + topicUser, Result1.toString());

		/// Get the requester money
		List<Money> accountMoney = data.accMoney.stream().filter(p -> p.AccountName.equals(topicUser))
				.collect(Collectors.toList());

		/// Send the requester money into the expected response topic
		sender.sendMessage(topicName + "/money/" + topicUser, accountMoney.get(0).getMoney().toString());
	}

	/**
	 * This process the Transfer Request by splitting the corresponding Message parts and Convert the money into a Long Integer from String.
	 * Find both the source and target of the transfer.
	 * 
	 * Check if both source and target is available and check if the source has enough money to transfer.
	 * If all is true, sends success message, add new transaction information and update the amount of money.
	 * 
	 * @param Message The Content of the Transfer request in a format: "Date Time~Sender~Receiver~Amount".
	 * @param topicName The name of the topic.
	 * @param topicUser The Client of the topic.
	 */
	private void processTransferRequest(MqttMessage Message, String topicName, String topicUser) {
		/// Split the Topic into parts and store it in a variable
		String[] message = Message.toString().split("~");
		
		String messageDate = message[0]; ///< This store the Date of the message into a variable
		String messageSender = message[1]; ///< This store the Source into a variable
		String messageReceiver = message[2]; ///< This store the Target into a variable
		String messageAmountRaw = message[3]; ///< This store the Amount of Money to transfer into a variable
		
		Long messageAmount = Long.valueOf(messageAmountRaw).longValue(); ///< This converts Amount of Money from String to Long Integer

		/// This method Find the Source Account
		List<Money> accountSender = data.accMoney.stream().filter(p -> p.AccountName.equals(messageSender))
				.collect(Collectors.toList());

		/// This method Find the Target Account
		List<Money> accountReceiver = data.accMoney.stream().filter(p -> p.AccountName.equals(messageReceiver))
				.collect(Collectors.toList());

		if (!accountSender.isEmpty()) {
			if (!accountReceiver.isEmpty()) {
				
				/// Checks if Transfer Request amount does not exceed balance 
				if ((accountSender.get(0).getMoney() - messageAmount) >= 0) {

					sendSuccess(topicName, topicUser, messageDate);

					addNewTransactionInformation(messageDate, messageSender, messageReceiver, messageAmountRaw);

					updateAccountMoney(messageSender, messageReceiver, messageAmount);

				} else {
					sendFailure(topicName, topicUser, messageDate);
				}
			} else {
				sendFailure(topicName, topicUser, messageDate);
			}

		} else {
			sendFailure(topicName, topicUser, messageDate);
		}
	}

	/**
	 * Send a successful message to the Client thru the Broker.
	 * @param topicName The name of the topic .
	 * @param topicUser The Client of the topic.
	 * @param messageDate The Date of the Message published from the Client.
	 */
	private void sendSuccess(String topicName, String topicUser, String messageDate) {
		String response = messageDate + "~confirmed";

		sender.sendMessage(topicName + "/response/" + topicUser, response);
	}

	/**
	 * Send a failure message to the Client thru the Broker.
	 * @param topicName The name of the topic.
	 * @param topicUser The Client of the topic.
	 * @param messageDate The Date of the Message published from the Client.
	 */
	private void sendFailure(String topicName, String topicUser, String messageDate) {
		// failure
		String response = messageDate + "~failed";
		sender.sendMessage(topicName + "/response/" + topicUser, response);
	}

	/**
	 * This Method Process the Verification Request by finding the username and password of the corresponding account.
	 * Checks if it is exist in the system and send success message if it is correct, else it is false.
	 * 
	 * @param Message The Content of the Verification request in a format: "Date Time~Username~Password".
	 * @param topicName The name of the topic.
	 * @param topicUser The Client of the topic.
	 */
	private void processVerificationRequest(MqttMessage Message, String topicName, String topicUser) {
		
		/// This method Find the ClientID Account
		List<Detail> accountName = data.accDetail.stream().filter(p -> p.AccountName.equals(topicUser))
				.collect(Collectors.toList());

		/// Split the Topic into parts and store it in a variable
		String[] message = Message.toString().split("~");
		
		String messageDate = message[0]; ///< This store the Date of the message into a variable
		String messageUsername = message[1]; ///< This store the Username into a variable
		String messagePassword = message[2]; ///< This store the Password into a variable

		if (!accountName.isEmpty()) {
			if (accountName.get(0).Password.equals(messagePassword)) {

				sendSuccess(topicName, topicUser, messageDate);

			} else {
				sendFailure(topicName, topicUser, messageDate);

			}
		} else {
			sendFailure(topicName, topicUser, messageDate);
		}
	}

	/** 
	 * This Method Replaces the Money for Sender and Receiver to reflect a transfer
	 * @param messageSender The Sender of the Transfer Request
	 * @param messageReceiver The Receiver of the Transfer Request
	 * @param messageAmount The amount of money transfered
	 */
	private void updateAccountMoney(String messageSender, String messageReceiver, Long messageAmount) {
		int loopBreaker = 0;
		boolean senderAmountUpdateIsDone = false;
		boolean receiverAmountUpdateIsDone = false;
		
		
		for (int i = 0; i < data.accMoney.size(); i++) {
			if (data.accMoney.get(i).AccountName.equals(messageSender) && !senderAmountUpdateIsDone) {
				long newSenderMoney = data.accMoney.get(i).Money - messageAmount;
				data.accMoney.get(i).setMoney(newSenderMoney);
				loopBreaker++;
				senderAmountUpdateIsDone = true;
			}
			if (data.accMoney.get(i).AccountName.equals(messageReceiver) && !receiverAmountUpdateIsDone) {
				long newReceiverMoney = data.accMoney.get(i).Money + messageAmount;
				data.accMoney.get(i).setMoney(newReceiverMoney);
				loopBreaker++;
				receiverAmountUpdateIsDone = true;
			}
			if (loopBreaker >= 2) {
				break;
			}
		}
	}

}
