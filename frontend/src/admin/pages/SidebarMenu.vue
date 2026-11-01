<template>
  <aside 
    class="fixed left-0 top-16 w-72 bg-[#131316] border-r border-[#27272A] z-40 flex flex-col justify-start overflow-y-auto"
    :style="{ height: sidebarHeight }"
  >
    <div class="flex flex-col p-4 space-y-6">
      <!-- Блок КОМАНДА -->
      <div v-if="canAccess(['ROLE_ADMIN'])">
        <span class="text-[11px] font-bold text-[#71717A] uppercase tracking-wider block mb-2 px-2">КОМАНДА</span>
        <nav class="flex flex-col gap-1">
          <router-link
            to="/admin/fighters"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#9CA3AF] hover:bg-[#2a2a2d] hover:text-white font-medium text-sm transition-all"
            active-class="!bg-[#B00D15] !text-white font-bold shadow-[0_0_16px_rgba(176,13,21,0.35)]"
          >
            <i class="pi pi-shield text-[18px]"></i>
            <span>Бойцы</span>
          </router-link>
          <router-link
            to="/admin/coache"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#9CA3AF] hover:bg-[#2a2a2d] hover:text-white font-medium text-sm transition-all"
            active-class="!bg-[#B00D15] !text-white font-bold shadow-[0_0_16px_rgba(176,13,21,0.35)]"
          >
            <i class="pi pi-user text-[18px]"></i>
            <span>Тренеры</span>
          </router-link>
        </nav>
      </div>

      <!-- Блок ЭКИПИРОВКА & МАГАЗИН (ВРЕМЕННО СКРЫТ ДЛЯ ВЕТКИ without-store)
      <div v-if="canAccess(['ROLE_ADMIN', 'ROLE_SHOP_MANAGER'])">
        <span class="text-[11px] font-bold text-[#71717A] uppercase tracking-wider block mb-2 px-2">МАГАЗИН</span>
        <nav class="flex flex-col gap-1">
          <router-link
            to="/admin/shop/orders"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#9CA3AF] hover:bg-[#2a2a2d] hover:text-white font-medium text-sm transition-all"
            active-class="!bg-[#B00D15] !text-white font-bold shadow-[0_0_16px_rgba(176,13,21,0.35)]"
          >
            <i class="pi pi-inbox text-[18px]"></i>
            <span>Заказы мерча</span>
          </router-link>

          <router-link
            to="/admin/shop/branch-stock"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#9CA3AF] hover:bg-[#2a2a2d] hover:text-white font-medium text-sm transition-all"
            active-class="!bg-[#B00D15] !text-white font-bold shadow-[0_0_16px_rgba(176,13,21,0.35)]"
          >
            <i class="pi pi-box text-[18px]"></i>
            <span>Склад филиала</span>
          </router-link>

          <router-link
            v-if="canAccess(['ROLE_ADMIN'])"
            to="/admin/shop"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#9CA3AF] hover:bg-[#2a2a2d] hover:text-white font-medium text-sm transition-all"
            active-class="!bg-[#B00D15] !text-white font-bold shadow-[0_0_16px_rgba(176,13,21,0.35)]"
          >
            <i class="pi pi-shopping-bag text-[18px]"></i>
            <span>Каталог товаров (Бренд)</span>
          </router-link>
        </nav>
      </div>
      -->

      <!-- Блок КОНТЕНТ -->
      <div v-if="canAccess(['ROLE_ADMIN', 'ROLE_MODERATOR'])">
        <span class="text-[11px] font-bold text-[#71717A] uppercase tracking-wider block mb-2 px-2">КОНТЕНТ</span>
        <nav class="flex flex-col gap-1">
          <router-link
            to="/admin/news"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#9CA3AF] hover:bg-[#2a2a2d] hover:text-white font-medium text-sm transition-all"
            active-class="!bg-[#B00D15] !text-white font-bold shadow-[0_0_16px_rgba(176,13,21,0.35)]"
          >
            <i class="pi pi-bell text-[18px]"></i>
            <span>Новости</span>
          </router-link>

          <router-link
            to="/admin/video"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#9CA3AF] hover:bg-[#2a2a2d] hover:text-white font-medium text-sm transition-all"
            active-class="!bg-[#B00D15] !text-white font-bold shadow-[0_0_16px_rgba(176,13,21,0.35)]"
          >
            <i class="pi pi-play text-[18px]"></i>
            <span>Видео</span>
          </router-link>

          <router-link
            to="/admin/partners"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#9CA3AF] hover:bg-[#2a2a2d] hover:text-white font-medium text-sm transition-all"
            active-class="!bg-[#B00D15] !text-white font-bold shadow-[0_0_16px_rgba(176,13,21,0.35)]"
          >
            <svg 
              xmlns="http://www.w3.org/2000/svg" 
              class="w-[18px] h-[18px] shrink-0" 
              viewBox="0 0 24 24" 
              fill="none" 
              stroke="currentColor" 
              stroke-width="2" 
              stroke-linecap="round" 
              stroke-linejoin="round"
            >
              <path d="m11 17 2 2a1 1 0 1 0 3-3"/>
              <path d="m14 14 2.5 2.5a1 1 0 1 0 3-3l-3.88-3.88a3 3 0 0 0-4.24 0l-.88.88a1 1 0 1 1-3-3l2.81-2.81a5.79 5.79 0 0 1 7.06-.87l.47.28a2 2 0 0 0 1.42.25L21 4"/>
              <path d="m21 3 1 11h-2M3 3l-1 11 6.5 6.5a1 1 0 1 0 3-3M3 4h8"/>
            </svg>
            <span>Партнеры</span>
          </router-link>
        </nav>
      </div>

      <!-- Блок УПРАВЛЕНИЕ -->
      <div v-if="canAccess(['ROLE_ADMIN'])">
        <span class="text-[11px] font-bold text-[#71717A] uppercase tracking-wider block mb-2 px-2">УПРАВЛЕНИЕ</span>
        <nav class="flex flex-col gap-1">
          <router-link 
            to="/admin/users" 
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#9CA3AF] hover:bg-[#2a2a2d] hover:text-white font-medium text-sm transition-all"
            active-class="!bg-[#B00D15] !text-white font-bold shadow-[0_0_16px_rgba(176,13,21,0.35)]"
          >
            <i class="pi pi-users text-[18px]"></i>
            <span>Пользователи</span>
          </router-link>
        </nav>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useUserStore } from '@/auth/model/authStore'

const userStore = useUserStore()
const sidebarHeight = ref('calc(100vh - 4rem)')

const updateSidebarHeight = () => {
  const footer = document.querySelector('footer')
  if (!footer) {
    sidebarHeight.value = 'calc(100vh - 4rem)'
    return
  }

  const footerRect = footer.getBoundingClientRect()
  const windowHeight = window.innerHeight

  if (footerRect.top < windowHeight) {
    const visibleFooterHeight = windowHeight - footerRect.top
    sidebarHeight.value = `calc(100vh - 4rem - ${visibleFooterHeight}px)`
  } else {
    sidebarHeight.value = 'calc(100vh - 4rem)'
  }
}

onMounted(() => {
  window.addEventListener('scroll', updateSidebarHeight)
  window.addEventListener('resize', updateSidebarHeight)
  updateSidebarHeight()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', updateSidebarHeight)
  window.removeEventListener('resize', updateSidebarHeight)
})

const canAccess = (allowedRoles: string[]) => {
  if (!userStore.roles) return true
  const userRoles = (Array.isArray(userStore.roles) 
    ? userStore.roles 
    : [userStore.roles]) as string[]
  return allowedRoles.some(r => userRoles.includes(r))
}
</script>