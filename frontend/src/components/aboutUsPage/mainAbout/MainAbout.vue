<script setup lang="ts">

</script>

<template>
  <section class="bg-[#0D0D0D] text-white w-full py-20 px-6 shadow-2xl">
    
    <h2 class="text-2xl sm:text-3xl font-extrabold mb-16 text-center tracking-tight uppercase border-b-2 border-red-700/50 pb-2 uppercase">
      {{ $t('lable.director') }}
    </h2>

    <div class="max-w-4xl grid grid-cols-1 md:grid-cols-2 gap-12 ml-16">
      
      <div class="flex justify-center md:justify-end">
        <div class="relative max-w-sm w-full">
          <img 
            src="/src/assets/img/AlidarU.jpg" 
            alt="Алидар Утемуратов" 
            class="
              rounded-xl 
              shadow-2xl 
              shadow-red-900/20 
              hover:shadow-red-900/15 
              transition-shadow 
              duration-500
              object-cover 
              w-full 
              h-auto
            "
          />
          </div>
      </div>

      <div class="space-y-4 text-center md:text-left">
        <p class="text-xl font-medium text-red-500 uppercase tracking-wider">{{ $t('lable.founderClub') }}</p>
        <h3 class="text-4xl lg:text-5xl font-black leading-tight bg-clip-text text-transparent bg-gradient-to-r from-white to-gray-400">
          {{ $t('lable.founderClubName') }}
        </h3>
        
        <p class="text-gray-300 italic pt-4 max-w-md md:max-w-none mx-auto md:mx-0">
          {{ $t('lable.speech') }}
        </p>
        
        </div>
    </div>
  </section>
</template>
