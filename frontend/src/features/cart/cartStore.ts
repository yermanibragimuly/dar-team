import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface CartItem {
  productId: number
  sku: string
  name: string
  price: number
  size: string
  quantity: number
  img: string
  maxStock: number
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const totalCount = computed(() => items.value.reduce((acc, item) => acc + item.quantity, 0))
  const totalPrice = computed(() => items.value.reduce((acc, item) => acc + item.price * item.quantity, 0))

  const addItem = (product: any, size: string, quantity = 1, maxStock = 999): boolean => {
    const existing = items.value.find(i => i.productId === product.id && i.size === size)
    if (existing) {
      if (existing.quantity + quantity > maxStock) {
        existing.quantity = maxStock
        existing.maxStock = maxStock
        return false
      }
      existing.quantity += quantity
      existing.maxStock = maxStock
      return true
    } else {
      const img = product.images?.[0]?.imageUrl || ''
      items.value.push({
        productId: product.id,
        sku: product.sku || '',
        name: product.name,
        price: product.retailPrice,
        size,
        quantity: Math.min(quantity, maxStock),
        img,
        maxStock
      })
      return true
    }
  }

  const updateQuantity = (productId: number, size: string, newQuantity: number) => {
    const item = items.value.find(i => i.productId === productId && i.size === size)
    if (item) {
      if (newQuantity <= 0) {
        removeItem(productId, size)
      } else {
        item.quantity = Math.min(newQuantity, item.maxStock || 999)
      }
    }
  }

  const removeItem = (productId: number, size: string) => {
    items.value = items.value.filter(i => !(i.productId === productId && i.size === size))
  }

  const clearCart = () => {
    items.value = []
  }

  return { items, totalCount, totalPrice, addItem, updateQuantity, removeItem, clearCart }
}, {
  persist: {
    storage: localStorage
  }
})