<script setup lang="ts">
import Header from '../layouts/Header.vue';
import Footer from '../layouts/Footer.vue';
import useConfirm from '@/shared/ui/useConfirm.vue'
</script>

<template>
  <div class="app-layout">    
    <Header/>
    <main class="app-content">
      <router-view />
      <useConfirm/>
    </main>
    <Footer/>
  </div>
</template>
