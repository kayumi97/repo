package org.easy.event;

import java.io.File;
import java.io.Serializable;

public class NewFileEvent implements Serializable{

	private static final long serialVersionUID = 8244229216737621902L;

	private String sender;
	
	public NewFileEvent(File file){
		this.file = file;
	}
	public NewFileEvent(File file, String sender){
		this.sender = sender;
		this.file = file;
	}
	private final File file;

	public File getFile() {
		return file;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	
}
