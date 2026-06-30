export const Toast = {
    showToast: (message, type = 'success') => {
        const toast = document.getElementById('toast-banner');
        const icon = document.getElementById('toast-icon');
        const msgSpan = document.getElementById('toast-message');

        toast.className = `toast show toast-${type}`;
        if (type === 'success') {
            icon.className = 'fa-solid fa-circle-check';
        } else {
            icon.className = 'fa-solid fa-triangle-exclamation';
        }
        msgSpan.innerText = message;

        setTimeout(() => {
            toast.classList.remove('show');
        }, 4000);
    }
};
