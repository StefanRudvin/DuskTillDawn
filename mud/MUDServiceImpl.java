package mud;

import java.util.*;

public class MUDServiceImpl implements MUDServiceInterface {

	//Class modified from practicals.package practicals.rmishout.ShoutServiceImpl

	/**
		Current instance of the MUD
	 */
	private MUD MUDInstance;
	private String MUDInstanceName;

	/**
	 * Hash map of all available MUDs
	 */
	private Map<String, MUD> Muds = new HashMap<>();

	/**
		Current players in MUD. playerName : <Timeout, Location>
	 */
	private Map<String, String[]> Players = new HashMap<>();

	/**
	 	These constants can be changed to allow for server size manipulation.
	 */
	private static final int MAX_NUM_OF_MUDS = 5;
	private static final int MAX_TOTAL_PLAYERS = 100;
	private static final int MAX_PLAYERS_PER_MUD = 10;


	/**
	 * Stores messages that are returned to the client. E.g. when player disconnects, times out or joins a mud.
	 */
	private String broadcastMessage = "";

	public MUDServiceImpl() {
		/*
			Create sample MUDs when server starts
		 */
		Muds.put("sample", new MUD("maps/sample.edg", "maps/sample.msg", "maps/sample.thg"));
		Muds.put("aberdeen", new MUD("maps/aberdeen.edg", "maps/aberdeen.msg", "maps/aberdeen.thg"));
		Muds.put("aberdeen2", new MUD("maps/aberdeen.edg", "maps/aberdeen.msg", "maps/aberdeen.thg"));

		/*
			Start player timeout function.
		 	This runs every 0.5 seconds, and after it runs 10 times without a player ping
		 	the player is disconnected from the server,
		 	removed from the Players object and the
		 	users object in the mud instance.
		 */

		Timer timerObj = new Timer(true);
		timerObj.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				decrementPlayerTimeOut();
			}
		}, 10, 500);
	}

	/**
	 *	This method runs every 500ms, decrementing the timeout variable of the players object to time out players.
	 */
	public void decrementPlayerTimeOut() {

		Iterator<Map.Entry<String, String[]>> iter = Players.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String[]> entry = iter.next();

			String playerName = entry.getKey();

			Integer timeOut = Integer.parseInt(entry.getValue()[0]);
			timeOut--;

			entry.getValue()[0] = Integer.toString(timeOut);

			String mud = entry.getValue()[1];

			if (timeOut <= 0) {
				disconnectPlayer(mud, playerName);
				iter.remove();
			}
		}
	}


	/**
	 * This method allows the client to refresh the players object so that the player does not get timed out.
	 * @param playerName String
	 * @return String
	 */
	public String refreshPlayerTimeOut(String playerName) {
		String[] player = Players.get(playerName);
		player[0] = Integer.toString(10);
		return broadcastMessage;
	}

	/**
	 * Disconnect player and send message to all players
	 * @param mud MUD to disconnect from
	 * @param playerName String
	 */
	private void disconnectPlayer(String mud, String playerName) {
		broadcastMessage = "Player " + playerName + " has timed out in MUD: " + mud;
		System.out.println(broadcastMessage);
		MUDInstance.users.remove(playerName);
	}

	/**
	 *	Allows client to create a new MUD with a custom name.
	 * @param name String
	 * @return boolean
	 */
	public boolean createMUD(String name) {
		if (MAX_NUM_OF_MUDS > Muds.size()) {
			Muds.put(name, new MUD("maps/aberdeen.edg", "maps/aberdeen.msg", "maps/aberdeen.thg"));
			return true;
		}
		return false;
	}

	/**
	 * Get total number of players
	 * @return String
	 */
	public String getPlayerTotal() {
		return Integer.toString(Players.size());
	}

	/**
	 * Get total number of MUDs running
	 * @return String
	 */
	public String getMUDTotal() {
		return Integer.toString(Muds.size());
	}

	/**
	 * Get total number of players in current MUD
	 * @return String
	 */
	public String getMUDPlayerTotal() {
		return Integer.toString(MUDInstance.users.size());
	}

	/**
	 * Get start location of player in current MUD
	 * @return String
	 */
	public String getStartLocation() {
		return MUDInstance.startLocation();
	}

	/**
	 * Get information about the current location of the player
	 * @param location String
	 * @return String
	 */
	public String getLocationInfo(String location) {
		return MUDInstance.getVertex(location).toString();
	}

	/**
	 * Move player in a specific direction
	 * @param currentLocation String
	 * @param direction String
	 * @param playerName String
	 * @return String
	 */
	public String moveDirection(String currentLocation, String direction, String playerName) {
		return MUDInstance.moveThing(currentLocation, direction, playerName);
	}

	/**
	 * Initialize the player in the service and send a message to all players
	 * @param playerName String
	 * @param serverName String
	 * @return boolean
	 */
	public boolean initializePlayer(String playerName, String serverName) {
		if ((MUDInstance.users.size() <= MAX_PLAYERS_PER_MUD) && (Players.size() < MAX_TOTAL_PLAYERS)) {

			String[] playerInfoArray = {"10", serverName};

			Players.put(playerName, playerInfoArray);

			broadcastMessage = "Player " + playerName + " has joined the game.";

			MUDInstance.addThing(MUDInstance.startLocation(), playerName);
			MUDInstance.users.put(playerName, MUDInstance.startLocation());
			return true;
		}
		return false;
	}

	/**
	 * Get all objects at current player location
	 * @param location String
	 * @return String
	 */
	public String getObjectsAtLocation(String location) {
		return MUDInstance.locationInfo(location);
	}

	/**
	 * Take a selected item at a location
	 * @param item String
	 * @param location String
	 */
	public void takeItem(String item, String location) {
		MUDInstance.deleteThing(location, item);
	}

	/**
	 * Drop an item to a location
	 * @param item String
	 * @param location String
	 */
	public void dropItem(String item, String location) {
		MUDInstance.addThing(location, item);
	}

	/**
	 * Change the current MUD to another specified one
	 * @param MUDName String
	 * @return String
	 */
	public String changeMUD(String MUDName, String playerName) {
		MUDInstanceName = MUDName;
		MUDInstance = Muds.get(MUDName);

		broadcastMessage = "Player " + playerName + " has joined MUD: " + MUDName;

		return MUDName;
	}

	/**
	 *	Exit the current MUD, send message
	 * @param playerName String
	 * @param location String
	 */
	public void exitMUD(String playerName, String location) {
		broadcastMessage = "Player " + playerName + " has left the game.";
		Players.remove(playerName);
		MUDInstance.users.remove(playerName);
		takeItem(playerName, location);
	}

	/**
	 * Return a string
	 * @return String
	 */
	public String getMudsString() {
		return Muds.keySet().toString().replaceAll("[\\[]]", "").replaceAll(",", " | ");
	}

	/**
	 *	Show welcome string specific to current MUDInstance
	 * @return String
	 */
	public String welcome(){
		return "================================================================================================================================== \n" +
				"                                                      Welcome to " + MUDInstanceName + "!                                          \n" +
				"                                                     " + getPlayerTotal() + " Total players online                                 \n" +
				"                                                  " + getMUDPlayerTotal() + " Players online in this MUD                           \n" +
				"==================================================================================================================================";
	}
}
