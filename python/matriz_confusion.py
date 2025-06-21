import sys
import pandas as pd
from sklearn.metrics import confusion_matrix, ConfusionMatrixDisplay
import matplotlib.pyplot as plt

def mostrar_matriz_confusion(archivo_csv):
    print(f"📂 Cargando resultados desde {archivo_csv}...")
    df = pd.read_csv(archivo_csv)

    y_true = df["EtiquetaReal"]
    y_pred = df["EtiquetaPredicha"]

    print("📊 Matriz de Confusión:")
    matriz = confusion_matrix(y_true, y_pred)
    print(matriz)

    disp = ConfusionMatrixDisplay(confusion_matrix=matriz, display_labels=["Negativa (0)", "Positiva (1)"])
    disp.plot(cmap="Blues")
    plt.title(f"Matriz de Confusión para {archivo_csv}")
    plt.tight_layout()
    plt.show()

# ---------- USO ----------
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("❌ Uso: python matriz_confusion.py resultados_nb.csv")
    else:
        mostrar_matriz_confusion(sys.argv[1])
