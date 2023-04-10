package org.easy.rest;

import java.util.List;

import org.easy.entity.Instance;

public class AjaxInstance {

	private Boolean success;
	private Instance instance;
	private List<Instance> list;
	private String base64;
	private String description;
	
	private int width;
	private int height;
	
	public AjaxInstance(Boolean success, Instance instance){
		this.success = success;
		this.instance = instance;
	}
	
	public AjaxInstance(Boolean success, List<Instance> instanceLst){
		this.success = success;
		this.list = instanceLst;
	}
	
	public AjaxInstance(Boolean success, String base64, int width, int height){
		this.success = success;
		this.width = width;
		this.height = height;
		this.base64 = base64;
	}
	
	public AjaxInstance(Boolean success, String base64, int width, int height, String description){
		this.success = success;
		this.width = width;
		this.height = height;
		this.base64 = base64;
		this.description = description;
	}
	
	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
	
	public Instance getInstance() {
		return instance;
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	public List<Instance> getList() {
		return list;
	}

	public void setList(List<Instance> list) {
		this.list = list;
	}

	public String getBase64() {
		return base64;
	}

	public void setBase64(String base64) {
		this.base64 = base64;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
}
