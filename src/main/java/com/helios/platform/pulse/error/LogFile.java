package com.helios.platform.pulse.error;

import lombok.NonNull;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogFile {

    private static File errorsPathFolder = new File(System.getProperty("user.home") + File.separator + "etecc" + File.separator + "cbt" + File.separator + "logs" + File.separator + "errors" + File.separator);

    public static void init() {
        if (!errorsPathFolder.exists()) {
            errorsPathFolder.mkdirs();
        }
    }

    /**
     * Constructs a new LogFile instance with a unique name based on the current date and time.
     * The log file is created in the specified path folder, which is determined by the system's
     * user home directory. The file name follows the format: ETECC_LOG_FILE_yyyy-MM-dd_HH-mm-ss.log.
     * @throws RuntimeException If an error occurs while creating the log file.
     */

    private LogFile() {
    }

    public static <T extends Exception> void writeLogError(@NonNull T error, boolean toSend) {
        try (StringWriter stringWriter = new StringWriter(); PrintWriter printWriter = new PrintWriter(stringWriter)) {
            // Imprimir el stack trace del error en el PrintWriter
            error.printStackTrace(printWriter);

            // Formatear la fecha y crear el archivo
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String formattedDate = LocalDateTime.now().format(dateTimeFormatter);
            File errorLogFile = new File(errorsPathFolder.getAbsolutePath() + File.separator + "ETECC_ERROR_LOG_FILE_" + formattedDate + ".log");

            // Crear el archivo y escribir el contenido
            try (FileWriter writer = new FileWriter(errorLogFile, true)) { // 'true' para agregar contenido al archivo existente
                String response = String.format("We have run into an issue of type %s", error.toString());
                writer.write(response);
                writer.write("\n"); // Nueva línea para separar el mensaje del stack trace
                writer.write(stringWriter.toString()); // Escribir el stack trace

                // Enviar notificación si es necesario
                if (toSend) {
                    // new TelegramNotificationError().sendNotification(response);
                    System.out.println("Notification sent: " + response);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error writing to log file: " + e.getMessage(), e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error handling log writing: " + e.getMessage(), e);
        }
    }


    public static <T extends Exception> void writeLogError(@NonNull T error, @NonNull String customMessage) throws IllegalArgumentException {
        if (customMessage.isBlank() || customMessage.isEmpty())
            throw new IllegalArgumentException("Custom message cannot be null or empty");

        try (StringWriter stringWriter = new StringWriter(); PrintWriter printWriter = new PrintWriter(stringWriter)) {
            // Imprimir el stack trace del error en el PrintWriter
            error.printStackTrace(printWriter);

            // Formatear la fecha y crear el archivo
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String formattedDate = LocalDateTime.now().format(dateTimeFormatter);
            File errorLogFile = new File(errorsPathFolder.getAbsolutePath() + File.separator + "ETECC_ERROR_LOG_FILE_" + formattedDate + ".log");

            // Crear el archivo y escribir el contenido
            try (FileWriter writer = new FileWriter(errorLogFile, true)) { // 'true' para agregar contenido al archivo existente
                String response = String.format("%s \n ERROR OF TYPE: %s ", customMessage, error.toString());
                writer.write(response);
                writer.write("\n"); // Nueva línea para separar el mensaje personalizado del stack trace
                writer.write(stringWriter.toString()); // Escribir el stack trace
            } catch (IOException e) {
                throw new RuntimeException("Error writing to log file: " + e.getMessage(), e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error handling log writing: " + e.getMessage(), e);
        }
    }

}
