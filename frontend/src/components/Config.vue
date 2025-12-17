<template>
  <div class="config-page">
    <header>
      <h1>DCC 配置</h1>
    </header>

    <div class="section" v-for="section in sections" :key="section.title">
      <h2>{{ section.title }}</h2>
      <div class="config-list">
        <div
          v-for="item in section.items"
          :key="item.key"
          class="config-card"
        >
          <div class="config-key">{{ item.key }}</div>
          <div class="config-desc">{{ descMap[item.key] || '配置项' }}</div>
          <div class="config-control">
            <label v-if="item.type === 'toggle'" class="switch">
              <input type="checkbox" :checked="item.value === 'on'" @change="toggle(item)" />
              <span class="slider"></span>
              <span class="switch-label">{{ item.value === 'on' ? '开启' : '关闭' }}</span>
            </label>
            <div v-else class="input-row">
              <input v-model="item.editing" type="text" />
              <button @click="save(item)">确认</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, computed } from 'vue';
import api from '../request/api.js';

const configs = reactive([]);
const descMap = {
  degradeRaffle: '降级抽奖接口',
  degradeConvert: '降级兑换接口',
  degradeRecharge: '降级充值接口',
  degradeBehavior: '降级互动接口',

  rateLimitEnable: '开启抽奖接口限流',
  rateLimitApiQPS: '接口 QPS 阈值',
  rateLimitUserQPS: '单用户 QPS 阈值',
  
  circuitBreakerEnable: '开启抽奖接口熔断',
  circuitBreakerThreshold: '失败阈值(次)',
  circuitBreakerWindowTime: '统计窗口(s)',
  circuitBreakerOpenTime: '熔断时长(s)',

  redisLockTTL: '持有时间(ms)',
  redisLockWaitTime: '等待时间(ms)',
  redisLockMaxRetry: '最大重试次数',
};

const sectionSchema = [
  {
    title: '降级配置',
    keys: ['degradeRaffle', 'degradeConvert', 'degradeRecharge', 'degradeBehavior'],
  },
  {
    title: '限流配置',
    keys: ['rateLimitEnable', 'rateLimitApiQPS', 'rateLimitUserQPS'],
  },
  {
    title: '熔断配置',
    keys: ['circuitBreakerEnable', 'circuitBreakerThreshold', 'circuitBreakerWindowTime', 'circuitBreakerOpenTime'],
  },
  {
    title: '分布式锁配置',
    keys: ['redisLockTTL', 'redisLockWaitTime', 'redisLockMaxRetry'],
  },
];

const knownKeys = new Set(sectionSchema.flatMap((section) => section.keys));

const toggleKeys = new Set(
  Object.keys(descMap).filter((key) => key.includes('Enable') || key.startsWith('degrade')),
);

const normalizeType = (key) => (toggleKeys.has(key) ? 'toggle' : 'text');

const loadAll = async () => {
  try {
    const res = await api.dccGetAll();
    configs.splice(0, configs.length);
    Object.entries(res || {}).forEach(([key, value]) => {
      if (!knownKeys.has(key)) return;
      configs.push({ key, value, editing: value, type: normalizeType(key) });
    });
  } catch (error) {
    window.alert(error?.message || '获取配置失败');
  }
};

const sections = computed(() => {
  const configMap = Object.fromEntries(configs.map((c) => [c.key, c]));

  return sectionSchema
    .map((section) => {
      const items = section.keys.map((key) => configMap[key]).filter(Boolean);
      return { title: section.title, items };
    })
    .filter((section) => section.items.length);
});

const refreshOne = async (item) => {
  try {
    const val = await api.dccGet({ key: item.key });
    item.value = val;
    item.editing = val;
  } catch (error) {
    window.alert(error?.message || '获取配置失败');
  }
};

const save = async (item) => {
  try {
    await api.dccSet({ key: item.key, value: item.editing });
    await refreshOne(item);
  } catch (error) {
    window.alert(error?.message || '更新失败');
  }
};

const toggle = async (item) => {
  try {
    await api.dccToggle({ key: item.key });
    await refreshOne(item);
  } catch (error) {
    window.alert(error?.message || '切换失败');
  }
};

onMounted(loadAll);
</script>

<style scoped>
.config-page {
  min-height: 100vh;
  padding: 1.8rem 2rem 4rem;
  background: linear-gradient(135deg, #eef2ff 0%, #e8f0ff 50%, #dde8ff 100%);
  color: #1f2a44;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

header h1 {
  margin: 0 0 0.2rem;
  font-size: 2.2rem;
}

header p {
  margin: 0 0 1rem;
  color: #4b5563;
  font-weight: 600;
}

.section + .section {
  margin-top: 1.2rem;
}

.config-list {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  grid-auto-rows: 1fr;
  gap: 0.8rem;
}

.section h2 {
  margin: 3rem 0 1rem;
  font-size: 1.2rem;
  color: #1f2a44;
}

.config-card {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 14px;
  padding: 0.9rem 1rem;
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  height: 100%;
}

.config-key {
  font-weight: 800;
  color: #1f2a44;
}

.config-desc {
  color: #4b5563;
  font-size: 0.92rem;
}

.config-control {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.6rem;
}

.input-row {
  display: flex;
  gap: 0.4rem;
  width: 100%;
}

.input-row input {
  flex: 1;
  padding: 0.5rem 0.6rem;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 10px;
}

.input-row button {
  border: none;
  border-radius: 10px;
  padding: 0.5rem 0.8rem;
  background: linear-gradient(135deg, #4f6bff, #7b8dff);
  color: #fff;
  font-weight: 800;
  cursor: pointer;
}

.switch {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: relative;
  width: 44px;
  height: 24px;
  background: #cbd5e1;
  border-radius: 999px;
  transition: 0.2s ease;
}

.slider::before {
  content: '';
  position: absolute;
  width: 18px;
  height: 18px;
  left: 4px;
  top: 3px;
  background: #fff;
  border-radius: 50%;
  transition: 0.2s ease;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.18);
}

input:checked + .slider {
  background: linear-gradient(135deg, #30c97c, #2aa56b);
}

input:checked + .slider::before {
  transform: translateX(18px);
}

.switch-label {
  font-weight: 700;
  color: #2f3b57;
}

.value-tip {
  color: #6b7280;
}
</style>
