# 🚀 Khalti Payment Integration - MVP Roadmap

## 📊 Current Status Evaluation

### ✅ What You Have (Good Foundation):

1. **KhaltiConfig** - Basic configuration ✅
2. **DTOs** - InitiatePaymentRequest/Response, VerifyRequest/Response ✅
3. **Entities** - Donation, PaymentTransaction ✅
4. **Basic Flow** - donateInitiate, fakePayment ✅

### ❌ What Needs Fixing:

1. **KhaltiConfig** - Missing space in Authorization header
2. **DTOs** - Missing required Khalti fields
3. **No KhaltiService** - Need service to call Khalti API
4. **No callback endpoint** - Need to handle Khalti redirect
5. **donateInitiate** - Doesn't return donation ID

---

## 🎯 MVP Implementation Plan

### Total Time: ~4-6 hours (broken into small tasks)

---

## 📝 Task Breakdown

### ✅ TASK 1: Fix KhaltiConfig (10 minutes)

**Issue:** Missing space in Authorization header
```java
.defaultHeader("Authorization", "Key"+secretKey)  // ❌ Missing space
```

**Fix:**
```java
.defaultHeader("Authorization", "Key " + secretKey)  // ✅ Add space
```

**File:** `KhaltiConfig.java`

**Test:** Run application, check logs for errors

---

### ✅ TASK 2: Fix Khalti DTOs (15 minutes)

Khalti requires specific fields. Your DTOs are incomplete.

#### 2a. Fix InitiatePaymentRequest

**Current:**
```java
private String return_url;
private String note;
```

**Should be:**
```java
private String return_url;
private String website_url;
private Long amount;  // in paisa (100 NPR = 10000 paisa)
private String purchase_order_id;  // Your donation ID
private String purchase_order_name;  // e.g., "Donation to John"
```

#### 2b. Fix InitiatePaymentResponse

**Current:**
```java
private String paymentUrl;
private String pidx;
```

**Should be:**
```java
private String pidx;
private String payment_url;
private Long expires_at;
private Long expires_in;
```

#### 2c. Fix verifyPaymentResponse

**Current:**
```java
private String orderId;
private PaymentStatus status;
private BigDecimal amount;
private String message;
```

**Should be:**
```java
private String pidx;
private Long total_amount;
private String status;  // "Completed", "Pending", "Refunded"
private String transaction_id;
private BigDecimal fee;
private Boolean refunded;
```

**Files to update:**
- `InitiatePaymentRequest.java`
- `InitiatePaymentResponse.java`
- `verifyPaymentResponse.java`

---

### ✅ TASK 3: Create KhaltiService (30 minutes)

Create a service to interact with Khalti API.

**File:** `src/main/java/org/blogapp/dg_blogapp/payment/service/KhaltiService.java`

**What it does:**
1. Call Khalti initiate API
2. Call Khalti verify API
3. Handle errors

**Methods needed:**
- `initiatePayment(donationId, amount, customerName)`
- `verifyPayment(pidx)`

---

### ✅ TASK 4: Update donateInitiate to integrate Khalti (20 minutes)

**Current:** Creates donation, returns void
**New:** Creates donation, calls Khalti, returns payment URL

**Changes:**
1. Return `InitiatePaymentResponse` instead of `void`
2. Call `khaltiService.initiatePayment()`
3. Store `pidx` in PaymentTransaction

---

### ✅ TASK 5: Create Khalti Callback Endpoint (30 minutes)

When user completes payment on Khalti, they're redirected back to your site.

**Endpoint:** `GET /payment/khalti/callback?pidx=xxx&status=Completed`

**What it does:**
1. Extract `pidx` from query params
2. Call Khalti verify API
3. Update donation status
4. Redirect user to success/failure page

---

### ✅ TASK 6: Add Error Handling (15 minutes)

Handle common errors:
- Khalti API down
- Invalid pidx
- Payment already processed
- Amount mismatch

---

### ✅ TASK 7: Test End-to-End (45 minutes)

1. Initiate donation
2. Get Khalti payment URL
3. Complete payment on Khalti sandbox
4. Verify callback works
5. Check database updates

---

### ✅ TASK 8: Add Logging (10 minutes)

Add logs for debugging:
- Khalti API requests
- Khalti API responses
- Payment status changes

---

## 🔥 Let's Start: TASK 1 - Fix KhaltiConfig

### Current Code:
```java
@Bean
public WebClient webClient() {
    return WebClient.builder().baseUrl(baseUrl)
            .defaultHeader("Authorization", "Key"+secretKey)  // ❌
    .build();
}
```

### Fixed Code:
```java
@Bean
public WebClient khaltiWebClient() {  // Better name
    return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Key " + secretKey)  // ✅ Space added
            .defaultHeader("Content-Type", "application/json")
            .build();
}
```

**Action:** Update this in `KhaltiConfig.java`

---

## 📋 Quick Reference: Khalti API Endpoints

### Initiate Payment
```
POST https://a.khalti.com/api/v2/epayment/initiate/
Headers: Authorization: Key test_secret_key_xxx
Body: {
  "return_url": "http://localhost:8085/payment/khalti/callback",
  "website_url": "http://localhost:3000",
  "amount": 10000,  // 100 NPR in paisa
  "purchase_order_id": "donation-uuid",
  "purchase_order_name": "Donation to John"
}
```

### Verify Payment
```
POST https://a.khalti.com/api/v2/epayment/lookup/
Headers: Authorization: Key test_secret_key_xxx
Body: {
  "pidx": "xxx"
}
```

---

## 🎯 MVP Success Criteria

After completing all tasks, you should be able to:

1. ✅ User clicks "Donate"
2. ✅ Backend creates donation record
3. ✅ Backend calls Khalti initiate API
4. ✅ Frontend redirects user to Khalti payment page
5. ✅ User completes payment on Khalti
6. ✅ Khalti redirects back to your callback URL
7. ✅ Backend verifies payment with Khalti
8. ✅ Backend updates donation status to COMPLETED
9. ✅ User sees success message

---

## 📊 Time Estimates

| Task | Time | Difficulty |
|------|------|------------|
| 1. Fix KhaltiConfig | 10 min | Easy |
| 2. Fix DTOs | 15 min | Easy |
| 3. Create KhaltiService | 30 min | Medium |
| 4. Update donateInitiate | 20 min | Medium |
| 5. Create callback endpoint | 30 min | Medium |
| 6. Error handling | 15 min | Easy |
| 7. End-to-end testing | 45 min | Medium |
| 8. Add logging | 10 min | Easy |
| **TOTAL** | **~3 hours** | **MVP** |

---

## 🚦 Next Steps

**Ready to start?** Let's begin with Task 1.

I'll guide you through each task one by one. After you complete Task 1 (fixing KhaltiConfig), let me know and I'll give you the code for Task 2.

**Start now:** Update `KhaltiConfig.java` with the fixed code above.
