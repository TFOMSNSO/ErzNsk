package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import util.UtilParseDbXml;

import com.google.gson.Gson;

import model.other.ZpLoadMock2;

/**
 * Servlet implementation class ActionServlet
 */

@WebServlet("/ImportZP1fromXMLToHandsontable")
public class ImportZP1fromXMLToHandsontable extends HttpServlet {
	
	public ImportZP1fromXMLToHandsontable() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	private static final long serialVersionUID = 1L;


  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  	System.out.println("reeques:" + request);
	  String json = null ;
	  //����� ������ � webSocketAnswer.js
	  String uprak2 = request.getParameter("uprak2");
	  String upr = request.getParameter("datauprmessZP");
	  //String kluch = request.getParameter("kluch");
	  // ������ ������ � ����� ����
	  //ArrayList<ArrayList<String>> parsedatauprmessZP1 = parsedatauprmessZP(upr);
	  ArrayList<ArrayList<String>>  parsedatauprmessZP1 = null;
	  try {
	  	parsedatauprmessZP1 = new UtilParseDbXml().unMarshalingExample(upr.replaceAll(".fromdbforuprmess", "").trim());
	  	for(int i =0; i < parsedatauprmessZP1.size(); i++){
	  		System.out.println("parsedatauprmessZP1[" + i + "]=" + parsedatauprmessZP1.get(i));
		}
	  } catch (JAXBException e) {
	  	e.printStackTrace();
	  }
	  
	  // ������ uprak2. ���� ����� ��� ����������� ����� ����� ������ ������� ����� �������.    
	  ZpLoadMock2 zpLoad = new ZpLoadMock2();
	  ArrayList<ArrayList<String>>  parsedUprak2 = zpLoad.load(uprak2);
	  System.out.println("parsed uprak2:" + parsedUprak2);
	  ArrayList<ArrayList<String>> parsedatauprak2ZP1 = parse(parsedUprak2);
	  compare(parsedatauprmessZP1,parsedatauprak2ZP1);
	  Map<String, ArrayList<ArrayList<String>>> ind = new LinkedHashMap<>();
	  System.out.println("parsedatauprak2zp1:");
	  for(int i =0; i < parsedatauprak2ZP1.size(); i++){
	  	System.out.println(parsedatauprak2ZP1.get(i));
	  }
	  System.out.println("parsedatauprmesszp1:");
	  for(int i = 0; i < parsedatauprmessZP1.size(); i++){
	  	System.out.println(parsedatauprmessZP1.get(i));
	  }
	  ind.put("data1zp1ajax", parsedatauprak2ZP1);
	  ind.put("data2upr", parsedatauprmessZP1);
      json= new Gson().toJson(ind);   
      System.out.println("json:" + json);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	  
  }


 protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  // TODO Auto-generated method stub
  
 }
 
  private ArrayList<ArrayList<String>>  parse(ArrayList<ArrayList<String>> list)
  {
	  ArrayList<String> zpRecordOnList = new ArrayList<String>();
  	
  	zpRecordOnList.add("ENP");zpRecordOnList.add("PERSON_SURNAME");zpRecordOnList.add("PERSON_KINDFIRSTNAME");zpRecordOnList.add("PERSON_KINDLASTNAME");zpRecordOnList.add("PERSON_BIRTHDAY");
  	zpRecordOnList.add("GD");zpRecordOnList.add("ENP_1");zpRecordOnList.add("ENP_2");zpRecordOnList.add("OKATO_2");
  	zpRecordOnList.add("SMO");zpRecordOnList.add("D_12");zpRecordOnList.add("D_13");zpRecordOnList.add("OKATO_3");zpRecordOnList.add("TYPE_POL");
  	zpRecordOnList.add("POL");zpRecordOnList.add("QRI_1");zpRecordOnList.add("QRI_2");zpRecordOnList.add("QRI_3");zpRecordOnList.add("QRI_4");zpRecordOnList.add("NPP");zpRecordOnList.add("D_INPUT");
  	zpRecordOnList.add("PID7");zpRecordOnList.add("PID8");zpRecordOnList.add("PID29");
  	 list.add(0, zpRecordOnList);
	  return list;
  }
  
 
  
  private void compare(ArrayList<ArrayList<String>> parsedatauprmessZP1,ArrayList<ArrayList<String>> parsedatauprak2ZP1 )
  {
  	System.out.println("------------------------------------before compare:\nparsedatauprmessZP1(0) size:" + parsedatauprak2ZP1.get(0).size());
  	for(int i =0; i < parsedatauprmessZP1.size(); i++){
  		System.out.println(i + ":" + parsedatauprmessZP1.get(i));
	}
	System.out.println("\nparsedataaprak2ZP1:");
	for(int i = 0; i < parsedatauprak2ZP1.size(); i++){
		System.out.println(i + ":" + parsedatauprak2ZP1.get(i));
	}

	  for (int i = 0; i < parsedatauprmessZP1.size(); i++)
	  {
		for (int j = 0; j < parsedatauprak2ZP1.size() ; j++)
		{
			if(parsedatauprmessZP1.get(i).get(68).trim().equals(parsedatauprak2ZP1.get(j).get(0).trim()) )
			{
				parsedatauprak2ZP1.get(j).add(0, parsedatauprmessZP1.get(i).get(3));	// birthday
				parsedatauprak2ZP1.get(j).add(0, parsedatauprmessZP1.get(i).get(2));	// lastname
				parsedatauprak2ZP1.get(j).add(0, parsedatauprmessZP1.get(i).get(1));	// name
				parsedatauprak2ZP1.get(j).add(0, parsedatauprmessZP1.get(i).get(0));	// surname
				parsedatauprak2ZP1.get(j).add(0, parsedatauprmessZP1.get(i).get(26)); // enp
			}
			
			// msa && npp
			if(	parsedatauprmessZP1.get(i).get(68).trim().equals(parsedatauprak2ZP1.get(j).get(5).trim()) && parsedatauprak2ZP1.get(j).get(19).trim().equals("100"))
			{
				// ��������� �� ������ ���� ������� zp1ok 
				parsedatauprmessZP1.get(i).add("1");
			
				// UDLfromZp1fiod
				if(parsedatauprak2ZP1.get(j).get(15).trim().equals("H01") || parsedatauprak2ZP1.get(j).get(15).trim().equals("H02") || (parsedatauprak2ZP1.get(j).get(16) != null && parsedatauprak2ZP1.get(j).get(16).trim().equals("H02")) || (parsedatauprak2ZP1.get(j).get(16) != null && parsedatauprak2ZP1.get(j).get(16).trim().equals("H01")))
				{
//					
					parsedatauprmessZP1.get(i).add("okUdl");
				}
				else{parsedatauprmessZP1.get(i).add("noUdl");}
				
				//EnpOutOur=EnpOutFedF
				if(parsedatauprak2ZP1.get(j).get(6).trim().equals(parsedatauprmessZP1.get(i).get(39).trim()) )
				{
					parsedatauprmessZP1.get(i).add("okEnp");
				}
				else if(parsedatauprak2ZP1.get(j).get(6).trim().equals(parsedatauprmessZP1.get(i).get(26).trim()) && parsedatauprmessZP1.get(i).get(39).trim().equals(""))
				{
					parsedatauprmessZP1.get(i).add("okEnp");
				}
				else{parsedatauprmessZP1.get(i).add("noEnp");}
				
			}
			
			
		}
	  }

	  System.out.println("\n\n------------------------------------after compare:\nparsedatauprmessZP1:");
	  for(int i =0; i < parsedatauprmessZP1.size(); i++){
		  System.out.println(i + ":" + parsedatauprmessZP1.get(i));
	  }
	  System.out.println("\nparsedataaprak2ZP1:");
	  for(int i = 0; i < parsedatauprak2ZP1.size(); i++){
		  System.out.println(i + ":" + parsedatauprak2ZP1.get(i));
	  }

	  System.out.println("\n\n");

  }
}