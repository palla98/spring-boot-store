## 🎯 DTO(Data Transfer Objects): Scopo e Vantaggi

I DTO (Data Transfer Objects) sono fondamentali nell'architettura delle applicazioni moderne per disaccoppiare l'API dalla logica e dal modello del database.

---

### 1. 🤝 Scopo Principale: Contratto di Dati
I DTO (come il tuo `UserDto`) servono come **contratto di dati** tra la tua applicazione (il **backend**) e il mondo esterno (tipicamente il **client** o un altro servizio). Definiscono esattamente cosa viene inviato e ricevuto.

### 2. 🛡️ Isolamento dell'Entità e Stabilità
**Non esponendo direttamente l'entità** (`User`), mantieni **coerente e stabile** la struttura interna del tuo database e del tuo modello ORM (Object-Relational Mapping). Se decidi di cambiare un nome di colonna nel database o di aggiungere un campo sensibile all'entità, **l'API esterna che consuma il `UserDto` non viene influenzata**.

### 3. ✂️ Filtering e Sicurezza
I DTO ti permettono di esporre **solo i dati necessari**. Ad esempio, `UserDto` non include la password hashata o altri dati interni (come la data di creazione/modifica o campi di *audit*) presenti nell'entità `User`, migliorando la sicurezza e riducendo il *payload* della risposta.
servono per esporre l entità al di fuori dell app, mantenendo coerente tutto e stabile
```java
@AllArgsConstructor
@Getter
public class UserDto {
    private Long id;
    private String name;
    private String email;
}

public interface UserRepository extends JpaRepository<User, Long> 
// la cambiamo in Jpa cosi quando i metodi ritornano delle list e non degli iterable e possiamo usare gli stream

@GetMapping
public Iterable<UserDto> getAllUsers() {

    return userRepository.findAll()
            .stream()
            .map(user -> new UserDto(user.getId(), user.getName(), user.getEmail()))
            .toList();
}
```
## 🛠️ Cosa Fa MapStruct?

MapStruct è un **generatore di codice** che semplifica l'implementazione dei *mapping* tra diversi tipi di bean Java (come entità e DTO).

* **Riduzione del Codice Boilerplate:** Il vantaggio principale è che **elimina il codice di *mapping* ripetitivo e noioso** (il cosiddetto *boilerplate*), che altrimenti dovresti scrivere manualmente. Ad esempio, non devi più scrivere dozzine di righe come `dto.setNome(entity.getNome());`
* **Interfaccia Semplice:** Devi solo definire una semplice **interfaccia *mapper*** annotata. MapStruct genera automaticamente l'implementazione in fase di compilazione.
* **Prestazioni Elevate:** Il codice generato utilizza **semplici invocazioni di metodi**, il che lo rende **veloce** e *type-safe* (sicuro dal punto di vista dei tipi), a differenza di soluzioni basate sulla *reflection* (come ModelMapper).
* **Controllo degli Errori in Compilazione:** Poiché il *mapping* viene generato e verificato in fase di compilazione, gli errori di *mapping* (ad esempio, un campo rinominato) vengono rilevati immediatamente, offrendo un **feedback rapido** allo sviluppatore.

In sintesi, MapStruct ti permette di **separare la logica di *mapping*** in interfacce dedicate, rendendo il codice della tua applicazione (come il livello *Service*) **più focalizzato sul *business*** e liberandolo dal compito meccanico di copiare i dati da un oggetto all'altro.
```java
@Mapper(componentModel = "spring") // per dire a spring di gestirlo
public interface UserMapper {
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())") //cosi facendo mappo il valore in runtime con lespressione
    UserDto toDto(User user);
}

@AllArgsConstructor
@Getter
public class UserDto {
    //@JsonIgnore // per fare in modo che venga ignorato l id in ritorno (come se lo togliessi)
    //@JsonProperty("user_id") //per rinominarlo invece
    private Long id;
    private String name;
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt; // campo aggiuntivo che ritornato è null dato che non lo abbiamo in User
}

@GetMapping("/{id}")
public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
    var user = userRepository.findById(id).orElse(null);
    if (user == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(userMapper.toDto(user));
}
```

## query parameters
```java
@GetMapping
public Iterable<UserDto> getAllUsers(@RequestParam(required = false, defaultValue = "", name = "sort") String sort) {
    //validazione del request parameter:
    //1. ci assicuriamo che possa essere opzionale con il required = false
    //2. vogliamo che come default quindi in caso non passo niente sia una stringa vuota sennò nel confronto con il set da eccezione
    //3. e vogliamo che il nome della key in caso cambi da key a sort e uno usa postman sia sicuro che continui a funzionare come "sort"
    //4. infine mi assicuro che il valore che passo sia o name o email altrimenti setta name
    if (!Set.of("name", "email").contains(sort))
        sort = "name";
    return userRepository.findAll(Sort.by(Sort.Direction.DESC, sort))
            .stream()
            .map(userMapper::toDto)
            .toList();
}
--> http://localhost:8080/users?sort=name
```
-----

## 🧭 Estrazione Header con `@RequestHeader`
```java
@RequestHeader(required = false, name = "x-auth-token") String authToken
```
L'annotazione `@RequestHeader` in Spring MVC ti permette di accedere direttamente ai valori specifici presenti negli header della richiesta HTTP.

| Parte | Funzione nel Codice | Spiegazione |
| :--- | :--- | :--- |
| **`@RequestHeader`** | Specifica che il valore della variabile `authToken` deve essere estratto dall'header HTTP. |
| **`name = "x-auth-token"`** | Definisce il nome esatto dell'header da cercare (`x-auth-token`). |
| **`required = false`** | Indica che se l'header è assente nella richiesta, la richiesta non fallirà. La variabile `authToken` sarà semplicemente `null`. |

Se in Postman invii un header come: `x-auth-token: 1234`, la variabile `authToken` nel tuo metodo prenderà il valore `"1234"`.

-----

## 💾 Endpoint: Creazione Utente (`POST /users`)

Questo endpoint accetta un DTO specifico per la registrazione (`RegisterUserRequest`), lo converte in entità, lo salva e restituisce un DTO pulito (`UserDto`).

```java
@PostMapping
public ResponseEntity<UserDto> createUser(
    // 1. DTO di Richiesta: Accetta i dati, inclusi quelli sensibili (p.e. password).
    @RequestBody RegisterUserRequest request
) {
    // 2. DESERIALIZZAZIONE (Mapping Request -> Entity)
    // MapStruct converte il DTO in Entità 'User'. 
    // * CRITICO: La logica di HASHING della password dovrebbe essere qui o nel Service Layer.
    var user = userMapper.toEntity(request);
    
    // userRepository.save() salva l'entità nel database.
    // L'entità 'user' ora contiene l'ID generato dal database.
    userRepository.save(user);
    
    // 3. SERIALIZZAZIONE (Mapping Entity -> Response DTO)
    // Converte l'entità salvata nel DTO di risposta (UserDto), 
    // che NON contiene la password (sicurezza).
    UserDto userDto = userMapper.toDto(user);
    
    // 4. CORREZIONE RESTful: Restituisce 201 Created invece di 200 OK.
    // (Aggiungere l'header Location è la best practice, qui è omesso per brevità).
    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
}
```

## 💾 Endpoint: Update Utente (`UPDATE /users`)
Questo endpont prende un DTO specifico per l aggiornamento dati su una specificia entità, l aggiorna e ritorna il DTO appropriato
```java
@PutMapping("/{id}")
public ResponseEntity<UserDto> updateUser(
@RequestBody UpdateUserRequest request,
@PathVariable Long id
)
{
var user = userRepository.findById(id).orElse(null);
if (user == null) {
return ResponseEntity.notFound().build();
}
// update: dentro l interfaccia mapper c'è il contratto del metodo che poi verrà implementato da spring
userMapper.update(request, user);
userRepository.save(user);
return ResponseEntity.status(HttpStatus.OK).body(userMapper.toDto(user));
}
```

## 💾 Endpoint: Delete Utente (`DELETE /users`)
Questo endpoint prende un id ed elimina il corrispettivo user se lo trova e restituisce il suo DTO
```java
@DeleteMapping("/{id}")
public ResponseEntity<UserDto> deleteUser(
@PathVariable Long id
)
{
    var user = userRepository.findById(id).orElse(null);
    if (user == null) {
    return ResponseEntity.notFound().build();
    }
    userRepository.delete(user);
    return ResponseEntity.status(HttpStatus.OK).body(userMapper.toDto(user));
}
```

## ESERCIZIO CON NOTE:
```java
@Mapper(componentModel = "spring")
public interface ProductMapper {
    //il seguente passaggio devo farlo manualmente perchè il mapper non mi mappa il categoryId dato che il campo nell entity si chiama category e non categoryId:
    @Mapping(target = "categoryId", source = "category.id")
    ProductDto toDto(Product product);

    Product toEntity(ProductDto productDto);

    //devo escludere l id perchè altrimenti lui mi mappa l id del productDTo che passo(null) e non va bene perchè quando poi fa la save mi da eccezione se passo da null ad un id
    @Mapping(target = "id", ignore = true)
    void update(ProductDto productDto, @MappingTarget Product product);
}

@PostMapping
public ResponseEntity<ProductDto> createProduct(
        @RequestBody ProductDto productDto
)
{
    var product = productMapper.toEntity(productDto);

    //bigogna vedere se la category esiste altrimenti non ha senso aggiungere un prodotto ad una categoria che non c'è:
    var category = categoryRepository.findById(productDto.getCategoryId()).orElse(null);

    if(category == null) {
        return ResponseEntity.badRequest().build();
    }
    //facciamo la set perchè nel toEntity di prima nell implementazione non viene settata:
    product.setCategory(category);

    productRepository.save(product);
    return ResponseEntity.
            status(HttpStatus.CREATED).
            body(productMapper.toDto(product)); //potevo passargli il DTO della request ma mi sarei trovato l id a null
}

@PutMapping("/{id}")
public ResponseEntity<ProductDto> updateProduct(
        @RequestBody ProductDto productDto,
        @PathVariable Long id
)
{
    var product =  productRepository.findById(id).orElse(null);
    if(product == null) {
        return ResponseEntity.notFound().build();
    }
    var category =  categoryRepository.findById(productDto.getCategoryId()).orElse(null);
    if(category == null) {
        return ResponseEntity.badRequest().build();
    }
    product.setCategory(category);
    productMapper.update(productDto, product);
    productRepository.save(product);
    return ResponseEntity.ok(productMapper.toDto(product));

}
```

---