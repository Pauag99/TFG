import pandas as pd

# Ruta base
ruta = 'modelo_nb/'

# Cargar archivos
sentences = pd.read_csv(ruta + 'datasetSentences.txt', sep='\t')
dictionary = pd.read_csv(ruta + 'dictionary.txt', sep='|', names=['phrase', 'phrase_id'], engine='python')
labels = pd.read_csv(ruta + 'sentiment_labels.txt', sep='|')
splits = pd.read_csv(ruta + 'datasetSplit.txt')

# Normalizar texto
dictionary['phrase'] = dictionary['phrase'].str.strip()
sentences['sentence'] = sentences['sentence'].str.strip()

# Unir frases con phrase_id, labels y splits
merged = pd.merge(sentences, dictionary, left_on='sentence', right_on='phrase', how='left')
merged = pd.merge(merged, labels, left_on='phrase_id', right_on='phrase ids', how='left')
merged = pd.merge(merged, splits, on='sentence_index', how='left')

# Filtrar training set
train_data = merged[merged['splitset_label'] == 1].copy()

# Clasificar sentimiento
def clasificar_sentimiento(valor):
    if valor < 0.4:
        return 'negative'
    elif valor <= 0.6:
        return 'neutral'
    else:
        return 'positive'

train_data['etiqueta'] = train_data['sentiment values'].apply(clasificar_sentimiento)

# Exportar CSV
resultado = train_data[['sentence', 'etiqueta']].rename(columns={'sentence': 'texto'})
resultado.to_csv('modelo_nb/sentiment_dataset.csv', index=False)
print("✅ Dataset guardado como 'sentiment_dataset.csv'")
