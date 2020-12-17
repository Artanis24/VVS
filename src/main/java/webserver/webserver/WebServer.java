package webserver.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public final class WebServer {

    public String handledRequests = "";
    public int port;
    public String webserverRootDirectory; //this contains index.html
    public String webserverMaintenanceDirectory; //this contains maintenance.html
    public String webserverStatus;

    public WebServer(int port, String webserverRootDirectory, String webserverMaintenanceDirectory, String webserverStatus) {
        if (webserverRootDirectory == null) {
            throw new NullPointerException();
        }

        this.port = port;
        this.webserverRootDirectory = webserverRootDirectory;
        this.webserverMaintenanceDirectory = webserverMaintenanceDirectory;
        this.webserverStatus = webserverStatus;
    }

    public void updateWebserverPort(int newPort) {
        port = newPort;
    }

    public void updateWebserverRootDirectory(final String newWebserverRootDirectory) {
        webserverRootDirectory = newWebserverRootDirectory;
    }

    public void updateWebserverMaintenanceDirectory(final String newWebserverMaintenanceDirectory) {
        webserverMaintenanceDirectory = newWebserverMaintenanceDirectory;
    }

    public void updateWebserverStatus(final String newWebserverStatus) {
        webserverStatus = newWebserverStatus;
    }

    public Path getFilePath(final String path) {
        if ("/".equals(path)) {
            return Paths.get(webserverRootDirectory, "index.html");
        } else if ("/index.css".equals(path)) {
            return Paths.get(webserverRootDirectory, "index.css");
        } else if ("/favicon.ico".equals(path)) {
            return Paths.get(webserverRootDirectory, "favicon.ico");
        } else {
            return Paths.get(path);
        }
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
            OutputStream out = clientSocket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            ArrayList<String> inputLines = new ArrayList<>();
            while ((inputLine = in.readLine()) != null) {
                inputLines.add(inputLine);

                if (inputLine.trim().equals("")) {
                    break;
                }
            }

            if (!inputLines.isEmpty()) {
                String rawPath = inputLines.get(0).split(" ")[1];
                Path filePath = getFilePath(rawPath);
                String contentType = Files.probeContentType(filePath);

                handledRequests += " " + filePath.toString();
                if (webserverStatus.equals("Running")) {
                    if (Files.exists(filePath)) {
                        sendResponse(out, "200 OK", contentType, Files.readAllBytes(filePath));
                    } else {
                        sendResponse(out, "404 Not Found", contentType, Files.readAllBytes(Paths.get(webserverRootDirectory, "/notfound.html")));
                    }
                } else if (webserverStatus.equals("Maintenance")) {
                    if (contentType.contains("html")) {
                        sendResponse(out, "503 Service Unavailable", contentType, Files.readAllBytes(Paths.get(webserverMaintenanceDirectory, "/maintenance.html")));
                    } else {
                        sendResponse(out, "200 OK", contentType, Files.readAllBytes(filePath));
                    }
                } else {
                    if (contentType.contains("html")) {
                        sendResponse(out, "503 Service Unavailable", contentType, Files.readAllBytes(Paths.get(webserverRootDirectory, "/serverdown.html")));
                    } else {
                        sendResponse(out, "200 OK", contentType, Files.readAllBytes(filePath));
                    }
                }
            }


            out.close();
            in.close();
        } catch (NullPointerException e) {
            System.err.println("Null client object was given");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Problem with Communication Server");
        }
    }

    public void handleOneRequest() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Socket clientSocket = serverSocket.accept();
            handleClient(clientSocket);
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Could not listen on port:" + port);
        }

    }
}
