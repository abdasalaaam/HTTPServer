import com.sun.tools.javac.util.List;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.StringTokenizer;

class Server {

    //contains cookie value for URLVisits but is reset for each thread created
    private static int cookie = 0;

        //main thread class
        public static class SocketThread extends Thread {

        private Socket cs;
        private String dir;

        public SocketThread(Socket connectionSocket, String directory) {
            cs = connectionSocket;
            dir = directory;
        }

        public void run() {
            try {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(cs.getInputStream()));
                String line;
                StringBuffer request = new StringBuffer();
                //determines the browser request and creates a line to be read from
                while (!((line = inFromClient.readLine()) == null) && !line.isEmpty()) {
                    request.append(line + "<BR>");
                }
                String[] splitResponse = request.toString().split("<BR>");
                //gets the number of browser accesses through the browser request field, which contains line "Cookie:"
                cookie = getNumberVisits(splitResponse);
                StringTokenizer headerParse = new StringTokenizer(splitResponse[0]);
                String call = "";
                String path = "";
                //call allows for multiple HTTP request functionality (not used in this project), path is the URL path
                if (headerParse.hasMoreTokens()) call = headerParse.nextToken(); //GET for most cases
                if (headerParse.hasMoreTokens()) path = headerParse.nextToken();
                String fileString;
                OutputStream clientOutput = cs.getOutputStream();

                if (call.equals("GET")) { //Get Call
                    //COOKIES
                    if (path.equals("/ass112/visits.html")) {
                        cookie++;
                        clientOutput.write(("HTTP/1.1 200 OK\r\n" +
                                "ContentType: text/html\r\n" +
                                "Set-Cookie: URLVisits=" + cookie + ";Path = /ass112;\r\n\r\n" +
                                "This is the number of URL visits from this browser: " + cookie + "\r\n\r\n").getBytes());
                    }
                    //HYPERLINK/IMAGE
                    else if (path.equals("/ass112/test1.html")) {
                        fileString = readHTMLFile(new File("HTMLpage.html"));
                        cookie++;
                        clientOutput.write(("HTTP/1.1 200 OK\r\n" +
                                "ContentType: text/html\r\n" +
                                "Set-Cookie: URLVisits=" + cookie + ";Path = /ass112;\r\n\r\n" +
                                fileString + "\r\n\r\n").getBytes());
                    }
                     //EMBEDDED IFRAME
                    else if (path.equals("/ass112/test2.html")) {
                        fileString = readHTMLFile(new File("HTMLpage2.html"));
                        cookie++;
                        clientOutput.write(("HTTP/1.1 200 OK\r\n" +
                                "ContentType: text/html\r\n" +
                                "Set-Cookie: URLVisits=" + cookie + ";Path = /ass112;\r\n\r\n" +
                                fileString + "\r\n\r\n").getBytes());
                    }
                    else  {
                        clientOutput.write(("HTTP/1.1 404 Not Found\r\n" +
                                "ContentType: text/html\r\n" +
                                "\r\n\r\n404 Not Found: " + path + "\r\n\r\n").getBytes());
                    }
                    clientOutput.flush();
                    cs.close();
                } else {
                    clientOutput.write(("HTTP/1.1 404 Not Found\r\n" +
                            "ContentType: text/html\r\n" +
                            "\r\n\r\n404 Not Found: " + path + "\r\n\r\n").getBytes());
                    }
                 } catch( Exception e) {
                         e.printStackTrace();
            }
        }

        //Reads the input HTML file and outputs a String representation of it
        public static String readHTMLFile(File file) throws IOException {
            StringBuffer fileString = new StringBuffer();
            BufferedReader fileRead = null;
            HashSet<StringBuffer> f = new HashSet<>();
            f.addAll(List.of(fileString));
            try {
                     fileRead = new BufferedReader(new FileReader(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            String line;
            while (!((line = fileRead.readLine()) == null) && !line.isEmpty()) {
                fileString.append(line + "\n");
            }
            return fileString.toString();
        }

        //Gets the number value for cookie URLVisits from the browser that made the request
        public static int getNumberVisits(String[] response) {
            for (int i = 0; i < response.length; i++) {
                if (response[i].contains("Cookie:")) {
                    //Request format: Cookie: URLVisits=i, splits based on '='
                    return Integer.parseInt(response[i].split("=")[1]);
                }
            }
            return 0;
        }
            }

        public static void main(String args[]) throws Exception {
            //sets up welcome socket to receive requests
            String config = SocketThread.readHTMLFile(new File("config.txt"));
            String[] configStmt = config.split("\n");
            int port = Integer.parseInt(configStmt[0].split(":")[1]);
            String directory = configStmt[1].split(":")[1];
            ServerSocket welcomeSocket = new ServerSocket(port);
            while(true) {
                //accepts request and creates a separate thread for it
                Socket connectionSocket = welcomeSocket.accept();
                SocketThread sockThread = new SocketThread(connectionSocket, directory);
                sockThread.start();
            }
        }
    }