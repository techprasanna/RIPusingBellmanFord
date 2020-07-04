import java.io.*;
import java.net.*;
import java.util.*;

public class Sender extends Thread {
	public int port;
	public String multicastIp;
	public String node_id;
	public static boolean first_Time = true;
	public DatagramSocket socket; 
	Main m = new Main();
	RIP_PacketCreater rpc;
	//public static int counter = 0;
	
	public Sender(int port, String multicastIp, String node_id) {
		this.port = port;
		this.multicastIp = multicastIp;
		this.node_id = node_id;
		Main m = new Main();
		m.update_RoutingTable(m.node_ip, m.myIp+" "+"0"+" "+System.currentTimeMillis()/1000);
	}
	
	public void sendPacket() throws IOException {
		
			socket = new DatagramSocket();
			InetAddress multicastIP = InetAddress.getByName(multicastIp);
			ArrayList<String> alist = new ArrayList<String>(25);
			
			//Formed everytime so that updated HashMap is there.
			
			HashMap<String,String> check = new HashMap<>();			
			check = m.getRoutingTable();
			for(String key:check.keySet()) {
				String val=check.get(key);	
					alist.add(key+" "+val);
			}
			// RIP packet in the form of an object of RIP_PacketCreater class
			
			rpc = new RIP_PacketCreater(1, 2, 0, alist);
			
			//Object Serialization
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(rpc);
			
			byte[] data = outputStream.toByteArray();
			DatagramPacket packet = new DatagramPacket(data,data.length,multicastIP,port);
			socket.send(packet);
			
			//Print Routing table everytime.
			System.out.println("-------------------------------------------------------------");
			for(String key: check.keySet()) {
				String value = check.get(key);
				String[] output = value.split(" ");
				String out = output[0] +"\t" +output[1];
				System.out.println(key +"\t"+ out);
			}
			System.out.println("-------------------------------------------------------------");
			
			
	}
	
	public void run() {
		while(true) {
			try {
				sendPacket();
				Thread.sleep(5000);    //Change this to 5000 while submitting,
										//This is for sending normal update 
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}
	
	

}
