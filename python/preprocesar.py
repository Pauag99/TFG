import sys
import json

# Leer la entrada JSON desde stdin
entrada = json.loads(sys.stdin.read())
texto = entrada.get("texto", "")

# Procesar: pasar a minúsculas
preprocesado = texto.lower()

# Devolver resultado como JSON
print(json.dumps({"resultado": preprocesado}))
