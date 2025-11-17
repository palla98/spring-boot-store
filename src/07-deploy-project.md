## **1️⃣ Creazione del progetto su Railway**

1. Vai su [Railway Dashboard](https://railway.app/dashboard).
2. Clicca **New Project** → collega il tuo account GitHub.
3. Seleziona il **DBMS** utilizzato (nel tuo caso **MySQL**).

---

## **2️⃣ Configurazione YAML per diversi ambienti**

* Crea diversi file YAML per i vari profili (`dev`, `prod`, ecc.).
* Lascia nello YAML principale le configurazioni comuni:

```yaml
spring:
  application:
    name: store
  jwt:
    secret: ${JWT_SECRET}
    accessTokenExpiration: 900        # 15 minuti
    refreshTokenExpiration: 604800    # 7 giorni
  profiles:
    active: dev
stripe:
  secretKey: ${STRIPE_SECRET_KEY}
  webhookSecretKey: ${STRIPE_WEBHOOK_SECRET_KEY}
```

* Nei file dei singoli profili (es. `application-prod.yaml`) metti le configurazioni specifiche:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
  websiteUrl: https://mystore.com
```

---

## **3️⃣ Configurazione Maven per Lombok e MapStruct**

Nel `pom.xml` aggiungi l’`annotationProcessorPaths` nel `maven-compiler-plugin`:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <annotationProcessorPaths>
          <path>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
          </path>
          <path>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <version>1.6.3</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
```

**Perché serve:**

* Lombok e MapStruct generano codice tramite *annotation processing*.
* Maven, di default, non include questi processor nel classpath → senza questa config in CI/CD fallisce la compilazione.
* In locale IntelliJ funziona, ma nel deploy (Railway, GitHub Actions, ecc.) serve la configurazione esplicita.

---

## **4️⃣ Build del progetto**

Esegui:

```bash
mvn clean package
```

* Questo pulisce il progetto e genera il `.jar` pronto per il deploy.

---

## **5️⃣ Creazione della repo GitHub**

1. Vai su GitHub → **New Repository**.
2. Dal terminale del progetto:

```bash
git remote add origin https://github.com/palla98/spring-boot-store.git
git branch -M main
git push -u origin main
```

* Ti chiederà **username** (il tuo GitHub) e **token personale**.
* Il token si genera su: [GitHub Personal Access Tokens](https://github.com/settings/personal-access-tokens/9752581) con permessi:

    * Read & Write su: code, pull requests, repository advisories, workflows.

---

## **6️⃣ Deploy su Railway**

1. Collega l’account GitHub a Railway.
2. Avvia il deploy e genera il dominio (porta `8080`).

---

## **7️⃣ Variabili di ambiente su Railway**

* Vai nel service → **Variables** e aggiungi:

| Variabile                 | Valore                         |
| ------------------------- | ------------------------------ |
| JWT_SECRET                | (il tuo secret JWT)            |
| STRIPE_SECRET_KEY         | (il tuo Stripe secret key)     |
| STRIPE_WEBHOOK_SECRET_KEY | (per ora puoi lasciarla vuota) |
| SPRING_DATASOURCE_URL     | `jdbc:${{MySQL.MYSQL_URL}}`    |
| SPRING_PROFILES_ACTIVE    | `prod`                         |

> Dopo averle aggiunte, rebuild del progetto.

---

## **8️⃣ Test con Postman**

* Configura **due environment**: `dev` e `prod`.

| Environment | Base URL                                                   |
| ----------- | ---------------------------------------------------------- |
| dev         | `http://localhost:8080`                                    |
| prod        | `https://spring-boot-store-production-74be.up.railway.app` |

* Testa tutte le API nei due ambienti.

---

