# 📘 Complete Guide to HTTP Headers

## Table of Contents
1. [What are HTTP Headers?](#what-are-http-headers)
2. [How Headers Work](#how-headers-work)
3. [Common Header Types](#common-header-types)
4. [Headers in Spring Boot](#headers-in-spring-boot)
5. [Khalti Payment Headers](#khalti-payment-headers)
6. [Practical Examples](#practical-examples)

---

## What are HTTP Headers?

HTTP headers are **key-value pairs** sent between client and server in HTTP requests and responses. They provide **metadata** about the request/response.

Think of headers like an envelope's information:
- **Body** = The actual letter (your data/payload)
- **Headers** = Sender address, recipient, postage info, priority label (metadata)

### Basic Structure
```
Header-Name: Header-Value
```

Example:
```
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Accept: application/json
```

---

## How Headers Work

### Request Flow
```
Client (Browser/App)  ──────────>  Server
         │                              │
         │  GET /api/users              │
         │  Headers:                    │
         │    Authorization: Bearer ... │
         │    Content-Type: app/json    │
         │    Accept: application/json  │
         │                              │
         └──────────────────────────────┘
```

### Response Flow
```
Server  ──────────>  Client
    │                   │
    │  Status: 200 OK   │
    │  Headers:         │
    │    Content-Type   │
    │    Set-Cookie     │
    │    Cache-Control  │
    │  Body: {...}      │
    └───────────────────┘
```

---

## Common Header Types

### 1. Authentication Headers

#### Authorization
Used to send credentials to the server.

**Formats:**
```http
# Bearer Token (JWT)
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Basic Auth (username:password in Base64)
Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=

# API Key (Custom format - like Khalti)
Authorization: Key live_secret_key_68791341fdd94846a146f0457ff7b455

# Custom Token
Authorization: Token abc123xyz
```

**When to use:**
- JWT tokens for user authentication
- API keys for third-party services (Khalti, Stripe, etc.)
- Basic auth for simple authentication

---

### 2. Content Headers

#### Content-Type
Tells the server what type of data you're sending.

```http
# JSON data
Content-Type: application/json

# Form data
Content-Type: application/x-www-form-urlencoded

# File upload
Content-Type: multipart/form-data

# XML
Content-Type: application/xml

# Plain text
Content-Type: text/plain

# HTML
Content-Type: text/html
```

#### Accept
Tells the server what type of response you want.

```http
# I want JSON response
Accept: application/json

# I want XML response
Accept: application/xml

# I accept multiple formats (preference order)
Accept: application/json, application/xml, */*
```

#### Content-Length
Size of the request/response body in bytes.

```http
Content-Length: 348
```

---

### 3. Cookie Headers

#### Cookie (Request)
Sends cookies from client to server.

```http
Cookie: sessionId=abc123; userId=42; theme=dark
```

#### Set-Cookie (Response)
Server tells client to store a cookie.

```http
Set-Cookie: accessToken=xyz789; HttpOnly; Secure; SameSite=Strict; Max-Age=3600
```

**Cookie Attributes:**
- `HttpOnly` - JavaScript cannot access (prevents XSS attacks)
- `Secure` - Only sent over HTTPS
- `SameSite` - CSRF protection (Strict/Lax/None)
- `Max-Age` - Expiration time in seconds
- `Path` - URL path where cookie is valid
- `Domain` - Domain where cookie is valid

---

### 4. CORS Headers

#### Access-Control-Allow-Origin
Specifies which origins can access the resource.

```http
# Allow specific origin
Access-Control-Allow-Origin: http://localhost:3000

# Allow all origins (not recommended for production)
Access-Control-Allow-Origin: *
```

#### Access-Control-Allow-Methods
Allowed HTTP methods.

```http
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
```

#### Access-Control-Allow-Headers
Allowed request headers.

```http
Access-Control-Allow-Headers: Content-Type, Authorization
```

#### Access-Control-Allow-Credentials
Allow cookies/credentials in cross-origin requests.

```http
Access-Control-Allow-Credentials: true
```

---

### 5. Caching Headers

#### Cache-Control
Controls caching behavior.

```http
# Don't cache
Cache-Control: no-cache, no-store, must-revalidate

# Cache for 1 hour
Cache-Control: max-age=3600

# Cache but revalidate
Cache-Control: public, max-age=3600, must-revalidate
```

#### ETag
Resource version identifier for cache validation.

```http
ETag: "33a64df551425fcc55e4d42a148795d9f25f89d4"
```

---

### 6. Custom Headers

You can create your own headers (usually prefixed with `X-`).

```http
X-Request-ID: 550e8400-e29b-41d4-a716-446655440000
X-API-Version: v2
X-Rate-Limit-Remaining: 99
X-Correlation-ID: abc-123-def
```

---

## Headers in Spring Boot

### 1. Reading Headers in Controller

```java
@RestController
@RequestMapping("/api")
public class ExampleController {

    // Method 1: Using @RequestHeader
    @GetMapping("/user")
    public ResponseEntity<String> getUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        
        System.out.println("Auth: " + authHeader);
        System.out.println("User-Agent: " + userAgent);
        return ResponseEntity.ok("Success");
    }

    // Method 2: Get all headers
    @GetMapping("/headers")
    public ResponseEntity<Map<String, String>> getAllHeaders(
            @RequestHeader Map<String, String> headers) {
        
        headers.forEach((key, value) -> {
            System.out.println(key + " = " + value);
        });
        return ResponseEntity.ok(headers);
    }

    // Method 3: Using HttpServletRequest
    @GetMapping("/request")
    public ResponseEntity<String> getFromRequest(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        String contentType = request.getHeader("Content-Type");
        
        // Get all header names
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            System.out.println(headerName + ": " + headerValue);
        }
        
        return ResponseEntity.ok("Success");
    }
}
```

### 2. Setting Headers in Response

```java
@RestController
public class ResponseHeaderController {

    // Method 1: Using ResponseEntity
    @GetMapping("/data")
    public ResponseEntity<String> getData() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Custom-Header", "MyValue");
        headers.add("Cache-Control", "no-cache");
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        return new ResponseEntity<>("Response body", headers, HttpStatus.OK);
    }

    // Method 2: Using HttpServletResponse
    @GetMapping("/download")
    public void downloadFile(HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=file.pdf");
        response.setHeader("Content-Type", "application/pdf");
        response.setHeader("Cache-Control", "no-cache");
        // ... write file content
    }

    // Method 3: Using @ResponseHeader (for all methods in controller)
    @GetMapping("/api-version")
    @ResponseStatus(HttpStatus.OK)
    public String getVersion(HttpServletResponse response) {
        response.setHeader("X-API-Version", "v1.0");
        return "Version 1.0";
    }
}
```

### 3. Using HttpHeaders Class

```java
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HeaderExample {
    
    public HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        // Set Content-Type
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Set Authorization
        headers.set("Authorization", "Bearer " + token);
        headers.setBearerAuth(token); // Shortcut for Bearer token
        
        // Set Basic Auth
        headers.setBasicAuth("username", "password");
        
        // Set Accept
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        
        // Set custom header
        headers.set("X-Request-ID", UUID.randomUUID().toString());
        
        // Add multiple values
        headers.add("X-Custom", "value1");
        headers.add("X-Custom", "value2");
        
        // Set cookie
        headers.add("Cookie", "sessionId=abc123");
        
        return headers;
    }
}
```

---

## Khalti Payment Headers

### Understanding Khalti API Headers

Khalti uses specific headers for authentication and content negotiation.

#### Required Headers for Khalti API

```http
Authorization: Key live_secret_key_68791341fdd94846a146f0457ff7b455
Content-Type: application/json
```

### Why These Headers?

1. **Authorization: Key [secret_key]**
   - Khalti uses custom "Key" scheme (not Bearer)
   - Authenticates your application
   - Secret key proves you're authorized to make payments
   - Format: `Key <your-secret-key>`

2. **Content-Type: application/json**
   - Tells Khalti you're sending JSON data
   - Khalti API expects JSON format
   - Response will also be JSON

### Khalti Config Helper Method

```java
@Configuration
@ConfigurationProperties(prefix = "khalti")
public class KhaltiConfig {
    
    private String baseUrl;
    private String publicKey;
    private String secretKey;
    private String returnUrl;
    private String websiteUrl;
    
    // Getters and setters...
    
    /**
     * Creates HttpHeaders with Khalti authentication
     * Use this for all Khalti API calls
     */
    public HttpHeaders getKhaltiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        // Khalti uses "Key" prefix, not "Bearer"
        headers.set("Authorization", "Key " + secretKey);
        
        // Khalti expects JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Optional: Add request tracking
        headers.set("X-Request-ID", UUID.randomUUID().toString());
        
        return headers;
    }
    
    /**
     * For public key operations (frontend)
     */
    public HttpHeaders getPublicHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + publicKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
    
    @Bean
    public WebClient khaltiWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                // Don't set Authorization here - add per request
                .build();
    }
}
```

---

## Practical Examples

### Example 1: Making Khalti Payment Request

```java
@Service
@RequiredArgsConstructor
public class KhaltiPaymentService {
    
    private final WebClient khaltiWebClient;
    private final KhaltiConfig khaltiConfig;
    
    public Mono<KhaltiInitiateResponse> initiatePayment(PaymentRequest request) {
        
        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("return_url", khaltiConfig.getReturnUrl());
        requestBody.put("website_url", khaltiConfig.getWebsiteUrl());
        requestBody.put("amount", request.getAmount() * 100); // Convert to paisa
        requestBody.put("purchase_order_id", request.getOrderId());
        requestBody.put("purchase_order_name", request.getProductName());
        
        // Make API call with headers
        return khaltiWebClient.post()
                .uri(khaltiConfig.getInitiatePath())
                .headers(headers -> {
                    // Add Khalti authentication
                    headers.set("Authorization", "Key " + khaltiConfig.getSecretKey());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(KhaltiInitiateResponse.class);
    }
    
    // Alternative: Using HttpHeaders
    public Mono<KhaltiInitiateResponse> initiatePaymentV2(PaymentRequest request) {
        
        Map<String, Object> requestBody = createRequestBody(request);
        
        return khaltiWebClient.post()
                .uri(khaltiConfig.getInitiatePath())
                .headers(httpHeaders -> 
                    httpHeaders.addAll(khaltiConfig.getKhaltiHeaders()))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(KhaltiInitiateResponse.class);
    }
}
```

### Example 2: JWT Authentication with Headers

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // 1. Extract Authorization header
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 2. Extract token
        String jwt = authHeader.substring(7); // Remove "Bearer " prefix
        
        // 3. Validate and set authentication
        // ... validation logic
        
        filterChain.doFilter(request, response);
    }
}
```

### Example 3: File Upload with Headers

```java
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authToken,
            @RequestHeader(value = "X-Upload-Context", required = false) String context) {
        
        // Check Content-Type
        String contentType = file.getContentType();
        if (!contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body("Only images allowed");
        }
        
        // Process file...
        
        // Return response with custom headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-File-ID", UUID.randomUUID().toString());
        headers.add("X-Upload-Status", "SUCCESS");
        
        return new ResponseEntity<>("File uploaded", headers, HttpStatus.OK);
    }
}
```

### Example 4: RestTemplate with Headers

```java
@Service
public class ExternalApiService {
    
    private final RestTemplate restTemplate;
    
    public String callExternalApi() {
        String url = "https://api.example.com/data";
        
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", "your-api-key");
        
        // Create request entity
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // Make request
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );
        
        // Read response headers
        HttpHeaders responseHeaders = response.getHeaders();
        String rateLimit = responseHeaders.getFirst("X-Rate-Limit-Remaining");
        
        return response.getBody();
    }
}
```

### Example 5: WebClient with Headers (Reactive)

```java
@Service
@RequiredArgsConstructor
public class ReactiveApiService {
    
    private final WebClient webClient;
    
    public Mono<ApiResponse> callApi(String endpoint, String token) {
        return webClient.get()
                .uri(endpoint)
                .headers(headers -> {
                    headers.setBearerAuth(token);
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("X-Request-ID", UUID.randomUUID().toString());
                    headers.set("User-Agent", "MyApp/1.0");
                })
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> {
                    // Handle 4xx errors
                    return Mono.error(new RuntimeException("Client error"));
                })
                .onStatus(HttpStatus::is5xxServerError, response -> {
                    // Handle 5xx errors
                    return Mono.error(new RuntimeException("Server error"));
                })
                .bodyToMono(ApiResponse.class)
                .doOnSuccess(response -> {
                    // Log success
                    System.out.println("API call successful");
                });
    }
}
```

---

## Common Header Scenarios

### Scenario 1: API Rate Limiting

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        
        String clientId = request.getHeader("X-Client-ID");
        
        // Check rate limit...
        int remaining = getRemainingRequests(clientId);
        
        // Add rate limit headers to response
        response.setHeader("X-Rate-Limit-Limit", "100");
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(remaining));
        response.setHeader("X-Rate-Limit-Reset", "1640000000");
        
        if (remaining <= 0) {
            response.setStatus(429); // Too Many Requests
            return false;
        }
        
        return true;
    }
}
```

### Scenario 2: Request Correlation

```java
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Get or generate correlation ID
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        
        // Add to response
        response.setHeader("X-Correlation-ID", correlationId);
        
        // Add to MDC for logging
        MDC.put("correlationId", correlationId);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
```

### Scenario 3: Content Negotiation

```java
@RestController
public class ContentNegotiationController {
    
    @GetMapping("/data")
    public ResponseEntity<?> getData(@RequestHeader("Accept") String acceptHeader) {
        
        if (acceptHeader.contains("application/json")) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("format", "json", "data", "..."));
        } 
        else if (acceptHeader.contains("application/xml")) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body("<data><format>xml</format></data>");
        }
        else {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Plain text data");
        }
    }
}
```

---

## Security Best Practices

### 1. Never Log Sensitive Headers
```java
// ❌ BAD
log.info("Authorization: " + request.getHeader("Authorization"));

// ✅ GOOD
log.info("Authorization: [REDACTED]");
```

### 2. Validate Headers
```java
String authHeader = request.getHeader("Authorization");
if (authHeader == null || authHeader.isEmpty()) {
    throw new UnauthorizedException("Missing Authorization header");
}

if (!authHeader.startsWith("Bearer ")) {
    throw new UnauthorizedException("Invalid Authorization format");
}
```

### 3. Use HTTPS for Sensitive Headers
```properties
# Force HTTPS in production
server.ssl.enabled=true
security.require-ssl=true
```

### 4. Set Security Headers
```java
@Configuration
public class SecurityHeadersConfig {
    
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SecurityHeadersFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}

public class SecurityHeadersFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");
        
        // Prevent MIME sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // Enable XSS protection
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Content Security Policy
        response.setHeader("Content-Security-Policy", "default-src 'self'");
        
        // HSTS (HTTPS only)
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        
        filterChain.doFilter(request, response);
    }
}
```

---

## Debugging Headers

### Using Postman
```
1. Open Postman
2. Create new request
3. Go to "Headers" tab
4. Add headers:
   - Key: Authorization
   - Value: Bearer your-token-here
5. Send request
6. Check response headers in "Headers" tab
```

### Using cURL
```bash
# Send request with headers
curl -X POST https://api.example.com/data \
  -H "Authorization: Bearer token123" \
  -H "Content-Type: application/json" \
  -H "X-Custom-Header: value" \
  -d '{"key":"value"}'

# View response headers
curl -i https://api.example.com/data

# View only headers
curl -I https://api.example.com/data
```

### Using Browser DevTools
```
1. Open DevTools (F12)
2. Go to "Network" tab
3. Make a request
4. Click on the request
5. View "Headers" section:
   - Request Headers (what you sent)
   - Response Headers (what server sent back)
```

### Logging Headers in Spring Boot
```java
@Component
@Slf4j
public class HeaderLoggingFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        
        log.info("=== Request Headers ===");
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            String headerValue = request.getHeader(headerName);
            // Redact sensitive headers
            if (headerName.equalsIgnoreCase("Authorization")) {
                headerValue = "[REDACTED]";
            }
            log.info("{}: {}", headerName, headerValue);
        });
        
        filterChain.doFilter(request, response);
    }
}
```

---

## Summary

### Key Takeaways

1. **Headers are metadata** - They describe the request/response, not the actual data
2. **Authorization header** - Used for authentication (Bearer, Basic, API Key)
3. **Content-Type** - Tells what format you're sending (JSON, XML, form data)
4. **Accept** - Tells what format you want back
5. **Custom headers** - Start with `X-` for your own headers
6. **Security** - Never log sensitive headers, use HTTPS, validate inputs
7. **Khalti uses** - `Authorization: Key <secret>` format (not Bearer)

### Common Mistakes to Avoid

❌ Forgetting to set Content-Type
❌ Using wrong Authorization format (Bearer vs Key)
❌ Logging sensitive header values
❌ Not validating header values
❌ Hardcoding tokens in code (use environment variables)
❌ Not handling missing headers gracefully

### Quick Reference

```java
// Read header
String value = request.getHeader("Header-Name");

// Set header in response
response.setHeader("Header-Name", "value");

// Spring HttpHeaders
HttpHeaders headers = new HttpHeaders();
headers.set("Header-Name", "value");
headers.setBearerAuth(token);
headers.setContentType(MediaType.APPLICATION_JSON);

// WebClient with headers
webClient.get()
    .uri("/endpoint")
    .headers(h -> h.setBearerAuth(token))
    .retrieve()
    .bodyToMono(Response.class);
```

---

**Happy coding! 🚀**
