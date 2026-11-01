<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/auth/model/authStore'
import Auth from '@/auth/pages/auth.vue'
import Register from '@/auth/pages/Register.vue'
import LanguageSwitcher from '@/features/language-switcher/LanguageSwitcher.vue'
import ForgotPassword from '@/auth/pages/ForgotPassword.vue'
import ResetPassword from '@/auth/pages/ResetPassword.vue'
import 'primeicons/primeicons.css'

const userStore = useUserStore()
const router = useRouter()
const scrolled = ref(false)
const mobileMenu = ref(false)
const visableAuthDialog = ref<boolean>(false)
const RegisterDialog = ref<boolean>(false)
const username = computed(() => userStore.username)
const dropdownVisible = ref(false)
const isAdmin = computed(() => userStore.roles[0])
const visableForgotDialog = ref<boolean>(false)
const visableResetDialog = ref<boolean>(false)

const handleScroll = () => {
  scrolled.value = window.scrollY > 10
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})
onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})

const menuItems = [
  { to: '/', label: 'menu.main', title: 'ГЛАВНАЯ' },
  { to: '/teams', label: 'menu.team', title: 'КОМАНДА' },
  { to: '/news', label: 'menu.news', title: 'НОВОСТИ' },
  { to: '/branch', label: 'menu.branches', title: 'ФИЛИАЛЫ' },
  { to: '/outside', label: 'menu.outsideTheOctagon', title: 'ВНЕ ОКТАГОНА' },
  // { to: '/shop', label: 'menu.shop', title: 'МАГАЗИН' }, // Магазин скрыт для ветки without-store
  { to: '/about', label: 'menu.aboutUs', title: 'О НАС' }
]

const toggleMenu = () => (mobileMenu.value = !mobileMenu.value)

const handleAuthClick = (e : any) => {
  if (e) {
    router.push('/login')
    visableAuthDialog.value = true
    RegisterDialog.value = false
  } else {
    visableAuthDialog.value = false
    router.push('/')
  }
}

const handleAuthClick12 = () => {
  dropdownVisible.value = !dropdownVisible.value
}

const logoutElse = (key: string) => {
  switch(key){
    case 'out':
      userStore.logout()
      router.push('/')
      break;
    case 'ROLE_ADMIN':
      router.push('/admin')
      break;
    case 'ROLE_MODERATOR':
      router.push('/admin/news')
      break;
    case 'ROLE_USER':
      router.push(`/profile`)
      break;
  }
  dropdownVisible.value = false
}

const openDialog = (action: string) => {
  visableAuthDialog.value = false
  RegisterDialog.value = false
  visableForgotDialog.value = false
  visableResetDialog.value = false

  switch (action) {
    case 'auth':
      visableAuthDialog.value = true
      router.push('/login')
      break
    case 'register':
      RegisterDialog.value = true
      router.push('/register')
      break
    case 'forgot':
      visableForgotDialog.value = true
      router.push('/forgot-password')
      break
    case 'reset':
      visableResetDialog.value = true
      router.push('/reset-password')
      break
    case 'out':
      router.push('/')
      break
  }
}
</script>

<template>
  <Auth 
    :visableAuthDialog="visableAuthDialog" 
    @update:visableAuthDialog="handleAuthClick" 
    @register="openDialog('register')"
    @forgot="openDialog('forgot')"
  />
  <Register 
    :visibleRegisterDialog="RegisterDialog" 
    @auth="openDialog('auth')"
    @out="openDialog('out')"
  />
  <ForgotPassword 
    :visableForgotDialog="visableForgotDialog" 
    @out="openDialog('out')"
    @reset="openDialog('reset')"
  />
  <ResetPassword 
    :visableResetDialog="visableResetDialog" 
    @out="openDialog('out')"
  />
  <header
    :class="[
      'fixed top-0 left-0 right-0 z-50 transition-all duration-300',
      scrolled
        ? 'bg-[#0d0f12]/95 shadow-lg py-3'
        : 'bg-[#0d0f12]/80 backdrop-blur-md py-4'
    ]"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex justify-between items-center">
      <router-link to="/" class="text-2xl font-bold text-white tracking-wider">
        <img 
          src="../assets/img/logoMain.png"
          alt="logo" 
          class="w-20 h-auto"
        />
      </router-link>

      <nav class="hidden md:flex space-x-8">
        <router-link
          v-for="item in menuItems"
          :key="item.to"
          :to="item.to"
          class="text-white hover:text-gray-300 transition-colors font-medium pb-1 border-b-2 border-transparent hover:border-red-600 uppercase"
          active-class="border-red-600 text-red-500"
        >
          {{ $te(item.label) ? $t(item.label) : item.title }}
        </router-link>
      </nav>

      <div class="flex items-center gap-4">
        <!-- Языковой переключатель -->
        <LanguageSwitcher />

        <div class="relative inline-block">
          <div
            v-if="username"
            class="flex items-center gap-2 cursor-pointer hover:text-red-500 transition text-white"
            @click="handleAuthClick12"
          >
            <i class="pi pi-user text-2xl"></i>
            <span class="font-medium">{{ username }}</span>
          </div>

          <div
            v-else
            class="cursor-pointer hover:text-red-500 transition text-white"
            @click="handleAuthClick"
          >
            <i class="pi pi-user text-2xl"></i>
          </div>

          <div
            v-if="dropdownVisible"
            class="absolute left-1/2 -translate-x-1/2 mt-2 w-32 bg-[#1f1f1f] border border-zinc-700 bg-gray-900/95 rounded-lg shadow-lg z-50 overflow-hidden"
          >
            <ul class="flex flex-col text-white text-xs">
              <li
                v-if="['ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_USER'].includes(isAdmin)"
                class="px-4 py-2 hover:bg-[#B00D15] cursor-pointer text-center"
                @click="logoutElse(isAdmin)"
              >
                {{ isAdmin === 'ROLE_USER' ? 'Профиль' : 'Админ панель' }}
              </li>
              <li
                class="px-4 py-2 hover:bg-[#B00D15] cursor-pointer text-center border-t border-zinc-800"
                @click="logoutElse('out')"
              >
                Выйти
              </li>
            </ul>
          </div>
        </div>

        <button class="md:hidden text-white" @click="toggleMenu">
          <svg
            v-if="!mobileMenu"
            xmlns="http://www.w3.org/2000/svg"
            class="h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
          <svg
            v-else
            xmlns="http://www.w3.org/2000/svg"
            class="h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>

    <transition name="slide">
      <div
        v-if="mobileMenu"
        class="md:hidden absolute top-full left-0 w-full bg-[#0d0f12]/95 border-t border-zinc-700"
      >
        <ul class="flex flex-col items-center py-4 space-y-4">
          <li v-for="item in menuItems" :key="item.to">
            <router-link
              :to="item.to"
              class="text-white hover:text-red-400 text-lg transition"
              active-class="text-red-500"
              @click="toggleMenu"
            >
              {{ $te(item.label) ? $t(item.label) : item.title }}
            </router-link>
          </li>
        </ul>
      </div>
    </transition>
  </header>

  <div class="pt-16"></div>
</template>

<style>
.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s ease;
}
.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>