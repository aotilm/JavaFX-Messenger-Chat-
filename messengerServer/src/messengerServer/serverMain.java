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
    public ArrayList<ClientHandler> clients = new ArrayList<>();
    public int port = 4004;
    public Server() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Сервер запущений!");

            while (true) {
                Socket clientSocket = server.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
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
//    
//    public void sendToClient(String str, ClientHandler client) {
//    	client.sendMessage(str);
//    }
//
//    
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

}

class ClientHandler implements Runnable {
	private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private ObjectInputStream inObject;
    private ObjectOutputStream outObject;

    private Server server;
    private String name;

    public ClientHandler(Socket socket, Server server) {
        try {
        	this.clientSocket = socket;
        	this.server = server;
			this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			this.outObject = new ObjectOutputStream(clientSocket.getOutputStream());
			this.inObject = new ObjectInputStream(clientSocket.getInputStream());
		    System.out.println("Ok");

//			sendMessage("Введіть ваше ім'я");
//			this.name = in.readLine();
//			
//			server.sendToAll("$", this);
//			server.sendToAll(this.name, this);
//			System.out.println(name);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public String getName() {
		return name;
	}

    @Override
    public void run() {
//        Thread currentThread = Thread.currentThread();
//        System.out.println("Створено новий потік для клієнта: " + currentThread.getName());
        System.out.println("Користувачів в спискові: " + server.clients.size());

        try {
//    		server.sendToAll("НОВИЙ КОРИСТУВАЧ ПІД'ЄДНАВСЯ", this);
          		
//        	String message ;
        	Message response;
        	while ((response = (Message) inObject.readObject()) != null) {
//        		String message = response.getMessage();
        		server.sendToAll(response, this);
        		
        		
//        		int spaceIndex = message.indexOf(" ");
//        		name = message.substring(0, spaceIndex);
//        	    System.out.println("Повідомлення Клієнта: " + message);
//        	    server.sendToAll(this.name +": " + message, this);
//        	    server.sendToAll(message, this);

//        	    System.out.println("+++");
//        	    out.flush();
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
            in.close();
            out.close();
            inObject.close();
            outObject.close();
            clientSocket.close();
            server.removeClient(this);
            System.out.println("вроді все закрив " + server.clients.size());
        } catch (IOException e) {
            System.err.println("Помилка під час закриття потоку вводу/виводу: " + e);
        }
    }

    
    public void sendMessage(Message ms) {
        try {
//            out.write(message + "\n");
//            out.flush();
        	outObject.writeObject(ms);
            outObject.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
