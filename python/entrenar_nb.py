import pandas as pd
import pickle
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.pipeline import Pipeline

# Cargar dataset
data = pd.read_csv('modelo_nb/sentiment_dataset.csv')
X = data['texto']
y = data['etiqueta']

# Dividir datos (puede ser útil para probar accuracy)
X_train, _, y_train, _ = train_test_split(X, y, test_size=0.2, random_state=42)

# Pipeline: TF-IDF + Naive Bayes
modelo = Pipeline([
    ('tfidf', TfidfVectorizer()),
    ('nb', MultinomialNB())
])

# Entrenar
modelo.fit(X_train, y_train)

# Guardar modelo entrenado
with open('modelo_nb/modelo_nb.pkl', 'wb') as f:
    pickle.dump(modelo, f)

print("✅ Modelo NB entrenado y guardado como modelo_nb.pkl")
