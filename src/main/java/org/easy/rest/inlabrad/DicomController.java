package org.easy.rest.inlabrad;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.plugins.jpeg.JPEGImageReadParam;
import javax.imageio.stream.ImageInputStream;
//import javax.media.jai.JAI;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.easy.dao.InstanceDao;
import org.easy.dao.SeriesDao;
import org.easy.dao.StudyDao;
import org.easy.entity.Instance;
import org.easy.entity.Series;
import org.easy.entity.Study;
import org.easy.rest.AjaxInstance;
import org.easy.rest.HomeController;
import org.easy.server.DicomServer;
import org.easy.util.Dcm2Dcm;
import org.easy.util.Dcm2Jpg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dicom")
//@CrossOrigin(origins = "http://localhost:4200")
public class DicomController {

	@Value("${pacs.storage.image}")
	private String pacsImageStoragePath;

	@Value("${pacs.storage.dcm}")
	private String pacsDcmStoragePath;
	
	@Value("${pacs.storage.2run}")
	private String pacs2RunStoragePath;

	private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

	private static final String JPG_EXT = ".jpg";
	private static final int THUMBNAIL_WIDTH  = 300;
	private static final int THUMBNAIL_HEIGHT = 300;
	private static final int MAX_IMAGE_WIDTH = 1000;
	private static final int MAX_IMAGE_HEIGHT = 800;

	@Autowired
	StudyDao studyDao;

	@Autowired
	SeriesDao serieDao;

	@Autowired
	InstanceDao instanceDao;

	@GetMapping("/{pkTBLPatientID}/studies")
	public ResponseEntity<List<Study>> pesquisarStudies(@PathVariable Long pkTBLPatientID){

		var list = this.studyDao.findByPkTBLPatientID(pkTBLPatientID);

		return new ResponseEntity(list, HttpStatus.OK);

	}

	@GetMapping(path="/studies/{pkTBLStudyID}/series", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Series>> pesquisarSeries(@PathVariable Long pkTBLStudyID){

		var list = this.serieDao.findByPkTBLStudyID(pkTBLStudyID);

		return new ResponseEntity(list, HttpStatus.OK);

	}

	@GetMapping(path="/studies/series/{pkTBLSeriesID}/instances", produces = MediaType.APPLICATION_JSON_VALUE )
	public @ResponseBody ResponseEntity pesquisarInstances(@PathVariable Long pkTBLSeriesID){
		List<Instance> instances = instanceDao.findByPkTBLSeriesID(pkTBLSeriesID);

		return new ResponseEntity(instances, HttpStatus.OK);



	}



	@RequestMapping(value = "/createjpg/{pkTBLInstanceID}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<AjaxInstance> getImage(@PathVariable Long pkTBLInstanceID, HttpServletRequest request, HttpServletResponse response) throws IOException{
		String mensagem = "Olá, mundo!";
		System.out.println(mensagem);
		System.out.println(pacsDcmStoragePath);

		java.io.File tempImage = null;   
		Instance instance = instanceDao.findById(pkTBLInstanceID);

		int width = 0;
		int height = 0;

		if(instance != null){
			File dicomFile = new File(pacsDcmStoragePath + "/" + instance.getMediaStorageSopInstanceUID()+".dcm");

			/********************************************************* TEMP IMAGE FILE CREATION *****************************************************************/              
			Dcm2Jpg dcm2Jpg = null; 
			try{
				String newfilename = FilenameUtils.removeExtension(dicomFile.getName()) + JPG_EXT; //remove the .dcm and  assign a JPG extension

				tempImage = new java.io.File(pacsImageStoragePath, newfilename); //create the temporary image file instance

				if(!tempImage.exists())
					this.convert(dicomFile, tempImage);




				ImageReader imageReader = (ImageReader) ImageIO.getImageReadersByFormatName("jpeg").next();

				JPEGImageReadParam jpgImageReadParam = (JPEGImageReadParam) imageReader.getDefaultReadParam();


				ImageInputStream iis = ImageIO.createImageInputStream(tempImage);	

				imageReader.setInput(iis,false);

				BufferedImage bimg = imageReader.read(0,jpgImageReadParam);

				width = bimg.getWidth();
				height = bimg.getHeight();

				iis.close();


				if(!tempImage.exists())	throw new Exception(); //if not exists, throw exception to log and return back

			}catch(Exception e){
				LOG.error("failed convert {} to jpeg... Exception: {}", dicomFile, e.getMessage()); // shouldn't care...	           
			}
			/********************************************************** END OF TEMP FILE CREATION ***************************************************************/

			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_JPEG);

			if(tempImage != null){	    	
				byte[] bytes = IOUtils.toByteArray( new FileInputStream(tempImage) ) ;
				//return new ResponseEntity<byte[]> (bytes, headers, HttpStatus.OK);

				String encodedString = Base64.getEncoder().encodeToString(bytes);

				return new ResponseEntity<AjaxInstance>(new AjaxInstance(true, encodedString, width, height), HttpStatus.OK);

			}

		}

		return new ResponseEntity<AjaxInstance>(new AjaxInstance(false, "", 0, 0), HttpStatus.OK);

	}
	
	@RequestMapping(value="/showimageResult/{pkTBLInstanceID}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<AjaxInstance> showImageResult(@PathVariable Long pkTBLInstanceID) throws IOException {

		AjaxInstance ret = null;

		File file = null;
		
		Instance instance = null;
		
		instance = instanceDao.findById(pkTBLInstanceID);
		
		String probablity = ""; 
		
		int width = 0;
		
		int height = 0;
		
		if(instance != null){
			File pngFile = new File(pacs2RunStoragePath + "/result/Map_" + instance.getMediaStorageSopInstanceUID()+".dcm"+".png");
			
			File dcmProbality = new File(pacs2RunStoragePath + "/result/attention_map_test_" + instance.getMediaStorageSopInstanceUID()+".dcm");
			
			Path path = Paths.get(pngFile.getAbsolutePath());
			
			byte[] data = Files.readAllBytes(path);
			
			
			/*BufferedImage image = JAI.create("fileload", file.getAbsolutePath()).getAsBufferedImage();
			width = image.getWidth();
			height = image.getHeight();*/
			
			width = 512;
			height = 512;
			
			
			String encodedString = Base64.getEncoder().encodeToString(data);
			
			
			
			if(encodedString != null && encodedString.length() > 0) {
				probablity = DicomServer.GetProbality(dcmProbality);
			}
			
			return new ResponseEntity<AjaxInstance>(new AjaxInstance(true, encodedString, width, height, probablity), HttpStatus.OK);
		}
		
		return new ResponseEntity<AjaxInstance>(new AjaxInstance(false, "", 0, 0,probablity), HttpStatus.OK);
	}
	
	
	@RequestMapping(value="/showimage/{pkTBLInstanceID}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<AjaxInstance> showImage(@PathVariable Long pkTBLInstanceID) throws IOException {

		System.out.println("Olá, mundo!");

		AjaxInstance ret = null;

		File file = null;
		
		Instance instance = null;
		
		instance = instanceDao.findById(pkTBLInstanceID);
		
		int width = 0;
		
		int height = 0;
		
		if(instance != null){
			File dicomFile = new File(pacsDcmStoragePath + "/" + instance.getMediaStorageSopInstanceUID()+".dcm");
			
			String newfilename = FilenameUtils.removeExtension(dicomFile.getName()) + JPG_EXT; 
			
			file = new File( pacsImageStoragePath, newfilename); 
			
			Path path = Paths.get(file.getAbsolutePath());
			
			byte[] data = Files.readAllBytes(path);
			
			
			/*BufferedImage image = JAI.create("fileload", file.getAbsolutePath()).getAsBufferedImage();
			width = image.getWidth();
			height = image.getHeight();*/
			
			width = 512;
			height = 512;
			
			
			String encodedString = Base64.getEncoder().encodeToString(data);
			
			
			
			return new ResponseEntity<AjaxInstance>(new AjaxInstance(true, encodedString, width, height), HttpStatus.OK);
		}
		
		return new ResponseEntity<AjaxInstance>(new AjaxInstance(false, "", 0, 0), HttpStatus.OK);
	}
	
	
	@RequestMapping(value="/runBayer/{pkTBLInstanceID}", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<AjaxInstance> runBayer(@PathVariable Long pkTBLInstanceID) throws IOException {
		Instance instance = null;

		try
		{	    	
			instance = instanceDao.findById(pkTBLInstanceID);

			if(instance != null){
				
				File dicomFile = new File(pacsDcmStoragePath + "/" + instance.getMediaStorageSopInstanceUID()+".dcm");
				
				File bayerFile = new File(pacs2RunStoragePath + "/bayer/" + instance.getMediaStorageSopInstanceUID()+".dcm");
				
				Dcm2Dcm dcm2dcm = new Dcm2Dcm();
				
				String args[] = {dicomFile.getAbsolutePath(), bayerFile.getAbsolutePath()};
				
				dcm2dcm.main(args);

			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return new ResponseEntity<AjaxInstance>(new AjaxInstance(false, "", 0, 0), HttpStatus.OK);

		
	}
	
	public @ResponseBody ResponseEntity<AjaxInstance> showImage2(@PathVariable Long pkTBLInstanceID) throws IOException {

		String img = "";
		File file = null;
		int width = 0;
		int height = 0;
		byte[] data = new byte[1024];

		ByteArrayOutputStream baos = new ByteArrayOutputStream();


		AjaxInstance ret = null;

		Instance instance = null;
		Dimension newImageSize = null;

		try
		{	    	
			instance = instanceDao.findById(pkTBLInstanceID);

			if(instance != null){

				File dicomFile = new File(pacsDcmStoragePath + "/" + instance.getMediaStorageSopInstanceUID()+".dcm");


				String newfilename = FilenameUtils.removeExtension(dicomFile.getName()) + JPG_EXT; 

				file = new File( pacsImageStoragePath, newfilename); //create the temporary image file instance	



				//data = new byte[(int) file.length()+1];


				ImageReader imageReader = (ImageReader) ImageIO.getImageReadersByFormatName("jpeg").next();

				JPEGImageReadParam jpgImageReadParam = (JPEGImageReadParam) imageReader.getDefaultReadParam();

				ImageInputStream iis = ImageIO.createImageInputStream(file);	

				imageReader.setInput(iis,false);

				BufferedImage bimg = imageReader.read(0,jpgImageReadParam);




				//BufferedImage bimg = ImageIO.read(file);
				width = bimg.getWidth();
				height = bimg.getHeight();
				ImageIO.write(bimg, "jpg", baos);

				LOG.debug("Original width:"+width+ " Original height:"+height);
				//System.setProperty("java.awt.headless","true");

				/*getScreenSize doesn't work properly, hold this until get it fixed and keep 1000x800 constant size*/
				//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	   
				/*Dimension screenSize = new Dimension(1000,800);//keep constant size for now
				if(width >= MAX_IMAGE_WIDTH || height >= MAX_IMAGE_HEIGHT)
	    		{
	    			Dimension imgSize = new Dimension(width, height);
	    			Dimension boundary = new Dimension(MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
	    			newImageSize = Utils.getScaledDimension(imgSize, boundary);
	    			width = newImageSize.width;
	    			height = newImageSize.height;	    			
	    		}	*/    		

				//LOG.debug("Screen width:"+screenSize.width+ "  Screen Height:"+screenSize.height);
				//LOG.debug("Image width:"+width+ " Image height:"+height);

				iis.close();

				String encodedString = Base64.getEncoder().encodeToString(baos.toByteArray());

				return new ResponseEntity<AjaxInstance>(new AjaxInstance(true, encodedString, width, height), HttpStatus.OK);
			}

		}
		catch(Exception ex)
		{
			LOG.error(ex.getMessage());
		}


		return new ResponseEntity<AjaxInstance>(new AjaxInstance(false, "", 0, 0), HttpStatus.OK);

	}

	/*private void convert_try(File dcm, File jpg) {
		Dcm2Jpg conv = new Dcm2Jpg();

		try {
			if(jpg.exists()) {
				jpg.delete();
			}
			jpg.createNewFile();
			conv.convert(dcm, jpg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	
	
	public void convert(File dcm, File jpg) {
      
		Dcm2Jpg.converter(dcm, jpg);

    }



/*
	private void convert_ORIGINAL(File dcm, File jpg) {
		File file = dcm;

		Raster raster = null ;

		Iterator<ImageReader> iterator =ImageIO.getImageReadersByFormatName("DICOM");

		while (iterator.hasNext()) {
			ImageReader imageReader = (ImageReader) iterator.next();

			DicomImageReadParam dicomImageReadParam = (DicomImageReadParam) imageReader.getDefaultReadParam();



			BufferedImage myJpegImage = null;
			try {
				ImageInputStream iis = ImageIO.createImageInputStream(file);




				imageReader.setInput(iis,false);


				myJpegImage  = imageReader.read(0, dicomImageReadParam);


				iis.close();
				if(myJpegImage == null){
					System.out.println("Could not read image!!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			File file2 = jpg;
			try {
				OutputStream fos = new BufferedOutputStream(new FileOutputStream(file2));


				ImageIO.write(myJpegImage, "jpeg", fos);

				fos.flush();

				fos.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Completed");
		}
	}*/



}
