# 🎯 Postman Guide - Donation API Testing

## Quick Start

### 1. Import Collection
1. Open Postman
2. Click "Import" button
3. Select `Donation_API_Postman_Collection.json`
4. Collection will appear in your sidebar

### 2. Set Environment Variables
The collection uses these variables:
- `base_url`: Your API base URL (default: `http://localhost:8085`)
- `access_token`: JWT token (auto-set after login)
- `post_id`: Sample blog post ID for testing

---

## 📋 Step-by-Step Testing Flow

### Step 1: Register a User (Optional)

**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Expected Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER"
}
```

---

### Step 2: Login

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Expected Response:**
The JWT token will be stored in a cookie named `accessToken`.

**Response Body (example):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Important:** The Postman collection has a test script that automatically saves the token to the environment variable `access_token`.

---

### Step 3: Get Blog Posts (Find receiverId)

**Endpoint:** `GET /api/blog`

**Purpose:** Get list of blog posts to find:
- `receiverId`: The user ID of the blog post author (content creator)
- `postId`: The blog post ID you want to support

**Expected Response:**
```json
[
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "title": "My First Blog Post",
    "content": "...",
    "imageUrl": "...",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "content_creator",
      "email": "creator@example.com"
    },
    "createdAt": "2026-02-24T10:00:00",
    "updatedAt": "2026-02-24T10:00:00"
  }
]
```

**Copy these values:**
- `user.id` → This is your `receiverId`
- `id` → This is your `postId`

---

### Step 4: Initiate Donation

**Endpoint:** `POST /payment`

**Headers:**
```
Content-Type: application/json
Cookie: accessToken=<your-jwt-token>
```

**Request Body (Donate to specific post):**
```json
{
  "receiverId": "550e8400-e29b-41d4-a716-446655440000",
  "postId": "660e8400-e29b-41d4-a716-446655440001",
  "amount": 100.00
}
```

**Request Body (General profile donation):**
```json
{
  "receiverId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 50.00
}
```

**Expected Response:**
```
200 OK
```

---

## 🔧 Postman Configuration

### Setting Up Cookies

Since your API uses cookies for authentication, you need to ensure Postman handles them correctly.

#### Method 1: Automatic (Recommended)
The collection is configured to automatically use cookies. Just make sure:
1. In Postman Settings → General → "Automatically follow redirects" is ON
2. In Postman Settings → General → "Send cookies" is ON

#### Method 2: Manual Cookie Header
If automatic cookies don't work, manually add the Cookie header:

```
Cookie: accessToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 📝 Request Examples

### Example 1: Small Donation (₹50)
```json
{
  "receiverId": "550e8400-e29b-41d4-a716-446655440000",
  "postId": "660e8400-e29b-41d4-a716-446655440001",
  "amount": 50.00
}
```

### Example 2: Medium Donation (₹100)
```json
{
  "receiverId": "550e8400-e29b-41d4-a716-446655440000",
  "postId": "660e8400-e29b-41d4-a716-446655440001",
  "amount": 100.00
}
```

### Example 3: Large Donation (₹500)
```json
{
  "receiverId": "550e8400-e29b-41d4-a716-446655440000",
  "postId": "660e8400-e29b-41d4-a716-446655440001",
  "amount": 500.00
}
```

### Example 4: Profile Donation (No specific post)
```json
{
  "receiverId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 100.00
}
```

---

## 🐛 Troubleshooting

### Error: "Unable to extract user ID from token"

**Cause:** Your JWT token doesn't have the `userId` claim (old token format).

**Solution:**
1. Log out
2. Log in again to get a new token with `userId` claim
3. Try the donation request again

---

### Error: "Unauthorized" or "401"

**Cause:** Missing or invalid JWT token.

**Solution:**
1. Check if you're logged in
2. Verify the `accessToken` cookie is being sent
3. Check if token has expired (default: 24 hours)
4. Log in again if needed

---

### Error: "User not found"

**Cause:** The `receiverId` doesn't exist in the database.

**Solution:**
1. Call `GET /api/blog` to get valid user IDs
2. Use the `user.id` from a blog post as `receiverId`

---

### Error: "Blog post not found"

**Cause:** The `postId` doesn't exist.

**Solution:**
1. Call `GET /api/blog` to get valid post IDs
2. Use a valid `id` from the response
3. Or omit `postId` for a general profile donation

---

### Error: "Invalid amount"

**Cause:** Amount is negative, zero, or invalid format.

**Solution:**
1. Use positive numbers only
2. Use decimal format: `100.00` not `100`
3. Don't use currency symbols: `100.00` not `₹100`

---

## 🔍 Testing Checklist

- [ ] Register a new user
- [ ] Login successfully
- [ ] Token is saved in cookie
- [ ] Get list of blog posts
- [ ] Copy receiverId and postId
- [ ] Initiate donation to specific post
- [ ] Initiate general profile donation
- [ ] Test with different amounts (50, 100, 500)
- [ ] Verify response is 200 OK

---

## 📊 Expected Database Changes

After successful donation initiation, check your database:

```sql
SELECT * FROM donations ORDER BY created_at DESC LIMIT 1;
```

You should see a new record with:
- `id`: Auto-generated UUID
- `sender_id`: Your user ID (from JWT)
- `receiver_id`: Content creator's ID
- `blog_post_id`: Post ID (or NULL)
- `amount`: Donation amount
- `status`: INITIATED
- `created_at`: Current timestamp

---

## 🎨 Postman Tips

### 1. Use Environment Variables
Instead of hardcoding values, use variables:
```
{{base_url}}/payment
{{access_token}}
{{receiver_id}}
```

### 2. Create Pre-request Scripts
Auto-generate test data:
```javascript
// Generate random amount
pm.environment.set("random_amount", Math.floor(Math.random() * 500) + 50);
```

### 3. Add Tests
Verify responses automatically:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response time is less than 500ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});
```

### 4. Use Collection Runner
Test multiple scenarios at once:
1. Click "Runner" button
2. Select your collection
3. Set iterations (e.g., 10)
4. Run all requests sequentially

---

## 🚀 Advanced Testing

### Test with cURL (Alternative to Postman)

```bash
# Login
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"SecurePass123!"}' \
  -c cookies.txt

# Initiate Donation
curl -X POST http://localhost:8085/payment \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "receiverId": "550e8400-e29b-41d4-a716-446655440000",
    "postId": "660e8400-e29b-41d4-a716-446655440001",
    "amount": 100.00
  }'
```

### Test with HTTPie (Alternative)

```bash
# Login
http POST http://localhost:8085/api/auth/login \
  email=john@example.com \
  password=SecurePass123! \
  --session=./session.json

# Initiate Donation
http POST http://localhost:8085/payment \
  receiverId=550e8400-e29b-41d4-a716-446655440000 \
  postId=660e8400-e29b-41d4-a716-446655440001 \
  amount:=100.00 \
  --session=./session.json
```

---

## 📱 Frontend Integration Example

Once you've tested in Postman, here's how to integrate in your frontend:

### JavaScript/Fetch
```javascript
async function initiateDonation(receiverId, postId, amount) {
  const response = await fetch('http://localhost:8085/payment', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include', // Important: Send cookies
    body: JSON.stringify({
      receiverId,
      postId,
      amount
    })
  });
  
  if (response.ok) {
    console.log('Donation initiated successfully!');
  } else {
    console.error('Donation failed:', await response.text());
  }
}

// Usage
initiateDonation(
  '550e8400-e29b-41d4-a716-446655440000',
  '660e8400-e29b-41d4-a716-446655440001',
  100.00
);
```

### Axios
```javascript
import axios from 'axios';

axios.defaults.withCredentials = true; // Send cookies

async function initiateDonation(receiverId, postId, amount) {
  try {
    const response = await axios.post('http://localhost:8085/payment', {
      receiverId,
      postId,
      amount
    });
    console.log('Donation initiated!', response.data);
  } catch (error) {
    console.error('Error:', error.response?.data);
  }
}
```

---

## 🎯 Next Steps

After testing the donation initiation:

1. **Implement Payment Gateway Integration** (Khalti)
   - Add Khalti payment flow
   - Handle payment callback
   - Update donation status

2. **Add More Endpoints**
   - Get user's donations (sent)
   - Get creator's donations (received)
   - Get donation statistics
   - Cancel pending donation

3. **Add Validation**
   - Minimum/maximum amount limits
   - Rate limiting
   - Fraud detection

4. **Add Notifications**
   - Email notification to creator
   - In-app notification
   - Thank you message to donor

---

**Happy Testing! 🚀**
