package org.easy.rest.inlabrad;

import java.util.ArrayList;
import java.util.List;

import org.easy.dao.inlabrad.HospitalDao;
import org.easy.entity.inlabrad.Hospital;
import org.easy.inlabrad.dto.FiltroDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
//import org.springframework.web.bind.annotation.CrossOrigin;


@RestController
@RequestMapping("/api/hospitais")
//@CrossOrigin(origins = "http://localhost:4200")
public class HospitalController {

	@Autowired
	HospitalDao dao;
	
	


	@GetMapping("/{id}")
	public ResponseEntity<Hospital> buscar(@PathVariable Long id) {
		 var p = this.dao.findById(id);
		 
		 
		 
		 if(p != null) return new ResponseEntity(p,HttpStatus.OK);
		 
		 
		 return new ResponseEntity("Hospital não encontrado", HttpStatus.NOT_FOUND);
		 
		 
	}
	

	@GetMapping
	public ResponseEntity<List<Hospital>> pesquisar(@RequestParam(name="filtro", required = false) String filtro) {
		List<Hospital> list = new ArrayList<>();

		
		if(filtro == null || filtro.isEmpty()) {
			list =  this.dao.findAll();
			
	
		}else {
			Gson g = new Gson();
			FiltroDTO filtroDTO = g.fromJson(filtro, FiltroDTO.class);
			
			if(filtroDTO != null /*&& filtroDTO.getNome().length() > 2*/) {
				
				list = this.dao.findByNome(filtroDTO.getNome());
				
			}
		}
		
		return new ResponseEntity(list,HttpStatus.OK);
		
		
		
	}
	
	
	@PostMapping(path = "", 
			consumes = MediaType.APPLICATION_JSON_VALUE, 
			produces = MediaType.APPLICATION_JSON_VALUE)

	public ResponseEntity<Hospital> create(@RequestBody Hospital reg) {

		var prof = this.dao.save(reg);

		return new ResponseEntity(prof,HttpStatus.OK);
	}


	@PutMapping(path = "/{id}",
			consumes = MediaType.APPLICATION_JSON_VALUE, 
			produces = MediaType.APPLICATION_JSON_VALUE)

	public ResponseEntity<Hospital> atualizar(@RequestBody Hospital reg, @PathVariable Long id) {

		var pac = this.dao.update(reg);

		return new ResponseEntity(pac,HttpStatus.OK); 
	}




	@RequestMapping(value = "/{id}",method=RequestMethod.DELETE)
	public ResponseEntity remover(@PathVariable Long id) {

		this.dao.deleteById(id);

		return new ResponseEntity(HttpStatus.OK);
	}
	
	
}
