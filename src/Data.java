/**
 * @author atn010
 *
 */
import java.util.ArrayList;

public class Data {
	private static Data instance;

	public static Data getInstance() {
		if (instance == null) {
			instance = new Data();
		}
		return instance;
	}

	ArrayList<Detail> accDetail = new ArrayList<Detail>();
	ArrayList<Money> accMoney = new ArrayList<Money>();
	ArrayList<objList> transList = new ArrayList<objList>();

	public Data() {
		ArrayList<objList> transList;
		ArrayList<Detail> accDetail;
		ArrayList<Money> accMoney;
	}

}
