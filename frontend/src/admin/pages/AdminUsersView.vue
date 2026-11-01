<template>
  <div class="p-6 text-white min-h-screen">
    <!-- Заголовок страницы в общем стиле админки -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold tracking-wide">Управление пользователями</h1>
      <p class="text-sm text-[#71717A] mt-1">Реестр пользователей системы, распределение ролей и привязка к филиалам клуба</p>
    </div>

    <!-- Индикаторы загрузки и ошибок -->
    <div v-if="loading" class="p-4 text-zinc-400 bg-[#18181B] rounded-lg border border-[#27272A]">
      <i class="pi pi-spin pi-spinner mr-2"></i> Загрузка списка пользователей...
    </div>

    <div v-else-if="error" class="p-4 text-red-400 bg-red-950/40 rounded-lg border border-red-800">
      <i class="pi pi-exclamation-triangle mr-2"></i> {{ error }}
    </div>

    <!-- Таблица пользователей -->
    <div v-else class="bg-[#18181B] border border-[#27272A] rounded-xl overflow-hidden shadow-lg">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse text-sm">
          <thead>
            <tr class="border-b border-[#27272A] bg-[#121214] text-[#A1A1AA] uppercase text-[11px] font-semibold tracking-wider">
              <th class="py-3.5 px-4">ID</th>
              <th class="py-3.5 px-4">Пользователь</th>
              <th class="py-3.5 px-4">Email</th>
              <th class="py-3.5 px-4">Роль в системе</th>
              <th class="py-3.5 px-4">Привязка к филиалу</th>
              <th class="py-3.5 px-4 text-right">Действия</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-[#27272A]">
            <tr 
              v-for="user in users" 
              :key="user.id" 
              class="hover:bg-[#202024] transition-colors"
            >
              <td class="py-3.5 px-4 text-zinc-400 font-mono text-xs">#{{ user.id }}</td>
              <td class="py-3.5 px-4 font-medium text-white flex items-center gap-2">
                <i class="pi pi-user text-zinc-400 text-xs"></i>
                {{ user.username }}
                <span 
                  v-if="isCurrentUser(user)" 
                  class="ml-1 px-1.5 py-0.5 rounded bg-zinc-800 border border-zinc-700 text-[10px] font-mono text-zinc-400"
                >
                  Вы
                </span>
              </td>
              <td class="py-3.5 px-4 text-zinc-300">{{ user.email || '—' }}</td>

              <!-- Селект роли -->
              <td class="py-3.5 px-4">
                <select 
                  v-model="user.currentRole" 
                  class="bg-[#27272A] text-white border border-[#3F3F46] rounded-md px-3 py-1.5 text-xs outline-none focus:border-[#B00D15] transition-all cursor-pointer"
                >
                  <option value="ROLE_USER">ROLE_USER (Клиент)</option>
                  <option value="ROLE_MODERATOR">ROLE_MODERATOR (Контент-менеджер)</option>
                  <option value="ROLE_SHOP_MANAGER">ROLE_SHOP_MANAGER (Менеджер магазина)</option>
                  <option value="ROLE_ADMIN">ROLE_ADMIN (Суперадмин)</option>
                </select>
              </td>

              <!-- Колонка привязки к филиалу -->
              <td class="py-3.5 px-4">
                <div v-if="user.currentRole === 'ROLE_SHOP_MANAGER'">
                  <select 
                    v-model="user.branchId"
                    class="bg-[#202024] text-white border border-amber-600/50 rounded-md px-2.5 py-1.5 text-xs outline-none focus:border-red-500 transition-all cursor-pointer w-52"
                  >
                    <option :value="null">-- Выберите филиал --</option>
                    <option v-for="b in branches" :key="b.id" :value="b.id">
                      {{ b.name }}
                    </option>
                  </select>
                </div>
                <span v-else-if="user.currentRole === 'ROLE_ADMIN'" class="text-zinc-500 font-mono text-xs italic">
                  Все филиалы клуба
                </span>
                <span v-else class="text-zinc-600 text-xs">—</span>
              </td>

              <!-- Действия -->
              <td class="py-3.5 px-4 text-right">
                <div class="inline-flex items-center justify-end gap-2">
                  <button 
                    class="bg-[#B00D15] hover:bg-[#8e0a11] text-white font-medium px-3.5 py-1.5 rounded-md text-xs transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-[0_0_12px_rgba(176,13,21,0.25)] cursor-pointer"
                    :disabled="user.saving" 
                    @click="saveRole(user)"
                  >
                    <i v-if="user.saving" class="pi pi-spin pi-spinner mr-1"></i>
                    {{ user.saving ? 'Сохранение...' : 'Сохранить' }}
                  </button>

                  <button 
                    class="w-8 h-8 rounded-md bg-[#27272A] text-zinc-400 hover:text-white hover:bg-red-950/80 hover:border-red-600 border border-[#3F3F46] flex items-center justify-center transition-all cursor-pointer group disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:bg-[#27272A] disabled:hover:border-[#3F3F46] disabled:hover:text-zinc-400"
                    :disabled="isCurrentUser(user)"
                    :title="isCurrentUser(user) ? 'Нельзя удалить собственный аккаунт' : 'Удалить пользователя'"
                    @click="openDeleteModal(user)"
                  >
                    <i class="pi pi-trash text-xs group-hover:text-red-400 transition-colors"></i>
                  </button>
                </div>
              </td>
            </tr>

            <tr v-if="users.length === 0">
              <td colspan="6" class="py-8 text-center text-zinc-500">
                Пользователи не найдены
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Модальное окно подтверждения удаления -->
    <Teleport to="body">
      <div 
        v-if="isDeleteModalOpen" 
        class="fixed inset-0 z-[100] bg-black/75 backdrop-blur-sm flex items-center justify-center p-4 transition-all"
        @click.self="closeDeleteModal"
      >
        <div class="bg-[#18181B] border border-[#27272A] w-full max-w-md rounded-2xl shadow-2xl overflow-hidden p-6 text-zinc-100">
          <div class="flex items-start gap-4">
            <div class="w-12 h-12 rounded-xl bg-red-950/50 border border-red-800/60 flex items-center justify-center shrink-0 text-red-500">
              <i class="pi pi-exclamation-triangle text-xl"></i>
            </div>

            <div class="space-y-1.5 flex-1">
              <h3 class="text-base font-bold text-white tracking-wide">
                Удаление пользователя
              </h3>
              <p class="text-xs text-zinc-400 leading-relaxed">
                Вы уверены, что хотите удалить пользователя 
                <span class="text-white font-semibold">«{{ userToDelete?.username }}»</span> 
                (ID <span class="font-mono text-zinc-300">#{{ userToDelete?.id }}</span>)? 
                Действие необратимо.
              </p>
            </div>
          </div>

          <div class="mt-6 flex items-center justify-end gap-3 pt-4 border-t border-[#27272A]">
            <button
              type="button"
              :disabled="isDeleting"
              @click="closeDeleteModal"
              class="px-4 py-2 rounded-lg text-xs font-semibold text-zinc-400 hover:text-white bg-[#202024] hover:bg-[#27272A] border border-[#3F3F46] transition-colors cursor-pointer disabled:opacity-50"
            >
              Отмена
            </button>
            <button
              type="button"
              :disabled="isDeleting"
              @click="confirmDelete"
              class="px-4 py-2 rounded-lg text-xs font-bold text-white bg-[#B00D15] hover:bg-[#8e0a11] shadow-[0_0_14px_rgba(176,13,21,0.35)] transition-all flex items-center gap-1.5 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <i v-if="isDeleting" class="pi pi-spin pi-spinner text-xs"></i>
              <span>{{ isDeleting ? 'Удаление...' : 'Да, удалить' }}</span>
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/auth/model/authStore'
import { showSuccess, showError } from '@/shared/lib/toastService'

interface UserItem {
  id: number
  username: string
  email: string
  roles: string[]
  currentRole: string
  branchId?: number | null
  branchName?: string | null
  saving?: boolean
}

const userStore = useUserStore()
const users = ref<UserItem[]>([])
const loading = ref(false)
const error = ref('')

const branches = ref<any[]>([
  { id: 1, name: 'DAR PRO GYM ALMATY' },
  { id: 2, name: 'DAR TEAM GYM ALMATY' },
  { id: 3, name: 'DAR TEAM FIGHT GYM ASTANA' }
])

const isDeleteModalOpen = ref(false)
const userToDelete = ref<UserItem | null>(null)
const isDeleting = ref(false)

const isCurrentUser = (user: UserItem): boolean => {
  return !!userStore.username && user.username.toLowerCase() === userStore.username.toLowerCase()
}

const getAuthHeaders = () => {
  let token = userStore.token
  if (!token) {
    const sessionUser = sessionStorage.getItem('user')
    if (sessionUser) {
      try {
        const parsed = JSON.parse(sessionUser)
        token = parsed.token
      } catch {}
    }
  }
  if (!token) {
    token = localStorage.getItem('token') || localStorage.getItem('jwt') || localStorage.getItem('accessToken') || ''
  }

  const headers: Record<string, string> = {
    'Content-Type': 'application/json'
  }
  if (token) {
    headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`
  }
  return headers
}

const fetchBranches = async () => {
  try {
    const res = await fetch('/api/v1/shop/branches')
    if (res.ok) {
      const data = await res.json()
      if (Array.isArray(data) && data.length > 0) branches.value = data
    }
  } catch {}
}

const fetchUsers = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch('/api/admin/v1/users', { headers: getAuthHeaders() })
    if (res.status === 401) {
      error.value = 'Сессия авторизации истекла. Пожалуйста, выйдите и войдите в аккаунт заново.'
      return
    }
    if (!res.ok) throw new Error(`Код ошибки сервера: ${res.status}`)
    const data = await res.json()
    initUsers(data)
  } catch (err: any) {
    error.value = err.message || 'Не удалось загрузить пользователей'
  } finally {
    loading.value = false
  }
}

const initUsers = (data: any[]) => {
  users.value = data.map(u => {
    let role = 'ROLE_USER'
    if (u.roles && u.roles.length > 0) {
      role = u.roles[0].startsWith('ROLE_') ? u.roles[0] : `ROLE_${u.roles[0]}`
    }
    return {
      ...u,
      currentRole: role,
      branchId: u.branchId || null,
      branchName: u.branchName || null,
      saving: false
    }
  })
}

const saveRole = async (user: UserItem) => {
  if (user.currentRole === 'ROLE_SHOP_MANAGER' && !user.branchId) {
    showError(`Пожалуйста, выберите филиал для менеджера магазина «${user.username}»`)
    return
  }

  user.saving = true
  try {
    let formattedRole = user.currentRole.trim().toUpperCase()
    if (!formattedRole.startsWith('ROLE_')) {
      formattedRole = `ROLE_${formattedRole}`
    }

    const payload = {
      roles: [formattedRole],
      branchId: user.currentRole === 'ROLE_SHOP_MANAGER' ? user.branchId : null
    }

    const res = await fetch(`/api/admin/v1/users/${user.id}/roles`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload)
    })

    if (res.status === 401) {
      showError('Сессия авторизации истекла. Нажмите на имя в шапке ➔ «Выйти» и войдите заново.')
      return
    }

    if (!res.ok) {
      let msg = `Ошибка сохранения (код ${res.status})`
      try {
        const errJson = await res.json()
        msg = errJson.message || errJson.error || msg
      } catch {}
      throw new Error(msg)
    }

    showSuccess(`Данные для ${user.username} успешно сохранены!`)
  } catch (err: any) {
    showError(err.message || 'Ошибка сохранения')
  } finally {
    user.saving = false
  }
}

const openDeleteModal = (user: UserItem) => {
  if (isCurrentUser(user)) {
    showError('Нельзя удалить собственный аккаунт')
    return
  }
  userToDelete.value = user
  isDeleteModalOpen.value = true
}

const closeDeleteModal = () => {
  if (isDeleting.value) return
  isDeleteModalOpen.value = false
  userToDelete.value = null
}

const confirmDelete = async () => {
  if (!userToDelete.value) return

  const targetUser = userToDelete.value
  isDeleting.value = true

  try {
    const res = await fetch(`/api/admin/v1/users/${targetUser.id}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    })

    if (res.status === 401) {
      showError('Сессия авторизации истекла. Войдите заново.')
      return
    }

    if (res.status === 200 || res.status === 204) {
      users.value = users.value.filter(u => u.id !== targetUser.id)
      isDeleteModalOpen.value = false
      userToDelete.value = null
      showSuccess(`Пользователь «${targetUser.username}» успешно удален`)
      return
    }

    let errorMessage = `Ошибка удаления (код ${res.status})`
    try {
      const errorJson = await res.json()
      errorMessage = errorJson.message || errorJson.error || errorMessage
    } catch {
      const text = await res.text()
      if (text) errorMessage = text
    }

    showError(errorMessage)
  } catch (err: any) {
    showError(err.message || 'Не удалось выполнить запрос на удаление')
  } finally {
    isDeleting.value = false
  }
}

onMounted(() => {
  fetchBranches()
  fetchUsers()
})
</script>