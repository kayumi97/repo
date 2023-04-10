package org.easy.entity.inlabrad.security;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.easy.entity.inlabrad.Hospital;

@Entity
@Table(name="tb_usuario")
public class Usuario {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private String login;
	
	private String senha;
	
	private String cpf;
	
	private String email;
	
	private Boolean enabled;
	
	private String nome;
	
	private Long tbl_hospital_id;
	
	@ManyToOne
	@JoinColumn(name="tbl_hospital_id", insertable = false, updatable = false, nullable = true)
	private Hospital hospital;
	
	
	
	@ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tb_usuario_has_tb_perfil",
        joinColumns = { @JoinColumn(name = "tb_usuario_id") },
        inverseJoinColumns = { @JoinColumn(name = "tb_perfil_id") })
    private List<Perfil> perfis; 
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	

	public Long getTbl_hospital_id() {
		return tbl_hospital_id;
	}

	public void setTbl_hospital_id(Long tbl_hospital_id) {
		this.tbl_hospital_id = tbl_hospital_id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public List<Perfil> getPerfis() {
		return perfis;
	}

	public void setPerfis(List<Perfil> perfis) {
		this.perfis = perfis;
	}

	public Hospital getHospital() {
		return hospital;
	}

	public void setHospital(Hospital hospital) {
		this.hospital = hospital;
	}
	
	
}
