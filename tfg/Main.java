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
    if (seleccionados.isEmpty()) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Sin algoritmos");
        alerta.setContentText("Por favor, selecciona al menos un algoritmo.");
        alerta.showAndWait();
        return;
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Seleccionar archivo de texto con frases");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
    File archivo = fileChooser.showOpenDialog(null);
    if (archivo == null) return;

    try {
        List<String> frases = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] partes = linea.split("\t", 2);
                if (partes.length == 2) {
                    frases.add(partes[1].trim()); // Usar solo el texto de la frase
                } else {
                    frases.add(linea.trim()); // Por si no hay tabulación
                }
            }
        }

        List<String> frasesPreprocesadas = new ArrayList<>();
        for (String frase : frases) {
            String pre = preprocesarTextoConPython(frase);
            frasesPreprocesadas.add((pre != null && !pre.isEmpty()) ? pre : "");
        }

        // Construir JSON con frases preprocesadas y algoritmos seleccionados
        JSONObject json = new JSONObject();
        json.put("frases", frasesPreprocesadas);
        json.put("algoritmos", seleccionados);

        ProcessBuilder pb = new ProcessBuilder("python", "python/comparar_batch.py");
        Process process = pb.start();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            writer.write(json.toString());
            writer.flush();
        }

        // Leer errores de Python si los hay
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String linea;
            while ((linea = errorReader.readLine()) != null) {
                System.err.println("❌ Error desde Python: " + linea);
            }
        }

        // Leer salida estándar si la hay
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String respuesta;
            while ((respuesta = reader.readLine()) != null) {
                System.out.println("✔ Salida Python: " + respuesta);
            }
        }

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Comparación completada");
        alerta.setHeaderText(null);
        alerta.setContentText("✅ Comparación terminada. Consulta 'historial_comparacion.csv'.");
        alerta.showAndWait();

    } catch (IOException ex) {
        ex.printStackTrace();
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setHeaderText("Error al procesar frases");
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
