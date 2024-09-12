package edu.escuelaing.arep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;

public class HttpServerFacade {
    private static final int TARGET_PORT = 36000;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(40000);
        } catch (IOException e) {
            System.err.println("No se pudo escuchar en el puerto: 40000.");
            System.exit(1);
        }

        while (true) {
            Socket clientSocket = null;
            try {
                System.out.println("Escuchando en puerto 40000 ...");
                clientSocket = serverSocket.accept();
                handleClientRequest(clientSocket);
            } catch (IOException e) {
                System.err.println("Error en la aceptación de conexión.");
                System.exit(1);
            }
        }
    }

    private static void handleClientRequest(Socket clientSocket) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            StringBuilder request = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                request.append(line).append("\r\n");
            }

            String response = forwardRequestToTargetServer(request.toString());

            out.println(response);
        } finally {
            clientSocket.close();
        }
    }

    private static String forwardRequestToTargetServer(String request) {
        try (Socket targetSocket = new Socket("localhost", TARGET_PORT);
             PrintWriter targetOut = new PrintWriter(targetSocket.getOutputStream(), true);
             BufferedReader targetIn = new BufferedReader(new InputStreamReader(targetSocket.getInputStream()))) {

            targetOut.println(request);

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = targetIn.readLine()) != null) {
                response.append(line).append("\r\n");
            }

            return response.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return "Error al redirigir la solicitud.";
        }
    }
}
