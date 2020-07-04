import java.io.Serializable;
import java.util.ArrayList;


/*
 * Packet to be sent on multicast with values of command,
 * verion which is 2 in this case, and the RTE entries
 * 
 * Author: Prasanna Mahesh Bhope
 */
public class RIP_PacketCreater implements Serializable {
	
	public int command;
	public int version;
	public int must_be_zero;
	ArrayList<String> RIP_entry;
	
	public RIP_PacketCreater(int command, int version, int must_be_zero, ArrayList<String> RIP_Entries) {
		this.command = command;
		this.version = version;
		this.must_be_zero = must_be_zero;
		this.RIP_entry = RIP_Entries;
	}
	
	public String toString() {
		return command +" " +version +" " +must_be_zero +" " +RIP_entry;
	}

}
