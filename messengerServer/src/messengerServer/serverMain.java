package messengerServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ClientInfoStatus;
import java.util.ArrayList;

import application.Message;



public class serverMain {
    
    public static void main(String[] args) {
        Server server = new Server();
    }

}

class Server {
    public static ArrayList<ClientHandler> clients = new ArrayList<>();
    public int port = 4004;
    private BufferedReader in;
    private BufferedWriter out;
    public Server() {
        try  {
        	ServerSocket server = new ServerSocket(port);
        	ServerSocket serverName = new ServerSocket(4005);
            System.out.println("Сервер запущений!");
            Socket nameSocket;
            Socket clientSocket;
            while (true) {
            	nameSocket = serverName.accept();
                in = new BufferedReader(new InputStreamReader(nameSocket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(nameSocket.getOutputStream()));

                String name = in.readLine();
                System.out.println("ім'я отримано!");
                nameSocket.close();
            	
                clientSocket = server.accept();
                ClientHandler client = new ClientHandler(clientSocket, this, name);
                clients.add(client);

                System.out.println(clients.size());
                new Thread(client).start();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    
    
    public void sendToAll(Message ms, ClientHandler currentClient) {
        for (ClientHandler client : clients) {
            if (client != currentClient) {
                client.sendMessage(ms);
            }
        }
    }
    
    public void sendToChat(Message ms, ClientHandler currentClient) {
    	Message inMess = ms;
    	String recipient = inMess.getRecipientName();
    	for (ClientHandler client : clients) {
            if (client.getName().equals(recipient)) {
                client.sendMessage(ms);
            }
        }
    	
    }
    
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

}

class ClientHandler implements Runnable {
	private Socket clientSocket;
//    private BufferedReader in;
//    private BufferedWriter out;
    private ObjectInputStream inObject;
    private ObjectOutputStream outObject;
    public String name;

    private Server server;
    private Socket status;

    public ClientHandler(Socket socket, Server server, String name) {
        try {
        	this.clientSocket = socket;
        	this.server = server;
			this.outObject = new ObjectOutputStream(clientSocket.getOutputStream());
			this.inObject = new ObjectInputStream(clientSocket.getInputStream());
			this.name = name;
		    System.out.println("Ok");

		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    

	@Override
    public void run() {
        System.out.println("Користувачів в спискові: " + server.clients.size());
        try {
        	Message response;
        	while ((response = (Message) inObject.readObject()) != null) {
        		server.sendToChat(response, this);
        		
        	}
        
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e);
        } finally {
        	close();
        	System.out.println("---");
        }
        	
        }
//    }
    
    public void close() {
    	try {
            inObject.close();
            outObject.close();
            clientSocket.close();
            server.removeClient(this);
            System.out.println("вроді все закрив " + server.clients.size() + this.name); 
        } catch (IOException e) {
            System.err.println("Помилка під час закриття потоку вводу/виводу: " + e);
        }
    }

    
    public void sendMessage(Message ms) {
        try {
        	outObject.writeObject(ms);
            outObject.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public  String getName() {
		return name; 
	}
}
