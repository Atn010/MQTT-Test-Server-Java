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



public class Main{
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Data data = Data.getInstance();
		ConnectionLogic connLogic = new ConnectionLogic();
		
		
		int Choice=0;
		Scanner sc = new Scanner(System.in);
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
				for(int i =0; i<data.transList.size();i++){
					System.out.print(i+1);
					System.out.print(" | "+data.transList.get(i).DateTime);
					System.out.print(" | "+data.transList.get(i).Account);
					System.out.print(" | "+data.transList.get(i).Recipient);
					System.out.println(" | "+data.transList.get(i).Amount);					
				}
				sc.nextLine();
				System.out.println();
				System.out.println();
				System.out.println(data.transList);
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
				for(int i =0; i<data.accDetail.size();i++){
					System.out.print(i+1);
					System.out.print(" | "+data.accDetail.get(i).Account);
					System.out.println(" | "+data.accDetail.get(i).Password);					
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
				for(int i =0; i<data.accMoney.size();i++){
					System.out.print(i+1);
					System.out.print(" | "+data.accMoney.get(i).Account);
					System.out.println(" | "+data.accMoney.get(i).Money);					
				}
				sc.nextLine();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
			}
			
		}while(Choice != 4); 
	}



	

}


