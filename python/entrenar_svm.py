import os
import joblib
from sklearn.svm import LinearSVC
from sklearn.calibration import CalibratedClassifierCV
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.pipeline import Pipeline
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report

# Ruta base al dataset
BASE_DIR = "C:/Users/Pau/Documents/TFG DATA/datasets/aclImdb/train"

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

# Crear pipeline con CountVectorizer + SVM calibrado
pipeline = Pipeline([
    ("vect", CountVectorizer(lowercase=True, stop_words='english')),
    ("clf", CalibratedClassifierCV(LinearSVC(), cv=5)),
])

# Entrenar modelo
pipeline.fit(X_train, y_train)

# Evaluación
y_pred = pipeline.predict(X_test)
print(classification_report(y_test, y_pred))

# Guardar modelo
os.makedirs("modelo_svm", exist_ok=True)
joblib.dump(pipeline, "modelo_svm/modelo_svm.pkl")
print("✅ Modelo SVM entrenado y guardado en modelo_svm/modelo_svm.pkl")
