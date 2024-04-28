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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import application.EnterOrReg;
import application.Message;
import tables.Clients;



public class serverMain {
    
    public static void main(String[] args) {
        Server server = new Server();
    }

}

class Server {
    public static ArrayList<ClientHandler> clients = new ArrayList<>();
    public Server() {
        try  {
        	ServerSocket enterServerSocket = new ServerSocket(4005);
        	ServerSocket messageServerSocket = new ServerSocket(4004);
            Socket enterSocket;
            Socket messageSocket;

            System.out.println("Сервер запущений!");

            while (true) {
            	enterSocket = enterServerSocket.accept();
            	messageSocket = messageServerSocket.accept();
                ClientHandler client = new ClientHandler(enterSocket, this, messageSocket);
                new Thread(client).start();
            }
        } catch (IOException e) {
            System.err.println(e);
            
        }
    }

    
    
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
    
    public void addClient(ClientHandler client) {
        clients.add(client);
		System.out.println("Кількість клієнтів у чаті: " + clients.size());
    }
    
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

}

class ClientHandler implements Runnable {
	private Server server;
	
	private Socket enterSocket;
	private Socket messageSocket;
	
    private ObjectInputStream enterIn;
    private ObjectInputStream messageIn;

    private ObjectOutputStream enterOut;
    private ObjectOutputStream messageOut;

    ImportHistoryReader importReader;
    public String name;

    public ArrayList<Clients> users = new ArrayList<>();
    public ArrayList<Message> history = new ArrayList<>();


    


    public ClientHandler(Socket socket, Server server, Socket msgSocket) {
        try {
        	this.enterSocket = socket;
        	this.server = server;
			this.enterOut = new ObjectOutputStream(enterSocket.getOutputStream());
			this.enterIn = new ObjectInputStream(enterSocket.getInputStream());
			
			this.messageSocket = msgSocket;
			this.messageIn = new ObjectInputStream(messageSocket.getInputStream());
			this.messageOut = new ObjectOutputStream(messageSocket.getOutputStream());
		    System.out.println("Конструктор відпрацював");

		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    

	@Override
    public void run() {
        try {
        	EnterOrReg enterResponse;
        	while ((enterResponse = (EnterOrReg) enterIn.readObject()) != null) {
    			name = enterResponse.getName();
        		if(!enterResponse.isType()) {
        			if(clientEnter(name)) {
        				System.out.println("GODDAMN IT WORKS. " +  name + " JUST ENTERED IN THE CHAT");
                		server.addClient(this);
                		selectAllUser();
                		break;
        			}           		
        		}
        		
        		else if(enterResponse.isType()) { 
        			
        			clientRegistration(name);
            		System.out.println("GODDAMN IT WORKS. " +  name + " JUST REGISTERED IN THE CHAT");
            		server.addClient(this);
            		selectAllUser();

            		break;
        		}
        		
        		else {
        			break;
        		}
        	}


//        	new ImportHistoryReader().start();
        	importReader = new ImportHistoryReader();
        	importReader.start();
        	updateActiveStatus(name, true);

        	Message response;
        	while ((response = (Message) messageIn.readObject()) != null) {
        		System.out.println(4);
        		if(response.isFileType()) {
        			System.out.println("Отримання файлу");
        			Message file = (Message) messageIn.readObject();
        	        server.sendToChat(file,response, this);
        	        System.out.println("Файл переслано клієнту!");

        		}
        		else {
        			Message sg = new Message(false);
            		server.sendToChat(response, sg, this);
            		saveMessage(response);
            		System.out.println(response.getMessage());

        		}
        	}
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("помилка в методі run "+ e);
        } finally {
        	updateActiveStatus(name, false);
            importReader.stop();
        	close();
        	System.out.println("---");
        }
        	 
   }
	
    public void checkIfUserIsOnline(String selectedChat) {
   	 	SessionFactory factory = new Configuration()
    			.configure("hibernate.cfg.xml")
    			.addAnnotatedClass(Clients.class) 
    			.buildSessionFactory();  
    	Session session = factory.openSession();
        try {
            session.beginTransaction();

            Query<Clients> query = session.createQuery("FROM Clients WHERE activeStatus = true AND name = ?1", Clients.class);
            query.setParameter(1, selectedChat);
            boolean result = !query.list().isEmpty();
            
            session.getTransaction().commit();
            
            try {
				enterOut.writeObject(result);
				enterOut.flush();
				System.out.println(selectedChat + result);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }finally {
            session.close();
            factory.close();
        }
   }
    
    public void importHistory(Message ms) {
    	SessionFactory factory = new Configuration()
     			.configure("hibernate.cfg.xml")
     			.addAnnotatedClass(Message.class) 
     			.buildSessionFactory();  
     	Session session = factory.openSession();
         try {
             session.beginTransaction();

             Query<Message> query = session.createQuery("FROM Message WHERE recipientName = ?1 and senderName = ?2 "
             		+ "or recipientName =?2 and senderName = ?1", Message.class);
             query.setParameter(1, ms.getRecipientName());
             query.setParameter(2, ms.getSenderName());

             history.clear();
             history.addAll(query.list());
           
             session.getTransaction().commit();
         }finally {
             session.close();
             factory.close();
         }
         
         try {
			enterOut.writeObject(history.size());
			enterOut.flush();
	         
	        for(int i=0; i<history.size(); i++) {
	        	 Message hs = history.get(i);
	        	 enterOut.writeObject(hs);
	        	 enterOut.flush();
	         }
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }

   public static void updateActiveStatus(String name, boolean activeStatus) {
   	SessionFactory factory = new Configuration()
               .configure("hibernate.cfg.xml")
               .addAnnotatedClass(Clients.class)
               .buildSessionFactory();
            Session session = factory.getCurrentSession();
       try  {

           session.beginTransaction();

           Query<Clients> query = session.createQuery("UPDATE Clients SET activeStatus = ?1 WHERE name = ?2");
           query.setParameter(1, activeStatus);
           query.setParameter(2, name);
        
           query.executeUpdate();
           session.getTransaction().commit();
       } catch (Exception e) {
       	session.close();
           factory.close();
           e.printStackTrace();
       }finally {
       	session.close();
         factory.close();
       }
   }

    public void saveMessage(Message ms) {
    	SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Message.class)
                .buildSessionFactory();
    	
    	Session session = factory.getCurrentSession();
		try {
			session.beginTransaction();
			
			session.save(ms);
			
			session.flush();
			System.out.println("Повідомлення збережено!");
			session.getTransaction().commit();
		} finally { factory.close(); session.close();}
    }
    
	public void selectAllUser() {
    	try {

			if(enterIn.readObject().equals("SelectUsers")) {
				SessionFactory factory = new Configuration()
		    			.configure("hibernate.cfg.xml")
		    			.addAnnotatedClass(Clients.class) 
		    			.buildSessionFactory();  
		    	Session session = factory.openSession();
		        try {
		            session.beginTransaction();

		            Query<Clients> query = session.createQuery("FROM Clients ", Clients.class);
		            users.clear();
		            users.addAll(query.list());
		            System.out.println(users);
		            session.getTransaction().commit();
		        }finally {
		            session.close();
		            factory.close();
		        }
		        enterOut.writeObject(users.size());
				enterOut.flush();

		        for(int i=0; i<users.size(); i++) {
		        	Clients client = users.get(i);
		        	enterOut.writeObject(client);
					enterOut.flush();
		        }
			       System.out.println("метод імпорту клієнтів");

			}
		} catch (IOException | HibernateException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
    	
        
    }
	
	public boolean clientEnter(String name) {
		SessionFactory factory = new Configuration()
    			.configure("hibernate.cfg.xml")
    			.addAnnotatedClass(Clients.class) 
    			.buildSessionFactory();  
    	Session session = factory.openSession();
        try {
            session.beginTransaction();

            Query<Clients> query = session.createQuery("FROM Clients WHERE name = ?1", Clients.class);
            query.setParameter(1, name);
            
            boolean result = !query.list().isEmpty();
    
            session.getTransaction().commit();
			try {
	            enterOut.writeObject(result);
				enterOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
        } finally {
            session.close();
            factory.close();
        }
	}

	
    public void clientRegistration(String name) {
    	SessionFactory factory = new Configuration()
				.configure("hibernate.cfg.xml")
				.addAnnotatedClass(Clients.class) 
				.buildSessionFactory();
		
				
				Session session = factory.getCurrentSession();
				try {
					Clients client = new Clients(name, true);
					session.beginTransaction();
					
					session.save(client);
					
					session.flush();
					System.out.println("Done!!!");
					session.getTransaction().commit();
					
					enterOut.writeObject(true);
					enterOut.flush();

				} catch (IOException e) {
					e.printStackTrace();
				} finally { factory.close(); session.close();}
    	
    } 
	
    public void close() {
    	try {
    		
            enterIn.close();
            enterOut.close();
            enterSocket.close();
                        
            messageIn.close();
            messageOut.close();
            messageSocket.close();
            server.removeClient(this);
            System.out.println("З'єднання закрито для: " + this.name + "Залишок клієнтів: " + server.clients.size() ); 
        } catch (IOException e) {
            System.err.println("Помилка під час закриття потоку вводу/виводу: " + e);
        }
    }

    
    public void sendMessage(Message ms) {
        try {
        	messageOut.writeObject(ms);
        	messageOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public String getName() {
		return name;
	}
    
    class ImportHistoryReader extends Thread{
    	 @Override
         public void run() {
    		 try {
    			 Message ms;
        		 while((ms = (Message) enterIn.readObject()) != null) {
        			 if(ms.getMessage().equals("ImportHistory")) {
        				 Message rs = (Message) enterIn.readObject();
        				 importHistory(rs);
        				 Message rss = (Message) enterIn.readObject();
        				 checkIfUserIsOnline(rss.getMessage());
        			 }
        			 
//        			 if(ms.getMessage().equals("SelectStatus")) {
//        				 System.out.println(7);
//        				 Message rs = (Message) enterIn.readObject();
//        				 System.out.println(8);
//        				 checkIfUserIsOnline(rs.getRecipientName());
//        				 System.out.println(9);
//        			 }
    				 
        		 	}
				} catch (ClassNotFoundException | IOException e) {
					close();
					e.printStackTrace();
				}
    	 }
    }
}
