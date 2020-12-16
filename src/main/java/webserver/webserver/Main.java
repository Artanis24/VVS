package webserver.webserver;

class Main {
    public static void main(String[] args) {
        WebServer webserver = new WebServer(
                8080,
                "Pages",
                "Pages/maintenance",
                "Stopped"
        );
        webserver.updateWebserverStatus("Running");
        while (true) {
            webserver.handleOneRequest();
        }
    }
}
