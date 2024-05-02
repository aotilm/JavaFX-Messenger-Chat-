package application;
	
import java.io.IOException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("Client.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setTitle("GODDAAMN Chat");
			primaryStage.setOnCloseRequest(event -> {
			    try {
			        if (ClientController.messageSocket != null && !ClientController.messageSocket.isClosed()) {
			            ClientController.messageSocket.close();
			        } 
			        
			        if (ClientController.serviceSocket != null && !ClientController.serviceSocket.isClosed()) {
			            ClientController.serviceSocket.close();
			        } 
			        
			        if (ClientController.statusSocket != null && !ClientController.statusSocket.isClosed()) {
			            ClientController.statusSocket.close();
			        } 
			        
			        if (ClientController.messageIn != null) {
			            ClientController.messageIn.close();
			        }
			        if (ClientController.messageOut != null) {
			            ClientController.messageOut.close();
			        }
			        if (ClientController.serviceIn != null) {
			            ClientController.serviceIn.close();
			        }
			        if (ClientController.serviceOut != null) {
			            ClientController.serviceOut.close();
			        }

			        if (ClientController.statusIn != null) {
			            ClientController.statusIn.close();
			        }
			    } catch (IOException e) { 
			        System.err.println(e);
			    } finally {
			    	primaryStage.close(); 
			    }
			});

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
