package org.easy.rest.inlabrad;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.easy.dao.inlabrad.PerfilDao;
import org.easy.dao.inlabrad.UsuarioDao;
import org.easy.entity.inlabrad.security.Perfil;
import org.easy.entity.inlabrad.security.Usuario;
import org.easy.inlabrad.dto.FiltroDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;
import com.google.gson.Gson;

import antlr.StringUtils;
//import org.springframework.web.bind.annotation.CrossOrigin;


@RestController
@RequestMapping("/api/usuarios")
//@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

	@Autowired
	private UsuarioDao dao;
	
	@Autowired
	private PerfilDao daoPerfil;


	@GetMapping("/{id}")
	public ResponseEntity<Usuario> buscar(@PathVariable Long id) {
		var p = this.dao.findById(id);



		if(p.isEmpty() == false) return new ResponseEntity(p.get(),HttpStatus.OK);


		return new ResponseEntity("Sala não encontrado", HttpStatus.NOT_FOUND);

	}
	
	
	@GetMapping("/mylogin/{login}")
	public ResponseEntity<Usuario> buscar(@PathVariable String login) {
		var p = this.dao.findByLogin(login);



		return new ResponseEntity(p,HttpStatus.OK);



	}
	
	@GetMapping("/perfis")
	public ResponseEntity<Perfil> buscarPerfis() {
		var p = this.daoPerfil.findAll(Sort.by(Sort.Direction.ASC, "perfil"));



		return new ResponseEntity(p,HttpStatus.OK);

	}

	@GetMapping
	public ResponseEntity<List<Usuario>> pesquisar(@RequestParam(value = "filtro", required = false) String filtro) {
		
		
		

		List<Usuario> res = new ArrayList<>();
		
		if(filtro != null && filtro != "") {
			Gson g = new Gson();
			
			FiltroDTO filtroDTO = g.fromJson(filtro, FiltroDTO.class);
			
			res = this.dao.findByEnabled(filtroDTO.getNome(), filtroDTO.getEnabled());
			
			return new ResponseEntity(res,HttpStatus.OK);

		}else {
			res = this.dao.findAll();
			
			return new ResponseEntity(res,HttpStatus.OK);
		}
	}


	@PostMapping(path = "", 
			consumes = MediaType.APPLICATION_JSON_VALUE, 
			produces = MediaType.APPLICATION_JSON_VALUE)

	public ResponseEntity<Usuario> create(@RequestBody Usuario reg) {

		if(reg.getId() == null || reg.getId() == 0) {
		
			reg.setSenha(this.genMD5(reg.getSenha()));	
		
		}else {
			
			Usuario tmp = this.dao.findById(reg.getId()).get();

			reg.setSenha(tmp.getSenha());
		}
		

		var pac = this.dao.save(reg);

		return new ResponseEntity(pac,HttpStatus.OK);
	}
	
	@PutMapping(path = "/{id}", 
			consumes = MediaType.APPLICATION_JSON_VALUE, 
			produces = MediaType.APPLICATION_JSON_VALUE)

	public ResponseEntity<Usuario> atualizar(@RequestBody Usuario reg, @PathVariable Long id) {

		if(reg.getId() != id) {
			
			return new ResponseEntity(null,HttpStatus.BAD_REQUEST);

		}
		
		Usuario tmp = this.dao.findById(reg.getId()).get();
		
		tmp.setSenha(this.genMD5(reg.getSenha()));	
		

		reg.setEnabled(true); 

		var pac = this.dao.save(reg);

		return new ResponseEntity(pac,HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{id}/force",method=RequestMethod.DELETE)
	public ResponseEntity removerForce(@PathVariable Long id) {
		
		this.dao.deleteById(id);
		
		return new ResponseEntity(HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{id}",method=RequestMethod.DELETE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	
	public ResponseEntity remover(@PathVariable Long id) {
		Usuario tmp = this.dao.findById(id).get();

		tmp.setEnabled(false);
		
		this.dao.save(tmp);
		
		return new ResponseEntity(tmp, HttpStatus.OK);
	}
	
	
	@PostMapping(path = "/senha", 
			consumes = MediaType.APPLICATION_JSON_VALUE, 
			produces = MediaType.APPLICATION_JSON_VALUE)

	public ResponseEntity<Usuario> alterarSenha(@RequestBody Usuario reg) {

		if( !(reg.getId() == null || reg.getId() == 0) ) {
		
			Usuario tmp = this.dao.findById(reg.getId()).get();
			
			tmp.setSenha(this.genMD5(reg.getSenha()));	
			
			this.dao.save(tmp);
			
			return new ResponseEntity(tmp,HttpStatus.OK);

		
		}
		
		return new ResponseEntity(null,HttpStatus.OK);


	}
	
	
	private String genMD5(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(password.getBytes());
			byte[] digest = md.digest();
			String myHash = DatatypeConverter
					.printHexBinary(digest).toUpperCase();

			return myHash;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	

}
