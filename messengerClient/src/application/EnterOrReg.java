package application;

import java.io.Serializable;

public class EnterOrReg implements Serializable{

	private String name;
	
	private String password;
	
	private boolean type;

	public EnterOrReg(String name, String password, boolean type) {
		super();
		this.name = name;
		this.password = password;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isType() {
		return type;
	}

	public void setType(boolean type) {
		this.type = type;
	}
	
	
}
