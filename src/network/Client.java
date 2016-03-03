package network;
import java.io.BufferedReader;
import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.Scanner;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.X509Certificate;

/*
 * This example shows how to set up a key manager to perform client
 * authentication.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */
public class Client {

    public static void main(String[] args) throws Exception {
    	if(args.length == 0) {
    		System.out.print("Localhost port pNbr: ");
    		args = new Scanner(System.in).nextLine().split(" ");    		
    	}
    	String host = null;
        int port = -1;
        String pNbr = null;
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "] = " + args[i]);
        }
        if (args.length < 2) {
            System.out.println("USAGE: java client host port");
            System.exit(-1);
        }
        Console cons;
        char[] password = null;
        if ((cons = System.console()) != null &&
            (password = cons.readPassword("[%s]", "Password:")) != null) {
        } else {
        	System.out.print("(In-Eclipse) Password: ");
        	password = new Scanner(System.in).nextLine().toCharArray();
        }
        
        try { /* get input parameters */
            host = args[0];
            port = Integer.parseInt(args[1]);
            pNbr = args[2];
        } catch (IllegalArgumentException e) {
            System.out.println("USAGE: java client host port");
            System.exit(-1);
        }
        try { /* set up a key manager for client authentication */
            SSLSocketFactory factory = null;
            try {
            	//char[] password = args[3].toCharArray();
                KeyStore ks = KeyStore.getInstance("JKS");
                KeyStore ts = KeyStore.getInstance("JKS");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                SSLContext ctx = SSLContext.getInstance("TLS");
                ks.load(new FileInputStream("certificates/client/" + pNbr + "/keystore"), password);  // keystore password (storepass)
				ts.load(new FileInputStream("certificates/client/" + pNbr + "/truststore"), password); // truststore password (storepass);
				kmf.init(ks, password); // user password (keypass)
				tmf.init(ts); // keystore can be used as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				java.util.Arrays.fill(password, ' ');
                factory = ctx.getSocketFactory();
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
            SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
            System.out.println("\nsocket before handshake:\n" + socket + "\n");

            /*
             * send http request
             *
             * See SSLSocketClient.java for more information about why
             * there is a forced handshake here when using PrintWriters.
             */
            socket.startHandshake();

            SSLSession session = socket.getSession();
            X509Certificate cert = (X509Certificate)session.getPeerCertificateChain()[0];
            String subject = cert.getSubjectDN().getName();
            System.out.println("certificate name (subject DN field) on certificate received from server:\n" + subject + "\n");
		    String issuer = cert.getIssuerDN().getName();
		    System.out.println("certificate name (issuer DN field) on certificate received from server:\n" + issuer + "\n");
		    
		    String serialnumber = String.valueOf(cert.getSerialNumber());
		    System.out.println("certificate serial number on certificate received from server:\n" + serialnumber + "\n");
		    
            System.out.println("socket after handshake:\n" + socket + "\n");
            System.out.println("secure connection established\n\n");

            BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
			for (;;) {
				System.out.println(in.readLine() + "\n");

				System.out.print("> ");
                msg = read.readLine();
                if (msg.equalsIgnoreCase("quit")) {
				    break;
				}
                out.println(msg);
                out.flush();

            }
            in.close();
            out.close();
            read.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}