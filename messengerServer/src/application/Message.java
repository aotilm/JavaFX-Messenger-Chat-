package application;
import java.util.Date;

import javax.persistence.*;

import tables.Files;
import tables.Text;

@Entity
@Table(name = "History")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column 
	@Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "recipientName")
    private String recipientName;

    @Column(name = "senderName")
    private String senderName;

    @OneToOne
    @JoinColumn(name = "fileID", unique = true)
    private Files files;

    @OneToOne
    @JoinColumn(name = "textID", unique = true)
    private Text text;

    public Message() {
    	
    }
    
	public Message(Date date, String recipientName, String senderName, Files file, Text text) {
		this.date = date;
		this.recipientName = recipientName;
		this.senderName = senderName;
		this.files = file;
		this.text = text;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public Files getFile() {
		return files;
	}

	public void setFile(Files file) {
		this.files = file;
	}

	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	
    
}
