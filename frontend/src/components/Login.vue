<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>Dasi 抽奖系统</h1>

      <div class="tabs">
        <button
          :class="{ active: mode === 'login' }"
          @click="mode = 'login'"
        >
          登录
        </button>
        <button
          :class="{ active: mode === 'register' }"
          @click="mode = 'register'"
        >
          注册
        </button>
      </div>

      <form class="auth-form" novalidate @submit.prevent="handleSubmit">
        <label>
          <span>用户ID</span>
          <input
            v-model.trim="form.userId"
            type="text"
            placeholder="请输入用户ID"
            @input="errors.userId = ''"
          />
          <small v-if="errors.userId" class="error-text">{{ errors.userId }}</small>
        </label>
        <label>
          <span>密码</span>
          <input
            v-model.trim="form.password"
            type="password"
            placeholder="请输入密码"
            @input="errors.password = ''"
          />
          <small v-if="errors.password" class="error-text">{{ errors.password }}</small>
        </label>
        <button class="auth-submit" type="submit" :disabled="submitting">
          {{ submitting ? '处理中...' : mode === 'login' ? '登录' : '注册' }}
        </button>
      </form>
    </div>
    <transition name="fade">
      <div v-if="errorModal.visible" class="error-modal-overlay" @click="closeError">
        <div class="error-modal" @click.stop>
          <h3>出错了</h3>
          <p>{{ errorModal.message }}</p>
          <button class="auth-submit danger" type="button" @click="closeError">我知道了</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../request/api.js';

const router = useRouter();
const mode = ref('login');
const submitting = ref(false);
const form = reactive({
  userId: '',
  password: '',
});
const errors = reactive({
  userId: '',
  password: '',
});
const errorModal = reactive({ visible: false, message: '' });

const showError = (msg) => {
  errorModal.visible = true;
  errorModal.message = msg || '操作失败，请稍后再试';
};

const closeError = () => {
  errorModal.visible = false;
  errorModal.message = '';
};

const handleSubmit = async () => {
  errors.userId = form.userId ? '' : '请输入用户ID';
  errors.password = form.password ? '' : '请输入密码';
  if (errors.userId || errors.password) return;
  submitting.value = true;
  try {
    if (mode.value === 'register') {
      const reg = await api.register({ userId: form.userId, password: form.password });
      if (reg?.token) {
        localStorage.setItem('bigmarket_token', reg.token);
      }
    }
    const loginRes = await api.login({ userId: form.userId, password: form.password });

    localStorage.setItem('bigmarket_user', form.userId);
    if (loginRes?.token) {
      localStorage.setItem('bigmarket_token', loginRes.token);
    }
    router.push({
      path: '/bigmarket/10001',
    });
  } catch (error) {
    showError(error?.message || '操作失败，请稍后再试');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
:global(body) {
  margin: 0;
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.65), rgba(210, 220, 255, 0.4) 40%, rgba(185, 200, 255, 0.25) 70%, rgba(150, 170, 240, 0.18)),
    linear-gradient(135deg, #eef2ff 0%, #dee6ff 50%, #ccd6ff 100%);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  overflow: hidden;
}

.auth-page {
  height: 100dvh;
  min-height: 100dvh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 20vh;
  padding-left: 2rem;
  padding-right: 2rem;
  overflow: hidden;
  box-sizing: border-box;
}

.auth-card {
  width: min(420px, 90vw);
  background: rgba(255, 255, 255, 0.22);
  border-radius: 20px;
  padding: 1.8rem 1.6rem;
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  box-shadow: 0 18px 32px rgba(0, 0, 0, 0.18);
  text-align: center;
  color: #1f2a44;
}

.auth-card h1 {
  margin: 0 0 1.0rem;
  font-size: 2.4rem;
  font-weight: 800;
}

.tabs {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.4rem;
  margin-bottom: 1.2rem;
}

.tabs button {
  border: none;
  border-radius: 12px;
  padding: 0.65rem 0.8rem;
  font-weight: 700;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.35);
  color: #1f2a44;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
  outline: none;
  box-shadow: none;
}

.tabs button.active {
  background: linear-gradient(135deg, #4f6bff, #7b8dff);
  color: #fff;
  box-shadow: 0 10px 22px rgba(79, 107, 255, 0.35);
}

.tabs button:hover {
  transform: translateY(-2px);
}

.tabs button:focus-visible {
  outline: none;
  box-shadow: none;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 0.95rem;
}

.auth-form label {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.35rem;
  font-weight: 700;
  color: #2f3b57;
}

.auth-form input {
  width: 100%;
  padding: 0.75rem 0.85rem;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.50);
  font-size: 0.95rem;
  outline: none;
  transition: box-shadow 0.18s ease, border-color 0.18s ease;
  box-sizing: border-box;
  color: #1f2a44;
}

.auth-form input:focus {
  border-color: #7b8dff;
  box-shadow: 0 0 0 3px rgba(79, 107, 255, 0.15);
}

.error-text {
  color: #d14343;
  font-size: 0.82rem;
  margin-top: 0.2rem;
}

.auth-submit {
  margin-top: 0.2rem;
  border: none;
  width: 100%;
  padding: 0.85rem 1rem;
  border-radius: 14px;
  font-weight: 900;
  color: #ffffff;
  cursor: pointer;
  background: linear-gradient(135deg, #30c97c, #2aa56b);
  box-shadow: 0 14px 26px rgba(46, 179, 128, 0.35);
  transition: transform 0.18s ease, box-shadow 0.18s ease, filter 0.18s ease;
}

.auth-submit.danger{
  background: linear-gradient(135deg, #ef4444, #b91c1c);
  box-shadow: 0 14px 26px rgba(239, 68, 68, 0.35);
}

.auth-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 18px 34px rgba(46, 179, 128, 0.45);
  filter: saturate(1.05);
}

.auth-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.error-modal-overlay{
  position: fixed;
  inset: 0;
  background: radial-gradient(circle at 50% 40%, rgba(248, 113, 113, 0.16), rgba(0,0,0,0.55) 65%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
  backdrop-filter: blur(6px);
}

.error-modal{
  width: min(360px, 90vw);
  background: linear-gradient(165deg, rgba(255,255,255,0.98), rgba(255,255,255,0.92));
  border-radius: 18px;
  padding: 1.1rem 1rem 1rem;
  text-align: center;
  box-shadow: 0 18px 32px rgba(0,0,0,0.22);
  color: #b91c1c;
}

.error-modal h3{
  margin: 0 0 0.6rem;
  font-weight: 900;
}

.error-modal p{
  margin: 0 0 0.8rem;
  font-weight: 700;
  color: #1f2a44;
}

.fade-enter-active {
  animation: fade-in 0.2s ease-out both;
}
.fade-leave-active {
  animation: fade-out 0.15s ease-in both;
}

@keyframes fade-in {
  from { opacity: 0; transform: scale(0.98); }
  to { opacity: 1; transform: scale(1); }
}
@keyframes fade-out {
  from { opacity: 1; transform: scale(1); }
  to { opacity: 0; transform: scale(0.98); }
}
</style>
