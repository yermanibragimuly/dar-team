<script setup lang="ts">
import MainBlockNews from '../../newsPage/mainNews/MainBlockNews.vue';


</script>

<template>
  <div class="flex flex-col h-full w-full">

    <div class="h-24 flex items-center justify-center">
      <h1 class="text-4xl text-white uppercase">{{ $t('menu.news') }}</h1>
    </div>
    <div class="flex-grow w-full">
      <div class="max-w-7xl mx-auto px-6">
        <MainBlockNews :newLimit="3" hide-pagination/>
      </div>
    </div>
    <div class="flex justify-center mt-4">
      <router-link to="/news" class="btn">
        <p class="uppercase">
          {{ $t('button.viewAll') }}
        </p>
      </router-link>
    </div>

  </div>
</template>

<style scoped>
.btn {
    padding: 12px 24px; /* Внутренние отступы */
    background-color: #B00D15; /* Цвет фона */
    color: white; /* Цвет текста */
    border-radius: 5px; /* Скругление углов */
    text-decoration: none; /* Убираем подчеркивание */
    transition: background-color 0.3s; /* Плавный переход цвета */
    margin: 12px 0; /* Верхний и нижний отступы */
}
.btn:hover {
    background-color: #9B0B12; /* Цвет фона при наведении (темнее) */
}
</style>