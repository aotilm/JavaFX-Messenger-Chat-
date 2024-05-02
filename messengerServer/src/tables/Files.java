package tables;

import javax.persistence.*;

@Entity
@Table(name = "files")
public class Files {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileID")
    private int fileID;

    @Column(name = "fileType")
    private String fileType;

    @Column(name = "file")
    private String file;

    public Files() {
    	
    }

	public Files(String fileType, String file) {
		super();
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

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
    
    
}
