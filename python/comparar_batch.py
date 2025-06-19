import sys
import json
import subprocess
import csv
import os
from multiprocessing import Pool
from datetime import datetime

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
    tareas = [(i + 1, frase, algo) for i, frase in enumerate(frases) for algo in algoritmos]

    with Pool() as pool:
        resultados = pool.map(procesar_entrada, tareas)

    # Asegurar que la carpeta 'texto/' existe
    os.makedirs("texto", exist_ok=True)

    # Crear nombre de archivo con fecha y hora dentro de 'texto/'
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    nombre_archivo = f"historial_comparacion_{timestamp}.csv"
    ruta_archivo = os.path.join("texto", nombre_archivo)

    # Guardar resultados
    with open(ruta_archivo, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=["FraseID", "Algoritmo", "Resultado"])
        writer.writeheader()
        writer.writerows(resultados)

    # Imprimir ruta del archivo generado
    print(ruta_archivo)

if __name__ == "__main__":
    main()
