package org.easy.rest.inlabrad;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import org.easy.dao.inlabrad.PerfilDao;
import org.easy.dao.inlabrad.UsuarioDao;
import org.easy.entity.inlabrad.security.Usuario;
import org.easy.inlabrad.dto.ValidateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import org.springframework.web.bind.annotation.CrossOrigin;


@RestController
@RequestMapping("/api/authentication")
public class SecurityController {
	
	@Autowired
	private UsuarioDao dao;
	
	@Autowired
	private PerfilDao perfilDao;

	@PostMapping(path = "", 
			consumes = MediaType.APPLICATION_JSON_VALUE, 
			produces = MediaType.APPLICATION_JSON_VALUE)

	public ResponseEntity<ValidateDTO> confirm(@RequestBody Usuario reg) {
	
		ValidateDTO val = new ValidateDTO();
		
		var user = this.dao.findByLogin(reg.getLogin());
		

		
		
		if(user == null) {
			val.setStatus(HttpStatus.UNAUTHORIZED.value());
			
			return new ResponseEntity(val, HttpStatus.OK);


		}
		
		if(user.getEnabled() == false) {
			val.setStatus(HttpStatus.UNAUTHORIZED.value());
			
			return new ResponseEntity(val, HttpStatus.OK);
		}

		
		
		String valMD5 = this.genMD5(reg.getSenha());
		
		
		if(user.getSenha().equals(valMD5)) {
			
			user.setSenha("");
			
			val.setData(user);
			
			val.setStatus(HttpStatus.OK.value());
			
			
			
		}else {
			
			user.setSenha("");
			
			val.setData(user);
			
			val.setStatus(HttpStatus.UNAUTHORIZED.value());
			
		}
		
		return new ResponseEntity(val, HttpStatus.OK);

		
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
