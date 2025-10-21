// Event Photography Management System - Main JavaScript File

document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    initializeTooltips();
    initializeDatePickers();
    initializeFormValidations();
    initializeAjaxHandlers();
    initializeNotificationSystem();
}

// Tooltip Initialization
function initializeTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// Date Picker Initialization
function initializeDatePickers() {
    // Set minimum date for future events to today
    const dateInputs = document.querySelectorAll('input[type="date"]');
    dateInputs.forEach(input => {
        if (!input.value) {
            const today = new Date().toISOString().split('T')[0];
            input.value = today;
        }

        // Set min date to today for future events
        if (input.id.includes('date') || input.name.includes('date')) {
            input.min = new Date().toISOString().split('T')[0];
        }
    });
}

// Form Validation
function initializeFormValidations() {
    const forms = document.querySelectorAll('.needs-validation');

    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }

            form.classList.add('was-validated');
        }, false);
    });

    // Custom validation for amount fields
    const amountInputs = document.querySelectorAll('input[type="number"][min="0.01"]');
    amountInputs.forEach(input => {
        input.addEventListener('input', function() {
            const value = parseFloat(this.value);
            if (value <= 0) {
                this.setCustomValidity('Amount must be greater than zero');
            } else {
                this.setCustomValidity('');
            }
        });
    });
}

// AJAX Handlers
function initializeAjaxHandlers() {
    // Handle form submissions with loading states
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function() {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="loading-spinner"></span> Processing...';
            }
        });
    });

    // Handle modal forms
    const modalForms = document.querySelectorAll('.modal form');
    modalForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            // Simulate AJAX submission
            setTimeout(() => {
                const modal = bootstrap.Modal.getInstance(form.closest('.modal'));
                modal.hide();
                showToast('Operation completed successfully!', 'success');
            }, 1000);
        });
    });
}

// Notification System
function initializeNotificationSystem() {
    // Check for new notifications periodically
    setInterval(checkNewNotifications, 30000); // Every 30 seconds

    // Mark notification as read when clicked
    const notificationItems = document.querySelectorAll('.notification-item');
    notificationItems.forEach(item => {
        item.addEventListener('click', function() {
            const notificationId = this.dataset.notificationId;
            if (notificationId) {
                markNotificationAsRead(notificationId);
            }
        });
    });
}

function checkNewNotifications() {
    // This would typically make an AJAX call to check for new notifications
    console.log('Checking for new notifications...');
}

function markNotificationAsRead(notificationId) {
    fetch(`/notifications/${notificationId}/mark-read`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const notificationElement = document.querySelector(`[data-notification-id="${notificationId}"]`);
                if (notificationElement) {
                    notificationElement.classList.add('read');
                }
            }
        })
        .catch(error => console.error('Error marking notification as read:', error));
}

// Toast Notification System
function showToast(message, type = 'info') {
    const toastContainer = document.getElementById('toast-container') || createToastContainer();

    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-bg-${type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');

    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;

    toastContainer.appendChild(toast);

    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();

    // Remove toast from DOM after it's hidden
    toast.addEventListener('hidden.bs.toast', function() {
        toast.remove();
    });
}

function createToastContainer() {
    const container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container position-fixed top-0 end-0 p-3';
    container.style.zIndex = '9999';
    document.body.appendChild(container);
    return container;
}

// File Upload Handling
function handleFileUpload(input, previewId) {
    const file = input.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const preview = document.getElementById(previewId);
            if (preview) {
                preview.src = e.target.result;
                preview.style.display = 'block';
            }
        };
        reader.readAsDataURL(file);
    }
}

// Search Functionality
function performSearch(query, searchType) {
    if (query.trim() === '') return;

    const searchUrl = `/search?q=${encodeURIComponent(query)}&type=${searchType}`;

    fetch(searchUrl)
        .then(response => response.json())
        .then(data => {
            displaySearchResults(data, searchType);
        })
        .catch(error => {
            console.error('Search error:', error);
            showToast('Search failed. Please try again.', 'error');
        });
}

function displaySearchResults(results, searchType) {
    const resultsContainer = document.getElementById('search-results');
    if (!resultsContainer) return;

    resultsContainer.innerHTML = '';

    if (results.length === 0) {
        resultsContainer.innerHTML = '<div class="text-center text-muted p-4">No results found</div>';
        return;
    }

    results.forEach(result => {
        const resultElement = createSearchResultElement(result, searchType);
        resultsContainer.appendChild(resultElement);
    });
}

function createSearchResultElement(result, searchType) {
    const div = document.createElement('div');
    div.className = 'search-result-item p-3 border-bottom';

    switch (searchType) {
        case 'events':
            div.innerHTML = `
                <h6><a href="/events/${result.id}">${result.name}</a></h6>
                <p class="mb-1">${result.date} at ${result.venue}</p>
                <small class="text-muted">${result.description}</small>
            `;
            break;
        case 'photos':
            div.innerHTML = `
                <h6><a href="/photos/${result.id}">${result.title}</a></h6>
                <p class="mb-1">From event: ${result.eventName}</p>
                <small class="text-muted">Uploaded: ${result.uploadDate}</small>
            `;
            break;
        default:
            div.innerHTML = `<h6>${result.name || result.title}</h6>`;
    }

    return div;
}

// Utility Functions
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(amount);
}

function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function formatDateTime(dateString) {
    return new Date(dateString).toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Export functions for global use
window.EventPhotoApp = {
    showToast,
    formatCurrency,
    formatDate,
    formatDateTime,
    debounce,
    handleFileUpload,
    performSearch
};