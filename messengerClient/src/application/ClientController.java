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
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;


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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tables.Clients;


public class ClientController implements Initializable {

    @FXML
    private AnchorPane userPane;

    @FXML
    private Button addButton, btnSend, btnGoChat, updateChats, update; 

    @FXML
    private VBox vbox, messagePane; 

    @FXML
    private Label number, chatStatusLabel, chatNameLabel, registrationLbl, enterLabel;
    
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

    public static Socket messageSocket; 
    public static Socket serviceSocket; 
    public static Socket statusSocket;

    public static ObjectInputStream statusIn;
    public static ObjectInputStream messageIn;
    public static ObjectOutputStream messageOut;
    
    public static ObjectInputStream serviceIn;
    public static ObjectOutputStream serviceOut;
    
    public static BufferedImage image;
 


    public ArrayList<Clients> users = new ArrayList<>();
    public ArrayList<Message> history = new ArrayList<>();
    public ArrayList<Clients> activeUsers = new ArrayList<>();

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
    	Platform.runLater(() -> {
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
    		new animatefx.animation.ZoomInLeft(img).setSpeed(2.0).play();
    	});
    	

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
    		new animatefx.animation.ZoomInRight(img).setSpeed(2.0).play();

    	});
    	
    }
    
    public void importHistory(String sender, String recipient) {
    	try {
    		Message sg = new Message("ImportHistory");
    		serviceOut.writeObject(sg);
			serviceOut.flush();
    		
        	Message hs = new Message(sender, recipient);
			serviceOut.writeObject(hs);
			serviceOut.flush();
			
			int historySize = (int) serviceIn.readObject();
			history.clear();
			for(int i=0; i<historySize; i++) {
				Message ms = (Message) serviceIn.readObject();

				history.add(ms);
				
				Date date = ms.getDate();
	            SimpleDateFormat formatter = new SimpleDateFormat("hh:mm EE");
	     		String dateText = formatter.format(date);
	     		  
	            if(ms.getImageType() == null) {
	            	  if (ms.getSenderName().equals(sender)) {
	    	           	  importUsersMessage(ms.getMessage(), ms.getDate());
	    	            }
	    	            else {
	    	           	  createOtherMessage(ms.getMessage(), ms.getSenderName(), dateText);
	    	            }
	            }
	            else if(ms.getMessage() == null) {
	            	 if (ms.getSenderName().equals(sender)) {
	 	            	drawUserImageMessage(ms.getImageArray());
	    	         }
	    	         else {
	    	        	 drawImageMessage(ms.getImageArray());
	    	         }
	            	
	            }
	            
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
    	
    }
    
    public boolean checkIfUserIsOnline() {
    	boolean result = false;
    	try {
    		Message sg = new Message(selectedChat);
			serviceOut.writeObject(sg);
			serviceOut.flush();
			
			result = (boolean) serviceIn.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return result;

    }
    
    public void addUser() {
    	selectAllUsers();
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
    	    	    
    	    	    Label chatName = new Label(client.getName());
    	    	    chatName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
    	    	    chatName.setLayoutY(20);
    	    	    chatName.setLayoutX(50);
    	    	    pane.setMargin(avatar , new Insets(10));
    	    	    
    	    	    Circle status = new Circle(); 
    	            status.setRadius(5); 
    	            status.setFill(Color.GREEN); 
    	            status.setVisible(false);
    	            
//    	            Circle unread = new Circle(); 
//    	            status.setRadius(10); 
//    	            status.setFill(Color.BLUE); 
//    	            status.setVisible(true);
    	            
//    	            AnchorPane unread = new AnchorPane();
//    	            unread.setPrefWidth(20);
//    	            unread.setPrefHeight(20);
//    	            unread.setStyle("-fx-backgroung-color: blue; -fx-backgroung-radius: 20px;");
//    	    	    
    	    	    vbox.getChildren().add(pane); 
    	    	    pane.getChildren().addAll(avatar, chatName, status);	
    	    	        	    	    
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

    	    		new animatefx.animation.SlideInLeft(pane).setSpeed(1.5).play();

    	    	});
    		}

    	}    
    }
    
    public void userAuthentication() {
    	name = textName.getText();

    	connectToServer();

    	if (checkClientInDB(name)) {
//    		updateActiveStatus(name, true);
//    		connectToServer();
    		chatPane.toFront();
    		chooseChatPane.toFront();
    		new animatefx.animation.FadeOutDown(singInPane).setSpeed(2.5).play();
    		new animatefx.animation.FadeInDown(chatPane).setSpeed(2.5).play();
    		addUser();

    	}
    	else { 
    		textName.clear();
    		enterLabel.setText("Вам слід зареєструватись!");
    	}
    }
   
    public void connectToServer() {
    	try{
    		serviceSocket =  new Socket(IP,4005);
    		serviceOut = new ObjectOutputStream(serviceSocket.getOutputStream());
            
            messageSocket = new Socket(IP, 4004);
			messageOut = new ObjectOutputStream(messageSocket.getOutputStream());
			
			statusSocket = new Socket(IP, 4003);
	        new ReadMessage().start(); 
	        new ActiveStatusReader().start();
	        
            serviceIn = new ObjectInputStream(serviceSocket.getInputStream()); 
            
	        System.out.println("підключилось");

    	} catch (IOException e) {
            System.err.println("помилка при приєднані до сервера"+ e);
        }
    }
        

    public void clientRegistration() {
    	if(!statusSocket.isConnected()) {
        	connectToServer();
    	}
    	name = textName.getText();
    	Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження реєстрації");
        alert.setHeaderText("Ви ще не зарєєстровані. Бажаєте зареєструватись?");
        alert.setHeight(30);
        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == ButtonType.OK) {
        	EnterOrReg registration = new EnterOrReg(name, null, true);
        	try {
				serviceOut.writeObject(registration);
				serviceOut.flush();
				
				if(serviceIn.readObject().equals(true)) {
		    		addUser();
					chatPane.toFront();
		    		chooseChatPane.toFront();
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			
        	
        }
    } 
    
    public boolean checkClientInDB(String name) {
    	try {
    	   	EnterOrReg enter = new EnterOrReg(name, null, false);
        	serviceOut.writeObject(enter);
			serviceOut.flush();
			 
			if(serviceIn.readObject().equals(true)) {
				return true;
			}
			
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("помилка у методі checkClientInDB " + e );
		}
		return false;
    }
    
    public void selectAllUsers() {
    	
      try {
		serviceOut.writeObject("SelectUsers");
		serviceOut.flush();
		
		int usersSize = (int) serviceIn.readObject();
		System.out.println(usersSize);
		for(int i=0; i<usersSize; i++) {
			Clients client = (Clients) serviceIn.readObject();
			users.add(client);
		}
	} catch (IOException | ClassNotFoundException e) {
		e.printStackTrace();
	}
    }
   
    public void importUsersMessage(String message, Date date) {
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
        	System.out.println("Повідомлення надіслано");
//        	scrollPane.vvalueProperty().bind(messagePane.heightProperty());
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
    		messageLbl.setMaxWidth(500);
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
    			messageOut.writeObject(ms);;
                messageOut.flush();
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

    		messageLbl.setMaxWidth(500);
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
                messageIn = new ObjectInputStream(messageSocket.getInputStream());
                while (true) {

                	Message response = (Message) messageIn.readObject();
                	if(response.isFileType()) {
            	        Message imageMsg = (Message) messageIn.readObject();
            	        
            	        int size = imageMsg.getImageSize();
                        byte[] imageArray = imageMsg.getImageArray();

                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageArray));

                        System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());
                        
//                        ImageIO.write(image, imageMsg.getImageType(), new File("/home/illyusha/Pictures/"+imageMsg.getImageName()+"."+ imageMsg.getImageType()));
                        if  (selectedChat.equals(imageMsg.getSenderName())) {
                            drawImageMessage(imageArray);

                        }
                        
       
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
    
    
    private class ActiveStatusReader extends Thread{
      	 @Override
           public void run() {
             try {
				statusIn = new ObjectInputStream(statusSocket.getInputStream());
				int listSize;
				 while((listSize  = (int) statusIn.readObject()) != -1) {
	      			 try {
						activeUsers.clear();

						for(int i=0; i<listSize; i++) {
							Clients activeClient = (Clients) statusIn.readObject();
							activeUsers.add(activeClient);

						}
//						System.out.println("--------------------------------------");
//						System.out.println(activeUsers.toString());
//						System.out.println("--------------------------------------");
						changeStatus();
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
					}
	      		 }
			} catch (IOException | ClassNotFoundException e) {
				ActiveStatusReader.this.stop();
				e.printStackTrace();
			}       		
      	 }
      	 
      	public void changeStatus() {
      		Platform.runLater(() -> {
      			for (Node node : vbox.getChildren()) {
          	        if (node instanceof HBox) {
          	            HBox hbox = (HBox) node;
          	            Label chatNameLabel = null;
          	            Circle statusCircle = null;

          	            for (Node hboxNode : hbox.getChildren()) {
          	                if (hboxNode instanceof Circle) {
          	                    statusCircle = (Circle) hboxNode;
          	                    statusCircle.setVisible(false); 
          	                } else if (hboxNode instanceof Label) {
          	                    Label label = (Label) hboxNode;
          	                    for (int i = 0; i < activeUsers.size(); i++) {
          	                        Clients client = activeUsers.get(i);
          	                        if (client.getName().equals(label.getText())) {
          	                            chatNameLabel = label;
          	                            break;
          	                        }
          	                    }
          	                }
          	            }
          	            if (chatNameLabel != null && statusCircle != null) {
          	                statusCircle.setVisible(true);
          	            }
          	        }
          	    }
          	    
          	  
          	  
          		for (int i = 0; i < activeUsers.size(); i++) {
            		Clients client = activeUsers.get(i);
            		if(client.getName().equals(selectedChat)) {
            			chatStatusLabel.setText("У мережі");
            			break;
            		}
            		else {
            			chatStatusLabel.setText("Не в мережі :(");
            		}
            	}

        	 });
      	    
      	    
      	  
      	  
      	}

      	 
      	 
    }
}
