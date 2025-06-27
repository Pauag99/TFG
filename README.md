# TFG
Durante tu proyecto de análisis de sentimientos en inglés con interfaz Java y scripts Python, has instalado las siguientes **dependencias**:

---

### 🐍 **Dependencias de Python**

#### 🔧 Generales

```bash
pip install nltk
pip install emoji
pip install textblob
pip install transformers
pip install torch
pip install scikit-learn
```

#### 📦 Recursos adicionales (usando código en los scripts)

* Desde `nltk`:

  ```python
  import nltk
  nltk.download('punkt')
  nltk.download('stopwords')
  nltk.download('wordnet')
  ```

* Desde `textblob`:

  ```python
  python -m textblob.download_corpora
  ```

#### 🔤 Análisis con modelos BERT:

```bash
pip install transformers
pip install torch
```

---

### ☕ **Dependencias para Java y JavaFX**

#### 📁 Librerías añadidas manualmente:

* Carpeta JavaFX:

  ```
  C:/Users/Pauag/Documents/javafx-sdk-24.0.1/lib/*.jar
  ```

* JSON (para manejo de datos entre Java y Python):

  ```
  C:/Users/Pauag/Documents/TFG DATA/json-20240303.jar
  ```

#### 📦 En `settings.json` de VSCode:

```json
{
  "java.project.referencedLibraries": [
    "C:/Users/Pauag/Documents/javafx-sdk-24.0.1/lib/*.jar",
    "C:/Users/Pauag/Documents/TFG DATA/json-20240303.jar"
  ]
}
```

---

### 🔄 **Integración Java ↔ Python**

* No requiere dependencia externa, pero **usa `ProcessBuilder`** en Java para ejecutar scripts Python desde la interfaz.

---

