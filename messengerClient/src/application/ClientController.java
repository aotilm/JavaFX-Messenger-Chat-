package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
//import messengerServer.ClientHandler;
import tables.Clients;
import tables.Story;

public class ClientController implements Initializable {

    @FXML
    private AnchorPane userPane;

    @FXML
    private Button addButton, btnSend, btnGoChat, updateChats; 

    @FXML
    private VBox vbox, messagePane; 

    @FXML
    private Label number;
    
    @FXML
    private TextField textMessage, textName;
    
    @FXML
    private ScrollPane scrollPane, usersListPane;
    
    @FXML
    private AnchorPane singInPane, chooseChatPane;
    
    @FXML
    private BorderPane chatPane;

    public static Socket clientSocket; 
    public static Socket nameSocket; 
    public static ObjectInputStream inObject;
    public static ObjectOutputStream outObject;
    public static BufferedWriter out;
    public ArrayList<Clients> onlineUsers = new ArrayList<>();
    public ArrayList<Message> history = new ArrayList<>();

    private String IP = "localhost";
        
    public static String name;
    public String selectedChat;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
		singInPane.toFront();
    }
    public void importHistory(String sender, String recipient) {
    	  SessionFactory factory = new Configuration()
      			.configure("hibernate.cfg.xml")
      			.addAnnotatedClass(Message.class) 
      			.buildSessionFactory();  
      	Session session = factory.openSession();
          try {
              session.beginTransaction();

              Query<Message> query = session.createQuery("FROM Message WHERE recipientName = ?1 and senderName = ?2 "
              		+ "or recipientName =?2 and senderName = ?1", Message.class);
              query.setParameter(1, recipient);
              query.setParameter(2, sender);

              history.clear();
              history.addAll(query.list());
              for(int i=0; i<history.size(); i++) {
                  Message ms = history.get(i);
                  Date date = ms.getDate();
                  SimpleDateFormat formatter = new SimpleDateFormat("hh:mm EE");
          		  String dateText = formatter.format(date);
          		  
                  if (ms.getSenderName().equals(sender)) {
                	  importUsersMessage(ms.getMessage());
//                	  createOtherMessage(ms.getMessage(), ms.getRecipientName(), dateText();
                  }
                  else {
                	  createOtherMessage(ms.getMessage(), ms.getSenderName(), dateText);
                  }
                  
              }
              System.out.println(history.toString());
              session.getTransaction().commit();
          }finally {
              session.close();
              factory.close();
          }
    }
    public void addUser() {
    	checkOnlineUsers();
    	vbox.getChildren().clear();
    	for (Clients client : onlineUsers) {
    		if(!client.getName().equals(name)) {
    			Platform.runLater(() -> {
    				HBox pane = new HBox();
    				pane.setAlignment(Pos.CENTER_LEFT);
    	    	    pane.setPrefWidth(300);
    	    	    pane.setPrefHeight(70);
    	    	    pane.getStyleClass().add("custom-pane");
    	    	    
    	    	    ImageView avatar = new ImageView(getClass().getResource("/images/avatar.png").toExternalForm());
    	    	    avatar.setFitWidth(50);
    	    	    avatar.setFitHeight(50);    	    	    
    	    	    
    	    	    Label l = new Label(client.getName());
    	    	    l.setStyle("-fx-text-fill: black; -fx-font-size: 16px;");
    	    	    l.setLayoutY(20);
    	    	    l.setLayoutX(50);
    	    	    pane.setMargin(avatar , new Insets(10,10,10,10));
//    	    	    pane.setMargin(l , new Insets(10));
    	    	    
    	    	    vbox.getChildren().add(pane); 
    	    	    pane.getChildren().addAll(avatar, l);	
    	    	    
    	    	    pane.setOnMouseEntered(event -> {
    	    	    	String currentColor = pane.getStyle();
    	    	        if (!currentColor.contains("#777777")) {
    	    	            pane.setStyle("-fx-background-color: #A7A7A7;");
    	    	        }
    	    	    });

    	    	    pane.setOnMouseExited(event -> {
    	    	    	String currentColor = pane.getStyle();
    	    	        if (!currentColor.contains("#777777")) {
    	    	        	pane.setStyle("-fx-background-color: #D9D9D9;");
    	    	        }
    	    	        
    	    	    });
    	    	    
    	    	    pane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
    	    	        if (event.getButton() == MouseButton.PRIMARY) {
    	    	        	for (Node node : vbox.getChildren()) {
    	    	        	    if (node instanceof HBox) {
    	    	        	        HBox hbox = (HBox) node;
    	    	        	        hbox.setStyle("-fx-background-color: #D9D9D9;");
    	    	        	    }
    	    	        	}
    	    	        	
    	    	            for (Node node : pane.getChildren()) {
    	    	                if (node instanceof Label) {
    	    	                	pane.setStyle("-fx-background-color: #777777;");
    	    	                    Label clickedLabel = (Label) node;
    	    	                    selectedChat = clickedLabel.getText();
    	    	                    System.out.println("Вибраний чат: " + selectedChat);
    	    	                    chooseChatPane.toBack();
    	    	                    messagePane.getChildren().clear();
    	    	                    importHistory(name, selectedChat);
    	    	                    break; 
    	    	                }
    	    	            }
    	    	        }
    	    	    });

    	    	});
    		}
    	}    
    }
    
    
    public void userAuthentication() {
    	name = textName.getText();
    	if (checkClientInDB(name)) {
    		updateActiveStatus(name, true);
    		connectToServer();
    		chatPane.toFront();
    		chooseChatPane.toFront();
    		addUser();
    	}
    	else {
    		clientRegistration();
    	}
    }
    
    
    public void connectToServer() {
    	try{
    		nameSocket =  new Socket(IP,4005);
    		out = new BufferedWriter(new OutputStreamWriter(nameSocket.getOutputStream()));
            out.write(name);
            out.flush(); 
            out.close();
            
            nameSocket.close();
            
            clientSocket = new Socket(IP, 4004);
            System.out.println("Ok");
            outObject = new ObjectOutputStream(clientSocket.getOutputStream());
            inObject = new ObjectInputStream(clientSocket.getInputStream());
         
            new ReadMessage().start();    
    	} catch (IOException e) {
            System.err.println(e);
        }
    }
    
    public void clientRegistration() {
    	Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження реєстрації");
        alert.setHeaderText("Ви ще не зарєєстровані. Бажаєте зареєструватись?");
        alert.setHeight(30);
        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == ButtonType.OK) {
        	String name = textName.getText();
    	 	
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
    		    		chatPane.toFront();

    				} finally { factory.close(); session.close();}
        }
    	
    } 
    
    public boolean checkClientInDB(String name) {
    	name = textName.getText();
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
            return result;
        }finally {
            session.close();
            factory.close();
        }
    }
    
    public void checkOnlineUsers() {
    	
        SessionFactory factory = new Configuration()
    			.configure("hibernate.cfg.xml")
    			.addAnnotatedClass(Clients.class) 
    			.buildSessionFactory();  
    	Session session = factory.openSession();
        try {
            session.beginTransaction();

            Query<Clients> query = session.createQuery("FROM Clients ", Clients.class);
//            WHERE activeStatus = true
            onlineUsers.clear();
            onlineUsers.addAll(query.list());
            System.out.println(onlineUsers);
            session.getTransaction().commit();
        }finally {
            session.close();
            factory.close();
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

    public void saveMessage(String sender, String recepient, String message, Date date) {

    	SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Message.class)
                .buildSessionFactory();
    	
    	Session session = factory.getCurrentSession();
		try {
			Message ms = new Message(sender, recepient, message, date);
			session.beginTransaction();
			
			session.save(ms);
			
			session.flush();
			System.out.println("Повідомлення збережено!");
			session.getTransaction().commit();
		} finally { factory.close(); session.close();}
    }
    
    public void sendUsersMessage() {
    	createUsersMessage(textMessage.getText());
    }
    public void importUsersMessage(String message) {
    	Platform.runLater(() -> {
    		Date date = new Date();
    		Message ms = new Message(name, selectedChat, message, date);
    		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm EE");
    		String dateText = formatter.format(date);
    		
    		AnchorPane mainPane = new AnchorPane();
    		VBox pane = new VBox();
    		messagePane.setMargin(mainPane , new Insets(6));
    		pane.setStyle(" -fx-padding: 15px; -fx-background-color: #6F6F6F; -fx-background-radius: 25px; ");
    		Label messageLbl = new Label();
    		messageLbl.setText("Ви: "+ message );
    		messageLbl.setStyle("-fx-text-fill: white;");

    		messageLbl.setMaxWidth(550);
    		messageLbl.setWrapText(true);
    		
    		Label dateLbl = new Label(dateText);
    		dateLbl.setFont(Font.font(10));
    		dateLbl.setStyle("-fx-text-fill: white;");
    		
    		
        	messagePane.getChildren().addAll(mainPane);
        	mainPane.getChildren().addAll(pane);
        	mainPane.setRightAnchor(pane, 0.0);
        	pane.setAlignment(Pos.CENTER_RIGHT);

        	pane.getChildren().addAll(messageLbl, dateLbl);
        	textMessage.clear();
//        	sendMessage(ms);
        	scrollPane.vvalueProperty().bind(messagePane.heightProperty());
//        	saveMessage(name, selectedChat, message, date);

    	});
    	
    }
    
    public void createUsersMessage(String message) {
    	Platform.runLater(() -> {
    		Date date = new Date();
    		Message ms = new Message(name, selectedChat, message, date);
    		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm EE");
    		String dateText = formatter.format(date);
    		
    		AnchorPane mainPane = new AnchorPane();
    		VBox pane = new VBox();
    		messagePane.setMargin(mainPane , new Insets(6));
    		pane.setStyle(" -fx-padding: 15px; -fx-background-color: #6F6F6F; -fx-background-radius: 25px; ");
    		Label messageLbl = new Label();
    		messageLbl.setText("Ви: "+ message );
    		messageLbl.setStyle("-fx-text-fill: white;");

    		messageLbl.setMaxWidth(550);
    		messageLbl.setWrapText(true);
    		
    		Label dateLbl = new Label(dateText);
    		dateLbl.setFont(Font.font(10));
    		dateLbl.setStyle("-fx-text-fill: white;");
    		
    		
        	messagePane.getChildren().addAll(mainPane);
        	mainPane.getChildren().addAll(pane);
        	mainPane.setRightAnchor(pane, 0.0);
        	pane.setAlignment(Pos.CENTER_RIGHT);

        	pane.getChildren().addAll(messageLbl, dateLbl);
        	textMessage.clear();
        	sendMessage(ms);
        	scrollPane.vvalueProperty().bind(messagePane.heightProperty());
        	saveMessage(name, selectedChat, message, date);

    	});
    	
    }
    
    public void drawUserMessage() {
    	
    }
    

    public void sendMessage(Message ms) {
        	
    		try {
    			outObject.writeObject(ms);;
                outObject.flush();
             } catch (IOException e) {
                 System.err.println(e);
             }
    }
    
    public void createOtherMessage(String message, String senderName, String date) {
        Platform.runLater(() -> {
            AnchorPane mainPane = new AnchorPane();
    		VBox pane = new VBox();

            messagePane.setMargin(mainPane, new Insets(6));

            pane.setStyle(" -fx-padding: 15px; -fx-background-color: #D9D9D9; -fx-background-radius: 25px; ");
    		Label messageLbl = new Label();
    		messageLbl.setText(senderName+ ": " + message );

    		messageLbl.setMaxWidth(550);
    		messageLbl.setWrapText(true);
    		
    		Label dateLbl = new Label(date);
    		dateLbl.setFont(Font.font(10));
    		
        	messagePane.getChildren().addAll(mainPane);
        	mainPane.getChildren().addAll(pane);
        	pane.setAlignment(Pos.CENTER_RIGHT);

        	pane.getChildren().addAll(messageLbl, dateLbl);
        	scrollPane.vvalueProperty().bind(messagePane.heightProperty());

        });
    }

    
    private class ReadMessage extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                	Message response = (Message) inObject.readObject();
                	String message = response.getMessage();
                	String senderName = response.getSenderName();
                	
                	Date date = response.getDate();
            		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm EE");
            		String dateText = formatter.format(date);
                	
                    if  (!message.isEmpty()) {
                       createOtherMessage(message, senderName, dateText);
                       
                   }
                }
            } catch (IOException | ClassNotFoundException  e) {
            	System.out.println(e);
            }
        }
    }
    

}
