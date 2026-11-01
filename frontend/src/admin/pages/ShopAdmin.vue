<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { 
  getAdminProducts, 
  createProduct, 
  updateProduct, 
  deleteProduct, 
  toggleProductStatus,
  sendImgMinio
} from '@/services/shop-services'
import { showError, showSuccess } from '@/shared/lib/toastService'
import { useConfirmDialog } from '@/shared/lib/useConfirm'

const { show: showConfirm } = useConfirmDialog()
const loading = ref(false)
const products = ref<any[]>([])
const dialogVisible = ref(false)
const editMode = ref(false)
const searchQuery = ref('')
const activeFilter = ref('ALL')

// Базовые доступные размеры
const ALL_SIZES = ['XS', 'S', 'M', 'L', 'XL', '2XL', '3XL']

// Форма Бренда: только активные размеры, без ручного ввода количества
const emptyProduct = () => ({
  name: '',
  sku: '',
  category: 'Худи и толстовки',
  collection: 'Kazakh Ornament 2026',
  retailPrice: 0,
  proPrice: 0,
  costPrice: 0,
  description: '',
  isActive: true,
  isProDiscount: true,
  images: [] as Array<{ imageUrl: string; isMain: boolean }>,
  selectedSizes: ['S', 'M', 'L', 'XL', '2XL'] // Выбранные размеры модели
})

const form = ref(emptyProduct())
const currentId = ref<number | null>(null)

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await getAdminProducts()
    products.value = res.data || []
  } catch (err) {
    showError('Ошибка загрузки каталога товаров')
  } finally {
    loading.value = false
  }
}

const totalPositions = computed(() => products.value.length)
const activeCount = computed(() => products.value.filter(p => p.isActive).length)
const totalStock = computed(() => {
  return products.value.reduce((sum, p) => {
    const pStock = (p.variants || []).reduce((acc: number, v: any) => acc + (v.stockQuantity || 0), 0)
    return sum + pStock
  }, 0)
})

const filteredProducts = computed(() => {
  return products.value.filter(p => {
    const query = (searchQuery.value || '').toLowerCase()
    const matchQuery = p.name?.toLowerCase().includes(query) || p.sku?.toLowerCase().includes(query)
    if (activeFilter.value === 'ACTIVE') return matchQuery && p.isActive
    if (activeFilter.value === 'HIDDEN') return matchQuery && !p.isActive
    return matchQuery
  })
})

const getMainImg = (product: any) => {
  const main = product.images?.find((img: any) => img.isMain)
  return main?.imageUrl || product.images?.[0]?.imageUrl || ''
}

const toggleSize = (size: string) => {
  const idx = form.value.selectedSizes.indexOf(size)
  if (idx > -1) {
    if (form.value.selectedSizes.length > 1) {
      form.value.selectedSizes.splice(idx, 1)
    } else {
      showError('У товара должен быть хотя бы один размер')
    }
  } else {
    form.value.selectedSizes.push(size)
  }
}

const openCreateDialog = () => {
  editMode.value = false
  currentId.value = null
  form.value = emptyProduct()
  dialogVisible.value = true
}

const openEditDialog = (item: any) => {
  editMode.value = true
  currentId.value = item.id
  
  const currentSizes = (item.variants || []).map((v: any) => v.size)
  form.value = {
    ...JSON.parse(JSON.stringify(item)),
    selectedSizes: currentSizes.length > 0 ? currentSizes : ['S', 'M', 'L', 'XL']
  }
  dialogVisible.value = true
}

const updateProPrice = () => {
  if (form.value.retailPrice) {
    form.value.proPrice = Math.round(form.value.retailPrice * 0.85)
  }
}

const handleImageUpload = async (event: any) => {
  const file = event.target.files?.[0]
  if (!file) return

  try {
    const res = await sendImgMinio(file, 'shop')
    const newUrl = res.data
    const isFirst = form.value.images.length === 0
    form.value.images.push({ imageUrl: newUrl, isMain: isFirst })
    showSuccess('Фотография успешно добавлена')
  } catch (err) {
    showError('Ошибка загрузки фото в MinIO')
  }
}

const removeImage = (index: number) => {
  form.value.images.splice(index, 1)
  if (form.value.images.length > 0 && !form.value.images.some(img => img.isMain)) {
    form.value.images[0].isMain = true
  }
}

const setMainImage = (index: number) => {
  form.value.images.forEach((img, i) => {
    img.isMain = i === index
  })
}

const saveProduct = async () => {
  if (!form.value.name || !form.value.sku || !form.value.retailPrice) {
    showError('Заполните название, артикул и цену')
    return
  }

  loading.value = true
  try {
    // Формируем варианты без необходимости заполнять остатки вручную
    const payload = {
      ...form.value,
      variants: form.value.selectedSizes.map(size => ({
        size,
        stockQuantity: 0 // Остатки выставляются в разделе "Склад филиала"
      }))
    }

    if (editMode.value && currentId.value) {
      await updateProduct(currentId.value, payload)
      showSuccess('Товар обновлен')
    } else {
      await createProduct(payload)
      showSuccess('Товар добавлен в реестр Бренда')
    }
    dialogVisible.value = false
    await fetchProducts()
  } catch (err) {
    showError('Не удалось сохранить изменения')
  } finally {
    loading.value = false
  }
}

const toggleActive = async (item: any) => {
  try {
    await toggleProductStatus(item.id, !item.isActive)
    item.isActive = !item.isActive
    showSuccess(`Товар ${item.isActive ? 'опубликован' : 'скрыт'}`)
  } catch (err) {
    showError('Ошибка изменения статуса')
  }
}

// Замените функцию deleteItem:
const deleteItem = async (item: any) => {
  const confirmed = await showConfirm({
    title: 'Удаление товара',
    message: `Вы уверены, что хотите удалить позицию «${item.name}» (артикул: ${item.sku})?`,
    confirmText: 'Да, удалить'
  })

  if (!confirmed) return

  try {
    await deleteProduct(item.id)
    showSuccess('Позиция удалена')
    await fetchProducts()
  } catch (err) {
    showError('Не удалось удалить товар')
  }
}

onMounted(fetchProducts)
</script>

<template>
  <div class="space-y-6 max-w-7xl mx-auto p-6 text-white">
    <!-- Верхний заголовок -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-black text-white tracking-tight uppercase">Каталог товаров (Бренд DAR TEAM)</h1>
        <p class="text-xs text-zinc-400 mt-1">Создание моделей, ценообразование и контроль номенклатуры сети</p>
      </div>

      <div class="flex items-center gap-3">
        <div class="bg-[#121215] border border-zinc-800 rounded-lg px-3 py-2 text-center min-w-[100px]">
          <span class="text-[10px] uppercase text-zinc-400 block font-semibold">Всего моделей</span>
          <span class="text-base font-black text-white">{{ totalPositions }}</span>
        </div>
        <div class="bg-[#121215] border border-zinc-800 rounded-lg px-3 py-2 text-center min-w-[100px]">
          <span class="text-[10px] uppercase text-zinc-400 block font-semibold">В продаже</span>
          <span class="text-base font-black text-emerald-400">{{ activeCount }}</span>
        </div>

        <button
          @click="openCreateDialog"
          class="bg-[#B00D15] hover:bg-[#8F0B11] text-white text-xs font-bold uppercase tracking-wider px-4 py-3 rounded-lg flex items-center gap-1.5 shadow-lg shadow-[#B00D15]/20 cursor-pointer transition"
        >
          <i class="pi pi-plus text-xs"></i> Создать товар
        </button>
      </div>
    </div>

    <!-- Поиск и фильтры -->
    <div class="rounded-xl border border-zinc-800 bg-[#121215] p-3 flex flex-wrap items-center justify-between gap-3">
      <div class="flex items-center gap-2 flex-1 max-w-md bg-[#0A0A0C] border border-zinc-800 rounded-lg px-3 py-2">
        <i class="pi pi-search text-zinc-500 text-sm"></i>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Поиск по названию или SKU..."
          class="bg-transparent text-xs text-white placeholder-zinc-500 focus:outline-none w-full"
        />
      </div>

      <div class="flex items-center gap-2">
        <button
          @click="activeFilter = 'ALL'"
          :class="['px-3 py-1.5 rounded-lg text-xs font-bold uppercase transition', activeFilter === 'ALL' ? 'bg-[#B00D15] text-white' : 'bg-[#18181D] text-zinc-400 hover:text-white']"
        >
          Все ({{ totalPositions }})
        </button>
        <button
          @click="activeFilter = 'ACTIVE'"
          :class="['px-3 py-1.5 rounded-lg text-xs font-bold uppercase transition', activeFilter === 'ACTIVE' ? 'bg-[#B00D15] text-white' : 'bg-[#18181D] text-zinc-400 hover:text-white']"
        >
          Опубликованные ({{ activeCount }})
        </button>
      </div>
    </div>

    <!-- Таблица каталога -->
    <div class="rounded-xl border border-zinc-800 bg-[#121215] overflow-hidden shadow-xl">
      <div class="overflow-x-auto">
        <table class="w-full text-left text-xs">
          <thead class="bg-[#18181D] text-zinc-400 uppercase text-[11px] font-bold border-b border-zinc-800">
            <tr>
              <th class="py-3.5 px-4">Товар и артикул</th>
              <th class="py-3.5 px-4">Категория</th>
              <th class="py-3.5 px-4">Размерная сетка</th>
              <th class="py-3.5 px-4">Розничная / PRO цена</th>
              <th class="py-3.5 px-4">Статус</th>
              <th class="py-3.5 px-4 text-right pr-6">Действия</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-zinc-800/80">
            <tr
              v-for="item in filteredProducts"
              :key="item.id"
              class="group hover:bg-[#18181D]/60 transition"
            >
              <td class="py-3 px-4">
                <div class="flex items-center gap-3">
                  <div class="w-12 h-12 rounded-lg bg-[#0A0A0C] border border-zinc-800 overflow-hidden shrink-0 flex items-center justify-center">
                    <img v-if="getMainImg(item)" :src="getMainImg(item)" :alt="item.name" class="w-full h-full object-cover" />
                    <span v-else class="text-[10px] text-zinc-600 font-bold uppercase">No img</span>
                  </div>
                  <div>
                    <span class="font-bold text-white block text-xs group-hover:text-red-500 transition">{{ item.name }}</span>
                    <span class="text-[10px] font-mono text-zinc-400">SKU: {{ item.sku }}</span>
                  </div>
                </div>
              </td>
              <td class="py-3 px-4 text-zinc-300 font-medium">{{ item.category }}</td>
              <td class="py-3 px-4">
                <div class="flex items-center gap-1">
                  <span
                    v-for="v in item.variants"
                    :key="v.size"
                    class="px-1.5 py-0.5 rounded text-[10px] font-mono font-bold bg-zinc-800 text-zinc-200"
                  >
                    {{ v.size }}
                  </span>
                </div>
              </td>
              <td class="py-3 px-4">
                <span class="font-black text-white text-sm block">{{ item.retailPrice?.toLocaleString() }} ₸</span>
                <span class="text-[10px] text-zinc-400 font-semibold">PRO: {{ item.proPrice?.toLocaleString() || '—' }} ₸</span>
              </td>
              <td class="py-3 px-4">
                <button
                  @click="toggleActive(item)"
                  :class="[
                    'px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider flex items-center gap-1.5 transition cursor-pointer',
                    item.isActive ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30' : 'bg-zinc-800 text-zinc-500'
                  ]"
                >
                  <span :class="['w-1.5 h-1.5 rounded-full', item.isActive ? 'bg-emerald-400' : 'bg-zinc-500']"></span>
                  {{ item.isActive ? 'Опубликован' : 'Скрыт' }}
                </button>
              </td>
              <td class="py-3 px-4 text-right pr-6">
                <div class="inline-flex items-center gap-2">
                  <button
                    @click="openEditDialog(item)"
                    class="w-8 h-8 rounded-lg bg-[#18181D] text-zinc-400 hover:text-white hover:bg-zinc-700 flex items-center justify-center transition cursor-pointer"
                    title="Редактировать"
                  >
                    <i class="pi pi-pencil text-xs"></i>
                  </button>
                  <button
                    @click="deleteItem(item)"
                    class="w-8 h-8 rounded-lg bg-red-500/10 text-red-500 hover:bg-red-600 hover:text-white flex items-center justify-center transition cursor-pointer"
                    title="Удалить"
                  >
                    <i class="pi pi-trash text-xs"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <!-- МОДАЛКА БРЕНДА: СОЗДАНИЕ МОДЕЛИ -->
  <Teleport to="body">
    <div
      v-if="dialogVisible"
      class="fixed inset-0 z-[100] bg-black/85 backdrop-blur-md flex items-center justify-center p-4"
      @click.self="dialogVisible = false"
    >
      <div class="bg-[#121215] border border-zinc-700 text-zinc-200 w-full max-w-2xl rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[92vh]">
        <!-- Шапка -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-zinc-800 bg-[#16161B] shrink-0">
          <div>
            <h2 class="text-base font-bold text-white uppercase tracking-wider">
              {{ editMode ? 'Редактирование модели' : 'Новая модель экипировки' }}
            </h2>
            <span v-if="editMode" class="text-xs text-zinc-400 font-mono">{{ form.name }} ({{ form.sku }})</span>
          </div>
          <button
            @click="dialogVisible = false"
            class="w-8 h-8 rounded-lg flex items-center justify-center text-zinc-400 hover:text-white hover:bg-zinc-800 transition cursor-pointer"
          >
            <i class="pi pi-times text-sm"></i>
          </button>
        </div>

        <!-- Контент формы -->
        <div class="p-6 space-y-5 overflow-y-auto text-xs">
          <!-- Название и SKU -->
          <div class="grid grid-cols-1 md:grid-cols-12 gap-4">
            <div class="md:col-span-8 space-y-1.5">
              <label class="text-zinc-400 font-bold uppercase text-[10px]">Название экипировки / мерча *</label>
              <input
                v-model="form.name"
                type="text"
                placeholder="ЗИП-ХУДИ DEEP NAVY «KAZAKHSTAN / DAR TEAM»"
                class="w-full bg-[#0A0A0C] border border-zinc-800 rounded-lg px-3 py-2.5 text-white font-medium focus:border-red-600 focus:outline-none"
              />
            </div>
            <div class="md:col-span-4 space-y-1.5">
              <label class="text-zinc-400 font-bold uppercase text-[10px]">Артикул (SKU) *</label>
              <input
                v-model="form.sku"
                type="text"
                placeholder="DAR-HD-001"
                class="w-full bg-[#0A0A0C] border border-zinc-800 rounded-lg px-3 py-2.5 text-white font-mono font-bold focus:border-red-600 focus:outline-none uppercase"
              />
            </div>
          </div>

          <!-- Категория и Коллекция -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="space-y-1.5">
              <label class="text-zinc-400 font-bold uppercase text-[10px]">Категория каталога</label>
              <select
                v-model="form.category"
                class="w-full bg-[#0A0A0C] border border-zinc-800 rounded-lg px-3 py-2.5 text-white focus:border-red-600 focus:outline-none"
              >
                <option value="Худи и толстовки">Худи и толстовки</option>
                <option value="Рашгарды">Рашгарды</option>
                <option value="Шорты ММА">Шорты ММА</option>
                <option value="Костюмы">Костюмы</option>
                <option value="Штаны / Брюки">Штаны / Брюки</option>
                <option value="Футболки">Футболки</option>
              </select>
            </div>
            <div class="space-y-1.5">
              <label class="text-zinc-400 font-bold uppercase text-[10px]">Коллекция бренда</label>
              <input
                v-model="form.collection"
                type="text"
                placeholder="Kazakh Ornament 2026"
                class="w-full bg-[#0A0A0C] border border-zinc-800 rounded-lg px-3 py-2.5 text-white focus:border-red-600 focus:outline-none"
              />
            </div>
          </div>

          <!-- Цены -->
          <div class="rounded-xl border border-zinc-800 p-4 bg-[#0F0F12] space-y-3">
            <span class="text-white font-bold uppercase tracking-wider text-[11px] flex items-center gap-1.5">
              <i class="pi pi-tag text-red-500"></i> Ценовая структура (KZT)
            </span>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label class="text-zinc-400 font-medium block mb-1">Розничная цена</label>
                <input
                  v-model.number="form.retailPrice"
                  @input="updateProPrice"
                  type="number"
                  class="w-full bg-[#0A0A0C] border border-zinc-800 rounded-lg px-3 py-2 text-white font-black text-sm focus:border-red-600 focus:outline-none"
                />
              </div>
              <div>
                <label class="text-zinc-400 font-medium block mb-1">Спеццена DAR PRO (-15%)</label>
                <input
                  v-model.number="form.proPrice"
                  type="number"
                  class="w-full bg-[#0A0A0C] border border-zinc-800 rounded-lg px-3 py-2 text-white font-black text-sm focus:border-red-600 focus:outline-none"
                />
              </div>
              <div>
                <label class="text-zinc-400 font-medium block mb-1">Себестоимость</label>
                <input
                  v-model.number="form.costPrice"
                  type="number"
                  class="w-full bg-[#0A0A0C] border border-zinc-800 rounded-lg px-3 py-2 text-zinc-400 font-black text-sm focus:border-red-600 focus:outline-none"
                />
              </div>
            </div>
          </div>

          <!-- ВЫБОР РАЗМЕРНОЙ СЕТКИ (БЕЗ РУЧНЫХ НУЛЕЙ) -->
          <div class="space-y-2">
            <div class="flex items-center justify-between">
              <label class="text-zinc-400 font-bold uppercase text-[10px]">
                Размерная сетка модели (кликните для выбора)
              </label>
              <span class="text-[10px] text-zinc-500">Выбрано размеров: {{ form.selectedSizes.length }}</span>
            </div>

            <!-- Чипсы выбора размеров -->
            <div class="flex flex-wrap gap-2">
              <button
                v-for="size in ALL_SIZES"
                :key="size"
                type="button"
                @click="toggleSize(size)"
                :class="[
                  'px-4 py-2.5 rounded-xl font-mono font-bold text-xs uppercase transition cursor-pointer border',
                  form.selectedSizes.includes(size)
                    ? 'bg-[#B00D15] text-white border-[#B00D15] shadow-[0_0_12px_rgba(176,13,21,0.3)]'
                    : 'bg-[#0A0A0C] text-zinc-500 border-zinc-800 hover:text-white hover:border-zinc-700'
                ]"
              >
                {{ size }}
              </button>
            </div>

            <div class="p-3 rounded-lg bg-blue-950/20 border border-blue-900/40 text-[11px] text-blue-300 flex items-center gap-2 mt-2">
              <i class="pi pi-info-circle text-blue-400 text-sm shrink-0"></i>
              <span>Учет фактического количества и приход по филиалам ведется в разделе <strong>«Склад филиала»</strong> для каждого зала отдельно.</span>
            </div>
          </div>

          <!-- Фотографии -->
          <div class="space-y-2">
            <label class="text-zinc-400 font-bold uppercase text-[10px]">Фотографии экипировки (Каталог)</label>
            <div class="flex flex-wrap items-center gap-3">
              <div
                v-for="(img, idx) in form.images"
                :key="idx"
                class="relative w-20 h-20 rounded-xl border border-zinc-700 bg-black overflow-hidden group shadow-md"
              >
                <img :src="img.imageUrl" class="w-full h-full object-cover" />
                <span
                  v-if="img.isMain"
                  class="absolute top-1 left-1 bg-red-600 text-white font-bold text-[8px] uppercase px-1.5 py-0.5 rounded shadow"
                >
                  Главное
                </span>
                <div class="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition flex items-center justify-center gap-1.5">
                  <button
                    v-if="!img.isMain"
                    type="button"
                    @click="setMainImage(idx)"
                    class="p-1 rounded bg-zinc-800 text-white hover:bg-zinc-700"
                    title="Сделать главным"
                  >
                    <i class="pi pi-star text-xs"></i>
                  </button>
                  <button
                    type="button"
                    @click="removeImage(idx)"
                    class="p-1 rounded bg-red-600 text-white hover:bg-red-700"
                    title="Удалить"
                  >
                    <i class="pi pi-trash text-xs"></i>
                  </button>
                </div>
              </div>

              <!-- Кнопка загрузки -->
              <label class="w-20 h-20 rounded-xl border-2 border-dashed border-zinc-700 hover:border-red-500 bg-[#0A0A0C] flex flex-col items-center justify-center gap-1 cursor-pointer text-zinc-500 hover:text-white transition">
                <i class="pi pi-image text-sm"></i>
                <span class="text-[9px] font-bold uppercase">+ Загрузить</span>
                <input type="file" accept="image/*" class="hidden" @change="handleImageUpload" />
              </label>
            </div>
          </div>

          <!-- Состав ткани -->
          <div class="space-y-1.5">
            <label class="text-zinc-400 font-bold uppercase text-[10px]">Состав ткани и свойства материала</label>
            <textarea
              v-model="form.description"
              rows="3"
              placeholder="Премиум футер 3-х нитка с начесом, 80% хлопок, 20% полиэстер..."
              class="w-full bg-[#0A0A0C] border border-zinc-800 rounded-lg p-3 text-white focus:border-red-600 focus:outline-none resize-none"
            ></textarea>
          </div>

          <!-- Чекбоксы публикации -->
          <div class="flex items-center gap-6 pt-2 border-t border-zinc-800/80">
            <label class="flex items-center gap-2 cursor-pointer text-zinc-300">
              <input type="checkbox" v-model="form.isActive" class="w-4 h-4 accent-red-600 rounded" />
              <span class="font-bold">Отображать в магазине</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer text-zinc-300">
              <input type="checkbox" v-model="form.isProDiscount" class="w-4 h-4 accent-red-600 rounded" />
              <span class="font-bold">Скидка по DAR PRO</span>
            </label>
          </div>
        </div>

        <!-- Подвал -->
        <div class="flex items-center justify-between px-6 py-4 border-t border-zinc-800 bg-[#16161B] shrink-0">
          <button
            type="button"
            @click="dialogVisible = false"
            class="px-4 py-2.5 rounded-lg border border-zinc-700 text-zinc-400 hover:text-white text-xs font-bold uppercase transition cursor-pointer"
          >
            Отмена
          </button>
          <button
            type="button"
            @click="saveProduct"
            :disabled="loading"
            class="bg-[#B00D15] hover:bg-[#8F0B11] text-white px-6 py-2.5 rounded-lg text-xs font-bold uppercase tracking-wider flex items-center gap-2 shadow-lg shadow-[#B00D15]/20 transition cursor-pointer disabled:opacity-50"
          >
            <i class="pi pi-check text-xs"></i>
            {{ editMode ? 'Сохранить изменения' : 'Создать товар' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>