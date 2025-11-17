## 🔐 Metodi di Autenticazione: **Session-Based** vs **Token-Based**

L’autenticazione è il processo con cui un server verifica l’identità di un utente.
I due approcci più comuni nel web moderno sono **Session-Based Authentication** e **Token-Based Authentication (es. JWT)**.

---

### 🧩 **1️⃣ Session-Based Authentication**

È il metodo **tradizionale**, usato nei siti web classici (SSR).

#### 🔹 Flusso

1. Il **client** invia le proprie **credenziali** (es. email e password) al server tramite una richiesta `POST`.
2. Il **server verifica** le credenziali.
   Se sono valide, **crea una sessione** nel proprio archivio (in memoria, database o cache) e genera un **session ID** univoco.
3. Il **session ID** viene inviato al client all’interno di un **cookie**.
4. Ad ogni richiesta successiva (es. `GET /orders`), il **browser** invia automaticamente il **cookie** con il session ID.
5. Il server legge il cookie, **riconosce la sessione** e autentica l’utente.

#### ✅ Vantaggi

* Semplice da implementare.
* Integrazione automatica con i browser (cookie gestiti nativamente).

#### ❌ Svantaggi

* Il **server deve conservare lo stato** di ogni sessione → memoria e database si appesantiscono.
* Difficile da scalare in ambienti distribuiti (più server → sessioni da condividere).
* Vulnerabile al *Cross-Site Request Forgery* (CSRF) se i cookie non sono protetti correttamente.

---

### 🔑 **2️⃣ Token-Based Authentication**

Metodo **stateless**, usato soprattutto nelle **API RESTful** e nelle **Single Page Application (SPA)**.

#### 🔹 Flusso

1. Il **client** invia le credenziali al server (`POST /login`).
2. Il **server verifica** le credenziali e, se corrette, **genera un token di accesso** (tipicamente un **JWT** – JSON Web Token).
3. Il **token** viene restituito al client, che lo **salva localmente** (es. in `localStorage` o `sessionStorage`).
4. Ad ogni richiesta successiva (es. `GET /orders`), il client **inserisce il token** nell’header della richiesta:

   ```
   Authorization: Bearer <token>
   ```
5. Il server **verifica la validità del token** (firma, scadenza, ecc.) e, se valido, riconosce l’utente senza consultare un archivio.

#### ✅ Vantaggi

* **Stateless** → il server non memorizza sessioni: più leggero e facilmente scalabile.
* Adatto per **API REST** e architetture distribuite (microservizi).
* Il token può contenere **informazioni aggiuntive** (es. ruolo, scadenza) nel suo payload.

#### ❌ Svantaggi

* Se un token viene rubato, l’attaccante può usarlo finché non scade.
* I token scaduti devono essere gestiti (es. tramite refresh token).
* Più complesso da implementare rispetto alle sessioni classiche.

---

### ⚖️ **Differenze Principali**

| Caratteristica                | Session-Based                       | Token-Based                       |
| ----------------------------- | ----------------------------------- | --------------------------------- |
| **Stato Server**              | ✅ Sì (server mantiene sessione)     | ❌ No (stateless)                  |
| **Scalabilità**               | Limitata                            | Ottima                            |
| **Memoria Server**            | Alta (una sessione per utente)      | Bassa                             |
| **Autenticazione Successiva** | Session ID in cookie                | Token nell’header `Authorization` |
| **Formato del token**         | ID univoco                          | JWT o simile (self-contained)     |
| **Uso tipico**                | Applicazioni web tradizionali (SSR) | API REST, SPA, Mobile Apps        |

---

## 🔑 **JSON Web Token (JWT)**

Un **JWT** (JSON Web Token) è uno **standard per l’autenticazione stateless**, usato nel modello **Token-Based Authentication**.
È un **token compatto e auto-contenuto**, che include tutte le informazioni necessarie per identificare un utente, **senza richiedere al server di memorizzare sessioni**.

---

### ⚙️ **Struttura del JWT**

Un JWT è composto da **tre parti**, separate da un punto (`.`):

```
xxxxx.yyyyy.zzzzz
```

Queste tre parti, codificate in **Base64**, rappresentano:

1. **Header**
2. **Payload**
3. **Signature**

---

### 🧩 **1️⃣ Header**

Contiene **metadati** sul token, in particolare:

* l’**algoritmo** di firma utilizzato (es. `HS256`, `RS256`),
* il **tipo** di token (`JWT`).

Esempio:

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

👉 Questo header viene poi **codificato in Base64** e rappresenta la **prima parte** del token.

---

### 📦 **2️⃣ Payload**

Contiene i **dati veri e propri** (claims), cioè le informazioni sull’utente o sul contesto di autenticazione.
Esempio tipico di payload:

```json
{
  "sub": "1234567890",  // Subject → ID utente
  "name": "Antonio",
  "admin": true,
  "iat": 1716142393     // Issued At → timestamp di creazione
}
```

I *claims* possono essere:

* **Standard** (definiti dalla specifica JWT):
  `sub`, `iat`, `exp`, `iss`, `aud`, ecc.
* **Custom** (definiti da te):
  `role`, `email`, `isAdmin`, ecc.

👉 Anche il payload viene **codificato in Base64**.

---

### 🔒 **3️⃣ Signature**

Serve per **verificare che il token non sia stato manomesso**.

Per crearla, il server combina:

```
base64UrlEncode(header) + "." + base64UrlEncode(payload)
```

e applica un algoritmo di firma (es. **HMAC-SHA256**) con una **chiave segreta** conosciuta solo dal server.

Esempio di pseudo-firma:

```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

👉 Questa parte **non viene decodificata**: serve solo per verificare l’integrità del token.

---

### 🔁 **Come Funziona in Pratica**

1. Il **client** fa login inviando le credenziali (`POST /login`).
2. Il **server verifica** le credenziali e **genera un JWT** firmato con la chiave segreta.
3. Il **client salva** il token (di solito in `localStorage` o `sessionStorage`).
4. Ad ogni richiesta successiva, il client invia il token negli header:

   ```
   Authorization: Bearer <jwt_token>
   ```
5. Il **server verifica la firma** usando la chiave segreta:

    * Se la firma è valida e il token non è scaduto → accesso consentito ✅
    * Altrimenti → richiesta rifiutata 🚫

---

### ⚖️ **In Sintesi**

| Parte         | Contenuto                   | Ruolo                              |
| ------------- | --------------------------- | ---------------------------------- |
| **Header**    | Algoritmo + tipo di token   | Specifica come è firmato           |
| **Payload**   | Dati utente e claims        | Identifica chi è l’utente          |
| **Signature** | Hash del contenuto + secret | Garantisce integrità e autenticità |

---

# 🔐 **Introduzione a Spring Security**

## ⚙️ 1️⃣ Aggiungere la dipendenza

Per abilitare la sicurezza in un progetto Spring Boot, basta aggiungere lo *starter* dedicato:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

## 🚪 2️⃣ Cosa succede di default

Dopo aver aggiunto la dipendenza e avviato l’applicazione, Spring Security:

* **protegge automaticamente tutte le rotte HTTP** (`/**`),
* e mostra una **pagina di login predefinita** su `http://localhost:8080/login`.

Per impostazione predefinita vengono generati:

* **Username:** `user`
* **Password:** stampata nel terminale all’avvio dell’app (esempio):

  ```
  Using generated security password: 1a2b3c4d-xxxx-xxxx-xxxx
  ```

Questa password cambia a ogni riavvio.

---

## 🚫 3️⃣ Problema iniziale

Con questa configurazione base, **tutte le richieste sono protette**, anche quelle che dovrebbero essere **pubbliche** (come registrazione utenti, homepage, o risorse statiche).

---

## 🔧 4️⃣ Rendere pubblici alcuni endpoint

Per rendere **accessibili senza login** determinati percorsi, bisogna **personalizzare la configurazione di sicurezza**.

Esempio:

```java

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.
            // 1. primo step per dire che bisogna creare una stateless session
            sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 2. disabilitare la CSRF (cross site request forgery)
            .csrf(AbstractHttpConfigurer::disable)
            // 3. autorizzazione delle richieste
            .authorizeHttpRequests(c -> c
                .requestMatchers("/carts/**").permitAll() // tutte permesse da /carts in poi
                .requestMatchers(HttpMethod.POST, "/users").permitAll() // autorizzo le post di /users
                .anyRequest().authenticated() // tutto il resto è protetto (403 forbidden)
            );

        return http.build();
    }
}
```

## ⚙️ **Cosa succede all’avvio dell’applicazione**

Quando avvii l’app (es. con `SpringApplication.run(...)`), Spring Boot:

1. **Crea il contesto dell’applicazione** (*ApplicationContext*):

    * Scansiona tutte le classi annotate con `@Configuration`, `@Component`, `@Service`, `@Controller`, ecc.
    * Registra i bean dichiarati tramite `@Bean`.

2. **Incontra la tua classe `SecurityConfig`**:

   ```java
   @Configuration
   @EnableWebSecurity
   public class SecurityConfig { ... }
   ```

    * `@Configuration` dice a Spring che questa classe **definisce dei bean**.
    * `@EnableWebSecurity` **attiva Spring Security** e dice al framework:

      > “usa la mia configurazione personalizzata per gestire la sicurezza HTTP”.

3. Spring individua il tuo **bean `SecurityFilterChain`**:

   ```java
   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { ... }
   ```

    * Questa è la **nuova modalità di configurazione** (dal 2023 in poi), che ha sostituito la vecchia `WebSecurityConfigurerAdapter`.
    * Spring chiama il metodo, esegue la configurazione e registra il risultato nel contesto come **bean di tipo `SecurityFilterChain`**.

---

## 🧱 **Cosa fa `HttpSecurity`**

`HttpSecurity` è un *builder* che ti permette di definire le regole di sicurezza della tua applicazione.
Il tuo codice configura tre aspetti fondamentali 👇

---

### 🧩 1️⃣ Session Management

```java
.sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

* Imposti la politica delle sessioni su **stateless**, quindi **Spring non crea né utilizza sessioni HTTP**.
* Questo è **fondamentale per le API REST** con autenticazione tramite token (JWT).
* In pratica: ogni richiesta è indipendente, non viene mantenuto uno “stato utente”.

---

### 🧩 2️⃣ CSRF

```java
.csrf(AbstractHttpConfigurer::disable)
```

* Disabiliti la protezione **CSRF (Cross-Site Request Forgery)**.
* È sicuro farlo nelle API REST perché **non si usano cookie di sessione**.
* Nelle applicazioni con autenticazione tramite cookie, invece, questa protezione andrebbe lasciata attiva.

---

### 🧩 3️⃣ Autorizzazione delle richieste

```java
.authorizeHttpRequests(c -> c
    .requestMatchers("/carts/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/users").permitAll()
    .anyRequest().authenticated()
)
```

* Qui stai definendo **le regole di accesso** per gli endpoint:

  | Regola                                                    | Significato                                                                      |
    | --------------------------------------------------------- | -------------------------------------------------------------------------------- |
  | `.requestMatchers("/carts/**").permitAll()`               | Tutte le richieste che iniziano con `/carts` sono **pubbliche**                  |
  | `.requestMatchers(HttpMethod.POST, "/users").permitAll()` | Le richieste `POST /users` (es. registrazione) sono **pubbliche**                |
  | `.anyRequest().authenticated()`                           | Tutto il resto richiede **autenticazione** (se non hai un token → 403 Forbidden) |

---

### 🧩 4️⃣ Costruzione della catena di filtri

```java
return http.build();
```

* Con questa riga, Spring costruisce e registra la **Security Filter Chain**, cioè una **catena di filtri** che intercettano *tutte le richieste HTTP*.
* Questi filtri sono responsabili di:

    * verificare l’autenticazione (es. tramite JWT),
    * controllare i permessi,
    * gestire eccezioni di sicurezza,
    * e applicare le regole definite sopra.

---

## 🚀 **Riassunto: cosa succede in pratica**

1. All’avvio, Spring carica `SecurityConfig` e crea un `SecurityFilterChain`.
2. Questa catena viene registrata internamente come “filtro globale” per tutte le richieste web.
3. Ogni volta che arriva una richiesta HTTP:

    * passa prima attraverso questa catena;
    * se corrisponde a un endpoint “permitAll”, viene lasciata passare;
    * altrimenti, Spring verifica se c’è un’**autenticazione valida** (es. un token JWT);
    * se manca → ritorna `403 Forbidden`.

---

💡 **In altre parole:**
Spring, all’avvio, “monta” un cancello di sicurezza davanti a tutta l’app.
Il tuo metodo `securityFilterChain()` definisce **chi può passare e chi no**.

---

## 🔐 **Hashing delle Password**

### 🧩 Cos’è l’hashing

L’**hashing** è una funzione *one-way*, cioè una trasformazione **non reversibile** che converte un testo (come una password) in una sequenza di caratteri apparentemente casuale.
Serve a **proteggere le password**: anche se qualcuno accede al database, non potrà risalire al valore originale.

Esempio concettuale:

```
"mypassword123" → "$2a$10$k3Y9ZB....R4QzW5Yq"
```

> ⚠️ L’hash **non può essere decifrato**, ma può essere **verificato**: si fa l’hash della password inserita e lo si confronta con quello salvato.

---

### ⚙️ **Configurazione del Password Encoder**

Spring Security mette a disposizione diversi algoritmi di hashing.
Il più comune (e raccomandato) è **BCrypt**, che applica più round di hashing e un *salt* casuale per ogni password.

Definisci un **bean** nel tuo file di configurazione:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

* `PasswordEncoder` è l’interfaccia.
* `BCryptPasswordEncoder` è l’implementazione concreta (usa BCrypt).
* Spring potrà iniettare automaticamente questo encoder dove serve.

---

### 🧱 **Esempio: Registrazione Utente con Password Hashata**

```java
@PostMapping
public ResponseEntity<?> createUser(
    @Valid @RequestBody RegisterUserRequest request
) {
    // 1️⃣ Controllo duplicati
    if (userRepository.existsByEmail(request.getEmail())) {
        return ResponseEntity.badRequest().body(
            Map.of("email", "already present")
        );
    }

    // 2️⃣ Mapping DTO → Entity (tramite MapStruct)
    var user = userMapper.toEntity(request);

    // 3️⃣ Hashing della password prima del salvataggio
    user.setPassword(passwordEncoder.encode(user.getPassword()));

    // 4️⃣ Salvataggio nel database
    userRepository.save(user);

    return ResponseEntity.status(HttpStatus.CREATED).build();
}
```

🔒 Ora, nel database, la password **non sarà salvata in chiaro**, ma solo come **hash**.

---

### 🔁 **Verifica in fase di Login**

Quando l’utente effettua il login:

1. Recuperi dal database l’utente tramite email.
2. Usi lo stesso `PasswordEncoder` per **verificare la corrispondenza**, ma utilizzi la funzione matches() del PasswordEncoder. Questo metodo sa come estrarre il salt dall'hash salvato, applicarlo alla password di login e confrontare il risultato.

```java
if (passwordEncoder.matches(rawPassword, user.getPassword())) {
    // Password corretta → login OK
} else {
    // Password errata
}
```

👉 In pratica, **Spring non decodifica mai l’hash**:
ricalcola l’hash della password fornita e lo confronta con quello salvato.

---

### ✅ **In sintesi**

| Passaggio       | Descrizione                                                |
| --------------- | ---------------------------------------------------------- |
| **Hashing**     | Trasforma la password in una stringa non reversibile       |
| **BCrypt**      | Algoritmo di hashing sicuro e “salato”                     |
| **Salvataggio** | Si memorizza solo l’hash nel DB, mai la password originale |
| **Verifica**    | Si confrontano gli hash, non le password in chiaro         |

---

# **1️⃣ Approccio manuale (Service gestisce login)**

```java
@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginUserRequest request) {
        boolean exist = userService.login(request.getEmail(), request.getPassword());
        if (exist)
            return ResponseEntity.ok().build();
        else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "user not found"));
    }
}
```

```java
@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean login(String email, String password) {
        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null)
            throw new UsernameNotFoundException("User not found");

        // Confronto manuale password
        return passwordEncoder.matches(password, user.getPassword());
    }
}
```

**Flusso:**

```
AuthController --> UserService --> UserRepository
```

* Il controller non fa logica, delega tutto al service.
* Il service gestisce recupero utente e verifica password.

---

# **2️⃣ Approccio con Spring Security (DaoAuthenticationProvider)**

Qui **Spring Security gestisce l’autenticazione** usando i provider, quindi non serve confrontare manualmente le password.

---

## **Flusso di autenticazione Spring Security**

```
AuthController
     |
     v
AuthenticationManager.authenticate(token)
     |
     v
ProviderManager (implements AuthenticationManager)
     |
     v
AuthenticationProvider(s) registrati
     |
     +--> DaoAuthenticationProvider
            |
            +--> UserDetailsService (recupera utente dal DB)
            +--> PasswordEncoder (verifica password)
```

## **Bean di sicurezza**

## **Controller usando AuthenticationManager**

```java
@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginUserRequest request) {

        // Spring Security gestisce autenticazione tramite provider
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
```

---

```java
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    // BCryptPasswordEncoder per hash delle password
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationProvider: DaoAuthenticationProvider per utenti dal DB
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // UserDetailsService custom
        provider.setPasswordEncoder(passwordEncoder());     // PasswordEncoder
        return provider;
    }

    // AuthenticationManager delega l’autenticazione ai provider registrati
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---


## **UserService implementa UserDetailsService**

```java
@AllArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Restituisce un UserDetails compatibile con Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList() // ruoli/authorities, qui vuoto
        );
    }
}
```

---
## Test case

### **1️⃣ L’utente invia login**

* Inserisce email e password e fa `POST /auth/login`.
* La richiesta arriva all’`AuthController`.

---

### **2️⃣ Il controller chiama l’AuthenticationManager**

* Passa email e password in un **token di autenticazione**.
* L’`AuthenticationManager` riceve il token.

---

### **3️⃣ L’AuthenticationManager chiama il Provider**

* Cerca tra i provider registrati chi può gestire il token.
* Trova il **DaoAuthenticationProvider**.

---

### **4️⃣ DaoAuthenticationProvider verifica l’utente**

* Chiama il **UserDetailsService** per cercare l’utente nel database.
* Se l’utente non esiste → lancia `UsernameNotFoundException`.
* Se esiste → restituisce i dati dell’utente.

---

### **5️⃣ DaoAuthenticationProvider verifica la password**

* Confronta la password inviata con quella memorizzata usando il **PasswordEncoder**.
* Se la password è corretta → autenticazione OK.
* Se sbagliata → lancia `BadCredentialsException`.

---

### **6️⃣ Controller risponde**

* Se autenticazione OK → ritorna `200 OK`.
* Se fallita → ritorna `401 Unauthorized` o `404 Not Found`.

---

💡 Nota: Spring Security supporta **altri provider** oltre a DaoAuthenticationProvider (LDAP, OAuth2, JWT, ecc.), quindi puoi cambiare facilmente la sorgente dell’utente.

---
# Generazione e validazione dei JSON Web Token (JWT)

## 🧩 1️⃣ — Dipendenze Maven necessarie

Aggiungile nel tuo `pom.xml`:

```xml
<!-- Per Thymeleaf + Spring Security (facoltativo, serve solo se usi pagine HTML protette) -->
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>

<!-- Libreria JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- Per leggere le variabili dal file .env -->
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```

---

## 🔐 2️⃣ — Generare una chiave segreta sicura

Da terminale:

```bash
openssl rand -base64 64
```

Questo comando genera una chiave sicura (random, 512 bit).

Esempio di output:

```
mef5mB4M4xpzkHJMiGeQrFGnFbvwhWd2OOGKJyb7EmM2XXSi+//Yd36Uz9SQOfM6MwlgKiuN/KzS42ZHUXf1kw==
```

---

## ⚙️ 3️⃣ — Configurare la secret con `.env` e `application.yml`

Nel file `.env` (nella root del progetto):

```env
JWT_SECRET=mef5mB4M4xpzkHJMiGeQrFGnFbvwhWd2OOGKJyb7EmM2XXSi+//Yd36Uz9SQOfM6MwlgKiuN/KzS42ZHUXf1kw==
```

Nel file `application.yml`:

```yaml
jwt:
  secret: ${JWT_SECRET}  # Spring leggerà il valore dal file .env
```

---

## 🧠 4️⃣ — Classe `JwtService`

```java
@Service
public class JwtService {

    // Legge la chiave segreta dal file .env tramite application.yml
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Genera un JWT contenente l'email dell'utente come "subject".
     */
    public String generateToken(String email) {
        final long tokenExpiration = 86400 * 1000; // 24 ore in millisecondi

        return Jwts.builder()
                .subject(email)                          // dati identificativi dell’utente
                .issuedAt(new Date())                    // data di emissione
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration)) // scadenza
                .signWith(Keys.hmacShaKeyFor(secret.getBytes())) // firma con la secret key
                .compact();                              // genera il token finale
    }

    /**
     * Valida il token JWT ricevuto.
     * - Controlla la firma (con la secret key)
     * - Verifica che non sia scaduto
     */
    public boolean validateToken(String token) {
        try {
            // Parsing e validazione del token
            var claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes())) // verifica la firma
                    .build()
                    .parseSignedClaims(token)  // decodifica il token e ne ottiene i dati
                    .getPayload();

            // Ritorna true se la data di scadenza è ancora valida
            return claims.getExpiration().after(new Date());
        } catch (JwtException e) {
            // Se la firma non è valida o il token è scaduto → false
            return false;
        }
    }
}
```

---

## 👤 5️⃣ — Generazione del token dopo l’autenticazione

Nel tuo controller o service per il login:

```java
@PostMapping("/login")
public ResponseEntity<JwtResponse> login(@RequestBody LoginUserRequest request) {
    // 1. Autentica l’utente tramite Spring Security
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );

    // 2. Genera il token associato all’email dell’utente autenticato
    var token = jwtService.generateToken(request.getEmail());

    // 3. Restituisci il token al client
    return ResponseEntity.ok(new JwtResponse(token));
}
```

---

## ✅ 6️⃣ — Endpoint di validazione del token (facoltativo)

Serve per test o per confermare che un token è valido.

```java
@PostMapping("/validate")
public boolean validate(@RequestHeader("Authorization") String authHeader) {
    // Rimuove il prefisso "Bearer " se presente
    var token = authHeader.replace("Bearer ", "");
    return jwtService.validateToken(token);
}
```

---

## 📦 7️⃣ — Classe `JwtResponse`

Una semplice DTO per restituire il token:

```java
@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
}
```

---

## 💡 In sintesi

| Passaggio | Cosa fa                                           | Dove                       |
| --------- | ------------------------------------------------- | -------------------------- |
| 1️⃣       | Installi `jjwt` e `spring-dotenv`                 | `pom.xml`                  |
| 2️⃣       | Generi una secret sicura                          | Terminale                  |
| 3️⃣       | La memorizzi nel `.env`                           | `.env` + `application.yml` |
| 4️⃣       | Implementi `JwtService` per creare/validare token | `service`                  |
| 5️⃣       | Generi il token dopo l’autenticazione             | `controller`               |
| 6️⃣       | (Opzionale) Endpoint per verificare i token       | `controller`               |

---

## Uso dei filtri 

ho fatto in modo che ogni richiesta che atterri sul controller prima passo per questo filtro e verifica se necessita di autenticazione per essere eseguita:

```java
@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1️⃣ Recupera l'header Authorization dalla richiesta
        var authHeader = request.getHeader("Authorization");

        
        // Quindi la condizione giusta è: "se l’header è mancante O non inizia con Bearer"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Nessun token → passo la richiesta al filtro successivo
            filterChain.doFilter(request, response);
            return;
        }

        // 2️⃣ Estraggo il token togliendo il prefisso "Bearer "
        var token = authHeader.replace("Bearer ", "");

        // 3️⃣ Verifico che il token sia valido
        if (!jwtService.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4️⃣ Se il token è valido, ottengo l'email (subject) dal token
        var email = jwtService.getEmailFromToken(token);

        // 5️⃣ Creo un oggetto Authentication manualmente.
        // Non serve la password, e non assegniamo ruoli in questo caso (authorities = null)
        //stavolta a diffenza del login per l oggetto authentication mi prendo l email dal token per non fare query per ottenerla dal db
        var authentication = new UsernamePasswordAuthenticationToken(
                email,  // principal → l’identità dell’utente
                null,   // credentials → password non necessaria
                null    // authorities → ruoli (null se non gestiti qui)
        );

        // 6️⃣ Aggiungo dettagli aggiuntivi presi dalla request (IP, sessione, ecc.)
        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        // 7️⃣ Imposto l’autenticazione nel SecurityContext corrente
        // Così Spring Security "riconosce" che l’utente è autenticato
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 8️⃣ Passo il controllo al filtro successivo nella catena
        filterChain.doFilter(request, response);
    }
}

@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.
                // 1. primo step per dire che bisogna creare una stateless session
                        sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 2. disabilitare la CSRF (cross site request forgery)
                .csrf(AbstractHttpConfigurer::disable)
                // 3. autorizzazione delle richieste
                .authorizeHttpRequests(c -> c
                        .requestMatchers("/carts/**").permitAll() // tutte permesse da /carts in poi
                        .requestMatchers(HttpMethod.POST, "/users").permitAll() // autorizzo le post di /users
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        //.requestMatchers(HttpMethod.POST, "/auth/validate").permitAll()  lo togliamo cosi vediamo se il filtro funziona
                        .anyRequest().authenticated() // tutto il resto è protetto (403 forbidden)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // questo filtro mi viene chimato prima di tutti perchè è quello che si occupa dell autenticazione e validazione token

        return http.build();
    }
```

# Differenza tra Access Token e Refresh Token

## 🧩 1️⃣ — Cos’è un **Access Token**

L’**Access Token** è il token che il client (es. frontend o app mobile) usa per accedere alle API protette.

In genere è:

* un **JWT firmato** (come quello che stai già generando)
* **a vita breve** → tipicamente 5–15 minuti
* **inviato nel header Authorization** in ogni richiesta:

  ```
  Authorization: Bearer <access_token>
  ```

### 📦 Contiene:

* l’identità dell’utente (`sub` = email, username, id…)
* eventuali ruoli/permessi (`roles`, `authorities`)
* data di scadenza (`exp`)

### ⚙️ Viene validato dal backend:

Ogni volta che il client chiama un endpoint, il backend:

1. Legge il token dall’header
2. Verifica la **firma** con la chiave segreta
3. Controlla se **è scaduto**
4. Se tutto ok → concede l’accesso

> 🧠 L’access token è ciò che “dimostra” che il client è autenticato.
> Ma scade presto per motivi di sicurezza.

---

## ♻️ 2️⃣ — Cos’è un **Refresh Token**

Il **Refresh Token** serve a **ottenere un nuovo Access Token** senza dover rifare il login.

In genere:

* è **più lungo** (scadenza 7–30 giorni)
* **non** viene inviato in ogni richiesta
* viene **salvato solo lato client** (ad esempio in un cookie HttpOnly o nel secure storage dell’app)
* viene usato **solo per chiedere un nuovo access token** all’endpoint `/refresh`

### 🧱 Flusso tipico:

1. L’utente fa login → riceve **Access Token** e **Refresh Token**
2. Il client usa l’access token per chiamare le API
3. Quando l’access token scade, il client chiama:

   ```
   POST /api/auth/refresh
   Body: { "refreshToken": "<refresh_token>" }
   ```
4. Il backend:

    * valida il refresh token
    * genera un **nuovo access token**
    * opzionalmente rinnova anche il refresh token

> ⚠️ Se il refresh token è scaduto → l’utente deve rifare il login.

---

## 🔐 3️⃣ — Differenze chiave

| Aspetto                   | **Access Token**                | **Refresh Token**                      |
| ------------------------- | ------------------------------- | -------------------------------------- |
| **Scopo**                 | Accedere alle API protette      | Ottenere un nuovo access token         |
| **Durata**                | Breve (5–15 minuti)             | Lunga (7–30 giorni)                    |
| **Inviato a**             | In ogni richiesta API           | Solo all’endpoint `/refresh`           |
| **Memorizzazione**        | In memoria o cookie HttpOnly    | Cookie HttpOnly / DB / secure storage  |
| **Contiene dati utente?** | ✅ Sì (es. email, ruoli)         | ❌ No, di solito solo ID o random token |
| **Rischio se rubato**     | Alto (accesso diretto alle API) | Medio (può solo generare altri token)  |

## 🔐 Perché usare il Refresh Token invece del Login continuo

Potresti pensare: “Se ogni 15 minuti devo comunque richiedere un nuovo access token, tanto vale rifare il login”.  
In realtà, il **refresh token** serve proprio a evitare questo. Ecco perché 👇

---

### 🤫 Login Silenzioso

Il refresh token permette un **login automatico in background**, senza che l’utente debba fare nulla.  
Il sistema rinnova l’access token in modo invisibile, mantenendo la sessione attiva.

---

### ⚙️ Flusso più semplice

- L’utente fa **login solo una volta** (es. ogni 30 giorni, quando scade il refresh token).  
- Ogni volta che l’access token scade (ogni 5–15 minuti), il client lo rinnova automaticamente.  
- Nessun intervento manuale, nessuna interruzione.

---

### 👀 Esperienza Utente Fluida

1. L’access token scade → il server risponde con `401 Unauthorized`.  
2. Il client invia il refresh token.  
3. Riceve un nuovo access token e ripete la richiesta fallita.  
4. Tutto accade in pochi millisecondi, **senza che l’utente se ne accorga**.

---

### 🚫 Senza Refresh Token?

Senza di lui, ogni volta che scade l’access token:
- l’utente dovrebbe rifare il **login manuale**;
- l’app perderebbe la sessione e interromperebbe il flusso di lavoro.

➡️ **Conclusione:** il refresh token offre sicurezza, praticità e un’esperienza d’uso continua.

---

## 🔹 1. Login: generazione di access e refresh token

Quando l’utente effettua il **login**, Spring Security autentica le sue credenziali.
Se l’autenticazione va a buon fine, generiamo due token:

* **Access token** → dura poco (es. 15 minuti), serve per autenticare le richieste.
* **Refresh token** → dura di più (es. 7 giorni), serve per ottenere un nuovo access token senza dover rifare il login.

Ecco il codice:

```java
@PostMapping("/login")
public ResponseEntity<JwtResponse> login(
        @RequestBody LoginUserRequest request,
        HttpServletResponse response
) {
    // 1️⃣ Autentica l'utente (Spring Security verifica email e password)
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
            )
    );

    // 2️⃣ Recupera l'utente dal database
    var user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    // 3️⃣ Genera i token
    var accessToken = jwtService.generateAccessToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
```

A questo punto, il refresh token non lo mandiamo nel body (troppo rischioso), ma lo salviamo in un **cookie HttpOnly** — così non è accessibile da JavaScript (protezione contro attacchi XSS).

---

## 🔹 2. Set del cookie sicuro con il refresh token

Il cookie viene configurato con alcune proprietà di sicurezza:

```java
    // 4️⃣ Crea il cookie per il refresh token
    var cookie = new Cookie("refreshToken", refreshToken);
    cookie.setHttpOnly(true); // non accessibile da JavaScript
    cookie.setPath("/auth/refresh"); //in automatico in localhost su questo path mi setta il cookie pronto per la richiesta (tipo nella request su postman)
    cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration()); // durata (es. 7 giorni)
    cookie.setSecure(true); // solo su HTTPS
    response.addCookie(cookie);

    // 5️⃣ Restituiamo nel body solo l'access token
    return ResponseEntity.ok(new JwtResponse(accessToken));
}
```

👉 **Risultato:**

* Il browser salva in automatico il cookie `refreshToken`.
* L’access token viene inviato al client (es. Postman o frontend) nel body della risposta.

---

## 🔹 3. Endpoint `/auth/refresh`

Quando l’access token scade, il client chiama `/auth/refresh`.
Spring recupera automaticamente il cookie `refreshToken` e lo passa al controller tramite `@CookieValue`.

```java
@PostMapping("/refresh")
public ResponseEntity<JwtResponse> refresh(
        @CookieValue(value = "refreshToken") String refreshToken
) {
    // 1️⃣ Verifica che il refresh token sia valido
    if (!jwtService.validateToken(refreshToken)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // 2️⃣ Estrae l'ID utente dal token
    var userId = jwtService.getUserIdFromToken(refreshToken);

    // 3️⃣ Recupera l'utente dal DB
    var user = userRepository.findById(userId).orElseThrow();

    // 4️⃣ Genera un nuovo access token
    var accessToken = jwtService.generateAccessToken(user);

    // 5️⃣ Restituisce solo il nuovo access token
    return ResponseEntity.ok(new JwtResponse(accessToken));
}
```

💡 In questo modo il refresh token:

* **non viaggia mai nel body** (più sicuro),
* **rimane gestito dal browser** tramite cookie.

---

## 🔹 4. Gestione degli errori con 401 invece di 403

Per impostazione predefinita, Spring Security restituisce **403 Forbidden** se un utente non è autenticato.
Ma in un’API JWT, è più corretto restituire **401 Unauthorized**, così il client sa che deve rigenerare l’access token.

Ecco come forzarlo nella configurazione:

```java
.exceptionHandling(c -> {
    // Se l'utente non è autenticato, rispondi con 401 invece di 403
    c.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
})
```

---

## 🔹 5. Riassunto logico

1. **Login:**

    * Verifica le credenziali.
    * Genera access + refresh token.
    * Access token → nel body.
    * Refresh token → nel cookie HttpOnly.

2. **Richieste protette:**

    * Il client invia l’access token nell’header `Authorization: Bearer <token>`.

3. **Quando l’access token scade:**

    * Il client chiama `/auth/refresh`.
    * Il server legge il refresh token dal cookie.
    * Se valido, genera un nuovo access token.

---


## 🔹 Role Authentication – Gestione dei Ruoli

Quando un utente viene creato, gli viene assegnato un **ruolo** (es. `USER` o `ADMIN`), che verrà poi salvato nel database.

## 🔹 1 Assegnazione del ruolo all’utente

Durante la registrazione (es. nel tuo `UserService` o `RegisterUserRequest`):

```java
// Setto il ruolo dello user al momento della creazione
user.setRole(Role.USER);
```

Nella tua entità `User`:

```java
@Column(name = "role")
@Enumerated(EnumType.STRING) // salva il nome del ruolo come stringa (es. "USER")
private Role role;
```

L’enum `Role` potrebbe essere qualcosa del genere:

```java
public enum Role {
    USER,
    ADMIN
}
```

---

## 🔹 2️⃣ Lettura del ruolo dal token

Quando il client invia una richiesta con un **JWT valido**, nel filtro JWT tu estrai i dati necessari dal token — ad esempio l’ID utente e il ruolo.

```java
var role = jwtService.getRole(token);
var userId = jwtService.getUserIdFromToken(token);
```

A questo punto il token è stato validato, quindi puoi autenticare l’utente nel contesto di Spring Security **senza fare query al database**.

---

## 🔹 3️⃣ Creazione dell’oggetto Authentication

Invece di cercare l’utente nel DB, puoi costruire un oggetto `UsernamePasswordAuthenticationToken` manualmente, usando le informazioni del token:

```java
// Il token è valido: autentichiamo l’utente nel SecurityContext
var authentication = new UsernamePasswordAuthenticationToken(
        userId, // o anche l'email, se preferisci
        null,   // nessuna password, perché stiamo usando un token
        List.of(new SimpleGrantedAuthority("ROLE_" + role)) // aggiungo il ruolo come autority
);
```

In questo modo, l’oggetto `Authentication` avrà il ruolo corretto (es. `ROLE_ADMIN` o `ROLE_USER`), che Spring userà per le autorizzazioni.

---

## 🔹 4️⃣ Protezione dei path in base al ruolo

Nella configurazione di **Spring Security**, puoi specificare quali endpoint sono accessibili solo a determinati ruoli.

Esempio:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**").permitAll()     // login e refresh aperti
    .requestMatchers("/admin/**").hasRole(Role.ADMIN.name()) // solo ADMIN può accedere
    .requestMatchers("/user/**").hasRole(Role.USER.name())   // solo USER
    .anyRequest().authenticated() // tutti gli altri devono essere autenticati
)
```

⚙️ **Cosa significa `hasRole(Role.ADMIN.name())`?**
Spring aggiunge automaticamente il prefisso `"ROLE_"` ai ruoli, quindi:

* `hasRole("ADMIN")` → controlla che l’utente abbia `ROLE_ADMIN`
* `hasRole("USER")` → controlla che l’utente abbia `ROLE_USER`

E infatti nel filtro tu hai aggiunto il ruolo così:

```java
new SimpleGrantedAuthority("ROLE_" + role)
```

---

## 🔹 5️⃣ Flusso completo in sintesi

1. **Registrazione:**

    * Viene creato l’utente e gli viene assegnato un ruolo (`USER` o `ADMIN`).

2. **Login:**

    * Generi un token JWT che contiene anche il ruolo dell’utente.

3. **Ogni richiesta successiva:**

    * Il filtro JWT legge il token.
    * Estrae `userId` e `role`.
    * Crea un `Authentication` con l’autorità corretta (`ROLE_ADMIN` o `ROLE_USER`).

4. **Spring Security:**

    * Controlla se l’utente ha il ruolo richiesto per l’endpoint.
    * Se non lo ha → 403 Forbidden.
    * Se non è autenticato → 401 Unauthorized.

---

## 🔹 Logging out users

Con JWT, il **logout** non è automatico come con le sessioni tradizionali, perché il token è **stateless** (il server non lo conserva da nessuna parte).
Per questo, ci sono due approcci principali:

---

### ✅ **1. Client-side logout** (più semplice, ma meno sicuro)

In questo caso è il **client** (es. browser o app frontend) che elimina i token memorizzati:

* cancella l’`accessToken` salvato in memoria o nel `localStorage`
* rimuove il `refreshToken` cookie (se presente)
* smette di inviare i token nelle richieste

📦 **Esempio frontend (JavaScript):**

```js
// Logout lato client
localStorage.removeItem("accessToken");

// Se il refresh token è in cookie HttpOnly, chiediamo al server di cancellarlo
fetch("/auth/logout", { method: "POST", credentials: "include" });
```

Questo metodo è **più semplice**, ma ha un limite:
👉 se un token era già stato rubato, **rimane valido** fino alla sua scadenza, perché il server non lo “invalida”.

---

### 🔒 **2. Server-side logout** (più sicuro)

In questo approccio il **server mantiene un registro** dei token attivi o revocati.

Quando l’utente fa logout:

* Il token viene **marcato come “invalid”** (ad esempio, salvato in una blacklist in memoria o nel database).
* Durante ogni richiesta, il filtro JWT controlla se il token è valido **e non è nella lista dei revocati**.

📘 **Esempio pratico (server side):**

```java
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        // 1️⃣ Invalida il refresh token (se presente)
        if (refreshToken != null) {
            tokenBlacklistService.addToBlacklist(refreshToken);
        }

        // 2️⃣ Rimuove il cookie dal browser
        var cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(0); // scadenza immediata
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}
```

📦 **Servizio di blacklist base:**

```java
@Service
public class TokenBlacklistService {
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void addToBlacklist(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
```

Nel filtro JWT, prima di autenticare l’utente, puoi aggiungere questo controllo:

```java
if (tokenBlacklistService.isBlacklisted(token)) {
    filterChain.doFilter(request, response);
    return;
}
```

---

## 🧠 **Riepilogo**

| Tipo di Logout  | Come funziona                              | Pro                                      | Contro                                 |
| --------------- | ------------------------------------------ | ---------------------------------------- | -------------------------------------- |
| **Client-side** | Il client elimina i token                  | Semplice, nessun carico sul server       | Se il token viene rubato, resta valido |
| **Server-side** | Il server tiene traccia dei token revocati | Più sicuro, puoi invalidare token rubati | Richiede memoria e gestione extra      |

---
