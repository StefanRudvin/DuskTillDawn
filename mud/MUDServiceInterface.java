package mud;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MUDServiceInterface extends Remote {

	/*
		Class modified from practicals.package practicals.rmishout.ShoutServiceInterface
	 */

	public String welcome() throws RemoteException;

	public String getStartLocation() throws RemoteException;

	public String getLocationInfo(String location) throws RemoteException;

	public void decrementPlayerTimeOut() throws RemoteException;

	public String refreshPlayerTimeOut(String username) throws RemoteException;

	public String moveDirection(String current, String direction, String username) throws RemoteException;

	public boolean initializePlayer(String username, String serverName) throws RemoteException;

	public String getObjectsAtLocation(String location) throws RemoteException;

	public void takeItem(String item, String location) throws RemoteException;

	public void dropItem(String item, String location) throws RemoteException;

	public String changeMUD(String name) throws RemoteException;

	public void exitMUD(String username, String location) throws RemoteException;

	public String getMudsString() throws RemoteException;

	public boolean createMUD(String name) throws RemoteException;

	public String getMUDTotal() throws RemoteException;

	public String getPlayerTotal() throws RemoteException;

	public String getMUDPlayerTotal() throws RemoteException;

}
