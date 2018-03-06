package mud;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.server.UnicastRemoteObject;

public class MUDServerMainline {
	/*
		Class modified from practicals.rmishout.ShoutServerMainline.java
	 */

	public static void main(String args[])
	{

		if (args.length < 2) {
			System.err.println( "Usage:\njava ShoutServerMainline <registryport> <serverport>" ) ;
			return;
		}

		try
		{
			String hostname = (InetAddress.getLocalHost()).getCanonicalHostName() ;

			// specify at which port the rmiregistry is listening for binding and
			// lookup requests

			int registryPort = Integer.parseInt( args[0] ) ;

			int servicePort = Integer.parseInt( args[1] );

			System.setProperty( "java.security.policy", "mud.policy" ) ;
			System.setSecurityManager( new RMISecurityManager() ) ;

			MUDServiceImpl MUDService = new MUDServiceImpl();

			MUDServiceInterface MUDStub =
					(MUDServiceInterface) UnicastRemoteObject.exportObject( MUDService, servicePort );

			// create a URL that uniquely identifies the registered service

			String regURL = "rmi://" + hostname + ":" + registryPort + "/MUDService";

			System.out.println("Registering " + regURL );

			// Finally, register the stub of the remote object with the rmiregistry

			Naming.rebind( regURL, MUDStub );

			// Note the server will not shut down!
		}
		catch(java.net.UnknownHostException e) {
			System.err.println( "Cannot get local host name." );
			System.err.println( e.getMessage() );
		}
		catch (java.io.IOException e) {
			System.err.println( "Failed to register." );
			System.err.println( e.getMessage() );
		}
	}

}
