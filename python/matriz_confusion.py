import pandas as pd
from sklearn.metrics import confusion_matrix, ConfusionMatrixDisplay
import matplotlib.pyplot as plt

def cargar_resultados(path_csv):
    df = pd.read_csv(path_csv)
    y_true = df["EtiquetaReal"]
    y_pred = df["EtiquetaPredicha"]
    return y_true, y_pred

def mostrar_matriz_confusion(y_true, y_pred, titulo):
    cm = confusion_matrix(y_true, y_pred)
    disp = ConfusionMatrixDisplay(confusion_matrix=cm)
    disp.plot(cmap="Blues")
    plt.title(titulo)
    plt.show()

if __name__ == "__main__":
    # Cambia los nombres si guardaste con otro nombre
    y_true_nb, y_pred_nb = cargar_resultados("../resultados_nb.csv")
    y_true_svm, y_pred_svm = cargar_resultados("../resultados_svm.csv")

    print("🔍 Mostrando matriz de confusión para Naive Bayes:")
    mostrar_matriz_confusion(y_true_nb, y_pred_nb, "Matriz de Confusión - Naive Bayes")

    print("🔍 Mostrando matriz de confusión para SVM:")
    mostrar_matriz_confusion(y_true_svm, y_pred_svm, "Matriz de Confusión - SVM")
