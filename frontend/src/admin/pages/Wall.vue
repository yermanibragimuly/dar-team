<template>
  <div class="min-h-screen bg-[#0e0e11] text-white">
    <SidebarMenu />
    <main class="pl-72 pt-16 min-h-screen p-8">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import SidebarMenu from './SidebarMenu.vue'
</script>