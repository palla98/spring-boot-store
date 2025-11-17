Lo **Spring MVC** (Model-View-Controller) è un framework web all'interno dell'ecosistema **Spring Framework** di Java. È progettato per lo sviluppo di applicazioni web flessibili, disaccoppiate e scalabili, aderendo al *design pattern* **MVC**.

---

## 🏛️ L'Architettura MVC

Il pattern MVC separa le responsabilità di un'applicazione in tre componenti principali, promuovendo codice pulito e manutenibile:

* **Model (Modello) 💾**: Rappresenta i **dati** dell'applicazione e la **logica di business**. Non si preoccupa di come i dati vengono visualizzati. Può essere rappresentato da semplici POJO (Plain Old Java Object) o entità complesse che interagiscono con il database (tramite JPA/Hibernate).
* **View (Vista) 🖼️**: È lo strato di **presentazione** che riceve i dati dal Modello e li rende all'utente finale, solitamente in formato **HTML**, ma anche JSON, XML, o altri. Esempi comuni sono **JSP** (JavaServer Pages), **Thymeleaf** o **Freemarker**.
* **Controller (Controllore) 🕹️**: Agisce come l'**intermediario** tra l'utente e l'applicazione. Riceve l'input dell'utente (la richiesta HTTP), chiama la logica di business nel Modello, e seleziona la Vista appropriata per presentare il risultato.

---

## 📚 Richiesta Semplificata in Spring MVC: Esempio: `GET /books`

### 1. Il Cliente Chiede

* **Tu (Client):** Il tuo browser invia la richiesta: **"Voglio la pagina dei libri (`GET /books`)"**.
* **Destinazione (Server):** La richiesta arriva a Spring MVC.

---

### 2. Spring MVC Dirige

* **`DispatcherServlet`:** Agisce come un centralino. Intercetta la richiesta e la passa al **Controller** giusto.
* **Controller:** È in attesa per l'indirizzo `/books`. Riceve la richiesta.

---

### 3. Il Controller Lavora

* **Controller ➡️ Model:** Il Controller dice al **Modello** (lo strato di logica/dati): **"Dammi l'elenco dei libri dal database."**
* **Model ⬅️ Controller:** Il Modello recupera la lista dei libri e la restituisce al Controller.

---

### 4. Il Server Crea la Pagina

* **Controller ➡️ View:** Il Controller passa la lista dei libri alla **View** (la struttura della pagina, es. un file HTML template).
* **View:** Prende la lista e **costruisce l'HTML completo** (aggiungendo la lista dei libri al template).

---

### 5. Il Cliente Riceve

* **Server ➡️ Client:** Il server invia la risposta: **"Ecco l'HTML della pagina Libri"** (con codice `200 OK`).
* **Tu (Client):** Il tuo browser mostra la pagina web completa.


Assolutamente\! Riorganizziamo e integriamo tutti i concetti su **Thymeleaf**, la sua configurazione e la distinzione tra **Controller** standard e **RestController** in un formato chiaro.

-----

## 🍃 Thymeleaf in Spring MVC: Dettagli e Configurazione

**Thymeleaf** è il **Motore di Template (View)** utilizzato per implementare l'approccio **Server-Side Rendering (SSR)** in un'applicazione Spring. Il suo compito è generare dinamicamente l'HTML finale che verrà inviato al client.

### 1\. Configurazione: `pom.xml`

Per utilizzare Thymeleaf in un progetto Spring Boot, è necessario includere la sua *dependency* nel file `pom.xml` (Maven):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

Quando questa dipendenza è presente, Spring Boot abilita l'**Auto-configurazione** di Thymeleaf.

### 2\. Posizione dei Template

Per impostazione predefinita, Spring Boot cerca i file template di Thymeleaf (che sono file `.html`) nella directory:

> `src/main/resources/templates/`

### 3\. Namespace e Sintassi

Nei file HTML che utilizzi come template, l'attributo `xmlns:th` (XML Namespace) è fondamentale:

```html
<html lang="en" xmlns:th="http://www.thymeleaf.org">
```

* **`xmlns:th`**: Questo namespace permette al tuo browser di visualizzare correttamente il file in modalità statica, ma soprattutto dice a Thymeleaf quali **attributi speciali** (`th:text`, `th:each`, ecc.) deve elaborare e sostituire in fase di rendering.

### 4\. Il Flusso Dati (Controller $\rightarrow$ View)

Il Controller è responsabile di recuperare i dati e di inserirli in un contenitore (`Model`) affinché Thymeleaf li possa utilizzare.

#### A. Il Controller (Java)

```java
@RequestMapping("/")
public String index(Model model){ // Model è il contenitore per i dati
    model.addAttribute("name", "Mosh"); // Aggiunge l'attributo "name" al Model
    return "index"; // Ritorna il nome logico del template (es. index.html)
}
```

#### B. La Vista (Thymeleaf HTML)

```html
<body>
    <h1 th:text="'Hello ' + ${name}">Testo Placeholder</h1>
</body>
```

**Risultato del rendering (HTML inviato al client):** `<h1>Hello Mosh</h1>`

-----

## 🆚 Distinzione: `@Controller` vs. `@RestController`

Questa è la differenza cruciale che definisce se stai facendo **SSR (Thymeleaf)** o costruendo un'**API RESTful (JSON)**.

### 1\. `@Controller` (Per SSR con Thymeleaf)

* **Scopo:** Gestisce le richieste web e restituisce il **nome logico di una View** (ad esempio, `"index"`).
* **Risposta:** Genera una risposta **HTML** completa.
* **Comportamento:** Funziona in coppia con il **View Resolver** e Thymeleaf.

### 2\. `@RestController` (Per API RESTful)

```java
@RestController // @Controller + @ResponseBody combinati
public class MessageController {

    @RequestMapping("/hello")
    public Message sayHello() {
        return new Message("Hello World!"); // non c'è bisogno del toString perchè viene fatto il cast dell'oggetto in JSON in automatico
    }
}
```

* **Scopo:** Gestisce le richieste API e restituisce **dati puri** (oggetti Java).
* **Risposta:** Di default, converte l'oggetto restituito (es. una Stringa, un oggetto List) in **JSON** o **XML** e lo inserisce direttamente nel corpo della risposta HTTP (grazie all'annotazione implicita `@ResponseBody`).
* **Comportamento:** **IGNORA** completamente il View Resolver e i template Thymeleaf. Utile per **Client-Side Rendering (CSR)**.

**Differenza Chiave:**
Un `@Controller` ritorna una **Vista** da renderizzare, mentre un `@RestController` ritorna **dati** nel corpo della risposta.

esempio classe restController:
```
@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    //@RequestMapping("/users") // di default una requestmapping è una GET
    @GetMapping
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}
```

## 1\. 🗃️ Risposta Semplice: `Iterable<User>` (GET /users)

Nel metodo `getAllUsers()`, la risposta è implicita.

```java
@GetMapping // Mappa a GET /users
public Iterable<User> getAllUsers() {
    return userRepository.findAll(); // Ritorna direttamente l'oggetto dati
}
```

* Il metodo findAll ritorna come default un iterable perchè non possiamo ritornare List ma dato che List eredita da iterable facciamo il ritorno in questo modo ovvero una lista di tutti gli utenti dal database).
* **Ruolo di `@RestController`:** Poiché la classe è annotata con **`@RestController`**, Spring Boot fa automaticamente due cose (combinando `@Controller` e `@ResponseBody`):
    1.  **Status Code Implicito:** Imposta lo **Status Code HTTP a `200 OK`**, indicando successo.
    2.  **Conversione Dati:** Serializza l'oggetto Java (`Iterable<User>`) in formato **JSON** (o XML, a seconda delle configurazioni) e lo inserisce nel **Body della risposta HTTP**.

> **Sintesi:** Questo approccio è **più semplice** perché ti concentri solo sui dati, lasciando a Spring la gestione dello status code di successo (`200 OK`).

-----

### 2\. Risposta Controllata: `ResponseEntity<User>` (Utente per ID)

```java
@GetMapping("/{id}")
public ResponseEntity<User> getUserById(@PathVariable Long id) {
    var user = userRepository.findById(id).orElse(null);
    if (user == null) {
        return ResponseEntity.notFound().build(); 
    }
    return ResponseEntity.ok(user);
}
```

* **Cosa Restituisce:** Ritorna un oggetto **`ResponseEntity`**, che ti dà il pieno **controllo** sulla risposta.
* **Controllo Esplicito:**
    * **Se Trovato:** `ResponseEntity.ok(user)` invia lo **Status `200 OK`** + i dati dell'utente.
    * **Se Non Trovato:** `ResponseEntity.notFound().build()` invia lo **Status `404 Not Found`** (senza dati nel corpo).
* **Quando Usarla:** Quando la richiesta può fallire o avere esiti diversi dal successo, e devi comunicare lo **Status Code corretto** al client (cruciale per le API REST).