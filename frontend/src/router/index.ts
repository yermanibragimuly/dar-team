import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/auth/model/authStore";
import { showError } from '@/shared/lib/toastService'

const routes = [
    {
      path: '/',
      name: 'Home',
      component: () => import('../components/mainPage/backWall/BackWall.vue'),
    },
    {
      path: '/login',
      name: 'login',
      components: {
        default: () => import('@/components/mainPage/backWall/BackWall.vue'),
        modal: () => import('@/auth/pages/auth.vue'),
      },
      props: { modal: { mode: 'login' } },
    },
    {
      path: '/forgot-password',
      name: 'forgotPassword',
      components: {
        default: () => import('@/components/mainPage/backWall/BackWall.vue'),
        modal: () => import('@/auth/pages/ForgotPassword.vue'),
      },
      props: { modal: { mode: 'login' } },
    },
    {
      path: '/reset-password',
      name: 'resetPassword',
      components: {
        default: () => import('@/components/mainPage/backWall/BackWall.vue'),
        modal: () => import('@/auth/pages/ResetPassword.vue'),
      },
      props: { modal: { mode: 'login' } },
    },
    {
      path: '/register',
      name: 'register',
      components: {
        default: () => import('@/components/mainPage/backWall/BackWall.vue'),
        modal: () => import('@/auth/pages/Register.vue'),
      },
      props: { modal: { mode: 'register' } },
    },
    {
      path: '/confirm-email',
      name: 'ConfirmEmail',
      component: () => import('@/auth/pages/ConfirmEmail.vue'),
    },
    {
      path: '/teams',
      redirect: '/teams/fighters',
      name: 'Team',
      component: () => import('../components/teamPage/backWallTeam/BackWallTeam.vue'),
      children: [
        {
          path: 'coaches',
          component: () => import('../components/teamPage/mainTeam/BlockCoaches.vue'),
        },
        {
          path: 'fighters',
          component: () => import('../components/teamPage/mainTeam/BlockFighters.vue'),
        },
        {
          path: 'video',
          component: () => import('../components/teamPage/mainTeam/BlockVideo.vue'),
        }
      ]
    },
    {
      path: '/news',
      name: 'News',
      component: () => import('../components/newsPage/backWallNews/BackWallNews.vue')
    },
    {
      path: '/outside',
      name: 'Outside',
      component: () => import('../components/outsideWindowPage/backWallOutsideWindow/BackWallOutsideWindow.vue')
    },
    {
      path: '/branch',
      name: 'Branch',
      component: () => import('../components/branchPage/backWallBranch/BackWallBranch.vue')
    },
    {
      path: '/about',
      name: 'About',
      component: () => import('../components/aboutUsPage/backWallAbout/BackWallAbout.vue')
    },
    {
      path: '/shop',
      name: 'Shop',
      component: () => import('../views/ShopView.vue')
    },
    {
      path: '/checkout',
      name: 'Checkout',
      component: () => import('../views/CheckoutView.vue')
    },
    {
      path: '/checkout/success',
      name: 'CheckoutSuccess',
      component: () => import('../views/CheckoutSuccessView.vue')
    },
    {
      path: '/mock-payment/:orderId',
      name: 'MockPayment',
      component: () => import('../views/MockPaymentView.vue')
    },
    {
      path: '/auth',
      name: 'auth',
      component: () => import('../auth/pages/auth.vue')
    },
    {
      path: '/admin',
      name: 'Wall',
      component: () => import('../admin/pages/Wall.vue'),
      redirect: '/admin/fighters',
      children: [
        {
          path: 'fighters',
          meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
          component: () => import('../admin/pages/fighters.vue'),
        },
        {
          path: 'coache',
          meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
          component: () => import('../admin/pages/couch.vue'),
        },
        {
          path: 'amateur',
          meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
          component: () => import('../admin/pages/gtoup.vue'),
        },
        {
          path: 'news',
          meta: { requiresAuth: true, roles: ['ROLE_ADMIN', 'ROLE_MODERATOR'] },
          component: () => import('../admin/pages/news.vue')
        },
        {
          path: 'video',
          meta: { requiresAuth: true, roles: ['ROLE_ADMIN', 'ROLE_MODERATOR'] },
          component: () => import('../admin/pages/video.vue')
        },
        {
          path: 'partners',
          name: 'AdminPartners',
          meta: { requiresAuth: true, roles: ['ROLE_ADMIN', 'ROLE_MODERATOR'] },
          component: () => import('../admin/pages/partners.vue'),
        },
        {
          path: 'shop',
          name: 'AdminShop',
          meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
          component: () => import('../admin/pages/ShopAdmin.vue'),
        },
        {
          path: 'shop/orders',
          name: 'AdminShopOrders',
          meta: { requiresAuth: true, roles: ['ROLE_ADMIN', 'ROLE_SHOP_MANAGER'] },
          component: () => import('../admin/pages/ShopOrdersAdmin.vue'),
        },
        {
          path: 'shop/branch-stock',
          name: 'AdminBranchStock',
          meta: { requiresAuth: true, roles: ['ROLE_ADMIN', 'ROLE_SHOP_MANAGER'] },
          component: () => import('../admin/pages/BranchStockAdmin.vue'),
        },
        {
          path: 'users',
          name: 'AdminUsers',
          meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
          component: () => import('../admin/pages/AdminUsersView.vue'),
        },
      ]
    },
    {
      path: '/profile',
      component: () => import('../profile/pages/BackWall.vue'),
      redirect: '/profile/info',
      meta: { requiresAuth: true },
      children: [
        {
          path: 'info',
          component: () => import('../profile/pages/UserProfilePage.vue')
        },
        {
          path: 'videos',
          component: () => import('../profile/pages/UserVideo.vue')
        }
      ]
    }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to: any, _from: any, next) => {
  const userStore = useUserStore()
  const isAuthenticated = !!userStore.token
  const userRoles = (Array.isArray(userStore.roles) ? userStore.roles : [userStore.roles]) as string[] || []

  // Редирект менеджера магазина сразу в заказы
  if (to.path === '/admin' && userRoles.includes('ROLE_SHOP_MANAGER') && !userRoles.includes('ROLE_ADMIN')) {
    return next('/admin/shop/orders')
  }

  if (to.meta.requiresAuth && !isAuthenticated) {
    showError("Для доступа нужно войти в систему")
    return next("/")
  }

  if (to.meta.roles && !to.meta.roles.some((role: any) => userRoles.includes(role))) {
    showError("Недостаточно прав для доступа")
    return next("/")
  }

  next()
})

export default router