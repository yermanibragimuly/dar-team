<template>
  <Dialog
    v-model:visible="modelValueLocal" 
    modal 
    :dismissableMask="true" 
    :closeOnEscape="true"
    @update:visible="handleClose"
    :style="{ 
      width: '1120px', 
      maxWidth: '96vw', 
      'min-height': '655px', 
      display: 'flex', 
      'flex-direction': 'column',
      '--p-dialog-border-color': '#3f3f46'
    }"
    :breakpoints="{ '1400px':'90vw', '1024px':'96vw' }"
    :pt="{
      root:{ class:'bg-black text-white rounded-2xl overflow-hidden' },
      header:{ class:'bg-black text-white border-0 pb-2' },
      content:{ class:'bg-black text-white flex-grow-1' } 
    }"
  >
    <template #header>
      <div class="flex items-end justify-between">
        <div>
          <div class="text-3xl sm:text-4xl font-extrabold uppercase">{{ name }}</div>
          <div class="text-3xl text-1xl mt-1">{{ $t('lable.titlesAwards') }}</div>
        </div>
      </div>
    </template>

    <div class="grid grid-cols-12 gap-8">
      <!-- achievements block -->
      <div class="col-span-12 lg:col-span-7">
        <div class="rounded-2xl bg-zinc-900/60 p-6">
          <ul class="space-y-3">
            <li v-for="(a, i) in props.userData?.merit" :key="i" class="flex gap-3">
              <span class="mt-[7px] w-2.5 h-2.5 rounded-full bg-red-600 shrink-0"></span>
              <span class="text-zinc-100">{{ a.list }}</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- image with decorative frame -->
      <div class="col-span-12 lg:col-span-5">
        <div class="relative">
          <div class="absolute inset-0 rounded-3xl bg-gradient-to-br from-zinc-800 to-zinc-900 opacity-60 blur-xl"></div>
          
          <div class="relative bg-zinc-900 rounded-3xl pt-4 px-4 flex items-end justify-center h-[420px] overflow-hidden">
            <img
              v-if="props.userData?.img"
              :src="props.userData.img" 
              alt="photo" 
              class="object-contain object-bottom max-h-full w-auto block" 
            />
            <Skeleton v-else width="80%" height="90%" class="bg-zinc-800" />
          </div>
        </div>
      </div>
    </div>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import Dialog from 'primevue/dialog'

const props = defineProps<{ modelValue: boolean; name?: string; image?: string; achievements?: string[]; userData: any }>()
const emit = defineEmits<{
  (e:'update:modelValue', v:boolean): void 
  }>()
const modelValueLocal = ref(props.modelValue)
watch(() => props.modelValue, v => (modelValueLocal.value = v))
watch(modelValueLocal, v => emit('update:modelValue', v))

const handleClose = (v: boolean) => {
  emit('update:modelValue', v)
}
</script>