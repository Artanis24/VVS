package webserver;
import java.nio.file.*;
import java.net.*;
import java.util.*;
import java.io.*;
class Main {
    public static void main(String[] args) {

        WebServer webserver = new WebServer(8080, "Pages", "Pages", "Stopped");
        webserver.updateStatus("Running");
        while (true) {
            webserver.handle_one_request();
        }
    }
}

public class WebServer extends Thread {

    public static String HANDLER_REQUEST = "";
    public int PORT = 8080;
    public String WEBSERVER_ROOT = "Pages/notfound";
    public String WEBSERVER_MAINTENANCE = "Pages/maintenance";
    public String WEBSERVER_STATUS = "Stopped";
    public WebServer(int port, String root, String maintenance, String status) {
        this.PORT = port;
        this.WEBSERVER_ROOT = root;
        this.WEBSERVER_MAINTENANCE = maintenance;
        this.WEBSERVER_STATUS = status;
    }
    public void updateStatus(String newStatus) {
        WEBSERVER_STATUS = newStatus;
    }
    public void sendResponse(OutputStream out, String status, String contentType, byte[] content) throws IOException {
        out.write(("HTTP/1.1 \r\n" + status).getBytes());
        out.write(("ContentType: " + contentType + "\r\n").getBytes());
        out.write("\r\n".getBytes());
        out.write(content);
        out.write("\r\n\r\n".getBytes());
    }
    public void handleClient(Socket clientSocket) {
        try {
            try {
                String inputLine;
                OutputStream out = clientSocket.getOutputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                ArrayList<String> inputLines = new ArrayList<String>();
                while ((inputLine = in.readLine()) != null) {
                    inputLines.add(inputLine);

                    if (inputLine.trim().equals(""))
                        break;
                }

                if (inputLines.size() != 0) {
                    String rawPath = inputLines.get(0).split(" ")[1];
                    System.out.println(rawPath);
                    Path filePath = getFilePath(rawPath);
                    String contentType = Files.probeContentType(filePath);

                    HANDLER_REQUEST = HANDLER_REQUEST + " " + filePath.toString();
                    if (WEBSERVER_STATUS.equals("Running")) {
                        if (Files.exists(filePath))
                            sendResponse(out, "200 OK", contentType, Files.readAllBytes(filePath));
                        else
                            sendResponse(out, "404 Not Found", contentType, Files.readAllBytes(Paths.get(WEBSERVER_ROOT, "/notfound.html")));
                    } else if (WEBSERVER_STATUS.equals("Maintenance")) {
                        if (contentType.contains("html"))
                            sendResponse(out, "503 Service Unavailable", contentType, Files.readAllBytes(Paths.get(WEBSERVER_MAINTENANCE, "/maintenance.html")));
                        else
                            sendResponse(out, "200 OK", contentType, Files.readAllBytes(filePath));
                    } else {
                        if (contentType.contains("html"))
                            sendResponse(out, "503 Service Unavailable", contentType, Files.readAllBytes(Paths.get(WEBSERVER_ROOT, "/serverdown.html")));
                        else
                            sendResponse(out, "200 OK", contentType, Files.readAllBytes(filePath));
                    }
                }
                out.close();
                in.close();
            } catch (NullPointerException e) {
                System.err.println("NULL Client");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Communication Server problem");
        }
    }
    public Path getFilePath(String path) {
        if ("/".equals(path)) {
            path = "/index.html";
            return Paths.get(WEBSERVER_ROOT, path);
        } else if ("/index.css".equals(path)) {
            path = "/index.css";
            return Paths.get(WEBSERVER_ROOT, path);
        } else if ("/favicon.ico".equals(path)) {
            return Paths.get(WEBSERVER_ROOT, path);
        }
        return Paths.get(path);
    }
    public void handle_one_request() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            Socket clientSocket = serverSocket.accept();
            handleClient(clientSocket);
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Can't listen from port " + PORT);
        }
    }
}