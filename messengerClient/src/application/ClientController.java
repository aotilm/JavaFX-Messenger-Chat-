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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tables.Clients;
import tables.Story;

public class ClientController implements Initializable {

    @FXML
    private AnchorPane userPane;

    @FXML
    private Button addButton, btnSend, btnGoChat; 

    @FXML
    private VBox vbox, messagePane; 

    @FXML
    private Label number;
    
    @FXML
    private TextField textMessage, textName;
    
    @FXML
    private ScrollPane scrollPane;
    
    @FXML
    private AnchorPane singInPane;
    
    @FXML
    private BorderPane chatPane;

    public static Socket clientSocket; 
    public static BufferedReader in; 
    public static BufferedWriter out; 
    public static ObjectInputStream inObject;
    public static ObjectOutputStream outObject;

    
  
        
    private String name;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	try{
    		singInPane.toFront();
    		clientSocket = new Socket("localhost", 4004);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));  
            outObject = new ObjectOutputStream(clientSocket.getOutputStream());
            inObject = new ObjectInputStream(clientSocket.getInputStream());
            System.out.println("Ok");
            new ReadMessage().start();             
    	} catch (IOException e) {
            System.err.println(e);
        }
    }
           
    public void addUser(String name) {
    	
    	Platform.runLater(() -> {
    		AnchorPane pane = new AnchorPane();

    	    pane.setPrefWidth(300);
    	    pane.setPrefHeight(70);
    	    pane.getStyleClass().add("custom-pane");

    	    Label l = new Label(name);
    	    l.setStyle("-fx-text-fill: black; -fx-font-size: 16px;");
    	    l.setLayoutY(20);
    	    l.setLayoutX(50);
    	    vbox.getChildren().add(pane); 
    	    pane.getChildren().add(l);
    	});
    	
    }
    
    
    public void userAuthentication() {
    	name = textName.getText();
    	if (checkClientInDB(name)) {
    		chatPane.toFront();
    	}
    	else {
    		clientRegistration();
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
    					Clients client = new Clients(name);
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
//    public void saveMessage() {
//    	String recepient= null;
//    	String sender = null;
//    	int chatId = 0;
//    	
//    	Session session = factory.getCurrentSession();
//		try {
//			Story message = new Story();
//			session.beginTransaction();
//			
//			session.save(message);
//			
//			session.flush();
//			System.out.println("Done!!!");
//			session.getTransaction().commit();
//		} finally { factory.close(); session.close();}
//    }
//    
    public void sendUsersMessage() {
    	createUsersMessage(textMessage.getText());
    }
    
    public void createUsersMessage(String message) {
    	Platform.runLater(() -> {
    		Message ms = new Message(name, null, message);
    		AnchorPane pane = new AnchorPane();
    		messagePane.setMargin(pane , new Insets(5));
    		
    	    pane.setStyle(" -fx-border-radius: 13px;");
    		Label label = new Label();
    		label.setText("Ви: "+ message);
    		label.setStyle(" -fx-padding: 15px; -fx-background-color: #6F6F6F; -fx-background-radius: 25px; -fx-text-fill: white;");
    		label.setMaxWidth(550);
    		label.setWrapText(true);
    	         	
        	messagePane.getChildren().add(pane);
        	pane.setRightAnchor(label, 0.0);
        	pane.getChildren().add(label);
        	textMessage.clear();
        	sendMessage(ms);
        	scrollPane.vvalueProperty().bind(messagePane.heightProperty());

    	});
    	
    }
    

    public void sendMessage(Message ms) {
        	
    		try {
//    			out.write(name + " " + message +"\n");
//                out.flush();
    			outObject.writeObject(ms);;
                outObject.flush();
             } catch (IOException e) {
                 System.err.println(e);
             }
    }
    
    public void createOtherMessage(String message, String senderName) {
        Platform.runLater(() -> {
            AnchorPane pane = new AnchorPane();
            messagePane.setMargin(pane, new Insets(5));

            pane.setStyle(" -fx-border-radius: 13px;");
            Label label = new Label();
            label.setText(senderName +": " + message);
            label.setStyle(" -fx-padding: 15px; -fx-background-color: #D9D9D9; -fx-background-radius: 25px;  ");
            label.setMaxWidth(550);
            label.setWrapText(true);

            pane.getChildren().add(label);
            messagePane.getChildren().add(pane);

        });
    }

    
    private class ReadMessage extends Thread {
        @Override
        public void run() {
            
//            String message = null;
        	
            try {
                while (true) {
//                    message = in.readLine(); 
                	Message response = (Message) inObject.readObject();
//                	createOtherMessage(message, null);
                	String message = response.getMessage();
                	String senderName = response.getSenderName();
                    if  (!message.isEmpty()) {
                       createOtherMessage(message, senderName);
                       
                   }
//                    
                }
            } catch (IOException | ClassNotFoundException  e) {
//                ClientSomthing.this.downService();
            }
        }
    }
    

}
