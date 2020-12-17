package webserver.webserver;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@FixMethodOrder(MethodSorters.JVM)
public class WebServerTest {

    public static final int CLIENT_PORT = 8080;
    public static final String WEBSERVER_ROOT_DIRECTORY = buildPath("");
    public static final String WEBSERVER_MAINTENANCE_DIRECTORY = buildPath("", "maintenance");
    public static final String WEBSERVER_STATUS = "Running";
    public static final int WEBSERVER_PORT = 8080;

    private static WebServer buildDefaultWebServer() {
        return new WebServer(WEBSERVER_PORT, WEBSERVER_ROOT_DIRECTORY, WEBSERVER_MAINTENANCE_DIRECTORY, WEBSERVER_STATUS);
    }

    private static String buildPath(String... childs) {
        return Paths.get("Pages", childs).toFile().getAbsolutePath();
    }

    @Test(expected = NullPointerException.class)
    public void setWebserverRootDirectoryToNull() {
        new WebServer(WEBSERVER_PORT, null, WEBSERVER_MAINTENANCE_DIRECTORY, WEBSERVER_STATUS);
    }

    @Test
    public void updateWebserverPort() {
        int expectedPort = 12345;

        WebServer webserver = buildDefaultWebServer();
        webserver.updateWebserverPort(expectedPort);
        Assert.assertEquals(expectedPort, webserver.port);
    }

    @Test
    public void updateWebserverDirectoryWithCompletelyCorrectDirectory() {
        String expectedPath = buildPath("My Pages", "Pages");
        WebServer webserver = buildDefaultWebServer();
        webserver.updateWebserverRootDirectory(expectedPath);
        Assert.assertEquals(expectedPath, webserver.webserverRootDirectory);
    }

    @Test
    public void updateWebserverMaintenanceDirectoryWithCorrectDirectory() {
        String expectedPath = buildPath("My Pages", "Pages", "Maintenance");

        WebServer webserver = buildDefaultWebServer();
        webserver.updateWebserverMaintenanceDirectory(expectedPath);
        Assert.assertEquals(expectedPath, webserver.webserverMaintenanceDirectory);
    }

    @Test
    public void updateWebserverStatus() {
        WebServer webserver = buildDefaultWebServer();
        webserver.updateWebserverStatus("Maintenance");
        Assert.assertEquals("Maintenance", webserver.webserverStatus);
    }

    @Test
    public void getFilePathIndexHtmlRequestedBySlash() {
        String expectedPath = buildPath("", "", "index.html");

        WebServer webserver = buildDefaultWebServer();
        Path path = webserver.getFilePath("/");
        Assert.assertEquals(expectedPath, path.toString());
    }

    @Test
    public void getFilePathIndexCssRequested() {
        String expectedPath = buildPath("", "", "index.css");

        WebServer webserver = buildDefaultWebServer();
        Path path = webserver.getFilePath("/index.css");
        Assert.assertEquals(expectedPath, path.toString());
    }

    @Test
    public void getFilePathFaviconRequested() {
        String expectedPath = buildPath("", "favicon.ico");

        WebServer webserver = buildDefaultWebServer();
        Path path = webserver.getFilePath("/favicon.ico");
        Assert.assertEquals(expectedPath, path.toString());
    }

    @Test
    public void getFilePathACompleteExistentPathRequested() {
        String expectedPath = buildPath("", "p1", "p1.html");

        WebServer webserver = buildDefaultWebServer();
        Path path = webserver.getFilePath(expectedPath);
        Assert.assertEquals(expectedPath, path.toString());
    }

    @Test
    public void getFilePathInexistentOddPathRequested() {
        WebServer webserver = buildDefaultWebServer();
        Path path = webserver.getFilePath("Hey");
        Assert.assertEquals("Hey", path.toString());
    }

    @Test(expected = IOException.class)
    public void sendResponseWithExceptionThrown() throws IOException {
        String expectedPath = buildPath("", "", "index.html");

        WebServer webserver = buildDefaultWebServer();
        Socket clientSocket;
        try (ServerSocket serverSocket = new ServerSocket(CLIENT_PORT)) {
            clientSocket = serverSocket.accept();
        }
        Path path = Paths.get(expectedPath);

        OutputStream out = clientSocket.getOutputStream();
        out.close();
        webserver.sendResponse(out, "200 OK", "text/html", Files.readAllBytes(path));//html only
    }

    @Test
    public void handleClientWithNullClient() {
        WebServer webserver = buildDefaultWebServer();
        webserver.handleClient(null);
    }

    @Test
    public void handleOneRequestWithRunningServerIOExceptionWhenCreatingAServerSocket() {

        WebServer webserver = new WebServer(CLIENT_PORT, WEBSERVER_ROOT_DIRECTORY, WEBSERVER_MAINTENANCE_DIRECTORY, WEBSERVER_STATUS);
        WebServer webserver2 = new WebServer(CLIENT_PORT, WEBSERVER_ROOT_DIRECTORY, WEBSERVER_MAINTENANCE_DIRECTORY, WEBSERVER_STATUS);
        webserver.handleOneRequest(); //html
        webserver2.handleOneRequest(); //html
        webserver.handleOneRequest(); //css
        webserver.handleOneRequest(); //favicon

    }

}
