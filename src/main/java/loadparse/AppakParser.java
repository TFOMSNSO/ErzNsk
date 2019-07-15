package loadparse;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import oracle.AppakOracle;
import oracle.ConnectOracle;
import oracle.ConnectionPoolOracle;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AppakParser extends DefaultHandler {
	private String curElement = "";
	private AppakRecord appakRecord = new AppakRecord();
	private String error = "";
    AppakOracle appakTable = new AppakOracle();
   	private Statement statement;
   	private Connection conn;
   	int counter = 1;
	private boolean errExist = false;
	
    public AppakParser() {
    	super();
    }    	
 
    @Override 
    public void startDocument() throws SAXException { 
    	try {
    	 DataSource dataSource = ConnectionPoolOracle.getConnectionDataSource();
    	 conn = dataSource.getConnection();
         ConnectionPoolOracle.printStatus();
         statement = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("Start parse XML...");
    } 
     
    @Override 
    public void endDocument() { 
    	if (statement != null) {  try {statement.close();} catch (SQLException e) {e.printStackTrace();}  }
		if (conn != null) { try {conn.close();} catch (SQLException e) {e.printStackTrace();} System.out.println("������� � ���!");}
    	System.out.println("Stop parse XML...");
    } 
    
    @Override 
    public void startElement(String namespaceURI, String localName,
    		String qName, Attributes atts) throws SAXException { 
    	curElement = qName;
    	if (curElement.equals("ERR")) {
			errExist = true;
    	}
    } 
	
    @Override 
    public void endElement(String namespaceURI,
    		String localName, String qName) throws SAXException {
    	curElement = qName;
    	if (curElement.equals("ACK") && errExist) {
    		appakRecord.setERR(error);
    		appakTable.insertAppak(statement, appakRecord.getMSA_2(), appakRecord.getTAG(), appakRecord.getERR().replace("'", ""), appakRecord.getFILE_NAME());
    		System.out.println(counter++);
    	}
    	curElement = "";
    }
    
	@Override 
	public void characters(char[] ch, int start, int length) throws SAXException {
		switch (curElement) {
			case "MSA.2": 
				if (length < 36){
					String curMsa2 = appakRecord.getMSA_2();
					appakRecord.setMSA_2((curMsa2.length() == 36 ? "" : curMsa2) + new String(ch, start, length));
				} else {
					appakRecord.setMSA_2(new String(ch, start, length));
				}
		    	error = "";
		    	errExist = false;
		    	break;
			case "BHS.11": 
				appakRecord.setFILE_NAME(new String(ch, start, length));
				System.out.println(counter++);
				break;				
			case "CWE.1": 
				error = error + " " + new String(ch, start, length);
				break;
			case "CWE.2": 
				error = error + " " +  new String(ch, start, length) + " ";
				break;
			case "ERR.6": 
				error = error + " " +  new String(ch, start, length) + " ";
				break;					
			case "ERL.1": 
				appakRecord.setTAG(new String(ch, start, length));
				break;
		}
	} 

}
