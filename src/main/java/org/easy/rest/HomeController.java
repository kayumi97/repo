package org.easy.rest;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.plugins.jpeg.JPEGImageReadParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.tool.dcm2jpg.Dcm2Jpg;
import org.easy.component.ActiveDicoms;
import org.easy.dao.InstanceDao;
import org.easy.dao.PatientDao;
import org.easy.dao.SeriesDao;
import org.easy.dao.StudyDao;
import org.easy.entity.Instance;
import org.easy.entity.Patient;
import org.easy.entity.Series;
import org.easy.entity.Study;
import org.easy.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;




//@RestController
//@RequestMapping("/api")
//@CrossOrigin(origins = "http://localhost:4200")
@Controller
public class HomeController {

	private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

	private static final String JPG_EXT = ".jpg";
	private static final int THUMBNAIL_WIDTH  = 300;
	private static final int THUMBNAIL_HEIGHT = 300;
	private static final int MAX_IMAGE_WIDTH = 1000;
	private static final int MAX_IMAGE_HEIGHT = 800;

	@Value("${pacs.storage.image}")
	private String pacsImageStoragePath;

	@Value("${pacs.storage.dcm}")
	private String pacsDcmStoragePath;

	private int frame = 1;

	@Autowired
	PatientDao patientDao;

	@Autowired
	StudyDao studyDao;

	@Autowired 
	SeriesDao seriesDao;

	@Autowired
	InstanceDao instanceDao;

	private final ImageReader imageReader =
			ImageIO.getImageReadersByFormatName("DICOM").next();

	@Autowired
	private ActiveDicoms activeDicoms;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index() { 

		LOG.debug("index()");		
		return "index"; 
	}



	@RequestMapping(value = "/welcome", method = RequestMethod.GET)
	public String welcome() { 
		LOG.debug("welcome()");		
		return "welcome"; 
	}	

	

	@RequestMapping(value="/live", method = RequestMethod.GET)
	public @ResponseBody AjaxResult live(){		
		return new AjaxResult(true, activeDicoms.toString());
	}

	@RequestMapping(value="/study", method = RequestMethod.GET)
	public @ResponseBody AjaxStudy study(@RequestParam(defaultValue="0", value="pkTBLStudyID", required=false) Long pkTBLStudyID){		
		Study study = studyDao.findById(pkTBLStudyID);			
		return new AjaxStudy(true, study);
	}

	@RequestMapping(value="/series", method = RequestMethod.GET)
	public @ResponseBody AjaxSeries series(@RequestParam(defaultValue="0", value="pkTBLSeriesID", required=false) Long pkTBLSeriesID){		
		Series series = seriesDao.findById(pkTBLSeriesID); 	
		return new AjaxSeries(true, series);
	}

	@RequestMapping(value="/instance", method = RequestMethod.GET)
	public @ResponseBody AjaxInstance instance(@RequestParam(defaultValue="0", value="pkTBLInstanceID", required=false) Long pkTBLInstanceID){		
		Instance instance = instanceDao.findById(pkTBLInstanceID); 	
		return new AjaxInstance(true, instance);
	}

	@RequestMapping(value="/patient", method = RequestMethod.GET)
	public @ResponseBody AjaxPatient patient(@RequestParam(defaultValue="0", value="pkTBLPatientID", required=false) Long pkTBLPatientID){		
		Patient patient = patientDao.findById(pkTBLPatientID); 	
		return new AjaxPatient(true, patient);
	}
}
