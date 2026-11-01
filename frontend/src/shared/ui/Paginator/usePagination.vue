<script setup lang="ts">

 defineProps<{
  totalElements: number | undefined
  pageSize: number | undefined
 }>()

 const emit = defineEmits<{
  (e: 'page-change', pageEvent:{page: number, rows: number}):void
 }>()

</script>

<template>
<Paginator 
:rows="9" 
:totalRecords="totalElements"
@page="emit('page-change', $event)"
/>
</template> 