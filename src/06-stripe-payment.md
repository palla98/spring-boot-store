## Add Stripe payment to project
```java
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>27.1.0</version>
</dependency>
```
# ✅ **CheckoutService — Contesto e metodi**

```java
@RequiredArgsConstructor
@Service
public class CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    private final AuthService authService;
    private final CartService cartService;

    private final PaymentGateway  paymentGateway;


    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        var cart = cartRepository.findById(request.getCartId()).orElse(null);
        if(cart == null) {
           throw new CartNotFoundException();
        }
        if (cart.isEmpty()){
            throw new CartEmptyException();
        }

        var order = Order.fromCart(cart, authService.getCurrentUser());
        orderRepository.save(order);

        //create a checkout session:
        try {
            var session = paymentGateway.createCheckoutSession(order);
            cartService.clearCart(cart.getId());
            return new CheckoutResponse(order.getId(), session.getCheckoutUrl());

        } catch (PaymentException e) {
            orderRepository.delete(order);
        }
        return new CheckoutResponse(order.getId(), null);
    }


    public void handleWebhookEvent(WebhookRequest request){
        paymentGateway
                .parseWebhookEvent(request)
                .ifPresent(payment -> {
                    var order = orderRepository.findById(payment.getOrderId()).orElseThrow();
                    order.setStatus(payment.getPaymentStatus());
                    orderRepository.save(order);
                });
    }

}
```

### **Contesto**

`CheckoutService` è il servizio che gestisce:

* la fase di checkout (creazione ordine + sessione Stripe)
* l’aggiornamento dell’ordine quando arriva un webhook Stripe

Dipende da:

* `CartRepository` → recupero carrelli
* `OrderRepository` → salvataggio ordini
* `AuthService` → ottenere utente loggato
* `CartService` → svuotare carrello
* `PaymentGateway` → integrazione con Stripe tramite StripePaymentGateway

---

## 🔷 **1. checkout(CheckoutRequest request)**

Metodo principale che avvia la procedura di checkout.

### **Contesto**

* viene chiamato dal tuo controller quando l’utente vuole pagare
* crea un ordine, genera la sessione Stripe e svuota il carrello
* è marcato `@Transactional` perché tutte le operazioni devono essere atomiche

### **Funzionamento sintetico**

1. Recupera il carrello tramite `cartRepository.findById(...)`
2. Se il carrello non esiste → `CartNotFoundException`
3. Se il carrello è vuoto → `CartEmptyException`
4. Converte il carrello in un ordine usando `Order.fromCart(...)`
5. Salva l’ordine nel DB
6. Chiama `paymentGateway.createCheckoutSession(order)`
7. Se Stripe risponde correttamente:

    * svuota il carrello (`cartService.clearCart`)
    * ritorna `CheckoutResponse(orderId, checkoutUrl)`
8. Se Stripe fallisce:

    * elimina l’ordine creato
    * ritorna `CheckoutResponse(orderId, null)`

---

## 🔷 **2. handleWebhookEvent(WebhookRequest request)**

Metodo chiamato dal controller dei webhook Stripe.

### **Contesto**

* Stripe invia eventi esterni (pagamento riuscito / fallito)
* questo metodo gestisce l’aggiornamento dell’ordine
* `PaymentGateway` si occupa della validazione firma e estrazione dei dati

### **Funzionamento sintetico**

1. Chiama `paymentGateway.parseWebhookEvent(request)`
2. Se l’evento è riconosciuto (e valido):

    * ottiene `PaymentResult` (contiene id ordine + stato pagamento)
    * recupera l’ordine corrispondente
    * aggiorna `order.setStatus(...)`
    * salva l’ordine aggiornato nel DB

---

# ✅ **StripePaymentGateway — Contesto e metodi**

```java
@NoArgsConstructor
@Service
public class StripePaymentGateway implements PaymentGateway {

    @Value("${websiteUrl}")
    private String websiteUrl;

    @Value("${stripe.webhookSecretKey}")
    private String webhookSecretKey;


    @Override
    public CheckotSession createCheckoutSession(Order order) {
        try {
            var builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(websiteUrl + "/checkout-success?orderId=" + order.getId())
                    .setCancelUrl(websiteUrl + "/checkout-cancel")
                    .putMetadata("order_id", order.getId().toString());

            order.getItems().forEach(item -> {
                var lineItem = createLineItem(item);
                builder.addLineItem(lineItem);
            });

            var session = Session.create(builder.build());
            return new CheckotSession(session.getUrl());
        } catch (StripeException e) {
            System.out.println(e.getMessage());
            throw new PaymentException("invalid");
        }
    }

    @Override
    public Optional<PaymentResult> parseWebhookEvent(WebhookRequest request) {
        try {
            var payload = request.getPayload();
            var signature = request.getHeaders().get("stripe-signature");
            var event = Webhook.constructEvent(payload, signature, webhookSecretKey);

            return switch (event.getType()) {
                case "payment_intent.succeeded" ->
                    Optional.of(new PaymentResult(extractOrdeId(event), PaymentStatus.PAID));

                case "payment_intent.payment_failed" ->
                    Optional.of(new PaymentResult(extractOrdeId(event), PaymentStatus.FAILED));

                default -> Optional.empty();

            };

        } catch (SignatureVerificationException e) {
            throw new PaymentException("invalid Stripe-Signature");
        }
    }



    private Long extractOrdeId(Event event) {
        var stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(
                () -> new PaymentException("Could not deserialize Stripe event.")
        );
        var payment_intent = (PaymentIntent) stripeObject;
        return Long.valueOf(payment_intent.getMetadata().get("order_id"));

    }


    private SessionCreateParams.LineItem createLineItem(OrderItem item) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(Long.valueOf(item.getQuantity()))
                .setPriceData(createPriceData(item))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData createPriceData(OrderItem item) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                .setUnitAmountDecimal(item.getUnitPrice().multiply(BigDecimal.valueOf(100)))
                .setProductData(createProductData(item))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData.ProductData createProductData(OrderItem item) {
        return SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(item.getProduct().getName())
                .build();
    }
}

```

### **Contesto**

È l’implementazione dell’interfaccia `PaymentGateway`.
Si occupa di:

* creare la Checkout Session di Stripe
* interpretare i webhook e restituire `PaymentResult`

Usa:

* la tua `stripe.webhookSecretKey` per verificare la firma
* `websiteUrl` per costruire le URL di redirect

---

## 🔷 **1. createCheckoutSession(Order order)**

Crea la Stripe Checkout Session.

### **Contesto**

* viene chiamato da `CheckoutService.checkout()`
* genera l’URL di pagamento Stripe da mostrare all’utente

### **Funzionamento sintetico**

1. Costruisce un builder `SessionCreateParams`
2. Imposta success/cancel URL
3. Inserisce `order_id` nei metadata
4. Aggiunge tutti gli articoli dell’ordine come line item
5. Chiama `Session.create(params)` per creare realmente la sessione
6. Ritorna un oggetto con l’URL della pagina Stripe
7. In caso di errore Stripe → `PaymentException`

---

## 🔷 **2. parseWebhookEvent(WebhookRequest request)**

Legge l’evento Stripe e rende un risultato interpretabile dal sistema.

### **Contesto**

* viene chiamato da `CheckoutService.handleWebhookEvent`
* si occupa della sicurezza: verifica firma, deserializza, interpreta

### **Funzionamento sintetico**

1. Estrarre payload e intestazione `stripe-signature`
2. Verificare la firma con `Webhook.constructEvent(...)`
3. Interpretare `event.getType()`:

    * `payment_intent.succeeded` → pagamento riuscito
    * `payment_intent.payment_failed` → pagamento fallito
4. Estrarre l’`order_id` dai metadata tramite `extractOrdeId(event)`
5. Restituire `Optional<PaymentResult>`
6. In caso di errore firma → eccezione `PaymentException`

---

## 🔷 **3. extractOrdeId(Event event)**

Estrae l’id dell’ordine dai metadata del PaymentIntent.

### **Contesto**

* Stripe ti rimanda l’ID ordine solo se lo hai salvato nei metadata
* viene usato dentro `parseWebhookEvent`

### **Funzionamento sintetico**

1. Deserializza l’oggetto evento
2. Lo converte in `PaymentIntent`
3. Legge `paymentIntent.metadata.get("order_id")`
4. Lo converte in `Long`

---

## 🔷 **4. createLineItem, createPriceData, createProductData**

### **Contesto**

Metodi di supporto chiamati dentro `createCheckoutSession`.

### **Funzionamento sintetico**

* `createLineItem` → crea un LineItem di Stripe
* `createPriceData` → imposta prezzo e valuta
* `createProductData` → imposta nome prodotto

---
