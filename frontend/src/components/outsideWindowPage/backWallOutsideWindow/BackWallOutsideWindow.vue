<script setup lang="ts">
    import HeadOutsideWindow from '../headOutsideWindow/HeadOutsideWindow.vue';
    // import MainBlockOutsideWindow from '../mainBlockOutsideWindow/MainBlockOutsideWindow.vue';
</script>

<template>
<div class="flex flex-col h-full">
  <HeadOutsideWindow class="head"/>
  <!-- <MainBlockOutsideWindow class="flex-grow"/> -->
</div>
</template>

<style scoped>
.head {
  height: 80vh;
}
</style>
