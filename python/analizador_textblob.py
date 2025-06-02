# analizador_textblob.py
import sys
import json
from textblob import TextBlob

try:
    entrada = json.loads(sys.stdin.read())
    texto = entrada.get("texto", "")
    blob = TextBlob(texto)
    resultado = {
        "polarity": round(blob.sentiment.polarity, 4),
        "subjectivity": round(blob.sentiment.subjectivity, 4)
    }
    print(json.dumps({"resultado": resultado}))
except Exception as e:
    print(json.dumps({"error": str(e)}))
