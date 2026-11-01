<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPublicPartners, type Partner } from '@/services/partner-services'

const partners = ref<Partner[]>([])

onMounted(async () => {
  try {
    partners.value = await getPublicPartners()
  } catch {}
})
</script>

<template>
  <div class="w-full flex flex-col">
    <!-- Фоновое фото команды -->
    <div class="relative w-full h-[632px]">
      <img 
        src="/src/assets/img/Main_img3.webp" 
        alt="DAR TEAM" 
        class="w-full h-full object-cover"
      />
    </div>

    <!-- Красный баннер с партнерами -->
    <div class="relative w-full bg-[#B00D15] min-h-[335px] flex flex-col justify-center items-center gap-8 text-white py-12 px-6">
      <h2 class="text-3xl sm:text-4xl font-extrabold uppercase tracking-wide">
        {{ $t('lable.ourPartners') || 'НАШИ ПАРТНЕРЫ' }}
      </h2>

      <!-- Сетка логотипов из базы данных -->
      <div class="flex flex-wrap items-center justify-center gap-10 md:gap-14 max-w-7xl mx-auto">
        <a 
          v-for="item in partners" 
          :key="item.id"
          :href="item.websiteUrl || '#'"
          :target="item.websiteUrl ? '_blank' : '_self'"
          class="flex items-center justify-center transition-transform hover:scale-110 duration-300"
          :title="item.name"
        >
          <img 
            :src="item.logoUrl" 
            :alt="item.name" 
            class="max-w-[170px] max-h-[70px] w-auto h-auto object-contain filter drop-shadow"
          />
        </a>

        <!-- Fallback на случай пустой базы -->
        <div v-if="partners.length === 0" class="text-white/80 text-sm font-medium">
          Партнеры обновляются через панель управления
        </div>
      </div>
    </div>
  </div>
</template>