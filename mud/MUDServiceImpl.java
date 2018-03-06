package mud;

import java.rmi.*;
import java.util.*;

public class MUDServiceImpl implements MUDServiceInterface {

	/*
		Class modified from practicals.package practicals.rmishout.ShoutServiceImpl
	 */

	protected MUD MUDInstance;

	protected String MUDInstanceName;

	protected Map<String, MUD> Servers = new HashMap<String, MUD>();

	// Current users in MUD. Username : <Timeout, Location>
	protected Map<String, String[]> Users = new HashMap<String, String[]>();

	protected Integer MUDTotal;

	private static final int MAX_USERS_PER_MUD = 10;
	private static final int MAX_NUM_OF_MUDS = 5;
	private static final int MAX_TOTAL_PLAYERS = 100;

	protected String lastMessage = "";

	public MUDServiceImpl() throws RemoteException {
		Servers.put("sample", new MUD("narnia.edg", "narnia.msg", "narnia.thg"));
		Servers.put("aberdeen", new MUD("aberdeen.edg", "aberdeen.msg", "aberdeen.thg"));
		Servers.put("aberdeen2", new MUD("aberdeen.edg", "aberdeen.msg", "aberdeen.thg"));
		MUDTotal = 3;

		Timer timerObj = new Timer(true);
		timerObj.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				decrementUserTimeOut();
			}
		}, 10, 500);
	}

	public String refreshUserTimeOut(String username) {
		String[] user = Users.get(username);
		user[0] = Integer.toString(10);
		return lastMessage;
	}

	public void decrementUserTimeOut() {

		Iterator<Map.Entry<String, String[]>> iter = Users.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String[]> entry = iter.next();

			String username = entry.getKey();

			Integer timeOut = Integer.parseInt(entry.getValue()[0]);
			timeOut--;

			entry.getValue()[0] = Integer.toString(timeOut);

			String mud = entry.getValue()[1];

			System.out.format("Username: %s, Timeout: %s in MUD %s%n", username, timeOut, mud);

			if (timeOut <= 0) {
				lastMessage = "User " + username + " has timed out in MUD: " + mud;
				System.out.println(lastMessage);
				iter.remove();
				MUDInstance.users.remove(username);
			}
		}
	}

	public boolean createMUD(String name) throws RemoteException {
		if (MAX_NUM_OF_MUDS > MUDTotal) {
			Servers.put(name, new MUD("aberdeen.edg", "aberdeen.msg", "aberdeen.thg"));
			MUDTotal++;
			return true;
		}
		return false;
	}

	public String getPlayerTotal() throws RemoteException {
		return Integer.toString(Users.size());
	}

	public String getMUDTotal() throws RemoteException {
		return MUDTotal.toString();
	}

	public String getMUDPlayerTotal() throws RemoteException {
		return Integer.toString(MUDInstance.users.size());
	}

	public String introduction() throws RemoteException {
		return "================================================================================================================================== \n" +
				"                                                      Welcome to " + MUDInstanceName + "!                                          \n" +
				"                                                     " + getPlayerTotal() + " Total players online                                 \n" +
				"                                                  " + getMUDPlayerTotal() + " Players online in this MUD                           \n" +
				"==================================================================================================================================";
	}

	public String getStartLocation() throws RemoteException {
		return MUDInstance.startLocation();
	}

	public String getLocationInfo(String location) throws RemoteException {
		return MUDInstance.getVertex(location).toString();
	}

	public String moveDirection(String currentLocation, String direction, String username) throws RemoteException {
		return MUDInstance.moveThing(currentLocation, direction, username);
	}

	public boolean initializeUser(String username, String serverName) throws RemoteException {
		if ((MUDInstance.users.size() <= MAX_USERS_PER_MUD) && (Users.size() < MAX_TOTAL_PLAYERS)) {

			String[] userArray = {"10", serverName};

			Users.put(username, userArray);

			lastMessage = "Player " + username + " has joined the game.";

			MUDInstance.addThing(MUDInstance.startLocation(), username);
			MUDInstance.users.put(username, MUDInstance.startLocation());
			return true;
		}
		return false;
	}

	public String getObjectsAtLocation(String location) throws RemoteException {
		return MUDInstance.locationInfo(location);
	}

	public void takeItem(String item, String location) throws RemoteException {
		MUDInstance.deleteThing(location, item);
	}

	public void dropItem(String item, String location) throws RemoteException {
		MUDInstance.addThing(location, item);
	}

	public String changeMUD(String MUDName) throws RemoteException {
		MUDInstanceName = MUDName;
		MUDInstance = Servers.get(MUDName);

		return MUDName;
	}

	public void exitMUD(String username, String location) throws RemoteException {
		lastMessage = "User " + username + " has left the game.";
		Users.remove(username);
		MUDInstance.users.remove(username);
		takeItem(username, location);
	}

	public String getServersString() throws RemoteException {
		return Servers.keySet().toString().replaceAll("\\[|\\]", "").replaceAll(",", " | ");
	}
}
