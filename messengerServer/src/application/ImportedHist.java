package application;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import tables.Files;
import tables.Text;


public class ImportedHist implements Serializable{
  private int id;

  private Text text;

  private Files files;

  private Date date;

  private String recipientName;

  private String senderName;    
  
  private int textID;

  private String message;
  
  private int fileID;

  private String fileType;

  private byte[] file;

  public ImportedHist() {
	  
  }

public int getId() {
	return id;
}

public void setId(int id) {
	this.id = id;
}

public Text getText() {
	return text;
}

public void setText(Text text) {
	this.text = text;
}

public Files getFiles() {
	return files;
}

public void setFiles(Files files) {
	this.files = files;
}

public Date getDate() {
	return date;
}

public void setDate(Date date) {
	this.date = date;
}

public String getRecipientName() {
	return recipientName;
}

public void setRecipientName(String recipientName) {
	this.recipientName = recipientName;
}

public String getSenderName() {
	return senderName;
}

public void setSenderName(String senderName) {
	this.senderName = senderName;
}

public int getTextID() {
	return textID;
}

public void setTextID(int textID) {
	this.textID = textID;
}

public String getMessage() {
	return message;
}

public void setMessage(String message) {
	this.message = message;
}

public int getFileID() {
	return fileID;
}

public void setFileID(int fileID) {
	this.fileID = fileID;
}

public String getFileType() {
	return fileType;
}

public void setFileType(String fileType) {
	this.fileType = fileType;
}

public byte[] getFile() {
	return file;
}

public void setFile(byte[] file) {
	this.file = file;
}
  
  
}
