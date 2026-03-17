# AES-GCM Encryption Implementation

## Overview
Implemented AES-GCM encryption for sensitive data in PayoutMethod entity with proper exception handling.

## Issues Fixed in AesGcmUtil

### 1. Critical Bug (Line 28)
**Before:**
```java
System.arraycopy(iv, 0, iv, 0, IV_LENGTH_BYTES); // Copying to itself!
```
**After:**
```java
System.arraycopy(iv, 0, combined, 0, IV_LENGTH_BYTES); // Correct
```

### 2. TAG_LENGTH_BITS Correction
**Before:** `16` (incorrect - this is bytes, not bits)
**After:** `128` (correct - 128 bits = 16 bytes)

### 3. Added Input Validation
- Null/empty checks for plaintext and encrypted data
- Null checks for encryption key
- Format validation for encrypted data

## Exception Handling

### Approach: Unchecked Exceptions in Utility
Instead of forcing callers to handle checked exceptions, `AesGcmUtil` handles exceptions internally and throws unchecked `EncryptionException`.

**Benefits:**
- ✅ No try-catch needed in converter (cleaner code)
- ✅ Exceptions propagate automatically to GlobalExceptionHandler
- ✅ Consistent error handling across the app

### AesGcmUtil
```java
public static String encrypt(String plainText, SecretKey key) {
    // No "throws Exception" - handles internally
    try {
        // encryption logic
    } catch (Exception e) {
        throw new EncryptionException("Encryption failed", e);
    }
}
```

### AccountNumberConverter (No try-catch needed!)
```java
@Override
public String convertToDatabaseColumn(String attribute) {
    return AesGcmUtil.encrypt(attribute, encryptionKey);
    // EncryptionException propagates automatically
}
```

### Custom Exception
`EncryptionException` extends `BaseException` for consistent error handling:
```java
public class EncryptionException extends BaseException {
    public EncryptionException(String message, Throwable cause) {
        super(ErrorCode.ENCRYPTION_ERROR, message);
        initCause(cause);
    }
}
```

### Error Code
Added to `ErrorCode` enum:
```java
ENCRYPTION_ERROR(500, "ENCRYPTION_ERROR", "Failed to encrypt/decrypt sensitive data.")
```

### Global Handler
Added to `GlobalExceptionHandler`:
```java
@ExceptionHandler(EncryptionException.class)
public ResponseEntity<GlobalApiResponse<ErrorResponse>> handleEncryptionException(EncryptionException ex) {
    // Returns 500 with generic message (doesn't leak sensitive details)
    // Logs full error details for debugging
}
```

**Security Note:** User sees generic "Data security error occurred" message, but full details are logged for admins.

## Implementation on PayoutMethod

### Files Created/Modified
1. `EncryptionConfig.java` - Manages encryption key
2. `AccountNumberConverter.java` - JPA converter with proper exception handling
3. `EncryptionException.java` - Updated to extend BaseException
4. `ErrorCode.java` - Added ENCRYPTION_ERROR
5. `GlobalExceptionHandler.java` - Added encryption exception handler

### How It Works

```java
@Entity
public class PayoutMethod {
    @Convert(converter = AccountNumberConverter.class)
    private String accountNumber; // Automatically encrypted in DB
}
```

When you save: `accountNumber` → encrypted → stored in database
When you read: encrypted data → decrypted → `accountNumber`

## Configuration

Add to `application.properties`:
```properties
# Generate a key using the app on first run, then add it here
encryption.secret-key=YOUR_BASE64_ENCODED_KEY_HERE
```

**First Run:** App will generate a key and log it. Copy to properties file.

## Security Notes
- Account numbers are encrypted at rest in database
- Uses AES-256-GCM (authenticated encryption)
- Random IV for each encryption (prevents pattern analysis)
- Key stored in application.properties (use environment variables in production)

## Testing
```java
PayoutMethod payout = PayoutMethod.builder()
    .accountNumber("1234567890") // Plain text
    .build();
payoutMethodRepository.save(payout); // Encrypted in DB

PayoutMethod retrieved = payoutMethodRepository.findById(id);
// retrieved.getAccountNumber() returns "1234567890" (decrypted)
```
