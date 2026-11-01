import { createApp } from 'vue'
import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import PrimeVue from 'primevue/config';
import ToastService from 'primevue/toastservice'
import App from './App.vue';
import i18n from './i18n/i18n'
import router from './router';
import './assets/style/index.css';
import './assets/style/table.css'
import Aura from '@primeuix/themes/aura';
import '@/shared/api/interceptors/auth-interceptor'
import '@/assets/style/cards.css'

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

const app = createApp(App);
app.use(PrimeVue, {
    theme: {
        preset: Aura
    }
});
app.use(ToastService)
app.use(pinia)
app.use(router)
app.use(i18n)
app.mount("#app");