<script setup lang="ts">
import Body from './views/Body.vue';
import { useToast } from 'primevue/usetoast'
import { setGlobalToast } from '@/shared/lib/toastService'

const toast = useToast()
setGlobalToast(toast)
</script>

<template>
  <Body/>
  <Toast />
</template>