import sys
import json
import joblib

# Cargar modelo SVM entrenado
with open("modelo_svm/modelo_svm.pkl", "rb") as f:
    modelo = joblib.load(f)

# Leer entrada JSON desde stdin
entrada = json.loads(sys.stdin.read())
texto = entrada.get("texto", "")

# Clasificar
etiqueta = modelo.predict([texto])[0]
probas = modelo.predict_proba([texto])[0]
clases = modelo.classes_

# Preparar respuesta
resultado = {
    "etiqueta": int(etiqueta),
    "probabilidades": {str(clase): round(float(p), 4) for clase, p in zip(clases, probas)}
}

# Enviar como JSON
print(json.dumps({"resultado": resultado}))
