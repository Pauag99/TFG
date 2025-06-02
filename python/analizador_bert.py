import sys
import json
from transformers import pipeline

# Cargar pipeline de análisis de sentimientos con modelo por defecto
analizador = pipeline("sentiment-analysis")

try:
    entrada = json.loads(sys.stdin.read())
    texto = entrada.get("texto", "")

    if not texto.strip():
        raise ValueError("Texto vacío")

    resultado = analizador(texto)[0]  # Devuelve una lista de dicts

    print(json.dumps({"resultado": resultado}))

except Exception as e:
    print(json.dumps({"error": str(e)}))
