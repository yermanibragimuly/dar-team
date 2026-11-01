<script setup lang="ts">
import { ref, reactive } from 'vue'
import { showError, showSuccess } from '@/shared/lib/toastService'
import { sendOnEmailResetPasswordUser } from '@/services/auth-services'

interface ResetForm {
  email: string;
  code: string;
  password: string;
  confirmPassword: string;
}

const form = reactive<ResetForm>({
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
})
const disableButton = ref<boolean>(false)

const initialFormState: ResetForm = {
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
}

const props = defineProps<{
  visableResetDialog: boolean,
}>()

const emit = defineEmits<{
  (e: 'out'): void
  (e: 'register'): void
}>()

// Ошибки валидации: используем Record для типизации
const errors = ref<Record<string, string>>({})

// Валидация
const validate = () => {
  errors.value = {}
  let valid = true

  if (!form.code.trim()) {
    errors.value.code = 'Введите код' // Исправлен ключ ошибки
    valid = false
  }

  if (!form.email.trim()) {
    errors.value.email = 'Введите email'
    valid = false
  } else if (!/^\S+@\S+\.\S+$/.test(form.email)) {
    errors.value.email = 'Неверный формат email'
    valid = false
  }

  if (!form.password.trim()) {
    errors.value.password = 'Введите пароль'
    valid = false
  }

  if (form.password !== form.confirmPassword) {
    errors.value.confirmPassword = 'Пароли не совпадают'
    valid = false
  }

  return valid
}

// Регистрация
const SendResetPassword = async () => {
  if (!validate()) return

  try {
    disableButton.value = true
    // Это покажет текущие данные, а не ссылку на прокси
    const resp = await sendOnEmailResetPasswordUser(JSON.parse(JSON.stringify(form)))
    showSuccess(resp.message)
    resetForm()
    emit('out')
  } catch (err) {
    showError(err)
  }finally{
    disableButton.value = false
  }
}

const resetForm = () => {
  Object.assign(form, initialFormState);
  errors.value = {}; // Правильная очистка ref объекта
}

const closeDialog = () => {
  emit('out')
  resetForm();
}
</script>

<template>
  <Dialog
    v-model:visible="props.visableResetDialog"
    modal
    @update:visible="closeDialog"
    :draggable="false"
    :closeOnEscape="true"
    :dismissableMask="true"
    :style="{
      width: '25rem',
      background: '#18181B',
      borderRadius: '18px',
      boxShadow: '0 20px 60px rgba(0,0,0,.75)',
      '--p-dialog-border-color': '#3f3f46'
    }"
    :pt="{
      root: { 
        class: 'bg-[#18181B] text-white rounded-2xl shadow-2xl overflow-hidden' 
      },
      // flex justify-between прижимает кнопку закрытия вправо
      header: { class: 'flex justify-between items-center bg-[#18181B] text-white border-0 p-4' },
      content: { class: 'bg-[#18181B] text-white pt-0 pb-4 px-4' },
      // Стилизация кнопки закрытия
      closeButton: { class: 'text-white hover:text-gray-400' }
    }"
    class="register-dialog"
  >
    <template #header>
      <div class="text-white text-lg font-semibold">
        {{ $t('lable.resetPassword') }}
      </div>
    </template>

    <div class="flex flex-col items-center justify-center gap-4 text-white mt-3 text-center">
      <InputText v-model="form.email" placeholder="Email" class="w-full" />
      <span class="text-red-500 text-sm">{{ errors.email }}</span>
      
      <InputText v-model="form.code" :placeholder="$t('auth.code')" class="w-full" />
      <span class="text-red-500 text-sm">{{ errors.code }}</span>

      <Password
        v-model="form.password"
        :placeholder="$t('auth.password')"
        toggleMask
        :feedback="true"
        inputClass="w-full"
        :pt="{ icon: { class: 'text-gray-400' } }"
        class="w-full"
      />
      <span class="text-red-500 text-sm">{{ errors.password }}</span>

      <Password
        v-model="form.confirmPassword"
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
        :label="$t('button.reset')"
        :disabled="disableButton"
        class="
          w-full
          bg-gradient-to-r from-red-600 to-red-700
          hover:from-red-700 hover:to-red-800
          text-white font-semibold rounded-xl
          py-3 transition duration-300 shadow-lg
        "
        @click="SendResetPassword"
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