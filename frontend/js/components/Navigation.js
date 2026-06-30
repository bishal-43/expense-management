export const Navigation = {
    updateProfileInfo: (username, role) => {
        document.getElementById('user-display-name').innerText = username.split('@')[0];
        document.getElementById('user-display-role').innerText = role;
        document.getElementById('user-avatar-initials').innerText = username.substring(0, 2).toUpperCase();
    },

    updateNavigation: (tabId) => {
        const navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(item => item.classList.remove('active'));
        document.getElementById(`nav-${tabId}`).classList.add('active');

        const sections = document.querySelectorAll('.tab-content');
        sections.forEach(s => s.classList.remove('active'));
        document.getElementById(`content-${tabId}`).classList.add('active');

        const titleEl = document.getElementById('workspace-title-text');
        const subtitleEl = document.getElementById('workspace-subtitle-text');
        
        if (tabId === 'dashboard') {
            titleEl.innerText = 'Corporate Dashboard';
            subtitleEl.innerText = 'Welcome back to your corporate expense terminal.';
        } else if (tabId === 'trips') {
            titleEl.innerText = 'Travel Requests';
            subtitleEl.innerText = 'Manage travel claims, expenses, and track approvals.';
        } else if (tabId === 'reports') {
            titleEl.innerText = 'Analytics Reports';
            subtitleEl.innerText = 'Aggregate expense summaries and category allocations.';
        } else if (tabId === 'notifications') {
            titleEl.innerText = 'Notification Hub';
            subtitleEl.innerText = 'Latest activity alerts and workflow summaries.';
        }
    }
};
