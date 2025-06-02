import sys
import json

entrada = json.loads(sys.stdin.read())
texto = entrada['texto'].lower()

# Análisis simple: esto lo puedes cambiar a un modelo real
if "bien" in texto or "excelente" in texto:
    resultado = "bueno"
elif "mal" in texto or "horrible" in texto:
    resultado = "malo"
else:
    resultado = "neutro"

print(json.dumps({'resultado': resultado}))
