package mud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author stefanrudvin 51549217
 *
 * Class modified from practicals.package practicals.rmishout.ShoutServiceClient
 */
public class MUDClient {

	private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	/**
	 * Store variables of current game session
	 */
	private static String playerName;
	private static String serverName = "aberdeen";

	private static String playerLocation;
	private static MUDServiceInterface MUDService;

	/**
	 * Specify whether the game is running, i.e. accept player input
	 */
	private static boolean running = true;

	/**
	 * Stores all items the player is carrying
	 */
	private static List<String> items = new ArrayList<>();

	/**
	 * Store current message which shows information about players leaving, timing out or joining a MUD
	 */
	private static String broadcastMessage = "";

	/**
	 * Specify whether the player is currently changing MUD. This stops the client pinging a non-existent player.
	 */
	private static Boolean changingMUD = false;

	/**
	 * Class modified from practicals.rmishout.ShoutServerMainline.java
	 */

	public static void main(String args[]) {

		if (args.length < 2) {
			System.err.println("Usage:\njava MUDClient <host> <port>");
			return;
		}

		String hostname = args[0];
		int registryPort = Integer.parseInt(args[1]);

		System.setProperty("java.security.policy", "mud.policy");
		System.setSecurityManager(new RMISecurityManager());

		try {
			String regURL = "rmi://" + hostname + ":" + registryPort + "/MUDService";
			System.out.println("Looking up " + regURL);

			// Setup service
			MUDService = (MUDServiceInterface) Naming.lookup(regURL);

			initialize();

			runGame();

		} catch (java.io.IOException e) {
			System.err.println("I/O error.");
			System.err.println(e.getMessage());
		} catch (java.rmi.NotBoundException e) {
			System.err.println("Server not bound.");
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Initialize the game before running
	 */
	private static void initialize() throws RemoteException {

		printMainIntro();

		selectPlayerName();

		showMudsAndJoin();

		startServerPing();

		printCurrentMudIntro();

		playerLocation = MUDService.getStartLocation();
	}

	/**
	 * Select playerName throughout session.
	 */
	private static void selectPlayerName() {
		System.out.println("Enter your playerName:");
		try {
			printCarets();
			playerName = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("");
	}

	/**
	 * Show available muds and allow player to join one
	 */
	private static void showMudsAndJoin() throws RemoteException {
		showMuds();

		joinMUD();
	}

	/**
	 * Show current MUDS
	 */
	private static void showMuds() throws RemoteException {
		System.out.println("Here are the available MUDs:");
		System.out.println(MUDService.getMudsString());
	}

	/**
	 * Allow player to join mud. If it is full, allow player to wait or try again indefinitely
	 */
	private static void joinMUD() throws RemoteException {
		System.out.println("Select which MUD to join:");

		boolean accepted = false;

		while (!accepted) {
			selectMud();
			// Try to join until accepted
			if (tryJoinMUD()) {
				accepted = true;
			} else {
				System.out.println("Sorry, this or all servers are full. Please wait or try another:");
			}
		}
	}

	/**
	 * Allow player to select which MUD to join
	 */
	private static void selectMud() {
		try {
			printCarets();
			serverName = in.readLine();
			MUDService.changeMUD(serverName, playerName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Try to join a mud, and return boolean on whether join was successful
	 */
	private static boolean tryJoinMUD() throws RemoteException {
		return MUDService.initializePlayer(playerName, serverName);
	}

	/**
	 * Begin pinging player object in server to avoid timeout. This is paused when player changes MUDs with changingMUD
	 */
	private static void startServerPing() {
		Timer timerObj = new Timer(true);
		timerObj.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					if (!changingMUD) {
						refreshTimeOutAndRetrieveBroadcast();
					}

				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}, 10, 500);
	}

	/**
	 * Refresh player timeout and printout broadcast message if it is different form the current one.
	 */
	private static void refreshTimeOutAndRetrieveBroadcast() throws RemoteException {
		String message = MUDService.refreshPlayerTimeOut(playerName);

		if (!message.equals(broadcastMessage) && !message.equals("")) {
			broadcastMessage = message;
			System.out.println(message);
			printCarets();
		}
	}

	/**
	 * Print intro for the current Mud
	 */
	private static void printCurrentMudIntro() throws RemoteException {
		clearConsole();

		System.out.println(MUDService.welcome());
	}

	/**
	 * Change the current mud player is playing in
	 */
	private static void changeMUD() throws RemoteException {

		showMudsAndJoin();

		changingMUD = false;

		printCurrentMudIntro();

		playerLocation = MUDService.getStartLocation();
	}

	/**
	 * Main game loop for playing
	 */
	private static void runGame() throws RemoteException {

		printHelp();

		printCurrentLocationInfo();

		while (running) try {
			System.out.println("");
			printCarets();
			String choice = in.readLine().toLowerCase();

			handlePlayerActions(choice);

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Game exit.");
		System.out.println("Catch you next time!");
		printMainIntro();
	}

	/**
	 * Handle user taking an object
	 * @param choice String
	 * @return Boolean
	 */
	private static Boolean handleTakeAction(String choice) throws RemoteException  {
		if (choice.contains("take")) {
			String item = parseChoice(choice);

			MUDService.takeItem(item, playerLocation);

			System.out.println("You have picked up: " + item);
			items.add(item);

			return true;
		}
		return false;
	}

	/**
	 * Handle user moving in the game
	 * @param choice String
	 * @return Boolean
	 */
	private static Boolean handleMoveAction(String choice) throws RemoteException {
		if (choice.contains("move")) {

			String direction = parseChoice(choice);

			String location = MUDService.moveDirection(playerLocation, direction, playerName);

			if (location.equals(playerLocation)) {
				System.out.println("There is an obstacle in the way. Try another direction:");
				return true;

			} else {
				playerLocation = location;
				System.out.println("You have moved to location: " + location);
				printCurrentLocationInfo();
				return true;
			}
		}
		return false;
	}

	/**
	 * Handle user dropping an item
	 * @param choice String
	 * @return Boolean
	 */
	private static Boolean handleDropAction(String choice) throws RemoteException {
		if (choice.contains("drop")) {
			String item = parseChoice(choice);

			MUDService.dropItem(item, playerLocation);

			System.out.println("You dropped: " + item);
			items.remove(item);
			return true;
		}
		return false;
	}

	/**
	 * Handle all the available actions a player can make
	 */
	private static void handlePlayerActions(String choice) throws IOException {

		if (handleMoveAction(choice) || handleTakeAction(choice) || handleDropAction(choice)) {
			return;
		}

		switch (choice) {
			case ("help"):
				printHelp();
				break;
			case "whoami":
				System.out.println(playerName);
				break;
			case "look":
				System.out.println(MUDService.getObjectsAtLocation(playerLocation));
				break;
			case "items":
				System.out.println("Contents of inventory: ");
				items.forEach(System.out::println);
				break;
			case "exit":
				exitMUD();
				running = false;
				break;
			case "setmaxtotalplayers":
				handleSetMaxTotalPlayers();
				break;
			case "setmaxmudplayers":
				handleSetMaxMudPlayers();
				break;
			case "setmaxmuds":
				handleSetMaxMuds();
				break;
			case "mudlist":
				showMuds();
				break;
			case "mudmigrate":
				exitMUD();
				changeMUD();
				printCurrentLocationInfo();
				break;
			case "mudcreate":
				handleCreateMud();
				break;
			case "mudtotal":
				System.out.println(MUDService.getMUDTotal());
				break;
			case "mudplayers":
				System.out.println(MUDService.getMUDPlayerTotal());
				break;
			case "mudtotalplayers":
				System.out.println(MUDService.getPlayerTotal());
				break;
			default:
				printHelp();
				break;
		}
	}

	private static void handleSetMaxTotalPlayers() throws IOException {
		System.out.println("Enter maximum total player allowance: ");

		printCarets();

		String choice = in.readLine().toLowerCase();

		System.out.println("Maximum player allowance set to: " + MUDService.setMaxTotalPlayers(Integer.parseInt(choice)));
	}

	private static void handleSetMaxMudPlayers() throws IOException {
		System.out.println("Enter maximum total player allowance per MUD: ");

		printCarets();

		String choice = in.readLine().toLowerCase();

		System.out.println("Maximum player allowance per MUD set to: " + MUDService.setMaxPlayersPerMud(Integer.parseInt(choice)));
	}

	private static void handleSetMaxMuds() throws IOException {
		System.out.println("Enter maximum MUD allowance: ");

		printCarets();

		String choice = in.readLine().toLowerCase();

		System.out.println("Maximum MUD allowance set to: " + MUDService.setMaxNumOfMuds(Integer.parseInt(choice)));
	}

	/**
	 *	Handle player exciting a MUD - drops all items, sends request to server and pauses timeout ping
	 */
	private static void exitMUD() throws RemoteException {

		// Drop all items
		for (String item : items) {
			MUDService.dropItem(item, playerLocation);
		}
		changingMUD = true;

		// Remove from players and items in MUD
		MUDService.exitMUD(playerName, playerLocation);
	}

	/**
	 * Return second parameter of String
	 * @param choice String
	 */
	private static String parseChoice(String choice) {
		String[] splitChoice = choice.split("\\s+");
		return splitChoice[1];
	}

	/**
	 * Allow player to create a new MUD
	 */
	private static void handleCreateMud() {
		System.out.println("Select name of new MUD:");

		try {
			printCarets();
			String name = in.readLine();

			if (MUDService.createMUD(name)) {
				System.out.println("New MUD created with name: " + name + ".");
			} else {
				System.out.println("Maximum MUD limit reached.");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Print information about current player location
	 */
	private static void printCurrentLocationInfo() throws RemoteException {
		System.out.println(MUDService.getLocationInfo(playerLocation));
	}

	/**
	 * Print help menu to show available commands
	 */
	private static void printHelp() {
		System.out.println("Available commands:");
		System.out.println("Move <direction> - move to a selected direction (North/East/South/West)");
		System.out.println("Take <item>      - Take selected item into your inventory");
		System.out.println("Drop <item>      - Drop selected item to the ground");
		System.out.println("Items            - Show items in your inventory");
		System.out.println("Whoami           - Show your player playerName");
		System.out.println("Look             - Show items, players and paths at current location");
		System.out.println("Help             - Show this help menu");
		System.out.println("Exit             - Exit the game");

		System.out.println("");

		System.out.println("MUDlist          - Show list of current MUDs");
		System.out.println("MUDmigrate       - Move to another MUD");
		System.out.println("MUDcreate        - Create a whole new MUD");

		System.out.println("MUDtotal         - Show total number of MUDs");
		System.out.println("MUDplayers       - Show number of players in current MUD");
		System.out.println("MUDtotalplayers  - Show total player number throughout all MUDs");

		System.out.println("");

		System.out.println("setmaxmuds         - Set maximum number of MUDs");
		System.out.println("setmaxmudplayers   - Set maximum total players per MUD");
		System.out.println("setmaxtotalplayers - Set maximum total players in the game");
	}

	/**
	 * Draw a set of carets to aid in player input
	 */
	private static void printCarets() {
		System.out.print(">> ");
	}

	/**
	 * Clear console window. May not work on windows machines.
	 */
	private static void clearConsole() {
		System.out.print("\033[H\033[2J");
	}

	/**
	 * Print 'From dusk till dawn' ASCII art intro
	 */
	private static void printMainIntro() {
		clearConsole();
		System.out.println("==================================================================================================================================");

		System.out.println(
				"___________                       ________                __     ___________.__.__  .__    ________                       \n" +
						"\\_   _____/______  ____   _____   \\______ \\  __ __  _____|  | __ \\__    ___/|__|  | |  |   \\______ \\ _____ __  _  ______  \n" +
						" |    __) \\_  __ \\/  _ \\ /     \\   |    |  \\|  |  \\/  ___/  |/ /   |    |   |  |  | |  |    |    |  \\\\__  \\\\ \\/ \\/ /    \\ \n" +
						" |     \\   |  | \\(  <_> )  Y Y  \\  |    `   \\  |  /\\___ \\|    <    |    |   |  |  |_|  |__  |    `   \\/ __ \\\\     /   |  \\\n" +
						" \\___  /   |__|   \\____/|__|_|  / /_______  /____//____  >__|_ \\   |____|   |__|____/____/ /_______  (____  /\\/\\_/|___|  /\n" +
						"     \\/                       \\/          \\/           \\/     \\/                                   \\/     \\/           \\/ ");

		System.out.println("==================================================================================================================================");
	}

}
