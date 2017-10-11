/**
 * @author Antonius George Sunggeriwan <atn010g@gmail.com>
 *
 */
class objList extends Account {

	String Amount;

	String DateTime;

	String Recipient;

	public String getAmount() {
		return Amount;
	}

	public String getDateTime() {
		return DateTime;
	}

	public String getRecipient() {
		return Recipient;
	}

	public void setAmount(String amount) {
		Amount = amount;
	}
	public void setDateTime(String dateTime) {
		DateTime = dateTime;
	}
	public void setRecipient(String recipient) {
		Recipient = recipient;
	}

	public String toString() {
		return (DateTime + "~" + AccountName + "~" + Recipient + "~" + Amount);
	}
}