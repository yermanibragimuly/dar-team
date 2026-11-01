<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccess, showError } from '@/shared/lib/toastService'
import { getconfirmUser } from '@/services/auth-services'

const route = useRoute();
const toroute = useRouter();

onMounted(async () => {
  const token = route.query.token; // Берем токен из URL
  try {
    await getconfirmUser(token)
    
    showSuccess('Аккаунт успешно активирован!')
    
    toroute.push('/login')
  } catch (error) {
    showError(error)
  }
});

</script>