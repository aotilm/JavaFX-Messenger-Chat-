package tables;
import java.util.Date;

import javax.persistence.*;

import tables.Files;
import tables.Text;

@Entity
@Table
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

//    @Column
//    private int messageId;
//    
//    @Column
//    private boolean fileType;

    @OneToOne
    @JoinColumn(name = "textID")
    private Text text;

    @OneToOne
    @JoinColumn(name = "fileID")
    private Files files;
    
    @Column 
	@Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "recipientName")
    private String recipientName;

    @Column(name = "senderName")
    private String senderName;


    public History() {
    	
    }


	public History(Text text, Files files, Date date, String recipientName, String senderName) {
		this.text = text;
		this.files = files;
		this.date = date;
		this.recipientName = recipientName;
		this.senderName = senderName;
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

  
	
}
