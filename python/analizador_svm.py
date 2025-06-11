# archivo: python/analizador_svm.py

import sys
import json
import joblib

# Cargar modelo SVM
modelo = joblib.load("modelo_svm/modelo_svm.pkl")

# Leer JSON desde stdin
try:
    entrada = json.loads(sys.stdin.read())
    texto = entrada.get("texto", "")

    # Realizar predicción
    probabilidades = modelo.predict_proba([texto])[0]
    etiquetas = modelo.classes_

    resultado = dict(zip(etiquetas, map(lambda x: round(x, 4), probabilidades)))
    etiqueta_predicha = etiquetas[probabilidades.argmax()]

    print(json.dumps({
        "resultado": {
            "etiqueta": etiqueta_predicha,
            "probabilidades": resultado
        }
    }))
except Exception as e:
    print(json.dumps({"error": str(e)}))
