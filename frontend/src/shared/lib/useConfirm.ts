import { ref } from 'vue'

export type ConfirmOptions = {
  title?: string
  header?: string // для обратной совместимости
  message: string
  confirmText?: string
  cancelText?: string
}

const isVisible = ref(false)
const confirmTitle = ref('Подтвердите действие')
const confirmMessage = ref('')
const confirmButtonText = ref('Подтвердить')
const cancelButtonText = ref('Отмена')

let resolver: (value: boolean) => void = () => {}

export function useConfirmDialog() {
  const show = (options: ConfirmOptions): Promise<boolean> => {
    confirmTitle.value = options.title || options.header || 'Подтвердите действие'
    confirmMessage.value = options.message || ''
    confirmButtonText.value = options.confirmText || 'Подтвердить'
    cancelButtonText.value = options.cancelText || 'Отмена'
    isVisible.value = true

    return new Promise<boolean>((resolve) => {
      resolver = resolve
    })
  }

  const confirm = () => {
    isVisible.value = false
    resolver(true)
  }

  const cancel = () => {
    isVisible.value = false
    resolver(false)
  }

  return {
    isVisible,
    confirmTitle,
    confirmMessage,
    confirmButtonText,
    cancelButtonText,
    show,
    confirm,
    cancel,
  }
}