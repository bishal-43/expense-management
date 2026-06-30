export const Metrics = {
    renderMetrics: (metrics) => {
        const container = document.getElementById('metrics-container');
        container.innerHTML = '';

        const isManager = metrics.role === 'ROLE_Manager' || metrics.role === 'ROLE_Admin';

        if (isManager) {
            document.getElementById('create-trip-quick-btn').style.display = 'none';
            container.innerHTML = `
                <div class="glass-card metric-card">
                    <div class="metric-glow"></div>
                    <div class="metric-header"><span>Company Total Spent</span><i class="fa-solid fa-indian-rupee-sign"></i></div>
                    <div class="metric-value">₹${metrics.totalSpent.toFixed(2)}</div>
                    <div class="metric-desc">Across all employees</div>
                </div>
                <div class="glass-card metric-card">
                    <div class="metric-glow"></div>
                    <div class="metric-header"><span>Active Travel Requests</span><i class="fa-solid fa-plane"></i></div>
                    <div class="metric-value">${metrics.totalTripsCount}</div>
                    <div class="metric-desc">Total submitted trips</div>
                </div>
                <div class="glass-card metric-card">
                    <div class="metric-glow"></div>
                    <div class="metric-header"><span>Pending Approvals</span><i class="fa-solid fa-hourglass-half"></i></div>
                    <div class="metric-value" style="color:var(--warning);">${metrics.pendingApprovalsCount}</div>
                    <div class="metric-desc">Require review</div>
                </div>
                <div class="glass-card metric-card">
                    <div class="metric-glow"></div>
                    <div class="metric-header"><span>System Users</span><i class="fa-solid fa-users"></i></div>
                    <div class="metric-value">${metrics.totalUsersCount}</div>
                    <div class="metric-desc">Active employees and managers</div>
                </div>
            `;
        } else {
            document.getElementById('create-trip-quick-btn').style.display = 'block';
            const progressWidth = Math.min(metrics.budgetUtilizationPercentage, 100);
            container.innerHTML = `
                <div class="glass-card metric-card">
                    <div class="metric-glow"></div>
                    <div class="metric-header"><span>My Total Spent</span><i class="fa-solid fa-indian-rupee-sign"></i></div>
                    <div class="metric-value">₹${metrics.totalSpent.toFixed(2)}</div>
                    <div class="metric-desc">Out of total budget limit (₹${metrics.totalBudget.toFixed(2)})</div>
                </div>
                <div class="glass-card metric-card">
                    <div class="metric-glow"></div>
                    <div class="metric-header"><span>Budget Utilization</span><i class="fa-solid fa-chart-bar"></i></div>
                    <div class="metric-value">${metrics.budgetUtilizationPercentage.toFixed(1)}%</div>
                    <div class="progress-bar-container">
                        <div class="progress-bar-fill" style="width: ${progressWidth}%;"></div>
                    </div>
                </div>
                <div class="glass-card metric-card">
                    <div class="metric-glow"></div>
                    <div class="metric-header"><span>Trip Requests Status</span><i class="fa-solid fa-list-check"></i></div>
                    <div style="display:flex; justify-content:space-between; margin-top:5px; font-weight:600;">
                        <div><span style="color:var(--warning);">${metrics.pendingTripsCount}</span> <span style="font-size:0.75rem; color:var(--text-muted);">Pend</span></div>
                        <div><span style="color:var(--success);">${metrics.approvedTripsCount}</span> <span style="font-size:0.75rem; color:var(--text-muted);">Appr</span></div>
                        <div><span style="color:var(--danger);">${metrics.rejectedTripsCount}</span> <span style="font-size:0.75rem; color:var(--text-muted);">Rej</span></div>
                    </div>
                    <div class="metric-desc" style="margin-top:12px;">Total Trips: ${metrics.totalTrips}</div>
                </div>
            `;
        }
    }
};
