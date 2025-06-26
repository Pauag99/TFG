# Analizador de Sentimientos con Comparación de Modelos

Este proyecto permite analizar el sentimiento de textos en inglés utilizando múltiples algoritmos de clasificación, comparando sus resultados y visualizando sus métricas de rendimiento. La aplicación dispone de una interfaz gráfica construida con JavaFX, que se integra con scripts Python para realizar el procesamiento y la clasificación de forma modular.

## 📌 Funcionalidades principales

- Carga y preprocesamiento de texto en inglés.
- Análisis de sentimientos con varios modelos:
  - VADER
  - TextBlob
  - Naive Bayes
  - SVM
  - BERT
- Comparación de resultados entre modelos.
- Análisis individual o por lotes (desde archivos `.txt`).
- Exportación de resultados en archivos `.csv`.
- Visualización de métricas y probabilidades.
- Historial de análisis por fecha, modelo y texto.

## ⚙️ Requisitos

### Backend (Python):

- Python 3.8+
- Bibliotecas necesarias:

```
pandas
scikit-learn
transformers
nltk
matplotlib
textblob
vaderSentiment
```

Instalación de dependencias:

```bash
pip install -r requirements.txt
```

### Frontend (Java):

- Java 17+
- JavaFX 17+
- Uso de ProcessBuilder para ejecutar scripts Python desde la interfaz

## 🚀 Cómo ejecutar el proyecto

1. Compilar y ejecutar la interfaz JavaFX con tu IDE o desde terminal:

```bash
javac -cp "ruta/javafx/lib/*" interfaz/*.java
java -cp "ruta/javafx/lib/*:." interfaz.Main
```

2. Seleccionar una frase o un archivo `.txt` desde la interfaz.
3. Elegir los algoritmos deseados.
4. Visualizar los resultados y compararlos.
5. Graficar los resultados según necesidad.

## 📈 Comparación de modelos

El botón **"Comparar archivo completo"** permite analizar todas las frases de un archivo línea por línea, generar un CSV con los resultados y visualizar gráficos comparativos mediante el script `graficar_resultados.py`.


## 🧪 Dataset usado

El modelo ha sido entrenado y evaluado sobre el conjunto de datos **IMDb Movie Reviews**, ampliamente utilizado en tareas de análisis de sentimientos.
El analisis de frases se ha realizado sobre el conjunto de datos **Kaggle Movie Review Sentiment Analysis**

## ✅ Estado actual

- Funcionalidad estable y operativa.
- Modelos NB y SVM entrenados y comparables.
- Comparación entre 5 algoritmos disponible.
- Interfaz básica funcional con mejoras en desarrollo.

## 📌 Trabajo futuro

- Mejora de la interfaz (separación de análisis individual y masivo).
- Visualización directa de métricas y resultados.
- Soporte multilingüe.
- Despliegue como API REST o aplicación web.
- Evaluación ética y detección de sesgos.

## 👤 Autor

Este proyecto ha sido desarrollado como parte de un trabajo académico.  
**Autor**: *[Pau Aguilar Silvestre]*