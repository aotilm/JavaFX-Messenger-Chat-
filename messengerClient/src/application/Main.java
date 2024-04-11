package application;
	
import java.io.IOException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javafx.application.Application;
import javafx.stage.Stage;
import tables.Clients;
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
			primaryStage.setOnCloseRequest(event -> {
			    try {
			        if (ClientController.clientSocket != null && !ClientController.clientSocket.isClosed()) {
			            ClientController.clientSocket.close();
			        }
			        if (ClientController.inObject != null) {
			            ClientController.inObject.close();
			        }
			        if (ClientController.outObject != null) {
			            ClientController.outObject.close();
			        }
			        if (!ClientController.name.isEmpty()) {
			            ClientController.updateActiveStatus(ClientController.name, false);
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
