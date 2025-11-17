deploy project on website

1) andare su railway che un host service, fare new project dopo aver collegato l account github e selezionare il dbms che si è utilizzato (mysql) nel mio caso
2) creare diversi yaml per i diversi ambienti, dev, prod... lasciare quindi nello yaml principale le conf in comune:
   spring:
   application:
   name: store
   jwt:
   secret: ${JWT_SECRET}
   accessTokenExpiration: 900 # 15 min
   refreshTokenExpiration: 604800 # 7 day
   profiles:
   active: dev
   stripe:
   secretKey: ${STRIPE_SECRET_KEY}
   webhookSecretKey: ${STRIPE_WEBHOOK_SECRET_KEY}
e mentre nei diversi profili come prod le cose specifiche:
   spring:
   datasource:
   url: ${SPRING_DATASOURCE_URL}
   websiteUrl: https://mystore.com

3) poi nel pom:
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

# ✅ Perché devi aggiungere `annotationProcessorPaths` nel `maven-compiler-plugin`

Quando usi **Lombok** e **MapStruct**, entrambi funzionano tramite *annotation processing*.

Questo significa che, durante la compilazione:

* **Lombok** genera getter, setter, builder, costruttori, ecc.
* **MapStruct** genera automaticamente gli implementation dei mapper (es: `MyMapperImpl`).

👉 **Maven però, di default, NON include gli annotation processor nel classpath del compilatore**, a meno che tu non glielo dica esplicitamente.

### Senza quella config succede che:

* Lombok non viene eseguito → errori tipo *cannot find symbol getX()*
* MapStruct non genera gli implementation → errori tipo *No implementation was found for Mapper*

⚠️ In locale magari funziona perché l’IDE (IntelliJ) ha un suo annotation processor integrato —
ma in un ambiente CI/CD (GitHub Actions, GitLab Runner, ecc.) **non c'è IntelliJ**, c’è solo Maven.

Per questo serve configurare il plugin.

4) lanciare poi un mvn clean package per fare il clean e creare il jar del progetto
5) creare una repo su github e pushare il codice
andare su github e fare new repo dopodichè da terminale del progetto:
   git remote add origin https://github.com/palla98/spring-boot-store.git
   git branch -M main
   git push -u origin main

