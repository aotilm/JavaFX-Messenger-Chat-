package application;

import java.io.Serializable;

public class Message implements Serializable{
	private String senderName;
	private String recipientName;
	private String message;
	
	public Message(String senderName, String recipientName, String message) {
		this.senderName = senderName;
		this.recipientName = recipientName;
		this.message = message;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getRecipientName() {
		return recipientName;
	}

	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
	
}
