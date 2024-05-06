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
import java.util.List;
import java.util.Optional;
import java.io.ByteArrayInputStream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.imageio.ImageIO;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import application.EnterOrReg;
import application.Message;
import application.ImportedHist;
import tables.Clients;
import tables.Files;
import tables.History;
import tables.Text;



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
        	ServerSocket statusServerSocket = new ServerSocket(4003); 
            Socket serviceSocket;
            Socket messageSocket;
            Socket statusSocket;

            System.out.println("Сервер запущений!");
            ActiveStatusReader reader = new ActiveStatusReader(this);
            new Thread(reader).start();

            while (true) {
            	try  {
            		serviceSocket = enterServerSocket.accept();
                	messageSocket = messageServerSocket.accept();
                	statusSocket = statusServerSocket.accept();
                	
                    ClientHandler client = new ClientHandler(serviceSocket, this, messageSocket, statusSocket);
                    new Thread(client).start();
            	}catch (IOException e) {
                    System.err.println(e);
                    
                }
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
    
    public void sendStatusToAll(int size) {
    	for (ClientHandler client : clients) {
            client.sendActiveSize(size);
        }
    	
    	for (ClientHandler client : clients) {
    		for(int i = 0; i<ActiveStatusReader.activeUsers.size(); i++) {
        		Clients activeClient = ActiveStatusReader.activeUsers.get(i); 
                client.sendActiveList(activeClient);;
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
	
	private Socket serviceSocket;
	private Socket messageSocket;
	
	
    private ObjectInputStream serviceIn;
    private ObjectInputStream messageIn;

    private ObjectOutputStream serviceOut;
    private ObjectOutputStream messageOut;
    
    private Socket statusSocket;
    private ObjectOutputStream statusOut;

    public String name;

    public ArrayList<Clients> users = new ArrayList<>();
    public List<ImportedHist> importedHistList = new ArrayList<>();

    ImportHistoryReader importReader;
    ActiveStatusReader statusReader;
    

    public ClientHandler(Socket socket, Server server, Socket msgSocket, Socket stSocket) {
        try {
        	this.serviceSocket = socket;
        	this.server = server;
			this.serviceOut = new ObjectOutputStream(serviceSocket.getOutputStream());
			this.serviceIn = new ObjectInputStream(serviceSocket.getInputStream());
			
			this.messageSocket = msgSocket;
			this.messageIn = new ObjectInputStream(messageSocket.getInputStream());
			this.messageOut = new ObjectOutputStream(messageSocket.getOutputStream());
			
			this.statusSocket = stSocket;
			this.statusOut = new ObjectOutputStream(statusSocket.getOutputStream());
			
			
		    System.out.println("Конструктор відпрацював");

		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    

	@Override
    public void run() {
        try {
        	EnterOrReg enterResponse;
        	while ((enterResponse = (EnterOrReg) serviceIn.readObject()) != null) {
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
            		saveMessage(file);
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
				serviceOut.writeObject(result);
				serviceOut.flush();
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
    	            .addAnnotatedClass(History.class)
    	            .addAnnotatedClass(Text.class)
    	            .addAnnotatedClass(Files.class)
    	            .buildSessionFactory();

    	    Session historySession = factory.openSession();
    	    try {
    	        historySession.beginTransaction();

    	        Query<Object[]> query = historySession.createQuery(
    	        		"SELECT h.id, h.date, h.recipientName, h.senderName, t.message, f.fileType, f.file " +
    	                "FROM History h " +
    	                "LEFT JOIN h.text t " +
    	                "LEFT JOIN h.files f " +
    	                "WHERE (h.recipientName = ?1 AND h.senderName = ?2) OR " +
    	                "(h.recipientName = ?2 AND h.senderName = ?1)");
    	        query.setParameter(1, ms.getRecipientName());
    	        query.setParameter(2, ms.getSenderName());
    	        
    	        List<Object[]> result = query.list();
    	        importedHistList.clear();
    	        for (Object[] row : result) {
    	            ImportedHist importedHist = new ImportedHist();
    	            importedHist.setId((Integer) row[0]);
    	            importedHist.setDate((Date) row[1]);
    	            importedHist.setRecipientName((String) row[2]);
    	            importedHist.setSenderName((String) row[3]);
    	            importedHist.setMessage((String) row[4]);
    	            importedHist.setFileType((String) row[5]);
    	            importedHist.setFile((byte[]) row[6]);
    	            importedHistList.add(importedHist);
    	        }

    	        System.out.println(importedHistList.toString());
    	        historySession.getTransaction().commit();
         }finally {
        	 historySession.close();
        	 factory.close();
         }
         
         try {
			serviceOut.writeObject(importedHistList.size());
			serviceOut.flush();
	         
			 for (ImportedHist hs : importedHistList) {
				 	Message hms = new Message(hs.getSenderName(), hs.getRecipientName(), hs.getMessage(), hs.getFile(), hs.getFileType(), hs.getDate());
				 	serviceOut.writeObject(hms);
		            serviceOut.flush();
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
	    SessionFactory sessionFactory = new Configuration()
	            .configure("hibernate.cfg.xml")
	            .addAnnotatedClass(History.class)
	            .addAnnotatedClass(Text.class)
	            .addAnnotatedClass(Files.class)
	            .buildSessionFactory();

	    Session session = sessionFactory.getCurrentSession();

	    try {
	        session.beginTransaction();

	        if (!ms.isFileType()) {
	            Text message = new Text(ms.getMessage());
	            session.save(message);

	            History history = new History(message, null, ms.getDate(), ms.getRecipientName(), ms.getSenderName());

	            session.save(history);
	        } else {
	            Files file = new Files(ms.getImageType(), ms.getImageArray());
	            session.save(file);

	            History history = new History(null, file, ms.getDate(), ms.getRecipientName(), ms.getSenderName());
	
	            session.save(history);
	        }

	        session.getTransaction().commit();
	        System.out.println("Повідомлення збережено!");
	    } finally {
	        sessionFactory.close();
	    }
	}

    
	public void selectAllUser() {
    	try {

			if(serviceIn.readObject().equals("SelectUsers")) {
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
		        serviceOut.writeObject(users.size());
				serviceOut.flush();

		        for(int i=0; i<users.size(); i++) {
		        	Clients client = users.get(i);
		        	serviceOut.writeObject(client);
					serviceOut.flush();
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
	            serviceOut.writeObject(result);
				serviceOut.flush();
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
					
					serviceOut.writeObject(true);
					serviceOut.flush();

				} catch (IOException e) {
					e.printStackTrace();
				} finally { factory.close(); session.close();}
    	
    } 
	
    public void close() {
    	try {
    		
            serviceIn.close();
            serviceOut.close();
            serviceSocket.close();
                        
            messageIn.close();
            messageOut.close();
            messageSocket.close();
            server.removeClient(this);
            System.out.println("З'єднання закрито для: " + this.name + "Залишок клієнтів: " + server.clients.size() ); 
        } catch (IOException e) {
            System.err.println("Помилка під час закриття потоку вводу/виводу: " + e);
        }
    }

	 public void sendActiveSize(int size) {
         try {
         	statusOut.writeObject(size);
         	statusOut.flush();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     public void sendActiveList(Clients activeClient) {
         try {
         	statusOut.writeObject(activeClient);
         	statusOut.flush();
         } catch (IOException e) {
             e.printStackTrace();
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
        		 while((ms = (Message) serviceIn.readObject()) != null) {
        			 if(ms.getMessage().equals("ImportHistory")) {
        				 Message rs = (Message) serviceIn.readObject();
        				 importHistory(rs);
        				 Message rss = (Message) serviceIn.readObject();
        				 checkIfUserIsOnline(rss.getMessage());
        			 }
    				 
        		 	}
				} catch (ClassNotFoundException | IOException e) {
					close();
					e.printStackTrace();
				}
    	 }
    }
}


class ActiveStatusReader implements Runnable{
    public static ArrayList<Clients> activeUsers = new ArrayList<>();

   
    private Server server;
    
    public ActiveStatusReader(Server server) {
		this.server = server;
    }
    
  	 @Override
       public void run() {
  		 while(true) {
  			try {
  				selectActiveUsers();
  	   			server.sendStatusToAll(activeUsers.size());
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
  		 }
  	 }
  	 
  	 
 	public void selectActiveUsers() {
		SessionFactory factory = new Configuration()
    			.configure("hibernate.cfg.xml")
    			.addAnnotatedClass(Clients.class) 
    			.buildSessionFactory();  
    	Session session = factory.openSession();
        try {
            session.beginTransaction();

            Query<Clients> query = session.createQuery("FROM Clients WHERE activeStatus = true", Clients.class);
            activeUsers.clear();
            activeUsers.addAll(query.list());
            session.getTransaction().commit();
        }finally {
            session.close();
            factory.close();
        }
    }
  }
