package tfg;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ComparadorAlgoritmos {

    public static void compararAlgoritmos(List<String> algoritmos, String texto, String nombreArchivo) {
        List<ResultadoAlgoritmo> resultados = new ArrayList<>();

        for (String algoritmo : algoritmos) {
            String script = switch (algoritmo.toUpperCase()) {
                case "VADER" -> "vader.py";
                case "TEXTBLOB" -> "analizador_textblob.py";
                case "BERT" -> "analizador_bert.py";
                case "NAIVE BAYES" -> "analizador_nb.py";
                case "SVM" -> "analizador_svm.py";
                default -> null;
            };

            if (script == null) continue;

            try {
                ProcessBuilder pb = new ProcessBuilder("python", "python/" + script);
                Process process = pb.start();

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                    String json = "{\"texto\": \"" + texto
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

                JSONObject json = new JSONObject(salida.toString());
                String resultado = json.has("resultado") ? json.get("resultado").toString() : salida.toString();

                resultados.add(new ResultadoAlgoritmo(algoritmo, resultado, nombreArchivo));

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        mostrarResultados(resultados);
    }

    private static void mostrarResultados(List<ResultadoAlgoritmo> resultados) {
        Stage ventana = new Stage();
        ventana.setTitle("Comparación de Algoritmos");

        TableView<ResultadoAlgoritmo> tabla = new TableView<>();

        TableColumn<ResultadoAlgoritmo, String> colAlgoritmo = new TableColumn<>("Algoritmo");
        colAlgoritmo.setCellValueFactory(new PropertyValueFactory<>("algoritmo"));

        TableColumn<ResultadoAlgoritmo, String> colResultado = new TableColumn<>("Resultado");
        colResultado.setCellValueFactory(new PropertyValueFactory<>("resultado"));

        TableColumn<ResultadoAlgoritmo, String> colArchivo = new TableColumn<>("Archivo");
        colArchivo.setCellValueFactory(new PropertyValueFactory<>("archivo"));

        tabla.getColumns().addAll(colAlgoritmo, colResultado, colArchivo);
        tabla.getItems().addAll(resultados);

        VBox layout = new VBox(tabla);
        Scene escena = new Scene(layout, 600, 400);
        ventana.setScene(escena);
        ventana.show();
    }

    public static class ResultadoAlgoritmo {
        private final String algoritmo;
        private final String resultado;
        private final String archivo;

        public ResultadoAlgoritmo(String algoritmo, String resultado, String archivo) {
            this.algoritmo = algoritmo;
            this.resultado = resultado;
            this.archivo = archivo;
        }

        public String getAlgoritmo() { return algoritmo; }
        public String getResultado() { return resultado; }
        public String getArchivo() { return archivo; }
    }
}
