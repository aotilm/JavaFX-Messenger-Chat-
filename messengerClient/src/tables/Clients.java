package tables;

import java.io.Serializable;

public class Clients implements Serializable {

	private int id;
	
	private String name;
	
	private boolean activeStatus;
	
	public Clients() {
		
	}

	public Clients(String name, boolean activeStatus) {
		this.name = name;
		this.activeStatus = activeStatus;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {  
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActiveStatus() {
		return activeStatus;
	}

	public void setActiveStatus(boolean activeStatus) {
		this.activeStatus = activeStatus;
	}

	@Override
	public String toString() {
		return "Clients [id=" + id + ", name=" + name + ", activeStatus=" + activeStatus + "]";
	}





	
	
}
