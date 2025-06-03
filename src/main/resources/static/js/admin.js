// Initialize tooltips
document.addEventListener('DOMContentLoaded', function() {
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
});

// Initialize popovers
document.addEventListener('DOMContentLoaded', function() {
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    var popoverList = popoverTriggerList.map(function(popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
});

// Confirm delete actions
document.addEventListener('DOMContentLoaded', function() {
    const deleteButtons = document.querySelectorAll('.btn-delete');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('Are you sure you want to delete this item?')) {
                e.preventDefault();
            }
        });
    });
});

// Form validation
document.addEventListener('DOMContentLoaded', function() {
    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });
});

// Search functionality
document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(function(e) {
            const query = e.target.value;
            if (query.length >= 2) {
                performSearch(query);
            }
        }, 300));
    }
});

// Debounce function for search
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

// Perform search
function performSearch(query) {
    const currentUrl = new URL(window.location.href);
    currentUrl.searchParams.set('query', query);
    currentUrl.searchParams.set('page', '0');
    window.location.href = currentUrl.toString();
}

// Sort table columns
document.addEventListener('DOMContentLoaded', function() {
    const sortButtons = document.querySelectorAll('.sort-button');
    sortButtons.forEach(button => {
        button.addEventListener('click', function() {
            const currentUrl = new URL(window.location.href);
            const currentSort = currentUrl.searchParams.get('sortBy') || 'id';
            const currentDirection = currentUrl.searchParams.get('direction') || 'asc';
            const newSort = this.dataset.sort;
            
            let newDirection = 'asc';
            if (currentSort === newSort) {
                newDirection = currentDirection === 'asc' ? 'desc' : 'asc';
            }
            
            currentUrl.searchParams.set('sortBy', newSort);
            currentUrl.searchParams.set('direction', newDirection);
            window.location.href = currentUrl.toString();
        });
    });
});

// Show loading spinner
function showLoading() {
    const spinner = document.createElement('div');
    spinner.className = 'spinner-overlay';
    spinner.innerHTML = '<div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div>';
    document.body.appendChild(spinner);
}

// Hide loading spinner
function hideLoading() {
    const spinner = document.querySelector('.spinner-overlay');
    if (spinner) {
        spinner.remove();
    }
}

// Handle AJAX form submissions
document.addEventListener('DOMContentLoaded', function() {
    const ajaxForms = document.querySelectorAll('.ajax-form');
    ajaxForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            showLoading();
            
            const formData = new FormData(form);
            const url = form.action;
            const method = form.method;
            
            fetch(url, {
                method: method,
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                hideLoading();
                if (data.success) {
                    showAlert('success', data.message);
                    if (data.redirect) {
                        setTimeout(() => {
                            window.location.href = data.redirect;
                        }, 1000);
                    }
                } else {
                    showAlert('danger', data.message);
                }
            })
            .catch(error => {
                hideLoading();
                showAlert('danger', 'An error occurred. Please try again.');
                console.error('Error:', error);
            });
        });
    });
});

// Show alert message
function showAlert(type, message) {
    const alertContainer = document.createElement('div');
    alertContainer.className = `alert alert-${type} alert-dismissible fade show`;
    alertContainer.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'} me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    const container = document.querySelector('.container-fluid');
    container.insertBefore(alertContainer, container.firstChild);
    
    setTimeout(() => {
        alertContainer.remove();
    }, 5000);
}

// Export data
document.addEventListener('DOMContentLoaded', function() {
    const exportButtons = document.querySelectorAll('.btn-export');
    exportButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const url = this.href;
            showLoading();
            
            fetch(url)
            .then(response => response.blob())
            .then(blob => {
                hideLoading();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = this.dataset.filename || 'export.csv';
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                a.remove();
            })
            .catch(error => {
                hideLoading();
                showAlert('danger', 'Error exporting data. Please try again.');
                console.error('Error:', error);
            });
        });
    });
}); 