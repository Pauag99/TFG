import os
import joblib
import string
import numpy as np
import pandas as pd
from sklearn.naive_bayes import MultinomialNB
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.pipeline import Pipeline
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report

# Ruta base al dataset
BASE_DIR = BASE_DIR = "C:/Users/Pau/Documents/TFG DATA/datasets/aclImdb/train"


# Función para cargar reseñas
def cargar_datos(base_dir):
    textos = []
    etiquetas = []
    for etiqueta, subdir in [(1, "pos"), (0, "neg")]:
        dir_path = os.path.join(base_dir, subdir)
        for nombre_archivo in os.listdir(dir_path):
            with open(os.path.join(dir_path, nombre_archivo), encoding="utf-8") as f:
                textos.append(f.read())
                etiquetas.append(etiqueta)
    return textos, etiquetas

# Cargar los datos
textos, etiquetas = cargar_datos(BASE_DIR)

# Separar en train/test
X_train, X_test, y_train, y_test = train_test_split(textos, etiquetas, test_size=0.2, random_state=42)

# Crear pipeline con CountVectorizer + Naive Bayes
pipeline = Pipeline([
    ("vect", CountVectorizer(lowercase=True, stop_words='english')),
    ("clf", MultinomialNB()),
])

# Entrenar modelo
pipeline.fit(X_train, y_train)

# Evaluación
y_pred = pipeline.predict(X_test)
print(classification_report(y_test, y_pred))

# Guardar modelo
os.makedirs("modelo_nb", exist_ok=True)
joblib.dump(pipeline, "modelo_nb/modelo_nb.pkl")
print("✅ Modelo entrenado y guardado en modelo_nb/modelo_nb.pkl")