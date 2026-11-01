<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { showError } from '@/shared/lib/toastService'
import { getAllCouches, getPersonCoucheId } from '@/services/couch-services'
import type { Coaches } from '@/types/coachesType'
import Dialog from '@/components/AllDialogs/dialogCoache/DialogCoache.vue'
import { renderAppleEmoji } from '@/shared/lib/appleEmoji'

const visibleDialog = ref(false)
const coacheCard = ref<Coaches[]>([])
const selectData = ref<Coaches | null>()
const loading = ref(false)

// Пагинация: по 9 тренеров на страницу
const itemsPerPage = 9
const currentPage = ref(0) // 0-индексировано

onMounted(async () => {
  await getDataFight()
})

const getDataFight = async () => {
  loading.value = true
  try {
    const res = await getAllCouches()
    coacheCard.value = res.data || []
  } catch (err) {
    showError(err)
  } finally {
    loading.value = false
  }
}

// 9 тренеров на текущую страницу
const paginatedCoaches = computed(() => {
  const start = currentPage.value * itemsPerPage
  return coacheCard.value.slice(start, start + itemsPerPage)
})

// Общее количество страниц
const totalPages = computed(() => {
  return Math.ceil(coacheCard.value.length / itemsPerPage) || 1
})

// Умный срез страниц с многоточием
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
    for (let i = start; i <= end; i++) {
      pages.push(i)
    }

    if (current < total - 2) pages.push('...')
    pages.push(total)
  }
  return pages
})

const changePage = (pageIndex: number) => {
  if (pageIndex < 0 || pageIndex >= totalPages.value) return
  currentPage.value = pageIndex
  window.scrollTo({ top: 120, behavior: 'smooth' })
}

const openDialog = async (value: boolean, data?: any) => {
  if (value) {
    visibleDialog.value = true
    if (data && data.id) {
      selectData.value = await getpersonalyDate(data.id)
    } else {
      selectData.value = null
    }
  } else {
    visibleDialog.value = false
  }
}

const getpersonalyDate = async (id: any) => {
  const res = await getPersonCoucheId(id)
  return res.data
}
</script>

<template>
  <Dialog :modelValue="visibleDialog" @update:modelValue="openDialog" :userData="selectData"/>

  <div class="container mx-auto min-h-screen px-4 py-8">
    <!-- Спиннер загрузки -->
    <div v-if="loading" class="flex justify-center items-center py-24">
      <i class="pi pi-spin pi-spinner text-3xl text-[#B00D15]"></i>
    </div>

    <!-- Сетка тренеров с оригинальным чёрным затемнением -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <Card 
        v-for="value in paginatedCoaches" 
        :key="value.id"
        class="dark-card w-full h-full max-w-sm mx-auto flex flex-col cursor-pointer hover:border-[#B00D15] transition-all"
        @click="openDialog(true, value)"
      >
        <template #header>
          <!-- Контейнер с глубоким чёрным фоном -->
          <div class="relative w-full h-64 rounded-md overflow-hidden bg-black">
            <!-- Размытие фона (оригинальная подложка) -->
            <img 
              v-if="value.img"
              :src="value.img"
              class="absolute inset-0 w-full h-full object-cover blur-xl opacity-30"
              loading="lazy"
            />

            <!-- Основное фото тренера -->
            <img 
              v-if="value.img"
              :src="value.img"
              class="absolute inset-0 m-auto max-h-full max-w-full object-contain"
              loading="lazy"
            />
          </div>
        </template>

        <template #title>
          <h3 
            class="text-lg font-semibold line-clamp-2 text-white"
            v-html="renderAppleEmoji(value.name)"
          ></h3>
        </template>

        <template #subtitle>
          <p 
            class="text-sm text-gray-500"
            v-html="renderAppleEmoji(value.status || 'Тренер')"
          ></p>
        </template>
      </Card>
    </div>

    <!-- Пагинация как в новостях, бойцах и видео -->
    <div 
      v-if="!loading && totalPages > 1" 
      class="mt-12 flex items-center justify-center gap-1.5"
    >
      <!-- В самое начало (<<) -->
      <button
        @click="changePage(0)"
        :disabled="currentPage === 0"
        title="В самое начало"
        class="w-9 h-9 rounded-lg bg-[#131316] text-[#A1A1AA] hover:text-white hover:bg-[#1f1f22] disabled:opacity-20 disabled:cursor-not-allowed flex items-center justify-center transition-all cursor-pointer border border-[#27272A]"
      >
        <i class="pi pi-angle-double-left text-xs"></i>
      </button>

      <!-- Назад (<) -->
      <button
        @click="changePage(currentPage - 1)"
        :disabled="currentPage === 0"
        title="Предыдущая страница"
        class="w-9 h-9 rounded-lg bg-[#131316] text-[#A1A1AA] hover:text-white hover:bg-[#1f1f22] disabled:opacity-20 disabled:cursor-not-allowed flex items-center justify-center transition-all cursor-pointer border border-[#27272A]"
      >
        <i class="pi pi-chevron-left text-xs"></i>
      </button>

      <!-- Список номеров страниц -->
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

      <!-- Вперед (>) -->
      <button
        @click="changePage(currentPage + 1)"
        :disabled="currentPage >= totalPages - 1"
        title="Следующая страница"
        class="w-9 h-9 rounded-lg bg-[#131316] text-[#A1A1AA] hover:text-white hover:bg-[#1f1f22] disabled:opacity-20 disabled:cursor-not-allowed flex items-center justify-center transition-all cursor-pointer border border-[#27272A]"
      >
        <i class="pi pi-chevron-right text-xs"></i>
      </button>

      <!-- В самый конец (>>) -->
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