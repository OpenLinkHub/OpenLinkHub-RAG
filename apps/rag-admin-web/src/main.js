import { createApp } from 'vue';
import App from './App.vue';
import router from './router/index.js';
import { setTokenGetter } from './api/request.js';
import { useAuth } from './composables/useAuth.js';
import './styles.css';

const { token } = useAuth();
setTokenGetter(() => token.value);

createApp(App).use(router).mount('#app');
