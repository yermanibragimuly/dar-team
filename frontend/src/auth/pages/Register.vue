<script setup lang="ts">
import { ref } from 'vue'
import { registerUser } from '@/services/auth-services'
import { showSuccess, showError } from '@/shared/lib/toastService'

const props = defineProps<{
  visibleRegisterDialog: boolean,
}>()

const emit = defineEmits<{
  (e: 'auth'): void
  (e: 'out'): void
}>()
const disableButton = ref<boolean>(false)

// Поля формы
const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')

// Ошибки валидации
const errors = ref<{ [key: string]: string }>({})

// Валидация
const validate = () => {
  errors.value = {}
  let valid = true

  if (!username.value.trim()) {
    errors.value.username = 'Введите имя пользователя'
    valid = false
  }

  if (!email.value.trim()) {
    errors.value.email = 'Введите email'
    valid = false
  } else if (!/^\S+@\S+\.\S+$/.test(email.value)) {
    errors.value.email = 'Неверный формат email'
    valid = false
  }

  if (!password.value.trim()) {
    errors.value.password = 'Введите пароль'
    valid = false
  }

  if (password.value !== confirmPassword.value) {
    errors.value.confirmPassword = 'Пароли не совпадают'
    valid = false
  }

  return valid
}

// Регистрация
const handleRegister = async () => {
  if (!validate()) return

  try {
    disableButton.value = true
    const response = await registerUser(username.value, password.value, email.value)

    showSuccess(response)
    emit('out')
  } catch (err : any) {
    showError(err?.response?.data.error)
  }finally{
    disableButton.value = false
  }
    username.value = ''
    email.value = ''
    password.value = ''
    confirmPassword.value = ''
}


// Переход на авторизацию
const goLogin = () => {
  emit('auth')
}
</script>

<template>
  <Dialog
    v-model:visible="props.visibleRegisterDialog"
    modal
    :draggable="false"
    :closable="false"
    :dismissableMask="true"
    :style="{
      width: '25rem',
      background: '#18181B',
      borderRadius: '18px',
      boxShadow: '0 20px 60px rgba(0,0,0,.75)',
      '--p-dialog-border-color': '#3f3f46'
    }"
    :pt="{
      root: 
      { 
        class: 'bg-[#18181B] text-white rounded-2xl shadow-2xl overflow-hidden' 
      },
      header: { class: 'bg-[#18181B] text-white border-0 p-4' },
      content: { class: 'bg-[#18181B] text-white pt-0 pb-4 px-4' }
    }"
    class="register-dialog"
  >
    <!-- Заголовок с кнопкой "Назад" -->
    <template #header>
      <div class="flex items-center justify-between w-full">
        <button
          class="flex items-center text-white transition cursor-pointer hover:text-red-500"
          @click="goLogin()"
        >
          <i class="pi pi-arrow-left mr-2"></i>
        </button>
        <h2 class="text-lg font-semibold text-white">{{ $t('auth.registration') }}</h2>
        <span class="w-8"></span>
      </div>
    </template>

    <!-- Контент формы -->
    <div class="flex flex-col items-center justify-center gap-4 text-white mt-3 text-center">
      <InputText v-model="username" :placeholder="$t('auth.username')" class="w-full" />
      <span class="text-red-500 text-sm">{{ errors.username }}</span>

      <InputText v-model="email" placeholder="Email" class="w-full" />
      <span class="text-red-500 text-sm">{{ errors.email }}</span>

      <!-- Новый стиль пароля -->
      <Password
        v-model="password"
        :placeholder="$t('auth.password')"
        toggleMask
        :feedback="true"
        inputClass="w-full"
        :pt="{ icon: { class: 'text-gray-400' } }"
        class="w-full"
      />
      <span class="text-red-500 text-sm">{{ errors.password }}</span>

      <Password
        v-model="confirmPassword"
        :placeholder="$t('auth.confirmThePassword')"
        toggleMask
        :feedback="true"
        inputClass="w-full"
        :pt="{ icon: { class: 'text-gray-400' } }"
        class="w-full"
      />
      <span class="text-red-500 text-sm">{{ errors.confirmPassword }}</span>

      <Button
      unstyled
      :label="$t('auth.register')"
      class="
        w-full
        bg-gradient-to-r from-red-600 to-red-700
        hover:from-red-700 hover:to-red-800
        text-white font-semibold rounded-xl
        py-3 transition duration-300 shadow-lg
      "
      :disabled="disableButton"
      @click="handleRegister"
      />
    </div>
  </Dialog>
</template>


<style scoped>
/* Центрирует диалог на экране */
:deep(.p-dialog) {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Настраиваем сам контейнер */
.register-dialog :deep(.p-dialog-content) {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 1rem;
}

/* Анимация для плавного появления */
:deep(.p-dialog-mask) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.p-inputtext:enabled:focus) {
  border-color: #B00D15 !important;
  box-shadow: 0 0 0 2px rgba(176, 13, 21, 0.3);
  outline: none;
}

:deep(
  .p-inputtext,
  .p-password input
) {
  background-color: #09090B !important;
  color: #E5E7EB !important;
  border: 1px solid #3F3F46 !important;
  border-radius: 0.75rem;
  box-shadow: none !important;
}

</style>
