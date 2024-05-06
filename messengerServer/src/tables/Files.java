package tables;

import javax.persistence.*;

@Entity
@Table
public class Files {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileID")
    private int fileID;

    @Column(name = "fileType")
    private String fileType;

    @Column(name = "file")
    private byte[] file;

    public Files() {
    	
    }

	public Files(String fileType, byte[] file) {
		this.fileType = fileType;
		this.file = file;
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
