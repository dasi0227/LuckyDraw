import { createRouter, createWebHistory } from 'vue-router';
import LuckyDraw from '../components/LuckyDraw.vue';
import Auth from '../components/Auth.vue';
import Config from '../components/Config.vue';
import NotFound from '../components/NotFound.vue';

const routes = [
  {
    path: '/',
    redirect: '/auth',
  },
  {
    path: '/index',
    redirect: '/auth',
  },
  {
    path: '/register',
    redirect: '/auth',
  },
  {
    path: '/login',
    redirect: '/auth',
  },
  {
    path: '/auth',
    name: 'Auth',
    component: Auth,
    meta: {
      title: '登陆 - Dasi 抽奖',
    },
  },
  {
    path: '/luckydraw/:activityId',
    name: 'LuckyDraw',
    component: LuckyDraw,
    meta: {
      title: 'Dasi 抽奖',
    },
  },
  {
    path: '/config',
    name: 'Config',
    component: Config,
    meta: {
      title: 'DCC 配置',
    },
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
    document.title = `${to.meta.title} - LuckyDraw`;
  }
});

export default router;
