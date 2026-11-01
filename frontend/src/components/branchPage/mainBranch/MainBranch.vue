<template>
  <div class="rounded-3xl p-8 text-zinc-200">
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-10">
      <button
        v-for="c in cities"
        :key="c"
        @click="selectCity(c)"
        :class="[
          'py-4 text-sm font-bold uppercase tracking-wider rounded-xl transition-all cursor-pointer border flex items-center justify-center',
          c === selectedCity
            ? 'bg-[#B00D15] text-white border-[#B00D15] shadow-[0_0_16px_rgba(176,13,21,0.35)]'
            : 'bg-[#131316] text-[#A1A1AA] border-[#27272A] hover:bg-[#1f1f22] hover:text-white'
        ]"
      >
        {{ $t(`cities.${c}`).toUpperCase() }}
      </button>
    </div>

    <div class="grid grid-cols-12 gap-8">
      <div class="col-span-12 md:col-span-5">
        <div class="rounded-xl overflow-hidden border border-zinc-700 h-[280px] md:h-[320px]">
          <iframe
            :src="mapUrl"
            width="100%" height="100%" frameborder="0"
            allowfullscreen
            referrerpolicy="no-referrer-when-downgrade"
          ></iframe>
        </div>

        <div v-if="cityAddresses.length" class="mt-4 space-y-2">
          <div class="text-xs text-zinc-500 uppercase tracking-wider">{{ $t('lable.addressesInAlmaty') }} {{ selectedCity }}:</div>
          <div class="flex flex-col gap-2">
            <button
              v-for="(addr, i) in cityAddresses"
              :key="i"
              @click="selectAddress(i)"
              class="text-left px-4 py-3 rounded-xl border transition-all cursor-pointer"
              :class="i === selectedIndex
                ? 'border-[#B00D15] bg-[#B00D15]/10 text-white'
                : 'border-[#27272A] bg-[#131316] hover:bg-[#1f1f22] text-zinc-300'"
            >
              <div class="font-medium text-sm">{{ addr.address }}</div>
              <div class="text-xs text-zinc-500 mt-1" v-if="addr.phones?.length">
                {{ addr.phones.join(' · ') }}
              </div>
            </button>
          </div>
        </div>
      </div>

      <div class="col-span-12 md:col-span-7 flex flex-col space-y-6">
        <div class="h-[505px] w-full max-w-[840px] flex flex-col overflow-hidden rounded-2xl border border-[#27272A] bg-[#131316] shadow-xl">
          <div class="flex-1 relative min-h-0"> 
            <template v-if="current.image && current.image.length > 0 && current.image[0] !== ''">
              <Galleria 
                :key="`${selectedCity}-${selectedIndex}`"
                :value="current.image" 
                :numVisible="current.image[selectedIndex] ? current.image[selectedIndex].length : 5" 
                :circular="true"
                :autoPlay="true"
                :showItemNavigators="false"
                :showThumbnailNavigators="false"
                :transitionInterval="3000"
                class="h-full custom-static-galleria"
              >
                <template #item="slotProps">
                  <img 
                    :src="slotProps.item" 
                    class="w-full h-[270px] object-cover block" 
                    alt="Interior" 
                  />
                </template>
                <template #thumbnail="slotProps">
                  <img 
                    :src="slotProps.item" 
                    class="w-full h-[50px] object-cover block" 
                  />
                </template>
              </Galleria>
            </template>

            <div v-else class="flex flex-col items-center justify-center h-full bg-[#0e0e11] text-zinc-500">
              <i class="pi pi-images text-5xl mb-2"></i>
              <p class="text-xs uppercase tracking-widest">Фото отсутствуют</p>
            </div>
          </div>

          <div class="bg-[#131316] border-t border-[#27272A] p-5 grid grid-cols-1 md:grid-cols-2 gap-4">
            
            <div class="flex gap-x-3 items-start">
              <div class="w-8 h-8 shrink-0 rounded-full bg-[#B00D15]/10 flex items-center justify-center">
                <i class="pi pi-map-marker text-[#B00D15] text-sm"></i>
              </div>
              <div class="min-w-0">
                <div class="text-[#B00D15] text-[10px] font-bold uppercase tracking-widest mb-0.5">{{ $t('lable.addressesPoint') }}</div>
                <div class="text-zinc-200 text-xs leading-tight truncate" :title="current.address">{{ current.address }}</div>
              </div>
            </div>

            <div class="flex gap-x-3 items-start">
              <div class="w-8 h-8 shrink-0 rounded-full bg-[#B00D15]/10 flex items-center justify-center">
                <i class="pi pi-phone text-[#B00D15] text-sm"></i>
              </div>
              <div>
                <div class="text-[#B00D15] text-[10px] font-bold uppercase tracking-widest mb-0.5">{{ $t('lable.telephone') }}</div>
                <div class="flex flex-col text-xs text-zinc-200">
                  <a v-for="p in current.phones" :key="p" :href="'tel:'+p" class="hover:text-[#B00D15] transition-colors">
                    {{ p }}
                  </a>
                </div>
              </div>
            </div>

            <div class="flex gap-x-3 items-start md:col-span-2 border-t border-[#27272A] pt-3 mt-1">
              <div class="w-8 h-8 shrink-0 rounded-full bg-[#B00D15]/10 flex items-center justify-center">
                <i class="pi pi-envelope text-[#B00D15] text-sm"></i>
              </div>
              <div>
                <div class="text-[#B00D15] text-[10px] font-bold uppercase tracking-widest mb-0.5">Email</div>
                <a :href="'mailto:'+current.email" class="text-zinc-200 text-xs hover:underline truncate block">
                  {{ current.email }}
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

type Address = { address: string; phones?: string[], lat?: number; lon?: number; email?: string; image?: string[]}
type Directory = Record<string, Address[]>

const cities = [
  'Almaty','Astana','Konaev','Atyrau','Aktau','Kyzylorda','Aktobe','Kokshetau','AlmatyRegion','TurkestanRegion',
]

const directory = computed<Directory>(()=> (
  {
 'Almaty': [
 {
  address: t('cities.AlmatyStr'), 
  phones: ['+7-747-243-4438'],
  email: 'darteam@dar.io',
  image: [new URL('@/assets/img/AlmatyStr/img1.webp', import.meta.url).href,new URL('@/assets/img/AlmatyStr/img2.webp', import.meta.url).href,
    new URL('@/assets/img/AlmatyStr/img3.webp', import.meta.url).href,new URL('@/assets/img/AlmatyStr/img4.webp', import.meta.url).href,
    new URL('@/assets/img/AlmatyStr/img5.webp', import.meta.url).href,new URL('@/assets/img/AlmatyStr/img6.webp', import.meta.url).href,
    new URL('@/assets/img/AlmatyStr/img7.webp', import.meta.url).href,new URL('@/assets/img/AlmatyStr/img8.webp', import.meta.url).href,
    new URL('@/assets/img/AlmatyStr/img9.webp', import.meta.url).href,new URL('@/assets/img/AlmatyStr/img10.webp', import.meta.url).href,
    new URL('@/assets/img/AlmatyStr/img11.webp', import.meta.url).href,
  ],
  lat: 43.211185, 
  lon: 76.890700
 },
 { 
  address: t('cities.AlmatyStr2'), 
  phones: ['+7‒708‒205‒06‒16', '+7‒776‒333‒54‒54'],
  email: 'darteam@dar.io',
  image: [new URL('@/assets/img/AlmatyStr2/img1.webp', import.meta.url).href,new URL('@/assets/img/AlmatyStr2/img2.webp', import.meta.url).href,
    new URL('@/assets/img/AlmatyStr2/img3.webp', import.meta.url).href,new URL('@/assets/img/AlmatyStr2/img4.webp', import.meta.url).href,
    new URL('@/assets/img/AlmatyStr2/img5.webp', import.meta.url).href,new URL('@/assets/img/AlmatyStr2/img6.webp', import.meta.url).href,
    new URL('@/assets/img/AlmatyStr2/img7.webp', import.meta.url).href,
  ],
  lat: 43.206670,
  lon: 76.857041
 }
 ],
 'Aktau': [
  { 
    address: t('cities.AktauStr'), 
    phones: ['+7‒702‒000‒53‒52'],
    email: 'darteam@dar.io',
    image: [new URL('@/assets/img/Актау/img1.webp', import.meta.url).href,new URL('@/assets/img/Актау/img2.webp', import.meta.url).href,new URL('@/assets/img/Актау/img3.webp', import.meta.url).href,],
    lat: 43.678784,
    lon: 51.155507
  }
],
 'Aktobe': [
  {
    address: t('cities.AktobeStr'), 
    phones: ['+7-777-560-09-61','+7-705-242-56-91'],
    email: 'darteam@dar.io',
    image: [new URL('@/assets/img/АктобеНурСити/img1.webp', import.meta.url).href,new URL('@/assets/img/АктобеНурСити/img2.webp', import.meta.url).href,new URL('@/assets/img/АктобеНурСити/img3.webp', import.meta.url).href,],
    lat: 50.318663,
    lon: 57.339019
  },
  {
    address: t('cities.AktobeStr2'), 
    phones: ['+7-701-558-49-00'],
    email: 'darteam@dar.io',
    image: [new URL('@/assets/img/АктобеЕсетБатыра/img1.webp', import.meta.url).href,new URL('@/assets/img/АктобеЕсетБатыра/img2.webp', import.meta.url).href,new URL('@/assets/img/АктобеЕсетБатыра/img3.webp', import.meta.url).href,],
    lat: 50.300477,
    lon: 57.164846
  },
  {
    address: t('cities.AktobeStr3'),
    phones: ['+7-702-889-99-60'],
    email: 'darteam@dar.io',
    image: [new URL('@/assets/img/АктобеМаресьева/img1.webp', import.meta.url).href,new URL('@/assets/img/АктобеМаресьева/img2.webp', import.meta.url).href,new URL('@/assets/img/АктобеМаресьева/img3.webp', import.meta.url).href,new URL('@/assets/img/АктобеМаресьева/img4.webp', import.meta.url).href,
      new URL('@/assets/img/АктобеМаресьева/img5.webp', import.meta.url).href,new URL('@/assets/img/АктобеМаресьева/img6.webp', import.meta.url).href,new URL('@/assets/img/АктобеМаресьева/img7.webp', import.meta.url).href,new URL('@/assets/img/АктобеМаресьева/img8.webp', import.meta.url).href,
    ],
    lat: 50.305162,
    lon: 57.144386
  }
],
'AlmatyRegion':[
  { 
    address: t('cities.AlmatyRegStr'), 
    phones: ['+7-747-106-54-77'],
    email: 'darteam@dar.io',
    image: ['',],
    lat: 43.306347,
    lon: 77.149421
  }
],
'TurkestanRegion':[
  { 
    address: t('cities.TurkestanStr'), 
    phones: ['+7-705-270-13-68'],
    email: 'darteam@dar.io',
    image: [new URL('@/assets/img/TurkestanRegion/img1.webp', import.meta.url).href,new URL('@/assets/img/TurkestanRegion/img2.webp', import.meta.url).href,new URL('@/assets/img/TurkestanRegion/img3.webp', import.meta.url).href,
      new URL('@/assets/img/TurkestanRegion/img4.webp', import.meta.url).href,new URL('@/assets/img/TurkestanRegion/img5.webp', import.meta.url).href,new URL('@/assets/img/TurkestanRegion/img6.webp', import.meta.url).href,
      new URL('@/assets/img/TurkestanRegion/img7.webp', import.meta.url).href,new URL('@/assets/img/TurkestanRegion/img8.webp', import.meta.url).href,new URL('@/assets/img/TurkestanRegion/img9.webp', import.meta.url).href,
      new URL('@/assets/img/TurkestanRegion/img10.webp', import.meta.url).href,new URL('@/assets/img/TurkestanRegion/img11.webp', import.meta.url).href,new URL('@/assets/img/TurkestanRegion/img12.webp', import.meta.url).href,
    ],
    lat: 42.493096,
    lon: 70.310401
  }
],
'Kokshetau':[
  { 
    address: t('cities.KokshetauStr'), 
    phones: ['+7-702-828-62-94'],
    email: 'darteam@dar.io',
    image: [new URL('@/assets/img/КокшетауАуельбекова/img1.webp', import.meta.url).href,new URL('@/assets/img/КокшетауАуельбекова/img2.webp', import.meta.url).href,new URL('@/assets/img/КокшетауАуельбекова/img3.webp', import.meta.url).href,new URL('@/assets/img/КокшетауАуельбекова/img4.webp', import.meta.url).href,],
    lat: 53.282040,
    lon: 69.365425
  },
  { 
    address: t('cities.KokshetauStr2'), 
    phones: ['+7-701-536-65-55'],
    email: 'darteam@dar.io',
    image: [new URL('@/assets/img/КокшетауБайтурсынова/img1.webp', import.meta.url).href,new URL('@/assets/img/КокшетауБайтурсынова/img2.webp', import.meta.url).href,new URL('@/assets/img/КокшетауБайтурсынова/img3.webp', import.meta.url).href,],
    lat: 53.292209,
    lon: 69.390972
  }
],
'Kyzylorda':[
  { 
    address: t('cities.KyzylordaStr'), 
    phones: ['+7-701-112-55-51'],
    email: 'darteam@dar.io',
    image: [new URL('@/assets/img/Кызылорда/img1.webp', import.meta.url).href,new URL('@/assets/img/Кызылорда/img2.webp', import.meta.url).href,],
    lat: 44.827906,
    lon: 65.501242
  }
],
'Atyrau':[
  { 
    address: t('cities.AtyrauStr'), 
    phones: ['+7-778-777-34-12'],
    email: 'darteam@dar.io',
    image: [
      new URL('@/assets/img/Атырау/img1.webp', import.meta.url).href,new URL('@/assets/img/Атырау/img2.webp', import.meta.url).href,
    ],
    lat: 47.124659,
    lon: 51.941792
  }
],
'Konaev':[
  { 
    address: t('cities.KonaevStr'), 
    phones: ['+7-707-173-39-83','+7-776-415-20-01','+7-771-402-45-47'],
    email: 'darteam@dar.io',
    image: [
      new URL('@/assets/img/Қонаев/img1.webp', import.meta.url).href,new URL('@/assets/img/Қонаев/img2.webp', import.meta.url).href,new URL('@/assets/img/Қонаев/img3.webp', import.meta.url).href,new URL('@/assets/img/Қонаев/img4.webp', import.meta.url).href,
      new URL('@/assets/img/Қонаев/img5.webp', import.meta.url).href,new URL('@/assets/img/Қонаев/img6.webp', import.meta.url).href,new URL('@/assets/img/Қонаев/img7.webp', import.meta.url).href,new URL('@/assets/img/Қонаев/img8.webp', import.meta.url).href,
      new URL('@/assets/img/Қонаев/img9.webp', import.meta.url).href,new URL('@/assets/img/Қонаев/img10.webp', import.meta.url).href,new URL('@/assets/img/Қонаев/img11.webp', import.meta.url).href,new URL('@/assets/img/Қонаев/img12.webp', import.meta.url).href,
      new URL('@/assets/img/Қонаев/img13.webp', import.meta.url).href,new URL('@/assets/img/Қонаев/img14.webp', import.meta.url).href,new URL('@/assets/img/Қонаев/img15.webp', import.meta.url).href,new URL('@/assets/img/Қонаев/img16.webp', import.meta.url).href,
    ],
    lat: 43.849462,
    lon: 77.052731
  }
],
'Astana':[
  { 
    address: t('cities.AstanaStr'), 
    phones: ['+7-702-505-03-52',' +7-701-393-42-70'],
    email: 'darteam@dar.io',
    image: [
      new URL('@/assets/img/Astana/Astanaimg1.webp',import.meta.url).href,new URL('@/assets/img/Astana/Astanaimg2.webp',import.meta.url).href,new URL('@/assets/img/Astana/Astanaimg3.webp',import.meta.url).href,new URL('@/assets/img/Astana/Astanaimg4.webp',import.meta.url).href,
      new URL('@/assets/img/Astana/Astanaimg5.webp',import.meta.url).href,new URL('@/assets/img/Astana/Astanaimg6.webp',import.meta.url).href,new URL('@/assets/img/Astana/Astanaimg7.webp',import.meta.url).href,new URL('@/assets/img/Astana/Astanaimg8.webp',import.meta.url).href,
      new URL('@/assets/img/Astana/Astanaimg9.webp',import.meta.url).href,new URL('@/assets/img/Astana/Astanaimg10.webp',import.meta.url).href,
    ],
    lat: 51.188204,
    lon: 71.414430
  }
],
}
)) 

const selectedCity = ref('Almaty')
const selectedIndex = ref(0)

const cityAddresses = computed(() => directory.value[selectedCity.value] ?? [])
const current = computed(() => cityAddresses.value[selectedIndex.value] ?? { address: '' })

function selectCity(c: string) {
 selectedCity.value = c
 selectedIndex.value = 0
}

function selectAddress(i: number) {
 selectedIndex.value = i
}

const mapUrl = computed(() => {
    const currentAddress = current.value;

    if (currentAddress.lat && currentAddress.lon) {
        const { lat, lon } = currentAddress;
        return `https://yandex.ru/map-widget/v1/?ll=${lon},${lat}&z=16&pt=${lon},${lat},pm2rdl`;
    }

    const addr = currentAddress?.address ? `${selectedCity.value}, ${currentAddress.address}` : selectedCity.value
    return `https://yandex.ru/map-widget/v1/?text=${encodeURIComponent(addr)}&z=16`
})
</script>