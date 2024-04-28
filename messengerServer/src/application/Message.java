package application;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "History")
public class Message implements Serializable{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private int id;
	
	@Column 
	private String senderName;
	
	@Column 
	private String recipientName;
	
	@Column 
	private String message;
	
	@Column
	private int imageSize;
	
	@Column
	private byte[] imageArray;
	
	@Column
	private String imageType;
	
	@Column
	private String imageName;
	
	@Column 
	@Temporal(TemporalType.TIMESTAMP)
    private Date date;
	
	@Column 
	private boolean fileType;
	

	public Message() { 
		
	}
	
	public Message(String message) {
		this.message = message;
	}
	
	
	public Message(String senderName, String recipientName) {
		this.senderName = senderName;
		this.recipientName = recipientName;
	}
	
	public Message(String senderName, String recipientName, String message, Date date ) {
		this.senderName = senderName;
		this.recipientName = recipientName;
		this.message = message;
		this.date = date;
	}

	public Message(String senderName, String recipientName, int size, byte[] imageArray, String imageType,
			String imageName, Date date, boolean fileType) {
		this.senderName = senderName;
		this.recipientName = recipientName;
		this.imageSize = size;
		this.imageArray = imageArray;
		this.imageType = imageType;
		this.imageName = imageName;
		this.date = date;
		this.fileType = fileType;
	}
	
	public Message(boolean type) {
		this.fileType = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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

	public boolean isFileType() {
		return fileType;
	}

	public void setFileType(boolean fileType) {
		this.fileType = fileType;
	}

	public int getImageSize() {
		return imageSize;
	}

	public void setImageSize(int imageSize) {
		this.imageSize = imageSize;
	}

	public byte[] getImageArray() {
		return imageArray;
	}

	public void setImageArray(byte[] imageArray) {
		this.imageArray = imageArray;
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	
	
	
}
