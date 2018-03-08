package mud;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MUDServiceInterface extends Remote {

	/*
		Class modified from practicals.package practicals.rmishout.ShoutServiceInterface
	 */

	String welcome() throws RemoteException;

	boolean initializePlayer(String username, String serverName) throws RemoteException;

	String getStartLocation() throws RemoteException;

	String getLocationInfo(String location) throws RemoteException;


	void decrementPlayerTimeOut() throws RemoteException;

	String refreshPlayerTimeOut(String username) throws RemoteException;


	String moveDirection(String current, String direction, String username) throws RemoteException;

	String getObjectsAtLocation(String location) throws RemoteException;

	void takeItem(String item, String location) throws RemoteException;

	void dropItem(String item, String location) throws RemoteException;


	boolean createMUD(String name) throws RemoteException;

	String changeMUD(String name, String playerName) throws RemoteException;

	void exitMUD(String username, String location) throws RemoteException;

	String getMudsString() throws RemoteException;


	String getMUDTotal() throws RemoteException;

	String getPlayerTotal() throws RemoteException;

	String getMUDPlayerTotal() throws RemoteException;

	Integer setMaxPlayersPerMud(int maxPlayersPerMud) throws RemoteException;

	Integer setMaxTotalPlayers(int maxTotalPlayers) throws RemoteException;

	Integer setMaxNumOfMuds(int maxNumOfMuds) throws RemoteException;

}
