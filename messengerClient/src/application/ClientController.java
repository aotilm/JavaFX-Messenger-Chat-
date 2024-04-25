package application;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
//import messengerServer.ClientHandler;
import tables.Clients;
import tables.Story;

public class ClientController implements Initializable {

    @FXML
    private AnchorPane userPane;

    @FXML
    private Button addButton, btnSend, btnGoChat, updateChats, update; 

    @FXML
    private VBox vbox, messagePane; 

    @FXML
    private Label number, chatStatusLabel, chatNameLabel;
    
    @FXML
    private TextField textMessage, textName;
    
    @FXML
    private ScrollPane scrollPane;
    
    @FXML
    private AnchorPane singInPane, chooseChatPane;
    
    @FXML
    private BorderPane chatPane;
    
    @FXML
    private ImageView pinFile;

    public static Socket clientSocket; 
    public static Socket nameSocket; 

    public static ObjectInputStream inObject;
    public static ObjectOutputStream outObject;
    
    public static BufferedWriter out;
    
    public static BufferedImage image;



    public ArrayList<Clients> users = new ArrayList<>();
    public ArrayList<Message> history = new ArrayList<>();
    public ArrayList<Message> activeUsers = new ArrayList<>();

    private String IP = "localhost";
        
    public static String name;
    public String selectedChat;
    private File selectedImageFile;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
		singInPane.toFront();
		textMessage.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                	createUsersMessage(textMessage.getText());
                }
            }
        });
    }
//    перевіряю 
  
    
    public void sendImage() {
    	 try {
	            FileChooser fileChooser = new FileChooser();
	            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.gif")); 

	            selectedImageFile = fileChooser.showOpenDialog(null);
	            BufferedImage image = ImageIO.read(selectedImageFile);

	            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	            
	            String imageType = selectedImageFile.getName().substring(selectedImageFile.getName().lastIndexOf(".") + 1); 
	            String imageName = selectedImageFile.getName().substring(0, selectedImageFile.getName().lastIndexOf("."));

	            ImageIO.write(image, imageType, byteArrayOutputStream);

	            int size = byteArrayOutputStream.size();

	            Date date = new Date();

	            String sizeString = Integer.toString(byteArrayOutputStream.size());
	            String imageDataString = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
	            
	            byte[] imageArray = byteArrayOutputStream.toByteArray();

	            Message imageMsg = new Message(name, selectedChat, size, imageArray, imageType, imageName, date, true);
	            Message signal = new Message(true);	
	         		
	            sendMessage(signal);
	            sendMessage(imageMsg);

	            
	            System.out.println("Flushed: " + System.currentTimeMillis());

	            System.out.println("Closing: " + System.currentTimeMillis());
	            drawUserImageMessage(imageArray);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
    }

    public void drawUserImageMessage(byte[] imageArray) {
    	Image image = new Image(new ByteArrayInputStream(imageArray));
        VBox pane = new VBox();
		messagePane.setMargin(pane, new Insets(6,10,10,10));
		
		ImageView img = new ImageView(image);
		img.setFitWidth(400);
		img.setFitHeight(400);
		img.setPreserveRatio(true);

    	pane.setAlignment(Pos.CENTER_RIGHT);

		messagePane.getChildren().add(pane);
		pane.getChildren().add(img);
    }
    
    public void drawImageMessage(byte[] imageArray) {
    	Platform.runLater(() -> {
    		Image image = new Image(new ByteArrayInputStream(imageArray));
            VBox pane = new VBox();
    		messagePane.setMargin(pane, new Insets(6,10,10,10));
    		
    		ImageView img = new ImageView(image);
    		img.setFitWidth(400);
    		img.setFitHeight(400);
    		img.setPreserveRatio(true);

    		messagePane.getChildren().add(pane);
    		pane.getChildren().add(img);
    	});
    	
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
                	  importUsersMessage(ms.getMessage(), ms.getDate(), ms.isFileType());
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
    	checkUser();
    	vbox.getChildren().clear();
    	vbox.setStyle("-fx-background-color: #2F3135");
    	for (Clients client : users) {
    		if(!client.getName().equals(name)) {
    			Platform.runLater(() -> {
    				HBox pane = new HBox();
    				vbox.setMargin(pane, new Insets(5, 10, 5, 10));

    				pane.setAlignment(Pos.CENTER_LEFT);
    	    	    pane.setPrefWidth(300);
    	    	    pane.setPrefHeight(70);
    	    	    pane.getStyleClass().add("custom-pane");
    	    	    
    	    	    ImageView avatar = new ImageView(getClass().getResource("/images/avatar.png").toExternalForm());
    	    	    avatar.setFitWidth(50);
    	    	    avatar.setFitHeight(50);    	    	    
    	    	    
    	    	    Label l = new Label(client.getName());
    	    	    l.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
    	    	    l.setLayoutY(20);
    	    	    l.setLayoutX(50);
    	    	    pane.setMargin(avatar , new Insets(10));
    	    	    
    	    	    vbox.getChildren().add(pane); 
    	    	    pane.getChildren().addAll(avatar, l);	
    	    	        	    	    
    	    	    pane.setOnMouseEntered(event -> {
    	    	    	String currentColor = pane.getStyle();
    	    	        if (!currentColor.contains("#504C4A")) {
    	    	            pane.setStyle("-fx-background-color: #5A5552;");
    	    	        }
    	    	    });

    	    	    pane.setOnMouseExited(event -> {
    	    	    	String currentColor = pane.getStyle();
    	    	        if (!currentColor.contains("#504C4A")) {
    	    	        	pane.setStyle("-fx-background-color: #6E7888;");
    	    	        }
    	    	        
    	    	    });
    	    	    
    	    	    pane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
    	    	        if (event.getButton() == MouseButton.PRIMARY) {
    	    	        	for (Node node : vbox.getChildren()) {
    	    	        	    if (node instanceof HBox) {
    	    	        	        HBox hbox = (HBox) node;
    	    	        	        hbox.setStyle("-fx-background-color: #6E7888;");
    	    	        	    }
    	    	        	}
    	    	        	
    	    	            for (Node node : pane.getChildren()) {
    	    	                if (node instanceof Label) {
    	    	                	pane.setStyle("-fx-background-color: #504C4A;");
    	    	                    Label clickedLabel = (Label) node;
    	    	                    selectedChat = clickedLabel.getText();
    	    	                    System.out.println("Вибраний чат: " + selectedChat);
    	    	                    chooseChatPane.toBack();
    	    	                    chatNameLabel.setText(selectedChat);
    	    	                    
    	    	                    messagePane.getChildren().clear();
    	    	                    importHistory(name, selectedChat);
    	    	                    
    	    	                    if(checkIfUserIsOnline()) {
        	    	                    chatStatusLabel.setText("У мережі");
    	    	                    }
    	    	                    else {
        	    	                    chatStatusLabel.setText("Не в мережі");

    	    	                    }
    	    	                    
    	    	                    break; 
    	    	                }
    	    	            }
    	    	        }
    	    	    });

    	    	});
    		}
    	}    
    }
    
    public boolean checkIfUserIsOnline() {
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
             
             return result;
         }finally {
             session.close();
             factory.close();
         }
    }
    
    public void userAuthentication() {
    	name = textName.getText();
    	if (checkClientInDB(name)) {
    		updateActiveStatus(name, true);
    		connectToServer();
    		new animatefx.animation.FadeOutDown(singInPane).setSpeed(2.5).play();
    		new animatefx.animation.FadeInDown(chatPane).setSpeed(2.5).play();
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
//            System.out.println("Ok1");

            
            clientSocket = new Socket(IP, 4004);
//            System.out.println("Ok2");
            outObject = new ObjectOutputStream(clientSocket.getOutputStream());
//            System.out.println("Ok2");
//            inObject = new ObjectInputStream(clientSocket.getInputStream());
//            System.out.println("Ok2");

//            statusSocket = new Socket(IP, 4003);
//    		outStatus = new ObjectOutputStream(statusSocket.getOutputStream());
//            inStatus = new ObjectInputStream(statusSocket.getInputStream());
//            System.out.println("Ok3");
//
//    		Message ms = new Message(name, null, "active", null);
//    		outStatus.writeObject(ms);
//    		outStatus.flush();
//            System.out.println("Ok4");


    		
            new ReadMessage().start();    
//            new ReadStatus().start();
//            System.out.println("Ok5");

    	} catch (IOException e) {
            System.err.println("помилка при приєднані до сервера"+ e);
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
    
    public void checkUser() {
    	
        SessionFactory factory = new Configuration()
    			.configure("hibernate.cfg.xml")
    			.addAnnotatedClass(Clients.class) 
    			.buildSessionFactory();  
    	Session session = factory.openSession();
        try {
            session.beginTransaction();

            Query<Clients> query = session.createQuery("FROM Clients ", Clients.class);
//            WHERE activeStatus = true
            users.clear();
            users.addAll(query.list());
            System.out.println(users);
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

    public void saveMessage(String sender, String recepient, String message, Date date, boolean fileType) {

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
  
    public void importUsersMessage(String message, Date date, boolean fileType) {
    	Platform.runLater(() -> {
    		Message ms = new Message(name, selectedChat, message, date);
    		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm EE");
    		String dateText = formatter.format(date);
    		
    		drawUserMessage(ms.getMessage(), dateText);

    	});
    	
    }
    
    public void createUsersMessage(String message) {
    	Platform.runLater(() -> {
    		Date dt = new Date();
    		Message ms = new Message(name, selectedChat, message, dt);
    		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm EE");
    		String dateText = formatter.format(dt);
    		
    		drawUserMessage(ms.getMessage(), dateText);
        	sendMessage(ms);
//        	scrollPane.vvalueProperty().bind(messagePane.heightProperty());
        	saveMessage(name, selectedChat, message, dt, false );

    	});
    	
    }
    
    public void drawUserMessage(String message, String date) {    		    		
    		AnchorPane mainPane = new AnchorPane();
    		VBox pane = new VBox();
    		messagePane.setMargin(mainPane , new Insets(6));
    		String style = " -fx-padding: 15px;  -fx-background-color: #9CEAFB; -fx-background-radius: 25px;";
    		pane.setStyle(style);
    		Label messageLbl = new Label();
    		messageLbl.setText("Ви: "+ message );
    		messageLbl.setStyle("-fx-text-fill: 2F3135; -fx-font-weight: bold; -fx-font-size: 14px;");

//    		messageLbl.setFont(Font.font(14))
    		messageLbl.setMaxWidth(550);
    		messageLbl.setWrapText(true);

    		Label dateLbl = new Label(date);
    		dateLbl.setFont(Font.font(12));
    		dateLbl.setStyle("-fx-text-fill: #2F3135;");
    		
    		
        	messagePane.getChildren().addAll(mainPane);
        	mainPane.getChildren().addAll(pane);
        	mainPane.setRightAnchor(pane, 0.0);
        	pane.setAlignment(Pos.CENTER_RIGHT);

        	pane.getChildren().addAll(messageLbl, dateLbl);
        	textMessage.clear();
        	scrollPane.vvalueProperty().bind(messagePane.heightProperty());
    		new animatefx.animation.ZoomInLeft(mainPane).setSpeed(2.0).play();

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

            pane.setStyle(" -fx-padding: 15px; -fx-background-color:  #6E7888; -fx-background-radius: 25px; ");
    		Label messageLbl = new Label();
    		messageLbl.setText(senderName+ ": " + message );
    		messageLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
//    		messageLbl.setFont(Font.font(14));

    		messageLbl.setMaxWidth(550);
    		messageLbl.setWrapText(true);
    		
    		Label dateLbl = new Label(date);
    		dateLbl.setFont(Font.font(12));
    		dateLbl.setStyle("-fx-text-fill: white;");

        	messagePane.getChildren().addAll(mainPane);
        	mainPane.getChildren().addAll(pane);
        	pane.setAlignment(Pos.CENTER_RIGHT);

        	pane.getChildren().addAll(messageLbl, dateLbl);
        	scrollPane.vvalueProperty().bind(messagePane.heightProperty());
        	
    		new animatefx.animation.ZoomInRight(mainPane).setSpeed(2.0).play();


        });
    }

    
    private class ReadMessage extends Thread {
        @Override
        public void run() {
            try {
                inObject = new ObjectInputStream(clientSocket.getInputStream());
                while (true) {

                	Message response = (Message) inObject.readObject();
                	if(response.isFileType()) {
            	        Message imageMsg = (Message) inObject.readObject();
         	  
            	        int size = imageMsg.getImageSize();

                        byte[] imageArray = imageMsg.getImageArray();

                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageArray));

                        System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());
                        
                        ImageIO.write(image, imageMsg.getImageType(), new File("/home/illyusha/Pictures/"+imageMsg.getImageName()+"."+ imageMsg.getImageType()));
                        drawImageMessage(imageArray);

                	}
                	else {
                		String message = response.getMessage();
                    	String senderName = response.getSenderName();
                    	
                    	Date date = response.getDate();
                		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm EE");
                		String dateText = formatter.format(date);
                        if  (!message.isEmpty() && selectedChat.equals(response.getSenderName())) {
                           createOtherMessage(message, senderName, dateText);
                           
                       }
                	}
                }
            } catch (IOException | ClassNotFoundException  e) {
            	System.out.println(e);
            }
        }
    }

}
