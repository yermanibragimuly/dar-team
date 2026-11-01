import { createI18n } from 'vue-i18n'
type MessageSchema = typeof ru
// Импортируем наши словари
import ru from '@/locales/ru.json'
import en from '@/locales/en.json'
import kk from '@/locales/kk.json'

const i18n = createI18n<[MessageSchema], 'ru' | 'en' | 'kk'>({
  legacy: false,      // Обязательно 'false' для работы с Vue 3 (Composition API)
  locale: 'kk',       // Язык по умолчанию при загрузке
  fallbackLocale: 'en', // Если в казахском забудем перевод, покажется английский
  messages: {
    kk: kk,
    ru: ru,
    en: en,
  }
})

export default i18n