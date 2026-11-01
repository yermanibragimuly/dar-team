<template>
  <div class="p-6 text-white min-h-screen space-y-6 max-w-7xl mx-auto">
    <!-- Шапка -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-black uppercase tracking-tight">Склад филиала / Учет остатков</h1>
        <p class="text-xs text-zinc-400 mt-1">
          Филиал: <strong class="text-white">{{ activeBranchName }}</strong>
        </p>
      </div>

      <!-- Переключатель залов для главного админа -->
      <div class="flex items-center gap-3">
        <div v-if="isSuperAdmin" class="flex items-center gap-2">
          <span class="text-xs text-zinc-400 font-semibold uppercase">Зал:</span>
          <select 
            v-model="selectedBranchId" 
            @change="fetchStocks"
            class="bg-[#18181D] border border-zinc-700 rounded-lg px-3 py-2 text-xs text-white outline-none cursor-pointer"
          >
            <option v-for="b in branches" :key="b.id" :value="b.id">{{ b.name }}</option>
          </select>
        </div>

        <button
          @click="saveStocks"
          :disabled="isSaving || isLoading"
          class="bg-[#B00D15] hover:bg-[#8F0B11] text-white px-5 py-2.5 rounded-lg text-xs font-bold uppercase tracking-wider transition shadow-lg shadow-[#B00D15]/20 flex items-center gap-2 cursor-pointer disabled:opacity-50"
        >
          <i v-if="isSaving" class="pi pi-spin pi-spinner text-xs"></i>
          <span>{{ isSaving ? 'Сохранение...' : 'Сохранить остатки зала' }}</span>
        </button>
      </div>
    </div>

    <!-- Поиск -->
    <div class="flex gap-2 max-w-md bg-[#121215] border border-zinc-800 rounded-lg px-3 py-2">
      <i class="pi pi-search text-zinc-500 self-center"></i>
      <input 
        v-model="searchQuery" 
        placeholder="Поиск по названию экипировки или SKU..." 
        class="bg-transparent text-xs text-white placeholder-zinc-500 focus:outline-none w-full"
      />
    </div>

    <!-- Загрузка -->
    <div v-if="isLoading" class="p-8 text-center text-zinc-400 bg-[#18181B] rounded-xl border border-zinc-800">
      <i class="pi pi-spin pi-spinner text-xl mr-2 text-[#B00D15]"></i> Загрузка остатков зала...
    </div>

    <!-- Таблица остатков -->
    <div v-else class="bg-[#18181B] border border-zinc-800 rounded-xl overflow-hidden shadow-xl">
      <div class="overflow-x-auto">
        <table class="w-full text-left text-xs text-zinc-300">
          <thead class="bg-[#121214] text-zinc-400 uppercase text-[10px] font-bold border-b border-zinc-800">
            <tr>
              <th class="p-3.5">Товар и артикул</th>
              <th class="p-3.5">Размер</th>
              <th class="p-3.5">Физическое наличие (на стойке)</th>
              <th class="p-3.5">В резерве заказов</th>
              <th class="p-3.5">Доступно к продаже</th>
              <th class="p-3.5 text-right">Статус</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-zinc-800">
            <tr v-for="item in filteredStocks" :key="item.variantId" class="hover:bg-zinc-800/40 transition">
              <td class="p-3.5">
                <span class="font-bold text-white block">{{ item.productName }}</span>
                <span class="text-[10px] font-mono text-zinc-400">SKU: {{ item.sku }}</span>
              </td>
              <td class="p-3.5">
                <span class="px-2 py-1 rounded bg-zinc-800 font-mono font-bold text-white">
                  {{ item.size }}
                </span>
              </td>
              <td class="p-3.5">
                <div class="flex items-center gap-2">
                  <input
                    v-model.number="item.quantity"
                    type="number"
                    min="0"
                    class="w-20 bg-[#121215] border border-zinc-700 focus:border-[#B00D15] rounded-lg px-2.5 py-1.5 font-bold font-mono text-white text-center text-sm outline-none"
                  />
                  <span class="text-zinc-500 text-[10px]">шт</span>
                </div>
              </td>
              <td class="p-3.5">
                <span class="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-amber-500/10 text-amber-400 border border-amber-500/30">
                  {{ item.reservedQuantity }} шт
                </span>
              </td>
              <td class="p-3.5 font-bold text-sm" :class="item.quantity - item.reservedQuantity > 0 ? 'text-emerald-400' : 'text-rose-500'">
                {{ Math.max(0, item.quantity - item.reservedQuantity) }} шт
              </td>
              <td class="p-3.5 text-right">
                <span v-if="item.quantity - item.reservedQuantity > 0" class="text-emerald-400 text-[10px] font-bold uppercase">
                  ● В наличии
                </span>
                <span v-else class="text-zinc-600 text-[10px] font-bold uppercase">
                  ○ Закончился
                </span>
              </td>
            </tr>
            <tr v-if="filteredStocks.length === 0">
              <td colspan="6" class="p-8 text-center text-zinc-500">Товары не найдены</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/auth/model/authStore'
import { showSuccess, showError } from '@/shared/lib/toastService'

interface StockRow {
  variantId: number
  productId: number
  productName: string
  sku: string
  size: string
  quantity: number
  reservedQuantity: number
  availableQuantity: number
}

const userStore = useUserStore()
const stocks = ref<StockRow[]>([])
const branches = ref<any[]>([])
const selectedBranchId = ref<number>(1)
const searchQuery = ref('')
const isLoading = ref(false)
const isSaving = ref(false)

const isSuperAdmin = computed(() => {
  const roles = Array.isArray(userStore.roles) ? userStore.roles : [userStore.roles]
  return roles.includes('ROLE_ADMIN')
})

const activeBranchName = computed(() => {
  const b = branches.value.find(x => x.id === selectedBranchId.value)
  return b ? b.name : `Филиал #${selectedBranchId.value}`
})

const filteredStocks = computed(() => {
  const q = searchQuery.value.toLowerCase().trim()
  if (!q) return stocks.value
  return stocks.value.filter(s => 
    s.productName.toLowerCase().includes(q) || 
    s.sku.toLowerCase().includes(q) ||
    s.size.toLowerCase().includes(q)
  )
})

const getHeaders = () => {
  const token = userStore.token || localStorage.getItem('token') || ''
  return {
    'Content-Type': 'application/json',
    Authorization: token.startsWith('Bearer ') ? token : `Bearer ${token}`
  }
}

const fetchBranches = async () => {
  try {
    const res = await fetch('/api/v1/shop/branches')
    if (res.ok) branches.value = await res.json()
  } catch {}
}

const fetchStocks = async () => {
  isLoading.value = true
  try {
    const res = await fetch(`/api/admin/v1/branches/${selectedBranchId.value}/stocks`, {
      headers: getHeaders()
    })
    if (res.ok) {
      stocks.value = await res.json()
    } else {
      const err = await res.json().catch(() => ({}))
      showError(err.error || 'Ошибка загрузки остатков зала')
    }
  } catch {
    showError('Не удалось соединиться с сервером')
  } finally {
    isLoading.value = false
  }
}

const saveStocks = async () => {
  isSaving.value = true
  try {
    const payload = stocks.value.map(s => ({
      variantId: s.variantId,
      quantity: s.quantity
    }))

    const res = await fetch(`/api/admin/v1/branches/${selectedBranchId.value}/stocks`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify(payload)
    })

    if (res.ok) {
      showSuccess('Остатки зала успешно сохранены!')
      await fetchStocks()
    } else {
      const err = await res.json().catch(() => ({}))
      showError(err.error || 'Ошибка сохранения')
    }
  } catch {
    showError('Сбой сохранения остатков')
  } finally {
    isSaving.value = false
  }
}

onMounted(async () => {
  await fetchBranches()
  await fetchStocks()
})
</script>