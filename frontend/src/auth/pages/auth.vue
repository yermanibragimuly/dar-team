<script setup lang="ts">
import { ref, watch  } from 'vue'
import { useRouter } from 'vue-router'
import { auth } from '@/services/auth-services'
import { showSuccess, showError } from '@/shared/lib/toastService'
import { useUserStore } from '@/auth/model/authStore'

const username = ref<string>('')
const password = ref<string>('')
const router = useRouter()
const userStore = useUserStore()


const props = defineProps<{
  visableAuthDialog: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visableAuthDialog', v:boolean) : void
  (e: 'register'): void
  (e: 'forgot'): void
}>()

const authUser = async () => {
  try {
    const response = await auth(username.value,password.value)

    userStore.setUser(response)

    showSuccess(response)
    closeDialog(false)
    
    if(userStore.roles[0] === 'ROLE_ADMIN') router.push('/admin')
    if(userStore.roles[0] === 'ROLE_MODERATOR') router.push('/admin/news')
  } catch (error) {
    showError(error)
  }
}

watch(
  () => props.visableAuthDialog,
  (visible) => {
    if (visible){
      username.value = ''
      password.value = ''
    }
  }
)

const closeDialog = (v:boolean) => {
  emit('update:visableAuthDialog', v)
}

const goTo = (v:string) => {
  switch(v){
    case 'forgot':
      emit('forgot')
      break;
    case 'register':
      emit('register')
      break;
  }
}

// const goForgot = () => {
//   emit('update:visableForgotDialog', 'forgotDialog')
// }
</script>

<template>
<Dialog
  v-model:visible="props.visableAuthDialog"
  @update:visible="closeDialog"
  modal
  :dismissableMask="true"
  :closeOnEscape="true"
  :style="{ 
    width: '25rem',
    background: '#18181B',
    borderRadius: '18px',
    boxShadow: '0 20px 60px rgba(0,0,0,.75)',
    '--p-dialog-border-color': '#3f3f46'
  }"
  :pt="{
    root: { class: 'auth-dialog' },
    header: { class: 'border-0 pb-0 bg-transparent' },
    content: { class: 'bg-transparent text-white' },
    footer: { class: 'border-0 pt-0 bg-transparent' }
  }"
>

    <template #header>
      <div class="text-white text-lg font-semibold">
        {{ $t('auth.authorization') }}
      </div>
    </template>

    <!-- Контент -->
    <div class="flex flex-col gap-5">
      <span class="p-input-icon-left w-full">
        <i class="pi pi-user text-gray-400" />
        <InputText
          v-model="username"
          :placeholder="$t('auth.username')"
          class="w-full"
        />
      </span>

      <span class="p-input-icon-left w-full">
        <i class="pi pi-lock text-gray-400" />
        <Password
          v-model="password"
          :placeholder="$t('auth.password')"
          toggleMask
          :feedback="false"
          class="w-full"
          inputClass="w-full"
        />
      </span>

      <Button
      unstyled
      :label="$t('auth.enter')"
      @click="authUser"
      @keydown.enter="authUser"
      class="
        w-full
        bg-gradient-to-r from-red-600 to-red-700
        hover:from-red-700 hover:to-red-800
        text-white font-semibold rounded-xl
        py-3 transition duration-300 shadow-lg
        cursor-pointer
      "
      />
    </div>
    <div class="mt-3 flex items-center justify-between text-sm">
      <button class="text-gray-500 hover:text-white cursor-pointer" @click="goTo('forgot')">Забыли пароль ?</button>
      <button class="text-gray-500 hover:text-white cursor-pointer" @click="goTo('register')">{{ $t('auth.registration') }}</button>
    </div>
  </Dialog>
</template>

<style scoped>
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


:deep(.p-inputtext:enabled:focus) {
  border-color: #B00D15;
  box-shadow: 0 0 0 2px rgba(176, 13, 21, 0.3);
}
</style>

