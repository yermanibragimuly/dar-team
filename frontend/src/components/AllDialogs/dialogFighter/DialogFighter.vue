<template>
  <Dialog
    v-model:visible="modelValueLocal"
    @update:visible="handleClose"
    modal
    :dismissableMask="true"
    :closeOnEscape="true"
    :breakpoints="{ '1280px':'70vw', '960px':'90vw', '640px':'95vw' }"
    :style="{ width: '920px', maxWidth: '95vw', '--p-dialog-border-color': '#3f3f46'}"
    :pt="{
      root:{ class:'bg-black text-white rounded-2xl overflow-hidden' },
      header:{ class:'bg-black text-white border-0 pb-0' },
      content:{ class:'bg-black text-white' },
      footer:{ class:'bg-black text-white border-0 pt-0' }
    }"
    aria-label="Карточка бойца"
  >
    <!-- Заголовок -->
    <template #header>
      <div class="flex flex-col gap-1">
        <h2 class="text-xl font-semibold">{{ props.userData?.name || 'Имя не указано' }}</h2>
        <span class="text-sm text-zinc-400">{{ props.userData?.nickname }}</span>
        <span class="text-xs text-zinc-500">{{ props.userData?.country }}</span>
      </div>
    </template>

    <div class="grid grid-cols-12 gap-6">
      <!-- Левая часть -->
      <div class="col-span-12 lg:col-span-7">
        <div class="text-sm tracking-wider text-zinc-300 mb-3">{{ $t('lable.victoryStatistics') }}</div>

        <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <div v-for="(label, key) in nameProcentInt" :key="key" class="bg-zinc-900 rounded-2xl p-4 flex flex-col items-center">
            <CircleProgress
              :percent="Number(numericData?.[key]) || 0"
              :size="100"
              :border-width="13"
              :is-percent="true"
              :animation="true"
              :animation-duration="1200"
              class="glow-circle"
            />
            <div class="mt-3 flex flex-col items-center">
              <span class="text-lg font-semibold text-white">{{ numericData?.[key] || 0 }}%</span>
              <span class="text-xs text-zinc-500 uppercase tracking-wider">{{ $t(label) }}</span>
            </div>
          </div>
        </div>

        <TabView class="mt-6" :pt="{ nav:{ class:'bg-transparent border-b border-zinc-800' } }">
          <TabPanel :header="$t('lable.biography')" value="bio">
            <div class="space-y-2 pt-3">
              <p class="text-sm leading-relaxed text-zinc-300">
                {{ $t('lable.born') }}: <strong>{{ props.userData?.birthplace }}</strong>
              </p>
              <p class="text-sm leading-relaxed text-zinc-300">
                {{ $t('lable.weightCategory') }}: <strong>{{ props.userData?.weightClass }}</strong>
              </p>
              <p class="text-sm leading-relaxed text-zinc-300">
                {{ $t('lable.record') }}: <strong>{{ props.userData?.record }}</strong>
              </p>
              <p class="text-sm leading-relaxed text-zinc-300">
                {{ $t('lable.TypeSport') }}: <strong>{{ props.userData?.sport }}</strong>
              </p>
              <p class="text-sm leading-relaxed text-zinc-300">
                {{ $t('lable.instagram') }}: <strong>{{ props.userData?.instagram }}</strong>
              </p>
            </div>
          </TabPanel>

          <TabPanel :header="$t('lable.achievements')" value="achievements">
            <ul class="list-disc ml-5 space-y-1 pt-3">
              <li v-for="(a, i) in props.userData?.achievements || []" :key="i" class="text-sm text-zinc-300">
                {{ a.title }}
              </li>
              <li v-if="!props.userData?.achievements?.length" class="text-zinc-500 text-sm">
                Достижений пока нет.
              </li>
            </ul>
          </TabPanel>

          <TabPanel :header="$t('lable.video')" value="achievements">
            <ul class="list-disc ml-5 space-y-1 pt-3">
              <li v-for="(a, i) in props.userData?.achievements || []" :key="i" class="text-sm text-zinc-300">
                {{ a.title }}
              </li>
              <li v-if="!props.userData?.achievements?.length" class="text-zinc-500 text-sm">
                Достижений пока нет.
              </li>
            </ul>
          </TabPanel>
        </TabView>
      </div>

      <!-- Фото бойца -->
      <div class="col-span-12 lg:col-span-5">
        <div class="bg-zinc-900 rounded-2xl overflow-hidden h-[490px] flex items-end justify-center">
          <img
            v-if="props.userData?.img"
            :src="props.userData.img"
            :alt="props.userData.name"
            class="object-contain max-h-full w-auto rounded-xl"
          />
          <Skeleton v-else width="80%" height="70%" class="bg-zinc-800" />
        </div>
      </div>
    </div>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import Dialog from 'primevue/dialog'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Skeleton from 'primevue/skeleton'
// @ts-ignore
import CircleProgress from 'vue3-circle-progress'

const props = defineProps<{
  modelValue: boolean
  /** Проценты для 4 карточек, по умолчанию все 72 */
  stats?: number[]
  userData?: any
}>()

const emit = defineEmits<{ 
  (e:'update:modelValue', v:boolean):void,
}>()

// ------- start -----------

const nameProcentInt = {
  other: 'lable.other',
  solution: 'lable.decision', 
  submissive: 'lable.submissive',
  tko: 'lable.tkd'
}

// Фильтруем userData, оставляя только числовые поля из nameProcentInt
const numericData = computed(() => {
  if (!props.userData) return {}
  
  const result: any = {}
  Object.keys(nameProcentInt).forEach(key => {
    if (props.userData[key] !== undefined && props.userData[key] !== null) {
      result[key] = props.userData[key]
    }
  })
  return result
})

// ------- end -------------

const modelValueLocal = ref(props.modelValue)

watch(() => props.modelValue, v => (modelValueLocal.value = v))
watch(modelValueLocal, v => emit('update:modelValue', v))

const handleClose = (v: boolean) => {
  emit('update:modelValue', v)
}
</script>

<style scoped>
:deep(svg circle:not(.circle-progress__background)) {
  stroke: #B00D15 !important;
}

:deep(svg .circle-progress__background),
:deep(svg circle:first-child) {
  stroke: #3f3f46 !important;
}

:deep(.p-tabview-ink-bar) {
  background: #B00D15 !important;
}

/* Контейнер вкладок (верхняя полоса) */
:deep(.p-tabview-tablist-container) {
  background-color: #3F3F46 !important;
  border-bottom: 1px solid #52525B;
}

/* Список вкладок */
:deep(.p-tabview-tablist) {
  background-color: #3F3F46 !important;
}

/* Кнопка вкладки */
:deep(.p-tabview-tablist .p-tabview-tab) {
  background-color: transparent !important;
  color: #D4D4D8;
  border: none;
}

/* Активная вкладка */
:deep(.p-tabview-tablist .p-tabview-tab.p-highlight) {
  color: #FFFFFF;
}

/* Контент вкладок */
:deep(.p-tabview-panels) {
  background-color: #3F3F46 !important;
  padding: 16px;
  border: 1px solid #52525B;
}

/* Убираем стандартные бордеры PrimeVue */
:deep(.p-tabview) {
  border: none;
}

/* текст вкладок */
:deep(.p-tabview-tab-title) {
  color: #d4d4d8;
  font-weight: 500;
  letter-spacing: 0.05em;
}
</style>
