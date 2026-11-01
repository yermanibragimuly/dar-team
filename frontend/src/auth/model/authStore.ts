import { defineStore } from "pinia";
import type { User } from "@/types/auth"

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '' ,
    username: '',
    roles: []
  }),

  actions: {
    setUser(data: User) {
      this.token = data.token
      this.username = data.username
      this.roles = data.roles
    },

    logout() {
      this.token = ''
      this.username = ''
      this.roles = []
    }
  },

  persist: {
    storage: sessionStorage,
  }

})