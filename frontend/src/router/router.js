import { createRouter, createWebHistory } from 'vue-router';
import BigMarket from '../components/BigMarket.vue';

const routes = [
  {
    path: '/',
    redirect: '/bigmarket/default',
  },
  {
    path: '/bigmarket/:activityId',
    name: 'BigMarket',
    component: BigMarket,
    meta: {
      title: 'Dasi 抽奖',
    },
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
