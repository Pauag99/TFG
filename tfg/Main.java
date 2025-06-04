package tfg;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

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
        Button botonVerHistorial = new Button("Ver historial");
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
                mostrarAlerta(Alert.AlertType.WARNING, "⚠️ Primero debes preprocesar el texto.");
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
                        mostrarAlerta(Alert.AlertType.WARNING, "⚠️ Algoritmo no soportado.");
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

                mostrarAlerta(Alert.AlertType.INFORMATION, "Resultado del análisis con " + algoritmoSeleccionado, salida.toString());

                String nombreArchivo = archivoCargado != null ? archivoCargado.getName() : "Texto manual";
                guardarHistorial(algoritmoSeleccionado, nombreArchivo, salida.toString());

            } catch (IOException ex) {
                ex.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "Error al ejecutar el análisis.", ex.getMessage());
            }
        });

        botonVerHistorial.setOnAction(e -> mostrarHistorial());

        botonCargar.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar archivo de texto");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
            File archivo = fileChooser.showOpenDialog(null);

            if (archivo != null) {
                try {
                    String contenido = new String(Files.readAllBytes(archivo.toPath()));
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
                    String contenido = new String(Files.readAllBytes(archivoCargado.toPath()));
                    String resultadoPython = preprocesarTextoConPython(contenido);

                    if (resultadoPython != null) {
                        File archivoNuevo = new File(archivoCargado.getParentFile(),
                                archivoCargado.getName().replace(".txt", "") + "_preprocesado.txt");
                        Files.write(archivoNuevo.toPath(), resultadoPython.getBytes());
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

        HBox botones = new HBox(10, botonCargar, botonPreprocesar, botonVerHistorial);
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

        Scene scene = new Scene(layout, 600, 450);
        primaryStage.setTitle("Analizador de Texto");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        mostrarAlerta(tipo, null, mensaje);
    }

    private void mostrarHistorial() {
        Stage ventanaHistorial = new Stage();
        TextArea areaHistorial = new TextArea();
        areaHistorial.setEditable(false);

        try {
            String contenido = Files.lines(Paths.get("historial_analisis.csv"))
                    .collect(Collectors.joining("\n"));
            areaHistorial.setText(contenido);
        } catch (IOException ex) {
            areaHistorial.setText("No se pudo cargar el historial.");
        }

        Scene escena = new Scene(new VBox(areaHistorial), 700, 400);
        ventanaHistorial.setTitle("Historial de análisis");
        ventanaHistorial.setScene(escena);
        ventanaHistorial.show();
    }

    private void guardarHistorial(String algoritmo, String archivoTexto, String resultado) {
        try (FileWriter writer = new FileWriter("historial_analisis.csv", true)) {
            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String linea = String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    fecha, algoritmo, archivoTexto, resultado.replace("\"", "'").replace("\n", " "));
            writer.write(linea);
        } catch (IOException ex) {
            System.err.println("❌ Error al guardar el historial: " + ex.getMessage());
        }
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
