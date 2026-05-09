/**
 * Profile Orders Management - Clean Implementation
 * Handles loading, rendering, and actions for user orders
 * NO refresh button, NO auto-refresh
 */

/**
 * Load orders from API and render them
 */
async function loadMyOrders() {
    const container = document.getElementById('myOrdersContainer');
    if (!container) {
        console.error('[loadMyOrders] Container #myOrdersContainer not found');
        return;
    }

    try {
        // Show loading state
        container.innerHTML = '<p class="text-muted text-center py-4"><i class="spinner-border spinner-border-sm me-2"></i>جاري التحميل... / Loading orders...</p>';

        // Fetch orders from API
        const orders = await apiGet('/orders/my');

        // Check if empty
        if (!orders || !Array.isArray(orders) || orders.length === 0) {
            container.innerHTML = '<p class="text-muted text-center py-4">لا توجد طلبات بعد. <a href="shop.html">ابدأ التسوق!</a><br><small>No orders yet. <a href="shop.html">Start shopping!</a></small></p>';
            return;
        }

        // Render orders
        const ordersHTML = orders.map(order => renderOrderCard(order)).join('');
        container.innerHTML = ordersHTML;

        // Set up event delegation for action buttons
        setupOrderActions();

        // Log for debugging
        console.log('[loadMyOrders] Orders loaded:', orders.length, 'orders');
        orders.forEach(order => {
            console.log(`[loadMyOrders] Order #${order.id}: status=${order.status}, returnStatus=${order.returnStatus || 'NONE'}`);
        });

    } catch (error) {
        console.error('[loadMyOrders] Failed to load orders:', error);
        const errorMsg = error.message || 'Unknown error';
        
        // Handle 401 - redirect to login
        if (errorMsg.includes('Session expired') || errorMsg.includes('Unauthorized')) {
            window.location.href = 'login.html';
            return;
        }
        
        // Handle 403 - show message without crash
        if (errorMsg.includes('Access denied') || errorMsg.includes('Not allowed')) {
            container.innerHTML = `
                <div class="alert alert-warning text-center">
                    <strong>Not allowed / غير مسموح</strong>
                    <br><small>${errorMsg}</small>
                </div>
            `;
            return;
        }
        
        // Other errors
        container.innerHTML = `
            <div class="alert alert-danger text-center">
                <strong>خطأ في تحميل الطلبات / Error loading orders:</strong>
                <br><small>${errorMsg}</small>
                <br><button class="btn btn-sm btn-primary mt-2" onclick="loadMyOrders()">
                    <i class="bi bi-arrow-clockwise me-1"></i>إعادة المحاولة / Retry
                </button>
            </div>
        `;
    }
}

/**
 * Render a single order card
 */
function renderOrderCard(order) {
    // Normalize status and returnStatus (handle lowercase, null, undefined)
    const status = (order.status || '').toUpperCase();
    const returnStatus = (order.returnStatus || 'NONE').toUpperCase();
    
    const statusBadge = getStatusBadge(status);
    const returnBadge = getReturnBadge(returnStatus);
    const paymentBadge = order.paymentMethod === 'COD' 
        ? '<span class="badge bg-secondary"><i class="bi bi-cash-coin me-1"></i>Cash on Delivery</span>'
        : '<span class="badge bg-primary"><i class="bi bi-credit-card me-1"></i>Card Payment</span>';

    // Format date
    const orderDate = new Date(order.createdAt).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });

    // Build action buttons based on normalized status
    let actionButtons = '';

    // Confirm Delivery button - only for SHIPPED orders
    if (status === 'SHIPPED') {
        actionButtons = `
            <button class="btn btn-success btn-sm" data-action="confirm-delivery" data-order-id="${order.id}">
                <i class="bi bi-check-circle me-1"></i>تم الاستلام / Order Received
            </button>
        `;
    }

    // Return Request button - only for DELIVERED orders with returnStatus === 'NONE'
    if (status === 'DELIVERED' && returnStatus === 'NONE') {
        actionButtons += `
            <button class="btn btn-warning btn-sm ${actionButtons ? 'ms-2' : ''}" onclick="openReturnModal(${order.id})">
                <i class="bi bi-arrow-counterclockwise me-1"></i>طلب إرجاع / Request Return
            </button>
        `;
    }

    // If no actions available
    if (!actionButtons) {
        actionButtons = '<span class="text-muted small">No actions available</span>';
    }

    return `
        <div class="card mb-3 shadow-sm">
            <div class="card-body">
                <div class="row align-items-center">
                    <div class="col-md-8">
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <h5 class="mb-0">Order #${order.id}</h5>
                            <span class="text-muted small">${orderDate}</span>
                        </div>
                        
                        <div class="mb-2">
                            ${statusBadge} ${paymentBadge} ${returnBadge}
                        </div>
                        
                        <div class="order-details">
                            <div class="row g-2 mb-2">
                                <div class="col-6">
                                    <small class="text-muted">Subtotal:</small>
                                    <div class="fw-bold">$${order.subtotal.toFixed(2)}</div>
                                </div>
                                <div class="col-6">
                                    <small class="text-muted">Shipping:</small>
                                    <div class="fw-bold ${order.shippingFee === 0 ? 'text-success' : ''}">
                                        ${order.shippingFee === 0 ? '<i class="bi bi-check-circle me-1"></i>FREE' : '$' + order.shippingFee.toFixed(2)}
                                    </div>
                                </div>
                            </div>
                            <div class="border-top pt-2">
                                <div class="d-flex justify-content-between align-items-center">
                                    <span class="text-muted">Total:</span>
                                    <strong class="fs-5 text-primary">$${order.total.toFixed(2)}</strong>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-4 text-end mt-3 mt-md-0">
                        ${actionButtons}
                    </div>
                </div>
            </div>
        </div>
    `;
}

/**
 * Get status badge HTML
 */
function getStatusBadge(status) {
    const badges = {
        'PENDING': '<span class="badge bg-warning"><i class="bi bi-clock me-1"></i>قيد الانتظار / Pending</span>',
        'CONFIRMED': '<span class="badge bg-info"><i class="bi bi-check me-1"></i>مؤكد / Confirmed</span>',
        'SHIPPED': '<span class="badge bg-primary"><i class="bi bi-truck me-1"></i>تم الشحن / Shipped</span>',
        'DELIVERED': '<span class="badge bg-success"><i class="bi bi-check-circle me-1"></i>تم التسليم / Delivered</span>',
        'CANCELLED': '<span class="badge bg-danger"><i class="bi bi-x-circle me-1"></i>ملغي / Cancelled</span>',
        'COMPLETED': '<span class="badge bg-success"><i class="bi bi-check2-all me-1"></i>مكتمل / Completed</span>'
    };
    return badges[status] || `<span class="badge bg-secondary">${status}</span>`;
}

/**
 * Get return status badge HTML
 */
function getReturnBadge(returnStatus) {
    if (!returnStatus || returnStatus === 'NONE') {
        return '';
    }
    const badges = {
        'REQUESTED': '<span class="badge bg-warning"><i class="bi bi-arrow-counterclockwise me-1"></i>تم طلب الإرجاع / Return Requested</span>',
        'APPROVED': '<span class="badge bg-success"><i class="bi bi-check-circle me-1"></i>موافق على الإرجاع / Return Approved</span>',
        'REJECTED': '<span class="badge bg-danger"><i class="bi bi-x-circle me-1"></i>مرفوض / Return Rejected</span>',
        'COMPLETED': '<span class="badge bg-info"><i class="bi bi-check2-all me-1"></i>اكتمل الإرجاع / Return Completed</span>'
    };
    return badges[returnStatus] || `<span class="badge bg-secondary">${returnStatus}</span>`;
}

/**
 * Set up event delegation for order action buttons
 */
function setupOrderActions() {
    const container = document.getElementById('myOrdersContainer');
    if (!container) return;

    // Remove existing listeners by cloning
    const newContainer = container.cloneNode(true);
    container.parentNode.replaceChild(newContainer, container);

        // Add click event listener with delegation (only for confirm-delivery)
        newContainer.addEventListener('click', async (e) => {
            const btn = e.target.closest('[data-action="confirm-delivery"]');
            if (!btn) return;

            const orderId = btn.dataset.orderId;
            await handleConfirmDelivery(orderId);
        });
}

// Global variable for return request
let currentReturnOrderId = null;

/**
 * Open return request modal
 */
function openReturnModal(orderId) {
    currentReturnOrderId = orderId;
    const reasonEl = document.getElementById("returnReasonInput");
    const errorBox = document.getElementById("returnError");
    
    if (reasonEl) {
        reasonEl.value = "";
        reasonEl.disabled = false;
        reasonEl.removeAttribute('readonly');
        reasonEl.removeAttribute('disabled');
    }
    
    if (errorBox) {
        errorBox.classList.add("d-none");
        errorBox.textContent = "";
    }
    
    const modal = new bootstrap.Modal(document.getElementById("returnModal"));
    modal.show();
    
    // Focus textarea after modal is shown
    setTimeout(() => {
        if (reasonEl) {
            reasonEl.focus();
        }
    }, 300);
}

/**
 * Handle confirm delivery action
 */
async function handleConfirmDelivery(orderId) {
    const container = document.getElementById('myOrdersContainer');
    if (!container) return;

    try {
        // Disable button and show loading
        const btn = container.querySelector(`[data-action="confirm-delivery"][data-order-id="${orderId}"]`);
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<i class="spinner-border spinner-border-sm me-1"></i>Processing...';
        }

        // Call API
        await apiPost(`/orders/${orderId}/confirm-delivery`, {});

        // Show success message
        showSuccessMessage('Order delivery confirmed successfully! / تم تأكيد استلام الطلبية بنجاح!');

        // Reload orders
        await loadMyOrders();

    } catch (error) {
        console.error('[handleConfirmDelivery] Error:', error);
        
        // Handle 401 - redirect to login
        if (error.message && (error.message.includes('Session expired') || error.message.includes('Unauthorized'))) {
            window.location.href = 'login.html';
            return;
        }
        
        // Show OCL errors using renderApiError
        renderApiError(container, error, { scrollToFirst: true, highlightFields: false });
        
        // Reload to restore button state
        await loadMyOrders();
    }
}

/**
 * Handle return request action - opens modal
 */
function handleReturnRequest(orderId) {
    openReturnModal(orderId);
}

/**
 * Show success message (toast)
 */
function showSuccessMessage(message) {
    // Try to use toast if available
    const toastEl = document.getElementById('successToast');
    if (toastEl) {
        const toastMessage = document.getElementById('toastMessage');
        if (toastMessage) {
            toastMessage.textContent = message;
        }
        const toast = new bootstrap.Toast(toastEl);
        toast.show();
    }
}

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    console.log('[profile-orders] DOM ready, loading orders...');
    loadMyOrders();
    
    // Set up return request submit handler
    const confirmReturnBtn = document.getElementById('confirmReturnBtn');
    if (confirmReturnBtn) {
        confirmReturnBtn.addEventListener('click', async () => {
            const reasonEl = document.getElementById('returnReasonInput');
            const errorBox = document.getElementById('returnError');
            
            // Clear previous errors
            errorBox.classList.add('d-none');
            errorBox.textContent = '';
            
            // Validate order ID
            if (!currentReturnOrderId) {
                errorBox.textContent = 'Error: No order selected / خطأ: لم يتم اختيار طلب';
                errorBox.classList.remove('d-none');
                return;
            }
            
            // Read and validate reason
            const reason = (reasonEl?.value || '').trim();
            
            if (!reason) {
                errorBox.textContent = 'Return reason is required / سبب الإرجاع مطلوب';
                errorBox.classList.remove('d-none');
                if (reasonEl) {
                    reasonEl.focus();
                }
                return;
            }
            
            try {
                // Disable button
                confirmReturnBtn.disabled = true;
                confirmReturnBtn.innerHTML = '<i class="spinner-border spinner-border-sm me-1"></i>Submitting...';
                
                // Call API with reason
                await apiPost(`/orders/${currentReturnOrderId}/return-request`, { reason: reason });
                
                // Hide modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('returnModal'));
                if (modal) modal.hide();
                
                // Show success
                showSuccessMessage('Return request submitted successfully! / تم إرسال طلب الإرجاع بنجاح!');
                
                // Reload orders
                await loadMyOrders();
                
            } catch (error) {
                console.error('[Return Request] Error:', error);
                
                // Handle 401 - redirect to login
                if (error.message && (error.message.includes('Session expired') || error.message.includes('Unauthorized'))) {
                    window.location.href = 'login.html';
                    return;
                }
                
                // Show OCL errors using renderApiError
                const modalBody = document.querySelector('#returnModal .modal-body');
                if (modalBody) {
                    renderApiError(modalBody, error, { scrollToFirst: true, highlightFields: true });
                } else {
                    // Fallback to errorBox
                    const errorMsg = error.message || 'Failed to submit return request';
                    errorBox.textContent = errorMsg;
                    errorBox.classList.remove('d-none');
                }
                
                // Restore button
                confirmReturnBtn.disabled = false;
                confirmReturnBtn.innerHTML = 'Submit Request';
            }
        });
    }
});
