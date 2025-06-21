import os
import joblib
import csv
import sys
from sklearn.metrics import classification_report

def cargar_datos(directorio_base):
    textos, etiquetas = [], []
    for etiqueta, subcarpeta in [(1, "pos"), (0, "neg")]:
        carpeta = os.path.join(directorio_base, subcarpeta)
        for archivo in os.listdir(carpeta):
            ruta = os.path.join(carpeta, archivo)
            with open(ruta, encoding="utf-8") as f:
                textos.append(f.read())
                etiquetas.append(etiqueta)
    return textos, etiquetas

def evaluar_modelo(path_modelo, directorio_test, salida_csv):
    print(f"📦 Cargando modelo desde {path_modelo}...")
    modelo = joblib.load(path_modelo)

    print(f"📂 Cargando datos de test desde {directorio_test}...")
    textos, etiquetas = cargar_datos(directorio_test)

    print("🧠 Realizando predicciones...")
    predicciones = modelo.predict(textos)
    probabilidades = modelo.predict_proba(textos)

    print("📝 Guardando resultados en", salida_csv)
    with open(salida_csv, mode="w", encoding="utf-8", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["FraseID", "Texto", "EtiquetaReal", "EtiquetaPredicha", "Prob_0", "Prob_1"])
        for idx, (texto, real, pred, probs) in enumerate(zip(textos, etiquetas, predicciones, probabilidades), start=1):
            writer.writerow([idx, texto[:200].replace('\n', ' ') + "...", real, pred, round(probs[0], 4), round(probs[1], 4)])

    print("\n📊 Clasification Report:\n")
    print(classification_report(etiquetas, predicciones))

# ----------- CONFIGURACIÓN -----------

if __name__ == "__main__":
    modelo = sys.argv[1]  # Ej: modelo_nb/modelo_nb.pkl
    carpeta_test = "C:/Users/Pau/Documents/TFG DATA/datasets/aclImdb/test"  # Ajusta si hiciste copia
    salida = sys.argv[2]  # Ej: resultados_nb.csv
    evaluar_modelo(modelo, carpeta_test, salida)
