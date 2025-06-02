import sys
import json

# Leer JSON desde stdin
entrada = json.loads(sys.stdin.read())
a = entrada['a']
b = entrada['b']
print(json.dumps({'resultado': a + b}))
