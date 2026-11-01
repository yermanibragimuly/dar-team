<script setup lang="ts">
</script>

<template>
  <footer class="w-full bg-[#0d0f12] text-white flex justify-center items-center border-t border-zinc-900">
    <div class="w-full max-w-[1280px] min-h-[100px] px-[32px] py-4 flex flex-col md:flex-row justify-between items-center gap-6">
      
      <!-- Секция слева: Логотип + Копирайт -->
      <div class="flex items-center gap-4 flex-none">
        <router-link to="/">
          <img 
            src="../assets/img/logoMain.png"
            alt="DAR TEAM" 
            class="h-8 w-auto object-contain"
          />
        </router-link>
        <span class="text-[#8c94a0] text-sm uppercase tracking-wider font-medium">
          © 2025 DAR TEAM. ВСЕ ПРАВА ЗАЩИЩЕНЫ.
        </span>
      </div>

      <!-- Секция по центру: Контакты строго в одну строчку -->
      <div class="flex flex-wrap items-center justify-center gap-6 text-sm">
        <router-link 
          to="/branch" 
          class="text-white hover:text-gray-300 transition-colors font-medium uppercase tracking-wide"
        >
          КОНТАКТЫ:
        </router-link>

        <!-- Телефон в строчку -->
        <a 
          href="tel:+77472434438" 
          class="text-white hover:text-[#B00D15] transition-colors font-bold flex items-center gap-2 whitespace-nowrap"
        >
          <i class="pi pi-phone text-xs text-[#B00D15]"></i>
          <span>+7-747-243-4438</span>
        </a>

        <span class="text-zinc-600 hidden sm:inline">•</span>

        <!-- Email в строчку -->
        <a 
          href="mailto:darteam@dar.io" 
          class="text-white hover:text-[#B00D15] transition-colors font-bold flex items-center gap-2 whitespace-nowrap"
        >
          <i class="pi pi-envelope text-xs text-[#B00D15]"></i>
          <span>darteam@dar.io</span>
        </a>
      </div>

      <!-- Секция справа: Социальные сети -->
      <div class="flex items-center gap-3 flex-none">
        <a 
          href="https://www.instagram.com/darteam.kazakhstan/" 
          target="_blank" 
          rel="noopener noreferrer"
          class="w-10 h-10 rounded-full bg-[#1b1e24] flex justify-center items-center text-gray-300 hover:bg-[#B00D15] hover:text-white transition-all cursor-pointer"
        >
          <i class="pi pi-instagram text-lg"></i>
        </a>

        <a 
          href="https://www.youtube.com/@DARTeam-pro" 
          target="_blank" 
          rel="noopener noreferrer"
          class="w-10 h-10 rounded-full bg-[#1b1e24] flex justify-center items-center text-gray-300 hover:bg-[#B00D15] hover:text-white transition-all cursor-pointer"
        >
          <i class="pi pi-youtube text-lg"></i>
        </a>

        <a 
          href="https://www.tiktok.com/@darteam_kazakhstan" 
          target="_blank" 
          rel="noopener noreferrer"
          class="w-10 h-10 rounded-full bg-[#1b1e24] flex justify-center items-center text-gray-300 hover:bg-[#B00D15] hover:text-white transition-all cursor-pointer"
        >
          <i class="pi pi-tiktok text-lg"></i>
        </a>
      </div>

    </div>
  </footer>
</template>