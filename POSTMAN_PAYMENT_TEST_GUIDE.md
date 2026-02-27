# Postman Payment Testing Guide

## Prerequisites
- Application running on `http://localhost:8085`
- Valid Khalti sandbox credentials in `.env`
- PostgreSQL database running

## Step-by-Step Testing Flow

### Step 1: Register a User (Donor)
**Endpoint:** `POST http://localhost:8085/auth/register`

**Body (JSON):**
```json
{
  "username": "donor_user",
  "email": "donor@example.com",
  "password": "password123"
}
```

**Expected Response:**
```json
{
  "message": "User registered successfully",
  "status": "Success",
  "data": {
    "id": "uuid-here",
    "username": "donor_user",
    "email": "donor@example.com"
  }
}
```

---

### Step 2: Register a Receiver User
**Endpoint:** `POST http://localhost:8085/auth/register`

**Body (JSON):**
```json
{
  "username": "receiver_user",
  "email": "receiver@example.com",
  "password": "password123"
}
```

**Save the receiver's UUID** from the response - you'll need it for the donation.

---

### Step 3: Login as Donor
**Endpoint:** `POST http://localhost:8085/auth/login`

**Body (JSON):**
```json
{
  "email": "donor@example.com",
  "password": "password123"
}
```

**Expected Response:**
```json
{
  "accessToken": "jwt-token-here",
  "refreshToken": "refresh-token-here"
}
```

**Important:** The `accessToken` is automatically set as a cookie named `accessToken`. 

**In Postman:**
1. After login, go to **Cookies** (below the Send button)
2. Verify the `accessToken` cookie is set for `localhost:8085`
3. This cookie will be automatically sent with subsequent requests

---

### Step 4: Create a Donation (Initiate Order)
**Endpoint:** `POST http://localhost:8085/payment/initiate`

**Headers:**
- Cookie: `accessToken=<your-jwt-token>` (automatically sent if you logged in via Postman)

**Body (JSON):**
```json
{
  "amount": 100,
  "receiverId": "receiver-uuid-from-step-2",
  "postId": null
}
```

**Note:** 
- `amount` is in NPR (Nepali Rupees)
- `postId` is optional - set to `null` if donating directly to user
- If you have a blog post, use its UUID

**Expected Response:**
```json
{
  "donationId": "donation-uuid-here"
}
```

**Save the `donationId`** - you'll need it for the next step.

---

### Step 5: Initiate Khalti Payment
**Endpoint:** `POST http://localhost:8085/payment/khalti/initiate`

**Headers:**
- Cookie: `accessToken=<your-jwt-token>` (automatically sent)

**Body (JSON):**
```json
{
  "donationId": "donation-uuid-from-step-4"
}
```

**Expected Response:**
```json
{
  "pidx": "khalti-payment-index-here",
  "paymentUrl": "https://test-pay.khalti.com/#/..."
}
```

**What happens:**
- Backend creates a `PaymentTransaction` with status `PENDING`
- Calls Khalti API to initiate payment
- Returns `payment_url` where user should complete payment

---

### Step 6: Complete Payment (Manual Step)
1. Copy the `paymentUrl` from Step 5 response
2. Open it in your browser
3. Complete the payment using Khalti sandbox test credentials:
   - **Test Mobile:** `9800000000` to `9800000010`
   - **Test OTP:** `987654`
   - **Test PIN:** Any 4 digits

4. After successful payment, Khalti will redirect to:
   ```
   http://localhost:8085/payment/callback?pidx=<pidx>&status=Completed
   ```

5. Your backend will automatically verify the payment

---

### Step 7: Verify Payment (Alternative/Manual Verification)
If the callback doesn't work or you want to manually verify:

**Endpoint:** `POST http://localhost:8085/payment/khalti/verify`

**Headers:**
- Cookie: `accessToken=<your-jwt-token>` (automatically sent)

**Body (JSON):**
```json
{
  "pidx": "pidx-from-step-5"
}
```

**Expected Response (Success):**
```json
{
  "status": "COMPLETED",
  "message": "Payment successful"
}
```

**Expected Response (Failed):**
```json
{
  "status": "FAILED",
  "message": "Payment failed"
}
```

---

## Testing Without Khalti (For Development)

If you want to test the flow without actually calling Khalti, you can:

1. Comment out the Khalti API call in `PaymentServiceImpl.initiatePayment()`
2. Return mock data:
```java
// Mock response for testing
String pidx = "test-pidx-" + UUID.randomUUID();
String paymentUrl = "http://localhost:3000/mock-payment?pidx=" + pidx;
```

---

## Common Issues & Solutions

### Issue 1: "User not authenticated"
**Solution:** Make sure you're logged in and the `accessToken` cookie is being sent with the request.

**In Postman:**
- Check Cookies tab after login
- Ensure cookie domain matches `localhost:8085`

### Issue 2: "Donation not found"
**Solution:** Verify the `donationId` from Step 4 is correct and exists in the database.

### Issue 3: "Authentication credentials were not provided" (Khalti 401)
**Solution:** 
- Check `.env` file has correct `KHALTI_SECRET_KEY`
- Restart application after changing `.env`
- Check logs for "Secret key present: true"

### Issue 4: "Invalid UUID string: anonymousUser"
**Solution:** You're not authenticated. Login first (Step 3) and ensure cookie is sent.

---

## Database Verification

After each step, you can verify in PostgreSQL:

```sql
-- Check donations
SELECT * FROM donation ORDER BY updated_at DESC LIMIT 5;

-- Check payment transactions
SELECT * FROM payment_transaction ORDER BY updated_at DESC LIMIT 5;

-- Check wallets (after successful payment)
SELECT * FROM wallet WHERE user_id = 'receiver-uuid';
```

---

## Complete Flow Summary

```
1. Register Donor → Get donor account
2. Register Receiver → Get receiver UUID
3. Login as Donor → Get JWT token (cookie)
4. Create Donation → Get donationId
5. Initiate Payment → Get pidx + payment_url
6. Complete Payment → Use Khalti sandbox (browser)
7. Verify Payment → Automatic via callback OR manual API call
```

---

## Postman Collection Variables

Set these in your Postman environment:

| Variable | Example Value |
|----------|---------------|
| `baseUrl` | `http://localhost:8085` |
| `donorEmail` | `donor@example.com` |
| `receiverId` | `uuid-from-registration` |
| `donationId` | `uuid-from-initiate` |
| `pidx` | `khalti-pidx-from-response` |

---

## Next Steps

1. Test the complete flow manually
2. Create a Postman Collection with all requests
3. Add tests/assertions to verify responses
4. Set up environment variables for reusability
