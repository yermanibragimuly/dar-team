<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { showError } from '@/shared/lib/toastService';
import { getDataMyVideo } from '@/services/user/profile-services';

const loading = ref(false);
const dataMyVideo = ref<any[]>([]);

const isPlayerOpen = ref(false);
const currentVideoId = ref('');

// Функция для запуска видео
const openVideo = (videoLink: string) => {
  if (!videoLink) {
    showError('ID видео отсутствует');
    return;
  }
  currentVideoId.value = videoLink; 
  isPlayerOpen.value = true;
};

// Функция закрытия плеера
const closePlayer = () => {
  isPlayerOpen.value = false;
  currentVideoId.value = '';
};

// Вычисляемая ссылка для безопасного встраивания в iframe
const activeVideoUrl = computed(() => {
  if (!currentVideoId.value) return '';
  return `https://www.youtube-nocookie.com/embed/${currentVideoId.value}?autoplay=1&modestbranding=1&rel=0`;
});

// Вычисление оставшихся дней подписки
const calculateDaysLeft = (expiresAtStr: string): number => {
  if (!expiresAtStr) return 0;
  const expiresAt = new Date(expiresAtStr);
  const now = new Date();
  const diffTime = expiresAt.getTime() - now.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays > 0 ? diffDays : 0;
};

const getMyVideo = async () => {
  try {
    loading.value = true;
    const res = await getDataMyVideo();
    dataMyVideo.value = res?.data || res || [];
  } catch (err: any) {
    showError(err?.response?.data || 'Не удалось загрузить видеоматериалы');
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  await getMyVideo();
});
</script>

<template>
  <div class="videos-page relative min-h-screen">
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-white mb-2">Мои видеоматериалы</h1>
      <p class="text-gray-400 text-sm">Список оплаченных курсов и доступных вам видео-уроков</p>
    </div>

    <div v-if="loading" class="flex justify-center items-center py-20">
      <i class="pi pi-spin pi-spinner text-3xl text-red-500"></i>
    </div>

    <div v-else-if="dataMyVideo.length === 0" class="empty-state bg-[#18181B] border border-gray-800 rounded-2xl p-12 text-center">
      <i class="pi pi-video text-5xl text-gray-600 mb-4 block"></i>
      <h3 class="text-white font-semibold text-lg mb-1">Доступных видео пока нет</h3>
      <p class="text-gray-500 text-sm mb-6">Оформите подписку на интересующие вас материалы на главной странице.</p>
      <a href="/teams/video" class="inline-block bg-red-600 hover:bg-red-700 text-white font-medium text-sm py-2.5 px-5 rounded-lg transition">
        Перейти к каталогу
      </a>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
      <div 
        v-for="sub in dataMyVideo" 
        :key="sub.id" 
        class="video-card bg-[#18181B] border border-gray-800 rounded-2xl overflow-hidden flex flex-col hover:border-gray-700 transition-all duration-300 group shadow-lg"
      >
        <div class="relative aspect-video bg-gray-900 flex items-center justify-center overflow-hidden cursor-pointer" @click="openVideo(sub.videoLink)">
          <div class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent z-10"></div>
          
          <img 
            v-if="sub.videoLink"
            :src="`https://i.ytimg.com/vi/${sub.videoLink}/mqdefault.jpg`" 
            alt="Превью видео"
            class="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
          <i v-else class="pi pi-video text-4xl text-gray-700 group-hover:scale-110 transition-transform duration-300"></i>
          
          <div class="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity duration-300 z-20">
            <div class="w-12 h-12 bg-red-600 rounded-full flex items-center justify-center text-white shadow-xl transform scale-75 group-hover:scale-100 transition-transform duration-300">
              <i class="pi pi-play text-lg ml-1"></i>
            </div>
          </div>

          <div class="absolute bottom-3 left-3 bg-black/70 backdrop-blur-sm text-xs px-2.5 py-1 rounded-md border border-white/10 z-20 flex items-center gap-1.5"
               :class="calculateDaysLeft(sub.expiresAt) <= 3 ? 'text-orange-400 border-orange-500/20' : 'text-emerald-400 border-emerald-500/20'">
            <span class="w-1.5 h-1.5 rounded-full" :class="calculateDaysLeft(sub.expiresAt) <= 3 ? 'bg-orange-400' : 'bg-emerald-400'"></span>
            Доступ: {{ calculateDaysLeft(sub.expiresAt) }} дн.
          </div>
        </div>

        <div class="p-5 flex flex-col flex-1">
          <h3 class="text-white font-bold text-base line-clamp-1 mb-2 group-hover:text-red-500 transition-colors cursor-pointer" @click="openVideo(sub.videoLink)">
            {{ sub.videoTitle || `Материал команды DAR Team` }}
          </h3>
          
          <p class="text-gray-400 text-xs line-clamp-2 mb-5 flex-1 leading-relaxed">
            {{ sub.description || 'Вам открыт эксклюзивный доступ к разбору техники от тренерского состава.' }}
          </p>

          <div class="pt-4 border-t border-gray-800 flex items-center justify-between mt-auto">
            <span class="text-[11px] text-gray-500">
              До {{ new Date(sub.expiresAt).toLocaleDateString() }}
            </span>
            <button 
              @click="openVideo(sub.videoLink)"
              class="text-xs bg-gray-800 hover:bg-red-600 text-gray-200 hover:text-white px-4 py-2 rounded-lg font-medium transition-all duration-200 flex items-center gap-2"
            >
              <i class="pi pi-play text-[10px]"></i> Смотреть
            </button>
          </div>
        </div>
      </div>
    </div>

    <div 
      v-if="isPlayerOpen" 
      class="fixed inset-0 bg-black/90 backdrop-blur-md z-50 flex items-center justify-center p-4 transition-all duration-300"
      @click.self="closePlayer"
    >
      <div class="relative w-full max-w-4xl bg-[#111] rounded-2xl overflow-hidden border border-gray-800 shadow-2xl animate-fade-in">
        
        <button 
          @click="closePlayer" 
          class="absolute top-4 right-4 z-50 bg-black/60 hover:bg-red-600 text-white w-10 h-10 rounded-full flex items-center justify-center transition shadow-lg border border-white/10"
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

/* Плавное появление модалки */
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