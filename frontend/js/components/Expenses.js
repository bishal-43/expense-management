import { state } from '../state.js?v=3';

export const Expenses = {
    renderExpensesList: (expenses, tripId, handlers) => {
        const tbody = document.getElementById('expenses-table-body');
        tbody.innerHTML = '';

        if (expenses.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--text-muted);">No expenses claimed yet.</td></tr>`;
            return;
        }

        expenses.forEach(e => {
            const receiptHtml = e.receiptFileName ? 
                `<a href="/expenses/${e.id}/receipt" target="_blank" style="color:var(--primary); font-weight:600;"><i class="fa-solid fa-file-image"></i> View File</a>` : 
                `<span style="color:var(--text-muted);">No Receipt</span>`;

            const trip = state.myTripsData.find(t => t.id === tripId);
            const isOwner = state.currentUser && trip && state.currentUser.id === trip.userId;
            const isLocked = !trip || trip.status !== 'PENDING';
            const policyHtml = e.isPolicyViolated ? 
                `<br/><span style="color:var(--danger); font-size:0.75rem;" title="${e.policyViolationMessage}"><i class="fa-solid fa-triangle-exclamation"></i> Policy Violated</span>` : 
                '';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td style="font-weight: 500;">${e.description}</td>
                <td><span style="font-size:0.8rem; background:rgba(255,255,255,0.05); padding:4px 8px; border-radius:4px;">${e.category}</span></td>
                <td>${e.date}</td>
                <td>${receiptHtml}</td>
                <td style="font-weight: 700; color:white;">₹${e.amount.toFixed(2)}${policyHtml}</td>
                <td class="action-cell"></td>
            `;

            const actionCell = tr.querySelector('.action-cell');
            if (!isLocked && isOwner) {
                const btn = document.createElement('button');
                btn.className = 'btn btn-secondary';
                btn.style.padding = '6px 12px';
                btn.style.width = 'auto';
                btn.style.fontSize = '0.8rem';
                btn.style.color = 'var(--danger)';
                btn.style.borderColor = 'transparent';
                btn.innerHTML = `<i class="fa-solid fa-trash"></i>`;
                btn.onclick = () => handlers.onDelete(e.id);
                actionCell.appendChild(btn);
            } else if (isLocked) {
                actionCell.innerHTML = `<span style="font-size:0.75rem; color:var(--text-muted); font-style:italic;">Locked</span>`;
            } else {
                actionCell.innerHTML = '';
            }

            tbody.appendChild(tr);
        });
    },

    openAddExpenseModal: () => {
        document.getElementById('modal-add-expense').style.display = 'flex';
        document.getElementById('receipt-preview-area').innerHTML = `
            <i class="fa-solid fa-cloud-arrow-up" style="font-size: 2.2rem; color:var(--primary); margin-bottom:8px;"></i>
            <p id="receipt-upload-text">Click to upload receipt image (JPEG, PNG)</p>
            <input type="file" id="receipt-file-input" style="display: none;" accept="image/jpeg,image/png,image/gif">
        `;
    },

    closeAddExpenseModal: () => {
        document.getElementById('modal-add-expense').style.display = 'none';
    },

    updateReceiptPreview: (imgSrc) => {
        const area = document.getElementById('receipt-preview-area');
        area.innerHTML = `<img src="${imgSrc}" alt="Receipt">`;
    }
};
