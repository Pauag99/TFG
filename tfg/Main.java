package tfg;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;

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

        Button botonCargar = new Button("Cargar texto");
        Button botonPreprocesar = new Button("Preprocesar");
        Label resultadoLabel = new Label();
        Label mensajeLabel = new Label();

        ComboBox<String> selectorAlgoritmo = new ComboBox<>();
        selectorAlgoritmo.getItems().addAll("VADER", "TEXTBLOB", "BERT", "NAIVE BAYES");
        selectorAlgoritmo.setPromptText("Selecciona un algoritmo");

        Button botonAnalizar = new Button("Analizar sentimientos");

        botonAnalizar.setOnAction(e -> {
    String algoritmoSeleccionado = selectorAlgoritmo.getValue();
    String textoPreprocesado = entradaTexto.getText();

    if (textoPreprocesado == null || textoPreprocesado.isEmpty()) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setHeaderText(null);
        alerta.setContentText("⚠️ Primero debes preprocesar el texto.");
        alerta.showAndWait();
        return;
    }

    try {
        String script;
        switch (algoritmoSeleccionado.toUpperCase()) {
            case "VADER":
                script = "vader.py";
                break;
            case "TEXTBLOB":
                script = "analizador_textblob.py";
                break;
            case "BERT":
                script = "analizador_bert.py";
                break;
            case "NAIVE BAYES":
                script = "analizador_nb.py";
                break;
            default:
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setHeaderText(null);
                alerta.setContentText("⚠️ Algoritmo no soportado.");
                alerta.showAndWait();
                return;
        }

        ProcessBuilder pb = new ProcessBuilder("python", "python/" + script);
        Process process = pb.start();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            String json = "{\"texto\": \"" + textoPreprocesado
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r") + "\"}";
            writer.write(json);
            writer.flush();
        }

        StringBuilder salida = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                salida.append(linea);
            }
        }

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setHeaderText("Resultado del análisis con " + algoritmoSeleccionado);
        alerta.setContentText(salida.toString());
        alerta.showAndWait();

    } catch (IOException ex) {
        ex.printStackTrace();
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setHeaderText("Error al ejecutar el análisis.");
        error.setContentText(ex.getMessage());
        error.showAndWait();
    }
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
                        entradaTexto.setText(resultadoPython);
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

        HBox botones = new HBox(10, botonCargar, botonPreprocesar);
        botones.setStyle("-fx-alignment: center;");

        HBox analisisBox = new HBox(10, selectorAlgoritmo, botonAnalizar);
        analisisBox.setStyle("-fx-alignment: center;");

        VBox layout = new VBox(10,
                new Label("Introduce texto:"),
                entradaTexto,
                botones,
                analisisBox,
                resultadoLabel,
                mensajeLabel
        );
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Scene scene = new Scene(layout, 500, 400);
        primaryStage.setTitle("Analizador de Texto");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String preprocesarTextoConPython(String texto) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "python/preprocesamiento_final.py");
            Process process = pb.start();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                String inputJson = "{\"texto\": \"" + texto
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r") + "\"}";
                writer.write(inputJson);
                writer.flush();
            }

            StringBuilder salida = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    salida.append(linea);
                }
            }

            try (BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String errorLine;
                while ((errorLine = err.readLine()) != null) {
                    System.err.println("⚠️ Error desde Python: " + errorLine);
                }
            }

            String output = salida.toString().trim();
            System.out.println("📤 Salida del script Python: " + output);

            if (output.startsWith("{") && output.endsWith("}")) {
                int idx = output.indexOf(":");
                if (idx != -1) {
                    return output.substring(idx + 1).replaceAll("[{}\"]", "").trim();
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
