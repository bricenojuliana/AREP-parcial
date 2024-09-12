package edu.escuelaing.arep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServerFacade {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(40000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 40000.");
            System.exit(1);
        }

        while (true){
            Socket clientSocket = null;
            try {
                System.out.println("Escuchando en puerto 40000 ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Recibido: " + inputLine);
                if (!in.ready()) {break; }
            }
            outputLine =
                    "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "<meta charset=\"UTF-8\">" +
                            "<title>Title of the document</title>\n" +
                            "</head>" +
                            "<body>" +
                            "<h1>Mi propio mensaje</h1>" +
                            "</body>" +
                            "</html>";
            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
//            serverSocket.close();
        }


    }
}
