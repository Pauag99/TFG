import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import json
import sys

# Cargar CSV
df = pd.read_csv("historial_comparacion.csv")

# Verificar los algoritmos presentes
algoritmos_permitidos = {"NAIVE BAYES", "SVM"}
algoritmos_actuales = set(df["Algoritmo"].str.upper().unique())

if algoritmos_actuales != algoritmos_permitidos:
    print("❌ El archivo contiene algoritmos no permitidos para este análisis.")
    print("Algoritmos detectados:", algoritmos_actuales)
    sys.exit(1)

# Extraer etiqueta
df["etiqueta"] = df["Resultado"].apply(lambda x: json.loads(x)["resultado"]["etiqueta"])

# Mostrar resumen en consola
print("=== Resultados por algoritmo ===")
resumen = df.groupby(["Algoritmo", "etiqueta"]).size().unstack(fill_value=0)
resumen.columns = ["Negativo (0)", "Positivo (1)"]
print(resumen)

print("\n=== Porcentajes por algoritmo ===")
print(resumen.div(resumen.sum(axis=1), axis=0).round(3))

# Gráfico con seaborn
sns.set(style="whitegrid", palette="pastel")
plt.figure(figsize=(10, 6))
sns.countplot(data=df, x="Algoritmo", hue="etiqueta")

# Ajustes
plt.title("Comparación de Sentimientos por Algoritmo (NB vs SVM)")
plt.xlabel("Algoritmo")
plt.ylabel("Número de frases")
plt.legend(title="Etiqueta", labels=["Negativo (0)", "Positivo (1)"])
plt.tight_layout()

# Mostrar gráfico
plt.show()
