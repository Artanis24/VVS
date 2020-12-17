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
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

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

    //getFilePath

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

    @Test
    public void sendResponseWithNoExceptionThrownPageAcceptedAndCorrectlyDisplayed() throws IOException {
        String expectedPath = buildPath("", "", "index.html");

        WebServer webserver = buildDefaultWebServer();
        Socket clientSocket;
        try (ServerSocket serverSocket = new ServerSocket(CLIENT_PORT)) {
            clientSocket = serverSocket.accept();
        }
        Path path = Paths.get(expectedPath);

        OutputStream out = clientSocket.getOutputStream();
        //trebuie accesat localhost:8080 in browser ca sa se finalizeze testul
        webserver.sendResponse(out, "200 OK", "text/html", Files.readAllBytes(path));//html only
        out.close();
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
        out.close();//exception is given because the client disconnected
        //trebuie accesat localhost:8080 in browser ca sa se finalizeze testul
        webserver.sendResponse(out, "200 OK", "text/html", Files.readAllBytes(path));//html only
    }

    @Test
    public void handleClientWithNullClient() {
        WebServer webserver = buildDefaultWebServer();
        webserver.handleClient(null);
    }

    //cant force it to not enter the while with the readline -- this is impossible since when you connect, you automatically send some request

    /*@Test
    public void handleClientWithRunningServerAndRequestForIndexPage() throws IOException {
        //connect to localhost:8080
        //request the index
        WebServer webserver = new WebServer(WEBSERVER_PORT + 1, WEBSERVER_ROOT_DIRECTORY, WEBSERVER_MAINTENANCE_DIRECTORY, "Maintenance");
        try (ServerSocket serverSocket = new ServerSocket(CLIENT_PORT + 2)) {
            Socket clientSocket;

            clientSocket = serverSocket.accept();
            webserver.handleClient(clientSocket); //html

            clientSocket = serverSocket.accept();
            webserver.handleClient(clientSocket); //css

            clientSocket = serverSocket.accept();
            webserver.handleClient(clientSocket); //favicon
        }

        //Due to how the handlerequests attribute is constructed, the first character will be a space
        String index = buildPath("", "", "index.html");
        String css = buildPath("", "", "index.css");
        String ico = buildPath("", "favicon.ico");

        String expectedRes = " " + index + " " + css + " " + ico;

        Assert.assertEquals(expectedRes, webserver.handledRequests);
    }*/

    @Test
    public void handleClientWithRunningServerAndRequestForUnknownPage() throws IOException {
        //connect to localhost:8080
        WebServer webserver = buildDefaultWebServer();
        webserver.handledRequests = "";
        ServerSocket serverSocket = new ServerSocket(CLIENT_PORT);

        Socket clientSocket = serverSocket.accept();
        webserver.handleClient(clientSocket); //html

        clientSocket = serverSocket.accept();
        webserver.handleClient(clientSocket); //css

        clientSocket = serverSocket.accept();
        webserver.handleClient(clientSocket); //favicon

        //Due to how the handlerequests attribute is constructed, the first character will be a space
        String index = buildPath("", "", "bubu.html");
        String css = buildPath("", "", "notfound.css");
        String ico = buildPath("", "favicon.ico");

        String expectedRes = " " + index + " " + css + " " + ico;

        Assert.assertEquals(expectedRes, webserver.handledRequests);

        webserver.handledRequests = "";
    }

    @Test
    public void handleClientWithMaintenanceServerAndRequestForIndexPage() throws IOException {
        //connect to localhost:8080
        //request the index

        WebServer webserver = new WebServer(WEBSERVER_PORT + 1, WEBSERVER_ROOT_DIRECTORY, WEBSERVER_MAINTENANCE_DIRECTORY, "Maintenance");
        webserver.handledRequests = "";
        ServerSocket serverSocket = new ServerSocket(CLIENT_PORT);

        Socket clientSocket = serverSocket.accept();
        webserver.handleClient(clientSocket); //html

        clientSocket = serverSocket.accept();
        webserver.handleClient(clientSocket); //css

        clientSocket = serverSocket.accept();
        webserver.handleClient(clientSocket); //favicon

        String index = buildPath("", "", "index.html");
        String css = buildPath("", "", "maintenance.css");
        String ico = buildPath("", "favicon.ico");

        String expectedRes = " " + index + " " + css + " " + ico;

        Assert.assertEquals(expectedRes, webserver.handledRequests);

        webserver.handledRequests = "";
    }

    @Test
    public void handleClientWithStoppedServerAndRequestForIndexPage() throws IOException {
        //connect to localhost:8080
        //request the index
        WebServer webserver = new WebServer(WEBSERVER_PORT + 1, WEBSERVER_ROOT_DIRECTORY, WEBSERVER_MAINTENANCE_DIRECTORY, "Stopped");
        webserver.handledRequests = "";
        ServerSocket serverSocket = new ServerSocket(CLIENT_PORT);

        Socket clientSocket = serverSocket.accept();
        webserver.handleClient(clientSocket); //html

        clientSocket = serverSocket.accept();
        webserver.handleClient(clientSocket); //css

        clientSocket = serverSocket.accept();
        webserver.handleClient(clientSocket); //favicon

        //Due to how the handlerequests attribute is constructed, the first character will be a space
        String index = buildPath("", "", "index.html");
        String css = buildPath("", "", "serverdown.css");
        String ico = buildPath("", "favicon.ico");

        String expectedRes = " " + index + " " + css + " " + ico;

        Assert.assertEquals(expectedRes, webserver.handledRequests);

        webserver.handledRequests = "";
    }

    @Test
    public void handleClientWithRunningServerAndRequestForIndexPageClientDisconnectingIOExceptionThrown() throws IOException {
        //connect to localhost:8080
        //request the index

        WebServer webserver = buildDefaultWebServer();
        ServerSocket serverSocket = new ServerSocket(CLIENT_PORT);

        Socket clientSocket = serverSocket.accept();
        clientSocket.close();
        webserver.handleClient(clientSocket); //htm
    }


    @Test
    public void handleOneRequestWithRunningServerHandlingKnownPageRequestNoExceptionOccuring() {
        //connect to localhost:8080
        //request the index

        WebServer webserver = buildDefaultWebServer();
        webserver.handledRequests = "";
        webserver.handleOneRequest(); //html
        webserver.handleOneRequest(); //css
        webserver.handleOneRequest(); //favicon

        String index = buildPath("", "", "index.html");
        String css = buildPath("", "", "index.css");
        String ico = buildPath("", "favicon.ico");

        String expectedRes = " " + index + " " + css + " " + ico;

        Assert.assertEquals(expectedRes, webserver.handledRequests);
        webserver.handledRequests = "";
    }

    @Test
    public void handleOneRequestWithRunningServerIOExceptionWhenCreatingAServerSocket() {

        //if the port is still established at the time of execution, you will see the message "Could not listen on port: xxxxxx"
        WebServer webserver = new WebServer(CLIENT_PORT, WEBSERVER_ROOT_DIRECTORY, WEBSERVER_MAINTENANCE_DIRECTORY, WEBSERVER_STATUS);
        WebServer webserver2 = new WebServer(CLIENT_PORT, WEBSERVER_ROOT_DIRECTORY, WEBSERVER_MAINTENANCE_DIRECTORY, WEBSERVER_STATUS);
        webserver.handleOneRequest(); //html
        webserver2.handleOneRequest(); //html
        webserver.handleOneRequest(); //css
        webserver.handleOneRequest(); //favicon

    }

    @Test
    public void handleOneRequestWithRunningServerIOExceptionWhenTwoClientsConnectAtSameServer() throws BrokenBarrierException, InterruptedException {

        WebServer webserver = buildDefaultWebServer();
        final CyclicBarrier gate = new CyclicBarrier(2 + 1);

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    gate.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                webserver.handleOneRequest(); //html

            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    gate.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                webserver.handleOneRequest(); //html
            }
        });
        t1.start();
        t2.start();

        gate.await();
    }
}
