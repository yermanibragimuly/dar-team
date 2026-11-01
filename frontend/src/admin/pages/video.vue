<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { showError, showSuccess } from '@/shared/lib/toastService'
import { useConfirmDialog } from '@/shared/lib/useConfirm'
import { renderAppleEmoji } from '@/shared/lib/appleEmoji'
import { getAllVideos, postVideos, putIdVideos, deletIdVideos } from '@/services/video-services'

const { show } = useConfirmDialog()
const dialogVisible = ref(false)
const videos = ref<any[]>([])
const loading = ref(false)

const videoForm = ref({
  title: '',
  description: '',
  youtubeVideoId: ''
})

const isEditing = ref(false)
const currentVideoId = ref<number | null>(null)

const currentPage = ref(1)
const itemsPerPage = ref(10)
const searchQuery = ref('')

const fetchVideos = async () => {
  loading.value = true
  try {
    const res = await getAllVideos()
    videos.value = res.data || []
  } catch (err) {
    showError(err)
  } finally {
    loading.value = false
  }
}

const filteredVideos = computed(() => {
  const q = searchQuery.value.toLowerCase()
  return videos.value.filter(v => {
    return v.title?.toLowerCase().includes(q) ||
           v.description?.toLowerCase().includes(q) ||
           v.youtubeVideoId?.toLowerCase().includes(q)
  })
})

const totalPages = computed(() => Math.ceil(filteredVideos.value.length / itemsPerPage.value) || 1)

const paginatedVideos = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredVideos.value.slice(start, end)
})

const changePage = (page: number) => {
  currentPage.value = page
}

const openNew = () => {
  videoForm.value = {
    title: '',
    description: '',
    youtubeVideoId: ''
  }
  isEditing.value = false
  currentVideoId.value = null
  dialogVisible.value = true
}

const editVideo = (data: any) => {
  isEditing.value = true
  currentVideoId.value = data.id
  videoForm.value = { ...data }
  dialogVisible.value = true
}

const saveVideo = async () => {
  loading.value = true
  try {
    if (isEditing.value && currentVideoId.value) {
      await putIdVideos(currentVideoId.value, videoForm.value)
      showSuccess('Видео успешно обновлено')
    } else {
      await postVideos(videoForm.value)
      showSuccess('Видео успешно создано')
    }
    await fetchVideos()
    dialogVisible.value = false
  } catch (err) {
    showError(err)
  } finally {
    loading.value = false
  }
}

const deleteVideo = async (data: any) => {
  const confirmed = await show({ 
    title: 'Удаление видео',
    message: `Вы точно хотите удалить видео "${data.title}"?`,
    confirmText: 'Да, удалить'
  })
  if (!confirmed) return

  try {
    await deletIdVideos(data.id)
    showSuccess('Успешно удалено')
    await fetchVideos()
  } catch (err) {
    showError(err)
  }
}

onMounted(() => {
  fetchVideos()
})
</script>

<template>
  <div class="max-w-7xl mx-auto space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white tracking-tight">Управление видео контентом</h1>
        <p class="text-sm text-[#9CA3AF] mt-1">Реестр видеоматериалов и трансляций DAR TEAM</p>
      </div>
      <button
        @click="openNew"
        class="bg-[#B00D15] hover:bg-[#9B0B12] text-white text-sm font-semibold px-4 py-2.5 rounded-lg flex items-center gap-2 shadow-lg shadow-[#B00D15]/20 transition-all cursor-pointer"
      >
        <i class="pi pi-plus"></i>
        Добавить видео
      </button>
    </div>

    <!-- Поиск -->
    <div class="flex items-center justify-end">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Поиск видео..."
        class="bg-[#1f1f22] border border-[#3F3F46] rounded-lg px-3 py-2 text-sm text-white placeholder-[#71717A] focus:outline-none focus:border-[#B00D15] w-64"
      />
    </div>

    <!-- Table Container -->
    <div class="rounded-xl border border-[#292930] bg-[#131316] overflow-hidden shadow-xl">
      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm">
          <thead class="bg-[#1f1f22] text-[#9CA3AF] uppercase text-xs font-semibold border-b border-[#292930]">
            <tr>
              <th class="py-3.5 px-4">Заглавие</th>
              <th class="py-3.5 px-4">Описание</th>
              <th class="py-3.5 px-4">YouTube ID</th>
              <th class="py-3.5 px-4 text-right pr-6">Действия</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-[#292930]">
            <tr v-for="item in paginatedVideos" :key="item.id" class="group hover:bg-[#1f1f22]/50 transition-colors">
              
              <!-- Заглавие с Apple Emoji -->
              <td class="py-3.5 px-4">
                <span 
                  class="font-medium text-white group-hover:text-[#B00D15] transition-colors line-clamp-1"
                  v-html="renderAppleEmoji(item.title)"
                ></span>
              </td>

              <!-- Описание с Apple Emoji -->
              <td class="py-3.5 px-4 text-[#A1A1AA]">
                <span 
                  class="line-clamp-1"
                  v-html="renderAppleEmoji(item.description) || '—'"
                ></span>
              </td>

              <td class="py-3.5 px-4 font-mono text-xs text-[#B00D15]">
                <a 
                  :href="`https://www.youtube.com/watch?v=${item.youtubeVideoId}`" 
                  target="_blank" 
                  class="hover:underline inline-flex items-center gap-1.5"
                >
                  <i class="pi pi-youtube text-sm"></i>
                  {{ item.youtubeVideoId }}
                </a>
              </td>
              <td class="py-3.5 px-4 text-right pr-6">
                <div class="inline-flex items-center gap-1.5 justify-end">
                  <button
                    @click="editVideo(item)"
                    class="w-8 h-8 rounded-lg bg-[#1f1f22] text-[#A1A1AA] hover:text-white hover:bg-[#2a2a2d] flex items-center justify-center transition-all cursor-pointer"
                    title="Редактировать"
                  >
                    <i class="pi pi-pencil text-xs"></i>
                  </button>
                  <button
                    @click="deleteVideo(item)"
                    class="w-8 h-8 rounded-lg bg-[#B00D15]/20 text-[#B00D15] hover:bg-[#B00D15] hover:text-white flex items-center justify-center transition-all cursor-pointer"
                    title="Удалить"
                  >
                    <i class="pi pi-trash text-xs"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="paginatedVideos.length === 0">
              <td colspan="4" class="py-8 text-center text-[#71717A]">
                Видео не найдены
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Пагинация -->
      <div class="bg-[#1f1f22] p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <span class="text-xs text-[#71717A] uppercase tracking-wider">
          Показано <span class="text-white font-bold">{{ paginatedVideos.length }}</span> из <span class="text-white font-bold">{{ filteredVideos.length }}</span> видео
        </span>

        <div class="flex items-center gap-2" v-if="totalPages > 1">
          <button
            v-for="page in totalPages"
            :key="page"
            @click="changePage(page)"
            :class="[
              'w-8 h-8 rounded-lg font-bold text-xs transition-all cursor-pointer',
              currentPage === page
                ? 'bg-[#B00D15] text-white shadow-[0_0_12px_rgba(176,13,21,0.3)]'
                : 'bg-[#2a2a2d] text-[#A1A1AA] hover:text-white'
            ]"
          >
            {{ page }}
          </button>
        </div>
      </div>
    </div>

    <!-- Модальное окно редактирования -->
    <Teleport to="body">
      <div v-if="dialogVisible" class="fixed inset-0 z-[100] bg-black/80 backdrop-blur-md flex items-center justify-center p-4" @click.self="dialogVisible = false">
        <div class="bg-[#131316] border border-[#3F3F46] text-zinc-200 w-full max-w-lg rounded-xl shadow-2xl relative flex flex-col">
          <div class="flex items-center justify-between px-6 py-4 border-b border-[#292930] shrink-0 bg-[#111115]">
            <h2 class="text-base font-bold text-white tracking-wide uppercase">
              {{ isEditing ? 'Редактировать запись' : 'Добавить видео' }}
            </h2>
            <button @click="dialogVisible = false" class="w-8 h-8 rounded-lg flex items-center justify-center text-[#A1A1AA] hover:text-white hover:bg-[#2a2a2d] transition-colors cursor-pointer">
              <i class="pi pi-times text-sm"></i>
            </button>
          </div>

          <form @submit.prevent="saveVideo" class="p-6 space-y-4 text-sm">
            <div>
              <label class="block text-[#E5E7EB] font-semibold mb-1.5">Заглавие</label>
              <input v-model="videoForm.title" required class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm" />
              <!-- Интерактивный iOS предпросмотр -->
              <div v-if="videoForm.title" class="mt-1.5 px-3 py-1.5 bg-[#18181D] border border-zinc-800 rounded-lg text-xs text-white flex items-center gap-2">
                <span class="text-[9px] font-bold uppercase tracking-wider text-emerald-400 bg-emerald-950/60 border border-emerald-800/50 px-1.5 py-0.2 rounded shrink-0">iOS Вид</span>
                <span class="font-medium truncate" v-html="renderAppleEmoji(videoForm.title)"></span>
              </div>
            </div>

            <div>
              <label class="block text-[#E5E7EB] font-semibold mb-1.5">Описание</label>
              <textarea v-model="videoForm.description" rows="3" class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm resize-none"></textarea>
              <!-- Интерактивный iOS предпросмотр -->
              <div v-if="videoForm.description" class="mt-1.5 px-3 py-1.5 bg-[#18181D] border border-zinc-800 rounded-lg text-xs text-zinc-300 space-y-1">
                <span class="text-[9px] font-bold uppercase tracking-wider text-emerald-400 bg-emerald-950/60 border border-emerald-800/50 px-1.5 py-0.2 rounded shrink-0 block w-fit">iOS Вид</span>
                <div class="leading-relaxed whitespace-pre-wrap" v-html="renderAppleEmoji(videoForm.description)"></div>
              </div>
            </div>

            <div>
              <label class="block text-[#E5E7EB] font-semibold mb-1.5">YouTube Video ID</label>
              <input v-model="videoForm.youtubeVideoId" placeholder="Например: XDkgduCBxIY" class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm font-mono" />
            </div>

            <div class="flex items-center justify-between pt-4 border-t border-[#292930]">
              <button type="button" @click="dialogVisible = false" class="bg-[#1f1f22] hover:bg-[#2a2a2d] text-[#A1A1AA] hover:text-white border border-[#3F3F46] px-4 py-2 rounded-lg font-semibold text-sm transition-colors cursor-pointer">
                Отмена
              </button>
              <button type="submit" :disabled="loading" class="bg-[#B00D15] hover:bg-[#9B0B12] text-white px-6 py-2 rounded-lg font-semibold text-sm shadow-lg shadow-[#B00D15]/20 transition-all cursor-pointer">
                {{ loading ? 'Сохранение...' : 'Сохранить' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: #0e0e11;
}
::-webkit-scrollbar-thumb {
  background: #2a2a32;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #3e3e48;
}
</style>