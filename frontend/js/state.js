export const state = {
    jwtToken: localStorage.getItem('token') || '',
    currentUser: null,
    activeTab: 'dashboard',
    currentTripId: null,
    myTripsData: [],
    categoryChartInstance: null,
    tripChartInstance: null,
    pendingOcrFile: null
};

export function setToken(token) {
    state.jwtToken = token;
    if (token) {
        localStorage.setItem('token', token);
    } else {
        localStorage.removeItem('token');
    }
}

export function clearState() {
    state.jwtToken = '';
    state.currentUser = null;
    state.activeTab = 'dashboard';
    state.currentTripId = null;
    state.myTripsData = [];
    state.pendingOcrFile = null;
    localStorage.removeItem('token');
}
