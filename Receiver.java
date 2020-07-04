import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

/*
 * Receiver.java
 * 
 * Author: Prasanna Mahesh Bhope
 */
public class Receiver extends Thread {
	
	public int port;
	public String MulticastAddress;
	public RIP_PacketCreater rpc;
	public Process_Request pr;
	Main m = new Main();
	HashMap<String,String> localRoutingTable;
	
// Constructor assigning the port number and multicast Address.	
	
	public Receiver(int port, String multicastaddr) {
		
		this.port = port;
		this.MulticastAddress = multicastaddr;
	}
	
// The function triggers  update when there is any change in the routing table. The RPC
// is sent on the multicast to every node.
	
	public void trigger_Update(HashMap<String,String> routingMap) throws IOException {
		ArrayList<String> alist = new ArrayList<>();
		for(String key:routingMap.keySet()) {
			String val=routingMap.get(key);			
				alist.add(key+" "+val);
	}
	rpc = new RIP_PacketCreater(2, 2, 0, alist);
	DatagramSocket socket = new DatagramSocket();
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	ObjectOutputStream os = new ObjectOutputStream(outputStream);
	os.writeObject(rpc);
	InetAddress multicastIP = InetAddress.getByName(MulticastAddress);
	byte[] data = outputStream.toByteArray();
	DatagramPacket packet = new DatagramPacket(data,data.length,multicastIP,port);
	socket.send(packet);
	socket.close();
	
}
	
/*
 * Check the timer of every node present in the routing table. If the node does not
 * respond within 10 seconds, the metric of the node is set to 16 and hence it is 
 * removed from the table. 
 */
	

	public void check_timer(HashMap<String,String> routingMap) throws IOException {
		for(String key: routingMap.keySet()) {
			if(key == Main.node_ip) {
				continue;
			}
			String[] value = routingMap.get(key).split(" ");
			if(System.currentTimeMillis()/1000 - Integer.parseInt(value[2].trim()) > 10) {
				System.out.println("Timeout has occured in :" +key);
				m.change_RoutingTable(key, value[0] +" "+Integer.parseInt("16") + " " +0);
//				Main.delete_RoutingTableEntry(key);
				trigger_Update(m.getRoutingTable());
			}
		}
		
	}
	
	public void process_request(ArrayList<String> alist,String packet_ip) throws IOException {
		
		//Check if the response is from the same device Ip, if yes, ignore
		if(alist.size() == 0) {
			System.out.println("Empty entry; ignore");
			return;
		}
		else {
			boolean flag = false;
			//If receiver receives anything from self address, ignore
			localRoutingTable = m.getRoutingTable();
			for(int index = 0; index < alist.size(); index++) {
				String rteEntry = alist.get(index);
				String[] splitted = rteEntry.split("\\s");
				int metric = Integer.parseInt(splitted[2].trim());
				String destination = splitted[0].trim();
				String next_hop = splitted[1].trim();
				if(metric > 15) {
					m.delete_RoutingTableEntry(destination);
					trigger_Update(m.getRoutingTable());
				}
				if(destination.equals(Main.node_ip.trim())) {
					//System.out.println("Same Node");
					m.change_RoutingTable(destination, next_hop +" "+0 +" " +System.currentTimeMillis()/1000);
					continue;
					
				}
				
				else if(metric < 16) {
					
					metric = Math.min(metric + 1, 16);
					
					if(localRoutingTable.containsKey(destination)) {
//						metric = Math.min(Integer.parseInt(splitted[2].trim())+1,Integer.parseInt(splitted[2].trim()));
						String val = localRoutingTable.get(destination);
						String[] new_splitted = val.split("\\s");
						String hop_address_of_destination = new_splitted[0].trim();
						int metric_table = Integer.parseInt(new_splitted[1].trim());
						if(metric_table == 16)
							m.delete_RoutingTableEntry(destination);
						if(packet_ip.trim().equalsIgnoreCase(hop_address_of_destination)) {
							if(metric == metric_table) {
							m.change_RoutingTable(destination, hop_address_of_destination+ " "+metric+ " "+System.currentTimeMillis()/1000);
							}
							else if(metric < metric_table) {
								m.change_RoutingTable(destination, packet_ip+" "+metric+" "+System.currentTimeMillis()/1000);
								flag = true;
							}
						}
						else if(metric < metric_table) {
							m.change_RoutingTable(destination, packet_ip+" "+metric+" "+System.currentTimeMillis()/1000);
							flag = true;
						}
						
						
					}
						
					else {
						if(metric < 16) {
						m.update_RoutingTable(destination, packet_ip+" " +metric +" " +System.currentTimeMillis()/1000);
						flag = true;
						}
					
					}	
					
					if(metric > 15) {
					//Delete the entry from hashmap
						m.delete_RoutingTableEntry(splitted[0].trim());
						flag = true;
					}
					
	//					else
	//						
	//						m.update_RoutingTable(splitted[0].trim(), packet_ip +" "+metric);
				}
				else {
					m.delete_RoutingTableEntry(destination);
				}
				
				
			}
			if(flag = true) {
				flag = false;
				trigger_Update(m.getRoutingTable());
			}
			check_timer(m.getRoutingTable());
		}
		
		
		
		
	}
	
	public void receivePacket() throws IOException, ClassNotFoundException {
		byte[] buffer = new byte[1024];
		
		MulticastSocket socket = new MulticastSocket(port);
		InetAddress group = InetAddress.getByName(MulticastAddress);
		socket.joinGroup(group);
		
		//keep listening and whenever the packet comes, check the command in the packet and send to respective classes.
		while(true) {
			DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
			
			socket.receive(packet);
			
			byte[] data = packet.getData();
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream is = new ObjectInputStream(in);
			
			RIP_PacketCreater rpc2 = (RIP_PacketCreater) is.readObject();
			InetAddress ip = packet.getAddress();
			String packet_ip = ip.getHostAddress();
			//System.out.println(packet_ip);
				process_request(rpc2.RIP_entry,packet_ip);
					
				if(rpc2.command == 99) {
					break;
				}
				
			}
			socket.close();
			
		}
	
	public void run() {
		try {
			receivePacket();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	

}
