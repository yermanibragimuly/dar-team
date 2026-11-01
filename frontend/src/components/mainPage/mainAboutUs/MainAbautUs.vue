<template>
  <div class="bg-black text-white w-full py-16 px-6">
    <div class="flex flex-col md:flex-row items-center md:items-start max-w-7xl mx-auto gap-28">
      
      <!-- Левая часть с картинкой -->
      <div class="w-full md:w-1/2 flex justify-center">
        <img 
          src="/src/assets/img/Main_img1.png" 
          alt="DAR TEAM" 
          class="rounded-xl shadow-lg max-w-full"
        />
      </div>

      <!-- Правая часть с текстом -->
      <div class="w-full md:w-1/2 leading-relaxed">
        <h2 class="text-3xl font-bold uppercase mb-6">{{ $t('menu.aboutUs') }}</h2>
        <p class="text-gray-300 mb-4">
          {{ $t('aboutPage.history') }}
        </p>

        <p class="text-gray-300 mb-4">
          {{ $t('aboutPage.proTeam') }}
        </p>

        <p class="text-gray-300">
          {{ $t('aboutPage.coaches') }}
        </p>
      </div>
    </div>
  </div>
</template>
