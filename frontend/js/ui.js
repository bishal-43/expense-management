import { Auth } from './components/Auth.js?v=3';
import { Toast } from './components/Toast.js?v=3';
import { Navigation } from './components/Navigation.js?v=3';
import { Metrics } from './components/Metrics.js?v=3';
import { Notifications } from './components/Notifications.js?v=3';
import { Trips } from './components/Trips.js?v=3';
import { Expenses } from './components/Expenses.js?v=3';

export const ui = {
    ...Auth,
    ...Toast,
    ...Navigation,
    ...Metrics,
    ...Notifications,
    ...Trips,
    ...Expenses
};
