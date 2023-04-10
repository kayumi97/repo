package org.easy.inlabrad.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidateDTO {

	private int status;
	private List<String> mensagens;
	private Object data;
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public List<String> getMensagens() {
		return mensagens;
	}
	public void setMensagens(List<String> mensagens) {
		this.mensagens = mensagens;
	}
	
	public void addMsg(String msg){
		if(this.mensagens == null) {
			this.mensagens = new ArrayList<>();
			
		}
		this.mensagens.add(msg);
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	
	
}
