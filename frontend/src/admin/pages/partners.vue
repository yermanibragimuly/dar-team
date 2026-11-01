<template>
  <div class="max-w-7xl mx-auto space-y-6 text-white p-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-black uppercase tracking-tight">Наши партнеры</h1>
        <p class="text-xs text-zinc-400 mt-1">Управление логотипами и ссылками спонсоров на главной странице DAR TEAM</p>
      </div>

      <button
        @click="openCreateDialog"
        class="bg-[#B00D15] hover:bg-[#8F0B11] text-white text-xs font-bold uppercase tracking-wider px-4 py-2.5 rounded-lg flex items-center gap-2 shadow-lg shadow-[#B00D15]/20 transition cursor-pointer"
      >
        <i class="pi pi-plus text-xs"></i>
        <span>Добавить партнера</span>
      </button>
    </div>

    <!-- Загрузка -->
    <div v-if="loading" class="p-8 text-center text-zinc-400 bg-[#141418] rounded-xl border border-zinc-800">
      <i class="pi pi-spin pi-spinner text-xl text-[#B00D15] mr-2"></i> Загрузка партнеров...
    </div>

    <!-- Карточки партнеров (быстрый предпросмотр) -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div
        v-for="item in partners"
        :key="item.id"
        class="bg-[#141418] border border-zinc-800 rounded-xl p-4 flex flex-col justify-between hover:border-zinc-700 transition space-y-4 group"
      >
        <!-- Превью логотипа на красном фоне (как на сайте) -->
        <div class="h-28 rounded-lg bg-[#B00D15] flex items-center justify-center p-4 relative overflow-hidden shadow-inner">
          <img
            :src="item.logoUrl"
            :alt="item.name"
            class="max-h-full max-w-full object-contain filter drop-shadow group-hover:scale-105 transition duration-300"
          />
        </div>

        <div class="space-y-1">
          <h3 class="font-bold text-white text-sm truncate">{{ item.name }}</h3>
          <a
            v-if="item.websiteUrl"
            :href="item.websiteUrl"
            target="_blank"
            class="text-[11px] text-zinc-400 hover:text-white truncate block flex items-center gap-1"
          >
            <i class="pi pi-external-link text-[10px] text-[#B00D15]"></i>
            <span class="truncate">{{ item.websiteUrl }}</span>
          </a>
          <span v-else class="text-[11px] text-zinc-600 block italic">Без ссылки</span>
        </div>

        <!-- Кнопки управления -->
        <div class="flex items-center justify-end gap-2 pt-2 border-t border-zinc-800/80">
          <button
            @click="openEditDialog(item)"
            class="w-8 h-8 rounded-lg bg-[#1B1B20] text-zinc-400 hover:text-white hover:bg-zinc-700 flex items-center justify-center transition cursor-pointer"
            title="Редактировать"
          >
            <i class="pi pi-pencil text-xs"></i>
          </button>
          <button
            @click="handleDelete(item)"
            class="w-8 h-8 rounded-lg bg-red-500/10 text-red-500 hover:bg-red-600 hover:text-white flex items-center justify-center transition cursor-pointer"
            title="Удалить"
          >
            <i class="pi pi-trash text-xs"></i>
          </button>
        </div>
      </div>

      <div
        v-if="partners.length === 0"
        class="col-span-full p-12 text-center text-zinc-500 bg-[#121215] border border-zinc-800 rounded-xl"
      >
        <i class="pi pi-handshake text-4xl mb-2 block"></i>
        <span>Список партнеров пуст. Нажмите «Добавить партнера», чтобы загрузить логотип.</span>
      </div>
    </div>

    <!-- Модальное окно загрузки логотипа -->
    <Teleport to="body">
      <div
        v-if="dialogVisible"
        class="fixed inset-0 z-[100] bg-black/85 backdrop-blur-md flex items-center justify-center p-4"
        @click.self="dialogVisible = false"
      >
        <div class="bg-[#121215] border border-zinc-700 text-zinc-200 w-full max-w-md rounded-2xl shadow-2xl overflow-hidden flex flex-col">
          <!-- Шапка -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-zinc-800 bg-[#16161B]">
            <h2 class="text-sm font-bold uppercase tracking-wider text-white">
              {{ isEditing ? 'Редактировать партнера' : 'Новый партнер' }}
            </h2>
            <button
              @click="dialogVisible = false"
              class="w-8 h-8 rounded-lg flex items-center justify-center text-zinc-400 hover:text-white hover:bg-zinc-800 transition cursor-pointer"
            >
              <i class="pi pi-times text-sm"></i>
            </button>
          </div>

          <!-- Форма -->
          <div class="p-6 space-y-4 text-xs">
            <!-- Загрузка логотипа -->
            <div class="space-y-2">
              <label class="text-zinc-400 font-bold uppercase text-[10px] block">Логотип партнера *</label>
              
              <div class="h-32 rounded-xl bg-[#B00D15] flex items-center justify-center relative overflow-hidden border border-zinc-700/60 p-4">
                <img
                  v-if="form.logoUrl"
                  :src="form.logoUrl"
                  class="max-h-full max-w-full object-contain"
                />
                <div v-else class="text-center text-white/70">
                  <i class="pi pi-image text-3xl mb-1 block"></i>
                  <span class="text-[10px] uppercase font-bold">Логотип не выбран</span>
                </div>
              </div>

              <div class="flex gap-2 pt-1">
                <label class="flex-1 bg-[#18181D] hover:bg-[#222228] border border-zinc-700 text-zinc-200 py-2 rounded-lg font-bold text-center cursor-pointer transition flex items-center justify-center gap-1.5">
                  <i class="pi pi-upload text-xs text-[#B00D15]"></i>
                  <span>{{ uploading ? 'Загрузка...' : 'Загрузить PNG/SVG' }}</span>
                  <input type="file" accept="image/*" class="hidden" @change="handleFileUpload" :disabled="uploading" />
                </label>
                <button
                  v-if="form.logoUrl"
                  type="button"
                  @click="form.logoUrl = ''"
                  class="px-3 bg-zinc-800 hover:bg-zinc-700 text-zinc-400 hover:text-white rounded-lg transition cursor-pointer"
                  title="Очистить"
                >
                  <i class="pi pi-trash text-xs"></i>
                </button>
              </div>
            </div>

            <!-- Название -->
            <div class="space-y-1.5">
              <label class="text-zinc-400 font-bold uppercase text-[10px] block">Название компании / бренда *</label>
              <input
                v-model="form.name"
                placeholder="Например: BUKA BOXING или KAPA.KZ"
                class="w-full bg-[#0A0A0C] border border-zinc-800 focus:border-red-600 rounded-lg p-2.5 text-white outline-none font-medium"
              />
            </div>

            <!-- Ссылка на сайт -->
            <div class="space-y-1.5">
              <label class="text-zinc-400 font-bold uppercase text-[10px] block">Ссылка на сайт партнера</label>
              <input
                v-model="form.websiteUrl"
                placeholder="https://example.kz"
                class="w-full bg-[#0A0A0C] border border-zinc-800 focus:border-red-600 rounded-lg p-2.5 text-white outline-none font-mono text-[11px]"
              />
            </div>
          </div>

          <!-- Подвал -->
          <div class="flex items-center justify-between px-6 py-4 border-t border-zinc-800 bg-[#16161B]">
            <button
              type="button"
              @click="dialogVisible = false"
              class="px-4 py-2 rounded-lg border border-zinc-700 text-zinc-400 hover:text-white text-xs font-bold uppercase transition cursor-pointer"
            >
              Отмена
            </button>
            <button
              type="button"
              @click="savePartner"
              :disabled="saving || uploading"
              class="bg-[#B00D15] hover:bg-[#8F0B11] text-white px-5 py-2 rounded-lg text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 shadow-lg shadow-[#B00D15]/20 transition cursor-pointer disabled:opacity-50"
            >
              <i v-if="saving" class="pi pi-spin pi-spinner text-xs"></i>
              <span>{{ saving ? 'Сохранение...' : (isEditing ? 'Сохранить' : 'Добавить') }}</span>
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  getAdminPartners,
  createPartner,
  updatePartner,
  deletePartner,
  sendPartnerLogoMinio,
  type Partner
} from '@/services/partner-services'
import { showSuccess, showError } from '@/shared/lib/toastService'
import { useConfirmDialog } from '@/shared/lib/useConfirm'

const { show: showConfirm } = useConfirmDialog()
const partners = ref<Partner[]>([])
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const currentId = ref<number | null>(null)

const form = ref<Partner>({
  name: '',
  logoUrl: '',
  websiteUrl: '',
  positionIndex: 0
})

const fetchPartners = async () => {
  loading.value = true
  try {
    partners.value = await getAdminPartners()
  } catch (err) {
    showError('Ошибка загрузки партнеров')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEditing.value = false
  currentId.value = null
  form.value = {
    name: '',
    logoUrl: '',
    websiteUrl: '',
    positionIndex: partners.value.length
  }
  dialogVisible.value = true
}

const openEditDialog = (item: Partner) => {
  isEditing.value = true
  currentId.value = item.id || null
  form.value = { ...item }
  dialogVisible.value = true
}

const handleFileUpload = async (event: any) => {
  const file = event.target.files?.[0]
  if (!file) return

  uploading.value = true
  try {
    const uploadedUrl = await sendPartnerLogoMinio(file)
    form.value.logoUrl = uploadedUrl
    showSuccess('Логотип успешно загружен')
  } catch {
    showError('Ошибка загрузки файла в хранилище')
  } finally {
    uploading.value = false
  }
}

const savePartner = async () => {
  if (!form.value.name || !form.value.logoUrl) {
    showError('Заполните название и загрузите логотип')
    return
  }

  saving.value = true
  try {
    if (isEditing.value && currentId.value) {
      await updatePartner(currentId.value, form.value)
      showSuccess('Партнер обновлен')
    } else {
      await createPartner(form.value)
      showSuccess('Партнер успешно добавлен')
    }
    dialogVisible.value = false
    await fetchPartners()
  } catch {
    showError('Не удалось сохранить изменения')
  } finally {
    saving.value = false
  }
}

// Замените функцию handleDelete:
const handleDelete = async (item: Partner) => {
  const confirmed = await showConfirm({
    title: 'Удаление партнера',
    message: `Вы уверены, что хотите удалить партнера «${item.name}»?`,
    confirmText: 'Да, удалить'
  })

  if (!confirmed) return

  try {
    if (item.id) {
      await deletePartner(item.id)
      showSuccess('Партнер удален')
      await fetchPartners()
    }
  } catch {
    showError('Ошибка удаления')
  }
}

onMounted(fetchPartners)
</script>