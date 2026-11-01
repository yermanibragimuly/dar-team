<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPublicProducts } from '@/services/shop-services'
import { useCartStore } from '@/features/cart/cartStore'
import { showSuccess, showError } from '@/shared/lib/toastService'

interface ProductImage {
  id?: number
  imageUrl: string
  isMain?: boolean
}

interface ProductVariant {
  id?: number
  size: string
  stockQuantity: number
}

interface Product {
  id: number
  name: string
  sku?: string
  category?: string
  collection?: string
  badge?: string
  retailPrice?: number
  proPrice?: number
  costPrice?: number
  description?: string
  isActive?: boolean
  isProDiscount?: boolean
  positionIndex?: number
  images?: ProductImage[]
  variants?: ProductVariant[]
  totalStock?: number
}

const router = useRouter()
const cartStore = useCartStore()

const activeCategory = ref('all')
const isLoading = ref(true)
const selectedSizes = ref<Record<number, string>>({})

// Состояние модального окна «Наличие в залах»
const isStockModalOpen = ref(false)
const selectedProductForModal = ref<Product | null>(null)
const modalSelectedSize = ref<string>('M')
const branches = ref<any[]>([])
const branchStocksCache = ref<Record<number, any[]>>({})

const categories = [
  { id: 'all', name: 'Все товары' },
  { id: 'ornament', name: 'Казахский орнамент' },
  { id: 'camo', name: 'Камуфляж / Camo' },
  { id: 'rashguards', name: 'Рашгарды' },
  { id: 'shorts', name: 'Шорты ММА' },
  { id: 'hoodies', name: 'Худи и костюмы' }
]

const products = ref<Product[]>([])

const fetchBranches = async () => {
  try {
    const res = await fetch('/api/v1/shop/branches')
    if (res.ok) branches.value = await res.json()
  } catch {}
}

const fetchProducts = async () => {
  try {
    isLoading.value = true
    const res = await getPublicProducts()
    const rawData = res && res.data !== undefined ? res.data : res

    if (Array.isArray(rawData)) {
      products.value = rawData
    } else {
      products.value = []
    }

    products.value.forEach(p => {
      const avail = getProductSizes(p)
      if (avail.length > 0) {
        selectedSizes.value[p.id] = avail[0]
      }
    })
  } catch (error) {
    products.value = []
  } finally {
    isLoading.value = false
  }
}

onMounted(async () => {
  await fetchBranches()
  await fetchProducts()
})

const featuredProduct = computed(() => {
  return products.value.length > 0 ? products.value[0] : null
})

const getProductImage = (product: Product | null | undefined): string => {
  if (!product || !product.images || !Array.isArray(product.images) || product.images.length === 0) {
    return ''
  }
  const mainImg = product.images.find(img => img && img.isMain)
  const target = mainImg || product.images[0]
  return target && target.imageUrl ? target.imageUrl : ''
}

const getProductSizes = (product: Product): string[] => {
  if (!product || !product.variants || !Array.isArray(product.variants)) return ['S', 'M', 'L', 'XL', '2XL']
  return product.variants.map(v => v.size)
}

const getAvailableStock = (product: Product, size?: string): number => {
  if (!product || !product.variants) return 0
  const chosenSize = size || selectedSizes.value[product.id]
  const variant = product.variants.find(v => v.size.toUpperCase() === chosenSize?.toUpperCase())
  return variant ? (variant.stockQuantity || 0) : 0
}

const filteredProducts = computed(() => {
  return products.value.filter(item => {
    if (!item) return false
    const itemCat = String(item.category || '').toLowerCase()
    const itemName = String(item.name || '').toLowerCase()
    const itemColl = String(item.collection || '').toLowerCase()

    return activeCategory.value === 'all' ||
      (activeCategory.value === 'ornament' && (itemColl.includes('ornament') || itemColl.includes('орнамент') || itemName.includes('ornament') || itemName.includes('орнамент'))) ||
      (activeCategory.value === 'camo' && (itemColl.includes('camo') || itemName.includes('camo') || itemName.includes('камуфляж'))) ||
      (activeCategory.value === 'rashguards' && (itemCat.includes('рашгард') || itemName.includes('рашгард'))) ||
      (activeCategory.value === 'shorts' && (itemCat.includes('шорт') || itemName.includes('шорт'))) ||
      (activeCategory.value === 'hoodies' && (itemCat.includes('худи') || itemCat.includes('костюм') || itemName.includes('худи') || itemName.includes('штаны')))
  })
})

// Открытие окна «Наличие в залах»
const openStockModal = async (product: Product) => {
  selectedProductForModal.value = product
  modalSelectedSize.value = selectedSizes.value[product.id] || getProductSizes(product)[0] || 'M'
  isStockModalOpen.value = true

  // Принудительно запрашиваем свежие остатки конкретно по залам
  try {
    const res = await fetch(`/api/v1/shop/products/${product.id}/stocks`)
    if (res.ok) {
      branchStocksCache.value[product.id] = await res.json()
    }
  } catch {}
}

// Получение остатка конкретного филиала в модалке
const getBranchStockCount = (branchId: number, size: string): number => {
  if (!selectedProductForModal.value) return 0
  const stocks = branchStocksCache.value[selectedProductForModal.value.id]
  
  if (Array.isArray(stocks) && stocks.length > 0) {
    const match = stocks.find(s => 
      s.size?.toUpperCase() === size.toUpperCase() && 
      Number(s.branchId) === Number(branchId)
    )
    if (match) {
      return match.availableQuantity !== undefined ? match.availableQuantity : (match.quantity || 0)
    }
    // Если для этого зала нет записи или в ней 0 — возвращаем строго 0
    return 0
  }

  // Если данные еще не загрузились — показываем 0, а не чужой остаток
  return 0
}

const addToCart = (product: Product) => {
  const chosenSize = selectedSizes.value[product.id]
  if (!chosenSize) {
    showError('Пожалуйста, выберите размер')
    return
  }
  const stock = getAvailableStock(product, chosenSize)
  if (stock <= 0) {
    showError(`К сожалению, размера ${chosenSize} нет в наличии на складе`)
    return
  }

  const existingInCart = cartStore.items.find(i => i.productId === product.id && i.size === chosenSize)?.quantity || 0
  if (existingInCart + 1 > stock) {
    showError(`На складе доступно всего ${stock} шт. этого размера (в корзине уже: ${existingInCart})`)
    return
  }

  const added = cartStore.addItem(product, chosenSize, 1, stock)
  if (added) {
    showSuccess(`"${product.name}" (${chosenSize}) добавлен в корзину`)
  } else {
    showError(`Достигнут максимум доступного остатка (${stock} шт.)`)
  }
}

const buyNow = (product: Product) => {
  const chosenSize = selectedSizes.value[product.id]
  const stock = getAvailableStock(product, chosenSize)
  if (stock <= 0) {
    showError(`Размера ${chosenSize} нет в наличии на складе`)
    return
  }
  addToCart(product)
  router.push('/checkout')
}

const formatPrice = (val?: number | null) => {
  if (val === undefined || val === null || isNaN(val)) return '0 ₸'
  return new Intl.NumberFormat('ru-RU').format(val) + ' ₸'
}
</script>

<template>
  <div class="min-h-screen bg-[#0A0A0C] text-zinc-100 pb-20 selection:bg-[#B00D15] selection:text-white">
    
    <!-- 1. HERO БАННЕР -->
    <section class="relative overflow-hidden border-b border-zinc-800/80 bg-gradient-to-b from-[#141418] via-[#0D0D10] to-[#0A0A0C] pt-12 pb-16">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-10 items-center">
          
          <div class="lg:col-span-7 space-y-6">
            <h1 class="text-3xl sm:text-4xl lg:text-5xl font-black uppercase tracking-tight text-white leading-tight">
              ОФИЦИАЛЬНЫЙ МЕРЧ <br />
              <span class="text-[#B00D15]">& FIGHTWEAR</span> DAR TEAM
            </h1>
            <p class="text-zinc-300 text-xs sm:text-sm max-w-xl leading-relaxed">
              Аутентичная экипировка бойцов UFC Шавката «Nomad» Рахмонова, Асу Алмабаева и всего ростера DAR Team. Боевые компрессионные рашгарды с сакральным казахским орнаментом, серия Camo «Өзгерісті өзіңнен баста», оверсайз худи и проверенное снаряжение октагона.
            </p>
            <div class="flex flex-wrap items-center gap-3.5 pt-2">
              <a
                href="#catalog"
                class="bg-[#B00D15] hover:bg-[#8F0B11] text-white font-bold text-xs uppercase tracking-wider px-6 py-3.5 rounded-lg shadow-lg shadow-[#B00D15]/30 flex items-center gap-2 transition cursor-pointer"
              >
                <span>СМОТРЕТЬ КАТАЛОГ</span>
                <i class="pi pi-arrow-right text-xs"></i>
              </a>
              <a
                href="#locations"
                class="bg-[#18181D] hover:bg-[#222228] text-zinc-300 hover:text-white border border-zinc-700/80 font-bold text-xs uppercase tracking-wider px-5 py-3.5 rounded-lg flex items-center gap-2 transition cursor-pointer"
              >
                <i class="pi pi-building text-xs text-zinc-400"></i>
                <span>ЗАЛЫ И МАРКЕТПЛЕЙСЫ</span>
              </a>
            </div>
          </div>

          <!-- Featured Card справа -->
          <div class="lg:col-span-5 flex justify-center">
            <div v-if="featuredProduct" class="relative w-full max-w-md rounded-2xl bg-[#121215] border border-zinc-800 p-5 shadow-2xl overflow-hidden">
              <div class="flex justify-between items-center text-[10px] font-bold uppercase tracking-wider mb-3">
                <span class="px-2.5 py-0.5 rounded-full bg-[#B00D15] text-white">
                  {{ featuredProduct.badge || 'KAZAKHSTAN EDITION' }}
                </span>
                <span class="text-zinc-400">
                  {{ featuredProduct.collection || 'ӨЗГЕРІСТІ ӨЗІҢНЕН БАСТА' }}
                </span>
              </div>

              <div class="aspect-[16/10] rounded-xl overflow-hidden bg-[#18181D] mb-4 flex items-center justify-center relative border border-zinc-800/80">
                <img
                  v-if="getProductImage(featuredProduct)"
                  :src="getProductImage(featuredProduct)"
                  :alt="featuredProduct.name"
                  class="w-full h-full object-cover"
                />
                <span v-else class="text-zinc-600 text-xs font-mono font-bold">DAR TEAM GEAR</span>
              </div>

              <div class="space-y-1">
                <div class="text-[9px] font-mono text-zinc-500 uppercase tracking-widest font-bold">FLAGSHIP SPECIAL DROP</div>
                <div class="flex items-baseline justify-between gap-2">
                  <h3 class="font-bold text-white text-sm uppercase line-clamp-1">{{ featuredProduct.name }}</h3>
                  <div class="text-right shrink-0">
                    <span class="text-base font-black text-white">{{ formatPrice(featuredProduct.retailPrice) }}</span>
                    <span class="block text-[9px] font-mono font-bold uppercase text-emerald-400">В НАЛИЧИИ</span>
                  </div>
                </div>
                <p class="text-[11px] text-zinc-400 pt-1 line-clamp-2">
                  {{ featuredProduct.description || 'Официальная экипировка команды DAR TEAM' }}
                </p>
              </div>
            </div>

            <div v-else class="relative w-full max-w-md rounded-2xl bg-[#121215] border border-zinc-800/80 p-8 shadow-2xl flex flex-col items-center justify-center text-center space-y-3 min-h-[300px]">
              <div class="w-14 h-14 rounded-full bg-[#18181D] border border-zinc-700/60 flex items-center justify-center text-[#B00D15]">
                <i class="pi pi-shopping-bag text-2xl"></i>
              </div>
              <h3 class="font-bold text-white text-sm uppercase tracking-wider">Каталог экипировки</h3>
              <p class="text-xs text-zinc-500 max-w-xs leading-relaxed">
                Добавьте товары через панель администратора, и первый товар появится в этой витрине.
              </p>
            </div>
          </div>

        </div>
      </div>
    </section>

    <!-- 2. РАЗДЕЛ: ГДЕ КУПИТЬ ПРОДУКЦИЮ DAR TEAM -->
    <section id="locations" class="py-14 border-b border-zinc-800/80 bg-[#0C0C0E]">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
        
        <div class="flex flex-col md:flex-row md:items-end justify-between gap-4">
          <div>
            <span class="text-[#B00D15] text-[10px] font-bold uppercase tracking-widest block">
              ОФИЦИАЛЬНЫЕ ТОЧКИ ПРОДАЖ И ПРИМЕРКИ
            </span>
            <h2 class="text-2xl sm:text-3xl font-black text-white uppercase tracking-tight mt-1">
              ГДЕ КУПИТЬ ПРОДУКЦИЮ DAR TEAM
            </h2>
          </div>
          <p class="text-xs text-zinc-400 max-w-lg leading-relaxed">
            Примеряйте экипировку прямо в официальных залах DAR Team, заказывайте онлайн с быстрой курьерской доставкой или оформите в официальных залах клуба.
          </p>
        </div>

        <div class="flex items-center gap-3 pt-2">
          <span class="text-xs font-bold uppercase tracking-wider text-white">
            ОФИЦИАЛЬНЫЕ ЗАЛЫ DAR TEAM — ПРИМЕРКА И ПОКУПКА В КЛУБЕ
          </span>
          <span class="px-2 py-0.5 rounded bg-red-950/80 border border-red-800 text-[10px] font-bold text-red-400 uppercase tracking-wider">
            ШОУРУМЫ В ЗАЛАХ
          </span>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
          
          <!-- Филиал 1: DAR PRO GYM ALMATY -->
          <div class="rounded-2xl border border-zinc-800 bg-[#121215] p-6 flex flex-col justify-between hover:border-zinc-700 transition space-y-5">
            <div class="space-y-4">
              <span class="text-[11px] font-bold uppercase tracking-wider text-emerald-400 block">
                В НАЛИЧИИ ВЕСЬ МЕРЧ
              </span>
              <div>
                <h3 class="text-base font-black text-white uppercase tracking-tight">DAR PRO GYM ALMATY</h3>
                <p class="text-[11px] text-zinc-400 mt-1 flex items-start gap-1.5 leading-snug">
                  <i class="pi pi-map-marker text-[#B00D15] text-xs mt-0.5 shrink-0"></i>
                  <span>г. Алматы, ул. Патшаим Тажибаевой, 155 <br />(главный тренировочный комплекс DAR Team)</span>
                </p>
              </div>

              <div class="border-t border-zinc-800/80 pt-3 space-y-2 text-xs">
                <div class="flex justify-between">
                  <span class="text-zinc-500">Телефон:</span>
                  <a href="tel:+77472434438" class="font-bold text-white hover:text-red-500 transition">+7 (747) 243-44-38</a>
                </div>
                <div class="flex justify-between">
                  <span class="text-zinc-500">Email:</span>
                  <span class="font-mono text-zinc-300">darteam@dar.io</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-zinc-500">Режим работы:</span>
                  <span class="font-medium text-white">Ежедневно 07:00 – 23:00</span>
                </div>
              </div>
            </div>

            <a
              href="https://yandex.ru/maps/?text=Алматы+Патшаим+Тажибаевой+155"
              target="_blank"
              class="w-full bg-[#18181D] hover:bg-[#222228] text-zinc-200 hover:text-white border border-zinc-700/80 py-2.5 rounded-xl text-xs font-bold uppercase tracking-wider flex items-center justify-center gap-2 transition"
            >
              <i class="pi pi-map text-xs text-amber-500"></i>
              <span>НА КАРТЕ</span>
            </a>
          </div>

          <!-- Филиал 2: DAR TEAM GYM ALMATY -->
          <div class="rounded-2xl border border-zinc-800 bg-[#121215] p-6 flex flex-col justify-between hover:border-zinc-700 transition space-y-5">
            <div class="space-y-4">
              <span class="text-[11px] font-bold uppercase tracking-wider text-emerald-400 block">
                ПРИМЕРКА & ВЫДАЧА
              </span>
              <div>
                <h3 class="text-base font-black text-white uppercase tracking-tight">DAR TEAM GYM ALMATY</h3>
                <p class="text-[11px] text-zinc-400 mt-1 flex items-start gap-1.5 leading-snug">
                  <i class="pi pi-map-marker text-[#B00D15] text-xs mt-0.5 shrink-0"></i>
                  <span>г. Алматы, ул. Ораза Жандосова, 87 <br />(Спортивная база единоборств)</span>
                </p>
              </div>

              <div class="border-t border-zinc-800/80 pt-3 space-y-2 text-xs">
                <div class="flex justify-between">
                  <span class="text-zinc-500">Телефон:</span>
                  <a href="tel:+77472434438" class="font-bold text-white hover:text-red-500 transition">+7 (747) 243-44-38</a>
                </div>
                <div class="flex justify-between">
                  <span class="text-zinc-500">Консьерж-сервис:</span>
                  <span class="text-zinc-300">Подбор экипировки бойца</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-zinc-500">Режим работы:</span>
                  <span class="font-medium text-white">Пн–Сб 08:00 – 22:00</span>
                </div>
              </div>
            </div>

            <a
              href="https://yandex.ru/maps/?text=Алматы+Ораза+Жандосова+87"
              target="_blank"
              class="w-full bg-[#18181D] hover:bg-[#222228] text-zinc-200 hover:text-white border border-zinc-700/80 py-2.5 rounded-xl text-xs font-bold uppercase tracking-wider flex items-center justify-center gap-2 transition"
            >
              <i class="pi pi-map text-xs text-amber-500"></i>
              <span>НА КАРТЕ</span>
            </a>
          </div>

          <!-- Филиал 3: Региональная сеть филиалов -->
          <div class="rounded-2xl border border-zinc-800 bg-[#121215] p-6 flex flex-col justify-between hover:border-zinc-700 transition space-y-5">
            <div class="space-y-3">
              <div class="flex items-center justify-between">
                <span class="text-[11px] font-bold uppercase tracking-wider text-zinc-400">
                  РЕГИОНАЛЬНАЯ СЕТЬ ФИЛИАЛОВ
                </span>
                <span class="px-2 py-0.5 rounded bg-zinc-800 text-[10px] font-mono text-zinc-300 font-bold uppercase">
                  8 ГОРОДОВ РК
                </span>
              </div>

              <h3 class="text-base font-black text-white uppercase tracking-tight">ФИЛИАЛЫ DAR TEAM В РЕГИОНАХ</h3>
              <p class="text-[11px] text-zinc-400 leading-relaxed">
                Официальные клубные корнеры и стойки экипировки в залах бойцовской сети DAR Team:
              </p>

              <div class="flex flex-wrap gap-1.5 pt-1">
                <span v-for="city in ['Астана', 'Атырау', 'Актау', 'Кокшетау', 'Актобе', 'Конаев', 'Кызылорда', 'Туркестанская область']" :key="city" class="px-2.5 py-1 rounded bg-[#18181D] border border-zinc-800 text-[10px] text-zinc-300 font-semibold">
                  {{ city }}
                </span>
              </div>
            </div>

            <router-link
              to="/branch"
              class="w-full bg-[#18181D] hover:bg-[#222228] text-zinc-200 hover:text-white border border-zinc-700/80 py-2.5 rounded-xl text-xs font-bold uppercase tracking-wider flex items-center justify-center gap-2 transition"
            >
              <i class="pi pi-compass text-xs text-blue-400"></i>
              <span>АДРЕСА ВСЕХ ФИЛИАЛОВ</span>
            </router-link>
          </div>

        </div>

      </div>
    </section>

    <!-- 3. КАТАЛОГ: БОЕВАЯ ЭКИПИРОВКА И КЭЖУАЛ -->
    <section id="catalog" class="py-14 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
      
      <div>
        <span class="text-[#B00D15] text-[10px] font-bold uppercase tracking-widest block">
          ОФИЦИАЛЬНАЯ ЛИНЕЙКА ЭКИПИРОВКИ
        </span>
        <h2 class="text-2xl sm:text-3xl font-black text-white uppercase tracking-tight mt-1">
          БОЕВАЯ ЭКИПИРОВКА И КЭЖУАЛ
        </h2>
      </div>

      <!-- Кнопки фильтрации категорий -->
      <div class="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
        <button
          v-for="cat in categories"
          :key="cat.id"
          @click="activeCategory = cat.id"
          :class="[
            'px-4 py-2 rounded-lg text-xs font-bold uppercase tracking-wider transition whitespace-nowrap cursor-pointer',
            activeCategory === cat.id
              ? 'bg-[#B00D15] text-white shadow-lg shadow-[#B00D15]/30'
              : 'bg-[#141418] text-zinc-400 hover:text-white border border-zinc-800'
          ]"
        >
          {{ cat.name }}
        </button>
      </div>

      <!-- Загрузка -->
      <div v-if="isLoading" class="py-20 text-center text-zinc-400">
        <i class="pi pi-spin pi-spinner text-3xl text-[#B00D15]"></i>
        <p class="text-sm mt-3">Загрузка каталога...</p>
      </div>

      <!-- Пусто -->
      <div v-else-if="filteredProducts.length === 0" class="py-20 text-center text-zinc-500">
        Товары в данной категории не найдены. Создайте их в панели администратора.
      </div>

      <!-- Сетка карточек товаров -->
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <div
          v-for="product in filteredProducts"
          :key="product.id"
          class="rounded-xl border border-zinc-800 bg-[#121215] flex flex-col justify-between overflow-hidden shadow-lg group hover:border-zinc-700 transition"
        >
          <div>
            <div class="relative aspect-square w-full bg-[#18181D] overflow-hidden flex items-center justify-center">
              <span v-if="product.badge || product.collection" class="absolute top-3 left-3 z-10 px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider text-white bg-[#B00D15]">
                {{ product.badge || product.collection }}
              </span>
              <img
                v-if="getProductImage(product)"
                :src="getProductImage(product)"
                :alt="product.name"
                class="w-full h-full object-cover group-hover:scale-105 transition duration-500"
              />
              <span v-else class="text-zinc-600 text-[11px] font-mono font-bold">НЕТ ФОТО</span>
            </div>

            <!-- Инфо о товаре -->
            <div class="p-4 space-y-2.5">
              <span class="text-[10px] font-mono text-zinc-500 uppercase tracking-wider block">
                {{ product.collection || 'DAR TEAM' }}
              </span>
              <h3 class="font-bold text-white text-xs sm:text-sm line-clamp-1 group-hover:text-red-500 transition">
                {{ product.name }}
              </h3>
              <p class="text-[11px] text-zinc-400 line-clamp-2 leading-relaxed">
                {{ product.description }}
              </p>

              <!-- Размеры и статус наличия -->
              <div class="pt-2 space-y-2">
                <div class="flex items-center justify-between">
                  <span class="text-[10px] uppercase font-bold text-zinc-500">Размер:</span>
                  
                  <div class="text-[10px] font-mono">
                    <span v-if="getAvailableStock(product) > 5" class="text-emerald-400 font-bold">
                      В наличии: {{ getAvailableStock(product) }} шт.
                    </span>
                    <span v-else-if="getAvailableStock(product) > 0" class="text-amber-400 font-bold">
                      Мало: {{ getAvailableStock(product) }} шт.
                    </span>
                    <span v-else class="text-red-500 font-bold uppercase">
                      Нет на складе
                    </span>
                  </div>
                </div>

                <!-- Кнопки выбора размера -->
                <div class="flex flex-wrap gap-1">
                  <button
                    v-for="size in getProductSizes(product)"
                    :key="size"
                    type="button"
                    @click="selectedSizes[product.id] = size"
                    :class="[
                      'px-2 py-1 rounded text-[10px] font-mono font-bold transition cursor-pointer relative',
                      selectedSizes[product.id] === size
                        ? 'bg-[#B00D15] text-white border border-[#B00D15] shadow-sm'
                        : (getAvailableStock(product, size) > 0 
                            ? 'bg-[#18181D] text-zinc-300 border border-zinc-800 hover:border-zinc-700' 
                            : 'bg-zinc-900/60 text-zinc-600 border border-zinc-800/40 opacity-50')
                    ]"
                  >
                    {{ size }}
                  </button>
                </div>

                <!-- КНОПКА «НАЛИЧИЕ В ЗАЛАХ» -->
                <button
                  type="button"
                  @click="openStockModal(product)"
                  class="w-full pt-1.5 flex items-center justify-center gap-1.5 text-[11px] font-medium text-zinc-400 hover:text-white transition cursor-pointer group/stock"
                >
                  <i class="pi pi-building text-[11px] text-[#B00D15] group-hover/stock:scale-110 transition-transform"></i>
                  <span class="group-hover/stock:underline">Наличие в залах</span>
                </button>
              </div>
            </div>
          </div>

          <!-- Нижняя панель с ценой и кнопками -->
          <div class="p-4 pt-0 border-t border-zinc-800/80 mt-2 space-y-2">
            <div class="flex items-baseline justify-between pt-3">
              <span class="text-base font-black text-white">{{ formatPrice(product.retailPrice) }}</span>
              <span v-if="product.proPrice" class="text-[10px] text-zinc-400">PRO: {{ formatPrice(product.proPrice) }}</span>
            </div>

            <div class="grid grid-cols-2 gap-2">
              <button
                type="button"
                :disabled="getAvailableStock(product) === 0"
                @click="addToCart(product)"
                class="bg-[#1A1A20] hover:bg-[#25252D] text-zinc-200 border border-zinc-700 py-2.5 rounded-lg text-[11px] font-bold uppercase tracking-wider flex items-center justify-center gap-1.5 transition cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <i class="pi pi-shopping-bag text-xs"></i>
                <span>В корзину</span>
              </button>
              <button
                type="button"
                :disabled="getAvailableStock(product) === 0"
                @click="buyNow(product)"
                class="bg-[#B00D15] hover:bg-[#8F0B11] text-white py-2.5 rounded-lg text-[11px] font-bold uppercase tracking-wider flex items-center justify-center gap-1.5 transition shadow-md shadow-[#B00D15]/20 cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <span>Купить</span>
              </button>
            </div>
          </div>

        </div>
      </div>
    </section>

    <!-- МОДАЛЬНОЕ ОКНО «НАЛИЧИЕ В ЗАЛАХ» -->
    <Teleport to="body">
      <div 
        v-if="isStockModalOpen" 
        class="fixed inset-0 z-[100] bg-black/80 backdrop-blur-md flex items-center justify-center p-4 transition-all"
        @click.self="isStockModalOpen = false"
      >
        <div class="bg-[#121215] border border-zinc-800 text-zinc-200 w-full max-w-lg rounded-2xl shadow-2xl overflow-hidden flex flex-col">
          <!-- Шапка модалки -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-zinc-800 bg-[#16161B] shrink-0">
            <div class="flex items-center gap-2.5">
              <i class="pi pi-building text-[#B00D15] text-base"></i>
              <h3 class="text-sm font-black uppercase tracking-wider text-white">Наличие в залах DAR TEAM</h3>
            </div>
            <button 
              @click="isStockModalOpen = false" 
              class="w-8 h-8 rounded-lg flex items-center justify-center text-zinc-400 hover:text-white hover:bg-zinc-800 transition cursor-pointer"
            >
              <i class="pi pi-times text-sm"></i>
            </button>
          </div>

          <!-- Контент -->
          <div class="p-6 space-y-5">
            <!-- Инфо о товаре -->
            <div class="flex items-center gap-3 bg-[#18181D] p-3 rounded-xl border border-zinc-800">
              <div class="w-12 h-12 rounded-lg bg-black overflow-hidden shrink-0 flex items-center justify-center">
                <img 
                  v-if="getProductImage(selectedProductForModal)" 
                  :src="getProductImage(selectedProductForModal)" 
                  class="w-full h-full object-cover" 
                />
                <span v-else class="text-[9px] text-zinc-600">НЕТ ФОТО</span>
              </div>
              <div class="min-w-0 flex-1">
                <h4 class="text-xs font-bold text-white truncate">{{ selectedProductForModal?.name }}</h4>
                <span class="text-[10px] font-mono text-zinc-400">SKU: {{ selectedProductForModal?.sku }}</span>
              </div>
            </div>

            <!-- Переключатель размеров -->
            <div class="space-y-1.5">
              <span class="text-[10px] font-bold uppercase text-zinc-400 block tracking-wider">Выберите размер для проверки:</span>
              <div class="flex flex-wrap gap-1.5">
                <button
                  v-for="size in getProductSizes(selectedProductForModal!)"
                  :key="size"
                  type="button"
                  @click="modalSelectedSize = size"
                  :class="[
                    'px-3 py-1.5 rounded-lg text-xs font-mono font-bold transition cursor-pointer',
                    modalSelectedSize === size
                      ? 'bg-[#B00D15] text-white shadow-sm'
                      : 'bg-[#18181D] text-zinc-400 hover:text-white border border-zinc-800'
                  ]"
                >
                  {{ size }}
                </button>
              </div>
            </div>

            <!-- Список всех клубов и остатков выбранного размера -->
            <div class="space-y-2.5 pt-1">
              <div 
                v-for="b in branches" 
                :key="b.id"
                class="p-3.5 rounded-xl border border-zinc-800/90 bg-[#16161B] flex items-center justify-between"
              >
                <div class="space-y-0.5">
                  <div class="flex items-center gap-2">
                    <span class="text-xs font-bold text-white">{{ b.name }}</span>
                  </div>
                  <p class="text-[11px] text-zinc-400 flex items-center gap-1">
                    <i class="pi pi-map-marker text-[10px] text-zinc-500"></i>
                    {{ b.address }}
                  </p>
                  <p class="text-[10px] text-zinc-500">Тел: {{ b.phone }}</p>
                </div>

                <!-- Бейдж количества -->
                <div class="text-right shrink-0">
                  <span 
                    v-if="getBranchStockCount(b.id, modalSelectedSize) > 3" 
                    class="px-2.5 py-1 rounded-full text-[10px] font-mono font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 block"
                  >
                    В наличии ({{ getBranchStockCount(b.id, modalSelectedSize) }} шт)
                  </span>
                  <span 
                    v-else-if="getBranchStockCount(b.id, modalSelectedSize) > 0" 
                    class="px-2.5 py-1 rounded-full text-[10px] font-mono font-bold bg-amber-500/10 text-amber-400 border border-amber-500/30 block"
                  >
                    Мало ({{ getBranchStockCount(b.id, modalSelectedSize) }} шт)
                  </span>
                  <span 
                    v-else 
                    class="px-2.5 py-1 rounded-full text-[10px] font-mono font-bold bg-zinc-800 text-zinc-500 block"
                  >
                    Нет в наличии
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Подвал модалки -->
          <div class="px-6 py-3.5 border-t border-zinc-800 bg-[#16161B] text-center">
            <span class="text-[10px] text-zinc-500">
              Вы можете заказать товар онлайн и забрать самовывозом с бесплатной примеркой в удобном зале.
            </span>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<style scoped>
.scrollbar-none::-webkit-scrollbar {
  display: none;
}
.scrollbar-none {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>