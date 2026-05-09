let apiBase = 'http://localhost:8090/api';
let apiInitialized = false;

async function initApi() {
    if (apiInitialized) return;
    try {
        const response = await fetch('http://localhost:8090/api/config');
        const config = await response.json();
        apiBase = config.apiBase || 'http://localhost:8090/api';
        apiInitialized = true;
        console.log('API Base initialized:', apiBase);
    } catch (error) {
        console.error('Failed to load API config, using default:', error);
        apiBase = 'http://localhost:8090/api';
        apiInitialized = true;
    }
}

// Initialize immediately
initApi();

/**
 * Get token from localStorage (supports multiple key names for compatibility)
 * Checks: token, jwt, accessToken
 */
function getToken() {
    return localStorage.getItem('token') || 
           localStorage.getItem('jwt') || 
           localStorage.getItem('accessToken');
}

/**
 * Get authentication headers with Bearer token
 * Automatically adds Authorization header if token exists
 */
function getAuthHeaders() {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json'
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    return headers;
}

/**
 * Unified API fetch wrapper that automatically adds Authorization header
 * This is the core wrapper function used by all API calls (apiGet, apiPost, etc.)
 * @param {string} endpoint - API endpoint (e.g., '/admin/orders', '/cart')
 * @param {object} options - Fetch options (method, body, etc.)
 * @returns {Promise<any>}
 */
async function apiFetch(endpoint, options = {}) {
    // Ensure API is initialized
    while (!apiInitialized) {
        await new Promise(resolve => setTimeout(resolve, 50));
    }
    
    const token = getToken();
    const url = `${apiBase}${endpoint}`;
    
    // Get base auth headers (includes Authorization if token exists)
    const authHeaders = getAuthHeaders();
    
    // Merge headers: options.headers override authHeaders
    const mergedHeaders = {
        ...authHeaders,
        ...(options.headers || {})
    };
    
    // Ensure Authorization is always added if token exists
    if (token && !mergedHeaders['Authorization'] && !mergedHeaders['authorization']) {
        mergedHeaders['Authorization'] = `Bearer ${token}`;
    }
    
    // Build final fetch options
    const fetchOptions = {
        ...options,
        headers: mergedHeaders
    };
    
    console.log(`[apiFetch] ${options.method || 'GET'} ${url}`, {
        hasToken: !!token,
        tokenPreview: token ? token.substring(0, 30) + '...' : 'none',
        authHeaderPresent: !!(mergedHeaders['Authorization'] || mergedHeaders['authorization'])
    });
    
    try {
        const response = await fetch(url, fetchOptions);
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ 
                message: `HTTP ${response.status}: ${response.statusText}` 
            }));
            
            // Create error object with response data for OCL error handling
            const apiError = new Error(errorData.message || `Request failed: ${response.status} ${response.statusText}`);
            apiError.response = errorData;
            apiError.status = response.status;
            
            // Handle 401 (Unauthorized)
            if (response.status === 401) {
                // Check if this is a login endpoint - don't clear token for login failures
                const isLoginEndpoint = endpoint.includes('/auth/login') || endpoint.includes('/login');
                
                if (!isLoginEndpoint) {
                    // For non-login endpoints, treat as session expired
                    console.error('[API Error 401] Unauthorized - session expired:', {
                        url: url,
                        endpoint: endpoint,
                        error: errorData.message || 'Unauthorized'
                    });
                    
                    // Force logout on 401 (invalid/expired token)
                    localStorage.removeItem('token');
                    localStorage.removeItem('jwt');
                    localStorage.removeItem('accessToken');
                    localStorage.removeItem('user');
                    
                    // Show toast notification if available
                    if (typeof showToast === 'function') {
                        showToast('Session expired, please login again.', 'warning');
                    } else if (typeof alert !== 'undefined') {
                        alert('Session expired, please login again.');
                    }
                    
                    // Redirect to login page
                    if (window.location.pathname !== '/login.html' && !window.location.pathname.includes('login.html')) {
                        window.location.href = 'login.html';
                    }
                } else {
                    // For login endpoint, just throw error with proper message
                    console.error('[API Error 401] Login failed:', {
                        url: url,
                        endpoint: endpoint,
                        error: errorData.message || 'Invalid credentials'
                    });
                }
                throw apiError;
            }
            
            // Handle 403 (Forbidden) - access denied, show message, keep token
            if (response.status === 403) {
                let userMessage = errorData.message || 'Access denied';
                
                // Customize message based on endpoint
                if (endpoint.includes('confirm-delivery')) {
                    userMessage = 'Not allowed (order not yours or not SHIPPED)';
                } else if (endpoint.includes('/admin/orders') || endpoint.includes('/admin/')) {
                    userMessage = 'Admin access required';
                }
                
                console.error('[API Error 403] Forbidden:', {
                    url: url,
                    endpoint: endpoint,
                    error: userMessage,
                    backendMessage: errorData.message
                });
                
                // Show error message but DO NOT logout
                if (typeof showToast === 'function') {
                    showToast(userMessage, 'error');
                }
                
                apiError.message = userMessage;
                throw apiError;
            }
            
            // For 400 (Bad Request) and other errors, throw with response data
            // This allows renderApiError to access OCL violations
            throw apiError;
        }
        
        // Handle empty responses
        if (response.status === 204 || response.headers.get('content-length') === '0') {
            return null;
        }
        
        return await response.json();
    } catch (error) {
        console.error('[apiFetch] Request failed:', {
            url,
            endpoint,
            error: error.message
        });
        throw error;
    }
}

async function apiGet(endpoint) {
    return apiFetch(endpoint, { method: 'GET' });
}

async function apiPost(endpoint, data) {
    return apiFetch(endpoint, {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

async function apiPut(endpoint, data) {
    return apiFetch(endpoint, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
}

async function apiDelete(endpoint) {
    return apiFetch(endpoint, { method: 'DELETE' });
}

async function apiPatch(endpoint, data) {
    return apiFetch(endpoint, {
        method: 'PATCH',
        body: JSON.stringify(data)
    });
}

async function login(username, password) {
    const response = await apiPost('/auth/login', { username, password });
    // Store token with unified key name: "token" (primary)
    const token = response.token || response.accessToken;
    if (token) {
        localStorage.setItem('token', token);
        console.log('[Login] Token stored as "token"');
    } else {
        console.warn('[Login] No token in response:', response);
    }
    localStorage.setItem('user', JSON.stringify(response.user));
    console.log('[Login] User logged in:', response.user);
    return response;
}

async function register(data) {
    const response = await apiPost('/auth/register', data);
    // Store token with unified key name: "token" (primary)
    const token = response.token || response.accessToken;
    if (token) {
        localStorage.setItem('token', token);
        console.log('[Register] Token stored as "token"');
    } else {
        console.warn('[Register] No token in response:', response);
    }
    localStorage.setItem('user', JSON.stringify(response.user));
    console.log('[Register] User registered:', response.user);
    return response;
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('jwt');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('user');
    window.location.href = 'index.html';
}

function getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
}

function isAuthenticated() {
    return !!getToken();
}

function isAdmin() {
    const user = getCurrentUser();
    return user && user.role === 'ROLE_ADMIN';
}

