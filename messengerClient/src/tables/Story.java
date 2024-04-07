package tables;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Story {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private int id;
	
	@Column 
	private String recipient;
	
	@Column 
	private String sender;
	
	@Column 
	private int chatId;

	public Story() {
		
	}

	public Story(String recipient, String sender, int chatId) {
		this.recipient = recipient;
		this.sender = sender;
		this.chatId = chatId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public int getChatId() {
		return chatId;
	}

	public void setChatId(int chatId) {
		this.chatId = chatId;
	}

	@Override
	public String toString() {
		return "Story [id=" + id + ", recipient=" + recipient + ", sender=" + sender + ", chatId=" + chatId + "]";
	}




	
	
}
