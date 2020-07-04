import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/*
* This program simulates a distance vector routing protocol using
* RIPv2. This class simulates the Request and Response classes where a node
* sends request to other nodes in multicasting and accordingily calculates the 
* routing table.
*
* @author     Prasanna Mahesh Bhope
*/


/*
* The main program.
*
* @param    args    command line arguments.
*/

public class Main {
	
	//Routing table in the form of data structure.
	
	private static HashMap<String,String> routingTable = new HashMap<>();
	public static String node_ip;
	
	public static String myIp;
	private static Object o = new Object();
	
	//Getter setter methods for Routing table
	
	public synchronized void update_RoutingTable(String key, String value) {
		routingTable.put(key, value);
	}
	
	public synchronized void delete_RoutingTableEntry(String key) {
		routingTable.remove(key);
		
	}
	
	public synchronized String read_RoutingTable(String key) {
		if(routingTable.containsKey(key)) {
			return key+" "+routingTable.get(key);
		}
		else
			return "NULL";
		
	}
	public HashMap<String,String> getRoutingTable(){
		
		return routingTable;
	}
	
	public ArrayList<String> getRTEntries(){
		ArrayList<String> alist = new ArrayList<>();
		alist.addAll(routingTable.values());
		return alist;
	}
	
	public void change_RoutingTable(String key,String value) {
		routingTable.replace(key, value);
	}
	
	public static void main(String[] args) throws UnknownHostException {
		if (args.length > 0){
			InetAddress localhost = InetAddress.getLocalHost();
			myIp = localhost.getHostAddress();
			String Node_id = args[0];
			int port = 63001;
			 node_ip = "10.0."+args[0]+".0/24";
			
			System.out.println("Starting Node:"+Node_id);
			
			System.out.println("Starting receiver on multicast");
			Thread receiver = new Thread(new Receiver(port, "224.0.0.9"));
			receiver.start();
			
			
			System.out.println("Address \t Next Hop \t Cost \t \n");
			Thread sender = new Thread(new Sender(port,"224.0.0.9",node_ip));
			sender.start();
			
			while(true) {
				try {
					Thread.sleep(1000);		//To ensure that the above thread starts
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
					
			
		}
		else {
			System.err.println("Wrong command line arguments");
			System.exit(0);
		}
		
		
	}

}
