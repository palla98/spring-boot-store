## 🚀**Validazione delle API Request in Spring Boot**

### 1️⃣ Dipendenza Maven

Aggiungi la dipendenza per **Jakarta Validation** (inclusa automaticamente in Spring Boot Starter Web, ma buona prassi esplicitarla):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

### 2️⃣ DTO di richiesta con annotazioni di validazione

```java
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequest {

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must be less than 255 characters")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 25, message = "password must be between 6 and 25 characters")
    private String password;
}
```

---

### 3️⃣ Applicazione della validazione nel Controller

Nel controller basta aggiungere `@Valid` davanti a `@RequestBody`.
Spring intercetterà automaticamente eventuali errori di validazione.

```java
@PostMapping
public ResponseEntity<UserDto> createUser(@Valid @RequestBody RegisterUserRequest request) {
    // Logica di creazione utente
    return ResponseEntity.ok(/* userDto */);
}
```

---

### 4️⃣ Gestione globale degli errori di validazione

Crea una classe dedicata per **intercettare le eccezioni di validazione** e restituire messaggi chiari al client.

```java
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }
}
```

💡 Ora **in qualunque Controller** ci sia `@Valid`, questa classe interverrà automaticamente e restituirà risposte del tipo:

```json
{
  "email": "email must be valid",
  "password": "password must be between 6 and 25 characters"
}
```

---

## 🧩 **Custom Validation Annotation**

### Esempio: vincolo per forzare il lowercase su un campo `email`

#### 🔸 Validator

```java
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LowerCaseValidator implements ConstraintValidator<LowerCase, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // i campi null vengono gestiti da @NotBlank
        return value.equals(value.toLowerCase());
    }
}
```

#### 🔸 Annotation personalizzata

```java
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.FIELD) // può essere applicata ai campi
@Retention(RetentionPolicy.RUNTIME) // attiva a runtime
@Constraint(validatedBy = LowerCaseValidator.class)
public @interface LowerCase {
    String message() default "must be lowercase";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

#### 🔸 Uso nel DTO

```java
@LowerCase(message = "email must be in lowercase")
private String email;
```

---

## ⚙️ **Business Rule Validation**

Per regole di business (come “email già presente”), si gestisce **dopo** la validazione sintattica:

```java
@PostMapping
public ResponseEntity<?> createUser(@Valid @RequestBody RegisterUserRequest request) {

    if (userRepository.existsByEmail(request.getEmail())) {
        return ResponseEntity.badRequest().body(
                Map.of("email", "already registered")
        );
    }

    // Se tutto ok, salva l'utente
    var savedUser = userService.createUser(request);
    return ResponseEntity.ok(savedUser);
}
```

---

### ✅ **Riassunto finale**

| Livello             | Tipo di validazione                              | Dove avviene                    |
| ------------------- | ------------------------------------------------ | ------------------------------- |
| 1️⃣ Sintattica      | `@NotBlank`, `@Email`, `@Size`, `@LowerCase`     | Automaticamente via `@Valid`    |
| 2️⃣ Logica/Business | “Email già presente”, “Username duplicato”, ecc. | Dentro al controller o service  |
| 3️⃣ Gestione Errori | `@ControllerAdvice` + `@ExceptionHandler`        | Globale, per tutti i controller |

---

Certo ✅ — ecco la tua spiegazione riscritta in modo **chiaro, ordinato e professionale**, perfetta per appunti o documentazione:

---

## 🧩 **Swagger / OpenAPI con Spring Boot**

### 1️⃣ Cos’è

**Swagger** è un tool che genera automaticamente la **documentazione interattiva** per le tue **REST API**.
Permette di visualizzare e testare gli endpoint direttamente da un’interfaccia web (Swagger UI).

---

### 2️⃣ Integrazione con Spring Boot

Per integrare **Swagger UI** nel tuo progetto Spring Boot, aggiungi la seguente dipendenza nel `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.14</version>
</dependency>
```

✅ Nessuna configurazione aggiuntiva è necessaria: Spring rileverà automaticamente i tuoi controller e genererà la documentazione.

---

### 3️⃣ Accesso alla documentazione

Dopo aver avviato l’applicazione, saranno disponibili:

* **Interfaccia Swagger UI (HTML):**
  👉 `http://localhost:8080/swagger-ui.html`

* **Descrizione OpenAPI (JSON):**
  👉 `http://localhost:8080/v3/api-docs`

*(Se l’app ha un context-path, aggiungilo all’URL, es. `/api/swagger-ui.html`)*

---

### 4️⃣ Annotazioni utili

#### 🔹 @Tag

Serve per **raggruppare gli endpoint** di un controller sotto un nome leggibile.

```java
@Tag(name = "Carts")
@RestController
@RequestMapping("/api/carts")
public class CartController {
    ...
}
```

👉 Tutti gli endpoint di questo controller appariranno nel gruppo **“Carts”** invece del generico “CartController”.

---

#### 🔹 @Operation

Serve per **descrivere il singolo endpoint** (metodo HTTP).

```java
@Operation(summary = "Adds a product to the cart")
@PostMapping("/{cartId}/items")
public ResponseEntity<CartItemDto> addToCart(
        @Parameter(description = "The ID of the cart") @PathVariable UUID cartId,
        @RequestBody AddItemToCartRequest request
) {
    ...
}
```

#### 🔹 @Parameter

Serve per aggiungere **una descrizione ai parametri** del metodo (PathVariable, RequestParam, ecc.).

---