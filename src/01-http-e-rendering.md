## 🌐 Richiesta e Risposta HTTP

La base della comunicazione sul web è il protocollo **HTTP (Hypertext Transfer Protocol)**, un protocollo senza stato basato su un modello **client-server**.

### 📤 Richiesta HTTP (Client $\rightarrow$ Server)

Il **client** (browser, app mobile, o altro) richiede un'azione o una risorsa al server.

* **Metodo (Verbo) 🛠️**: Definisce l'azione desiderata:
    * **`GET`**: Richiede dati/risorsa (Lettura).
    * **`POST`**: Invia dati per la creazione di una nuova risorsa.
    * **`PUT`**: Aggiorna o sostituisce una risorsa esistente.
    * **`DELETE`**: Rimuove una risorsa.
* **URL (Uniform Resource Locator)**: L'indirizzo esatto della risorsa richiesta (es. `https://api.sito.com/articoli/123`).
* **Headers (Intestazioni)**: Meta-informazioni essenziali:
    * `User-Agent`: Identifica il software che invia la richiesta.
    * `Authorization`: Contiene token o credenziali per l'autenticazione.
    * `Accept`: Specifica il formato di risposta preferito (es. `application/json`, `text/html`).
* **Body (Corpo)**: Contiene i dati che il client sta inviando al server (tipicamente usato con `POST` e `PUT`).

### 📥 Risposta HTTP (Server $\rightarrow$ Client)

Il **server** elabora la richiesta e invia l'esito.

* **Status Code (Codice di Stato)**: Un codice a tre cifre che indica il risultato:
    * **$1\text{xx}$ (Informational)**: Richiesta ricevuta, continua il processo.
    * **$2\text{xx}$ (Successo)**: Esempio: **`200 OK`** (Richiesta riuscita).
    * **$3\text{xx}$ (Reindirizzamento)**: Esempio: **`301 Moved Permanently`**.
    * **$4\text{xx}$ (Errore Client)**: Esempio: **`404 Not Found`**, **`400 Bad Request`**, **`401 Unauthorized`**.
    * **$5\text{xx}$ (Errore Server)**: Esempio: **`500 Internal Server Error`**.
* **Headers (Intestazioni)**: Meta-informazioni sulla risposta:
    * `Content-Type`: Formato dei dati nel body (es. `text/html`, `application/json`).
    * `Cache-Control`: Istruzioni per la cache.
* **Body (Corpo)**: Il payload effettivo (pagina HTML, dati JSON, immagine, ecc.).

---

## 🖥️ Modalità di Rendering e Consumo Dati

Il rendering determina chi (server o client) è responsabile della generazione del markup HTML finale.

### 1. Server-Side Rendering (SSR)

Il **Server-Side Rendering (SSR)** è l'approccio tradizionale: il server genera l'intera pagina HTML prima di inviarla al client.

| Caratteristica | Descrizione |
| :--- | :--- |
| **Flusso** | 1. Browser richiede $\rightarrow$ 2. Server recupera dati e **genera HTML** $\rightarrow$ 3. Server invia HTML completo $\rightarrow$ 4. Browser visualizza. |
| **Dati scambiati** | Pagina **HTML completa** con dati già incorporati. |
| **Vantaggi** | **Migliore SEO** (i motori di ricerca vedono subito il contenuto), **tempo di visualizzazione iniziale più veloce** (First Contentful Paint). |
| **Svantaggi** | **Lento per le navigazioni successive** (richiede un ricaricamento completo), maggiore carico di elaborazione sul server. |
| **Framework Esempio** | PHP (Laravel), Python (Django), Java (Spring), Next.js/Nuxt.js (per il rendering ibrido). |

### 2. Client-Side Rendering (CSR)

Il **Client-Side Rendering (CSR)** sposta il lavoro di rendering sul browser del client.

| Caratteristica | Descrizione |
| :--- | :--- |
| **Flusso** | 1. Browser riceve HTML minimale e file JS $\rightarrow$ 2. JS esegue una chiamata **API** per i dati $\rightarrow$ 3. JS **genera dinamicamente HTML/CSS** con i dati $\rightarrow$ 4. Browser visualizza l'interfaccia completa. |
| **Dati scambiati** | Dati grezzi, principalmente **JSON**. |
| **Vantaggi** | **Esperienza Utente Fluida** (Single Page Application - SPA), minore carico sulla CPU del server, **Disaccoppiamento** (Frontend/Backend separati). |
| **Svantaggi** | **SEO peggiore** (inizialmente), tempo di caricamento iniziale più lento (il browser deve scaricare ed eseguire il JS). |
| **Framework Esempio** | **React**, **Angular**, **Vue.js** (utilizzati dal browser). |

## Conclusione Breve
L'SSR non scala bene in presenza di molti utenti perché la CPU del server diventa un collo di bottiglia. Il CSR scala meglio perché il carico di rendering è distribuito sulla potenza di calcolo di ciascun client.


## 🔗 API per il Consumo di Dati

L'uso moderno di **API (Application Programming Interface)** è intrinsecamente legato al CSR, in quanto l'API serve come unico fornitore di dati per il frontend disaccoppiato.

### Differenza Fondamentale nell'Approccio

| Caratteristica | SSR (Server-Side) | CSR (Client-Side) |
| :--- | :--- | :--- |
| **Output del Server** | **HTML renderizzato** (Il server è un "costruttore di pagine"). | **Dati JSON** (Il server è un "fornitore di dati"). |
| **Formato API** | L'API è spesso solo un'interfaccia interna per accedere al DB. | **API RESTful** o **GraphQL** (interfacce esterne per il frontend). |
| **Principio** | Il server gestisce sia i dati che la presentazione. | **Separazione delle Competenze**: Il server gestisce i dati, il client gestisce la presentazione. |

L'API, in questo contesto, definisce gli **endpoint** (URL specifici) che corrispondono alle risorse e ai metodi HTTP (`GET`, `POST`, ecc.) necessari per interagire con i dati.
