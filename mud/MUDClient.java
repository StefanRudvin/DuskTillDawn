package mud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.*;

public class MUDClient {

	protected static String playerLocation;
	private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	private static MUDServiceInterface MUDService;

	private static String serverName = "aberdeen";
	private static String username;

	private static boolean running = true;
	private static List<String> items = new ArrayList<String>();

	private static String lastMessage = "";
	private static Boolean changingMUD = false;

	/*
		Class modified from practicals.rmishout.ShoutServerMainline.java
	 */

	public static void main(String args[]) throws RemoteException {

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

	static void runGame() throws RemoteException {

		showHelp();

		printCurrentLocationInfo();

		while (running) try {
			System.out.println("");
			drawCarets();
			String choice = in.readLine().toLowerCase();

			if (choice.contains("move")) {

				String direction = parseChoice(choice);

				String location = MUDService.moveDirection(playerLocation, direction, username);

				if (location.equals(playerLocation)) {
					System.out.println("There is an obstacle in the way. Try another direction:");
					continue;

				} else {
					playerLocation = location;
					System.out.println("You have moved to location: " + location);
					printCurrentLocationInfo();
					continue;
				}
			}

			if (choice.contains("take")) {
				String item = parseChoice(choice);

				MUDService.takeItem(item, playerLocation);

				System.out.println("You have picked up: " + item);
				items.add(item);

				continue;
			}

			if (choice.contains("drop")) {
				String item = parseChoice(choice);

				MUDService.dropItem(item, playerLocation);

				System.out.println("You dropped: " + item);
				items.remove(item);
				continue;
			}

			choiceSwitch(choice);

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Game exit.");
		System.out.println("Catch you next time!");
		introText();
	}

	static void choiceSwitch(String choice) throws RemoteException {

		switch (choice) {
			case ("help"):
				showHelp();
				break;
			case "whoami":
				System.out.println(username);
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
			case "mudshow":
				showServers();
				break;
			case "mudmigrate":
				exitMUD();
				changeMUD();
				break;
			case "mudcreate":
				createMUD();
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
				showHelp();
				break;
		}
	}

	static void exitMUD() throws RemoteException {

		// Drop all items
		for (Iterator<String> i = items.iterator(); i.hasNext();) {
			String item = i.next();
			MUDService.dropItem(item, playerLocation);
		}
		changingMUD = true;

		// Remove from users and items in MUD
		MUDService.exitMUD(username, playerLocation);
	}

	static String parseChoice(String choice) {
		String[] splitChoice = choice.split("\\s+");
		return splitChoice[1];
	}

	static void changeMUD () throws RemoteException {

		System.out.println("Here are the available MUDs:");

		showServers();

		System.out.println("");

		joinMUD();

		changingMUD = false;

		clearConsole();

		System.out.println(MUDService.introduction());

		playerLocation = MUDService.getStartLocation();

		printCurrentLocationInfo();
	}

	static void createMUD() throws RemoteException {
		System.out.println("Select name of new MUD:");

		try {
			drawCarets();
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

	static void joinMUD() throws RemoteException {
		System.out.println("Select which MUD to join:");

		boolean accepted = false;

		while (!accepted) {
			selectServer();
			// Try to join until accepted
			if (tryJoinMUD()) {
				accepted = true;
			} else {
				System.out.println("Sorry, this or all servers are full. Please wait or try another:");
			}
		}
	}

	static void initialize() throws RemoteException {

		introText();

		System.out.println("Enter your username:");

		selectUserName();

		System.out.println("");

		System.out.println("Here are the available MUDs:");

		showServers();

		System.out.println("");

		joinMUD();

		startServerPing();

		clearConsole();

		System.out.println(MUDService.introduction());

		playerLocation = MUDService.getStartLocation();
	}

	static boolean tryJoinMUD() throws RemoteException {
		return MUDService.initializeUser(username, serverName);
	}

	static void startServerPing() throws RemoteException {
		Timer timerObj = new Timer(true);
		timerObj.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					if (!changingMUD) {
						refreshTimeOut();
					}

				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}, 10, 500);
	}

	static void refreshTimeOut() throws RemoteException {
		String message = MUDService.refreshUserTimeOut(username);

		if (!message.equals(lastMessage) && !message.equals("")) {
			lastMessage = message;
			System.out.println(message);
			drawCarets();
		}
	}


	static void selectServer() {
		try {
			drawCarets();
			serverName = in.readLine();
			MUDService.changeMUD(serverName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void showServers() throws RemoteException {
		System.out.println(MUDService.getServersString());
	}

	static void selectUserName() {
		try {
			drawCarets();
			username = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	static void printCurrentLocationInfo() throws RemoteException {
		printLocationInfo(playerLocation);
	}

	static void printLocationInfo(String location) throws RemoteException {
		System.out.println(MUDService.getLocationInfo(location));
	}

	static void showHelp() {
		System.out.println("Available commands:");
		System.out.println("Move <direction> - move to a selected direction (North/East/South/West)");
		System.out.println("Take <item>      - Take selected item into your inventory");
		System.out.println("Drop <item>      - Drop selected item to the ground");
		System.out.println("Items            - Show items in your inventory");
		System.out.println("Whoami           - Show your player username");
		System.out.println("Look             - Show items, players and paths at current location");
		System.out.println("Help             - Show this help menu");
		System.out.println("Exit             - Exit the game");

		System.out.println("");

		System.out.println("MUDshow          - Show list of current MUDs");
		System.out.println("MUDmigrate       - Move to another MUD");
		System.out.println("MUDcreate        - Create a whole new MUD");
		System.out.println("MUDtotal         - Show total number of MUDs");
		System.out.println("MUDplayers       - Show number of players in current MUD");
		System.out.println("MUDtotalplayers  - Show total player number throughout all MUDs");
	}

	static void drawCarets () {
		System.out.print(">> ");
	}

	static void clearConsole () {
		System.out.print("\033[H\033[2J");
	}


	static void introText() {
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
