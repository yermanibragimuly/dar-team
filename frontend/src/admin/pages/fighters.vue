<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import draggable from 'vuedraggable'
import { 
  getAllFighters, 
  senFighters, 
  deleteFighterId, 
  updateFighterId, 
  deleteAchievementId, 
  deleteFighterImgId, 
  sendImgMinio,
  postUpdateTableAllFighters 
} from '@/services/fighter-services'
import { showError, showSuccess } from '@/shared/lib/toastService'
import { useConfirmDialog } from '@/shared/lib/useConfirm'

const { show } = useConfirmDialog()

const fighters = ref<any[]>([])
const dialogVisible = ref(false)
const editMode = ref(false)
const currentId = ref<number | null>(null)
const loading = ref(false)

// Пагинация: По 10 бойцов на страницу
const currentPage = ref(1)
const itemsPerPage = ref(10)

const fighter = ref({
  name: '',
  nickname: '',
  birthplace: '',
  weightClass: '',
  record: '',
  tko: 0,
  solution: 0,
  submissive: 0,
  other: 0,
  country: '',
  sport: '',
  rank: '',
  instagram: '',
  img: '',
  achievements: [] as Array<{ title: string }>
})

const searchQuery = ref('')

const fetchFighters = async () => {
  loading.value = true
  try {
    const res = await getAllFighters()
    fighters.value = res.data
  } catch (err) {
    showError(err)
  } finally {
    loading.value = false
  }
}

const onDragEnd = async () => {
  try {
    const ids = fighters.value.map(f => f.id)
    await postUpdateTableAllFighters(ids)
    showSuccess('Порядок сохранен!')
  } catch (err) {
    showError('Не удалось сохранить порядок')
  }
}

// Поиск
const filteredFighters = computed(() => {
  return fighters.value.filter(f => {
    return f.name?.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
           f.nickname?.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
           f.country?.toLowerCase().includes(searchQuery.value.toLowerCase())
  })
})

// Настоящий подсчет количества страниц
const totalPages = computed(() => Math.ceil(filteredFighters.value.length / itemsPerPage.value) || 1)

// Настоящий срез по 10 элементов
const paginatedFighters = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredFighters.value.slice(start, end)
})

const changePage = (page: number) => {
  currentPage.value = page
}

const openDialog = (data?: any) => {
  if (data) {
    editMode.value = true
    currentId.value = data.id
    fighter.value = {
      ...data,
      tko: Number(data.tko) || 0,
      solution: Number(data.solution) || 0,
      submissive: Number(data.submissive) || 0,
      other: Number(data.other) || 0,
      achievements: data.achievements ? [...data.achievements] : []
    }
  } else {
    editMode.value = false
    currentId.value = null
    fighter.value = {
      name: '',
      nickname: '',
      birthplace: '',
      weightClass: '',
      record: '',
      tko: 0,
      solution: 0,
      submissive: 0,
      other: 0,
      country: '',
      sport: '',
      rank: '',
      instagram: '',
      img: '',
      achievements: []
    }
  }
  dialogVisible.value = true
}

const saveFighter = async () => {
  loading.value = true
  try {
    if (editMode.value && currentId.value) {
      await updateFighterId(currentId.value, fighter.value)
      showSuccess('Боец успешно обновлен')
    } else {
      await senFighters(fighter.value)
      showSuccess('Боец успешно создан')
    }
    await fetchFighters()
    dialogVisible.value = false
  } catch (err) {
    showError(err)
  } finally {
    loading.value = false
  }
}

const deleteFighter = async (data: any) => {
  const confirmed = await show({ message: `Вы точно хотите удалить ${data.name}?` })
  if (!confirmed) return

  try {
    await deleteFighterId(data.id)
    showSuccess('Успешно удалено')
    await fetchFighters()
  } catch (err) {
    showError(err)
  }
}

const removeAchievement = async (index: number) => {
  if (!currentId.value) return
  try {
    await deleteAchievementId(String(currentId.value), String(index))
    showSuccess('Успешно удалено')
    await fetchFighters()
    const updated = fighters.value.find(f => f.id === currentId.value)
    if (updated) {
      fighter.value.achievements = updated.achievements || []
    }
  } catch (err) {
    showError(err)
  }
}

const addAchievement = () => {
  fighter.value.achievements.push({ title: '' })
}

const uploadImage = async (event: any) => {
  const file = event.target.files?.[0]
  if (!file) return

  try {
    const res = await sendImgMinio(file, 'fighter')
    fighter.value.img = res.data
    showSuccess('Фото загружено!')
  } catch (err) {
    showError(err)
  }
}

const deleteImg = async () => {
  if (!currentId.value) return
  try {
    await deleteFighterImgId(currentId.value)
    showSuccess('Фото удалено')
    fighter.value.img = ''
  } catch (err) {
    showError(err)
  }
}

onMounted(() => {
  fetchFighters()
})
</script>

<template>
  <div class="max-w-7xl mx-auto space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white tracking-tight">Управление бойцами</h1>
        <p class="text-sm text-[#9CA3AF] mt-1">Реестр профессионального состава атлетов DAR TEAM</p>
      </div>
      <button
        @click="openDialog()"
        class="bg-[#B00D15] hover:bg-[#9B0B12] text-white text-sm font-semibold px-4 py-2.5 rounded-lg flex items-center gap-2 shadow-lg shadow-[#B00D15]/20 transition-all cursor-pointer"
      >
        <i class="pi pi-plus"></i>
        Добавить бойца
      </button>
    </div>

    <!-- Поиск -->
    <div class="flex items-center justify-end">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Поиск..."
        class="bg-[#1f1f22] border border-[#3F3F46] rounded-lg px-3 py-2 text-sm text-white placeholder-[#71717A] focus:outline-none focus:border-[#B00D15] w-64"
      />
    </div>

    <!-- Table -->
    <!-- Table -->
<div class="rounded-xl border border-[#292930] bg-[#131316] overflow-hidden shadow-xl">
  <div class="overflow-x-auto">
    <table class="w-full text-left text-sm">
      <thead class="bg-[#1f1f22] text-[#9CA3AF] uppercase text-xs font-semibold border-b border-[#292930]">
        <tr>
          <th class="py-3.5 px-4 w-12"></th>
          <th class="py-3.5 px-4">Боец / Атлет</th>
          <th class="py-3.5 px-4">Никнейм</th>
          <th class="py-3.5 px-4">Страна / Локация</th>
          <th class="py-3.5 px-4">Весовая категория</th>
          <th class="py-3.5 px-4">Рекорд / Лига</th>
          <th class="py-3.5 px-4 text-right pr-6">Действия</th>
        </tr>
      </thead>

      <!-- Обертка vuedraggable для tbody -->
      <draggable
        v-model="fighters"
        tag="tbody"
        item-key="id"
        handle=".drag-handle"
        animation="200"
        @end="onDragEnd"
        class="divide-y divide-[#292930]"
      >
        <template #item="{ element: fighterItem }">
          <tr class="group hover:bg-[#1f1f22]/50 transition-colors">
            <!-- За эту иконку берем и тянем -->
            <td class="py-3.5 px-4 text-center text-[#71717A] cursor-grab active:cursor-grabbing drag-handle">
              <i class="pi pi-bars text-base hover:text-white transition-colors"></i>
            </td>
            <td class="py-3.5 px-4">
              <div class="flex items-center gap-3">
                <div class="relative w-11 h-11 rounded-lg overflow-hidden bg-[#1f1f22] shrink-0 shadow-sm pointer-events-none select-none">
                  <img
                    v-if="fighterItem.img"
                    :src="fighterItem.img"
                    :alt="fighterItem.name"
                    draggable="false"
                    class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300 pointer-events-none"
                  />
                  <div v-else class="w-full h-full flex items-center justify-center text-[#71717A] font-extrabold text-sm">
                    {{ fighterItem.name?.slice(0, 2).toUpperCase() }}
                  </div>
                </div>
                <span class="font-medium text-white group-hover:text-[#B00D15] transition-colors truncate">
                  {{ fighterItem.name }}
                </span>
              </div>
            </td>
            <td class="py-3.5 px-4">
              <span v-if="fighterItem.nickname" class="px-2 py-0.5 rounded bg-[#1f1f22] text-white font-semibold text-xs">
                «{{ fighterItem.nickname }}»
              </span>
              <span v-else class="text-[#71717A]">—</span>
            </td>
            <td class="py-3.5 px-4 text-[#A1A1AA]">
              <div class="flex items-center gap-1.5">
                <i class="pi pi-map-marker text-[12px] text-[#71717A]"></i>
                {{ fighterItem.country || 'Казахстан' }}
              </div>
            </td>
            <td class="py-3.5 px-4">
              <span class="text-white font-medium">{{ fighterItem.weightClass || '—' }}</span>
            </td>
            <td class="py-3.5 px-4">
              <div class="flex items-center gap-2">
                <span class="px-2 py-1 rounded bg-[#2a2a2d] text-white font-bold text-xs tracking-wide">
                  {{ fighterItem.record || '—' }}
                </span>
              </div>
            </td>
            <td class="py-3.5 px-4 text-right pr-6">
              <div class="inline-flex items-center gap-1.5 justify-end">
                <button
                  @click="openDialog(fighterItem)"
                  class="w-8 h-8 rounded-lg bg-[#1f1f22] text-[#A1A1AA] hover:text-white hover:bg-[#2a2a2d] flex items-center justify-center transition-all cursor-pointer"
                  title="Редактировать"
                >
                  <i class="pi pi-pencil text-xs"></i>
                </button>
                <button
                  @click="deleteFighter(fighterItem)"
                  class="w-8 h-8 rounded-lg bg-[#B00D15]/20 text-[#B00D15] hover:bg-[#B00D15] hover:text-white flex items-center justify-center transition-all cursor-pointer"
                  title="Удалить"
                >
                  <i class="pi pi-trash text-xs"></i>
                </button>
              </div>
            </td>
          </tr>
        </template>
      </draggable>
    </table>
  </div>

      <!-- Исправленная динаминаческая пагинация -->
      <div class="bg-[#1f1f22] p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <span class="text-xs text-[#71717A] uppercase tracking-wider">
          Показано <span class="text-white font-bold">{{ paginatedFighters.length }}</span> из <span class="text-white font-bold">{{ filteredFighters.length }}</span> бойцов
        </span>

        <!-- Динамические кнопки 1, 2, 3... -->
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

  <!-- Modal -->
  <Teleport to="body">
    <div
      v-if="dialogVisible"
      class="fixed inset-0 z-[100] bg-black/80 backdrop-blur-md flex items-center justify-center p-4"
      @click.self="dialogVisible = false"
    >
      <div class="bg-[#131316] border border-[#3F3F46] text-zinc-200 w-full max-w-4xl rounded-xl shadow-2xl relative max-h-[92vh] overflow-y-auto flex flex-col">
        <div class="flex items-center justify-between px-6 py-4 border-b border-[#292930] shrink-0 bg-[#111115]">
          <h2 class="text-base font-bold text-white tracking-wide uppercase">
            {{ editMode ? 'Редактировать бойца' : 'Добавить бойца' }}
          </h2>
          <button
            type="button"
            @click="dialogVisible = false"
            class="w-8 h-8 rounded-lg flex items-center justify-center text-[#A1A1AA] hover:text-white hover:bg-[#2a2a2d] transition-colors cursor-pointer"
          >
            <i class="pi pi-times text-sm"></i>
          </button>
        </div>

        <div class="p-6 grid grid-cols-1 md:grid-cols-12 gap-6 overflow-y-auto text-sm">
          <div class="md:col-span-4 flex flex-col items-center space-y-4">
            <div class="w-full aspect-[3/4] max-w-[240px] rounded-lg overflow-hidden border border-[#292930] bg-[#0e0e11] relative shadow-lg">
              <img
                v-if="fighter.img"
                :src="fighter.img"
                :alt="fighter.name"
                class="w-full h-full object-cover object-top"
              />
              <div
                v-else
                class="w-full h-full flex items-center justify-center bg-gradient-to-b from-[#1f1f22] to-[#0e0e11]"
              >
                <span class="text-[#71717A] text-sm">Нет фото</span>
              </div>
            </div>
            <div class="flex flex-col w-full max-w-[240px] gap-2">
              <label class="w-full bg-[#B00D15] hover:bg-[#9B0B12] text-white px-3.5 py-2 rounded-lg font-semibold text-sm flex items-center justify-center gap-2 shadow-md shadow-[#B00D15]/20 transition-colors cursor-pointer">
                <i class="pi pi-plus text-xs"></i>
                Загрузить фото
                <input type="file" accept="image/*" class="hidden" @change="uploadImage" />
              </label>
              <button
                v-if="fighter.img"
                type="button"
                @click="deleteImg"
                class="w-full bg-[#1f1f22] hover:bg-[#2a2a2d] text-[#A1A1AA] hover:text-white border border-[#3F3F46] px-3.5 py-2 rounded-lg font-semibold text-sm flex items-center justify-center gap-2 transition-colors cursor-pointer"
              >
                <i class="pi pi-minus text-xs text-[#B00D15]"></i>
                Удалить фото
              </button>
            </div>
          </div>

          <div class="md:col-span-8 space-y-4">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1.5">Имя</label>
                <input
                  v-model="fighter.name"
                  type="text"
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm"
                />
              </div>
              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1.5">Никнейм</label>
                <input
                  v-model="fighter.nickname"
                  type="text"
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm"
                />
              </div>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1.5">Место рождения</label>
                <input
                  v-model="fighter.birthplace"
                  type="text"
                  placeholder="Город, страна"
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white placeholder-[#71717A] focus:outline-none focus:border-[#B00D15] text-sm"
                />
              </div>
              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1.5">Весовая категория</label>
                <input
                  v-model="fighter.weightClass"
                  type="text"
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm"
                />
              </div>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1.5">Страна</label>
                <input
                  v-model="fighter.country"
                  type="text"
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm"
                />
              </div>
              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1.5">Спорт</label>
                <input
                  v-model="fighter.sport"
                  type="text"
                  placeholder="ММА, Бокс"
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white placeholder-[#71717A] focus:outline-none focus:border-[#B00D15] text-sm"
                />
              </div>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1.5">Инстаграм</label>
                <input
                  v-model="fighter.instagram"
                  type="text"
                  placeholder="@username"
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white placeholder-[#71717A] focus:outline-none focus:border-[#B00D15] text-sm"
                />
              </div>
              <div>
                <label class="block text-[#E5E7EB] font-semibold mb-1.5">Ранг</label>
                <input
                  v-model="fighter.rank"
                  type="text"
                  class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm"
                />
              </div>
            </div>

            <div>
              <label class="block text-[#E5E7EB] font-semibold mb-1.5">Рекорд</label>
              <input
                v-model="fighter.record"
                type="text"
                placeholder="18-0-0"
                class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm font-mono font-semibold"
              />
            </div>

            <div class="border border-[#292930] rounded-xl p-3.5 bg-[#111115] space-y-3">
              <span class="font-bold text-white text-sm uppercase tracking-wide block pb-2 border-b border-[#292930]/60">
                Статистика Побед
              </span>
              <div class="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                <div>
                  <label class="block text-[#A1A1AA] font-medium mb-1 text-xs">KO/TKO</label>
                  <input
                    v-model.number="fighter.tko"
                    type="number"
                    min="0"
                    class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-2.5 py-1.5 text-white font-mono focus:outline-none focus:border-[#B00D15] text-sm"
                  />
                </div>
                <div>
                  <label class="block text-[#A1A1AA] font-medium mb-1 text-xs">Решением</label>
                  <input
                    v-model.number="fighter.solution"
                    type="number"
                    min="0"
                    class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-2.5 py-1.5 text-white font-mono focus:outline-none focus:border-[#B00D15] text-sm"
                  />
                </div>
                <div>
                  <label class="block text-[#A1A1AA] font-medium mb-1 text-xs">Сабмишен</label>
                  <input
                    v-model.number="fighter.submissive"
                    type="number"
                    min="0"
                    class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-2.5 py-1.5 text-white font-mono focus:outline-none focus:border-[#B00D15] text-sm"
                  />
                </div>
                <div>
                  <label class="block text-[#A1A1AA] font-medium mb-1 text-xs">Остальное</label>
                  <input
                    v-model.number="fighter.other"
                    type="number"
                    min="0"
                    class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-2.5 py-1.5 text-white font-mono focus:outline-none focus:border-[#B00D15] text-sm"
                  />
                </div>
              </div>
            </div>

            <div class="space-y-2">
              <label class="block text-[#E5E7EB] font-semibold">Достижения</label>
              <div
                v-for="(ach, index) in fighter.achievements"
                :key="index"
                class="flex items-center gap-2"
              >
                <input
                  v-model="ach.title"
                  type="text"
                  class="flex-1 bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm"
                />
                <button
                  type="button"
                  @click="removeAchievement(index)"
                  class="w-8 h-8 rounded-lg bg-[#B00D15]/20 hover:bg-[#B00D15] text-[#B00D15] hover:text-white flex items-center justify-center transition-colors shrink-0 cursor-pointer"
                >
                  <i class="pi pi-times text-xs"></i>
                </button>
              </div>
              <button
                type="button"
                @click="addAchievement"
                class="bg-[#1f1f22] hover:bg-[#2a2a2d] text-[#E5E7EB] hover:text-white border border-[#3F3F46] px-3.5 py-1.5 rounded-lg font-semibold text-sm flex items-center gap-1.5 transition-colors mt-2 cursor-pointer"
              >
                <i class="pi pi-plus text-xs text-[#B00D15]"></i>
                Добавить достижение
              </button>
            </div>
          </div>
        </div>

        <div class="flex items-center justify-between px-6 py-4 border-t border-[#292930] bg-[#111115] shrink-0">
          <button
            type="button"
            @click="dialogVisible = false"
            class="bg-[#1f1f22] hover:bg-[#2a2a2d] text-[#A1A1AA] hover:text-white border border-[#3F3F46] px-4 py-2 rounded-lg font-semibold text-sm flex items-center gap-2 transition-colors cursor-pointer"
          >
            <i class="pi pi-times text-xs"></i>
            Отмена
          </button>
          <button
            type="button"
            @click="saveFighter"
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