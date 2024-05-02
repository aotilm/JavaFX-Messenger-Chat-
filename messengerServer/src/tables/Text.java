package tables;

import javax.persistence.*;

@Entity
@Table(name = "text")
public class Text {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "textID")
    private int textID;

    @Column(name = "message")
    private String message;

    public Text() {
    	
    }

	public Text(String message) {
		this.message = message;
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
    
    
}
