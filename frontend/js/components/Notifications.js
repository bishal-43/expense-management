import { state } from '../state.js?v=3';

export const Notifications = {
    renderNotifications: (notifications, onMarkRead) => {
        const container = document.getElementById('notifications-container');
        const unreadCount = notifications.filter(n => !n.isRead).length;

        const badge = document.getElementById('unread-notif-badge');
        if (unreadCount > 0) {
            badge.innerText = unreadCount;
            badge.style.display = 'inline-block';
        } else {
            badge.style.display = 'none';
        }

        if (state.activeTab === 'notifications') {
            container.innerHTML = '';
            if (notifications.length === 0) {
                container.innerHTML = `<div style="text-align:center; padding: 40px; color:var(--text-muted);">No notifications yet.</div>`;
                return;
            }
            
            notifications.forEach(n => {
                const date = new Date(n.createdAt).toLocaleString();
                const item = document.createElement('div');
                item.className = `glass-card notification-item ${!n.isRead ? 'unread' : ''}`;
                item.innerHTML = `
                    ${!n.isRead ? '<div class="notification-unread-dot"></div>' : ''}
                    <div class="notification-icon-wrapper">
                        <i class="fa-solid fa-bell"></i>
                    </div>
                    <div class="notification-content">
                        <p>${n.message}</p>
                        <span class="notification-time">${date}</span>
                    </div>
                `;
                
                if (!n.isRead) {
                    const btn = document.createElement('button');
                    btn.className = 'btn btn-secondary';
                    btn.style.width = 'auto';
                    btn.style.padding = '6px 12px';
                    btn.style.fontSize = '0.8rem';
                    btn.innerText = 'Mark Read';
                    btn.onclick = () => onMarkRead(n.id);
                    item.appendChild(btn);
                }
                container.appendChild(item);
            });
        }
    }
};
