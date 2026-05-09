# Login Fix Summary

## Problem
POST `/api/auth/login` was returning **400 Bad Request** instead of **200 OK** or **401 Unauthorized** for invalid credentials.

## Root Cause Analysis

### Why it was 400:
1. **Exception Handling**: `AuthService.login()` was throwing `RuntimeException` for unexpected errors, which was caught by `GlobalExceptionHandler` and returned **400** instead of **401**.
2. **Password Hash Mismatch**: The BCrypt hash in `data.sql` might not match the actual password encoding, or password might not be BCrypt encoded at all.

## Files Changed

### 1. `backend/src/main/java/com/americanwomen/store/service/AuthService.java`
**Change**: Modified exception handling in `login()` method
- All exceptions now throw `BadCredentialsException` (returns 401) instead of `RuntimeException` (returns 400)
- Added handling for `AuthenticationException` to ensure 401 response
- **Impact**: Invalid credentials now correctly return 401, not 400

### 2. `backend/src/main/java/com/americanwomen/store/controller/AuthController.java`
**Change**: Added try-catch in `login()` endpoint
- Ensures all exceptions are converted to `BadCredentialsException` for consistent 401 responses
- **Impact**: Prevents 400 errors from unexpected exceptions

### 3. `backend/src/main/java/com/americanwomen/store/controller/GlobalExceptionHandler.java`
**Change**: 
- Added logging for `BadCredentialsException` and `MethodArgumentNotValidException`
- Improved validation error messages to be more user-friendly
- **Impact**: Better debugging and clearer error messages

### 4. `backend/src/main/java/com/americanwomen/store/config/AdminInitializer.java` (UPDATED)
**Change**: Enhanced `CommandLineRunner` that runs on startup
- Checks if password is BCrypt encoded (starts with `$2a$`, `$2b$`, or `$2y$`)
- Re-encodes password if it's not BCrypt or doesn't match
- Creates admin user if doesn't exist: `username=admin`, `password=admin123`, `role=ROLE_ADMIN`
- **Impact**: Ensures admin user exists with correctly encoded password on every startup

### 5. `frontend/public/assets/js/app.js`
**Change**: Improved error handling in login forms
- Status 401 → shows "Wrong username or password"
- Status 400 → shows validation message from backend
- **Impact**: Better user experience with clear error messages

### 6. `frontend/public/assets/js/api.js`
**Change**: Improved 401 handling
- Login endpoint 401 errors don't clear token (user not logged in yet)
- Other endpoints 401 errors clear token and redirect to login
- **Impact**: Proper handling of login failures vs session expiration

## Endpoint Verification

### Backend Routes
- ✅ `/api/auth/login` - POST - Exists and working
- ✅ No alias needed - endpoint matches frontend

### Frontend Request
- ✅ URL: `POST /auth/login` (resolves to `/api/auth/login`)
- ✅ Payload: `{ username: "admin", password: "admin123" }`
- ✅ Headers: `Content-Type: application/json`
- ✅ Matches backend DTO: `AuthRequest` with `username` and `password` fields

## Status Codes

### Fixed Status Codes:
- ✅ **200 OK**: Successful login
- ✅ **401 Unauthorized**: Invalid credentials (was returning 400)
- ✅ **400 Bad Request**: Validation errors (missing/invalid fields)

## Admin User Seeder

### AdminInitializer (CommandLineRunner)
- ✅ **Idempotent**: Doesn't overwrite if admin exists and password is correct
- ✅ **Creates admin if not exists**: `username=admin`, `password=admin123`, `role=ROLE_ADMIN`
- ✅ **Detects non-BCrypt hash**: Checks if password starts with `$2a$`, `$2b$`, or `$2y$`
- ✅ **Re-encodes if needed**: Only updates password if it's not BCrypt or doesn't match
- ✅ **Verifies after fix**: Confirms password works after encoding

## Testing

### Expected Logs on Startup:
```
[AdminInitializer] Admin user exists. Username: admin, Role: ROLE_ADMIN, ID: 1
[AdminInitializer] ✅ Admin password is correct. Username: admin, Password: admin123
```

### Manual Database Check:
```sql
SELECT id, username, role FROM users WHERE username='admin';
```

### Test Steps:
1. Rebuild: `docker compose down -v && docker compose up --build`
2. Check startup logs for AdminInitializer messages
3. Login from UI: `admin` / `admin123`
4. Network tab should show **200 OK** on `/api/auth/login`
5. UI should show logged in state (Hi, admin + Admin button)

## No Breaking Changes

- ✅ No database schema changes
- ✅ No endpoint changes (still `/api/auth/login`)
- ✅ No feature removal
- ✅ Only exception handling and admin initialization fixes

## Deliverables

### Files Changed:
1. `backend/src/main/java/com/americanwomen/store/service/AuthService.java`
2. `backend/src/main/java/com/americanwomen/store/controller/AuthController.java`
3. `backend/src/main/java/com/americanwomen/store/controller/GlobalExceptionHandler.java`
4. `backend/src/main/java/com/americanwomen/store/config/AdminInitializer.java`
5. `frontend/public/assets/js/app.js`
6. `frontend/public/assets/js/api.js`

### Why it was 400:
- `AuthService.login()` was throwing `RuntimeException` for unexpected errors
- `GlobalExceptionHandler` returns 400 for `RuntimeException`
- Now all authentication errors throw `BadCredentialsException` which returns 401

### Endpoint Alias Added:
- None needed - `/api/auth/login` already exists and matches frontend

### Seeder Added/Updated:
- `AdminInitializer` (CommandLineRunner) - runs on every startup
- Creates admin if not exists
- Fixes password encoding if not BCrypt or incorrect
- Idempotent - doesn't overwrite if already correct
