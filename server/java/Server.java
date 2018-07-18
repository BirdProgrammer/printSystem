

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class Server {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Server();
	}
	private LocalDevice localDevice;
    private StreamConnectionNotifier streamConnectionNotifier = null;  
    private final static UUID uuid=new UUID("0000110100001000800000805F9B34FB",false);
    private ServiceRecord serviceRecord;
	public Server() {
		try {
			localDevice=LocalDevice.getLocalDevice();
			if(!localDevice.setDiscoverable(DiscoveryAgent.GIAC)) {
				return;
			};
			System.out.println(uuid.toString());
			streamConnectionNotifier=(StreamConnectionNotifier) Connector.open(getConnectionStr());
			serviceRecord=localDevice.getRecord(streamConnectionNotifier);
			startService();
			stopService();
		}catch(Exception e) {
			e.printStackTrace();
		}
		//startService();
	}
	private String getConnectionStr(){
        StringBuffer sb=new StringBuffer("btspp://");
        sb.append("localhost").append(":");
        sb.append(uuid);
        //sb.append(";authorize=false");
        return sb.toString();
    }    
	private void startService() {
		StreamConnection streamConnection=null;
		while(true) {
			try {
				System.out.println("waiting for connection...");
				streamConnection=streamConnectionNotifier.acceptAndOpen();
				RemoteDevice dev = RemoteDevice.getRemoteDevice(streamConnection);
				System.out.println("Remote device address: "
						+ dev.getBluetoothAddress());
				System.out.println("Remote device name: " + dev.getFriendlyName(true));
				new ServerThread(streamConnection).start();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void stopService() {
		try {
			streamConnectionNotifier.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
