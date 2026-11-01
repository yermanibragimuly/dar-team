<template>
  <Dialog
    v-model:visible="modelValueLocal" 
    modal 
    maximizable
    @update:visible="handleClose"
    :pt="{ 
        root: { 
            class: 'bg-black text-white !rounded-none border-0', 
            style: 'max-height: 95vh; display: flex; flex-direction: column;' 
        }, 
        header: { class: 'bg-black text-white border-0 p-0 flex-shrink-0' }, 
        content: { class: 'bg-black text-white p-0 overflow-hidden' } 
    }"
    :style="{ width: '95vw', maxWidth: '1200px' }"
  >
    <template #header>
      <div class="w-full">
        <div class="text-xl sm:text-3xl font-extrabold uppercase p-4">          
          <p class="m-0 leading-tight" v-html="renderAppleEmoji(currentTitle)"></p>
        </div>
        <div class="h-[2px] w-full bg-red-600"></div>
      </div>
    </template>

    <div class="flex flex-col items-center p-4 sm:p-6 h-full max-h-[calc(95vh-80px)]">
      <div class="w-full md:w-[85%] lg:w-[75%] flex flex-col gap-4 overflow-hidden">
        
        <div v-if="props.userData?.img" 
            class="flex-shrink flex justify-center items-center min-h-[150px]">
          <img 
            :src="props.userData?.img"
            alt="Новости"
            class="max-h-[40vh] md:max-h-[50vh] w-auto object-contain rounded-lg shadow-2xl"
          />
        </div>

        <div v-else class="text-white text-4xl flex-shrink-0 text-center py-4">
          📸
        </div>

        <div 
          class="bg-zinc-800 w-full flex-1 overflow-y-auto text-white text-base md:text-lg 
                font-medium p-4 md:p-6 rounded-lg leading-relaxed shadow-inner border border-zinc-700"
        >
          <p class="whitespace-pre-wrap" v-html="renderAppleEmoji(currentContent)"></p>
        </div>
      </div>
    </div>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import Dialog from 'primevue/dialog'
import { useI18n } from 'vue-i18n'
import { renderAppleEmoji } from '@/shared/lib/appleEmoji'

const { locale } = useI18n()
const props = defineProps<{ modelValue: boolean; userData: any }>()
const emit = defineEmits<{ (e:'update:modelValue', v:boolean): void }>()
const modelValueLocal = ref(props.modelValue)
watch(() => props.modelValue, v => (modelValueLocal.value = v))
watch(modelValueLocal, v => emit('update:modelValue', v))

const handleClose = (v: boolean) => {
  emit('update:modelValue', v)
}

const currentTitle = computed(() => {
  if (locale.value === 'en') return props.userData?.titleEn || props.userData?.title;
  if (locale.value === 'kk') return props.userData?.titleKz || props.userData?.title;
  return props.userData?.title;
});

const currentContent = computed(() => {
  if (locale.value === 'en') return props.userData?.contentEn || props.userData?.content;
  if (locale.value === 'kk') return props.userData?.contentKz || props.userData?.content;
  return props.userData?.content;
});
</script>