package edu.escuelaing.arep;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

public class HttpServerCalculator {
    private static final int PORT = 36000;
    private static final String WEB_ROOT = "src/main/resources/static";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Escuchando en puerto " + PORT + "...");
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleRequest(clientSocket);
                } catch (IOException e) {
                    System.err.println("Error al aceptar la conexión: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo escuchar en el puerto " + PORT + ": " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleRequest(Socket clientSocket) throws IOException {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedOutputStream dataOut = new BufferedOutputStream(clientSocket.getOutputStream())) {

            String requestLine = in.readLine();
            if (requestLine == null) return;

            String[] tokens = requestLine.split(" ");
            if (tokens.length < 2) return;

            String method = tokens[0];
            String fileRequested = tokens[1];

            if ("GET".equalsIgnoreCase(method)) {
                handleGetRequest(fileRequested, out, dataOut);
            } else if ("POST".equalsIgnoreCase(method)) {
                // handlePostRequest(in, out);
            }

            String inputLine;
            while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {
                System.out.println("Recibí: " + inputLine);
            }


            System.out.println("Resultado de cálculo: " + calculate("max", new double[]{3.5, 5.0}));

            String outputLine =
                    "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "<meta charset=\"UTF-8\">" +
                            "<title>Title of the document</title>" +
                            "</head>" +
                            "<body>" +
                            "<h1>Mi propio mensaje</h1>" +
                            "</body>" +
                            "</html>";

            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html");
            out.println("Content-Length: " + outputLine.length());
            out.println();
            out.println(outputLine);
        }
    }

    private static void handlePostRequest(BufferedReader in, PrintWriter out) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            requestBody.append(line).append("\r\n");
        }

        String[] params = requestBody.toString().split("&");
        Map<String, String> paramMap = new HashMap<>();
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                paramMap.put(keyValue[0], keyValue[1]);
            }
        }

        String expression = paramMap.get("expression");
        if (expression != null) {
            try {
                String operation;
                double[] numbers;

                int start = expression.indexOf('(');
                int end = expression.indexOf(')');

                if (start != -1 && end != -1) {
                    operation = expression.substring(0, start).trim();
                    String numbersPart = expression.substring(start + 1, end).trim();
                    String[] values = numbersPart.split(",");

                    numbers = Arrays.stream(values)
                            .mapToDouble(Double::parseDouble)
                            .toArray();

                    Double result = calculate(operation, numbers);
                    String response;
                    if (result != null) {
                        response = "{\"answer\": " + result + "}";
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: application/json");
                        out.println("Content-Length: " + response.length());
                        out.println();
                        out.println(response);
                    } else {
                        response = "{\"error\": \"Invalid operation or parameters.\"}";
                        out.println("HTTP/1.1 400 Bad Request");
                        out.println("Content-Type: application/json");
                        out.println("Content-Length: " + response.length());
                        out.println();
                        out.println(response);
                    }
                } else {
                    String response = "{\"error\": \"Invalid expression format.\"}";
                    out.println("HTTP/1.1 400 Bad Request");
                    out.println("Content-Type: application/json");
                    out.println("Content-Length: " + response.length());
                    out.println();
                    out.println(response);
                }
            } catch (Exception e) {
                String response = "{\"error\": \"Invalid expression format.\"}";
                out.println("HTTP/1.1 400 Bad Request");
                out.println("Content-Type: application/json");
                out.println("Content-Length: " + response.length());
                out.println();
                out.println(response);
            }
        } else {
            String response = "{\"error\": \"Expression Not Provided\"}";
            out.println("HTTP/1.1 400 Bad Request");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + response.length());
            out.println();
            out.println(response);
        }
    }




    private static void handleGetRequest(String fileRequested, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        if (fileRequested.startsWith("/math")) {
            String[] values = getValues(fileRequested);
            String operation = values[0];
            double[] numbers = Arrays.stream(values, 1, values.length)
                    .mapToDouble(Double::parseDouble)
                    .toArray();

            Double result = calculate(operation, numbers);

            if (result != null) {
                String response = "{\"answer\": " + result + "}";
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println("Content-Length: " + response.length());
                out.println();
                out.println(response);
            } else {
                out.println("HTTP/1.1 400 Bad Request");
                out.println("Content-Type: text/html");
                out.println();
                out.println("<html><body><h1>Invalid Operation</h1></body></html>");
            }

            out.flush();
        } else {
            File file = new File(WEB_ROOT, fileRequested);
            if (file.exists() && !file.isDirectory()) {
                String contentType = getContentType(fileRequested);
                byte[] fileData = Files.readAllBytes(file.toPath());

                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: " + contentType);
                out.println("Content-Length: " + fileData.length);
                out.println();
                out.flush();
                dataOut.write(fileData, 0, fileData.length);
                dataOut.flush();
            } else {
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/html");
                out.println();
                out.println("<html><body><h1>File Not Found</h1></body></html>");
                out.flush();
            }
        }
    }

    private static Double calculate(String op, double[] numbers) {
        try {
            Method method = getMathMethod(op, numbers.length);
            if (method != null) {
                return (Double) method.invoke(null, toObjectArray(numbers));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            System.err.println("Error al calcular: " + e.getMessage());
        }
        return null;
    }

    private static Method getMathMethod(String op, int numParams) throws NoSuchMethodException {
        Class<?>[] paramTypes = numParams == 1 ? new Class<?>[]{double.class} : new Class<?>[]{double.class, double.class};
        return Math.class.getMethod(op, paramTypes);
    }


    private static Object[] toObjectArray(double[] primitives) {
        return Arrays.stream(primitives).boxed().toArray(Double[]::new);
    }

    private static String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html")) return "text/html";
        return "text/plain";
    }

    public static String[] getValues(String path) {
        String[] parts = path.split("\\?");
        String queryPart = parts.length > 1 ? parts[1] : "";

        String[] queryPairs = queryPart.split("&");
        String[] result = new String[queryPairs.length + 1];
        result[0] = parts[0].substring(parts[0].lastIndexOf('/') + 1);

        for (int i = 0; i < queryPairs.length; i++) {
            String[] keyValue = queryPairs[i].split("=");
            if (keyValue.length > 1) {
                result[i + 1] = keyValue[1];
            }
        }

        return result;
    }
}
