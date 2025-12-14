import { createRouter, createWebHistory } from 'vue-router';
import BigMarket from '../components/BigMarket.vue';
import Login from '../components/Login.vue';
import NotFound from '../components/NotFound.vue';

const routes = [
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录 - Dasi 抽奖',
    },
  },
  {
    path: '/bigmarket/:activityId',
    name: 'BigMarket',
    component: BigMarket,
    meta: {
      title: 'Dasi 抽奖',
    },
  },
  {
    path: '/index',
    redirect: '/login',
  },
  {
    path: '/big-market',
    redirect: '/login',
  },
  {
    path: '/register',
    redirect: '/login',
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { title: '页面未找到' },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
});

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title} - BigMarket`;
  }
});

export default router;
