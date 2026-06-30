import { state } from './state.js?v=3';

const API_URL = '';

async function fetchWithAuth(url, options = {}) {
    const headers = options.headers || {};
    if (state.jwtToken) {
        headers['Authorization'] = `Bearer ${state.jwtToken}`;
    }
    
    const response = await fetch(`${API_URL}${url}`, {
        ...options,
        headers
    });
    
    if (response.status === 401 || response.status === 403) {
        throw new Error('Unauthorized');
    }
    
    return response;
}

export const api = {
    login: async (email, password) => {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Invalid login credentials.');
        }
        return response.json();
    },

    register: async (fullname, email, password, role) => {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fullname, email, password, role })
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Email already exists or invalid data.');
        }
        return response;
    },

    getMetrics: async () => {
        const response = await fetchWithAuth('/dashboard/metrics');
        if (!response.ok) throw new Error('Failed to load metrics');
        return response.json();
    },

    getNotifications: async () => {
        const response = await fetchWithAuth('/notifications');
        if (!response.ok) throw new Error('Failed to load notifications');
        return response.json();
    },

    markNotificationRead: async (id) => {
        const response = await fetchWithAuth(`/notifications/${id}/read`, {
            method: 'PUT'
        });
        return response.ok;
    },

    getTrips: async () => {
        const response = await fetchWithAuth('/trips');
        if (!response.ok) throw new Error('Failed to load trips');
        return response.json();
    },

    getTripById: async (id) => {
        const response = await fetchWithAuth(`/trips/${id}`);
        if (!response.ok) throw new Error('Failed to load trip details');
        return response.json();
    },

    createTrip: async (tripData) => {
        const response = await fetchWithAuth('/trips', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(tripData)
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Failed to submit travel request.');
        }
        return response.json();
    },

    approveTrip: async (tripId) => {
        const response = await fetchWithAuth(`/trips/${tripId}/approve`, {
            method: 'PUT'
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Failed to approve trip.');
        }
        return response;
    },

    rejectTrip: async (tripId) => {
        const response = await fetchWithAuth(`/trips/${tripId}/reject`, {
            method: 'PUT'
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Failed to reject trip.');
        }
        return response;
    },

    reimburseTrip: async (tripId) => {
        const response = await fetchWithAuth(`/trips/${tripId}/reimburse`, {
            method: 'PUT'
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Failed to mark trip as reimbursed.');
        }
        return response;
    },

    getExpensesForTrip: async (tripId) => {
        const response = await fetchWithAuth(`/trips/${tripId}/expenses`);
        if (!response.ok) throw new Error('Failed to load expenses');
        return response.json();
    },

    createExpense: async (tripId, expenseData) => {
        const response = await fetchWithAuth(`/trips/${tripId}/expenses`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(expenseData)
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Failed to create expense claim.');
        }
        return response.json();
    },

    deleteExpense: async (expenseId) => {
        const response = await fetchWithAuth(`/expenses/${expenseId}`, {
            method: 'DELETE'
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Failed to delete expense claim.');
        }
        return response;
    },

    uploadReceipt: async (expenseId, file) => {
        const formData = new FormData();
        formData.append('file', file);
        const response = await fetchWithAuth(`/expenses/${expenseId}/receipt`, {
            method: 'POST',
            body: formData
        });
        if (!response.ok) {
            throw new Error('Receipt upload failed.');
        }
        return response.json();
    },

    getReportsSummary: async () => {
        const response = await fetchWithAuth('/dashboard/reports/summary');
        if (!response.ok) throw new Error('Failed to load reports summary');
        return response.json();
    },

    getReportsCsv: async () => {
        const response = await fetchWithAuth('/dashboard/reports/csv');
        if (!response.ok) throw new Error('Failed to export CSV report.');
        return response.text();
    }
};
