package edu.escuelaing.arep;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

public class HttpServerCalculator {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(36000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 36000.");
            System.exit(1);
        }
        while (true) {
            Socket clientSocket = null;
            try {
                System.out.println("Escuchando en puerto 36000 ...");
                clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

        }

    }

    private static void handleRequest(Socket clientSocket) throws IOException {
        PrintWriter out = new PrintWriter(
                clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        BufferedOutputStream dataOut = new BufferedOutputStream(clientSocket.getOutputStream());
        String requestLine = in.readLine();
        if (requestLine == null) return ;
        String[] tokens = requestLine.split(" ");
        String method = tokens[0];
        String fileRequested = tokens[1];
        if (method.equals("GET")) {
            handleGetRequest(fileRequested, out, dataOut);
        } else if (method.equals("POST")) {
            //handlePostRequest(in, out);
        }
        String inputLine, outputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Recibí: " + inputLine);

            if (!in.ready()) {break; }
        }
        System.out.println(String.valueOf(calculate("max", new double[]{3.5, 5.0})));
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

    private static String makeOperation(String inputLine) {
//        String[] urlSplit = inputLine.split(" ");
//        String[] request = urlSplit[1].split("/?");
//        String[] op = request[1].split("&");
//        String[] val= op[1].split("=");
        return null;
    }

    private static Double calculate(String op, double[] numbers) {
        try {
            Class c = Math.class;
            if (numbers.length == 1){
                Method m = c.getMethod(op, double.class);
                double res = (double) m.invoke(null, (Object) numbers[0]);
                return res;
            }
            if (numbers.length == 2){
                Method m = c.getMethod(op, new Class[]{double.class, double.class});
                System.out.println(m.toString());
                Object[] args = {numbers[0], numbers[1]};
                double res = (double) m.invoke(null, args);
                return res;
            }


        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static void handleGetRequest(String fileRequested, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        if (fileRequested.startsWith("/math")) {
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: application/json");
            out.println();
            out.println("{\"answer\": " + calculate("max", new double[]{3.5, 5.0}) + "}");
            String[] values = getValues(fileRequested);
//            out.println("{\"answer\": " + calculate(values[0], new double[]{Double.parseDouble(values[1]) , Double.parseDouble(values[2])}) + "}");
            out.flush();
//        } else { // Manejo de solicitudes para archivos
//            File file = new File(WEB_ROOT, fileRequested);
//            String fileReadable = file.toString();
//            int fileLength = (int) file.length();
//            String content = getContentType(fileRequested);
//            if (file.exists()) {
//                if (fileRequested.endsWith(".png") || fileRequested.endsWith(".jpg") || fileRequested.endsWith(".jpeg")) {
//                    byte[] imageData = Files.readAllBytes(file.toPath()); // Enviar la respuesta HTTP para la imagen
//                    out.println("HTTP/1.1 200 OK");
//                    out.println("Content-Type: " + content);
//                    out.println("Content-Length: " + imageData.length);
//                    out.println();
//                    out.flush(); // Enviar los datos de la imagen
//                    dataOut.write(imageData, 0, imageData.length);
//                    dataOut.flush();
//                } else {
//                    byte[] fileData = readFileData(file, fileLength);
//                    out.println("HTTP/1.1 200 OK");
//                    out.println("Content-Type: " + content);
//                    out.println("Content-Length: " + fileLength);
//                    out.println();
//                    out.flush();
//                    dataOut.write(fileData, 0, fileLength);
//                    dataOut.flush();
//                }
//            } else {
//                out.println("HTTP/1.1 404 Not Found");
//                out.println("Content-Type: text/html");
//                out.println();
//                out.flush();
//                out.println("<html><body><h1>File Not Found</h1></body></html>");
//                out.flush();
//            }
        }
    }

    private static String[] getValues(String fileRequested) {
        String[] parts = fileRequested.split("/math/");
        String[] params = parts[1].split("&");
        ArrayList<String> values = new ArrayList<>();
        for (String s : params){
            String[] num = s.split("=");
            values.add(num[1]);
        }
        System.out.println(values.toString());
        System.out.println(parts[0]);
        return null;
    }

}
