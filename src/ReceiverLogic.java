
/**
 * @author Antonius George Sunggeriwan <atn010g@gmail.com>
 *
 */

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

	public ReceiverLogic() {
		sender.Client.setCallback(this);
	}

	@Override
	public void connectionLost(Throwable arg0) {

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {

	}

	/***
	 * @Override The messageArrived method is provided by the library to receive
	 *           message from the broker The method included the corresponding Topic
	 *           that is previously subcribed to and Message
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

	/// This method adds the new transaction information into the list
	private void addNewTransactionInformation(String messageDate, String messageSender, String messageReceiver,
			String messageAmountRaw) {	
		objList newTransList = new objList();
		
		newTransList.setDateTime(messageDate);
		newTransList.setAccount(messageSender);
		newTransList.setRecipient(messageReceiver);
		newTransList.setAmount(messageAmountRaw);

		data.transList.add(newTransList);
	}

	/// this method process the Transaction Request
	private void processTransactionRequest(String topicName, String topicUser) {
		
		List<objList> Result1 = data.transList.stream().filter(p -> p.AccountName.equals(topicUser))
				.collect(Collectors.toList()); ///< This method find all the Requester in the Sender list
		List<objList> Result2 = data.transList.stream().filter(p -> p.Recipient.equals(topicUser))
				.collect(Collectors.toList()); ///< This method find all the Requester in the Recipient list

		Result1.addAll(Result2);///< This combines both of the result from the Requester in Sender list and Recipient list into one List

		/***
		 * The method below sorts the Result from latest to oldest
		 * 
		 * The method parse the variable into the "day/month/year hour:minute" format and compare which are higher
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

	private void processTransferRequest(MqttMessage Message, String topicName, String topicUser) {
		/// Split the Topic into parts and store it in a variable
		String[] message = Message.toString().split("~");
		
		String messageDate = message[0]; ///< This store the Date of the message into a variable
		String messageSender = message[1]; ///< This store the Source into a variable
		String messageReceiver = message[2]; ///< This store the Target into a variable
		String messageAmountRaw = message[3]; ///< This store the Amount of Money to transfer into a variable
		
		Long messageAmount = Long.valueOf(messageAmountRaw).longValue(); ///< This converts Amount of Money from String to Long Integer

		/// This method checks the
		List<Money> accountSender = data.accMoney.stream().filter(p -> p.AccountName.equals(messageSender))
				.collect(Collectors.toList());

		// check Receiver Name
		List<Money> accountReceiver = data.accMoney.stream().filter(p -> p.AccountName.equals(messageReceiver))
				.collect(Collectors.toList());

		// if Sender is not false
		if (!accountSender.isEmpty()) {
			// if Receiver is not false
			if (!accountReceiver.isEmpty()) {
				// if Money is available
				if ((accountSender.get(0).getMoney() - messageAmount) >= 0) {
					// checks if Money is enough to transfer
					String response = messageDate + "~confirmed";

					sender.sendMessage(topicName + "/response/" + topicUser, response);

					// new TransferData
					addNewTransactionInformation(messageDate, messageSender, messageReceiver, messageAmountRaw);

					// find and replace data
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

	private void sendFailure(String topicName, String topicUser, String messageDate) {
		// failure
		String response = messageDate + "~failed";
		sender.sendMessage(topicName + "/response/" + topicUser, response);
	}

	private void processVerificationRequest(MqttMessage Message, String topicName, String topicUser) {
		// search for the clientID
		// Predicate<Detail> username = p -> p.Account.equals(topicUser);
		List<Detail> accountName = data.accDetail.stream().filter(p -> p.AccountName.equals(topicUser))
				.collect(Collectors.toList());

		// Split The message
		String[] message = Message.toString().split("~");
		String messageDate = message[0];
		String messageUsername = message[1];
		String messagePassword = message[2];

		if (!accountName.isEmpty()) {
			if (accountName.get(0).Password.equals(messagePassword)) {

				// Send Date and Message
				String response = messageDate + "~confirmed";

				sender.sendMessage(topicName + "/response/" + topicUser, response);

			} else {
				// Send Date and Message
				sendFailure(topicName, topicUser, messageDate);

			}

		} else {
			sendFailure(topicName, topicUser, messageDate);
		}
	}

	private void updateAccountMoney(String messageSender, String messageReceiver, Long messageAmount) {
		int loopBreaker = 0;
		for (int i = 0; i < data.accMoney.size(); i++) {
			if (data.accMoney.get(i).AccountName.equals(messageSender)) {
				long newSenderMoney = data.accMoney.get(i).Money - messageAmount;
				data.accMoney.get(i).setMoney(newSenderMoney);
				loopBreaker++;
			}
			if (data.accMoney.get(i).AccountName.equals(messageReceiver)) {
				long newReceiverMoney = data.accMoney.get(i).Money + messageAmount;
				data.accMoney.get(i).setMoney(newReceiverMoney);
				loopBreaker++;

			}
			if (loopBreaker >= 2) {
				break;
			}
		}
	}

}
