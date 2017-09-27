import java.util.ArrayList;

/**
 * 
 */

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
		return (DateTime + "~" + Account + "~" + Recipient + "~" + Amount);
	}
}

public class Data {
	private static Data instance;

	public Data() {
		ArrayList<objList> transList;
		ArrayList<Detail> accDetail;
		ArrayList<Money> accMoney;
	}

	ArrayList<objList> transList = new ArrayList<objList>();
	ArrayList<Detail> accDetail = new ArrayList<Detail>();
	ArrayList<Money> accMoney = new ArrayList<Money>();

	public static Data getInstance() {
		if (instance == null) {
			instance = new Data();
		}
		return instance;
	}

}
