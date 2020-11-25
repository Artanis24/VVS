package webserver;

import org.junit.Test;
import java.net.*;
import java.io.*;
import org.junit.*;
import java.nio.file.*;

public class TestServer {
	public static String HANDLER_REQUEST = "";
    public int PORT = 8080;
    public String WEBSERVER_ROOT = "Pages/notfound";
    public String WEBSERVER_MAINTENANCE = "Pages/maintenance";
    public String WEBSERVER_STATUS = "Stopped";
    
    @Test 
    public void ModifyServerStatus() {
    	WebServer server= new WebServer(PORT,WEBSERVER_ROOT,WEBSERVER_MAINTENANCE,WEBSERVER_STATUS);
    	server.updateStatus("START");
    	Assert.assertEquals("START",server.WEBSERVER_STATUS);
    }
    
    @Test 
    public void ModifyServerPort() {
    	WebServer server= new WebServer(PORT,WEBSERVER_ROOT,WEBSERVER_MAINTENANCE,WEBSERVER_STATUS);
    	server.updatePort(1);
    	Assert.assertEquals(1,server.PORT);
    }
    
    @Test
    public void HandleOneRequestServerRunningNoException()
    {
        Path a= Paths.get("E:/Downloads/webserver/Pages");
        Assert.assertTrue(Files.exists(a));
    }
    
    @Test
    public void TestGetFilePathIndex() {
    	WebServer server= new WebServer(PORT,WEBSERVER_ROOT,WEBSERVER_MAINTENANCE,WEBSERVER_STATUS);
    	String path="/";
    	Assert.assertEquals(Paths.get(WEBSERVER_ROOT, path),server.getFilePath(path));
    }
    
    @Test
    public void TestGetFilePathCSS() {
    	WebServer server= new WebServer(PORT,WEBSERVER_ROOT,WEBSERVER_MAINTENANCE,WEBSERVER_STATUS);
    	String css="/index.ccs";
    	Assert.assertEquals(Paths.get(WEBSERVER_ROOT, css),server.getFilePath(css));
    }
    
    @Test
    public void TestGetFilePathFavicon() {
    	WebServer server= new WebServer(PORT,WEBSERVER_ROOT,WEBSERVER_MAINTENANCE,WEBSERVER_STATUS);
    	String fav="/favicon.ico";
    	Assert.assertEquals(Paths.get(WEBSERVER_ROOT, fav),server.getFilePath(fav));
    }
    
    @Test
    public void HandleClientNullClient()
    {
        WebServer webserver = new WebServer(PORT,WEBSERVER_ROOT,WEBSERVER_MAINTENANCE,WEBSERVER_STATUS);
        webserver.handleClient(null);
    }
    
    @Test
    public void HandleClientRequestIndexPage() throws IOException
    {
        WebServer.HANDLER_REQUEST = "";
        WebServer webserver = new WebServer(PORT,WEBSERVER_ROOT,WEBSERVER_MAINTENANCE,WEBSERVER_STATUS);
        ServerSocket serverSocket = new ServerSocket(8080);
        Socket clientSocket = serverSocket.accept();
        webserver.handleClient(clientSocket); //pt html
        clientSocket = serverSocket.accept();
        webserver.handleClient(clientSocket); //pt css
        clientSocket = serverSocket.accept();
        webserver.handleClient(clientSocket); //pt favicon
        Assert.assertEquals(" E:/Downloads/webserver/Pages",WebServer.HANDLER_REQUEST);

        WebServer.HANDLER_REQUEST ="/index.html";
        serverSocket.close();
        clientSocket.close();
    }
}
