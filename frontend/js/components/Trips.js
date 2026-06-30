import { state } from '../state.js?v=3';

export const Trips = {
    renderDashboardTrips: (trips, onViewTrip) => {
        const homeTripsList = document.getElementById('dashboard-trips-list');
        if (state.activeTab === 'dashboard') {
            homeTripsList.innerHTML = '';
            if (trips.length === 0) {
                homeTripsList.innerHTML = `<div style="text-align:center; padding:20px; color:var(--text-muted); font-size:0.9rem;">No active trips.</div>`;
                return;
            }
            trips.slice(0, 4).forEach(t => {
                const dateText = `${t.startDate} to ${t.endDate}`;
                const badgeClass = t.status === 'PENDING' ? 'status-pending' : (t.status === 'APPROVED' ? 'status-approved' : (t.status === 'REIMBURSED' ? 'status-reimbursed' : 'status-rejected'));
                const card = document.createElement('div');
                card.className = 'glass-card trip-item';
                card.onclick = () => onViewTrip(t.id);
                card.innerHTML = `
                    <div class="trip-info-block">
                        <div class="trip-icon-wrapper"><i class="fa-solid fa-location-dot"></i></div>
                        <div class="trip-details">
                            <h3>${t.destination}</h3>
                            <p>${dateText}</p>
                        </div>
                    </div>
                    <div style="display:flex; align-items:center; gap:20px;">
                        <div class="trip-budget">
                            <div class="trip-budget-amount">₹${t.budget.toFixed(2)}</div>
                        </div>
                        <span class="status-badge ${badgeClass}">${t.status}</span>
                    </div>
                `;
                homeTripsList.appendChild(card);
            });
        }
    },

    renderTripsList: (trips, handlers) => {
        const container = document.getElementById('trips-container');
        container.innerHTML = '';

        if (trips.length === 0) {
            container.innerHTML = `<div style="text-align:center; padding: 40px; color:var(--text-muted);">No travel requests created yet.</div>`;
            return;
        }

        trips.forEach(t => {
            const dateText = `${t.startDate} to ${t.endDate}`;
            const badgeClass = t.status === 'PENDING' ? 'status-pending' : (t.status === 'APPROVED' ? 'status-approved' : (t.status === 'REIMBURSED' ? 'status-reimbursed' : 'status-rejected'));
            const isManager = state.currentUser.role === 'manager' || state.currentUser.role === 'admin';

            const card = document.createElement('div');
            card.className = 'glass-card trip-item';
            
            card.innerHTML = `
                <div class="trip-info-block" id="trip-info-${t.id}">
                    <div class="trip-icon-wrapper"><i class="fa-solid fa-plane"></i></div>
                    <div class="trip-details">
                        <h3>${t.destination}</h3>
                        <p>${dateText} &bull; ${t.description || 'No description'}${t.employeeName ? ` &bull; Requested by: <strong>${t.employeeName}</strong>` : ''}</p>
                    </div>
                </div>
                <div style="display:flex; align-items:center; gap:20px;">
                    <div class="trip-budget" id="trip-budget-${t.id}">
                        <div class="trip-budget-amount">₹${t.budget.toFixed(2)}</div>
                        <span class="status-badge ${badgeClass}">${t.status}</span>
                    </div>
                    <div class="actions-wrapper" id="trip-actions-${t.id}"></div>
                </div>
            `;
            
            card.querySelector(`#trip-info-${t.id}`).onclick = () => handlers.onViewTrip(t.id);
            card.querySelector(`#trip-budget-${t.id}`).onclick = () => handlers.onViewTrip(t.id);

            const actionsWrapper = card.querySelector(`#trip-actions-${t.id}`);
            
            if (isManager && t.status === 'PENDING') {
                actionsWrapper.innerHTML = `
                    <div class="trip-actions">
                        <button class="btn btn-success" id="btn-approve-${t.id}" style="padding: 8px 14px; width:auto; font-size:0.85rem;"><i class="fa-solid fa-check"></i></button>
                        <button class="btn btn-danger" id="btn-reject-${t.id}" style="padding: 8px 14px; width:auto; font-size:0.85rem;"><i class="fa-solid fa-xmark"></i></button>
                    </div>
                `;
                actionsWrapper.querySelector(`#btn-approve-${t.id}`).onclick = (e) => { e.stopPropagation(); handlers.onApprove(t.id); };
                actionsWrapper.querySelector(`#btn-reject-${t.id}`).onclick = (e) => { e.stopPropagation(); handlers.onReject(t.id); };
            } else if (isManager && t.status === 'APPROVED') {
                actionsWrapper.innerHTML = `
                    <div class="trip-actions">
                        <button class="btn btn-success" id="btn-reimburse-${t.id}" style="padding: 8px 14px; width:auto; font-size:0.85rem; background:#3b82f6; box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);" title="Mark as Reimbursed"><i class="fa-solid fa-money-bill-transfer"></i> Reimburse</button>
                    </div>
                `;
                actionsWrapper.querySelector(`#btn-reimburse-${t.id}`).onclick = (e) => { e.stopPropagation(); handlers.onReimburse(t.id); };
            }

            container.appendChild(card);
        });
    },

    updateTripMeta: (trip, spent) => {
        document.getElementById('detail-trip-title').innerText = `${trip.destination} Trip`;
        document.getElementById('meta-trip-destination').innerText = trip.destination;
        
        const reqContainer = document.getElementById('meta-trip-requester-container');
        const reqElement = document.getElementById('meta-trip-requester');
        if (trip.employeeName) {
            reqElement.innerText = trip.employeeName;
            reqContainer.style.display = 'block';
        } else {
            if (reqContainer) reqContainer.style.display = 'none';
        }

        document.getElementById('meta-trip-dates').innerText = `${trip.startDate} to ${trip.endDate}`;
        document.getElementById('meta-trip-budget').innerText = `₹${trip.budget.toFixed(2)}`;
        document.getElementById('meta-trip-spent').innerText = `₹${spent.toFixed(2)}`;
        
        const badgeClass = trip.status === 'PENDING' ? 'status-pending' : (trip.status === 'APPROVED' ? 'status-approved' : (trip.status === 'REIMBURSED' ? 'status-reimbursed' : 'status-rejected'));
        document.getElementById('meta-trip-status').innerHTML = `<span class="status-badge ${badgeClass}">${trip.status}</span>`;

        const addClaimBtn = document.getElementById('add-expense-btn');
        const isOwner = state.currentUser && state.currentUser.id === trip.userId;
        if (trip.status !== 'PENDING' || !isOwner) {
            addClaimBtn.style.display = 'none';
        } else {
            addClaimBtn.style.display = 'block';
        }
    },

    openCreateTripModal: () => {
        document.getElementById('modal-create-trip').style.display = 'flex';
    },

    closeCreateTripModal: () => {
        document.getElementById('modal-create-trip').style.display = 'none';
    },

    clearTripDetails: () => {
        document.getElementById('detail-trip-title').innerText = 'Loading...';
        document.getElementById('meta-trip-destination').innerText = 'Loading...';
        const reqContainer = document.getElementById('meta-trip-requester-container');
        if (reqContainer) reqContainer.style.display = 'none';
        document.getElementById('meta-trip-dates').innerText = '-';
        document.getElementById('meta-trip-budget').innerText = '-';
        document.getElementById('meta-trip-spent').innerText = '-';
        document.getElementById('meta-trip-status').innerHTML = '-';
        document.getElementById('add-expense-btn').style.display = 'none';
        document.getElementById('expenses-table-body').innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--text-muted);">Loading expenses...</td></tr>`;
    }
};
