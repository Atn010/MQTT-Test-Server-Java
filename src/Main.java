/**
 * 
 */
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.*;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
/**
 * @author atn01
 *
 */



class Account {
	String Account;

	public String getAccount() {
		return Account;
	}

	public void setAccount(String account) {
		Account = account;
	}

}

class Detail extends Account {
	String Password;

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}
}

class Money extends Account {
	Long Money;

	public Long getMoney() {
		return Money;
	}

	public void setMoney(Long money) {
		Money = money;
	}
}

class objList extends Account {

	public String getDateTime() {
		return DateTime;
	}
	public void setDateTime(String dateTime) {
		DateTime = dateTime;
	}
	public String getRecipient() {
		return Recipient;
	}
	public void setRecipient(String recipient) {
		Recipient = recipient;
	}
	public String getAmount() {
		return Amount;
	}
	public void setAmount(String amount) {
		Amount = amount;
	}
	
	String DateTime;
	String Recipient;
	String Amount;
	
	public String toString() {
		return (DateTime+" - "+Account+" - "+Recipient+" - "+Amount+"|");
	}
}



public class Main implements MqttCallback {
	
	static ArrayList<objList> transList = new ArrayList<objList>();
	static ArrayList<Detail> accDetail = new ArrayList<Detail>();
	static ArrayList<Money> accMoney  = new ArrayList<Money>();
	


	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//initiator
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
		
		

		Scanner sc = new Scanner(System.in);


        connOpts.setCleanSession(false);
        connOpts.isAutomaticReconnect();
        try {
			Client.connect(connOpts);
			Client.subscribe("Transaction/#");
	        Client.subscribe("Transfer/#");
	        Client.subscribe("Verification/#");
		} catch (MqttSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
		
		int Choice=0;
		do {
			
			System.out.println("INFO XYZ SERVER");
			System.out.println("*****************");
			System.out.println("This is an automatic server application");
			System.out.println("Most of the function are automated");
			System.out.println();
			System.out.println("1. Show all Transaction");
			System.out.println("2. Show all Account information");
			System.out.println("3. Show all Account and Money");
			System.out.println("4. Exit");
			System.out.println();
			System.out.print("Select Choice: ");
			Choice = sc.nextInt();
			sc.nextLine();
			
			if(Choice == 1) {
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println("Transaction List");
				System.out.println("*************************************************");				
				for(int i =0; i<transList.size();i++){
					System.out.print(i+1);
					System.out.print(" | "+transList.get(i).DateTime);
					System.out.print(" | "+transList.get(i).Account);
					System.out.print(" | "+transList.get(i).Recipient);
					System.out.println(" | "+transList.get(i).Amount);					
				}
				sc.nextLine();
				System.out.println();
				System.out.println();
				System.out.println(transList);
				System.out.println();
				System.out.println();
			}
			if(Choice == 2) {
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println("Account Detail");
				System.out.println("*************************************************");				
				for(int i =0; i<transList.size();i++){
					System.out.print(i+1);
					System.out.print(" | "+accDetail.get(i).Account);
					System.out.println(" | "+accDetail.get(i).Password);					
				}
				sc.nextLine();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
			}
			if(Choice == 3) {
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println("Account Money");
				System.out.println("*************************************************");				
				for(int i =0; i<transList.size();i++){
					System.out.print(i+1);
					System.out.print(" | "+accMoney.get(i).Account);
					System.out.println(" | "+accMoney.get(i).Money);					
				}
				sc.nextLine();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
			}
			
		}while(Choice != 4); 
	}

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String Topic, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
		
		String []topic = Topic.split("/");
		String topicName = topic[0];
		String topicType = topic[1];
		String topicUser = topic[2];
		
		if (topicName.compareTo("transaction") == 0) {
			if (topicType.compareTo("request") == 0) {
				Predicate<objList> bySender = p -> p.Account.compareTo(topicUser) == 0;
				Predicate<objList> byReceiver = p -> p.Recipient.compareTo(topicUser) == 0;
				
				List<objList> Result1 = FluentIterable.from(transList).filter(bySender).toList();
				List<objList> Result2 = FluentIterable.from(transList).filter(byReceiver).toList();
				
				Result1.addAll(Result2);
				
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
				
				MqttMessage messageFromServer = new MqttMessage(Result1.toString().getBytes());
			}
		}
		if (topicName.compareTo("transfer") == 0) {
			if (topicType.compareTo("request") == 0) {
				Predicate<Money> username = p -> p.Account.equals(topicUser);	
				List<Money> accountName = accMoney.stream().filter(p-> p.Account.equals(topicUser)).collect(Collectors.toList());
			
				if(accountName != null) {
					
					
				}else if(accountName == null) {
					
				}
				
			}
		}
		if (topicName.compareTo("verification") == 0) {
			if (topicType.compareTo("request") == 0) {
				Predicate<Detail> username = p -> p.Account.equals(topicUser);
				List<Detail> accountName = accDetail.stream().filter(p-> p.Account.equals(topicUser)).collect(Collectors.toList());
				
				if(accountName != null) {
					
				}else if(accountName == null) {
						
				}
			}
		}
		
		
	}

	
	public void transferProcessing(String Topic, String Message) {
		
		
		
		
		
	}
	
	public boolean transferResponse() {
		return false;
		
	}
	
	public void transactionListResponse() {
		
		
	}
	
	public boolean accountResponse() {
		return false;
		
	}
	
	public void accountAmountProcessing(String Topic, String Message) {
		
	}

	

}


