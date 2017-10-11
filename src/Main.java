/**
 * @author Antonius George Sunggeriwan <atn010g@gmail.com>
 *
 */
import java.util.*;




public class Main{
	
	public static void main(String[] args) {
		Data data = Data.getInstance();
		SenderLogic sender = SenderLogic.getInstance();
		ReceiverLogic connLogic = new ReceiverLogic();
		
		
		int Choice=0;
		Scanner sc = new Scanner(System.in);
		do {
			if(data.transList.isEmpty() && data.accDetail.isEmpty() && data.accMoney.isEmpty()) {
				dataEmpty();
				}
			
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
					System.out.print(" | "+data.transList.get(i).getAccount());
					System.out.print(" | "+data.transList.get(i).Recipient);
					System.out.println(" | "+data.transList.get(i).Amount);					
				}
				sc.nextLine();
				System.out.println();
				System.out.println();
				//System.out.println(data.transList);
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
					System.out.print(" | "+data.accDetail.get(i).getAccount());
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
					System.out.print(" | "+data.accMoney.get(i).getAccount());
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
	
	public static void dataEmpty() {
		Data data = Data.getInstance();
		
		//Transfer List Populate 1
		objList newTransList1 = new objList();
		
		newTransList1.setDateTime("09/09/17 17:20");
		newTransList1.setAccount("username");
		newTransList1.setRecipient("password");
		newTransList1.setAmount("20000");
		
		data.transList.add(newTransList1);
		
		//Transfer List Populate 2
		objList newTransList2 = new objList();
		
		newTransList2.setDateTime("05/10/17 11:00");
		newTransList2.setAccount("password");
		newTransList2.setRecipient("username");
		newTransList2.setAmount("5000");
		
		data.transList.add(newTransList2);
		
		//Transfer List Populate 3
		objList newTransList3 = new objList();
		
		newTransList3.setDateTime("31/12/16 09:20");
		newTransList3.setAccount("username");
		newTransList3.setRecipient("password");
		newTransList3.setAmount("25000");
		
		data.transList.add(newTransList3);
		
		//Transfer List Populate 4
		objList newTransList4 = new objList();
		
		newTransList4.setDateTime("31/10/16 09:20");
		newTransList4.setAccount("admin");
		newTransList4.setRecipient("password");
		newTransList4.setAmount("2500");
		
		data.transList.add(newTransList4);
		
		//Transfer List Populate 5
		objList newTransList5 = new objList();
		
		newTransList5.setDateTime("31/11/16 09:02");
		newTransList5.setAccount("admin");
		newTransList5.setRecipient("username");
		newTransList5.setAmount("25000");
		
		data.transList.add(newTransList5);
		
		//Account Detail Populate 1
		Detail newAccDetail1 = new Detail();
		
		newAccDetail1.setAccount("username");
		newAccDetail1.setPassword("password");
		
		data.accDetail.add(newAccDetail1);
		
		//Account Detail Populate 2
		Detail newAccDetail2 = new Detail();
		
		newAccDetail2.setAccount("password");
		newAccDetail2.setPassword("username");
		
		data.accDetail.add(newAccDetail2);
		
		//Account Detail Populate 3
		Detail newAccDetail3 = new Detail();
		
		newAccDetail3.setAccount("admin");
		newAccDetail3.setPassword("admin");
		
		data.accDetail.add(newAccDetail3);
		
		//Account Money Populate 1
		Money newAccMoney1 = new Money();
		
		newAccMoney1.setAccount("username");
		newAccMoney1.setMoney((long) 50000);
		
		data.accMoney.add(newAccMoney1);
		
		//Account Money Populate 2
		Money newAccMoney2 = new Money();
		
		newAccMoney2.setAccount("password");
		newAccMoney2.setMoney((long) 25000);
		
		data.accMoney.add(newAccMoney2);
		
		//Account Money Populate 3
		Money newAccMoney3 = new Money();
		
		newAccMoney3.setAccount("admin");
		newAccMoney3.setMoney((long) 25000000);
		
		data.accMoney.add(newAccMoney3);

	
	}
}


