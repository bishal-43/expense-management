import { state, setToken, clearState } from './state.js?v=3';
import { api } from './api.js?v=3';
import { ui } from './ui.js?v=3';
import { renderCharts } from './charts.js?v=3';

document.addEventListener('DOMContentLoaded', () => {
    if (state.jwtToken) {
        initApp();
    } else {
        ui.showAuth();
    }

    // Attach handlers globally to the window object so inline event handlers in index.html work.
    window.switchAuthTab = ui.switchAuthTab;
    window.togglePasswordVisibility = ui.togglePasswordVisibility;
    window.handleLogin = handleLogin;
    window.handleRegister = handleRegister;
    window.handleLogout = handleLogout;
    window.navigateTo = navigateTo;
    window.refreshData = refreshData;
    window.openCreateTripModal = ui.openCreateTripModal;
    window.closeCreateTripModal = ui.closeCreateTripModal;
    window.openAddExpenseModal = ui.openAddExpenseModal;
    window.closeAddExpenseModal = ui.closeAddExpenseModal;
    window.handleCreateTrip = handleCreateTrip;
    window.handleAddExpense = handleAddExpense;
    window.downloadCsv = downloadCsv;
    window.backToTrips = backToTrips;
    window.viewTripDetails = viewTripDetails;
    window.triggerFileInput = triggerFileInput;
    window.previewAndOcrFile = previewAndOcrFile;
    window.markAllNotificationsAsRead = markAllNotificationsAsRead;
    window.deleteExpense = deleteExpense;
    window.approveTrip = approveTrip;
    window.rejectTrip = rejectTrip;
    window.reimburseTrip = reimburseTrip;
});

// App Initialization
async function initApp() {
    try {
        const payload = JSON.parse(atob(state.jwtToken.split('.')[1]));
        state.currentUser = {
            id: payload.userId,
            email: payload.sub,
            role: payload.role.replace('ROLE_', '').toLowerCase()
        };

        ui.showApp();
        ui.updateProfileInfo(payload.sub, state.currentUser.role);
        refreshData();
    } catch (err) {
        handleLogout();
    }
}

// Authentication
async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    try {
        const data = await api.login(email, password);
        setToken(data.accessToken);
        ui.showToast('Successfully logged in.');
        initApp();
    } catch (err) {
        ui.showToast(err.message, 'error');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const fullName = document.getElementById('reg-name').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const role = document.getElementById('reg-role').value;

    try {
        await api.register(fullName, email, password, role);
        ui.showToast('Registration successful! Please login.');
        ui.switchAuthTab('login');
    } catch (err) {
        ui.showToast(err.message, 'error');
    }
}

function handleLogout() {
    clearState();
    ui.showAuth();
    ui.showToast('Logged out successfully.');
}

// Navigation & refreshing
function navigateTo(tabId) {
    ui.updateNavigation(tabId);
    state.activeTab = tabId;
    if (tabId === 'trips') {
        backToTrips();
    } else if (tabId === 'reports') {
        loadChartsData();
    }
    refreshData();
}

async function refreshData() {
    if (!state.jwtToken) return;
    loadMetrics();
    loadNotifications();
    if (state.activeTab === 'trips') {
        loadTrips();
    }
}

// Metrics
async function loadMetrics() {
    try {
        const metrics = await api.getMetrics();
        ui.renderMetrics(metrics);
    } catch (err) {
        console.error(err);
    }
}

// Notifications
async function loadNotifications() {
    try {
        const notifications = await api.getNotifications();
        ui.renderNotifications(notifications, markNotificationRead);
        
        if (state.activeTab === 'dashboard') {
            const trips = await api.getTrips();
            state.myTripsData = trips;
            ui.renderDashboardTrips(trips, (tripId) => {
                navigateTo('trips');
                viewTripDetails(tripId);
            });
        }
    } catch (err) {
        console.error(err);
    }
}

async function markNotificationRead(id) {
    try {
        const ok = await api.markNotificationRead(id);
        if (ok) {
            ui.showToast('Notification read.');
            refreshData();
        }
    } catch (err) {
        console.error(err);
    }
}

async function markAllNotificationsAsRead() {
    try {
        const notifs = await api.getNotifications();
        const unreads = notifs.filter(n => !n.isRead);
        for (let n of unreads) {
            await api.markNotificationRead(n.id);
        }
        ui.showToast('All notifications marked as read.');
        refreshData();
    } catch (err) {
        console.error(err);
    }
}

// Trips & Claims
async function loadTrips() {
    try {
        state.myTripsData = await api.getTrips();
        ui.renderTripsList(state.myTripsData, {
            onViewTrip: viewTripDetails,
            onApprove: approveTrip,
            onReject: rejectTrip,
            onReimburse: reimburseTrip
        });
    } catch (err) {
        ui.showToast('Failed to load trips.', 'error');
    }
}

function backToTrips() {
    document.getElementById('trips-main-view').style.display = 'block';
    document.getElementById('trip-details-view').style.display = 'none';
    state.currentTripId = null;
    loadTrips();
}

async function viewTripDetails(tripId) {
    state.currentTripId = tripId;
    document.getElementById('trips-main-view').style.display = 'none';
    document.getElementById('trip-details-view').style.display = 'block';

    ui.clearTripDetails();

    if (!state.myTripsData || state.myTripsData.length === 0) {
        try {
            state.myTripsData = await api.getTrips();
        } catch (err) {
            console.error('Failed to pre-fetch trips:', err);
        }
    }

    let trip = state.myTripsData.find(t => t.id === tripId);
    if (!trip) {
        try {
            trip = await api.getTripById(tripId);
            state.myTripsData.push(trip);
        } catch (err) {
            ui.showToast('Failed to load trip details.', 'error');
            return;
        }
    }

    if (trip) {
        loadExpenses(tripId, trip);
    } else {
        ui.showToast('Trip not found.', 'error');
    }
}

async function loadExpenses(tripId, trip) {
    try {
        const expenses = await api.getExpensesForTrip(tripId);
        ui.renderExpensesList(expenses, tripId, {
            onDelete: deleteExpense
        });
        const spent = expenses.reduce((sum, e) => sum + e.amount, 0);
        ui.updateTripMeta(trip, spent);
    } catch (err) {
        console.error(err);
    }
}

async function handleCreateTrip(e) {
    e.preventDefault();
    const destination = document.getElementById('trip-destination').value;
    const startDate = document.getElementById('trip-start-date').value;
    const endDate = document.getElementById('trip-end-date').value;
    const budget = document.getElementById('trip-budget').value;
    const description = document.getElementById('trip-description').value;

    try {
        await api.createTrip({ destination, startDate, endDate, budget, description });
        ui.showToast('Travel request submitted successfully.');
        ui.closeCreateTripModal();
        refreshData();
    } catch (err) {
        ui.showToast(err.message, 'error');
    }
}

// Expenses
async function handleAddExpense(e) {
    e.preventDefault();
    const description = document.getElementById('exp-description').value;
    const amount = document.getElementById('exp-amount').value;
    const category = document.getElementById('exp-category').value;
    const date = document.getElementById('exp-date').value;

    try {
        const savedExpense = await api.createExpense(state.currentTripId, { description, amount, category, date });

        if (state.pendingOcrFile) {
            try {
                await api.uploadReceipt(savedExpense.id, state.pendingOcrFile);
            } catch (fileErr) {
                ui.showToast('Expense created, but receipt upload failed.', 'error');
            }
        }

        ui.showToast('Expense claim saved successfully.');
        ui.closeAddExpenseModal();
        viewTripDetails(state.currentTripId);
    } catch (err) {
        ui.showToast(err.message, 'error');
    }
}

async function deleteExpense(expenseId) {
    if (!confirm('Are you sure you want to delete this expense claim?')) return;
    try {
        await api.deleteExpense(expenseId);
        ui.showToast('Expense claim deleted.');
        viewTripDetails(state.currentTripId);
    } catch (err) {
        ui.showToast(err.message, 'error');
    }
}

// Approvals
async function approveTrip(tripId) {
    try {
        await api.approveTrip(tripId);
        ui.showToast('Trip APPROVED.');
        refreshData();
    } catch (err) {
        ui.showToast(err.message, 'error');
    }
}

async function rejectTrip(tripId) {
    try {
        await api.rejectTrip(tripId);
        ui.showToast('Trip REJECTED.');
        refreshData();
    } catch (err) {
        ui.showToast(err.message, 'error');
    }
}

async function reimburseTrip(tripId) {
    if (!confirm('Are you sure you want to mark this claim as fully reimbursed?')) return;
    try {
        await api.reimburseTrip(tripId);
        ui.showToast('Trip claim REIMBURSED.');
        refreshData();
    } catch (err) {
        ui.showToast(err.message, 'error');
    }
}

// File triggers
function triggerFileInput() {
    const input = document.getElementById('receipt-file-input');
    input.onchange = previewAndOcrFile;
    input.click();
}

async function previewAndOcrFile(e) {
    const file = e.target.files[0];
    if (!file) return;

    state.pendingOcrFile = file;

    const reader = new FileReader();
    reader.onload = (event) => {
        ui.updateReceiptPreview(event.target.result);
    };
    reader.readAsDataURL(file);

    ui.showToast('Receipt attached. Real OCR analysis will run when saved.');

    const descInput = document.getElementById('exp-description');
    if (descInput && (!descInput.value || descInput.value.trim() === '')) {
        descInput.value = file.name.split('.')[0];
    }
}

// Download csv & charts summary loader
async function downloadCsv() {
    try {
        const csvData = await api.getReportsCsv();
        const blob = new Blob([csvData], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.setAttribute('href', url);
        a.setAttribute('download', `VentureSpend_Report_${new Date().toISOString().split('T')[0]}.csv`);
        a.click();
    } catch (err) {
        ui.showToast('Failed to export CSV report.', 'error');
    }
}

async function loadChartsData() {
    try {
        const summary = await api.getReportsSummary();
        renderCharts(summary);
    } catch (err) {
        console.error(err);
    }
}
