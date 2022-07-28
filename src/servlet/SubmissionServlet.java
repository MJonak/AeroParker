package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SubmissionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;  
	private String[] colNames; 
	String url = "jdbc:mysql://localhost:3306/";
	String dbName = "aeroparker";
	String driver = "com.mysql.jdbc.Driver";
	String userName = "root";
	String password = "";
	String bootstrapCDN = "<meta charset=\"utf-8\"> <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.2.0/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-gH2yIJqKdNHPEq0n4Mqa/HGKIhSkIHeL5AyhkYV8i59U5AR6csBvApHHNl/vI1Bx\" crossorigin=\"anonymous\">"; 

	public void init(ServletConfig config) {
//		Properties appProps = new Properties();
//		try {
//			appProps.load(new FileInputStream(appConfigPath));
//		} catch (Exception e) {
//			log("Exception occurred when attempting to load configuration file in init()", e);
//		}

     	Connection conn = null;
     	PreparedStatement prepStmnt = null;
     	ResultSet rs = null;
    	ResultSetMetaData rsmd = null;
    	
     	try {  
            Class.forName(driver);  
            conn = DriverManager.getConnection(url + dbName, userName, password);  

            prepStmnt = conn.prepareStatement("SELECT * FROM Customers LIMIT 1"); 

            rs = prepStmnt.executeQuery();  
            rsmd = rs.getMetaData();
            int numCols = rsmd.getColumnCount();
            colNames = new String[numCols];
            for (int i = 0; i < numCols; i++) {
            	colNames[i] = rsmd.getColumnName(i+1);
			}
     	} catch (SQLException e) {
     		log("Exception occured when attempting to fetch column names from the database.", e);
     	} catch (ClassNotFoundException e) {
     		log("Exception occured when attempting to fetch column names from the database.", e);
     	}
    }
	
	//TODO Implement doGet() which redirects the user to the form again
     
	private boolean formDataIsValid(HttpServletRequest request) {
		for (int i = 2; i < colNames.length; i++) {
			String nextParamName = colNames[i];
			String nextParamValue = request.getParameter(nextParamName);
			
			switch (nextParamName) {
			case "Email":
				if (nextParamValue.length() == 0 || nextParamValue.length() > 255) {
					return false;
				}
				Pattern pattern = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
				Matcher matcher = pattern.matcher(nextParamValue);
				if (!matcher.find() ) {
					return false;
				}
				break;
			case "Title":
				if (nextParamValue.length() == 0 || nextParamValue.length() > 5) {
					return false;
				}
				break;
			case "FirstName":
			case "LastName":
				if (nextParamValue.length() == 0 || nextParamValue.length() > 50) {
					return false;
				}
				break;
			case "AddressLine1":
				if (nextParamValue.length() == 0 || nextParamValue.length() > 255) {
					return false;
				}
				break;
			case "AddressLine2":
			case "City":	
				if (nextParamValue.length() > 255) {
					return false;
				}
				break;
			case "Postcode":
				if (nextParamValue.length() == 0 || nextParamValue.length() > 10) {
					return false;
				}
				break;
			case "PhoneNumber":
				if (nextParamValue.length() == 0 || nextParamValue.length() > 20) {
					return false;
				}
				break;
			default:
				break;
			}
		}
		return true;
	}
	
	private String jsPopUpAndRedirect(String msg, String targetURI) {
		
		String script = "<script type='text/javascript'>";
		script += "window.alert(\""+msg+"\");";
		script += "window.location.replace(\""+targetURI+"\");";
		script += "</script>";
		return "<head> <title>AeroParker Registration Form</title> " + script +"</head>";
	}
	
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {       	
    	PrintWriter writer = response.getWriter();  	

    	if(!formDataIsValid(request)) {
    		writer.println(jsPopUpAndRedirect(
    			"The information entered into the form is invalid, please fill the form out again.", 
    			request.getContextPath()
    		));     		
    		return;
     	}
    	
     	Connection conn = null;
     	PreparedStatement prepStmnt = null;
     	ResultSet resultSet = null;
     	String site = request.getParameter("site");
     	String siteID = "0";
     	String customerID = "0";
     	
     	site = URLDecoder.decode(site, "UTF-8")  ;

     	try {
     		Class.forName(driver);
     		conn = DriverManager.getConnection(url + dbName, userName, password);
     		
     		prepStmnt = conn.prepareStatement("SELECT ID FROM Sites WHERE Name=\""+site+"\" LIMIT 1");
     		resultSet = prepStmnt.executeQuery();
     		if(!resultSet.next()) {
        		writer.println(jsPopUpAndRedirect(
            		"The site supplied is invalid", 
            		request.getContextPath()
            	));    
     			return;
     		} else {
     			siteID = resultSet.getString(1);
     		}
     			
     		prepStmnt = conn.prepareStatement("SELECT ID FROM Customers WHERE UPPER(Email)=\""+request.getParameter("Email").toUpperCase()+"\"");
     		resultSet = prepStmnt.executeQuery();
     		if(resultSet.next()) {
     			//The customer has been found in the customers table
     			customerID = resultSet.getString(1);
     		}
     		
     		prepStmnt = conn.prepareStatement("SELECT * FROM Customer_Sites WHERE Site_ID=\""+siteID+"\" AND Customer_ID=\""+customerID+"\"");
     		resultSet = prepStmnt.executeQuery();
     		if(resultSet.next()) {
        		writer.println(jsPopUpAndRedirect(
            		"You are already registered for this site", 
            		request.getContextPath()
            	));     		
     			return;
     		}
     		
     	} catch (SQLException e) {
     		log("Exception occured when attempting to query DB for existing records", e);
     	} catch (ClassNotFoundException e) {
     		log("Exception occured when attempting to query DB for existing records", e);
     	}
     	
     	ArrayList<String> sqlStatements = new ArrayList<String>();
     	
     	if (Integer.valueOf(customerID) < 1) {
     		String stmt1 = "INSERT INTO Customers (";
     		String stmt2 = "VALUES ("; 
     		for (int i = 2; i < colNames.length; i++) {
     			String nextParamName = colNames[i];
     			stmt1 += nextParamName;
     			stmt2 += "\"" + request.getParameter(nextParamName) + "\"";
     			if (i<colNames.length-1) {
     				stmt1 += " ,";
     				stmt2 += " ,";
     			} else {
     				stmt1 += ")";
     				stmt2 += ");";
     				break;
     			}
     		}
     		String sqlStmt = stmt1 + " " + stmt2;
     		sqlStatements.add(sqlStmt);
     	}
     	sqlStatements.add("INSERT INTO Customer_Sites (Customer_ID, Site_ID) VALUES ("+ customerID + ", "+ siteID +");");
     	
     	
     	try {  
            Class.forName(driver);  
            conn = DriverManager.getConnection(url + dbName, userName, password);  

            for (String stmt : sqlStatements) {
            	prepStmnt = conn.prepareStatement(stmt); 
            	prepStmnt.executeUpdate();
            }

     	} catch (SQLException e) {
     		String head = "<head>" + bootstrapCDN + "</head>";
     		String body = "<body> <div class=\\\"container\\\"> <h1>There was a problem when attempting to save your submission.</h1> <p>Please try again. If this error persists please contact a system administrator.</p> </div></body>"; 
     		writer.println("<html> "+ head + body +" </html>");
     		log("Exception occured when attempting to update DB with new record", e);
     		return;
     	} catch (ClassNotFoundException e) {
     		log("Exception occured when attempting to update DB with new record", e);
     	} finally {  
            if (conn != null) {  
                try {  
                    conn.close();  
                } catch (SQLException e) {  
                    e.printStackTrace();  
                }  
            }  
            if (prepStmnt != null) {  
            	try {  
            		prepStmnt.close();  
            	} catch (SQLException e) {  
            		e.printStackTrace();  
            	}  
            }  
     	}	
     	String head = jsPopUpAndRedirect("Success! Your submission has been recorded", request.getContextPath());
     	String body = "<body> </body>";
     	String html = "<html>" + head + body + "</html>";
     	writer.println(html);
     	
     	writer.flush();
    }
}
