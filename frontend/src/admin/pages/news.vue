<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { showError, showSuccess } from '@/shared/lib/toastService'
import { useConfirmDialog } from '@/shared/lib/useConfirm'
import { renderAppleEmoji } from '@/shared/lib/appleEmoji'
import { 
  getAllNews, 
  sendNews, 
  deleteNewsId, 
  updateNewsId, 
  deleteNewsImgId, 
  sendImgMinio 
} from '@/services/news-services'

const { show } = useConfirmDialog()

const newsList = ref<any[]>([])
const dialogVisible = ref(false)
const isEditing = ref(false)
const currentId = ref<number | null>(null)
const loading = ref(false)

const currentPage = ref(1)
const itemsPerPage = ref(10)
const activeTab = ref<'ru' | 'en' | 'kz'>('ru')

const news = ref({
  id: null as number | null,
  title: '',
  titleEn: '',
  titleKz: '',
  content: '',
  contentEn: '',
  contentKz: '',
  img: '',
  date: null as string | Date | null
})

const searchQuery = ref('')

const fetchNews = async () => {
  loading.value = true
  try {
    const res = await getAllNews(0, 100)
    newsList.value = res.content || res.data || []
  } catch (err) {
    showError(err)
  } finally {
    loading.value = false
  }
}

const filteredNews = computed(() => {
  return newsList.value.filter(n => {
    const q = searchQuery.value.toLowerCase()
    return n.title?.toLowerCase().includes(q) ||
           n.content?.toLowerCase().includes(q)
  })
})

const totalPages = computed(() => Math.ceil(filteredNews.value.length / itemsPerPage.value) || 1)

const paginatedNews = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredNews.value.slice(start, end)
})

const changePage = (page: number) => {
  currentPage.value = page
}

const openNew = () => {
  isEditing.value = false
  currentId.value = null
  news.value = {
    id: null,
    title: '',
    titleEn: '',
    titleKz: '',
    content: '',
    contentEn: '',
    contentKz: '',
    img: '',
    date: new Date().toISOString().split('T')[0]
  }
  activeTab.value = 'ru'
  dialogVisible.value = true
}

const editNews = (data: any) => {
  isEditing.value = true
  currentId.value = data.id
  news.value = { ...data }
  activeTab.value = 'ru'
  dialogVisible.value = true
}

const formatDateLocal = (date: any) => {
  if (!date) return ''
  if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
    return date
  }
  const d = new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const saveNews = async () => {
  loading.value = true
  const payload = {
    ...news.value,
    date: news.value.date ? formatDateLocal(news.value.date) : null
  }
  try {
    if (isEditing.value && currentId.value) {
      await updateNewsId(currentId.value, payload)
      showSuccess('Новость успешно обновлена')
    } else {
      await sendNews(payload)
      showSuccess('Новость успешно создана')
    }
    await fetchNews()
    dialogVisible.value = false
  } catch (err) {
    showError(err)
  } finally {
    loading.value = false
  }
}

const deleteNews = async (data: any) => {
  const confirmed = await show({ 
    title: 'Удаление новости',
    message: `Вы точно хотите удалить новость "${data.title}"?`,
    confirmText: 'Да, удалить'
  })
  if (!confirmed) return

  try {
    await deleteNewsId(data.id)
    showSuccess('Успешно удалено')
    await fetchNews()
  } catch (err) {
    showError(err)
  }
}

const uploadImage = async (event: any) => {
  const file = event.target.files?.[0]
  if (!file) return

  try {
    const res = await sendImgMinio(file, 'news')
    news.value.img = res.data
    showSuccess('Фото загружено!')
  } catch (err) {
    showError(err)
  }
}

const deleteImg = async () => {
  try {
    if (currentId.value) {
      await deleteNewsImgId(currentId.value)
      showSuccess('Успешно удалено фото')
    }
    news.value.img = ''
    await fetchNews()
  } catch (err) {
    showError(err)
  }
}

onMounted(() => {
  fetchNews()
})
</script>

<template>
  <div class="max-w-7xl mx-auto space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white tracking-tight">Управление новостями</h1>
        <p class="text-sm text-[#9CA3AF] mt-1">Публикация и редактирование новостей DAR TEAM</p>
      </div>
      <button
        @click="openNew"
        class="bg-[#B00D15] hover:bg-[#9B0B12] text-white text-sm font-semibold px-4 py-2.5 rounded-lg flex items-center gap-2 shadow-lg shadow-[#B00D15]/20 transition-all cursor-pointer"
      >
        <i class="pi pi-plus"></i>
        Добавить новость
      </button>
    </div>

    <!-- Поиск -->
    <div class="flex items-center justify-end">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Поиск новостей..."
        class="bg-[#1f1f22] border border-[#3F3F46] rounded-lg px-3 py-2 text-sm text-white placeholder-[#71717A] focus:outline-none focus:border-[#B00D15] w-64"
      />
    </div>

    <!-- Table -->
    <div class="rounded-xl border border-[#292930] bg-[#131316] overflow-hidden shadow-xl">
      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm">
          <thead class="bg-[#1f1f22] text-[#9CA3AF] uppercase text-xs font-semibold border-b border-[#292930]">
            <tr>
              <th class="py-3.5 px-4 w-16 text-center">Превью</th>
              <th class="py-3.5 px-4">Заголовок</th>
              <th class="py-3.5 px-4">Дата публикации</th>
              <th class="py-3.5 px-4 text-right pr-6">Действия</th>
            </tr>
          </thead>

          <tbody class="divide-y divide-[#292930]">
            <tr
              v-for="item in paginatedNews"
              :key="item.id"
              class="group hover:bg-[#1f1f22]/50 transition-colors"
            >
              <td class="py-3.5 px-4">
                <div class="w-16 h-11 rounded-lg overflow-hidden bg-[#1f1f22] shrink-0 shadow-sm border border-[#292930]">
                  <img
                    v-if="item.img"
                    :src="item.img"
                    :alt="item.title"
                    class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  />
                  <div v-else class="w-full h-full flex items-center justify-center text-[#71717A] text-xs">
                    <i class="pi pi-image text-lg"></i>
                  </div>
                </div>
              </td>

              <!-- Заголовок с Apple iOS эмодзи -->
              <td class="py-3.5 px-4">
                <span 
                  class="font-medium text-white group-hover:text-[#B00D15] transition-colors line-clamp-1"
                  v-html="renderAppleEmoji(item.title)"
                ></span>
              </td>

              <td class="py-3.5 px-4 text-[#A1A1AA]">
                <div class="flex items-center gap-1.5 font-mono text-xs">
                  <i class="pi pi-calendar text-[12px] text-[#71717A]"></i>
                  {{ item.date || '—' }}
                </div>
              </td>
              <td class="py-3.5 px-4 text-right pr-6">
                <div class="inline-flex items-center gap-1.5 justify-end">
                  <button
                    @click="editNews(item)"
                    class="w-8 h-8 rounded-lg bg-[#1f1f22] text-[#A1A1AA] hover:text-white hover:bg-[#2a2a2d] flex items-center justify-center transition-all cursor-pointer"
                    title="Редактировать"
                  >
                    <i class="pi pi-pencil text-xs"></i>
                  </button>
                  <button
                    @click="deleteNews(item)"
                    class="w-8 h-8 rounded-lg bg-[#B00D15]/20 text-[#B00D15] hover:bg-[#B00D15] hover:text-white flex items-center justify-center transition-all cursor-pointer"
                    title="Удалить"
                  >
                    <i class="pi pi-trash text-xs"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="paginatedNews.length === 0">
              <td colspan="4" class="py-8 text-center text-[#71717A]">
                Новости не найдены
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Пагинация -->
      <div class="bg-[#1f1f22] p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <span class="text-xs text-[#71717A] uppercase tracking-wider">
          Показано <span class="text-white font-bold">{{ paginatedNews.length }}</span> из <span class="text-white font-bold">{{ filteredNews.length }}</span> новостей
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
  </div>

  <!-- Модальное окно редактирования -->
  <Teleport to="body">
    <div
      v-if="dialogVisible"
      class="fixed inset-0 z-[100] bg-black/80 backdrop-blur-md flex items-center justify-center p-4"
      @click.self="dialogVisible = false"
    >
      <div class="bg-[#131316] border border-[#3F3F46] text-zinc-200 w-full max-w-3xl rounded-xl shadow-2xl relative max-h-[92vh] overflow-y-auto flex flex-col">
        <!-- Шапка -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-[#292930] shrink-0 bg-[#111115]">
          <h2 class="text-base font-bold text-white tracking-wide uppercase">
            {{ isEditing ? 'Редактировать новость' : 'Добавить новость' }}
          </h2>
          <button
            type="button"
            @click="dialogVisible = false"
            class="w-8 h-8 rounded-lg flex items-center justify-center text-[#A1A1AA] hover:text-white hover:bg-[#2a2a2d] transition-colors cursor-pointer"
          >
            <i class="pi pi-times text-sm"></i>
          </button>
        </div>

        <!-- Тело формы -->
        <div class="p-6 space-y-5 text-sm overflow-y-auto">
          <!-- Фото -->
          <div class="flex flex-col sm:flex-row gap-4 items-center bg-[#0e0e11] p-4 rounded-xl border border-[#292930]">
            <div class="w-full sm:w-48 aspect-video rounded-lg overflow-hidden bg-[#1f1f22] shrink-0 shadow-inner relative flex items-center justify-center border border-[#292930]">
              <img
                v-if="news.img"
                :src="news.img"
                alt="preview"
                class="w-full h-full object-cover"
              />
              <span v-else class="text-[#71717A] text-xs">Нет изображения</span>
            </div>

            <div class="flex flex-col gap-2 w-full">
              <label class="bg-[#B00D15] hover:bg-[#9B0B12] text-white px-4 py-2 rounded-lg font-semibold text-xs flex items-center justify-center gap-2 shadow-md shadow-[#B00D15]/20 transition-colors cursor-pointer w-fit">
                <i class="pi pi-plus text-xs"></i>
                Загрузить фото
                <input type="file" accept="image/*" class="hidden" @change="uploadImage" />
              </label>
              <button
                v-if="news.img"
                type="button"
                @click="deleteImg"
                class="bg-[#1f1f22] hover:bg-[#2a2a2d] text-[#A1A1AA] hover:text-white border border-[#3F3F46] px-3 py-1.5 rounded-lg font-semibold text-xs flex items-center justify-center gap-2 transition-colors cursor-pointer w-fit"
              >
                <i class="pi pi-minus text-xs text-[#B00D15]"></i>
                Удалить фото
              </button>
            </div>
          </div>

          <!-- Дата публикации -->
          <div>
            <label class="block text-[#E5E7EB] font-semibold mb-1.5">Дата публикации</label>
            <input
              v-model="news.date"
              type="date"
              class="w-full sm:w-64 bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm"
            />
          </div>

          <!-- Вкладки языков -->
          <div class="space-y-3">
            <div class="flex items-center gap-2 border-b border-[#292930] pb-2">
              <button
                type="button"
                @click="activeTab = 'ru'"
                :class="[
                  'px-3 py-1.5 rounded-lg font-bold text-xs uppercase tracking-wider transition-all cursor-pointer',
                  activeTab === 'ru' ? 'bg-[#B00D15] text-white' : 'bg-[#1f1f22] text-[#A1A1AA] hover:text-white'
                ]"
              >
                Русский
              </button>
              <button
                type="button"
                @click="activeTab = 'en'"
                :class="[
                  'px-3 py-1.5 rounded-lg font-bold text-xs uppercase tracking-wider transition-all cursor-pointer',
                  activeTab === 'en' ? 'bg-[#B00D15] text-white' : 'bg-[#1f1f22] text-[#A1A1AA] hover:text-white'
                ]"
              >
                English
              </button>
              <button
                type="button"
                @click="activeTab = 'kz'"
                :class="[
                  'px-3 py-1.5 rounded-lg font-bold text-xs uppercase tracking-wider transition-all cursor-pointer',
                  activeTab === 'kz' ? 'bg-[#B00D15] text-white' : 'bg-[#1f1f22] text-[#A1A1AA] hover:text-white'
                ]"
              >
                Қазақша
              </button>
            </div>

            <!-- RU -->
            <div v-show="activeTab === 'ru'" class="space-y-3">
              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1">Заголовок (RU)</label>
                <input
                  v-model="news.title"
                  type="text"
                  placeholder="Введите заголовок..."
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm"
                />
                <!-- Интерактивный iOS предпросмотр -->
                <div v-if="news.title" class="mt-1.5 px-3 py-1.5 bg-[#18181D] border border-zinc-800 rounded-lg text-xs text-white flex items-center gap-2">
                  <span class="text-[9px] font-bold uppercase tracking-wider text-emerald-400 bg-emerald-950/60 border border-emerald-800/50 px-1.5 py-0.2 rounded shrink-0">iOS Вид</span>
                  <span class="font-medium truncate" v-html="renderAppleEmoji(news.title)"></span>
                </div>
              </div>

              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1">Содержание (RU)</label>
                <textarea
                  v-model="news.content"
                  rows="4"
                  placeholder="Введите текст новости..."
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm resize-none"
                ></textarea>
                <!-- Интерактивный iOS предпросмотр -->
                <div v-if="news.content" class="mt-1.5 px-3 py-2 bg-[#18181D] border border-zinc-800 rounded-lg text-xs text-zinc-300 space-y-1 max-h-24 overflow-y-auto">
                  <span class="text-[9px] font-bold uppercase tracking-wider text-emerald-400 bg-emerald-950/60 border border-emerald-800/50 px-1.5 py-0.2 rounded shrink-0 block w-fit">iOS Вид</span>
                  <div class="leading-relaxed whitespace-pre-wrap" v-html="renderAppleEmoji(news.content)"></div>
                </div>
              </div>
            </div>

            <!-- EN -->
            <div v-show="activeTab === 'en'" class="space-y-3">
              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1">Title (EN)</label>
                <input
                  v-model="news.titleEn"
                  type="text"
                  placeholder="Enter title..."
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm"
                />
                <div v-if="news.titleEn" class="mt-1.5 px-3 py-1.5 bg-[#18181D] border border-zinc-800 rounded-lg text-xs text-white flex items-center gap-2">
                  <span class="text-[9px] font-bold uppercase tracking-wider text-emerald-400 bg-emerald-950/60 border border-emerald-800/50 px-1.5 py-0.2 rounded shrink-0">iOS Preview</span>
                  <span class="font-medium truncate" v-html="renderAppleEmoji(news.titleEn)"></span>
                </div>
              </div>

              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1">Content (EN)</label>
                <textarea
                  v-model="news.contentEn"
                  rows="4"
                  placeholder="Enter news text..."
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm resize-none"
                ></textarea>
                <div v-if="news.contentEn" class="mt-1.5 px-3 py-2 bg-[#18181D] border border-zinc-800 rounded-lg text-xs text-zinc-300 space-y-1 max-h-24 overflow-y-auto">
                  <span class="text-[9px] font-bold uppercase tracking-wider text-emerald-400 bg-emerald-950/60 border border-emerald-800/50 px-1.5 py-0.2 rounded shrink-0 block w-fit">iOS Preview</span>
                  <div class="leading-relaxed whitespace-pre-wrap" v-html="renderAppleEmoji(news.contentEn)"></div>
                </div>
              </div>
            </div>

            <!-- KZ -->
            <div v-show="activeTab === 'kz'" class="space-y-3">
              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1">Тақырыбы (KZ)</label>
                <input
                  v-model="news.titleKz"
                  type="text"
                  placeholder="Тақырыпты енгізіңіз..."
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm"
                />
                <div v-if="news.titleKz" class="mt-1.5 px-3 py-1.5 bg-[#18181D] border border-zinc-800 rounded-lg text-xs text-white flex items-center gap-2">
                  <span class="text-[9px] font-bold uppercase tracking-wider text-emerald-400 bg-emerald-950/60 border border-emerald-800/50 px-1.5 py-0.2 rounded shrink-0">iOS Көрініс</span>
                  <span class="font-medium truncate" v-html="renderAppleEmoji(news.titleKz)"></span>
                </div>
              </div>

              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1">Мазмұны (KZ)</label>
                <textarea
                  v-model="news.contentKz"
                  rows="4"
                  placeholder="Жаңалық мәнін енгізіңіз..."
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm resize-none"
                ></textarea>
                <div v-if="news.contentKz" class="mt-1.5 px-3 py-2 bg-[#18181D] border border-zinc-800 rounded-lg text-xs text-zinc-300 space-y-1 max-h-24 overflow-y-auto">
                  <span class="text-[9px] font-bold uppercase tracking-wider text-emerald-400 bg-emerald-950/60 border border-emerald-800/50 px-1.5 py-0.2 rounded shrink-0 block w-fit">iOS Көрініс</span>
                  <div class="leading-relaxed whitespace-pre-wrap" v-html="renderAppleEmoji(news.contentKz)"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Подвал -->
        <div class="flex items-center justify-between px-6 py-4 border-t border-[#292930] bg-[#111115] shrink-0">
          <button
            type="button"
            @click="dialogVisible = false"
            class="bg-[#1f1f22] hover:bg-[#2a2a2d] text-[#A1A1AA] hover:text-white border border-[#3F3F46] px-4 py-2 rounded-lg font-semibold text-sm transition-colors cursor-pointer"
          >
            Отмена
          </button>
          <button
            type="button"
            @click="saveNews"
            :disabled="loading"
            class="bg-[#B00D15] hover:bg-[#9B0B12] text-white px-6 py-2 rounded-lg font-semibold text-sm flex items-center gap-2 shadow-lg shadow-[#B00D15]/20 transition-all disabled:opacity-50 cursor-pointer"
          >
            <i class="pi pi-check text-xs"></i>
            {{ loading ? 'Сохранение...' : 'Сохранить' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
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