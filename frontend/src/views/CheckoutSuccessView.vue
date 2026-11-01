<template>
  <div class="min-h-screen bg-[#0A0A0C] text-white flex items-center justify-center p-4">
    <div class="max-w-md w-full bg-[#121215] border border-zinc-800 rounded-2xl p-8 text-center space-y-6 shadow-2xl">
      <div class="w-16 h-16 rounded-full bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center mx-auto text-emerald-400">
        <i class="pi pi-check text-2xl"></i>
      </div>

      <div>
        <h1 class="text-2xl font-black uppercase tracking-tight">Заказ успешно оформлен!</h1>
        <p class="text-sm font-mono text-zinc-400 mt-2">
          Номер заказа: <strong class="text-white text-base">{{ orderNumber }}</strong>
        </p>
      </div>

      <div class="p-4 rounded-xl bg-[#18181D] border border-zinc-800 text-xs text-zinc-300 space-y-2 text-left">
        <div class="font-bold text-white uppercase text-[11px]">Инструкция по получению:</div>
        <p v-if="mode === 'branch'">
          Ваш заказ принят. Оплата и выдача производятся на ресепшн выбранного зала DAR GYM при примерке. Назовите администратору номер заказа.
        </p>
        <p v-else>
          Оплата прошла успешно! Ваш заказ передан на сборку. Вы получите уведомление о готовности к выдаче на стойке ресепшн.
        </p>
      </div>

      <router-link to="/shop" class="inline-block w-full bg-[#B00D15] hover:bg-[#8e0a11] text-white py-3 rounded-lg text-xs font-bold uppercase tracking-wider transition">
        Вернуться в магазин
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const orderNumber = computed(() => route.query.order || 'DAR-1000')
const mode = computed(() => route.query.mode || 'online')
</script>