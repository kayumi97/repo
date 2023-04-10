package org.easy.entity.inlabrad;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="tbl_hospital")
public class Hospital implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)	
	@Column(name="id")
	private Long id;
	
	@Column(name="nome")
	private String nome;
	
	@Column(name="prefix")
	private String prefix;
	
	@Column(name="ip_sender")
	private String ipSender;
	
	@Column(name="cnpj")
	private String cnpj;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getIpSender() {
		return ipSender;
	}

	public void setIpSender(String ipSender) {
		this.ipSender = ipSender;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}
	
	
	
}
