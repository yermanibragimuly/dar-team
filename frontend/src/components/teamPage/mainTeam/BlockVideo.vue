<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { showError } from '@/shared/lib/toastService';
import { getPublicAllVideos } from '@/services/video-services';
import { renderAppleEmoji } from '@/shared/lib/appleEmoji';

const loading = ref(false);
const dataMyVideo = ref<any[]>([]);

const isPlayerOpen = ref(false);
const currentVideoId = ref('');

// Пагинация: по 9 видео на страницу
const itemsPerPage = 9
const currentPage = ref(0)

const openVideo = (videoLink: string) => {
  if (!videoLink) {
    showError('ID видео отсутствует');
    return;
  }
  currentVideoId.value = videoLink; 
  isPlayerOpen.value = true;
};

const closePlayer = () => {
  isPlayerOpen.value = false;
  currentVideoId.value = '';
};

const activeVideoUrl = computed(() => {
  if (!currentVideoId.value) return '';
  return `https://www.youtube-nocookie.com/embed/${currentVideoId.value}?autoplay=1&modestbranding=1&rel=0`;
});

const getMyVideo = async () => {
  try {
    loading.value = true;
    const res = await getPublicAllVideos();
    dataMyVideo.value = res?.data || res || [];
  } catch (err: any) {
    showError(err?.response?.data || 'Не удалось загрузить видеоматериалы');
  } finally {
    loading.value = false;
  }
};

// 9 видео для текущей страницы
const paginatedVideos = computed(() => {
  const start = currentPage.value * itemsPerPage
  return dataMyVideo.value.slice(start, start + itemsPerPage)
})

// Общее количество страниц
const totalPages = computed(() => {
  return Math.ceil(dataMyVideo.value.length / itemsPerPage) || 1
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

onMounted(async () => {
  await getMyVideo();
});
</script>

<template>
  <div class="videos-page relative px-4 sm:px-6 py-8 container mx-auto min-h-screen">

    <!-- Индикатор загрузки -->
    <div v-if="loading" class="flex justify-center items-center py-24">
      <i class="pi pi-spin pi-spinner text-3xl text-[#B00D15]"></i>
    </div>

    <!-- Заглушка, если видео нет -->
    <div v-else-if="dataMyVideo.length === 0" class="empty-state bg-[#18181B] border border-zinc-800 rounded-2xl p-12 text-center">
      <i class="pi pi-video text-5xl text-zinc-600 mb-4 block"></i>
      <h3 class="text-white font-semibold text-lg mb-1">Доступных видео пока нет</h3>
      <p class="text-zinc-500 text-sm mb-6">Видеоматериалы обновляются через панель управления.</p>
    </div>

    <!-- Сетка по 9 видео на страницу -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
      <div 
        v-for="sub in paginatedVideos" 
        :key="sub.id" 
        class="video-card bg-[#18181B] border border-zinc-800 rounded-2xl overflow-hidden flex flex-col hover:border-[#B00D15]/60 transition-all duration-300 group shadow-lg"
      >
        <!-- Превью с оригинальным чёрным затемнением (без серых подложек) -->
        <div class="relative aspect-video bg-black flex items-center justify-center overflow-hidden cursor-pointer" @click="openVideo(sub.youtubeVideoId)">
          <img 
            v-if="sub.youtubeVideoId"
            :src="`https://i.ytimg.com/vi/${sub.youtubeVideoId}/mqdefault.jpg`" 
            alt="Превью видео"
            class="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-500 pointer-events-none"
            loading="lazy"
          />
          <i v-else class="pi pi-video text-4xl text-zinc-700"></i>

          <!-- Мягкий градиент снизу превью -->
          <div class="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-transparent pointer-events-none z-10"></div>
          
          <!-- Кнопка Play при наведении -->
          <div class="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity duration-300 z-20">
            <div class="w-12 h-12 bg-[#B00D15] rounded-full flex items-center justify-center text-white shadow-xl transform scale-75 group-hover:scale-100 transition-transform duration-300">
              <i class="pi pi-play text-lg ml-1"></i>
            </div>
          </div>
        </div>

        <div class="p-5 flex flex-col flex-1">
          <!-- Заголовок с Apple iOS эмодзи -->
          <h3 
            class="text-white font-bold text-base line-clamp-1 mb-2 group-hover:text-[#B00D15] transition-colors cursor-pointer" 
            @click="openVideo(sub.youtubeVideoId)"
            v-html="renderAppleEmoji(sub.title || 'Материал команды DAR Team')"
          ></h3>
          
          <!-- Описание с Apple iOS эмодзи -->
          <p 
            class="text-zinc-400 text-xs line-clamp-2 leading-relaxed"
            v-html="renderAppleEmoji(sub.description || 'Эксклюзивный доступ к разбору техники от тренерского состава.')"
          ></p>
        </div>
      </div>
    </div>

    <!-- Пагинация как в новостях и бойцах -->
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

      <!-- Список страниц -->
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

    <!-- Модальное окно плеера YouTube -->
    <div 
      v-if="isPlayerOpen" 
      class="fixed inset-0 bg-black/90 backdrop-blur-md z-50 flex items-center justify-center p-4 transition-all duration-300"
      @click.self="closePlayer"
    >
      <div class="relative w-full max-w-4xl bg-[#111] rounded-2xl overflow-hidden border border-zinc-800 shadow-2xl animate-fade-in">
        <button 
          @click="closePlayer" 
          class="absolute top-4 right-4 z-50 bg-black/60 hover:bg-[#B00D15] text-white w-10 h-10 rounded-full flex items-center justify-center transition shadow-lg border border-white/10 cursor-pointer"
        >
          <i class="pi pi-times text-sm"></i>
        </button>

        <div class="aspect-video w-full">
          <iframe
            v-if="activeVideoUrl"
            :src="activeVideoUrl"
            title="YouTube video player"
            frameborder="0"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
            allowfullscreen
            class="w-full h-full"
          ></iframe>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;  
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;  
  overflow: hidden;
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>