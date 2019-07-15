package servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
  
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.SystemOutLogger;
import org.xml.sax.SAXException;


import pylypiv.errorGZ.dao.Factory;
import pylypiv.errorGZ.domain.Person;
import pylypiv.errorGZ.main.Data;
import pylypiv.errorGZ.messages.MessageA08p03;
import pylypiv.errorGZ.messages.MessageA08p03pr;
import pylypiv.errorGZ.messages.MessageA08p03pr_anouthOkato;
import pylypiv.errorGZ.messages.MessageA08p03pr_newDr;
import pylypiv.errorGZ.messages.MessageA08p16;
import pylypiv.errorGZ.messages.MessageA08p16chanchelastfioOnnew;
import pylypiv.errorGZ.messages.MessageA08p16emptyalllast;
import pylypiv.errorGZ.messages.MessageA24p10;
import pylypiv.errorGZ.messages.MessageZp1;
import pylypiv.errorGZ.messages.MessageZp1pr;
import pylypiv.errorGZ.parser.Zp;
import pylypiv.errorGZ.parser.ZpLoader;
import util.FileTransfer;
import util.ResObject;
import util.UtilForErrorGz;

  
@WebServlet("/processerror")
/*@MultipartConfig(fileSizeThreshold=1024*1024*10,    // 10 MB 
                 maxFileSize=1024*1024*50,          // 50 MB
                 maxRequestSize=1024*1024*100)      // 100 MB
*/
public class ProcessErrorGZ extends HttpServlet {
  
    private static final long serialVersionUID = 205242440643911308L;
    private ArrayList<ArrayList<String>> chancheOutEnp = new ArrayList<ArrayList<String>>();
    private String uploadFilePath;
    private String abslutepath;
     
    /**
     * Directory where uploaded files will be saved, its relative to
     * the web application directory.
     */
    private static final String UPLOAD_DIR = "uploads";
    private ArrayList<String> listcol = new ArrayList<String>();
      
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException
    {
    	boolean isMultipart = ServletFileUpload.isMultipartContent(request);
    	
    	// gets absolute path of the web application
        String applicationPath = request.getServletContext().getRealPath("");
        System.out.println(applicationPath);
        // constructs path of the directory to save uploaded file
        this.uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;
        System.out.println(uploadFilePath);

        // creates the save directory if it does not exists
        File fileSaveDir = new File(uploadFilePath);
        if (!fileSaveDir.exists()) { fileSaveDir.mkdirs(); }
        else { for(File file : fileSaveDir.listFiles()) file.delete(); }
        
    	// process only if its multipart content
    	if (isMultipart) {
    		// Create a factory for disk-based file items
    		FileItemFactory factory = new DiskFileItemFactory();

    		// Create a new file upload handler
    		ServletFileUpload upload = new ServletFileUpload(factory);
    		try {
    			// Parse the request
    			List<FileItem> multiparts = upload.parseRequest(request);

    			for (FileItem item : multiparts) {
    				if (!item.isFormField()) {
    					String name = new File(item.getName()).getName();
    					abslutepath = uploadFilePath + File.separator + name;
    					item.write(new File(abslutepath));
    				}
    			}
    			
    		} 
    		catch (Exception e) 
    		{
    		  System.out.println("File upload failed");
    		}
    	}
    	
    }
    
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException
    {
    	if(listcol.size() > 0) 
    	{
    		response.setContentType("application/json");
    	    response.setCharacterEncoding("UTF-8");
    	    response.getWriter().write("stop");
    	}
    	else {
    		
    		List<ArrayList<String>> bag = null;
    		try { bag =  paseInputStreamExcelForQuery(abslutepath); } catch (Exception e) {e.printStackTrace();}
    		// �� ������� ��� ��������� ������������������ ��������
    		List<ArrayList<String>> task = createTasks(bag);
    		System.out.println(task);
    		// ��������� � �����
    		List<String> listEnpForDeleteFromExcel =  new ArrayList<String>();
    		try { threads(task,listEnpForDeleteFromExcel,request); } catch (InterruptedException e) {e.printStackTrace();}
    		System.out.println("listEnpForDeleteFromExcel "+listEnpForDeleteFromExcel);
    		deleteFromTaskXLS(listEnpForDeleteFromExcel,abslutepath,chancheOutEnp);
    	}
    	
    }

    
    /*
     * ����� ������ ����������� ������, ������� ������ � ���������� ��������� � 
     * ������� ������� � ��� �� ������� Person
     */
    private List<ArrayList<String>> paseInputStreamExcelForQuery(String absolutePath) throws Exception
    {
    	List<ArrayList<String>>  listRow = new ArrayList<ArrayList<String>>();
    	FileInputStream is = new FileInputStream(absolutePath);
    	POIFSFileSystem fs = new POIFSFileSystem(is);
		HSSFWorkbook wb = new HSSFWorkbook(fs);	
		
		HSSFSheet sheet = wb.getSheetAt(0);		
		int rowsIn = sheet.getPhysicalNumberOfRows();
		System.out.println("���������� �������� ����� "+ rowsIn);
		
		Iterator<Row> rows = sheet.rowIterator();
		while (rows.hasNext())
		{
		    HSSFRow row = (HSSFRow) rows.next();
		    ArrayList<String> listCell = new ArrayList<String>();
		    for(int i = 0;i<75;i++)
		    {		    
		    	if(row.getCell(i) == null)
		    	{
		    		listCell.add("");
		    	}
		    	else {listCell.add(row.getCell(i).getStringCellValue());}
		    }
		    listRow.add(listCell);
		}
		
		FileOutputStream fileOut = new FileOutputStream(absolutePath);
		wb.write(fileOut);
		fileOut.close();
		is.close();
		
		return listRow;
    }
    
    
    /**
     * Utility method to get file name from HTTP header content-disposition
     * 
     */
    private String getFileName(Part part) throws UnsupportedEncodingException {
        String contentDisp = part.getHeader("content-disposition");
      //  System.out.println("���������� ��������� ��� jetty "+part.getHeader("content-disposition").format("UTF-8",part.getHeader("content-disposition") ));
        
        System.out.println("content-disposition header= "+contentDisp);
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) 
        {
        	/*
        	 * ��������� �� ��������� ������� � �������� ������� �����
        	 */
        	int count = 0;
            if (token.trim().startsWith("filename")) 
            {
            	String [] mas = token.split("");
            	
            	for(int i=0;i<mas.length;i++)
            	{
            		if(mas[i].equals("\\"))
            		{
            			count = i;
            		}
            	}
            	
            	if(count == 0)
            	{
	                return token.substring(token.indexOf("=")+2,token.length()-1);
            	}
            	else
            	{
	                return token.substring(count,token.length()-1);
            	}
            }
        }
        return "";
    }
   
    /*
     * ��������� ���� � ������� � �������
     * 
     */
    
    private void downloadExcel(HttpServletResponse response, String absolutePath) throws ServletException, IOException
    {
		System.out.println("pach....."+absolutePath);
		ServletOutputStream stream = null;
		BufferedInputStream buf = null;
		try {
			stream = response.getOutputStream();
			File doc = new File(absolutePath);
			response.setCharacterEncoding("application/msexcel");
			response.addHeader("Content-Disposition", "attachment; filename=" + absolutePath);
			response.setContentLength((int)doc.length());
			FileInputStream input = new FileInputStream(doc);
			buf = new BufferedInputStream(input);
			int readBytes = 0;
			while((readBytes = buf.read()) != -1) { stream.write(readBytes); }
		} catch (IOException ioe) {
			throw new ServletException(ioe.getMessage());
		} finally {
			if(stream != null) { stream.close(); }
			if(buf != null) { buf.close(); }
			
			File file =new File(absolutePath);
			System.out.println(file.delete());
		}
    }
    
    public class AutoThread implements Runnable {
    	
		private ArrayList<String> taskQueue;
		private UtilForErrorGz ul;
		private List<String> listEnpForDeleteFromExcel;
		private ArrayList<String> st = new ArrayList<String>();
		
		AutoThread(ArrayList<String> taskQueue,UtilForErrorGz ul,List<String> listEnpForDeleteFromExcel) {
			this.taskQueue = taskQueue;
			this.ul = ul;
			this.listEnpForDeleteFromExcel = listEnpForDeleteFromExcel;
		}

		@Override
		public void run() {
			// pass enp to query from bd (person and personadd)
			Data person = new Data(taskQueue.get(0));
			FileTransfer fileTransfer = new FileTransfer();
			ResObject res = new ResObject();
			ArrayList<Zp> zp = null;
			String enpCalc = "";
			boolean flag = false;
			for (int j = 1; j < taskQueue.size(); j++)
			{
    			if(taskQueue.get(j).equals("������� �� ��������"))
    			{
    				try { 
	    				String filename = new MessageZp1(fileTransfer, res, person).create();
	    				zp = new ZpLoader().load(filename, taskQueue.get(0),"�������� ������ ZP1"); 
		    				if(ul.checkRespons(zp,taskQueue.get(0),person))
		    				{
		    					// �������� �� ������ ����� zp1 
		    					if(person.getZpList() !=null)
		    					{
		    						person.setZp(person.getZpList().get(0)); System.out.println("Check is double response OK");
		    					}
		    					else
		    					{
		    						/*
		    						 * ������ ������ �����...����� zp1 �� ������
		    						 */
		    						System.out.println("response NULL. Will do Zp1pr");
		    					    filename = new MessageZp1pr(fileTransfer, res, person).create();
		    					    zp = new ZpLoader().load(filename, taskQueue.get(0),"�������� ������ ZP1pr ����� ����� �������� ������");
		    					    
		    					    person.setZpList(zp);
		    					    // ���� �� zp1pr ������ ������ ����� (����� ����� �������� �������� a08p01 � �������� �������)    ����������
		    					    if(person.getZpList().size() == 0) {
		    					    	listcol.remove(0);
		    					    	System.out.println("Exit from thread becouse zp1pr NULL response "+ filename);
    		    						return;	
		    					    }else
		    					    {
		    					    	person.setZp(person.getZpList().get(0));
		    					    	flag = true;
		    					    }
		    					    
		    					}
		    					
	    					}else
		    				{
		    					// ����������� �10
		    					// ������� ������ ����� ������ �� zp1 (��� ����� ����������� � ����������� � ����� pid2 � ������ ����� ��������� ������ �� ����� ����)
		    					person.setZp(person.getZpList().get(1));
		    					filename = new MessageA24p10(fileTransfer, res, person).create();
		    					
		    							if(ul.waitUprak2(filename, "������� ������ �� A24P10"))
		    							{
		    								// ��������� ����� zp1 ����� �����������
		    								filename = new MessageZp1(fileTransfer, res, person).create();
		    								zp = new ZpLoader().load(filename, taskQueue.get(0),"�������� ������ ZP1 ����� ������ ������� �����������");
		    								if(ul.checkRespons(zp,taskQueue.get(0),person))
		    								{
		    									person.setZp(person.getZpList().get(0));	System.out.println("Check is double response OK");
		    									
		    								}
		    								else
		    								{
		    									System.out.println("Check is double response BAD");
		    									// ������� ������ ����� ������ �� zp1 (��� ����� ����������� � ����������� � ����� pid2 � ������ ����� ��������� ������ �� ����� ����)
		    			    					person.setZp(person.getZpList().get(0));
		    			    					filename = new MessageA24p10(fileTransfer, res, person).create();
		    			    					if(ul.waitUprak2(filename, "������� ������ �� A24P10"))
				    							{
		    			    						// ��������� ����� zp1 ����� �����������
				    								filename = new MessageZp1(fileTransfer, res, person).create();
				    								zp = new ZpLoader().load(filename, taskQueue.get(0),"�������� ������ ZP1 ����� ������ �����������");
				    								if(ul.checkRespons(zp,taskQueue.get(0),person))
				    								{
					    								// ����������� ��������� ������ � npp
				    									person.setZp(person.getZpList().get(0));	
				    									System.out.println("Check is double response OK");
			    									}else
			    									{
			    										System.out.println("Check is double response BAD");
		    										}
				    							}
	    									}
		    							}
		    				}		
		    				// check vs
		    				if(ul.checkVs(zp,taskQueue.get(1),taskQueue.get(0)))
		    				{
		    					System.out.println("@@@@@@@@@@@ "+person.getZp());
		    					listEnpForDeleteFromExcel.add(taskQueue.get(0));
		    					// ���� � ������ zp1pr ����� ������ vs ������ � �� ������� �� ������....���� �������� �� p16 
		    					if(flag) {
		    						filename = new MessageA08p16chanchelastfioOnnew(fileTransfer, res, person).create(); System.out.println("Send A08P16pr after Zp1pr- "+ filename);
		    					}
		    					System.out.println("Exit from thread on OK VsNum");
		    					
	    					}else
		    				{
	    						
	    						enpCalc = ul.enp_calc(person.getPerson_birthday2(), Integer.valueOf(person.getPerson().getPerson_sex())+1);
    						 if(person.getZp().getIn1_15().equals("50000"))
	    					 {
	    						// ���� ����� �������� ��� (� ����� ����) ���������
	    					
	    						
	    						if(	person.getPerson().getPersonAdd().getEnp().substring(2, 10).equals(enpCalc) )
	    						{
			    						if(getNppZero(zp).equals(String.valueOf(Integer.valueOf(person.getPerson_sex()))) )
			    						{
					    						/*
					    						 * Check if all fio and  bithday is empty, then send p03
					    						 * e.g. before we a union on p24 and now is send p03
					    						 */
					    						if(taskQueue.get(3).equals("") && taskQueue.get(4).equals("") && taskQueue.get(5).equals("") && taskQueue.get(6).equals(""))
					    						{
					    							filename = new MessageA08p03(fileTransfer, res, person).create(); System.out.println("Send A08P03 when all last empty "+ filename);
					    							if(ul.waitUprak2(filename, "Wait response A03P03 when all last empty "+filename))
					    							{
					    								// send and check resalt �03
					    								filename = new MessageZp1(fileTransfer, res, person).create();
					    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03 when all last empty ");
					    								if(ul.checkVs(zp,taskQueue.get(1),taskQueue.get(0))){listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after A08P03 (condition if last fiod and last birthday empty)"+ filename);System.out.println("Exit from thread on OK VsNum");}
					    								else
						    							{
						    								System.out.println("NO set VSNUm after A08P03 (condition if last fiod and last birthday empty)");
						    								System.out.println("Trying to learn a data our person from db");
						    								/*
							    								1. ������ ������ � ���� �� �� ��� ���������� �� zp1 
							    								2. ����� set ������������� ������� data  ����������� �� ����� 
							    								3. ��������� �03pr
							    							*/	 		
						    								for(Zp m : zp)
						    								{
						    									if(m.getNpp() == 0)
						    									{
						    										 Person p =  Factory.getInstance().getPersonDAO().getPerson(m.getPid3cx1_1());
						    										 // ���� �� �� ������ ������ ������ ���� ��������� (�.�. ���� �������=��� ��� ����������=������ � ��...)
						    										 if(equalsInitials(p,person))
						    										 {
						    											 person.getPerson().getPersonAdd().setLast_fam(p.getPerson_surname());
							    										 person.getPerson().getPersonAdd().setLast_im(p.getPerson_kindfirstname());
							    										 person.getPerson().getPersonAdd().setLast_ot(p.getPerson_kindlastname());
							    										 person.getPerson().getPersonAdd().setLast_dr(p.getPerson_birthday());
						    										 }else
						    										 {
						    											 
						    											 System.out.println("���� ���������� ��� ��� �� ��� ��� ��� ���������� �� �� 54.. "+ filename);
						    										 }
						    										 
						    										 
						    									}
						    								} 
						    								
						    								filename = new MessageA08p03pr(fileTransfer, res, person).create(); System.out.println("Send A08P03pr after set last fiod "+ filename);
						    								if(ul.waitUprak2(filename, "Wait response A03P03pr after set last fiod "+filename))
							    							{
						    									// ��������� ��������� �03
							    								filename = new MessageZp1(fileTransfer, res, person).create();
							    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03pr after set last fiod ");
							    								if(ul.checkVs(zp,taskQueue.get(1),taskQueue.get(0)))
							    								{
							    									listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after A08P03pr "+ filename);
						    									}else
						    									{
						    										System.out.println("VSnum is NO after A08P03pr after set last fiod - EXIT");
						    										
						    										
						    									}
							    							}
						    								
						    							}
					    							}
					    							
					    						}
					    						else
					    						{	
					    						
								    					//�������� ���� ��������
								    					if(taskQueue.get(2).equals(taskQueue.get(3)) ){
								    						System.out.println("Bithday OK");
								    						// check fam firtsname and lastname with old fio 
								    						if(	taskQueue.get(7).trim().equals(taskQueue.get(4).trim()) && taskQueue.get(5).trim().equals(taskQueue.get(8).trim()) && taskQueue.get(6).trim().equals(taskQueue.get(9).trim())	)
								    						{
								    							
								    							filename = new MessageA08p03(fileTransfer, res, person).create(); System.out.println("Send A08P03 when all last equalswith firs fiod"+ filename);
								    							if(ul.waitUprak2(filename, "Wait response A03P03 when all last equalswith firs fiod"+filename))
								    							{
								    								// send and check resalt �03
								    								filename = new MessageZp1(fileTransfer, res, person).create();
								    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03 when all last equalswith firs fiod");
								    								if(ul.checkVs(zp,taskQueue.get(1),taskQueue.get(0))){listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after A08P03 (condition if last fiod and last birthday equals first fiod first birthday )"+ filename);System.out.println("Exit from thread on OK VsNum");}
								    								else
									    							{
									    								System.out.println("NO set VSNUm after A08P03 (condition if last fiod and last birthday equals first fiod first birthday )");
									    							}
								    							}	
								    						}else
								    							{
								    							
										    							//=====================================================================================================
										    							
										    							/* ������� ������� p03 �� ������� ��� �� �� ������ ��� ����� ��������� �� ������, ������� ��� � ��
										    							 @@@@@@@@@@@@@@@@@@@ �������� ��������� �� ��������. �������� � ��� ��� yprak2 p03 �� ������ ����� �� ��������
										    							 ���� � ������� ������ �� ����� � ����� == ����� �� 90% �������� � �����
										    							*/
											    							filename = new MessageA08p03(fileTransfer, res, person).create(); System.out.println("Send A08P03 (before A08P03 after A08P03pr)"+ filename);
											    							if(ul.waitUprak2(filename, "Wait response A03P03 Send A08P03 (before A08P03 after A08P03pr)"+filename))
											    							{
											    								// send and check resalt �03
											    								filename = new MessageZp1(fileTransfer, res, person).create();
											    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03 Send A08P03 (before A08P03 after A08P03pr)");
											    								if(ul.checkVs(zp,taskQueue.get(1),taskQueue.get(0))){listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after  (before A08P03 after A08P03pr)"+ filename);System.out.println("Exit from thread on OK VsNum"); listcol.remove(0); return;}
											    								else
												    							{
												    								System.out.println(" NO set VSNUm after (before A08P03 after A08P03pr)");
												    							}
											    							}	
										    							//=============================================
										    							
										    								filename = new MessageA08p03pr(fileTransfer, res, person).create(); System.out.println("Send A08P03pr "+ filename);
										    								if(ul.waitUprak2(filename, "Wait response A03P03pr "+filename))
											    							{
										    									// ��������� ��������� �03
											    								filename = new MessageZp1(fileTransfer, res, person).create();
											    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03pr ");
											    								if(ul.checkVs(zp,taskQueue.get(1),taskQueue.get(0)))
											    								{
											    									listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after A08P03pr "+ filename);
										    									}
											    								else
											    								{ 
											    									
											    									
											    									/* --------------------------------------------*/
											    									
											    									System.out.println("NO set VSNUm after A08P03pr (condition if last birthday=bythday and diffrent last fio vs fio )");
												    								System.out.println("Trying to learn a data our person from db");
												    								/*
													    								1. ������ ������ � ���� �� �� ��� ���������� �� zp1 
													    								2. ����� set ������������� ������� data  ����������� �� ����� 
													    								3. ��������� �03pr
													    							*/	 		
												    								for(Zp m : zp)
												    								{
												    									if(m.getNpp() == 0)
												    									{
												    										 Person p =  Factory.getInstance().getPersonDAO().getPerson(m.getPid3cx1_1());
												    										 // ���� �� �� ������ ������ ������ ���� ��������� (�.�. ���� �������=��� ��� ����������=������ � ��...)
												    										 /*
												    										  * ��� ����� ������
												    										  * ���������� ������ ��������������� �� �����(������) �� ��� � ���������(������) �� ����� ���� � �������� ��� ������ � ������ zp1 �������
												    										  * � ���� � ������� ���� �� 1 ���� ��������� � �������� �� ����� � �������� ������ �������� �������
												    										  * �����
												    										  * ������ ��� ��� ����� ������� ������� �� ��������� ��� ����� �� ������� � �� ����������� �� �� ��� ����� ���� ������� ���
												    										  * ����������� ��� ������.
												    										  * ���� ���������� �� ����� ������ �� person � ����� ��� ����������� personadd
												    										  */
												    										 if(equalsInitials(p,person))
												    										 {
												    											 person.getPerson().getPersonAdd().setLast_fam(p.getPerson_surname());
													    										 person.getPerson().getPersonAdd().setLast_im(p.getPerson_kindfirstname());
													    										 person.getPerson().getPersonAdd().setLast_ot(p.getPerson_kindlastname());
													    										 person.getPerson().getPersonAdd().setLast_dr(p.getPerson_birthday());
												    										 }else
												    										 {
												    											 System.out.println("���� ���������� ��� ��� �� ��� ��� ��� ���������� �� �� 54... "+ filename);
												    											 System.out.println("NTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"); listcol.remove(0); return;
												    											 
												    										 }
												    										 
												    										 
												    									}
												    								}
												    								
												    								filename = new MessageA08p03pr(fileTransfer, res, person).create(); System.out.println("Send A08P03pr after set last fiod (condition if last birthday=bythday and diffrent last fio vs fio )"+ filename);
												    								if(ul.waitUprak2(filename, "Wait response A03P03pr after set last fiod (condition if last birthday=bythday and diffrent last fio vs fio ) "+filename))
													    							{
												    									// ��������� ��������� �03
													    								filename = new MessageZp1(fileTransfer, res, person).create();
													    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03pr after set last fiod (condition if last birthday=bythday and diffrent last fio vs fio )");
													    								if(ul.checkVs(zp,taskQueue.get(1),taskQueue.get(0)))
													    								{
													    									listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after A08P03pr (condition if last birthday=bythday and diffrent last fio vs fio ) "+ filename);
												    									}else
												    									{
												    										
												    										System.out.println("VSnum is NO after A08P03pr after set last fiod, condition if last birthday=bythday and diffrent last fio vs fio ");
												    										/*
												    										 * ������������ ����� �������� ��� ������� ���. �.�. �� �������� �������� �������� 
												    										 * ������� ��� ���� � �� � �� ������� ��� �� ������ ������ (�.�. ���� ���������� ���� ������ �� ��������)
												    										 * ������ � ����� ��������� �������� zp1  � ��� ����� � ������  ��� 2 �������� �������� � �� ����� ����������
												    										 * �� ���� ������ ����� ������� ������ ������ �������� ��� ��� ������� ������������ ���� ������ �� �������� (������� ���)
												    										 * � ���� ������
												    										 * 1. ������ zp1 �� ������
												    										 * 2. ������ �� p16 �� ����� ��� ��� ��
												    										 * 3. zp1 �� ����� ������ � ��������� �� ��� npp0
												    										 */
												    										
												    										System.out.println("A08P16 ������ �� ����� ���. ������ ��� p16 ���������� �� zp1 �� ������");
												    										/* ���������. ������, ���������� ��� set ����� �� ���������� ������� 
												    										 * �.�. ���� 1. ������� �.�
												    										 * ������ � ��������� ������ � �������� �.�. �� ������� �.�. 
												    										 * � �� ��������� �� ��������� � ��� 1. 
												    										 */
												    										person = new Data(taskQueue.get(0));
												    										filename = new MessageZp1pr(fileTransfer, res, person).create();
														    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1pr for A08P16 chanche last fio on new fio dr"+ filename);
														    								// ������������� ����� npp zp1 �� �� ��� ����������
														    								if(zp.size() > 0)// ���� ������ ����� 
														    								{
														    									person.setZp(zp.get(0));
														    								}
														    								else{ System.out.println("������ ����� �� zp1pr ");System.out.println("NTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"); listcol.remove(0); return; }
														    								
														    								filename = new MessageA08p16chanchelastfioOnnew(fileTransfer, res, person).create(); System.out.println("Send A08P16pr chanche last fio on new - "+ filename);
													    									if(ul.waitUprak2(filename, "Wait response A08P16pr chanche last fio on new "+filename))
															    							{
													    										 filename = new MessageZp1(fileTransfer, res, person).create();
													    						    				zp = new ZpLoader().load(filename, taskQueue.get(0),"�������� ������ ZP1 ����� A08P16pr chanche last fio on new "); 
													    							    				if(ul.checkRespons(zp,taskQueue.get(0),person))
													    							    				{
													    							    					// �������� �� ������ ����� zp1 
													    							    					if(person.getZpList() !=null)
													    							    					{
													    							    						person.setZp(person.getZpList().get(0)); System.out.println("Check is double response OK");
													    							    						
													    							    						filename = new MessageA08p03(fileTransfer, res, person).create(); System.out.println("Send A08P03 when all last empty "+ filename);
													    						    							if(ul.waitUprak2(filename, "Wait response A03P03 when all last empty "+filename))
													    						    							{
													    						    								// send and check resalt �03
													    						    								filename = new MessageZp1(fileTransfer, res, person).create();
													    						    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03 after A08P16pr chanche last fio on new");
													    						    								if(ul.checkVs(zp,taskQueue.get(1),taskQueue.get(0))){listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after A08P16pr chanche last fio on new and A08P03 "+ filename);System.out.println("Exit from thread on OK VsNum");}
													    						    								else
													    							    							{
													    						    									System.out.println("VSnum is NO after ZP1pr A08P16pr A08P03"+ filename);
													    							    							}
												    						    								}
													    							    					}
													    							    					else
													    							    					{
													    							    						listcol.remove(0);
													    							    						System.out.println("Exit from thread becouse null response on ZP1");
													    							    						return;
													    							    						
													    							    					}
													    							    					
													    						    					}else
													    						    					{
		
													    							    					// ����������� �10
													    							    					// ������� ������ ����� ������ �� zp1 (��� ����� ����������� � ����������� � ����� pid2 � ������ ����� ��������� ������ �� ����� ����)
													    							    					person.setZp(person.getZpList().get(1));
													    							    					filename = new MessageA24p10(fileTransfer, res, person).create();
													    							    					
													    							    							if(ul.waitUprak2(filename, "������� ������ �� A24P10"))
													    							    							{
													    							    								// ��������� ����� zp1 ����� �����������
													    							    								filename = new MessageZp1(fileTransfer, res, person).create();
													    							    								zp = new ZpLoader().load(filename, taskQueue.get(0),"�������� ������ ZP1 ����� ������ ������� �����������");
													    							    								if(ul.checkRespons(zp,taskQueue.get(0),person))
													    							    								{
													    							    									person.setZp(person.getZpList().get(0));	System.out.println("Check is double response OK");
													    							    									
													    							    									filename = new MessageA08p03(fileTransfer, res, person).create(); System.out.println("Send A08P03 when all last empty "+ filename);
																    						    							if(ul.waitUprak2(filename, "Wait response A03P03 after A24P10 and A08P16pr"+filename))
																    						    							{
																    						    								// send and check resalt �03
																    						    								filename = new MessageZp1(fileTransfer, res, person).create();
																    						    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03 after A08P16pr chanche last fio on new");
																    						    								if(ul.checkVs(zp,taskQueue.get(1),taskQueue.get(0))){listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after A08P16pr chanche last fio on new and A08P03 "+ filename);System.out.println("Exit from thread on OK VsNum");}
																    						    								else
																    							    							{
																    						    									System.out.println("VSnum is NO after ZP1pr A08P16pr A08P03"+ filename);
																    							    							}
															    						    								}
													    							    									
													    							    								}
													    							    								else
													    							    								{
													    							    									System.out.println("Check is double response BAD");
													    							    									// ������� ������ ����� ������ �� zp1 (��� ����� ����������� � ����������� � ����� pid2 � ������ ����� ��������� ������ �� ����� ����)
													    							    			    					person.setZp(person.getZpList().get(0));
													    							    			    					filename = new MessageA24p10(fileTransfer, res, person).create();
													    							    			    					if(ul.waitUprak2(filename, "������� ������ �� A24P10"))
													    									    							{
													    							    			    						// ��������� ����� zp1 ����� �����������
													    									    								filename = new MessageZp1(fileTransfer, res, person).create();
													    									    								zp = new ZpLoader().load(filename, taskQueue.get(0),"�������� ������ ZP1 ����� ������ �����������");
													    									    								if(ul.checkRespons(zp,taskQueue.get(0),person))
													    									    								{
													    										    								// ����������� ��������� ������ � npp
													    									    									person.setZp(person.getZpList().get(0));	
													    									    									System.out.println("Check is double response OK");
													    									    									
													    									    									filename = new MessageA08p03(fileTransfer, res, person).create(); System.out.println("Send A08P03 when all last empty "+ filename);
																		    						    							if(ul.waitUprak2(filename, "Wait response A03P03 when all last empty "+filename))
																		    						    							{
																		    						    								// send and check resalt �03
																		    						    								filename = new MessageZp1(fileTransfer, res, person).create();
																		    						    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03 after A08P16pr chanche last fio on new");
																		    						    								if(ul.checkVs(zp,taskQueue.get(1),taskQueue.get(0))){listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after A08P16pr chanche last fio on new and A08P03 "+ filename);System.out.println("Exit from thread on OK VsNum");}
																		    						    								else
																		    							    							{
																		    						    									System.out.println("VSnum is NO after ZP1pr A08P16pr A08P03"+ filename);
																		    							    							}
																	    						    								}
																		    						    							
													    								    									}else
													    								    									{
													    								    										System.out.println("Check is double response BAD");
													    							    										}
													    									    							}
													    						    									}
													    							    							}
													    							    				
													    						    					}
															    							}
														    								
												    										
												    									}
													    							}
												    								
												    								
											    									
											    								}
											    							}
								    					    
								    					    
								    					}
								    					}else
								    					{
								    						System.out.println("Bythday different");
								    						/*
								    						 * ���� ���� �� !=�� � ���� �� =='' � ���� ��� �� ������ �� ���������� �� ����������
								    						 * ���������� �� ���� ��������� ������������ ������������� � ����� 7777777
								    						 */
								    						if(taskQueue.get(3).equals(""))
								    						{
				
							    								// �� ������ ����� ���
							    								if(	!taskQueue.get(4).trim().equals("") && !taskQueue.get(8).trim().equals("") && !taskQueue.get(9).trim().equals("")	)
									    						{
							    									filename = new MessageA08p03pr(fileTransfer, res, person).create(); System.out.println("Send A08P03pr (condition if last bythday!=bythday and lasr fio != '') ) - "+ filename);
								    								if(ul.waitUprak2(filename, "Wait response A03P03pr (condition if last bythday!=bythday and lasr fio != '') "+filename))
									    							{
								    									// ��������� ��������� �03
									    								filename = new MessageZp1(fileTransfer, res, person).create();
									    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03pr (condition if last bythday!=bythday and lasr fio != '') ");
									    								if(ul.checkVsandBythday(zp,taskQueue.get(1),taskQueue.get(0),taskQueue.get(2)))
									    								{
									    									listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after A08P03pr (condition if last bythday!=bythday and lasr fio != '')"+ filename);
								    									}
									    								else
									    								{
								    										System.out.println("VSnum is NO after A08P03pr (condition if last bythday!=bythday and lasr fio != '') - "+filename);
								    									}
									    							}
									    						}
							    								else
							    								{
							    									System.out.println("�� ������� ����� ���" + filename);
							    								}
							    								
						    								
							    							}else
							    							{
							    								/* ���� ���� �� != �� � ���� �� �� �����
							    								   1.  �� ������ ����� ���.........?????? ������������ ����������
							    								���� �� �����   2.  �������� ��� �������������� ����� �� 50000 ����� (�� �� ������ ���������� ������ �������� ��)
							    								   3.  �16 �� ������� ������� ������ ����� ��
							    								   4.  �03pr ��� � ���������� �� � ������� �������
							    								   
							    								*/ 
							    								if(	!taskQueue.get(4).trim().equals("") && !taskQueue.get(8).trim().equals("") && !taskQueue.get(9).trim().equals("")	)
									    						{
							    									filename = new MessageA08p16(fileTransfer, res, person).create(); System.out.println("Send A08P16pr (condition if last bythday!= && !'' bythday && last fio!='' ) ) - "+ filename);
							    									if(ul.waitUprak2(filename, "Wait response A03P16pr (condition if last bythday!=bythday and lasr fio != '') "+filename))
									    							{
								    									filename = new MessageA08p03pr_newDr(fileTransfer, res, person).create(); System.out.println("Send A08P03pr (condition if last bythday!= && !'' bythday && last fio!='' ) ) - "+ filename);
								    									if(ul.waitUprak2(filename, "Wait response A03P03pr (condition if last bythday!=bythday and lasr fio != '') "+filename))
										    							{
								    										filename = new MessageZp1(fileTransfer, res, person).create();
										    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03pr (condition if last bythday!=bythday and lasr fio != '') -" + filename);
										    								if(ul.checkVsandBythday(zp,taskQueue.get(1),taskQueue.get(0),taskQueue.get(2)))
										    								{
										    									listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after A08P16pr(bythday)+A08P03pr (condition if last bythday!=bythday and lasr fio != '')"+ filename);
									    									}else
									    									{
									    										System.out.println("VSnum is NO after A08P16pr(bythday)+A08P03pr (condition if last bythday!=bythday and lasr fio != '')"+ filename);
									    									}
										    							}
									    							}
				
									    						}
							    								else
							    								{
							    									System.out.println("�� ������� ����� ��� (condition if last bythday!= && !'' bythday)" + filename);
							    								}
							    							}
								    						
								    					}
			    								}		
				    					}
			    						else   
			    						{
			    							System.out.println("������ ��� � ������ Zp1 � � ���� Person");
			    							// if last bythday empty
			    							if(	!taskQueue.get(4).trim().equals("") && !taskQueue.get(5).trim().equals("") && !taskQueue.get(6).trim().equals("")	)
				    						{
			    								filename = new MessageA08p16(fileTransfer, res, person).create(); System.out.println("Send A08P16pr the set new sex - "+ filename);
		    									if(ul.waitUprak2(filename, "Wait response A03P16pr the set new sex -"+filename))
				    							{
		    										filename = new MessageA08p03pr_newDr(fileTransfer, res, person).create(); System.out.println("Send A08P03pr after chanche sex - "+ filename);
			    									if(ul.waitUprak2(filename, "Wait response A03P03pr after chanche sex "+filename))
					    							{
			    										filename = new MessageZp1(fileTransfer, res, person).create();
					    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03pr after chanche sex -" + filename);
					    								if(ul.checkVsandBythday(zp,taskQueue.get(1),taskQueue.get(0),taskQueue.get(2)))
					    								{
					    									listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after A08P16pr + A08P03pr after chanche sex"+ filename);
				    									}else
				    									{
				    										System.out.println("VSnum is NO after A08P16pr + A08P03pr after chanche sex"+ filename);
				    									}
					    							}
				    							}
				    						}	
		    									else
		    									{
		    										// if lf last all empty
		    										System.out.println(taskQueue.get(4)+" "+taskQueue.get(5)+" "+taskQueue.get(6)+" "+taskQueue.get(3));
		    										if(	taskQueue.get(4).trim().equals("") && taskQueue.get(5).trim().equals("") && taskQueue.get(6).trim().equals("")	&& taskQueue.get(3).trim().equals(""))
						    						{
		    											filename = new MessageA08p16emptyalllast(fileTransfer, res, person).create(); System.out.println("Send A08P16pr the set new sex (empty all last)- "+ filename);
		    											if(ul.waitUprak2(filename, "Wait response A03P03pr after chanche sex (empty all last) "+filename))
						    							{
		    												filename = new MessageA08p03(fileTransfer, res, person).create(); System.out.println("Send A08P03pr after chanche sex (empty all last)- "+ filename);
					    									if(ul.waitUprak2(filename, "Wait response A03P03pr after chanche sex (empty all last)-  "+filename))
							    							{
					    										filename = new MessageZp1(fileTransfer, res, person).create();
							    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03pr after chanche sex (empty all last)- " + filename);
							    								if(ul.checkVs(zp,taskQueue.get(1),taskQueue.get(0)))
							    								{
							    									listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after A08P16pr + A08P03pr after chanche sex (empty all last)- "+ filename);
							    								}
							    								else
							    								{
							    									System.out.println("VSnum is NO after A08P16pr + A08P03pr after chanche sex (empty all last)- "+ filename);
							    								}
							    							}
						    							}
						    						}
		    									}
				    						
			    							
			    						}
	    						}
	    						else{
		    							System.out.println("������� ��� ���������� ���( � ����� ����) �� ��������� �������� - "+filename);
		    							st.add(person.getPerson().getEnp());
		    							st.add(person.getPerson().getPersonAdd().getEnp());
		    							st.add(enpCalc);
		    							
		    							
										 
		    							/* 
		    							 * 1. ��������� ��� ������ ������� (�� �� P16 ����� ����� �������� � ���  � ��) �� ���������� ����� ��zp1 != �������� ��� ���zo1 != ��� ������ (� �������� ���� �����
		    							 *    ����� ���������� �� ������ ������ ����� ���...�� �� ������� ����� - ����� ����� � �������� �������� ������) 
		    							 * 2. ���� ����� ��� == ��� �� A08P16 (� ��������� � ������ ������(in) ����� ��� � �� �� zp1)
		    							 * 3. ������� ����� ��� ������� ��� � ����� ����
		    							 * 4. �08P03 (������ ��� � in ����� � pid)
		    							 */
		    							// last fiod == fiod || last fiod ""
		    							if((taskQueue.get(4).trim().equals("") && taskQueue.get(5).trim().equals("") && taskQueue.get(6).trim().equals("") && taskQueue.get(3).trim().equals("")) ||
		    									taskQueue.get(4).trim().equals(taskQueue.get(7).trim()) && taskQueue.get(5).trim().equals(taskQueue.get(8).trim()) && taskQueue.get(6).trim().equals(taskQueue.get(9).trim()) && taskQueue.get(3).trim().equals(taskQueue.get(2).trim()) 
		    									) 
		    							{
			    							filename = new MessageA08p16emptyalllast(fileTransfer, res, person).create(); System.out.println("Send A08P16 last fiod == fiod (features in1 pid7 pid8 from zp1)- "+ filename);
											if(ul.waitUprak2(filename, "Wait response A08P16 last fiod == fiod (features in1 pid7 pid8 from zp1) "+filename))
			    							{
												String oldenp = person.getPerson().getPersonAdd().getEnp();
												 person.newEnp();
												 Data oers = new Data(person.getPerson().getEnp());
												 String newenp = oers.getPerson().getPersonAdd().getEnp();
												person.getPerson().getPersonAdd().setEnp(newenp);
												st.add(newenp);
												filename = new MessageA08p03(fileTransfer, res, person).create(); System.out.println("Send A08P03 last fiod == fiod - "+ filename);
		    									if(ul.waitUprak2(filename, "Wait response A03P03 last fiod == fiod -  "+filename))
				    							{
		    										filename = new MessageZp1(fileTransfer, res, person).create();
				    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P03 last fiod == fiod- " + filename);
				    								if(ul.checkVs(zp,taskQueue.get(1),taskQueue.get(0)))
				    								{
				    									listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK after A08P16 + A08P03  last fiod == fiod  - "+ filename);
				    								}
				    								else
				    								{
				    									System.out.println("VSnum is NO after A08P16 + A08P03   last fiod == fiod  - "+ filename);
				    								}
				    							}
			    							}
		    							}
		    						
		    							
		    							chancheOutEnp.add(st);
	    							}
	    						//===========================================================================================
	    					 	}else
	    					    	// ���� ����� ZP1 ��� ZP1pr �������� �� �� 50000 �����
			    					    {
		    					    		if(	person.getPerson().getPersonAdd().getEnp().substring(2, 10).equals(enpCalc) ) 
		    					    		{
		    					    			System.out.println("Okato !=50000 EnpCalc OK");
		    					    			if(getNppZero(zp).equals(String.valueOf(Integer.valueOf(person.getPerson_sex()))) )
					    						{
		    					    				System.out.println("Okato !=50000 Sex OK ");
		    					    				//��������: lastdr = dr && dr = drzp1
		    					    				System.out.println("TEST "+ person.getZpPid7()+" "+taskQueue.get(2).subSequence(0, 10));
							    					if(taskQueue.get(2).equals(taskQueue.get(3)) && taskQueue.get(2).subSequence(0, 10).equals(person.getZpPid7()))
							    					{
							    						System.out.println("Okato !=50000 DR OK ");
						    					    	filename = new MessageA08p03pr_anouthOkato(fileTransfer, res, person).create(); System.out.println("Send A08P03pr_anouthOkato after ZP1 == NULL response and Zp1pr "+ filename);
						    					    	if(ul.waitUprak2(filename, "Wait response A08P03pr_anouthOkato"+filename))
						    							{
						    					    		filename = new MessageZp1pr(fileTransfer, res, person).create();
								    					    zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1pr after A08P03pr_anouthOkato");
								    					    person.setZpList(zp);
								    					    person.setZp(person.getZpList().get(0));
								    					    System.out.println("@@@@@@@@@@@@@@@@@@@@@���������@@@@@@@@@@@@@@@@@@@@ "+ person.getZp());
								    					    // ������-�� �� ���� �� �16
								    					    // ��� npp = 0
								    					    if(person.getZp().getIn1_15().trim().equals("50000"))
								    					    {
								    					    	filename = new MessageA08p16chanchelastfioOnnew(fileTransfer, res, person).create(); System.out.println("Send A08P16pr after A08P03pr_anouthOkato - "+ filename);
						    									if(ul.waitUprak2(filename, "Wait response A08P16pr after A08P03pr_anouthOkato "+filename))
								    							{
						    										filename = new MessageZp1(fileTransfer, res, person).create();
								    								zp = new ZpLoader().load(filename, taskQueue.get(0),"Wait response ZP1 after A08P16pr after A08P03pr_anouthOkato " + filename);
								    								if(ul.checkVs(zp,taskQueue.get(1),taskQueue.get(0)))
								    								{
								    									listEnpForDeleteFromExcel.add(taskQueue.get(0));	System.out.println("VSnum is OK fter A08P16pr after A08P03pr_anouthOkato "+ filename);
								    									//listcol.remove(0);
								    									//return;
								    								}
								    							}
								    					    }
								    					    else
								    					    {
								    					    	System.out.println("Exit becouse A08P03pr_anouthOkato is not OK");
								    					    	//listcol.remove(0);
						    									//return;
								    					    }
						    							}
							    					}
							    					else
							    					{
							    						System.out.println("Okato !=50000 DR bad ");
						    					    	/*
						    					    	 * �.�. ������ ��� ��� �������� ��������� (����� ���������) � � ������ �� zp1 �������� �� �� ��������������� ���
						    					    	 * + ��������� �� ��� ���� ������� �� �� ����������
						    					    	 * ��������
						    					    	 * p16 ��� ������ ����� � �� ������� �������!!!!  ������ ���� ��������
						    					    	 * p03 � ������� ��� �� ��� ���, ������������� �� ���� ����������
						    					    	 */
							    					}
					    						}	
		    					    		}    	
			    					    }
		    				}
		    				
		    				if(listcol.size()>0){listcol.remove(0);}
		    				
		    				System.out.println("NTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
		    			    return;
    					} catch (SAXException | IOException | InterruptedException e) {e.printStackTrace();} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
    			}
			}
			
		}


		 
		
	}
    
    
    /*
     * Method calculate and get from excel error.
     * Error - this my ruls 
     */
    private List<ArrayList<String>> createTasks(List<ArrayList<String>> ba)
    {
    	List<ArrayList<String>> taskQueue = new ArrayList<ArrayList<String>>();
        ArrayList<String> queue = null;					
		for(int i=1;i<ba.size();i++)
		{
			queue = new ArrayList<String>();
			queue.add(ba.get(i).get(1));// ��� ����� 0
			queue.add(ba.get(i).get(66));// �� 1
			queue.add(ba.get(i).get(13));// ���� ����� 2
			queue.add(ba.get(i).get(74));// ���� ������ 3
			queue.add(ba.get(i).get(71));// ��� ���� 4
			queue.add(ba.get(i).get(72));// ��� ���� 5
			queue.add(ba.get(i).get(73));// �������� ���� 6
			queue.add(ba.get(i).get(10));// ��� 7
			queue.add(ba.get(i).get(11));// ��� 8
			queue.add(ba.get(i).get(12));// �������� 9
			queue.add("������� �� ��������");
			if(ba.get(i).get(13).equals(ba.get(i).get(74))){	queue.add("����������� �� �� ���������");	}else{	queue.add("����������� ��");	}
			taskQueue.add(queue);
			
		}
		return taskQueue;
    }
    
	private void threads(List<ArrayList<String>> ts,List<String> listEnpForDeleteFromExcel,HttpServletRequest request) throws InterruptedException
    {
    	ExecutorService exec = Executors.newCachedThreadPool();
    	int numberOfTasks=0;
    	int allsize = 0;
    	double rez = 0;
    	int pr= 0; 
    	int active = Thread.activeCount();
    	System.out.println("Before start "+active);
    	for (int i = 0; i < ts.size(); i++) {
            UtilForErrorGz ut = new UtilForErrorGz();    		
    		exec.execute(new AutoThread(ts.get(i),ut,listEnpForDeleteFromExcel));
    		active = Thread.activeCount();
    		listcol.add(String.valueOf(++numberOfTasks)+"("+String.valueOf(active)+")");
    	}
    	exec.shutdown();
    	allsize = listcol.size();
    	while(listcol.size() !=0)
    	{
    		System.out.println("ls "+listcol);
    		rez = (double) (allsize - listcol.size())/allsize;
    		pr =(int) (rez *100.0);
    		request.getSession().setAttribute("size", pr);
    		//System.out.println("pr "+ pr);
    		active = Thread.activeCount();
    		Thread all[] = new Thread[active];
            Thread.enumerate(all);

            for (int i = 0; i < active; i++) {
               System.out.println(i + ": " + all[i]);
            }
    		Thread.sleep(10000);
    	}
    	pr = 100;
    	request.getSession().setAttribute("size", pr);
    	
    }
    

	/*
	 * delete from task excel row that is OK 
	 */
	public void deleteFromTaskXLS(List<String> listEnpForDeleteFromExcel,String absolutePath,ArrayList<ArrayList<String>> chanche) throws FileNotFoundException, IOException
	{
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(absolutePath));
		HSSFWorkbook wb = new HSSFWorkbook(fs);	
		HSSFSheet sheet = wb.getSheetAt(0);	
		HSSFRow excelRow = null;
		HSSFCell excelCell = null;
		int rows = sheet.getPhysicalNumberOfRows();
	
		int rowIndex = 0;
		for(int t=0;t< listEnpForDeleteFromExcel.size();t++)
		{
			int lastRowNum=sheet.getLastRowNum();
			String s = listEnpForDeleteFromExcel.get(t);
			for(int y=1;y<=lastRowNum;y++)
			  {
				  excelRow = sheet.getRow(y);
				  // ����� ������� � ��� � ������
				  excelCell = excelRow.getCell(1);
				  String enpfromExcel = excelCell.getStringCellValue();
				  // ����������� ��� �� ��������� � � ������� => ���� ����� �� ������� �������
				  if(s.equals(enpfromExcel))
				  {
					  System.out.println("Posicion row for delete "+excelCell.getRowIndex()+" Enp for deleting "+s+" lastRowNum= "+lastRowNum);
					  rowIndex = excelCell.getRowIndex();
				  }
			  }
			if(rowIndex>=0&&rowIndex<lastRowNum){ sheet.shiftRows(rowIndex+1,lastRowNum, -1);}
			if(rowIndex==lastRowNum)
			{
		        HSSFRow removingRow=sheet.getRow(rowIndex);
		        if(removingRow!=null){sheet.removeRow(removingRow);}
		    }
		}
		
		if(chanche.size()>0)
		{
			//wb.getSheet("�� ��������� �������� ���");
			sheet = wb.createSheet("�� ��������� �������� ���");
			
			excelRow = sheet.createRow(0);
			excelRow = sheet.getRow(0);			
			excelCell = excelRow.createCell(0);
			excelCell = excelRow.getCell(0);
			excelCell.setCellValue("���������� ���");
			
			excelCell = excelRow.createCell(1);
			excelCell = excelRow.getCell(1);
			excelCell.setCellValue("���� � ����� ��");
			
			excelCell = excelRow.createCell(2);
			excelCell = excelRow.getCell(2);
			excelCell.setCellValue("�����");
			
			excelCell = excelRow.createCell(3);
			excelCell = excelRow.getCell(3);
			excelCell.setCellValue("����� ���");
			
			for(int i= 1; i< chanche.size();i++)
			{
				ArrayList<String> row = chanche.get(i-1);
				
				excelRow = sheet.createRow(i);
				excelRow = sheet.getRow(i);			
				excelCell = excelRow.createCell(0);
				excelCell = excelRow.getCell(0);
				excelCell.setCellValue(row.get(0));
				
				excelCell = excelRow.createCell(1);
				excelCell = excelRow.getCell(1);
				excelCell.setCellValue(row.get(1));
				
				excelCell = excelRow.createCell(2);
				excelCell = excelRow.getCell(2);
				excelCell.setCellValue(row.get(2));
				
				excelCell = excelRow.createCell(3);
				excelCell = excelRow.getCell(3);
				excelCell.setCellValue(row.get(3));
			}
			
			
		}
		
		
		/*
		 * Before save this Excel, we will delete all files in folder of the uploadFilePath
		 */
		System.out.println("uploadFilePathTEST "+ uploadFilePath);
		System.out.println("absolutePathTEST "+ absolutePath);
		 File file = new File(uploadFilePath);
	        File[] files = file.listFiles(); 
	        for (File f:files) 
	        {
	        	if (f.isFile() && !f.getAbsolutePath().equals(absolutePath)) 
	            { 
	        		f.delete();
	        		System.out.println("successfully deleted "+ f.getAbsolutePath());
	            }else
	            {
            		System.out.println("cant delete a file due to open or error "+ absolutePath);
	            }
        	}
		
		chancheOutEnp = new ArrayList<ArrayList<String>>();
		System.out.println("������� ����������: " +listEnpForDeleteFromExcel.size());
		FileOutputStream fileOut = new FileOutputStream(absolutePath);
		wb.write(fileOut);
		fileOut.close();
	}
	
	private boolean equalsInitials(Person p,Data person)
	{
		if(person.getPerson() !=null && p != null)
		{
		if(p.getPerson_kindfirstname().equals(person.getPerson().getPerson_kindfirstname())){return true;}
		if(p.getPerson_kindlastname().equals(person.getPerson().getPerson_kindlastname())){return true;}
		if(p.getPerson_surname().equals(person.getPerson().getPerson_surname()))		  {return true;}
		if(p.getPerson_birthday().equals(person.getPerson().getPerson_birthday()))		  {return true;}
		}
		return false;
	}
	
	private String getNppZero(ArrayList<Zp>  zp)
	{
		String str = null;
		for(Zp m :zp)
		{
			if(m.getNpp()==0)
			{
				str = m.getPid8();
			}
		}
		
		return str;
	}
	
	private String getGeneralEnpZp1calc(ArrayList<Zp>  zp)
	{
		String str = null;
		for(Zp m :zp)
		{
			if(m.getNpp()==0)
			{
				str = m.getPid3cx1_1().substring(2, 10);
			}
		}
		
		return str;
	}
}
