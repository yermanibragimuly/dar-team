<script setup lang="ts">
</script>

<template>
  <section class="relative w-full py-24 px-6 flex justify-center items-center overflow-hidden min-h-[500px] bg-black">
    
    <div class="absolute inset-0 z-0">
      <img 
        src="/src/assets/img/dar_team_10_years.webp" 
        alt="Главная картинка" 
        class="w-full h-full object-cover filter grayscale-[50%] brightness-[0.4]"
      />
    </div>

    <div class="absolute inset-0 bg-black/60 z-10 bg-radial-gradient"></div>

    <div class="relative z-20 max-w-3xl text-center space-y-6">
      <h2 class="text-3xl md:text-5xl font-bold text-white uppercase tracking-widest drop-shadow-2xl uppercase">
        {{ $t('lable.darTimOutOctagon') }}
      </h2>
      
      <div class="space-y-4">
        <p class="text-gray-200 text-lg md:text-xl leading-relaxed font-medium">
          {{ $t('outsidePage.history') }}
        </p>
        <p class="text-gray-200 text-lg md:text-xl leading-relaxed font-medium">
          {{ $t('outsidePage.proTeam') }}
        </p>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* Добавим эффект виньетки (затемнения по краям) для сходства с первым фото */
.bg-radial-gradient {
  background: radial-gradient(circle, rgba(0,0,0,0) 0%, rgba(0,0,0,0.8) 100%);
}
</style>
