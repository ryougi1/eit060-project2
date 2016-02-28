package network;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.X509Certificate;

import database.Database;
import database.Record;
import types.Doctor;
import types.Government;
import types.Nurse;
import types.Patient;
import types.User;

public class Server implements Runnable {
    private ServerSocket serverSocket = null;
    private static int numConnectedClients = 0;
    private Database db;
    private boolean debug = false;

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
            println("client connected");
            println("client name (cert subject DN field): " + subject);
            println(numConnectedClients + " concurrent connection(s)\n");

            PrintWriter out = null;
            BufferedReader in = null;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            db.establishConnection();
            try {
				dbInput(u, in, out);
			} catch (Exception e) {
				//Client disconnected
        	}
            db.terminateConnection();

			in.close();
			out.close();
			socket.close();
    	    numConnectedClients--;
            println("client disconnected");
            println(numConnectedClients + " concurrent connection(s)\n");
		} catch (IOException e) {
            println("Client died: " + e.getMessage());
            e.printStackTrace();
            return;
        } catch (SQLException e) {
        	println("Database: " + e.getMessage());
			e.printStackTrace();
		}
    }

    private void dbInput(User u, BufferedReader in, PrintWriter out) throws Exception {
    	String command = sendRequest("Enter a command:", in, out);
    	String log = null;
		switch (command.toLowerCase()) {
		case "add":	
			// Only doctor can create new records for a patient provided that the doctor is treating the patient
			if(!(u instanceof Doctor)) {
				sendDeniedPermission(command, in, out);
				break;
			}
	        String patient = sendRequest("Enter patient name", in, out);
			String nurse = sendRequest("Enter name of nurse associated with " + patient, in, out);
			String data = sendRequest("Enter data for " + patient + "'s record", in, out);
			db.createRecord(u, new Patient(null, patient), new Nurse(null, nurse, null), data);
			log = u.getPNbr() + " executed " + command.toUpperCase() + " - Patient: " + patient 
					+ " with nurse: " + nurse + " and data: " + data;
			println(log);
			break;
		case "remove": 
			// Only government agency is allowed to delete records
			if(!(u instanceof Government)) {
				sendDeniedPermission(command, in, out);
				break;
			}
			int recordNbr = Integer.parseInt(sendRequest("Enter record number", in, out));
			db.deleteRecord(u, recordNbr);
			log = u.getPNbr() + " executed " + command.toUpperCase() + " - RecordNbr: " + recordNbr;
			println(log);
			break;
		case "read": 
			// Everyone can read records, assumed they are associated with patient
			List<Record> list = db.getRecords(u);
			sendRequest(list.toString() + " PRESS [ENTER] to continue", in, out);
			log = u.getPNbr() + " executed " + command.toUpperCase();
			break;
		case "edit": 
			// Only Nurse and Doctor is allowed to edit records
			if(!(u instanceof Nurse || u instanceof Doctor)) {
				sendDeniedPermission(command, in, out);
				break;
			}
			recordNbr = Integer.parseInt(sendRequest("Enter record number", in, out));
			String moredata = sendRequest("Enter additional data for record " + recordNbr, in, out);
			db.editRecord(u, recordNbr, moredata);
			log = u.getPNbr() + " executed " + command.toUpperCase() + " - RecordNbr: " + recordNbr 
					+ " with data: " + moredata;
			println(log);
			break;
		default: 
			sendRequest("You didn't use a correct command", in, out);
			break;
		}
    	println("done\n");
    	dbInput(u, in, out);
	}
    
    private String sendRequest(String request, BufferedReader in, PrintWriter out) throws Exception {
    	print("sending '" + request + "' to client...");
        out.println(request);
    	out.flush();
        String clientAns = in.readLine();
        if(clientAns == null)
        	throw new Exception("client disconnected");
        println("received '" + clientAns + "' from client");
        return clientAns;
    }
    
    private void sendDeniedPermission(String command, BufferedReader in, PrintWriter out) throws Exception {
    	sendRequest("You don't have the required permission to execute " + command 
    			+ ". Press [ENTER] to continue", in, out);
    }

	private void newListener() { (new Thread(this)).start(); } // calls run()

    public static void main(String args[]) throws ClassNotFoundException, SQLException {
    	if(args.length == 0) {
    		System.out.print("Port: ");
    		args = new Scanner(System.in).nextLine().split(" ");
    	}
        int port = -1;
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "] = " + args[i]);
        }
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
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
                
                ks.load(new FileInputStream("certificates/server/keystore"), password);  // keystore password (storepass)
                ts.load(new FileInputStream("certificates/server/truststore"), password); // truststore password (storepass)
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
    
    private void print(String text) {
    	if(debug)
    		System.out.print(text);
    }
    
    private void println(String text) {
    	if(debug)
    		System.out.println(text);
    }
}