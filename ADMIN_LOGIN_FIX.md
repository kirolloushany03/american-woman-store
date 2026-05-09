# Admin Login Fix - Summary

## Problem
Admin login was returning **400 Bad Request** instead of **200 OK** or **401 Unauthorized**.

## Root Cause
1. **Exception Handling**: `AuthService.login()` was throwing `RuntimeException` for unexpected errors, which was caught by `GlobalExceptionHandler` and returned **400** instead of **401**.
2. **Password Hash Mismatch**: The BCrypt hash in `data.sql` might not match the actual password encoding.

## Files Changed

### 1. `backend/src/main/java/com/americanwomen/store/service/AuthService.java`
- **Change**: Modified exception handling in `login()` method
- **Why**: All exceptions now throw `BadCredentialsException` (returns 401) instead of `RuntimeException` (returns 400)
- **Impact**: Invalid credentials now correctly return 401, not 400

### 2. `backend/src/main/java/com/americanwomen/store/controller/AuthController.java`
- **Change**: Added try-catch in `login()` endpoint
- **Why**: Ensures all exceptions are converted to `BadCredentialsException` for consistent 401 responses
- **Impact**: Prevents 400 errors from unexpected exceptions

### 3. `backend/src/main/java/com/americanwomen/store/controller/GlobalExceptionHandler.java`
- **Change**: Added logging for `BadCredentialsException` and `MethodArgumentNotValidException`
- **Why**: Better debugging to identify if 400 is from validation or authentication
- **Impact**: Easier troubleshooting

### 4. `backend/src/main/java/com/americanwomen/store/config/AdminInitializer.java` (NEW)
- **Change**: Created `CommandLineRunner` that runs on startup
- **Why**: Automatically creates/fixes admin user with correct password hash
- **Impact**: Ensures admin user exists with password `admin123` encoded correctly

## Verification

### Check Admin User in Database
```sql
SELECT id, username, role FROM users WHERE username='admin';
```

### Expected Logs on Startup
```
[AdminInitializer] Admin user exists. Username: admin, Role: ROLE_ADMIN, ID: 1
[AdminInitializer] ✅ Admin password is correct. Username: admin, Password: admin123
```

### Login Request Details
- **URL**: `POST http://localhost:8090/api/auth/login`
- **Headers**: `Content-Type: application/json`
- **Body**: `{ "username": "admin", "password": "admin123" }`
- **Expected Response**: `200 OK` with token and user data
- **Invalid Credentials**: `401 Unauthorized` (not 400)

## Testing

1. **Rebuild and restart backend**:
   ```bash
   mvn clean install
   # Restart Spring Boot application
   ```

2. **Check startup logs** for AdminInitializer messages

3. **Login from UI**:
   - Username: `admin`
   - Password: `admin123`
   - Should see **200 OK** in Network tab
   - Should receive token and redirect

## No Schema Changes
- ✅ No database schema changes
- ✅ No endpoint changes (still `/api/auth/login`)
- ✅ Only exception handling and admin initialization fixes
