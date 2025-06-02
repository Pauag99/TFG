import sys
import json
import re
import emoji
import nltk
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.stem import PorterStemmer, WordNetLemmatizer

# Descargar recursos necesarios solo si no están ya presentes
try:
    nltk.data.find('tokenizers/punkt')
except LookupError:
    nltk.download('punkt')

try:
    nltk.data.find('corpora/stopwords')
except LookupError:
    nltk.download('stopwords')

try:
    nltk.data.find('corpora/wordnet')
except LookupError:
    nltk.download('wordnet')

# Inicializadores
stop_words = set(stopwords.words('english'))
stemmer = PorterStemmer()
lemmatizer = WordNetLemmatizer()

def limpiar_texto(texto):
    texto = texto.lower()
    texto = re.sub(r'<.*?>', '', texto)  # eliminar etiquetas HTML
    texto = re.sub(r'http\S+|www\.\S+', '', texto)  # eliminar URLs
    texto = emoji.replace_emoji(texto, replace='')  # eliminar emojis conocidos
    texto = texto.encode("ascii", "ignore").decode()  # eliminar caracteres no ASCII
    texto = re.sub(r'\d+', '', texto)  # eliminar números
    texto = re.sub(r'[^\w\s]', '', texto)  # eliminar puntuación
    texto = re.sub(r'(.)\1{2,}', r'\1\1', texto)  # reducir letras repetidas (ej: happppy -> happy)
    return texto

def eliminar_stopwords(tokens):
    return [word for word in tokens if word not in stop_words and len(word) > 1]

def aplicar_stemming(tokens):
    return [stemmer.stem(word) for word in tokens]

def aplicar_lemmatizacion(tokens):
    return [lemmatizer.lemmatize(word) for word in tokens]

def preprocesar(texto):
    limpio = limpiar_texto(texto)
    tokens = word_tokenize(limpio, preserve_line=True)
    tokens = eliminar_stopwords(tokens)
    tokens = aplicar_stemming(tokens)
    tokens = aplicar_lemmatizacion(tokens)
    return ' '.join(tokens)

# Procesamiento principal
try:
    entrada_json = sys.stdin.read()
    entrada = json.loads(entrada_json)
    texto_original = entrada.get("texto", "")
    resultado = preprocesar(texto_original)
    print(json.dumps({"resultado": resultado}))
except Exception as e:
    print(json.dumps({"error": str(e)}))
