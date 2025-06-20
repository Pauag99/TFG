# graficar_resultados.py
import sys
import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import json
import os

if len(sys.argv) < 2:
    print("❌ Debes indicar el archivo CSV a graficar.")
    sys.exit(1)

archivo_csv = sys.argv[1]

if not os.path.exists(archivo_csv):
    print(f"❌ El archivo '{archivo_csv}' no existe.")
    sys.exit(1)

try:
    # Cargar CSV
    df = pd.read_csv(archivo_csv)

    # Extraer etiqueta del campo Resultado (asumiendo formato correcto)
    df["etiqueta"] = df["Resultado"].apply(lambda x: json.loads(x)["resultado"]["etiqueta"])

    # Mostrar resumen en consola
    print("=== Resultados por algoritmo ===")
    resumen = df.groupby(["Algoritmo", "etiqueta"]).size().unstack(fill_value=0)
    resumen.columns = ["Negativo (0)", "Positivo (1)"]
    print(resumen)

    print("\n=== Porcentajes por algoritmo ===")
    print(resumen.div(resumen.sum(axis=1), axis=0).round(3))

    # Generar gráfico
    sns.set(style="whitegrid", palette="pastel")
    plt.figure(figsize=(10, 6))
    sns.countplot(data=df, x="Algoritmo", hue="etiqueta")

    plt.title("Comparación de Sentimientos por Algoritmo")
    plt.xlabel("Algoritmo")
    plt.ylabel("Número de frases")
    plt.legend(title="Etiqueta", labels=["Negativo (0)", "Positivo (1)"])
    plt.tight_layout()
    plt.show()

except Exception as e:
    print(f"❌ Error procesando el archivo: {e}")
    sys.exit(1)
