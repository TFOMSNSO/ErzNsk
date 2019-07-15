package oracle;

import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;



public class ConnectionPoolOracle 
{
	
	
	private ConnectionPoolOracle()
	{
		
	}
	
	private static String DRIVER;
	private static String URL;
	private static String USERNAME;
	private static String PASSWORD;

    private static GenericObjectPool connectionPool;
    private static javax.sql.DataSource pool; 
    
    static
    {
    	try{
    		
			pool = setUp();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
    }
    
    

    private static DataSource setUp() throws Exception
    {
    	Properties properties = new Properties();
    	InputStream inputStream = ConnectionPoolOracle.class.getClassLoader().getResourceAsStream("oracle.properties");
    	System.out.println("e:" + ConnectionPoolOracle.class.getClassLoader().toString());
    	if(inputStream == null) System.out.println("input steam null");
    	properties.load(inputStream);
    	DRIVER = properties.getProperty("jdbc.driver");
    	System.out.println("DRIVER:"+DRIVER);
    	//Locale.setDefault(Locale.ENGLISH); // use this for change NLS
    	URL = "jdbc:oracle:thin:@" +properties.getProperty("jdbc.server")+ ":" + properties.getProperty("jdbc.port") + "/" + properties.getProperty("jdbc.sid");
    	USERNAME = properties.getProperty("jdbc.username");
    	PASSWORD = properties.getProperty("jdbc.password");
        System.out.println("URL:"+URL);
        System.out.println("user:"+USERNAME);
        System.out.println("pass:"+PASSWORD);
    	
    	 //  String URL = "jdbc:oracle:thin:@//"+khost+":"+kport+"/"+kbasename;

    	//DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
    	  // Connection con = DriverManager.getConnection(URL,kuser,kpassword);
        //
        // Load JDBC Driver class.
        //
    	Class.forName(DRIVER);
       // Class.forName(ConnectionPoolExample.DRIVER).newInstance();

        //
        // Creates an instance of GenericObjectPool that holds our
        // pool of connections object.
        //
        connectionPool = new GenericObjectPool();
        connectionPool.setMaxActive(	Integer.parseInt(properties.getProperty("jdbc.setMaxActive"))	);

        //
        // Creates a connection factory object which will be use by
        // the pool to create the connection object. We passes the
        // JDBC url info, username and password.
        //
        ConnectionFactory cf = new DriverManagerConnectionFactory(URL,USERNAME,PASSWORD);
        
        //
        // Creates a PoolableConnectionFactory that will wraps the
        // connection object created by the ConnectionFactory to add
        // object pooling functionality.
        //
        
        PoolableConnectionFactory factory = new PoolableConnectionFactory(cf, connectionPool,null, null, false, true);
     	if(connectionPool != null){	System.out.println("Connect JDBC Pool Oracle");}


        return new PoolingDataSource(factory.getPool());
        //return new PoolingDataSource(connectionPool);
    }

    public static GenericObjectPool getConnectionPool() {
        return connectionPool;
    }
    
    public static javax.sql.DataSource getConnectionDataSource() {
        //return new PoolingDataSource(connectionPool);
    	return pool;
    }
    

    /**
     * Prints connection pool status.
     */
    public static void printStatus() {
        System.out.println("Max   : " + getConnectionPool().getMaxActive() + "; " +
            "Active: " + getConnectionPool().getNumActive() + "; " +
            "Idle  : " + getConnectionPool().getNumIdle());
    }
}