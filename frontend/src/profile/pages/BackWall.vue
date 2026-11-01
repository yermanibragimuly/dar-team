<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { showError } from '@/shared/lib/toastService';
import { getDataProfile } from '@/services/user/profile-services';

const profile = ref({
  firstName: '',
  lastName: '',
  phone: '',
  bio: '',
  birthDate: null,
  avatarUrl: '',
  email: '',
  username: ''
});

const getProfile = async () => {
  try{
    let res = await getDataProfile()
    profile.value = {
      firstName: res.data.firstName || '',
      lastName:  res.data.lastName  || '',
      phone:     res.data.phone     || '',
      bio:       res.data.bio       || '',
      birthDate: res.data.birthDate || null,
      avatarUrl: res.data.avatarUrl || '',
      email: res.data.email || '',
      username: res.data.username  || ''
    }
  }catch(err){
  showError(err)
  }
}


onMounted( async ()=>{
  await getProfile();
})

</script>

<template>
  <div class="profile-container bg-[#121212] w-full flex flex-row min-h-[calc(100vh-64px)] items-stretch">
    
    <aside class="w-64 bg-[#18181B] border-r border-gray-800 p-6 flex flex-col flex-shrink-0">
      <div class="flex flex-col items-center mb-10">
        <div class="w-20 h-20 bg-gray-800 rounded-full mb-3 flex items-center justify-center border border-gray-700">
          <i class="pi pi-user text-2xl text-gray-400"></i>
        </div>
        <h2 class="text-white font-bold text-sm">{{ profile.firstName }}</h2>
        <span class="text-xs text-gray-500">Пользователь</span>
      </div>

      <nav class="space-y-2 flex-1">
        <router-link 
          to="/profile/info" 
          class="flex items-center gap-3 p-3 rounded-lg text-gray-400 hover:bg-gray-800 transition"
          exact-active-class="bg-emerald-500/10 text-emerald-500 font-medium"
        >
          <i class="pi pi-home"></i> <span>Профиль</span>
        </router-link>

        <router-link 
          to="/profile/videos" 
          class="flex items-center gap-3 p-3 rounded-lg text-gray-400 hover:bg-gray-800 transition"
          exact-active-class="bg-emerald-500/10 text-emerald-500 font-medium"
        >
          <i class="pi pi-video"></i> <span>Мои видео</span>
        </router-link>
      </nav>
    </aside>

    <main class="flex-1 p-10 bg-[#121212]">
      <router-view />
    </main>
    
  </div>
</template>

<style scoped>
/* Включаем flex-row, чтобы они встали строго: меню слева, контент справа */
.profile-container {
  display: flex;
  flex-direction: row;
}

/* Стили кнопок и инпутов для PrimeVue сохраняем ниже */
:deep(.p-button) {
  padding: 12px 24px;
  background-color: #B00D15;
  border:#B00D15;
  color: white;
  border-radius: 5px;
  transition: background-color 0.3s;
  margin: 12px 0;
}
:deep(.p-button:hover) {
  background-color: #9B0B12;
}
:deep(.p-inputtext) {
  background-color: #161b22 !important;
  border-color: #374151 !important;
  color: white !important;
}
:deep(.p-textarea) {
  height: 150px !important;
  resize: none !important;
  background-color: #161b22 !important;
  border-color: #374151 !important;
  color: white !important;
}
</style>