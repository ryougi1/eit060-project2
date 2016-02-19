package network;
import java.io.*;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.sql.SQLException;
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

    public Server(ServerSocket ss) throws IOException, ClassNotFoundException, SQLException {
        serverSocket = ss;
        newListener();
        db = new Database();
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
            User u = null;
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
            
            db.establishConnection();
            int i = 10;
            while(i > 0) {
            	try {
					dbInput(u, in, out);
				} catch (Exception e) {
					break;
				}
            	i--;
            }
            db.terminateConnection();
            
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
        } catch (SQLException e) {
        	System.out.println("Database: " + e.getMessage());
			e.printStackTrace();
		}
    }

    private void dbInput(User u, BufferedReader in, PrintWriter out) throws Exception {
    	String command = sendRequest("Enter a command:", in, out);
        String patient = sendRequest("Enter patient name", in, out);
    	String log = null;
		switch (command.toLowerCase()) {
		case "add":	
			// Only doctor can create new records for a patient provided that the doctor is treating the patient
			if(!(u instanceof Doctor)) {
				sendDeniedPermission(command, in, out);
			}
			String nurse = sendRequest("Enter name of nurse associated with " + patient, in, out);
			String data = sendRequest("Enter data for " + patient + "'s record", in, out);
			
			log = "Record: " + "Command: " + command + ". Patient: " + patient 
					+ ". Nurse: " + nurse + ". Data: " + data;
			System.out.println(log);
			break;
		case "remove": 
			// Only government agency is allowed to delete records
			if(!(u instanceof Government)) {
				sendDeniedPermission(command, in, out);
			}
			break;
		case "read": 
			// Everyone can read records, assumed they are associated with him/her
			break;
		case "edit": 
			// Only patients and government is not allowed to edit records
			if(u instanceof Patient || u instanceof Government) {
				sendDeniedPermission(command, in, out);
			}
			break;
		default: 
			sendRequest("You didn't use a correct command", in, out);
			break;
		}
    	System.out.println("done\n");
	}
    
    private String sendRequest(String request, BufferedReader in, PrintWriter out) throws Exception {
    	System.out.print("sending '" + request + "' to client...");
        out.println(request);
    	out.flush();
        String clientAns = in.readLine();
        if(clientAns == null)
        	throw new Exception("client disconnected");
        System.out.println("received '" + clientAns + "' from client");
        return clientAns;
    }
    
    private void sendDeniedPermission(String command, BufferedReader in, PrintWriter out) throws Exception {
    	sendRequest("You don't have the required permission to execute " + command, in, out);
    }

	private void newListener() { (new Thread(this)).start(); } // calls run()

    public static void main(String args[]) throws ClassNotFoundException, SQLException {
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
