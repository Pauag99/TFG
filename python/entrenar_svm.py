import pandas as pd
import joblib
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.model_selection import train_test_split
from sklearn.svm import SVC
from sklearn.pipeline import Pipeline

# Cargar dataset
ruta_dataset = "modelo_svm/sentiment_dataset.csv"
df = pd.read_csv(ruta_dataset)

# Codificar etiquetas (si ya están como texto, esta parte puede omitirse)
etiquetas = df["etiqueta"]

# Dividir datos
X_train, X_test, y_train, y_test = train_test_split(df["texto"], etiquetas, test_size=0.2, random_state=42)

# Crear pipeline con CountVectorizer y SVM
modelo = Pipeline([
    ("vect", CountVectorizer()),
    ("clf", SVC(kernel="linear", probability=True))
])

# Entrenar modelo
modelo.fit(X_train, y_train)

# Guardar modelo
joblib.dump(modelo, "modelo_svm/modelo_svm.pkl")

print("✅ Modelo SVM entrenado y guardado correctamente en modelo_svm/modelo_svm.pkl")
