<template>
  <div class="min-h-screen bg-[#0A0A0C] text-zinc-100 py-10 px-4 sm:px-6 lg:px-8">
    <div class="max-w-7xl mx-auto">
      <h1 class="text-3xl sm:text-4xl font-black uppercase tracking-tight text-white mb-8">
        ОФОРМЛЕНИЕ ЗАКАЗА
      </h1>

      <div v-if="cartStore.items.length === 0" class="bg-[#121215] border border-zinc-800 rounded-2xl p-12 text-center space-y-4">
        <div class="w-16 h-16 rounded-full bg-zinc-800 flex items-center justify-center mx-auto text-zinc-500">
          <i class="pi pi-shopping-bag text-2xl"></i>
        </div>
        <h2 class="text-lg font-bold text-white">Ваша корзина пуста</h2>
        <p class="text-xs text-zinc-400">Выберите экипировку в официальном каталоге DAR TEAM</p>
        <router-link to="/shop" class="inline-block bg-[#B00D15] hover:bg-[#8F0B11] text-white px-6 py-2.5 rounded-lg text-xs font-bold uppercase tracking-wider transition">
          Перейти в магазин
        </router-link>
      </div>

      <form v-else @submit.prevent="submitOrder" class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        <div class="lg:col-span-8 space-y-6">
          
          <!-- 1. Контакты -->
          <div class="rounded-2xl border border-zinc-800 bg-[#121215] p-6 space-y-4">
            <div class="flex items-center gap-3">
              <span class="w-6 h-6 rounded-full bg-[#B00D15] text-white text-xs font-bold flex items-center justify-center">1</span>
              <h2 class="text-sm font-black uppercase tracking-wider text-white">Контактные данные</h2>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label class="block text-[11px] font-semibold uppercase text-zinc-400 mb-1.5">ФИО Получателя *</label>
                <input v-model="form.customerName" required placeholder="Ерман Ибрагимулы" class="w-full bg-[#18181D] border border-zinc-800 focus:border-red-600 rounded-lg p-2.5 text-xs text-white outline-none" />
              </div>
              <div>
                <label class="block text-[11px] font-semibold uppercase text-zinc-400 mb-1.5">Телефон *</label>
                <input v-model="form.customerPhone" required placeholder="+7 (___) ___-__-__" class="w-full bg-[#18181D] border border-zinc-800 focus:border-red-600 rounded-lg p-2.5 text-xs text-white outline-none" />
              </div>
              <div class="sm:col-span-2">
                <label class="block text-[11px] font-semibold uppercase text-zinc-400 mb-1.5">Email (для квитанции и статуса) *</label>
                <input v-model="form.customerEmail" type="email" required placeholder="example@dar.io" class="w-full bg-[#18181D] border border-zinc-800 focus:border-red-600 rounded-lg p-2.5 text-xs text-white outline-none" />
              </div>
            </div>
          </div>

          <!-- 2. Пункт самовывоза -->
          <div class="rounded-2xl border border-zinc-800 bg-[#121215] p-6 space-y-4">
            <div class="flex items-center gap-3">
              <span class="w-6 h-6 rounded-full bg-[#B00D15] text-white text-xs font-bold flex items-center justify-center">2</span>
              <h2 class="text-sm font-black uppercase tracking-wider text-white">Пункт самовывоза и примерки (Зал DAR)</h2>
            </div>

            <div class="space-y-2.5">
              <label 
                v-for="b in branches" 
                :key="b.id"
                :class="[
                  'flex items-center justify-between p-4 rounded-xl border cursor-pointer transition',
                  form.branchId === b.id ? 'border-[#B00D15] bg-[#18181D]' : 'border-zinc-800 bg-[#141418] hover:border-zinc-700'
                ]"
              >
                <div class="flex items-center gap-3">
                  <input type="radio" :value="b.id" v-model="form.branchId" class="accent-[#B00D15]" />
                  <div>
                    <span class="text-xs font-bold text-white block">{{ b.name }}</span>
                    <span class="text-[11px] text-zinc-400">{{ b.address }} • {{ b.phone }}</span>
                  </div>
                </div>
                <span class="text-[10px] font-bold text-emerald-400 uppercase">Бесплатно</span>
              </label>
            </div>
          </div>

          <!-- 3. Способ оплаты -->
          <div class="rounded-2xl border border-zinc-800 bg-[#121215] p-6 space-y-4">
            <div class="flex items-center gap-3">
              <span class="w-6 h-6 rounded-full bg-[#B00D15] text-white text-xs font-bold flex items-center justify-center">3</span>
              <h2 class="text-sm font-black uppercase tracking-wider text-white">Способ оплаты</h2>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <button 
                type="button" 
                v-for="m in paymentMethods" :key="m.id"
                @click="form.paymentMethod = m.id"
                :class="[
                  'p-4 text-left rounded-xl border transition cursor-pointer',
                  form.paymentMethod === m.id
                    ? 'border-[#B00D15] bg-[#18181D] text-white'
                    : 'border-zinc-800 bg-[#141418] text-zinc-400 hover:text-white'
                ]"
              >
                <div class="text-xs font-bold">{{ m.label }}</div>
                <div class="text-[10px] text-zinc-500 mt-1">{{ m.desc }}</div>
              </button>
            </div>
          </div>

        </div>

        <!-- Правая колонка: Итоги -->
        <div class="lg:col-span-4 bg-[#121215] border border-zinc-800 rounded-2xl p-6 space-y-5 sticky top-24">
          <div class="flex justify-between items-center border-b border-zinc-800 pb-3">
            <h3 class="text-xs font-bold uppercase tracking-wider text-white">Ваш заказ</h3>
            <span class="text-[10px] font-mono text-zinc-400">{{ cartStore.totalCount }} шт.</span>
          </div>

          <div class="space-y-3 max-h-64 overflow-y-auto pr-1">
            <div v-for="item in cartStore.items" :key="item.productId + item.size" class="p-2.5 rounded-lg bg-[#18181D] border border-zinc-800 space-y-2">
              <div class="flex justify-between items-start text-xs">
                <div>
                  <span class="font-bold text-white block">{{ item.name }}</span>
                  <span class="text-zinc-400 text-[11px]">Размер: <strong class="text-white">{{ item.size }}</strong></span>
                </div>
                <button type="button" @click="cartStore.removeItem(item.productId, item.size)" class="text-zinc-600 hover:text-red-500">
                  <i class="pi pi-trash text-xs"></i>
                </button>
              </div>

              <!-- Корректировка количества с учетом остатка -->
              <div class="flex items-center justify-between text-xs pt-1 border-t border-zinc-800/80">
                <div class="flex items-center gap-2">
                  <button
                    type="button"
                    @click="cartStore.updateQuantity(item.productId, item.size, item.quantity - 1)"
                    class="w-6 h-6 rounded bg-zinc-800 hover:bg-zinc-700 text-white flex items-center justify-center font-bold"
                  >
                    -
                  </button>
                  <span class="font-mono font-bold text-white px-1">{{ item.quantity }}</span>
                  <button
                    type="button"
                    :disabled="item.quantity >= (item.maxStock || 999)"
                    @click="cartStore.updateQuantity(item.productId, item.size, item.quantity + 1)"
                    class="w-6 h-6 rounded bg-zinc-800 hover:bg-zinc-700 text-white flex items-center justify-center font-bold disabled:opacity-30 disabled:cursor-not-allowed"
                  >
                    +
                  </button>
                  <span v-if="item.maxStock" class="text-[9px] text-zinc-500">макс: {{ item.maxStock }}</span>
                </div>
                <span class="font-bold text-white">{{ (item.price * item.quantity).toLocaleString() }} ₸</span>
              </div>
            </div>
          </div>

          <div class="border-t border-zinc-800 pt-3 space-y-2 text-xs">
            <div class="flex justify-between text-zinc-400">
              <span>Самовывоз и примерка</span>
              <span class="text-emerald-400 font-bold">0 ₸</span>
            </div>
            <div class="flex justify-between items-baseline pt-2">
              <span class="font-bold uppercase text-zinc-300">Итого к оплате</span>
              <span class="text-2xl font-black text-white">{{ cartStore.totalPrice.toLocaleString() }} ₸</span>
            </div>
          </div>

          <button 
            type="submit" 
            :disabled="loading"
            class="w-full bg-[#B00D15] hover:bg-[#8F0B11] text-white py-3.5 rounded-xl text-xs font-black uppercase tracking-wider transition shadow-lg shadow-[#B00D15]/30 cursor-pointer disabled:opacity-50"
          >
            {{ loading ? 'Создание заказа...' : (form.paymentMethod === 'at_branch' ? 'Оформить с оплатой в зале' : 'Перейти к оплате') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/features/cart/cartStore'
import { useUserStore } from '@/auth/model/authStore'
import { showError } from '@/shared/lib/toastService'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const loading = ref(false)

const branches = ref<any[]>([
  { id: 1, name: 'DAR PRO GYM ALMATY', address: 'ул. Достык 160, БЦ Premier', phone: '+7 (727) 345-00-11' },
  { id: 2, name: 'DAR TEAM GYM ALMATY', address: 'ул. Ораза Жандосова, 87', phone: '+7 (708) 205-06-10' },
  { id: 3, name: 'DAR TEAM FIGHT GYM ASTANA', address: 'пр. Туран 24, СК Барыс', phone: '+7 (7172) 99-88-12' }
])

const paymentMethods = [
  { id: 'online_card', label: 'Банковская карта', desc: 'Visa / Mastercard' },
  { id: 'online_qr', label: 'Kaspi / QR', desc: 'Универсальный QR' },
  { id: 'at_branch', label: 'Оплата в зале', desc: 'Примерка и оплата на ресепшн' }
]

const form = ref({
  customerName: '',
  customerPhone: '',
  customerEmail: '',
  branchId: 1,
  paymentMethod: 'online_card'
})

onMounted(async () => {
  try {
    const res = await fetch('/api/v1/shop/branches')
    if (res.ok) {
      const data = await res.json()
      if (Array.isArray(data) && data.length > 0) branches.value = data
    }
  } catch {}
})

const submitOrder = async () => {
  loading.value = true
  try {
    const payload = {
      ...form.value,
      items: cartStore.items.map(i => ({
        productId: i.productId,
        size: i.size,
        quantity: i.quantity
      }))
    }

    const token = userStore.token || localStorage.getItem('token') || localStorage.getItem('jwt')
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    if (token) {
      headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`
    }

    const res = await fetch('/api/v1/shop/orders', {
      method: 'POST',
      headers,
      body: JSON.stringify(payload)
    })

    const data = await res.json()
    if (!res.ok) throw new Error(data.error || 'Ошибка оформления заказа')

    cartStore.clearCart()
    router.push(data.redirectUrl)
  } catch (err: any) {
    showError(err.message || 'Ошибка оформления')
  } finally {
    loading.value = false
  }
}
</script>