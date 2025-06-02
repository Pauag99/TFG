import sys
import json
import nltk
from nltk.sentiment import SentimentIntensityAnalyzer

# Descargar el léxico de VADER (solo la primera vez)
nltk.download('vader_lexicon', quiet=True)

def analizar_sentimiento(texto):
    sia = SentimentIntensityAnalyzer()
    scores = sia.polarity_scores(texto)
    return scores

if __name__ == "__main__":
    try:
        entrada = json.loads(sys.stdin.read())
        texto = entrada.get("texto", "")
        resultado = analizar_sentimiento(texto)

        print(json.dumps({"resultado": resultado}))
    except Exception as e:
        print(json.dumps({"error": str(e)}))
