import { BaseApi } from "@/server/index";
import { showError } from '@/shared/lib/toastService'
import router from '@/router'

BaseApi.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('user')
  if(token) {
    try {
      const userToken = JSON.parse(token)
      if(userToken.token){
        config.headers.Authorization = `Bearer ${userToken.token}`
      }
    }catch(err){
      showError(err)
    }
  }
  return config
})

let redirecting = false

BaseApi.interceptors.response.use(
  (r) => r,
  (error) => {
    const status = error?.response?.status

    if(status === 401 && !redirecting){
      redirecting = true
      sessionStorage.removeItem('token')
      // router.push('/login').finally(() => { redirecting = false })
    }else if (status === 403 && !redirecting){
      showError('Недостаточно права доступа')
      router.push('/').finally(() => { redirecting = false })
    }
    return Promise.reject(error)
  }
)