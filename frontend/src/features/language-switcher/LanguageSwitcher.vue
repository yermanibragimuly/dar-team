<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import Select from 'primevue/select';

const { locale } = useI18n();
const scrolled = ref(false);

const languages = [
  { label: 'KZ', code: 'kk', flag: 'kz' },
  { label: 'RU', code: 'ru', flag: 'ru' },
  { label: 'EN', code: 'en', flag: 'gb' }
];

// Находим объект текущего языка для отображения в селекте
const selectedLangObj = computed(() => 
  languages.find(lang => lang.code === locale.value) || languages[0]
);

const handleScroll = () => {
  scrolled.value = window.scrollY > 10;
};

onMounted(() => {
  window.addEventListener('scroll', handleScroll);
  // Загружаем сохраненный язык при инициализации
  const saved = localStorage.getItem('user-lang');
  if (saved && ['ru', 'en', 'kk'].includes(saved)) {
    locale.value = saved;
  }
});

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll);
});

const onLanguageChange = (e: any) => {
  // e.value будет содержать только 'code' (строку), так как мы используем optionValue="code"
  locale.value = e.value;
  localStorage.setItem('user-lang', e.value);
};
</script>

<template>
  <Select
    v-model="locale"
    :options="languages"
    optionLabel="label"
    optionValue="code"
    @change="onLanguageChange"
    :pt="{
        root: { 
          class: [
            scrolled ? '!bg-[#111724]/95' : '!bg-[#111724]/80',
            '!border-white/20 !rounded-md !transition-all !duration-300'
          ]
        },
        label: { class: '!text-white !text-[10px] !p-0 !px-2 !flex !items-center !justify-center' },
        overlay: {
          style: {
            backgroundColor: '#111724',
            border: '1px solid rgba(255, 255, 255, 0.1)'
          }
        },
        list: { class: 'p-0' },
        option: { class: 'text-white text-xs hover:bg-white/5 p-2' }
      }"
  >
    <template #value>
      <div v-if="selectedLangObj" class="flex items-center gap-1">
        <span :class="`fi fi-${selectedLangObj.flag}`" class="scale-75" />
        <span class="font-bold">{{ selectedLangObj.label }}</span>
      </div>
    </template>
    
    <template #option="slotProps">
      <div class="flex items-center gap-2">
        <span :class="`fi fi-${slotProps.option.flag}`" />
        <span>{{ slotProps.option.label }}</span>
      </div>
    </template>
  </Select>
</template>

<style scoped>
@import "https://cdn.jsdelivr.net/gh/lipis/flag-icons@7.2.3/css/flag-icons.min.css";

/* 1. Главный контейнер */
.p-select {
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  height: 25px !important;    /* Немного увеличим высоту для солидности */
  min-width: 45px !important; /* Увеличиваем ширину здесь */
  width: auto !important;
  border-radius: 4px;
}

/* 3. Размер иконки стрелочки */
:deep(.p-select-dropdown) {
  display: none !important;    /* Делаем зону стрелочки уже */
}

.p-select:not(.p-disabled).p-focus {
  background-color: azure;
}
</style>