<script setup lang="ts">
import { ref } from 'vue';
import type { Project } from '../../../types/project';


const projeCtcard = ref<Project[]>([
  {
    id: 1,
    title: "DAR TEAM завоевал новые победы",
    date: "2025-09-01",
    img: "/src/assets/img/Page1.jpg",
  },
  {
    id: 2,
    title: "Открытие нового зала в Алматы",
    date: "2025-08-20",
    img: "/src/assets/img/Page2.jpg",
  },
  {
    id: 3,
    title: "Шавкат Рахмонов готовится к бою",
    date: "2025-08-15",
    img: "/src/assets/img/Page3.jpg",
  },
])

</script>

<template>
  <div class="text-white py-12 px-4 sm:px-6 lg:px-12">
    <h2 class="text-3xl font-extrabold mb-10 text-center tracking-wider">
      НАШИ ПРОЕКТЫ
    </h2>
  </div>
  <div class="container mx-auto main-h-screen px-4 py-8">
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <Card 
        v-for="value in projeCtcard" 
        :key="value.id"
        class="w-full h-full max-w-sm mx-auto flex flex-col"
      >
        <template #header>
          <img 
            alt="user header" 
            src="../../../assets/img/Page1.jpg" 
            class="w-full h-48 object-cover"
          />
        </template>

        <template #title>
          <h3 class="text-lg font-semibold line-clamp-2">
            {{ value.title }}
          </h3>
        </template>

        <template #subtitle>
          <p class="text-sm text-gray-500">{{ value.date }}</p>
        </template>
      </Card>
    </div>
  </div>
</template>