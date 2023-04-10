package org.easy.server;



import com.google.common.eventbus.EventBus;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.UserIdentityAC;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.BasicQueryTask;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.TagUtils;
import org.easy.dao.inlabrad.HospitalDao;
import org.easy.event.NewFileEvent;
import org.easy.event.NewLogEvent;
import org.easy.util.Dcm2Dcm;
import org.easy.util.Dcm2Jpg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;


@Configurable
public class DicomServer {
	private static final Logger LOG = LoggerFactory.getLogger(DicomServer.class);

	private static final String DCM_EXT = ".dcm";
	private static final String JPG_EXT = ".jpg";

	
	@Value("${pacs.storage.image}")
	private String pacsImageStoragePath;

	@Value("${pacs.storage.dcm}")
	private String pacsDcmStoragePath;

	private final Device device = new Device("storescp");
	private final ApplicationEntity ae = new ApplicationEntity("*");
	private final Connection conn = new Connection();
	private File storageDir;

	private int status;

	
	public EventBus eventBus;


	@Autowired
	HospitalDao daoHospital;




	private final class CStoreSCPImpl extends BasicCStoreSCP {

		

		CStoreSCPImpl() {
			super("*");
		}

		@Override
		protected void store(Association as, PresentationContext pc,
				Attributes rq, PDVInputStream data, Attributes rsp)
						throws IOException {
			rsp.setInt(Tag.Status, VR.US, status);
			if (storageDir == null)
				return;

			String ipAddress  = as.getSocket().getInetAddress().getHostAddress(); //ip address
			String associationName = as.toString();
			String cuid = rq.getString(Tag.AffectedSOPClassUID);
			String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
			String name = rq.getString(Tag.PatientName);
			String tsuid = pc.getTransferSyntax();

			var listHospital = daoHospital.findByIpSender(ipAddress);
			String prefix = "none";

			if(listHospital == null || listHospital.size() == 0) {

				LOG.error("Hospital not exists! Connection Details--> ipAddress: {}  " , ipAddress);

				return;
			}else {
				prefix = listHospital.get(0).getPrefix();
			}
			
			String tmp_file_name = "_raw_"+iuid+DCM_EXT;

			String final_file_name = prefix + "_" + iuid + DCM_EXT;


			//File file = new File(storageDir, ipAddress + "_" + iuid + DCM_EXT);
			File file = new File(storageDir, final_file_name);
			try {
				LOG.info("as: {}", as);



				Attributes atrs = new Attributes();


				String NAME = name;

				LOG.info("----------- NAME: "+ name);

			


				var attrbs = as.createFileMetaInformation(iuid, cuid, tsuid);


				storeToOriginal(as, attrbs,
						data, file);

				if(!file.exists()){
					LOG.error("File {} does not exists! Connection Details--> ipAddress: {}  associationName: {}  sopclassuid: {}  sopinstanceuid: {} transfersyntax: {}", file.getAbsolutePath(), ipAddress, associationName, cuid, iuid, tsuid);
					return;
				}

				

				eventBus.post(new NewFileEvent(file, prefix));

				//let's parse the files
				Attributes attrs = parse(file);
				if(attrs != null){                	
					String studyiuid = attrs.getString(Tag.StudyInstanceUID);
					String patientID = attrs.getString(Tag.PatientID);
					patientID = (patientID == null || patientID.length() == 0) ? "<UNKNOWN>" : patientID;
					Long projectID = -1L;
					String patientName = attrs.getString(Tag.PatientName);
					String institutionName = attrs.getString(Tag.InstitutionName);
					String modality = attrs.getString(Tag.Modality);
					String uniqueID = file.getName();
					Date studyDate =  attrs.getDate(Tag.StudyDate);
					Date studyTime =  attrs.getDate(Tag.StudyTime);         		
					
					
					
					
					String studyDateTime = (studyDate != null && studyTime != null)?new SimpleDateFormat("MM-dd-yyyy").format(studyDate)+" "+new SimpleDateFormat("HH-mm-ss").format(studyTime):"01-01-1901 01-01-01";

					System.out.println(ipAddress+" : "+patientName+" : "+modality);

					//eventBus.post(new NewLogEvent(as.toString(), "IMAGE_RECEIVED", ipAddress, studyDateTime, projectID, patientID, patientName, null, studyiuid, institutionName, uniqueID));
					//eventBus.post(new NewFileEvent(file, ipAddress, studyiuid, iuid, cuid, associationName));

				}else{
					LOG.error("File Name {} could not be parsed!",file.getName());
				}
				
				
				
				

			}catch(EOFException e){
				//deleteFile(as, file); //broken file, just remove...     
				LOG.error("Dicom Store EOFException: " + e.getMessage());               
			}
			catch (Exception e) {              
				deleteFile(as, file); //broken file, just remove...     
				LOG.error("Dicom Store Exception: " + e.getMessage());        
			}
			
			

		}
	};




	public DicomServer() throws IOException {
		device.setDimseRQHandler(createServiceRegistry());
		device.addConnection(conn);
		device.addApplicationEntity(ae);
		device.setAssociationHandler(associationHandler);
		ae.setAssociationAcceptor(true);
		ae.addConnection(conn);
		
	}

	public static String GetProbality(File fileDCM) {
		try {
			byte[] raw = new byte[1024];
	
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
			Iterator<ImageReader> iterator =ImageIO.getImageReadersByFormatName("DICOM");
			
			DicomInputStream dis = new DicomInputStream(fileDCM);
	
			Attributes meta = dis.readFileMetaInformation();
	
			Attributes attribs = dis.readDataset(-1, Tag.PixelData);
	
			while(dis.read(raw,0,1024) != -1) {
				
				baos.write(raw);
	
				raw = new byte[1024];
				
			};
			
			String n = attribs.getString(Tag.PatientSex);
			
			LOG.info("FILE TAG NAME: " + n);

			dis.close();
			
			baos.close();
			
			return n;
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "tag not found";
	}
	
	
	public static void anonymize(File fileDCM) {
		try {

			byte[] raw = new byte[1024];

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			Iterator<ImageReader> iterator =ImageIO.getImageReadersByFormatName("DICOM");


			DicomInputStream dis = new DicomInputStream(fileDCM);

			Attributes meta = dis.readFileMetaInformation();

			Attributes attribs = dis.readDataset(-1, Tag.PixelData);

			while(dis.read(raw,0,1024) != -1) {
				
				baos.write(raw);

				raw = new byte[1024];
				
			};

			/*while (iterator.hasNext()) {
				
				ImageReader imageReader = (ImageReader) iterator.next();

			}*/

			String n = attribs.getString(Tag.PatientName);

			LOG.info("FILE TAG NAME: " + n);

			dis.close();

			//String name = "ANONYMOUS";
			attribs.setSpecificCharacterSet("ISO_IR 100");
			attribs.setBytes(Tag.PatientName, VR.PN, n.getBytes("ISO-8859-1"));
			attribs.setSpecificCharacterSet("ISO_IR 192");
			attribs.setBytes(Tag.PixelData, VR.OW, baos.toByteArray());

			DicomOutputStream dcmo = new DicomOutputStream(fileDCM);
			dcmo.writeFileMetaInformation(meta);


			attribs.writeTo(dcmo);
			dcmo.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}




	}

	private void storeToOriginal(Association as, Attributes fmi,
			PDVInputStream data, File file) throws IOException  {
		LOG.info("{}: M-WRITE {}", as, file);

		file.getParentFile().mkdirs();
		
		

		DicomOutputStream out = new DicomOutputStream(file);

		try {
			out.writeFileMetaInformation(fmi);

			

			data.copyTo(out);

		} finally {
			SafeClose.close(out);
		}
		
		Dcm2Dcm dcm2dcm = new Dcm2Dcm();
		
		String args[] = {file.getAbsolutePath(), file.getAbsolutePath()+"_fix"};
		
		dcm2dcm.main(args);
		
		String f_name = file.getAbsolutePath();
		
		file.delete();
		
		File f = new File(f_name+"_fix");
		
		f.renameTo(new File(f_name));
		
		
		
		File dcmFile = new File(f_name);
	
		Dcm2Jpg dcm2jpg = new Dcm2Jpg();
		
		String newfilename = FilenameUtils.removeExtension(dcmFile.getName()) + JPG_EXT; //remove the .dcm and  assign a JPG extension


		File tempImage = new java.io.File(pacsImageStoragePath, newfilename); //create the temporary image file instance
		
		if(tempImage.exists()) {
			tempImage.delete();
			tempImage.createNewFile();
		}

		dcm2jpg.converter(dcmFile, tempImage);
		
		
		
		
		
	}

	private void storeTo(Association as, Attributes fmi,
			PDVInputStream data, File file) throws IOException  {
		
		LOG.info("{}: M-WRITE {}", as, file);

		file.getParentFile().mkdirs();

		
		/****************/
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		DicomOutputStream out = new DicomOutputStream(baos,"");

		try {
		
			out.writeFileMetaInformation(fmi);

			data.copyTo(out);

		} finally {

			SafeClose.close(out);
		
		}
		
		/****************/
		
		byte[] raw = new byte[1024];
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		DicomInputStream dis = new DicomInputStream(bais);

		Attributes meta = dis.readFileMetaInformation();

		Attributes attribs = dis.readDataset(-1, Tag.PixelData);

		while(dis.read(raw,0,1024) != -1) {
			
			baos.write(raw);

			raw = new byte[1024];
			
		};

		dis.close();
		
		/****************/
		
		
		DicomOutputStream dcmo = new DicomOutputStream(file);
		dcmo.writeFileMetaInformation(meta);

		attribs.writeTo(dcmo);
		dcmo.close();


	}

	/*private void storeTo(Association as, Attributes fmi,
                         PDVInputStream data, File file) throws IOException  {
        LOG.info("{}: M-WRITE {}", as, file);

        file.getParentFile().mkdirs();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();



        data.copyTo(baos);


        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());


        DicomInputStream dis = new DicomInputStream(bais);

		Attributes attribs = dis.readDataset();

        dis.close();



        DicomOutputStream out = new DicomOutputStream(file);

        try {
            out.writeFileMetaInformation(fmi);

            attribs.setBytes(Tag.PixelData, VR.OW, baos.toByteArray());
            //out.write(baos.toByteArray());

            anonymize(attribs);


            attribs.writeTo(out);

        } finally {
            SafeClose.close(out);
        }
    }*/

	/*
	private void storeTo(Association as, Attributes fmi,
			PDVInputStream data, File file) throws IOException  {
		LOG.info("{}: M-WRITE {}", as, file);

		file.getParentFile().mkdirs();


		BufferedImage myJpegImage = null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		data.transferTo(baos);


		ImageReader imageReader = (ImageReader)ImageIO.getImageReadersByFormatName("DICOM").next();

		DicomImageReadParam dicomImageReadParam = (DicomImageReadParam) imageReader.getDefaultReadParam();






		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		ImageInputStream iis = ImageIO.createImageInputStream(bais);

		imageReader.setInput(iis,false);

		myJpegImage  = imageReader.read(0, dicomImageReadParam);


		iis.close();

		if(myJpegImage == null){

			System.out.println("Could not read image!!");

		}

		ByteArrayOutputStream fos = new ByteArrayOutputStream();


		ImageIO.write(myJpegImage, "jpeg", fos);





		DicomInputStream dis = new DicomInputStream(bais);

		Attributes attribs = dis.readDataset(-1, Tag.PixelData);

		dis.close();



		DicomOutputStream out = new DicomOutputStream(file);

		try {
			out.writeFileMetaInformation(fmi);

			attribs.setBytes(Tag.PixelData, VR.OW, fos.toByteArray());

			//anonymize(attribs);


			attribs.writeTo(out);

		} finally {
			SafeClose.close(out);
		}
	}*/

	private static void anonymize(Attributes attribs) throws UnsupportedEncodingException {
		String name = "Anonimizado";

		attribs.setSpecificCharacterSet("ISO_IR 100");

		attribs.setBytes(Tag.PatientName, VR.PN, name.getBytes("ISO-8859-1"));

		attribs.setSpecificCharacterSet("ISO_IR 192");





	}

	private static Attributes parse(File file) throws IOException {
		DicomInputStream in = new DicomInputStream(file);
		try {
			in.setIncludeBulkData(IncludeBulkData.NO);

			Attributes attribs = in.readDataset(-1, Tag.PixelData);

			return attribs;
		} finally {
			SafeClose.close(in);
		}
	}

	private static void deleteFile(Association as, File file) {
		if (file.delete())
			LOG.info("{}: M-DELETE {}", as, file);
		else
			LOG.warn("{}: M-DELETE {} failed!", as, file);
	}

	private DicomServiceRegistry createServiceRegistry() {
		DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
		serviceRegistry.addDicomService(new BasicCEchoSCP());
		serviceRegistry.addDicomService(new CStoreSCPImpl());
		//serviceRegistry.addDicomService(new BasicQueryTask(null, null, null, null));
		return serviceRegistry;
	}

	public void setStorageDirectory(File storageDir) {
		if (storageDir != null)
			storageDir.mkdirs();
		this.storageDir = storageDir;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public static void configureConn(Connection conn){
		conn.setReceivePDULength(Connection.DEF_MAX_PDU_LENGTH);
		conn.setSendPDULength(Connection.DEF_MAX_PDU_LENGTH);

		conn.setMaxOpsInvoked(0);
		conn.setMaxOpsPerformed(0);
	}

	public static DicomServer init(String aeHost, int aePort, String aeTitle, String storageDirectory, EventBus eventBus, DicomServer dicomServerInstance) {
		LOG.info("Bind to: " + aeTitle + "@" + aeHost + ":" + aePort + "; storage: " + storageDirectory);

		DicomServer ds = null;
		try {
			ds = dicomServerInstance;


			ds.eventBus = eventBus;
			if(aeHost != null) {
				ds.conn.setHostname(aeHost);
			}
			ds.conn.setPort(aePort);
			ds.ae.setAETitle(aeTitle);

			//default conn parameters
			configureConn(ds.conn);

			//accept-unknown
			ds.ae.addTransferCapability(
					new TransferCapability(null,
							"*",
							TransferCapability.Role.SCP,
							"*"));

			ds.setStorageDirectory(new File(storageDirectory));

			ExecutorService executorService = Executors.newCachedThreadPool();
			ScheduledExecutorService scheduledExecutorService =
					Executors.newSingleThreadScheduledExecutor();
			ds.device.setScheduledExecutor(scheduledExecutorService);
			ds.device.setExecutor(executorService);
			ds.device.bindConnections();

		}catch (Exception e) {
			LOG.error("dicomserver: {}", e.getMessage());
			e.printStackTrace();
		}

		return ds;
	}


	private AssociationHandler associationHandler = new AssociationHandler(){

		@Override
		protected AAssociateAC makeAAssociateAC(Association as,
				AAssociateRQ rq, UserIdentityAC arg2) throws IOException {

			State st = as.getState();

			if(as != null)
			{				
				LOG.info("makeAAssociateAC: {}  Associate State: {}  Associate State Name: {}", as.toString(), st, st.name());
				try {					
					//eventBus.post(new NewLogEvent(as.toString(),st.name(),as.getSocket().getInetAddress().getHostAddress(), null, null,null,null,null,null,null,null));
				}catch (Exception e) {
					LOG.error(e.getMessage());
				}
			}

			if(rq != null)
				LOG.info("Max OpsInvoked: {}  Max OpsPerformed: {}  Max PDU Length: {}  Number of Pres. Contexts: {}",rq.getMaxOpsInvoked(), rq.getMaxOpsPerformed(), rq.getMaxPDULength(), rq.getNumberOfPresentationContexts());

			if(arg2 != null)
				LOG.info("UserIdentityAC Length:{}",arg2.length());

			var ret = super.makeAAssociateAC(as, rq, arg2);



			return ret; 
		}

		@Override
		protected AAssociateAC negotiate(Association as, AAssociateRQ rq)
				throws IOException {

			if(as != null)
				LOG.info("AAssociateAC negotiate:{}",as.toString());

			var ret = super.negotiate(as, rq);



			return ret;
		}

		@Override
		protected void onClose(Association as) {

			State st = as.getState();

			if(as != null && st == State.Sta13){
				LOG.info("Assocation Released and Closed: {} Name: {}", as.getState(), as.toString());			

				try {					
					//eventBus.post(new NewLogEvent(as.toString(),st.name(),as.getSocket().getInetAddress().getHostAddress(), null, null, null, null,null,null,null,null));
				}  catch (Exception e) {					
					LOG.error(e.getMessage());					
				} 
			}
			else
			{
				LOG.info("Association Closed");
			}

			super.onClose(as);
		}
	};






}
