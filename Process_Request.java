import java.util.*;
public class Process_Request extends Thread {
	
	public ArrayList<String> rteList;
	
	public Process_Request(ArrayList<String> alist) {
		this.rteList = alist;
	}
	
	public void scan_request(ArrayList<String> alist) {
		System.out.println("Hello");
	}

	public void run() {
		scan_request(rteList);
	}
}
