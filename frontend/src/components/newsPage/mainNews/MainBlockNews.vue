<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { getAllNews, getSoloNewsId } from "@/services/news-services"
import { showError } from '@/shared/lib/toastService'
import type { News, PageResponse } from "../../../types/news"
import Dialog from "@/components/AllDialogs/dialogNews/DialogNews.vue";
import { useI18n } from 'vue-i18n';
import { renderAppleEmoji } from '@/shared/lib/appleEmoji'

const { locale } = useI18n();
const visibleDialog = ref(false)
const newscard = ref<PageResponse<News> | null>(null)
const selectData = ref<News | undefined>()
const currentPage = ref(0)

const props = defineProps({
  newLimit: {
    type: Number,
    default: 0,
  },
  hidePagination: {
    type: Boolean,
    default: false,
  }
});

onMounted(async () => {
  await allNews(0);
})

const allNews = async (page: number = 0) => {
  try {
    currentPage.value = page
    newscard.value = await getAllNews(page)
  } catch (err) {
    showError(err)
  }
}

const openDialog = async (value: boolean, data?: any) => {
  if (value) {
    visibleDialog.value = true
  } else {
    visibleDialog.value = false
  }
  selectData.value = await getpersonalyDate(data?.id)
}

const getpersonalyDate = async (id: any) => {
  let res = await getSoloNewsId(id)
  return res.data
}

const limitedNews = computed(() => {
  if (props.newLimit > 0) {
    return newscard.value?.content.slice(0, props.newLimit)
  }
  return newscard.value?.content
})

const totalPages = computed(() => {
  if (!newscard.value) return 1
  return newscard.value.totalPages || Math.ceil(newscard.value.totalElements / newscard.value.size) || 1
})

const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value + 1
  const pages: (number | string)[] = []

  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else {
    pages.push(1)
    if (current > 3) pages.push('...')
    const start = Math.max(2, current - 1)
    const end = Math.min(total - 1, current + 1)
    for (let i = start; i <= end; i++) pages.push(i)
    if (current < total - 2) pages.push('...')
    pages.push(total)
  }
  return pages
})

const getLangField = (item: any, field: string) => {
  const suffix = locale.value === 'ru' ? '' : (locale.value === 'en' ? 'En' : 'Kz');
  const fieldName = `${field}${suffix}`;
  return item[fieldName] || item[field];
};

const changePage = async (pageIndex: number) => {
  if (pageIndex < 0 || pageIndex >= totalPages.value) return
  await allNews(pageIndex)
}
</script>

<template>
  <Dialog :modelValue="visibleDialog" @update:modelValue="openDialog" :userData="selectData"/>
  
  <div class="container mx-auto px-4 py-6">
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <Card 
        v-for="value in limitedNews"
        :key="value.id"
        class="dark-card w-full h-full max-w-sm mx-auto flex flex-col cursor-pointer hover:border-red-600 transition-all"
        @click="openDialog(true, value)"
      >
        <template #header>
          <div class="relative w-full h-64 rounded-md overflow-hidden bg-[#1f1f22]">
            <img 
              v-if="value.img"
              :src="value.img"
              class="absolute inset-0 w-full h-full object-cover blur-xl opacity-30"
            />
            <img 
              v-if="value.img"
              :src="value.img"
              class="absolute inset-0 m-auto max-h-full max-w-full object-contain"
            />
            <div v-else class="w-full h-full flex items-center justify-center text-zinc-600 text-xs font-bold">
              НЕТ ФОТО
            </div>
          </div>
        </template>

        <!-- Заголовок с Apple iOS эмодзи -->
        <template #title>
          <h3 
            class="text-lg font-semibold line-clamp-2 text-white"
            v-html="renderAppleEmoji(getLangField(value, 'title'))"
          ></h3>
        </template>

        <template #subtitle>
          <p class="text-sm text-gray-400 mt-2 font-mono">{{ value.date }}</p>
        </template>
      </Card>
    </div>

    <!-- Пагинация -->
    <div 
      v-if="!hidePagination && totalPages > 1" 
      class="mt-12 flex items-center justify-center gap-1.5"
    >
      <button
        @click="changePage(0)"
        :disabled="currentPage === 0"
        title="В самое начало"
        class="w-9 h-9 rounded-lg bg-[#131316] text-[#A1A1AA] hover:text-white hover:bg-[#1f1f22] disabled:opacity-20 disabled:cursor-not-allowed flex items-center justify-center transition-all cursor-pointer border border-[#27272A]"
      >
        <i class="pi pi-angle-double-left text-xs"></i>
      </button>

      <button
        @click="changePage(currentPage - 1)"
        :disabled="currentPage === 0"
        title="Предыдущая страница"
        class="w-9 h-9 rounded-lg bg-[#131316] text-[#A1A1AA] hover:text-white hover:bg-[#1f1f22] disabled:opacity-20 disabled:cursor-not-allowed flex items-center justify-center transition-all cursor-pointer border border-[#27272A]"
      >
        <i class="pi pi-chevron-left text-xs"></i>
      </button>

      <template v-for="(item, idx) in visiblePages" :key="idx">
        <button
          v-if="typeof item === 'number'"
          @click="changePage(item - 1)"
          :class="[
            'w-9 h-9 rounded-lg font-bold text-xs transition-all cursor-pointer flex items-center justify-center border',
            currentPage === (item - 1)
              ? 'bg-[#B00D15] text-white border-[#B00D15] shadow-[0_0_12px_rgba(176,13,21,0.4)]'
              : 'bg-[#131316] text-[#A1A1AA] hover:bg-[#1f1f22] hover:text-white border-[#27272A]'
          ]"
        >
          {{ item }}
        </button>

        <span 
          v-else 
          class="w-7 h-9 flex items-center justify-center text-[#71717A] font-bold text-xs select-none"
        >
          ...
        </span>
      </template>

      <button
        @click="changePage(currentPage + 1)"
        :disabled="currentPage >= totalPages - 1"
        title="Следующая страница"
        class="w-9 h-9 rounded-lg bg-[#131316] text-[#A1A1AA] hover:text-white hover:bg-[#1f1f22] disabled:opacity-20 disabled:cursor-not-allowed flex items-center justify-center transition-all cursor-pointer border border-[#27272A]"
      >
        <i class="pi pi-chevron-right text-xs"></i>
      </button>

      <button
        @click="changePage(totalPages - 1)"
        :disabled="currentPage >= totalPages - 1"
        title="В самый конец"
        class="w-9 h-9 rounded-lg bg-[#131316] text-[#A1A1AA] hover:text-white hover:bg-[#1f1f22] disabled:opacity-20 disabled:cursor-not-allowed flex items-center justify-center transition-all cursor-pointer border border-[#27272A]"
      >
        <i class="pi pi-angle-double-right text-xs"></i>
      </button>
    </div>
  </div>
</template>