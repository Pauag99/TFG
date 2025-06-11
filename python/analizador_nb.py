import sys
import json
import joblib

# Cargar el pipeline completo (vectorizer + Naive Bayes)
pipeline = joblib.load("modelo_nb/modelo_nb.pkl")

# Leer entrada JSON desde stdin
entrada = json.loads(sys.stdin.read())
texto = entrada.get("texto", "")

# Clasificar
etiqueta = pipeline.predict([texto])[0]
probas = pipeline.predict_proba([texto])[0]
clases = pipeline.classes_

# Preparar respuesta
resultado = {
    "etiqueta": int(etiqueta),
    "probabilidades": {str(clase): round(float(p), 4) for clase, p in zip(clases, probas)}
}

# Enviar como JSON
print(json.dumps({"resultado": resultado}))
