package org.easy.rest.inlabrad;

import java.util.ArrayList;
import java.util.List;

import org.easy.dao.PatientDao;
import org.easy.dao.impl.PatientDaoImpl;
import org.easy.entity.Patient;
import org.easy.entity.inlabrad.Hospital;
import org.easy.inlabrad.dto.FiltroDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.bind.annotation.CrossOrigin;
import com.google.gson.Gson;

@RestController
@RequestMapping("/api/pacientes")
public class PatientController {

	@Autowired
	PatientDaoImpl dao;
	
	@GetMapping
	public ResponseEntity<List<Patient>> pesquisar(
			@RequestParam(name="prefix", required = true) String prefix, 
			@RequestParam(name="filtro", required = false) String filtro) {
		
		List<Patient> list = new ArrayList<>();

		list =  this.dao.filter(filtro, prefix);
		
		return new ResponseEntity(list,HttpStatus.OK);
		
	}
}
