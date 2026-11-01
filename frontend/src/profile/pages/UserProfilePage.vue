<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { showError, showSuccess } from '@/shared/lib/toastService';
import { getDataProfile, sendDataProfile } from '@/services/user/profile-services';

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

const saveProfile = async () => {
  const payload = {
    ...profile.value,
    birthDate: profile.value.birthDate ? 
    formatDateLocal(profile.value.birthDate) : null
  }
  try{
    await sendDataProfile(payload)
    showSuccess('успешно обновлен')
  }catch(err){
  showError(err)
  }
};

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

const formatDateLocal = (date:any) => {
  if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
    return date
  }
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

onMounted( async ()=>{
  await getProfile();
})

</script>

<template>
<div class="head bg-[#121212]">

  <main class="flex-1 p-10">
    <div class="max-w-3xl">
      <h1 class="text-2xl font-bold text-white mb-8">Основная информация</h1>
      <div class="bg-[#18181B] border border-gray-800 rounded-2xl p-8 grid gap-8">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="flex flex-col gap-2">
            <label class="text-gray-400 text-sm">Имя</label>
            <InputText v-model="profile.firstName" required class="w-full bg-[#161b22]" placeholder="Введите имя" />
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-gray-400 text-sm">Фамилия</label>
            <InputText v-model="profile.lastName" required class="w-full bg-[#161b22]" placeholder="Введите фамилию" />
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-gray-400 text-sm">Телефон</label>
            <InputText v-model="profile.phone" required class="w-full bg-[#161b22]" placeholder="+7 (___) ___-__-__" />
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-gray-400 text-sm">Дата рождения</label>
            <Calendar v-model="profile.birthDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
          </div>

          <div class="flex flex-col gap-2 md:col-span-2">
            <label class="text-gray-400 text-sm">О себе</label>
            <Textarea v-model="profile.bio" rows="4" class="w-full" placeholder="Расскажите немного о себе..."/>
          </div>
        </div>
      </div>
      <Button label="Сохранить" icon="pi pi-check" @click="saveProfile" />
    </div>
  </main>
</div>
</template>

<style scoped>
:global(.app-content) {
  display: flex;
  flex-direction: column;
}

.head {
  display: flex;
  flex: 1; /* Теперь он увидит, что родитель стал флексом, и растянется */
  min-height: 0;
}

:deep(.p-button) {
  padding: 12px 24px;
  background-color: #B00D15;
  border:#B00D15;
  color: white;
  border-radius: 5px;
  text-decoration: none;
  transition: background-color 0.3s;
  margin: 12px 0;
}
:deep(.p-button:hover) {
  background-color: #9B0B12;
  border:#B00D15;
}
.p-button-text:not(:disabled):hover {
  background: #ffe5e5;
  border-color: transparent;
  color: #B00D15;
}

.p-inputtext {
  background-color: #161b22 !important;
  border-color: #374151 !important; /* gray-700 */
  color: white !important;
}

.p-inputtext:focus {
  border-color: #9B0B12 !important; /* blue-500 */
  box-shadow: 0 0 0 1px #B00D15 !important;
}

:deep(.p-textarea:focus) {
  border-color: #9B0B12 !important;
  box-shadow: 0 0 0 0.2rem rgba(155, 11, 18, 0.25) !important;
}

:deep(.p-textarea) {
  height: 150px !important; /* Твоя фиксированная высота */
  resize: none !important;  /* Запрещает растягивать за угол */
  background-color: #161b22 !important;
  border-color: #374151 !important; /* gray-700 */
  color: white !important;
}

:deep(.p-inputtext:enabled:focus) {
  border-color: #9B0B12 !important;
  box-shadow: 0 0 0 1px #9B0B12 !important;
}


:deep(.p-datepicker-input) {
    background-color: #161b22 !important;
    border-color: #374151 !important; /* Цвет границы gray-700 */
    color: white !important;
}

:deep(.p-datepicker-dropdown) {
    background-color: #161b22 !important;
    border-color: #374151 !important;
    color: #94a3b8 !important; /* Цвет иконки (серый) */
}

</style>