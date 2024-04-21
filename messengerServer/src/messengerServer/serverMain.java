package messengerServer;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.ClientInfoStatus;
import java.util.ArrayList;
import java.util.Base64;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

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
                
                String name = in.readLine();
                System.out.println("ім'я отримано!");
                in.close();
                nameSocket.close();

                clientSocket = server.accept();
                
//                statusSocket = serverStatus.accept();
//                System.out.println("Ok3");

                ClientHandler client = new ClientHandler(clientSocket, this, name);
                clients.add(client);

                System.out.println("Користувачів в спискові: " + clients.size());
                new Thread(client).start();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    
    
//    public void sendToAll(Message ms) {
//        for (ClientHandler client : clients) {
//            client.sendStatus(ms);
//
//        }
//    }
    
    public void sendToChat(Message ms, Message signal, ClientHandler currentClient) {
    	Message inMess = ms;

    	Message inSignal = signal;

    	String recipient = inMess.getRecipientName();

    	if (inSignal.isFileType()) {

    		for (ClientHandler client : clients) {
                if (client.getName().equals(recipient)) {
                    client.sendMessage(signal);
                    client.sendMessage(ms);
                }
            }

    	}
    	
    	else {
    		for (ClientHandler client : clients) {
                if (client.getName().equals(recipient)) {
                    client.sendMessage(ms);
                }
            }

    	}
    	
    	
    }
    
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

}

class ClientHandler implements Runnable {
	private Server server;
	private Socket clientSocket;

    private ObjectInputStream inObject;
//    private ObjectInputStream inStatus;

    private ObjectOutputStream outObject;
//    private ObjectOutputStream outStatus;

    
    public String name;

    


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
        try {
        	Message response;
        	while ((response = (Message) inObject.readObject()) != null) {
        		if(response.isFileType()) {
        			System.out.println("Отримання файлу");
        			Message file = (Message) inObject.readObject();
        	        server.sendToChat(file,response, this);
        	        System.out.println("Файл переслано клієнту!");

        		}
        		else {
        			Message sg = new Message(false);
            		server.sendToChat(response, sg, this);

        		}
        		
//        		server.sendToChat(response, this);
        		
        	}
//        	Message status = (Message) inStatus.readObject();
//        	if(status.getMessage().equals("active")) {
//        		server.sendToAll(status);
//        		while ((response = (Message) inObject.readObject()) != null) {
//            		server.sendToChat(response, this);
//            		
//            	}
//        	}
        	
        
//        
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("помилка в методі run "+ e);
        } finally {
//        	Message status = new Message(name, null, "inactive", null);
//    		server.sendToAll(status);
        	close();
        	System.out.println("---");
        }
        	
   }
    


	
    public void close() {
    	try {
//    		inStatus.close();
//    		outStatus.close();
//    		statusSocket.close();
            inObject.close();
            outObject.close();
            clientSocket.close();
            server.removeClient(this);
            System.out.println("З'єднання закрито для: " + this.name + "Залишок клієнтів: " + server.clients.size() ); 
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
//
//    public void sendStatus(Message ms) {
//        try {
//        	outStatus.writeObject(ms);
//            outStatus.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
	public String getName() {
		return name;
	}
    
    
}
