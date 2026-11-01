<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { showError, showSuccess } from '@/shared/lib/toastService'
import { useConfirmDialog } from '@/shared/lib/useConfirm'
import { getAllCouches, sendCouches, deleteCoucheId, updateCoucheId, deleteMeritId, deleteCoucheImgId, sendImgMinio } from '@/services/couch-services'

const { show } = useConfirmDialog()
const coaches = ref<any[]>([])
const dialogVisible = ref(false)
const isEditing = ref(false)
const currentCouchesId = ref<number | null>(null)
const loading = ref(false)

// Пагинация
const currentPage = ref(1)
const itemsPerPage = ref(10)
const searchQuery = ref('')

const coach = ref({ 
  name: '', 
  status: '', 
  img: '', 
  merit: [] as Array<{ id?: number; list: string }> 
})

const fetchCoaches = async () => {
  loading.value = true
  try {
    const res = await getAllCouches()
    coaches.value = res.data || []
  } catch (err) {
    showError(err)
  } finally {
    loading.value = false
  }
}

// Поиск
const filteredCoaches = computed(() => {
  return coaches.value.filter(c => {
    return c.name?.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
           c.status?.toLowerCase().includes(searchQuery.value.toLowerCase())
  })
})

// Пагинация
const totalPages = computed(() => Math.ceil(filteredCoaches.value.length / itemsPerPage.value) || 1)

const paginatedCoaches = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredCoaches.value.slice(start, end)
})

const changePage = (page: number) => {
  currentPage.value = page
}

const openNew = () => {
  coach.value = { name: '', status: '', img: '', merit: [] }
  isEditing.value = false
  currentCouchesId.value = null
  dialogVisible.value = true
}

const editCoach = (data: any) => {
  isEditing.value = true
  currentCouchesId.value = data.id
  coach.value = { ...data, merit: data.merit ? [...data.merit] : [] }
  dialogVisible.value = true
}

const saveCoach = async () => {
  loading.value = true
  try {
    if (isEditing.value && currentCouchesId.value) {
      await updateCoucheId(currentCouchesId.value, coach.value)
      showSuccess('Тренер успешно обновлен')
    } else {
      await sendCouches(coach.value)
      showSuccess('Тренер успешно создан')
    }
    await fetchCoaches()
    dialogVisible.value = false
  } catch (err) {
    showError(err)
  } finally {
    loading.value = false
  }
}

const deleteCoach = async (data: any) => {
  const confirmed = await show({ message: `Вы точно хотите удалить тренера ${data.name}?` })
  if (!confirmed) return
  try {
    await deleteCoucheId(data.id)
    showSuccess('Успешно удалено')
    await fetchCoaches()
  } catch (err) {
    showError(err)
  }
}

const addMerit = () => {
  coach.value.merit.push({ list: '' })
}

const removeMerit = async (index: number, meritId?: number) => {
  try {
    if (meritId && currentCouchesId.value) {
      await deleteMeritId(String(currentCouchesId.value), String(meritId))
    }
    coach.value.merit.splice(index, 1)
    showSuccess('Заслуга удалена')
  } catch (err) {
    showError(err)
  }
}

const uploadImage = async (e: any) => {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    const res = await sendImgMinio(file, 'coache')
    coach.value.img = res.data
    showSuccess('Фото загружено')
  } catch (err) {
    showError(err)
  }
}

const deleteImg = async () => {
  try {
    if (currentCouchesId.value) {
      await deleteCoucheImgId(currentCouchesId.value)
    }
    coach.value.img = ''
    showSuccess('Фото удалено')
    await fetchCoaches()
  } catch (err) {
    showError(err)
  }
}

onMounted(() => {
  fetchCoaches()
})
</script>

<template>
  <div class="max-w-7xl mx-auto space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white tracking-tight">Управление тренерами</h1>
        <p class="text-sm text-[#9CA3AF] mt-1">Реестр тренерского штаба DAR TEAM</p>
      </div>
      <button
        @click="openNew"
        class="bg-[#B00D15] hover:bg-[#9B0B12] text-white text-sm font-semibold px-4 py-2.5 rounded-lg flex items-center gap-2 shadow-lg shadow-[#B00D15]/20 transition-all cursor-pointer"
      >
        <i class="pi pi-plus"></i>
        Добавить тренера
      </button>
    </div>

    <!-- Поиск -->
    <div class="flex items-center justify-end">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Поиск тренеров..."
        class="bg-[#1f1f22] border border-[#3F3F46] rounded-lg px-3 py-2 text-sm text-white placeholder-[#71717A] focus:outline-none focus:border-[#B00D15] w-64"
      />
    </div>

    <!-- Table Container -->
    <div class="rounded-xl border border-[#292930] bg-[#131316] overflow-hidden shadow-xl">
      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm">
          <thead class="bg-[#1f1f22] text-[#9CA3AF] uppercase text-xs font-semibold border-b border-[#292930]">
            <tr>
              <th class="py-3.5 px-4">Имя</th>
              <th class="py-3.5 px-4">Статус / Специализация</th>
              <th class="py-3.5 px-4 text-right pr-6">Действия</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-[#292930]">
            <tr v-for="c in paginatedCoaches" :key="c.id" class="group hover:bg-[#1f1f22]/50 transition-colors">
              <td class="py-3.5 px-4">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-lg overflow-hidden bg-[#1f1f22] shrink-0 shadow-sm border border-[#292930]">
                    <img v-if="c.img" :src="c.img" :alt="c.name" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
                    <div v-else class="w-full h-full flex items-center justify-center text-[#71717A] font-extrabold text-xs">
                      {{ c.name?.slice(0, 2).toUpperCase() }}
                    </div>
                  </div>
                  <span class="font-medium text-white group-hover:text-[#B00D15] transition-colors truncate">{{ c.name }}</span>
                </div>
              </td>
              <td class="py-3.5 px-4 text-[#A1A1AA]">
                {{ c.status || '—' }}
              </td>
              <td class="py-3.5 px-4 text-right pr-6">
                <div class="inline-flex items-center gap-1.5 justify-end">
                  <button
                    @click="editCoach(c)"
                    class="w-8 h-8 rounded-lg bg-[#1f1f22] text-[#A1A1AA] hover:text-white hover:bg-[#2a2a2d] flex items-center justify-center transition-all cursor-pointer"
                    title="Редактировать"
                  >
                    <i class="pi pi-pencil text-xs"></i>
                  </button>
                  <button
                    @click="deleteCoach(c)"
                    class="w-8 h-8 rounded-lg bg-[#B00D15]/20 text-[#B00D15] hover:bg-[#B00D15] hover:text-white flex items-center justify-center transition-all cursor-pointer"
                    title="Удалить"
                  >
                    <i class="pi pi-trash text-xs"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="paginatedCoaches.length === 0">
              <td colspan="3" class="py-8 text-center text-[#71717A]">
                Тренеры не найдены
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Пагинация -->
      <div class="bg-[#1f1f22] p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <span class="text-xs text-[#71717A] uppercase tracking-wider">
          Показано <span class="text-white font-bold">{{ paginatedCoaches.length }}</span> из <span class="text-white font-bold">{{ filteredCoaches.length }}</span> тренеров
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

    <!-- Modal Dialog -->
    <Teleport to="body">
      <div v-if="dialogVisible" class="fixed inset-0 z-[100] bg-black/80 backdrop-blur-md flex items-center justify-center p-4" @click.self="dialogVisible = false">
        <div class="bg-[#131316] border border-[#3F3F46] text-zinc-200 w-full max-w-xl rounded-xl shadow-2xl relative flex flex-col max-h-[92vh]">
          <div class="flex items-center justify-between px-6 py-4 border-b border-[#292930] shrink-0 bg-[#111115]">
            <h2 class="text-base font-bold text-white tracking-wide uppercase">
              {{ isEditing ? 'Редактировать тренера' : 'Добавить тренера' }}
            </h2>
            <button @click="dialogVisible = false" class="w-8 h-8 rounded-lg flex items-center justify-center text-[#A1A1AA] hover:text-white hover:bg-[#2a2a2d] transition-colors cursor-pointer">
              <i class="pi pi-times text-sm"></i>
            </button>
          </div>

          <form @submit.prevent="saveCoach" class="p-6 space-y-4 text-sm overflow-y-auto">
            <div>
              <label class="block text-[#E5E7EB] font-semibold mb-1.5">Имя</label>
              <input v-model="coach.name" required class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm" />
            </div>

            <div>
              <label class="block text-[#E5E7EB] font-semibold mb-1.5">Статус / Должность</label>
              <input v-model="coach.status" class="w-full bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm" />
            </div>

            <div>
              <label class="block text-[#E5E7EB] font-semibold mb-1.5">Фотография</label>
              <div class="flex items-center gap-3">
                <label class="bg-[#B00D15] hover:bg-[#9B0B12] text-white px-3.5 py-2 rounded-lg text-xs font-semibold cursor-pointer transition-colors">
                  Загрузить фото
                  <input type="file" accept="image/*" class="hidden" @change="uploadImage" />
                </label>
                <button v-if="coach.img" type="button" @click="deleteImg" class="bg-[#1f1f22] text-[#B00D15] hover:bg-[#2a2a2d] border border-[#3F3F46] px-3.5 py-2 rounded-lg text-xs font-semibold cursor-pointer">Удалить</button>
              </div>
              <img v-if="coach.img" :src="coach.img" class="h-28 mt-3 rounded-lg border border-[#292930] object-cover" />
            </div>

            <div class="space-y-2">
              <label class="block text-[#E5E7EB] font-semibold">Заслуги</label>
              <div v-for="(m, index) in coach.merit" :key="index" class="flex gap-2">
                <input v-model="m.list" placeholder="Описание заслуги" class="flex-1 bg-[#0e0e11] border border-[#292930] rounded-lg px-3 py-2 text-white focus:outline-none focus:border-[#B00D15] text-sm" />
                <button type="button" @click="removeMerit(index, m.id)" class="w-8 h-8 rounded-lg bg-[#B00D15]/20 hover:bg-[#B00D15] text-[#B00D15] hover:text-white flex items-center justify-center transition-colors shrink-0 cursor-pointer">
                  <i class="pi pi-times text-xs"></i>
                </button>
              </div>
              <button type="button" @click="addMerit" class="bg-[#1f1f22] hover:bg-[#2a2a2d] text-[#E5E7EB] hover:text-white border border-[#3F3F46] px-3.5 py-1.5 rounded-lg font-semibold text-sm flex items-center gap-1.5 transition-colors mt-2 cursor-pointer">
                <i class="pi pi-plus text-xs text-[#B00D15]"></i>
                Добавить заслугу
              </button>
            </div>

            <div class="flex items-center justify-between pt-4 border-t border-[#292930] bg-[#111115]">
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