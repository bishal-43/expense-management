import { state } from './state.js?v=3';

export function renderCharts(summary) {
    // Category spend pie chart
    const catLabels = Object.keys(summary.categorySpend);
    const catData = Object.values(summary.categorySpend);

    if (state.categoryChartInstance) state.categoryChartInstance.destroy();
    const ctx1 = document.getElementById('categoryChart').getContext('2d');
    state.categoryChartInstance = new Chart(ctx1, {
        type: 'doughnut',
        data: {
            labels: catLabels,
            datasets: [{
                data: catData,
                backgroundColor: ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#a855f7', '#6b7280'],
                borderWidth: 1,
                borderColor: 'rgba(255,255,255,0.1)'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: { color: '#f8fafc' }
                }
            }
        }
    });

    // Trip spend bar chart
    const tripLabels = Object.keys(summary.tripSpend);
    const tripData = Object.values(summary.tripSpend);

    if (state.tripChartInstance) state.tripChartInstance.destroy();
    const ctx2 = document.getElementById('tripChart').getContext('2d');
    state.tripChartInstance = new Chart(ctx2, {
        type: 'bar',
        data: {
            labels: tripLabels,
            datasets: [{
                label: 'Spend Amount (₹)',
                data: tripData,
                backgroundColor: 'rgba(99, 102, 241, 0.6)',
                borderColor: '#6366f1',
                borderWidth: 1,
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                x: {
                    grid: { color: 'rgba(255,255,255,0.05)' },
                    ticks: { color: '#94a3b8' }
                },
                y: {
                    grid: { color: 'rgba(255,255,255,0.05)' },
                    ticks: { color: '#94a3b8' }
                }
            }
        }
    });
}
