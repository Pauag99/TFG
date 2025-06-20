# comparar_batch.py
import sys
import json
import subprocess
import csv
from multiprocessing import Pool
from datetime import datetime
import os

scripts = {
    "VADER": "python/vader.py",
    "TEXTBLOB": "python/analizador_textblob.py",
    "BERT": "python/analizador_bert.py",
    "NAIVE BAYES": "python/analizador_nb.py",
    "SVM": "python/analizador_svm.py"
}

def procesar_entrada(params):
    frase_id, frase, algoritmo = params
    script = scripts.get(algoritmo)
    if not script:
        return None
    try:
        proc = subprocess.Popen(
            ["python", script],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        entrada_json = json.dumps({"texto": frase})
        salida, error = proc.communicate(entrada_json)
        return {
            "FraseID": frase_id,
            "Algoritmo": algoritmo,
            "Resultado": salida.strip()
        }
    except Exception as e:
        return {
            "FraseID": frase_id,
            "Algoritmo": algoritmo,
            "Resultado": f"error: {e}"
        }

def main():
    datos = json.load(sys.stdin)
    frases = datos["frases"]
    algoritmos = datos["algoritmos"]
    tareas = [(i+1, frase, algo) for i, frase in enumerate(frases) for algo in algoritmos]

    with Pool() as pool:
        resultados = pool.map(procesar_entrada, tareas)

    # Crear carpeta si no existe
    os.makedirs("texto", exist_ok=True)

    # Generar nombre de archivo con fecha y hora
    nombre_archivo = datetime.now().strftime("texto/%Y-%m-%d_%H-%M-%S.csv")

    # Guardar resultados en CSV
    with open(nombre_archivo, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=["FraseID", "Algoritmo", "Resultado"])
        writer.writeheader()
        writer.writerows(resultados)

    # Mostrar ruta por salida estándar
    print(f"✔ Resultados guardados en: {nombre_archivo}")

if __name__ == "__main__":
    main()
