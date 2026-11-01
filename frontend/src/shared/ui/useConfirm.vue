<template>
  <Teleport to="body">
    <div 
      v-if="isVisible" 
      class="fixed inset-0 z-[9999] bg-black/75 backdrop-blur-sm flex items-center justify-center p-4 transition-all"
      @click.self="cancel"
    >
      <div class="bg-[#18181B] border border-[#27272A] w-full max-w-md rounded-2xl shadow-2xl overflow-hidden p-6 text-zinc-100 animate-modal-in">
        <div class="flex items-start gap-4">
          <!-- Красная плашка с иконкой предупреждения -->
          <div class="w-12 h-12 rounded-xl bg-red-950/50 border border-red-800/60 flex items-center justify-center shrink-0 text-red-500">
            <i class="pi pi-exclamation-triangle text-xl"></i>
          </div>

          <div class="space-y-1.5 flex-1 min-w-0">
            <h3 class="text-base font-bold text-white tracking-wide" v-html="renderAppleEmoji(confirmTitle)"></h3>
            <p class="text-xs text-zinc-400 leading-relaxed break-words whitespace-pre-line" v-html="renderAppleEmoji(confirmMessage)"></p>
          </div>
        </div>

        <div class="mt-6 flex items-center justify-end gap-3 pt-4 border-t border-[#27272A]">
          <button
            type="button"
            @click="cancel"
            class="px-4 py-2 rounded-lg text-xs font-semibold text-zinc-400 hover:text-white bg-[#202024] hover:bg-[#27272A] border border-[#3F3F46] transition-colors cursor-pointer"
          >
            {{ cancelButtonText }}
          </button>
          <button
            type="button"
            @click="confirm"
            class="px-4 py-2 rounded-lg text-xs font-bold text-white bg-[#B00D15] hover:bg-[#8e0a11] shadow-[0_0_14px_rgba(176,13,21,0.35)] transition-all flex items-center gap-1.5 cursor-pointer"
          >
            <span>{{ confirmButtonText }}</span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { useConfirmDialog } from '@/shared/lib/useConfirm'
import { renderAppleEmoji } from '@/shared/lib/appleEmoji'

const {
  isVisible,
  confirmTitle,
  confirmMessage,
  confirmButtonText,
  cancelButtonText,
  confirm,
  cancel
} = useConfirmDialog()
</script>

<style scoped>
@keyframes modalIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
.animate-modal-in {
  animation: modalIn 0.2s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>