javac -cp %CATALINA_HOME%\lib\servlet-api.jar -d WebContent\WEB-INF\classes src\servlet\FormServlet.java
javac -cp %CATALINA_HOME%\lib\servlet-api.jar -d WebContent\WEB-INF\classes src\servlet\SubmissionServlet.java
jar cfv deploy\AeroParkerRegistrationForm.war -C WebContent .
copy deploy\AeroParkerRegistrationForm.war %CATALINA_HOME%\webapps\AeroParkerRegistrationForm.war 
%CATALINA_HOME%\bin\startup.bat