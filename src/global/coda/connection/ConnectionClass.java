package global.coda.connection;
import java.sql.*;
public class ConnectionClass {
 public static Connection establish() {
	 Connection con = null;
	 try {
		 con=DriverManager.getConnection("jdbc:mysql://localhost:3306/airportdb","root","root");
		
		}
	 catch (SQLException e) {
		
		e.printStackTrace();
	}
	return con;
 }
 public static void closeConnection(Connection c) throws SQLException {
	 c.close();
 }
}
