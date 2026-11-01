<script setup lang="ts">
import { sendOnEmailForgotPasswordUser } from '@/services/auth-services'
import { showSuccess, showError } from '@/shared/lib/toastService'
import { ref } from 'vue'


const email = ref('')
const props = defineProps<{
  visableForgotDialog: boolean
}>()
const disableButton = ref<boolean>(false)

const emit = defineEmits<{
  (e: 'out'): void
  (e: 'reset'): void
}>()


const closeDialog = () => {
  emit('out')
}

const SendEmail = async () => {
  try{
    disableButton.value = true
    let resp = await sendOnEmailForgotPasswordUser(email.value)
    showSuccess(resp.message)
    emit('reset')
  }catch(err){
    showError(err)
  }finally{
    disableButton.value = false
    email.value = ''
  }
}
</script>
<template>
  <!-- <ResetPassword/> -->
  <Dialog
    v-model:visible="props.visableForgotDialog"
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
        {{ $t('lable.enterEmail') }}
      </div>
    </template>

    <!-- Контент -->
    <div class="flex flex-col gap-5">
      <span class="p-input-icon-left w-full">
        <i class="pi pi-user text-gray-400" />
      <InputText v-model="email" placeholder="Email" class="w-full" />
      <span class="text-red-500 text-sm"></span>
      </span>


      <Button
        unstyled
        :label="$t('button.reset')"
        @click="SendEmail"
        :disabled="disableButton"
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
  </Dialog>
</template>