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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main extends Application {

    private File archivoCargado = null;
    private final List<String> algoritmos = Arrays.asList("VADER", "TEXTBLOB", "BERT", "NAIVE BAYES", "SVM");

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
        Button botonAnalizar = new Button("Analizar sentimientos");
        Button botonComparar = new Button("Comparar algoritmos");
        Button botonCompararArchivo = new Button("Comparar archivo completo");


        Label mensajeLabel = new Label();

        ComboBox<String> selectorAlgoritmo = new ComboBox<>();
        selectorAlgoritmo.getItems().addAll(algoritmos);
        selectorAlgoritmo.setPromptText("Selecciona un algoritmo");

        ListView<String> listaComparacion = new ListView<>();
        listaComparacion.getItems().addAll(algoritmos);
        listaComparacion.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listaComparacion.setPrefHeight(80);



botonCompararArchivo.setOnAction(e -> {
    List<String> seleccionados = listaComparacion.getSelectionModel().getSelectedItems();
    if (seleccionados.isEmpty()) return;

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Seleccionar archivo de texto con frases");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
    File archivo = fileChooser.showOpenDialog(null);
    if (archivo == null) return;

    try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
        String linea;
        int id = 1;
        File comparacionCSV = new File("historial_comparacion.csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(comparacionCSV));
        writer.write("FraseID,Algoritmo,Sentimiento,Detalles\n");

        Map<String, String> mapaScripts = Map.of(
            "VADER", "vader.py",
            "TEXTBLOB", "analizador_textblob.py",
            "BERT", "analizador_bert.py",
            "NAIVE BAYES", "analizador_nb.py",
            "SVM", "analizador_svm.py"
        );

        while ((linea = lector.readLine()) != null) {
            if (linea.trim().isEmpty()) continue;

            String preprocesado = preprocesarTextoConPython(linea);
            if (preprocesado == null || preprocesado.isEmpty()) continue;

            for (String nombreAlgoritmo : seleccionados) {
                String script = mapaScripts.getOrDefault(nombreAlgoritmo.toUpperCase(), null);
                if (script == null) continue;

                ProcessBuilder pb = new ProcessBuilder("python", "python/" + script);
                Process process = pb.start();

                try (BufferedWriter pyWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                    String json = "{\"texto\": \"" + preprocesado
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r") + "\"}";
                    pyWriter.write(json);
                    pyWriter.flush();
                }

                StringBuilder resultado = new StringBuilder();
                try (BufferedReader pyReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String lineaResultado;
                    while ((lineaResultado = pyReader.readLine()) != null) {
                        resultado.append(lineaResultado);
                    }
                }

                String sentimiento = "";
                String detalles = resultado.toString();
                if (detalles.contains("\"etiqueta\"")) {
                    int idx = detalles.indexOf("\"etiqueta\":\"");
                    if (idx != -1) {
                        int start = idx + 12;
                        int end = detalles.indexOf("\"", start);
                        sentimiento = detalles.substring(start, end);
                    }
                } else if (detalles.contains("\"label\"")) {
                    int idx = detalles.indexOf("\"label\":\"");
                    if (idx != -1) {
                        int start = idx + 9;
                        int end = detalles.indexOf("\"", start);
                        sentimiento = detalles.substring(start, end);
                    }
                } else if (detalles.contains("\"resultado\"")) {
                    sentimiento = detalles.substring(detalles.indexOf(":") + 1).replaceAll("[{}\"]", "").trim();
                }

                writer.write(id + "," + nombreAlgoritmo + "," + sentimiento + "," + detalles.replaceAll(",", ";") + "\n");
            }
            id++;
        }
        writer.close();

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Comparación completada");
        alerta.setHeaderText(null);
        alerta.setContentText("✅ Comparación finalizada. Los resultados se han guardado en 'historial_comparacion.csv'.");
        alerta.showAndWait();

    } catch (IOException ex) {
        ex.printStackTrace();
    }
});




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
                        File archivoNuevo = new File(archivoCargado.getParentFile(), archivoCargado.getName().replace(".txt", "") + "_preprocesado.txt");
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

        botonAnalizar.setOnAction(e -> analizarTexto(selectorAlgoritmo.getValue(), entradaTexto.getText()));

        botonComparar.setOnAction(e -> {
            List<String> seleccionados = listaComparacion.getSelectionModel().getSelectedItems();
            if (seleccionados.isEmpty()) {
                mostrarAlerta("Selecciona al menos un algoritmo para comparar.", Alert.AlertType.WARNING);
                return;
            }
            String texto = entradaTexto.getText();
            if (texto == null || texto.trim().isEmpty()) {
                mostrarAlerta("⚠️ Primero debes preprocesar el texto.", Alert.AlertType.WARNING);
                return;
            }
            ComparadorAlgoritmos.compararAlgoritmos(seleccionados, texto, archivoCargado != null ? archivoCargado.getName() : "texto_manual");
        });

        VBox layout = new VBox(10,
                new Label("Introduce texto:"), entradaTexto,
                new HBox(10, botonCargar, botonPreprocesar),
                new Label("Selecciona algoritmo para análisis individual:"), selectorAlgoritmo,
                botonAnalizar,
                new Label("Selecciona algoritmos para comparar:"), listaComparacion,botonComparar , botonCompararArchivo,
                mensajeLabel
        );

        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        Scene scene = new Scene(layout, 600, 650);
        primaryStage.setTitle("Analizador de Sentimientos");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String preprocesarTextoConPython(String texto) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "python/preprocesamiento_final.py");
            Process process = pb.start();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                String inputJson = "{\"texto\": \"" + texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"}";
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

            return new JSONObject(salida.toString()).getString("resultado");

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void analizarTexto(String algoritmo, String texto) {
        if (algoritmo == null || texto == null || texto.isEmpty()) {
            mostrarAlerta("⚠️ Debes seleccionar un algoritmo y cargar texto primero.", Alert.AlertType.WARNING);
            return;
        }

        String script = switch (algoritmo.toUpperCase()) {
            case "VADER" -> "vader.py";
            case "TEXTBLOB" -> "analizador_textblob.py";
            case "BERT" -> "analizador_bert.py";
            case "NAIVE BAYES" -> "analizador_nb.py";
            case "SVM" -> "analizador_svm.py";
            default -> null;
        };

        if (script == null) {
            mostrarAlerta("Algoritmo no soportado.", Alert.AlertType.ERROR);
            return;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("python", "python/" + script);
            Process process = pb.start();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                String inputJson = "{\"texto\": \"" + texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"}";
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

            mostrarAlerta("Resultado del análisis con " + algoritmo + ":\n" + salida.toString(), Alert.AlertType.INFORMATION);

        } catch (IOException ex) {
            ex.printStackTrace();
            mostrarAlerta("Error al ejecutar el análisis.", Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
