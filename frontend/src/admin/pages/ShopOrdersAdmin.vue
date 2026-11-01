<template>
  <div class="p-6 text-white min-h-screen space-y-6 max-w-7xl mx-auto">
    <!-- Шапка и выбор филиала -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-black uppercase tracking-tight">Заказы и Выдача</h1>
        <p class="text-xs text-zinc-400 mt-1">
          <span v-if="isSuperAdmin">Все филиалы сети DAR TEAM</span>
          <span v-else>Филиал: <strong class="text-white">{{ currentManagerBranchName }}</strong></span>
        </p>
      </div>

      <div v-if="isSuperAdmin" class="flex items-center gap-2">
        <span class="text-xs text-zinc-400 uppercase font-semibold">Филиал:</span>
        <select 
          v-model="selectedBranch" 
          @change="fetchOrders" 
          class="bg-[#18181D] border border-zinc-700 rounded-lg px-3 py-2 text-xs text-white outline-none cursor-pointer"
        >
          <option :value="0">Все филиалы</option>
          <option :value="1">DAR PRO GYM ALMATY</option>
          <option :value="2">DAR TEAM GYM ALMATY</option>
          <option :value="3">DAR TEAM FIGHT GYM ASTANA</option>
        </select>
      </div>
    </div>

    <!-- ТАБЛО ВЫРУЧКИ И КАССЫ -->
    <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3.5">
      <div class="bg-[#121215] border border-zinc-800 rounded-xl p-3.5 space-y-1">
        <span class="text-[10px] uppercase font-bold text-zinc-400 tracking-wider block">Выручка (Оплачено)</span>
        <div class="text-lg font-black text-emerald-400">{{ metrics.totalRevenue.toLocaleString() }} ₸</div>
        <span class="text-[10px] text-zinc-500 block">{{ metrics.paidCount }} успешных заказов</span>
      </div>

      <div class="bg-[#121215] border border-zinc-800 rounded-xl p-3.5 space-y-1">
        <span class="text-[10px] uppercase font-bold text-zinc-400 tracking-wider block">Касса в зале</span>
        <div class="text-lg font-black text-white">{{ metrics.branchCash.toLocaleString() }} ₸</div>
        <span class="text-[10px] text-zinc-500 block">Kaspi QR / Терминал / Нал</span>
      </div>

      <div class="bg-[#121215] border border-zinc-800 rounded-xl p-3.5 space-y-1">
        <span class="text-[10px] uppercase font-bold text-zinc-400 tracking-wider block">Онлайн-эквайринг</span>
        <div class="text-lg font-black text-white">{{ metrics.onlineRevenue.toLocaleString() }} ₸</div>
        <span class="text-[10px] text-zinc-500 block">Карты на сайте</span>
      </div>

      <div class="bg-[#121215] border border-zinc-800 rounded-xl p-3.5 space-y-1">
        <span class="text-[10px] uppercase font-bold text-zinc-400 tracking-wider block">Ожидают выдачи</span>
        <div class="text-lg font-black text-amber-400">{{ metrics.pendingCount }} шт.</div>
        <span class="text-[10px] text-zinc-500 block">{{ metrics.pendingAmount.toLocaleString() }} ₸ в резерве</span>
      </div>

      <div class="bg-[#121215] border border-zinc-800 rounded-xl p-3.5 space-y-1">
        <span class="text-[10px] uppercase font-bold text-zinc-400 tracking-wider block">Возвраты</span>
        <div class="text-lg font-black text-rose-500">{{ metrics.refundedAmount.toLocaleString() }} ₸</div>
        <span class="text-[10px] text-zinc-500 block">{{ metrics.refundedCount }} заказов отменено</span>
      </div>
    </div>

    <!-- Поиск -->
    <div class="flex gap-2 max-w-md bg-[#121215] border border-zinc-800 rounded-lg px-3 py-2">
      <i class="pi pi-search text-zinc-500 self-center"></i>
      <input 
        v-model="searchQuery" 
        @input="fetchOrders"
        placeholder="Поиск по номеру заказа, коду выдачи или телефону..." 
        class="bg-transparent text-xs text-white placeholder-zinc-500 focus:outline-none w-full"
      />
    </div>

    <!-- Таблица заказов -->
    <div class="bg-[#18181B] border border-zinc-800 rounded-xl overflow-hidden shadow-xl">
      <div class="overflow-x-auto">
        <table class="w-full text-left text-xs text-zinc-300">
          <thead class="bg-[#121214] text-zinc-400 uppercase text-[10px] font-bold border-b border-zinc-800">
            <tr>
              <th class="p-3.5 w-36">Заказ / Код</th>
              <th class="p-3.5 w-44">Клиент</th>
              <th class="p-3.5 min-w-[220px]">Состав</th>
              <th class="p-3.5 w-28">Сумма</th>
              <th class="p-3.5 w-28">Оплата</th>
              <th class="p-3.5 w-36">Статус</th>
              <th class="p-3.5 text-right w-40">Выдача / Действия</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-zinc-800">
            <tr v-for="order in orders" :key="order.id" class="hover:bg-zinc-800/40 transition">
              
              <!-- Код и номер -->
              <td class="p-3.5 align-top">
                <div class="font-mono font-bold text-white text-xs">{{ order.orderNumber }}</div>
                <div v-if="order.orderStatus === 'new' || order.orderStatus === 'ready_for_pickup'" class="text-[10px] text-amber-500 font-mono mt-1 font-bold">
                  КОД: {{ order.pickupCode || '—' }}
                </div>
                <span class="text-[10px] text-zinc-500 block font-mono mt-0.5">{{ formatDate(order.createdAt) }}</span>
              </td>
              
              <!-- Клиент -->
              <td class="p-3.5 align-top">
                <div class="font-bold text-white">{{ order.customerName || 'Клиент' }}</div>
                <div class="text-[10px] text-zinc-400 font-mono flex items-center gap-1 mt-0.5">
                  <i class="pi pi-phone text-[9px]"></i>
                  {{ maskPhone(order.customerPhone) }}
                </div>
              </td>
              
              <!-- Состав заказа (аккуратные компактные бейджи вместе, а не разорванные по краям) -->
              <td class="p-3.5 align-top">
                <div v-if="order.items && order.items.length > 0" class="flex flex-col gap-1.5">
                  <div 
                    v-for="item in order.items" 
                    :key="item.id" 
                    class="inline-flex items-center gap-2 bg-[#121215] border border-zinc-800/80 rounded-md px-2.5 py-1 text-xs w-fit"
                  >
                    <span class="font-medium text-white">{{ item.productName }}</span>
                    <span class="bg-[#B00D15]/20 text-[#B00D15] border border-[#B00D15]/40 text-[10px] font-mono font-bold px-1.5 py-0.2 rounded">
                      {{ item.size }}
                    </span>
                    <span class="text-zinc-400 font-mono text-[11px]">×{{ item.quantity }}</span>
                  </div>
                </div>
                <span v-else class="text-zinc-600 text-xs italic">— Позиции отсутствуют</span>
              </td>

              <!-- Сумма -->
              <td class="p-3.5 align-top font-black text-white font-mono text-sm whitespace-nowrap">
                {{ order.totalAmount?.toLocaleString() }} ₸
              </td>

              <!-- Статус оплаты -->
              <td class="p-3.5 align-top">
                <span v-if="order.paymentStatus === 'paid'" class="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                  Оплачено
                </span>
                <span v-else-if="order.paymentStatus === 'pending'" class="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/10 text-amber-400 border border-amber-500/30">
                  При получении
                </span>
                <span v-else-if="order.paymentStatus === 'refunded'" class="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-500/10 text-rose-400 border border-rose-500/30">
                  Возврат
                </span>
                <span v-else class="px-2 py-0.5 rounded text-[10px] font-bold bg-zinc-800 text-zinc-500">
                  Отменён
                </span>
              </td>

              <!-- Статус заказа с живым таймером -->
              <td class="p-3.5 align-top">
                <span :class="[
                  'px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider',
                  order.paymentStatus === 'refunded' ? 'bg-rose-950/80 text-rose-400 border border-rose-800' :
                  (order.orderStatus === 'completed' ? 'bg-zinc-800 text-zinc-400' : 
                  (order.orderStatus === 'ready_for_pickup' ? 'bg-emerald-950 text-emerald-400 border border-emerald-600' :
                  (order.orderStatus === 'cancelled' ? 'bg-rose-950 text-rose-500' : 'bg-blue-900/50 text-blue-400 border border-blue-800/50')))
                ]">
                  {{ getOrderStatusLabel(order) }}
                </span>

                <!-- ТИКАЮЩИЙ ТАЙМЕР РЕЗЕРВА -->
                <div v-if="order.orderStatus === 'new' || order.orderStatus === 'ready_for_pickup'" class="text-[10px] font-mono text-zinc-400 mt-1 flex items-center gap-1">
                  <i class="pi pi-clock text-[9px] text-amber-500"></i>
                  <span>{{ getRemainingTime(order.reservedUntil) }}</span>
                </div>
              </td>

              <!-- Действия: Собрать заказ -> Выдать заказ -> Возврат -->
              <td class="p-3.5 align-top text-right whitespace-nowrap space-y-1">
                <!-- Шаг 1: Собрать заказ (отправляет Email клиенту) -->
                <button 
                  v-if="order.orderStatus === 'new'" 
                  @click="assembleOrder(order)"
                  :disabled="order.assembling"
                  class="bg-blue-600 hover:bg-blue-500 text-white px-3 py-1.5 rounded-lg text-[10px] font-bold uppercase tracking-wider transition cursor-pointer shadow-md disabled:opacity-50"
                  title="Отправить клиенту уведомление на Email о готовности заказа к выдаче"
                >
                  <i v-if="order.assembling" class="pi pi-spin pi-spinner mr-1"></i>
                  {{ order.assembling ? 'Сборка...' : 'Собрать заказ' }}
                </button>

                <!-- Шаг 2: Выдать заказ (ввод пин-кода и списание) -->
                <button 
                  v-else-if="order.orderStatus === 'ready_for_pickup'" 
                  @click="openIssueModal(order)"
                  class="bg-[#B00D15] hover:bg-[#8F0B11] text-white px-3.5 py-1.5 rounded-lg text-[10px] font-bold uppercase tracking-wider transition cursor-pointer shadow-lg shadow-[#B00D15]/30 animate-pulse"
                >
                  Выдать заказ
                </button>

                <!-- Шаг 3: Оформить возврат -->
                <button 
                  v-else-if="order.orderStatus === 'completed' && order.paymentStatus !== 'refunded'" 
                  @click="refundOrder(order)"
                  class="text-zinc-500 hover:text-rose-400 underline text-[11px] transition cursor-pointer block text-right"
                  title="Оформить возврат денег клиенту и вернуть товар на полку склада"
                >
                  Оформить возврат
                </button>

                <span v-else-if="order.paymentStatus === 'refunded'" class="text-zinc-600 text-[10px] uppercase font-bold block">
                  ✓ Возвращён
                </span>
              </td>
            </tr>

            <tr v-if="orders.length === 0">
              <td colspan="7" class="text-center p-8 text-zinc-500">Заказы не найдены</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- МОДАЛКА ВЫДАЧИ ТОВАРА -->
    <Teleport to="body">
      <div v-if="isModalOpen" class="fixed inset-0 z-[100] bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
        <div class="bg-[#121215] border border-zinc-800 w-full max-w-md rounded-2xl shadow-2xl p-6 text-white space-y-5">
          
          <div class="text-center space-y-1">
            <h2 class="text-xl font-black uppercase">Выдача заказа {{ selectedOrder?.orderNumber }}</h2>
            <p class="text-xs text-zinc-400">
              Сверьте последние 4 цифры телефона: <strong class="text-white">{{ selectedOrder?.customerPhone?.slice(-4) }}</strong>
            </p>
          </div>

          <!-- Примерка и замена размера -->
          <div class="space-y-2 bg-[#18181D] p-3.5 rounded-xl border border-zinc-800">
            <span class="text-[10px] font-bold uppercase text-zinc-400 block tracking-wider">
              Примерка: замена размера (если не подошел)
            </span>
            <div v-for="item in selectedOrder?.items" :key="item.id" class="flex items-center justify-between text-xs pt-1">
              <span class="text-zinc-200 truncate max-w-[180px]">{{ item.productName }}</span>
              <div class="flex items-center gap-2">
                <span class="text-zinc-400 text-[11px]">Был: <strong class="text-white">[{{ item.size }}]</strong></span>
                <select 
                  @change="changeSize(selectedOrder.id, item.id, ($event.target as HTMLSelectElement).value)"
                  class="bg-[#121215] border border-zinc-700 text-white text-xs rounded px-2 py-1 outline-none cursor-pointer"
                >
                  <option value="" disabled selected>Сменить на...</option>
                  <option value="S">S</option>
                  <option value="M">M</option>
                  <option value="L">L</option>
                  <option value="XL">XL</option>
                  <option value="2XL">2XL</option>
                </select>
              </div>
            </div>
          </div>

          <!-- Оплата на стойке -->
          <div v-if="selectedOrder?.paymentStatus === 'pending'" class="space-y-2 bg-[#18181D] p-3.5 rounded-xl border border-zinc-800">
            <span class="text-xs font-bold uppercase text-amber-500 block">
              Оплата на стойке: {{ selectedOrder?.totalAmount?.toLocaleString() }} ₸
            </span>
            <div class="grid grid-cols-3 gap-2 pt-1">
              <button 
                v-for="method in ['Каспи QR', 'Терминал', 'Наличные']" 
                :key="method" 
                type="button"
                @click="actualPaymentMethod = method"
                :class="['py-2 text-[10px] font-bold uppercase rounded-lg border transition cursor-pointer', actualPaymentMethod === method ? 'bg-amber-500 text-black border-amber-500' : 'bg-[#121215] text-zinc-400 border-zinc-700 hover:text-white']"
              >
                {{ method }}
              </button>
            </div>
          </div>

          <!-- Ввод PIN-кода -->
          <div class="space-y-1.5 text-center">
            <label class="text-[11px] font-bold uppercase text-zinc-400 block">Пин-код выдачи из SMS/Приложения</label>
            <input 
              v-model="inputPin" 
              type="text" 
              maxlength="4" 
              placeholder="0000" 
              class="w-full bg-[#18181D] border border-zinc-700 text-center text-3xl font-mono font-black text-white rounded-xl py-2.5 focus:border-[#B00D15] outline-none tracking-[0.5em]"
            />
          </div>

          <div class="flex gap-3 pt-2">
            <button @click="isModalOpen = false" class="flex-1 bg-[#18181D] hover:bg-zinc-800 border border-zinc-700 text-zinc-300 py-3 rounded-xl text-xs font-bold uppercase transition cursor-pointer">
              Отмена
            </button>
            <button @click="confirmIssueOrder" class="flex-1 bg-[#B00D15] hover:bg-[#8F0B11] text-white py-3 rounded-xl text-xs font-black uppercase transition shadow-lg shadow-[#B00D15]/30 cursor-pointer">
              Подтвердить выдачу
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useUserStore } from '@/auth/model/authStore'
import { showSuccess, showError } from '@/shared/lib/toastService'
import { useConfirmDialog } from '@/shared/lib/useConfirm'

const { show: showConfirm } = useConfirmDialog()
const userStore = useUserStore()
const orders = ref<any[]>([])
const selectedBranch = ref(0)
const searchQuery = ref('')
const now = ref(Date.now())

let timerInterval: any = null

const isModalOpen = ref(false)
const selectedOrder = ref<any>(null)
const inputPin = ref('')
const actualPaymentMethod = ref('Каспи QR')

const isSuperAdmin = computed(() => {
  const roles = Array.isArray(userStore.roles) ? userStore.roles : [userStore.roles]
  return roles.includes('ROLE_ADMIN')
})

const currentManagerBranchName = computed(() => {
  if (isSuperAdmin.value) return null
  return orders.value.length > 0 ? orders.value[0].branchName : null
})

// KPI метрики
const metrics = computed(() => {
  let totalRevenue = 0
  let branchCash = 0
  let onlineRevenue = 0
  let paidCount = 0
  let pendingCount = 0
  let pendingAmount = 0
  let refundedAmount = 0
  let refundedCount = 0

  orders.value.forEach(o => {
    if (o.paymentStatus === 'refunded') {
      refundedAmount += o.totalAmount || 0
      refundedCount++
    } else if (o.paymentStatus === 'paid') {
      totalRevenue += o.totalAmount || 0
      paidCount++
      if (['Каспи QR', 'Терминал', 'Наличные', 'at_branch'].includes(o.paymentMethod)) {
        branchCash += o.totalAmount || 0
      } else {
        onlineRevenue += o.totalAmount || 0
      }
    } else if (o.orderStatus === 'new' || o.orderStatus === 'ready_for_pickup') {
      pendingCount++
      pendingAmount += o.totalAmount || 0
    }
  })

  return { totalRevenue, branchCash, onlineRevenue, paidCount, pendingCount, pendingAmount, refundedAmount, refundedCount }
})

// ТИКАЮЩИЙ ТАЙМЕР 24 ЧАСОВ
const getRemainingTime = (reservedUntilStr: string) => {
  if (!reservedUntilStr) return '24ч резерв'
  const target = new Date(reservedUntilStr).getTime()
  const diff = target - now.value

  if (diff <= 0) return 'Бронь истекла'

  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)

  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`
}

const maskPhone = (phone: string) => {
  if (!phone) return '—'
  if (phone.length <= 4) return phone
  return phone.replace(/(\+7\s?\(\d{3}\)\s?)\d{3}-\d{2}-(\d{2})/, '$1***-**-$2')
}

const getOrderStatusLabel = (order: any) => {
  if (order.paymentStatus === 'refunded') return 'Возврат'
  if (order.orderStatus === 'completed') return 'Выдан'
  if (order.orderStatus === 'ready_for_pickup') return 'Готов к выдаче'
  if (order.orderStatus === 'cancelled') return 'Отменён'
  if (order.orderStatus === 'new') return 'В резерве'
  return order.orderStatus
}

const getAuthHeaders = () => {
  const token = userStore.token || localStorage.getItem('token') || ''
  return {
    Authorization: token.startsWith('Bearer ') ? token : `Bearer ${token}`
  }
}

const fetchOrders = async () => {
  const params = new URLSearchParams()
  if (selectedBranch.value > 0) params.append('branchId', String(selectedBranch.value))
  if (searchQuery.value) params.append('search', searchQuery.value)

  try {
    const res = await fetch(`/api/admin/v1/orders?${params.toString()}`, {
      headers: getAuthHeaders()
    })
    if (res.ok) orders.value = await res.json()
  } catch {}
}

// ДЕЙСТВИЕ: Сборка заказа и отправка email клиенту
const assembleOrder = async (order: any) => {
  order.assembling = true
  try {
    const res = await fetch(`/api/admin/v1/orders/${order.id}/assemble`, {
      method: 'POST',
      headers: getAuthHeaders()
    })
    if (res.ok) {
      showSuccess(`Заказ ${order.orderNumber} собран! Письмо отправлено на ${order.customerEmail}`)
      await fetchOrders()
    } else {
      const err = await res.json().catch(() => ({}))
      showError(err.error || 'Ошибка при сборке заказа')
    }
  } catch {
    showError('Сбой связи с сервером')
  } finally {
    order.assembling = false
  }
}

const openIssueModal = (order: any) => {
  selectedOrder.value = order
  inputPin.value = ''
  actualPaymentMethod.value = order.paymentStatus === 'paid' ? order.paymentMethod : 'Каспи QR'
  isModalOpen.value = true
}

const changeSize = async (orderId: number, itemId: number, newSize: string) => {
  try {
    const res = await fetch(`/api/admin/v1/orders/${orderId}/items/${itemId}/change-size`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders()
      },
      body: JSON.stringify({ newSize })
    })

    if (res.ok) {
      showSuccess(`Размер успешно изменен на ${newSize}`)
      selectedOrder.value = await res.json()
      await fetchOrders()
    } else {
      const err = await res.json().catch(() => ({}))
      showError(err.error || 'Ошибка смены размера')
    }
  } catch {
    showError('Сбой сервера при смене размера')
  }
}

const confirmIssueOrder = async () => {
  if (inputPin.value.length !== 4) {
    showError('Введите 4-значный пин-код')
    return
  }

  try {
    const res = await fetch(`/api/admin/v1/orders/${selectedOrder.value.id}/issue`, {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        ...getAuthHeaders()
      },
      body: JSON.stringify({ 
        pickupCode: inputPin.value,
        paymentMethod: actualPaymentMethod.value 
      })
    })

    if (res.ok) {
      showSuccess('Заказ успешно выдан клиенту! Складские остатки списаны.')
      isModalOpen.value = false
      fetchOrders()
    } else {
      const err = await res.json().catch(() => ({}))
      showError(err.error || 'Неверный пин-код или ошибка выдачи')
    }
  } catch {
    showError('Сбой сервера')
  }
}

// Замените функцию refundOrder на этот код:
const refundOrder = async (order: any) => {
  const confirmed = await showConfirm({
    title: 'Оформление возврата',
    message: `Вы уверены, что хотите оформить возврат по заказу ${order.orderNumber}? Деньги будут возвращены клиенту, а товар вернется на полку склада.`,
    confirmText: 'Да, оформить возврат'
  })

  if (!confirmed) return

  try {
    const res = await fetch(`/api/admin/v1/orders/${order.id}/refund`, {
      method: 'PATCH',
      headers: getAuthHeaders()
    })

    if (res.ok) {
      showSuccess(`Возврат по заказу ${order.orderNumber} успешно оформлен!`)
      await fetchOrders()
    } else {
      const err = await res.json().catch(() => ({}))
      showError(err.error || 'Не удалось оформить возврат')
    }
  } catch {
    showError('Сбой соединения с сервером')
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('ru-RU', { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  fetchOrders()
  // Обновляем таймер каждую секунду
  timerInterval = setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>