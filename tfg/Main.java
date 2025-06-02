package tfg;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

public class Main extends Application {

    private File archivoCargado = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        TextArea entradaTexto = new TextArea();
        entradaTexto.setWrapText(true);
        entradaTexto.setPrefRowCount(5);
        entradaTexto.setPrefColumnCount(30);

        Button botonRevisar = new Button("Revisar");
        Button botonCargar = new Button("Cargar texto");
        Button botonPreprocesar = new Button("Preprocesar");
        Label resultadoLabel = new Label();
        Label mensajeLabel = new Label();

        botonRevisar.setOnAction(e -> {
            String textoUsuario = entradaTexto.getText();
            String resultado = llamarAScriptPython(textoUsuario);
            resultadoLabel.setText("Resultado: " + resultado);
        });

        botonCargar.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar archivo de texto");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
            File archivo = fileChooser.showOpenDialog(null);

            if (archivo != null) {
                try {
                    String contenido = new String(java.nio.file.Files.readAllBytes(archivo.toPath()));
                    if (contenido.trim().isEmpty()) {
                        mensajeLabel.setText("⚠️ El archivo está vacío.");
                        entradaTexto.clear();
                    } else {
                        entradaTexto.setText(contenido);
                        mensajeLabel.setText("✅ Archivo cargado correctamente.");
                        archivoCargado = archivo;
                    }
                } catch (IOException ex) {
                    mensajeLabel.setText("❌ Error al leer el archivo.");
                }
            } else {
                mensajeLabel.setText("⛔ No se seleccionó ningún archivo.");
            }
        });

        botonPreprocesar.setOnAction(e -> {
            if (archivoCargado != null) {
                try {
                    String contenido = new String(java.nio.file.Files.readAllBytes(archivoCargado.toPath()));
                    String resultadoPython = preprocesarTextoConPython(contenido);

                    if (resultadoPython != null) {
                        File archivoNuevo = new File(archivoCargado.getParentFile(),
                                archivoCargado.getName().replace(".txt", "") + "_preprocesado.txt");
                        java.nio.file.Files.write(archivoNuevo.toPath(), resultadoPython.getBytes());
                        mensajeLabel.setText("✅ Preprocesado por Python y guardado como: " + archivoNuevo.getName());
                    } else {
                        mensajeLabel.setText("❌ Python no devolvió resultado válido.");
                    }
                } catch (IOException ex) {
                    mensajeLabel.setText("❌ Error al preprocesar: " + ex.getMessage());
                }
            } else {
                mensajeLabel.setText("⚠️ Primero debes cargar un archivo.");
            }
        });

        HBox botones = new HBox(10, botonCargar, botonRevisar, botonPreprocesar);
        botones.setStyle("-fx-alignment: center;");

        VBox layout = new VBox(10,
                new Label("Introduce texto:"),
                entradaTexto,
                botones,
                resultadoLabel,
                mensajeLabel
        );
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Scene scene = new Scene(layout, 500, 350);
        primaryStage.setTitle("Analizador de Texto");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String llamarAScriptPython(String texto) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "python/analizador.py");
            Process process = pb.start();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                String inputJson = "{\"texto\": \"" + texto.replace("\"", "\\\"") + "\"}";
                writer.write(inputJson);
                writer.flush();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = reader.readLine();
                if (output != null && output.contains("resultado")) {
                    return output.split(":")[1].replaceAll("[^a-zA-Z]", "");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return "error";
    }

    private String preprocesarTextoConPython(String texto) {
    try {
        ProcessBuilder pb = new ProcessBuilder("python", "python/preprocesar.py");
        Process process = pb.start();

        // Enviar el JSON al script
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
           String inputJson = "{\"texto\": \"" + texto
                .replace("\\", "\\\\")       // escapar backslashes
                .replace("\"", "\\\"")       // escapar comillas
                .replace("\n", "\\n")        // escapar saltos de línea
                .replace("\r", "\\r")        // escapar retornos de carro
                + "\"}";

            System.out.println("Enviando a Python: " + inputJson); // 🟡 Debug
            writer.write(inputJson);
            writer.flush();
        }

        // Leer salida estándar (stdout)
        StringBuilder salida = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                salida.append(linea);
            }
        }

        // Leer errores (stderr)
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String lineaError;
            while ((lineaError = errorReader.readLine()) != null) {
                System.err.println("❌ Error desde Python: " + lineaError);
            }
        }

        String output = salida.toString().trim();
        System.out.println("Salida de Python: " + output); // 🟢 Debug

        // Extraer el campo resultado del JSON
        if (output.contains("\"resultado\"")) {
            return output.split(":", 2)[1].replaceAll("[{}\"]", "").trim();
        }

    } catch (IOException ex) {
        ex.printStackTrace();
    }
    return null;
}

}
