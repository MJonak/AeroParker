package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.*;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FormServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String[] colNames, colLabels; 
	private String bootstrapCDN = "<meta charset=\"utf-8\"> <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.2.0/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-gH2yIJqKdNHPEq0n4Mqa/HGKIhSkIHeL5AyhkYV8i59U5AR6csBvApHHNl/vI1Bx\" crossorigin=\"anonymous\">";;
	
	public void init(ServletConfig config) {
		
		//Commented out as the properties were causing too many issues, but this would be my next step
//		Properties appProps = new Properties();
//		InputStream is = getClass().getResourceAsStream("config.properties");
//		try {
//			appProps.load(is);
//		} catch (Exception e) {
//			log("Exception occurred when attempting to load configuration file in init()", e);
//		}
		
		String url = "jdbc:mysql://localhost:3306/";
		String dbName = "aeroparker";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "root";
		String password = ""; 

    	Connection conn = null;
     	PreparedStatement prepStmnt = null;
     	ResultSet resultSet = null;
    	ResultSetMetaData rsmd = null;
    	
     	try {  
            Class.forName(driver);  
            conn = DriverManager.getConnection(url + dbName, userName, password);  

            prepStmnt = conn.prepareStatement("SELECT * FROM Customers LIMIT 1"); 

            resultSet = prepStmnt.executeQuery();  
            rsmd = resultSet.getMetaData();
            int numCols = rsmd.getColumnCount();
            colNames = new String[numCols];
            colLabels = new String[numCols];
            for (int i = 0; i < numCols; i++) {
            	colNames[i] = rsmd.getColumnName(i+1);
            	colLabels[i] = rsmd.getColumnLabel(i+1);
			}
     	} catch (SQLException e) {
     		log("Exception occured when attempting to fetch column names from the database.", e);
     	} catch (ClassNotFoundException e) {
     		log("Exception occured when attempting to fetch column names from the database.", e);
     	}
    }
	
	private String getInputTypeForColumn(String colName) {
		switch (colName) {
		case "Email":
			return "email";
		case "PhoneNumber":
			return "tel";
		default:
			return "text";
		}
	}
    
	private String getElementEventFunctionCalls(String colName) {
		String eventFuncCalls = "";
		switch (colName) {
		case "Email":
			eventFuncCalls += " oninput=\"validateWithinSizeLimit(this, this.value, 7, 255)\"";
			break;
		case "Title":
			eventFuncCalls += " oninput=\"validateWithinSizeLimit(this, this.value, 2, 5)\"";
			break;
		case "FirstName":
		case "LastName":
			eventFuncCalls += " oninput=\"validateWithinSizeLimit(this, this.value, 2, 50)\"";
			break;
		case "AddressLine1":
			eventFuncCalls += " oninput=\"validateWithinSizeLimit(this, this.value, 2, 255)\"";
			break;
		case "AddressLine2":
		case "City":
			eventFuncCalls += " oninput=\"validateWithinSizeLimit(this, this.value, 0, 255)\"";
			break;
		case "Postcode":
			eventFuncCalls += " oninput=\"validateWithinSizeLimit(this, this.value, 4, 10)\"";
			break;
		case "PhoneNumber":
			eventFuncCalls += " oninput=\"validateWithinSizeLimit(this, this.value, 0, 20)\"";
			break;
		}
		
		return eventFuncCalls + " onblur=\"validateAllInputsOK()\"";
	}

	private String getFormElementForColumn(String colName, String colLabel) {
		String eventFuncCalls = getElementEventFunctionCalls(colName);
		String element = "<div class=\"form-group\">";
	    element += "<label for=\""+colName+"\" class=\"mb-1\">"+colLabel+"</label>";
	    element += "<input id=\""+colName+"\" name=\""+colName+"\"type=\""+getInputTypeForColumn(colName)+"\" class=\"form-control mb-2\" id=\""+colName+"\" "+eventFuncCalls+" >";
	    element += "</div>";
	  
		return element;
	}
	
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter writer = response.getWriter();

        String site = request.getParameter("site");
        site = (site != null) ? site : "Avalon+City";
        
    	String formContent = "";
    	for (int i = 2; i < colNames.length; i++) {
    		formContent += getFormElementForColumn(colNames[i], colLabels[i]);
    	}
    	formContent += "<button id=\"submitButton\" type=\"submit\" class=\"btn btn-primary\">Submit</button>";
    	String form = "<form action=\"./success?site="+site+"\" method=\"POST\" >"+ formContent +"</form>";
    	
    	String jsSource = request.getContextPath() + "/functions.js"; 
    	String heading = "<h2 class=\"mt-3 mb-2\">AeroParker Registration Form</h2>";
    	String head = "<head> <title>AeroParker Registration Form</title>" + bootstrapCDN + "<script language=\"text/javascript\" src=\""+ jsSource +"\"> </script>" + "</head>";
    	String body = "<body> <div class=\"container\"> <div class=\"row\"> <div class=\"col\"></div> <div class = \"col-5\">"+ heading + form +"</div> <div class=\"col\"></div> </div> </div> </body>";
    	String html = "<html>" + head + body + "</html>";
    	writer.println(html);

    }
}
