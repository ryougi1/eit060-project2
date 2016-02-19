package network;
import java.io.*;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.util.Scanner;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

import types.*;
import database.*;

public class Server implements Runnable {
    private ServerSocket serverSocket = null;
    private static int numConnectedClients = 0;
    private Database db;

    public Server(ServerSocket ss) throws IOException {
        serverSocket = ss;
        newListener();
    }

    public void run() {
        try {
            SSLSocket socket=(SSLSocket)serverSocket.accept();
            newListener();
            SSLSession session = socket.getSession();
            X509Certificate cert = (X509Certificate)session.getPeerCertificateChain()[0];
            String subject = cert.getSubjectDN().getName();
            String info[] = new String[] {
            	subject.split("CN=")[1].split(",")[0],	// PNbr
            	subject.split("OU=")[1].split(",")[0],	// Division
            	subject.split("O=")[1].split(",")[0],	// Usertype
            	subject.split("L=")[1].split(",")[0],	// Name
            };
            User u;
            switch (info[2].toString()) {
			case "Doctor":
				u = new Doctor(info[3], info[0], info[1]);
				break;
			case "Nurse":
				u = new Nurse(info[3], info[0], info[1]);
				break;
			case "Patient":
				u = new Patient(info[3], info[0]);
				break;
			case "Government":
				u = new Government(info[0]);
				break;
			}
    	    numConnectedClients++;
            System.out.println("client connected");
            System.out.println("client name (cert subject DN field): " + subject);
            System.out.println(numConnectedClients + " concurrent connection(s)\n");

            PrintWriter out = null;
            BufferedReader in = null;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String clientMsg = null;
            while ((clientMsg = in.readLine()) != null) {
			    String input = dbInput(clientMsg.split(" "));
                System.out.println("received '" + clientMsg + "' from client");
                System.out.print("sending '" + input + "' to client...");
				out.println(input);
				out.flush();
                System.out.println("done\n");
			}
			in.close();
			out.close();
			socket.close();
    	    numConnectedClients--;
            System.out.println("client disconnected");
            System.out.println(numConnectedClients + " concurrent connection(s)\n");
		} catch (IOException e) {
            System.out.println("Client died: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    private String dbInput(String[] split) {
		
    	return null;
	}

	private void newListener() { (new Thread(this)).start(); } // calls run()

    public static void main(String args[]) {
    	Scanner scan = new Scanner(System.in);
        int port = -1;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        System.out.print("Port: ");
        port = scan.nextInt();
        System.out.println("\nServer Started\n");
        String type = "TLS";
        try {
            ServerSocketFactory ssf = getServerSocketFactory(type);
            ServerSocket ss = ssf.createServerSocket(port);
            ((SSLServerSocket)ss).setNeedClientAuth(true); // enables client authentication
            new Server(ss);
        } catch (IOException e) {
            System.out.println("Unable to start Server: " + e.getMessage());
            e.printStackTrace();
        }
        scan.close();
    }

    private static ServerSocketFactory getServerSocketFactory(String type) {
        if (type.equals("TLS")) {
            SSLServerSocketFactory ssf = null;
            try { // set up key manager to perform server authentication
                SSLContext ctx = SSLContext.getInstance("TLS");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
                char[] password = "password".toCharArray();
                
                ks.load(new FileInputStream("resources/serverkeystore"), password);  // keystore password (storepass)
                ts.load(new FileInputStream("resources/servertruststore"), password); // truststore password (storepass)
                kmf.init(ks, password); // certificate password (keypass)
                tmf.init(ts);  // possible to use keystore as truststore here
                ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                ssf = ctx.getServerSocketFactory();
                return ssf;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return ServerSocketFactory.getDefault();
        }
        return null;
    }
}
