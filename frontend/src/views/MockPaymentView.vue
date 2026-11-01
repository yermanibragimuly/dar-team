<template>
  <div class="min-h-screen bg-[#0A0A0C] flex items-center justify-center p-4 text-white">
    <div class="w-full max-w-md bg-[#121215] border border-zinc-800 rounded-2xl p-6 shadow-2xl text-center space-y-6">
      <div class="inline-block bg-amber-500/10 text-amber-500 px-3 py-1 rounded-full text-xs font-mono font-bold">
        [ТЕСТОВЫЙ ЭКВАЙРИНГ: KASPI / FREEDOM]
      </div>

      <div>
        <h1 class="text-xl font-black uppercase text-white">Оплата заказа {{ order?.orderNumber }}</h1>
        <p class="text-xs text-zinc-400 mt-1">
          Сумма к списанию: <strong class="text-white text-base">{{ order?.totalAmount?.toLocaleString() }} ₸</strong>
        </p>
      </div>

      <div class="bg-[#18181D] p-4 rounded-xl border border-zinc-800">
        <span class="text-xs text-zinc-400 block font-semibold">Время сессии</span>
        <span class="text-2xl font-mono font-bold text-red-500">{{ timerDisplay }}</span>
      </div>

      <div class="space-y-3">
        <button 
          @click="sendCallback('paid')"
          :disabled="processing"
          class="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-3.5 rounded-xl text-xs uppercase tracking-wider transition cursor-pointer disabled:opacity-50"
        >
          {{ processing ? 'Обработка транзакции...' : '✓ [ТЕСТ] Успешная оплата' }}
        </button>
        <button 
          @click="sendCallback('failed')"
          :disabled="processing"
          class="w-full bg-[#18181D] hover:bg-red-950/80 text-zinc-400 hover:text-red-400 font-bold py-3 rounded-xl text-xs uppercase tracking-wider transition border border-zinc-800 cursor-pointer disabled:opacity-50"
        >
          ✕ [ТЕСТ] Отклонить платёж
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const order = ref<any>(null)
const processing = ref(false)
const timeLeft = ref(600)

const timerDisplay = computed(() => {
  const m = Math.floor(timeLeft.value / 60)
  const s = timeLeft.value % 60
  return `${m}:${s < 10 ? '0' : ''}${s}`
})

onMounted(async () => {
  const res = await fetch(`/api/v1/shop/orders/${route.params.orderId}`)
  if (res.ok) {
    order.value = await res.json()
  }

  const interval = setInterval(() => {
    if (timeLeft.value > 0) timeLeft.value--
    else clearInterval(interval)
  }, 1000)
})

const sendCallback = async (status: 'paid' | 'failed') => {
  processing.value = true
  try {
    const res = await fetch('/api/v1/shop/mock-payment/callback', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orderId: route.params.orderId, status })
    })

    if (res.ok) {
      if (status === 'paid') {
        router.push(`/checkout/success?order=${order.value?.orderNumber}&mode=online`)
      } else {
        alert('Платеж отклонен. Заказ отменен, остатки возвращены на склад.')
        router.push('/shop')
      }
    }
  } catch (err) {
    alert('Ошибка шлюза оплаты')
  } finally {
    processing.value = false
  }
}
</script>