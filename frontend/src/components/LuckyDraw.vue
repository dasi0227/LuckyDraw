<template>
  <div class="luckydraw-page">
    
  <div class="top-area">
    <div v-if="activityBannerText" class="activity-banner">
      <div class="banner-track">
        <span class="banner-text">{{ activityBannerText }}</span>
      </div>
    </div>

    <div class="top-bar">
        <div class="page-brand">Dasi 抽奖系统</div>

        <div class="page-actions">
            <button class="action-btn" type="button" @click="openRechargeModal">积分充值</button>
            <button class="action-btn" type="button" @click="openActivityModal">切换活动</button>
            <button class="action-btn" type="button" @click="handleLogout">退出登录</button>
        </div>
    </div>
  </div>


    <!-- Header -->
    <header class="page-header">
      <h1>{{ activityInfo?.activityName || '抽奖活动' }}</h1>
    </header>

    <div class="content-grid">
      <!-- BehaviorPanel + UserPanel + ConvertPanel -->
      <section class="user-section">
        <div class="behavior-panel">
          <div class="panel-title">
            <h2>互动任务</h2>
          </div>
          <div class="behavior-grid convert-grid card-grid">
            <div
              v-for="action in behaviors"
              :key="action.name || action.type"
              class="behavior-card convert-card frost-card"
            >
              <div class="behavior-name convert-name">{{ action.rewardDesc || action.name }}</div>
              <button
                class="behavior-btn convert-btn card-btn"
                :class="{ done: action.done }"
                :disabled="action.done"
                @click="handleBehavior(action)"
              >
                {{ action.behaviorName || action.name }}
              </button>
            </div>
          </div>
        </div>

        <div class="user-panel">
          <h2>用户信息</h2>
          <ul>
            <li>
              <span>用户名</span>
              <strong>{{ userStats.name }}</strong>
            </li>
            <li>
              <span>用户积分</span>
              <strong>{{ userStats.points }}</strong>
            </li>
            <li>
              <span>总剩余抽奖次数</span>
              <strong>{{ userStats.totalChance }}</strong>
            </li>
            <li>
              <span>当月剩余抽奖次数</span>
              <strong>{{ userStats.monthChance }}</strong>
            </li>
            <li>
              <span>当日剩余抽奖次数</span>
              <strong>{{ userStats.dayChance }}</strong>
            </li>
            <li>
              <span>当月待领取抽奖次数</span>
              <strong>{{ userStats.monthPending }}</strong>
            </li>
            <li>
              <span>当日待领取抽奖次数</span>
              <strong>{{ userStats.dayPending }}</strong>
            </li>
          </ul>
        </div>

        <div class="convert-panel">
          <div class="panel-title">
            <h2>积分兑换</h2>
          </div>
          <div class="convert-grid card-grid">
            <div
              v-for="item in convertOptions"
              :key="item.id"
              class="convert-card frost-card"
            >
            <div class="convert-name">{{ item.title }}</div>
            <button class="convert-btn card-btn" @click="handleRedeemPoints(item)">
              {{ item.points }} 积分
            </button>
          </div>
          </div>
        </div>
      </section>

      <!-- AllHistoryList + AwardWheel + PersonalHistoryList -->
      <section class="core-section">
        <div class="history-list">
          <div class="panel-title">
            <h3>历史中奖</h3>
          </div>
          <ul>
            <li v-for="record in historyRecords" :key="record.id">
              <strong>{{ record.user }}</strong>
              <span>获得</span>
              <strong>{{ record.prize }}</strong>
              <span>· {{ record.time }}</span>
            </li>
          </ul>
        </div>

        <div class="lottery-wrapper">
          <div class="grid">
            <div
              v-for="(prize, index) in gridPrizes"
            :key="prize.id"
            class="grid-item"
            :class="{ active: highlightIndex === index, button: index === 4 }"
            @click="index === 4 && startGridLottery()"
          >
              <template v-if="index === 4">
                <button :disabled="isRolling" class="grid-button">
                  {{ isRolling ? '抽奖中...' : '开始抽奖' }}
                </button>
              </template>
              <template v-else>
                <div class="prize-card" :style="{ background: prize.bg }">
                  <span
                    v-if="prize.rate !== undefined && prize.rate !== null"
                    class="prize-rate"
                  >
                    {{ formatAwardRate(prize.rate) }}
                  </span>
                  <div class="prize-name">{{ prize.label }}</div>
                  <div v-if="prize.isLock" class="lock-overlay">
                    <span>再抽奖 {{ prize.needLotteryCount }} 次解锁</span>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>

        <div class="history-list personal">
          <div class="panel-title">
            <h3>我的中奖</h3>
          </div>
          <ul>
            <li v-for="record in personalRecords" :key="record.id">
              <strong>{{ record.prize }}</strong>
              <span>· {{ record.time }}</span>
            </li>
          </ul>
        </div>
      </section>
    </div>

    <section class="bottom-area">
      <div class="progress-header">
        <span>幸运值</span>
        <strong>{{ luckValue }} / {{ luckGoal }}</strong>
      </div>
      <div class="progress-bar">
        <div class="progress-marks">
          <div
            v-for="mark in luckMarks"
            :key="mark.key"
            class="progress-mark"
            :style="{ left: `${mark.percent}%` }"
          >
            <span
              class="mark-label"
              :data-tip="`${mark.detail || mark.label || mark.value || ''} 中奖概率大幅度提升`"
            >
              {{ mark.label ?? mark.value }}
            </span>
            <span
              v-if="mark.percent > 0 && mark.percent < 100"
              class="mark-line"
            ></span>
          </div>
        </div>
        <div class="progress-fill" :style="{ width: `${luckPercent}%` }"></div>
      </div>
    </section>

    <transition name="fade">
      <div v-if="rewardModal.visible" class="reward-modal-overlay" @click="closeRewardModal">
        <div class="reward-modal" @click.stop>
          <h3>{{ '🎉 获取成功 🎉' }}</h3>
          <ul>
            <li v-for="(text, idx) in rewardModal.rewards" :key="idx">
              <span class="reward-pill">{{ text }}</span>
            </li>
          </ul>
          <button class="modal-close" @click="closeRewardModal">OK</button>
        </div>
      </div>
    </transition>
    <transition name="fade">
      <div v-if="warnModal.visible" class="warn-modal-overlay" @click="closeWarnModal">
        <div
          class="warn-modal"
          :class="{ shaking: warnModal.shake }"
          @click.stop
          @animationend="warnModal.shake = false"
        >
          <h3>{{ warnModal.title }}</h3>
          <p v-for="(line, idx) in warnModal.lines" :key="idx">{{ line }}</p>
          <button class="modal-close warn-close" @click="closeWarnModal">我已了解</button>
        </div>
      </div>
    </transition>
    <transition name="fade">
      <div v-if="errorModal.visible" class="error-modal-overlay" @click="closeErrorModal">
        <div
          class="error-modal"
          :class="{ shaking: errorModal.shake }"
          @click.stop
          @animationend="errorModal.shake = false"
        >
          <p>{{ errorModal.message }}</p>
          <button class="modal-close error-close" @click="closeErrorModal">我已知晓</button>
        </div>
      </div>
    </transition>
    <transition name="fade">
      <div v-if="activityModal.visible" class="activity-modal-overlay" @click="closeActivityModal">
        <div class="activity-modal" @click.stop>
          <h3>切换活动</h3>
          <p class="activity-sub">选择一个活动进入抽奖</p>
          <div class="activity-list">
            <button
              v-for="item in activityModal.items"
              :key="item.activityId"
              class="activity-item"
              @click="jumpActivity(item.activityId)"
            >
              <div class="activity-item__name">{{ item.activityName }}</div>
              <div class="activity-item__desc">{{ item.activityDesc }}</div>
            </button>
          </div>
          <button class="modal-close" @click="closeActivityModal">关闭</button>
        </div>
      </div>
    </transition>
    <transition name="fade">
      <div v-if="rechargeModal.visible" class="recharge-modal-overlay" @click="closeRechargeModal">
        <div class="recharge-modal" @click.stop>
          <h3>积分充值</h3>
          <p class="recharge-sub">选择充值档位和支付方式，确认后发起支付</p>
          <div class="recharge-list">
            <button
              v-for="item in rechargeModal.items"
              :key="item.tradeId"
              class="recharge-card"
              :class="{ active: selectedRecharge?.tradeId === item.tradeId }"
              @click="selectedRecharge = item"
            >
              <div class="recharge-value">
                <div class="recharge-money-plain">{{ item.tradeMoney }} 元</div>
                <div class="recharge-point-plain">{{ item.tradeValue }} 积分</div>
              </div>
            </button>
          </div>
          <div class="pay-row">
            <button class="pay-btn" :class="{ active: paySelection === '微信支付' }" @click="paySelection = '微信支付'" aria-label="微信支付">
              <img :src="payIcons.wechat" alt="微信支付" />
            </button>
            <button class="pay-btn" :class="{ active: paySelection === '支付宝' }" @click="paySelection = '支付宝'" aria-label="支付宝">
              <img :src="payIcons.alipay" alt="支付宝" />
            </button>
            <button class="pay-btn" :class="{ active: paySelection === '银联支付' }" @click="paySelection = '银联支付'" aria-label="银联支付">
              <img :src="payIcons.unionpay" alt="银联支付" />
            </button>
          </div>
          <div class="pay-submit-row">
            <button class="pill-btn" @click="askRecharge(selectedRecharge, paySelection)">发起支付</button>
            <button class="pill-btn ghost danger" @click="closeRechargeModal">关闭</button>
          </div>
        </div>
      </div>
    </transition>
    <transition name="fade">
      <div v-if="rechargeConfirm.visible" class="recharge-confirm-overlay" @click="closeRechargeConfirm">
        <div class="recharge-confirm" @click.stop>
          <h3>确认支付</h3>
          <p>
            使用 {{ rechargeConfirm.channel }} 支付
            <strong>{{ rechargeConfirm.item?.tradeMoney }}</strong>
            获取 <strong>{{ rechargeConfirm.item?.tradeValue }}</strong> 积分
          </p>
          <div class="confirm-actions">
            <button class="pill-btn" @click="submitRecharge">确认</button>
            <button class="pill-btn ghost danger" @click="closeRechargeConfirm">取消</button>
          </div>
        </div>
      </div>
    </transition>
    <canvas ref="confettiCanvas" class="confetti-canvas"></canvas>

  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { formatDateTime } from "../utils/utils.js";
import { namePool } from '../utils/name.js';
import { burstConfetti, resetConfetti } from '../utils/confetti.js';
import api from '../request/api.js';
import wechatIcon from '../assets/wechatpay.svg';
import alipayIcon from '../assets/alipay.svg';
import unionpayIcon from '../assets/unionpay.svg';

const userStats = reactive({
  points: 0,
  totalChance: 0,
  monthChance: 0,
  dayChance: 0,
  monthPending: 0,
  dayPending: 0,
});

const convertOptions = ref([]);
const wheelPrizes = ref([]);
const historyRecords = ref([]);
const personalRecords = ref([]);
const luckValue = ref(0);
const luckGoal = ref(150);
const luckMarksData = ref([]);
const rewardModal = ref({ visible: false, rewards: [], title: '' });
const warnModal = ref({ visible: false, title: '提示', lines: [], shake: false });
const errorModal = ref({ visible: false, message: '', shake: false });
const activityInfo = ref(null);
const behaviors = ref([]);
const confettiCanvas = ref(null);
const activityModal = reactive({ visible: false, items: [] });
const rechargeModal = reactive({ visible: false, items: [] });
const rechargeConfirm = reactive({ visible: false, item: null, channel: '' });
const selectedRecharge = ref(null);
const paySelection = ref('微信支付');
const payIcons = { wechat: wechatIcon, alipay: alipayIcon, unionpay: unionpayIcon };

const brushColors = [
  "linear-gradient(160deg, rgba(255,255,255,0.92), rgba(210,220,255,0.78))",
  "linear-gradient(160deg, rgba(255,255,255,0.92), rgba(222,210,255,0.78))",
  "linear-gradient(160deg, rgba(255,255,255,0.93), rgba(255,235,205,0.80))",
  "linear-gradient(160deg, rgba(255,255,255,0.93), rgba(205,245,230,0.80))",
  "linear-gradient(160deg, rgba(255,255,255,0.93), rgba(255,220,235,0.80))",
  "linear-gradient(160deg, rgba(255,255,255,0.93), rgba(225,240,255,0.80))",
  "linear-gradient(160deg, rgba(255,255,255,0.93), rgba(232,224,255,0.80))"
];

function randomBrush() {
  return brushColors[Math.floor(Math.random() * brushColors.length)];
}

function randomName() {
  return namePool[Math.floor(Math.random() * namePool.length)];
}

const gridOrderTemplate = [0, 1, 2, 5, 8, 7, 6, 3];
const gridPrizes = computed(() => {
  const layout = Array(9).fill(null);
  layout[4] = { id: 'center', label: 'button' };
  const basePrizes = wheelPrizes.value.slice(0, 8);
  gridOrderTemplate.forEach((gridIndex, orderIndex) => {
    layout[gridIndex] =
      basePrizes[orderIndex] || { id: `empty-${gridIndex}`, label: '敬请期待' };
  });
  return layout;
});

const luckPercent = computed(() => {
  if (!luckGoal.value) {
    return 0;
  }
  return Math.min(100, Math.round((luckValue.value / luckGoal.value) * 100));
});

const formatActivityTime = (value) => {
  if (!value) return '';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : formatDateTime(date);
};

const activityBannerText = computed(() => {
  if (!activityInfo.value) return '';
  const info = activityInfo.value;
  const begin = formatActivityTime(info.activityBeginTime);
  const end = formatActivityTime(info.activityEndTime);
  return `📢 📢 活动【${info.activityName}】火热开启：${info.activityDesc}，活动时间从 ${begin} 到 ${end}。截至目前，已有 ${info.activityAccountCount ?? 0} 人参与，累计抽奖 ${info.activityRaffleCount ?? 0} 次，已送出 ${info.activityAwardCount ?? 0} 份中奖奖品 —— 还在等什么？现在就来试试手气，下一位欧皇可能就是你 🎊🎊`;
});

const luckMarks = computed(() => {
  const goal = luckGoal.value || 1;
  return luckMarksData.value
    .filter((mark) => typeof mark.value === 'number' && !Number.isNaN(mark.value))
    .map((mark, idx) => {
      const percent = Math.min(100, Math.max(0, (mark.value / goal) * 100));
      return {
        ...mark,
        key: `${mark.value}-${idx}`,
        percent,
      };
    })
    .sort((a, b) => a.value - b.value);
});

const formatAwardRate = (rate) => {
  if (rate === undefined || rate === null) return '';
  const numeric = Number(rate);
  if (Number.isNaN(numeric)) return String(rate);
  const percent = numeric > 0 && numeric <= 1 ? numeric * 100 : numeric;
  return `${Math.round(percent)}%`;
};

const maxListLength = 10;

const handleBehavior = (action) => {
  if (action.done) {
    window.alert('今日已完成该任务');
    return;
  }
  runBehavior(action);
};

const addHistoryRecord = (record) => {
  historyRecords.value.unshift(record);
  if (historyRecords.value.length > maxListLength) {
    historyRecords.value.pop();
  }
};

const addPersonalRecord = (record) => {
  personalRecords.value.unshift(record);
  if (personalRecords.value.length > maxListLength) {
    personalRecords.value.pop();
  }
};

const seedHistoryRecords = (count = 10) => {
  if (!wheelPrizes.value.length) return;

  const now = Date.now();
  const times = Array.from({ length: count })
    .map(() => now - Math.floor(Math.random() * 60_000))
    .sort((a, b) => b - a);

  historyRecords.value = times.map((ts, idx) => {
    const randomPrize = wheelPrizes.value[Math.floor(Math.random() * wheelPrizes.value.length)];
    const d = new Date(ts);

    return {
      id: crypto.randomUUID ? crypto.randomUUID() : `${ts}-${idx}`,
      user: randomName(),
      prize: randomPrize.label,
      time: d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
    };
  });
};

const handleRedeemPoints = async (item) => {
  if (!currentActivityId.value) return;
  try {
    const resp = await api.doConvert({
      activityId: currentActivityId.value,
      tradeId: item.id,
    });
    const desc = resp?.tradeDesc || '兑换成功';
    rewardModal.value = {
      visible: true,
      title: '🎉 兑换成功，恭喜获得奖励 🎉',
      rewards: [desc],
    };
    const luckBonus = Math.floor(Math.random() * 10) + 1;
    await addFortuneLuck(luckBonus);
    createConfetti();
    setTimeout(() => {
      fetchActivityAccountData(currentActivityId.value, currentUserId.value);
    }, 400);
  } catch (error) {
    showErrorModal(error?.message || '积分兑换失败');
  }
};

const createConfetti = async () => {
  await burstConfetti(confettiCanvas.value, { x: 0.5, y: 0.35 });
};

const runBehavior = async (action) => {
  if (!currentActivityId.value) return;
  try {
    const resp = await api.doBehavior({
      activityId: currentActivityId.value,
      behaviorType: action.type || action.behaviorType,
    });
    const rewards = Array.isArray(resp?.rewardDescList) ? resp.rewardDescList : [];
    rewardModal.value = {
      visible: true,
      title: '🎉 互动成功，恭喜获得奖励 🎉',
      rewards: rewards.length ? rewards : ['感谢参与'],
    };
    const luckBonus = Math.floor(Math.random() * 10) + 1;
    await addFortuneLuck(luckBonus);
    createConfetti();
    await fetchActivityBehaviorData(currentActivityId.value, currentUserId.value);
    setTimeout(() => {
      fetchActivityAccountData(currentActivityId.value, currentUserId.value);
    }, 400);
  } catch (error) {
    showErrorModal(error?.message || '执行互动任务失败');
  }
};

const closeRewardModal = () => {
  rewardModal.value = { visible: false, rewards: [] };
  resetConfetti();
};

const showErrorModal = (message) => {
  errorModal.value = { visible: true, message: message || '服务异常', shake: true };
  setTimeout(() => {
    errorModal.value = { ...errorModal.value, shake: false };
  }, 400);
};

const closeErrorModal = () => {
  errorModal.value = { visible: false, message: '', shake: false };
};

const showWarnModal = ({ isLock, isEmpty, awardName }) => {
  const lines = [];
  let title = '提示';

  if (isLock) {
    title = '奖品未解锁';
    lines.push('当前抽奖次数不够，请多多参与抽奖！');
    lines.push(`获取兜底奖品：${awardName}`);
  } else if (isEmpty) {
    title = '奖品已抽完';
    lines.push('当前奖品库存不足，请尽早参与活动！');
    lines.push(`获取兜底奖品：${awardName}`);
  } else {
    lines.push('提示');
  }

  warnModal.value = { visible: true, title, lines, shake: true };
  setTimeout(() => {
    warnModal.value = { ...warnModal.value, shake: false };
  }, 400);
};

const closeWarnModal = () => {
  warnModal.value = { visible: false, title: '提示', lines: [], shake: false };
};

const route = useRoute();
const router = useRouter();
const currentActivityId = ref(route.params.activityId || 'default');
const currentUserId = ref(localStorage.getItem('luckydraw_user') || '');

const fetchActivityConvertData = async (activityId) => {
  if (!activityId) return;
  try {
    const response = await api.queryActivityConvert({ activityId });
    const trades = Array.isArray(response) ? response : [];
    convertOptions.value = trades.map((trade) => ({
      id: trade.tradeId,
      title: trade.tradeName,
      points: trade.tradePoint,
    }));
  } catch (error) {
    console.error('获取积分兑换信息失败: ', error);
  }
};

const fetchActivityAccountData = async (activityId) => {
  if (!activityId) return;
  try {
    const account = await api.queryActivityAccount({ activityId });

    Object.assign(userStats, {
      name: currentUserId.value,
      points: account?.accountPoint ?? 0,
      totalChance: account?.totalSurplus ?? 0,
      monthChance: account?.monthSurplus ?? 0,
      dayChance: account?.daySurplus ?? 0,
      monthPending: account?.monthPending ?? 0,
      dayPending: account?.dayPending ?? 0,
    });
  } catch (error) {
    console.error('获取用户信息失败: ', error);
  }
};

const fetchActivityLuckData = async (activityId) => {
  if (!activityId) return;
  try {
    const resp = await api.queryActivityLuck({ activityId });
    luckValue.value = resp?.accountLuck ?? 0;

    const thresholdEntries =
      resp?.luckThreshold && typeof resp.luckThreshold === 'object'
        ? Object.entries(resp.luckThreshold)
        : [];

    const marks = thresholdEntries.map(([value, labels], idx) => {
      const numeric = Number(value);
      const detail =
        Array.isArray(labels) && labels.length
          ? labels.join(' / ')
          : labels ?? numeric;
      return {
        value: Number.isNaN(numeric) ? 0 : numeric,
        label: Number.isNaN(numeric) ? value : numeric,
        detail,
        key: `luck-${idx}`,
      };
    });

    const maxThreshold = marks.reduce((max, item) => Math.max(max, item.value), 0);
    const fallbackGoal = luckGoal.value || 150;
    luckGoal.value = Math.max(maxThreshold, fallbackGoal);
    luckMarksData.value = marks;
  } catch (error) {
    console.error('获取幸运值信息失败: ', error);
  }
};

const addFortuneLuck = async (luckBonus) => {
  if (!currentActivityId.value) return;
  const luck = Math.max(1, Math.min(10, Number(luckBonus) || 1));
  try {
    const resp = await api.addFortune({
      activityId: currentActivityId.value,
      luck,
    });
    if (resp?.accountLuck !== undefined && resp?.accountLuck !== null) {
      luckValue.value = resp.accountLuck;
    } else {
      luckValue.value = (luckValue.value || 0) + luck;
    }
  } catch (error) {
    console.error('增加幸运值失败: ', error);
  }
};

const fetchActivityBehaviorData = async (activityId) => {
  if (!activityId) return;
  try {
    const resp = await api.queryActivityBehavior({ activityId });
    const list = Array.isArray(resp) ? resp : [];
    if (list.length) {
      behaviors.value = list.map((item, idx) => ({
        name: item.behaviorName || item.behaviorType || `任务${idx + 1}`,
        behaviorName: item.behaviorName || item.behaviorType || `任务${idx + 1}`,
        rewardDesc: item.rewardDesc || item.behaviorName || item.behaviorType || `任务${idx + 1}`,
        type: item.behaviorType,
        done: item.isDone === true,
      }));
    }
  } catch (error) {
    console.error('获取互动任务失败: ', error);
  }
};

const fetchActivityAwardData = async (activityId) => {
  if (!activityId) return;
  try {
    const awards = await api.queryActivityAward({ activityId });
    const normalized = Array.isArray(awards) ? awards : [];
    const sorted = normalized
      .slice()
      .sort((a, b) => (a?.awardIndex ?? 0) - (b?.awardIndex ?? 0));
    wheelPrizes.value = sorted.map((award, index) => ({
      id: award?.awardId ?? `award-${award?.awardIndex ?? index}`,
      label: award?.awardName ?? '敬请期待',
      rate: award?.awardRate,
      isLock: award?.isLock,
      needLotteryCount: award?.needLotteryCount ?? 0,
      bg: randomBrush()
    }));
    if (!historyRecords.value.length) {
      seedHistoryRecords(10);
    }
  } catch (error) {
    console.error('获取活动奖品信息失败: ', error);
  }
};

const fetchActivityInfo = async (activityId) => {
  if (!activityId) return;
  try {
    const info = await api.queryActivityInfo({ activityId });
    activityInfo.value = info || null;
  } catch (error) {
    console.error('获取活动信息失败: ', error);
  }
};

const fetchActivityList = async () => {
  try {
    const list = await api.queryActivityList();
    activityModal.items = Array.isArray(list) ? list : [];
  } catch (error) {
    console.error('获取活动列表失败: ', error);
  }
};

const fetchRechargeList = async (activityId) => {
  if (!activityId) return;
  try {
    const list = await api.queryActivityRecharge({ activityId });
    rechargeModal.items = Array.isArray(list) ? list : [];
  } catch (error) {
    console.error('获取充值列表失败: ', error);
  }
};

const fetchUserAwardData = async (activityId) => {
  if (!activityId) return;
  try {
    const awards = await api.queryUserAward({ activityId });
    const normalized = Array.isArray(awards) ? awards : [];
    const formatAwardTime = (time) => {
      const date = new Date(time);
      return Number.isNaN(date.getTime()) ? time : formatDateTime(date);
    };
    personalRecords.value = normalized.map((award, index) => ({
      id: award.awardId || `${award.awardName || 'award'}-${index}`,
      prize: award.awardName,
      time: formatAwardTime(award.awardTime),
    }));
  } catch (error) {
    console.error('获取用户获奖信息失败: ', error);
  }
};

const refreshAllData = async () => {
  if (!currentActivityId.value) return;
  try {
    await Promise.all([
      fetchActivityConvertData(currentActivityId.value),
      fetchActivityAccountData(currentActivityId.value),
      fetchActivityLuckData(currentActivityId.value),
      fetchUserAwardData(currentActivityId.value),
      fetchActivityBehaviorData(currentActivityId.value),
      fetchActivityAwardData(currentActivityId.value),
      fetchActivityInfo(currentActivityId.value),
      fetchRechargeList(currentActivityId.value),
    ]);
  } catch (error) {
    console.error('定时刷新数据失败: ', error);
  }
};

watch(
  () => route.params.activityId,
  async (newActivityId) => {
    currentActivityId.value = newActivityId;
    currentUserId.value = localStorage.getItem('luckydraw_user') || '';
    if (!currentUserId.value) {
      router.push('/login');
      return;
    }
    fetchActivityConvertData(currentActivityId.value);
    await fetchActivityAccountData(currentActivityId.value);
    await fetchActivityLuckData(currentActivityId.value);
    fetchUserAwardData(currentActivityId.value);
    fetchActivityBehaviorData(currentActivityId.value);
    fetchActivityAwardData(currentActivityId.value);
    fetchActivityInfo(currentActivityId.value);
    if (!historyRecords.value.length) {
      seedHistoryRecords(10);
    }
  },
  { immediate: true },
);

const highlightSequence = [0, 1, 2, 5, 8, 7, 6, 3];
const highlightIndex = ref(4);
const isRolling = ref(false);
let rollingTimer = null;
let historyTimer = null;
let pauseTimer = null;
let autoRefreshTimer = null;

const startGridLottery = async () => {
  const prizeCount = Math.min(highlightSequence.length, wheelPrizes.value.length);
  if (isRolling.value || !prizeCount) return;
  isRolling.value = true;

  let resultOrderIndex = Math.floor(Math.random() * prizeCount);
  let raffleFlags = { isLock: false, isEmpty: false, awardName: '' };

  try {
    const resp = await api.doRaffle({
      activityId: currentActivityId.value,
    });

    raffleFlags = {
      isLock: !!resp?.isLock,
      isEmpty: !!resp?.isEmpty,
      awardName: resp?.awardName || '',
    };

    const awardId = resp?.awardId;
    const foundIndex = wheelPrizes.value.findIndex((p) => String(p.id) === String(awardId));
    if (foundIndex >= 0) {
      resultOrderIndex = foundIndex % prizeCount;
    }
  } catch (error) {
    isRolling.value = false;
    showErrorModal(error?.message || '抽奖失败');
    return;
  }

  let pointer = highlightSequence.indexOf(highlightIndex.value);
  if (pointer === -1) pointer = 0;

  const seqLength = highlightSequence.length;
  const extraSteps = (resultOrderIndex - pointer + seqLength) % seqLength;
  const totalSteps = seqLength * 3 + extraSteps;
  let steps = 0;

  const tick = () => {
    pointer = (pointer + 1) % seqLength;
    highlightIndex.value = highlightSequence[pointer];
    steps += 1;

    if (steps < totalSteps) {
      const delay = Math.min(80 + steps * 8, 260);
      rollingTimer = setTimeout(tick, delay);
      return;
    }

    isRolling.value = false;

    const prize = wheelPrizes.value[resultOrderIndex] || { label: '敬请期待', rate: 0 };
    const finalName = raffleFlags.awardName || prize.label;

    addHistoryRecord({
      id: Date.now(),
      user: '你',
      prize: finalName,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
    });

    addPersonalRecord({
      id: Date.now(),
      prize: finalName,
      time: formatDateTime(new Date()),
    });

      luckValue.value = Math.min(luckGoal.value || 0, luckValue.value + 12);

    pauseTimer = setTimeout(async () => {
      if (raffleFlags.isLock || raffleFlags.isEmpty) {
        showWarnModal({
          isLock: raffleFlags.isLock,
          isEmpty: raffleFlags.isEmpty,
          awardName: finalName,
        });
      } else {
        rewardModal.value = {
          visible: true,
          title: '🎉 抽奖成功，恭喜获得奖励 🎉',
          rewards: [finalName],
        };
        createConfetti();
      }

      const luckBonus = Math.floor(Math.random() * 10) + 1;
      await addFortuneLuck(luckBonus);

      highlightIndex.value = 4;
      await fetchActivityAccountData(currentActivityId.value);
      await fetchActivityLuckData(currentActivityId.value);
      await fetchUserAwardData(currentActivityId.value);
      await fetchActivityAwardData(currentActivityId.value);

      isRolling.value = false;
    }, 500);

  };

  rollingTimer = setTimeout(tick, 80);
};

onBeforeUnmount(() => {
  if (rollingTimer) {
    clearTimeout(rollingTimer);
  }
  if (historyTimer) {
    clearInterval(historyTimer);
  }
  if (pauseTimer) {
    clearTimeout(pauseTimer);
  }
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer);
  }
});

const scheduleNextHistory = () => {
  const delay = 1000 + Math.random() * 9000;

  historyTimer = setTimeout(() => {
    if (wheelPrizes.value.length) {
      const randomPrize = wheelPrizes.value[Math.floor(Math.random() * wheelPrizes.value.length)];
      addHistoryRecord({
        id: Date.now(),
        user: randomName(),
        prize: randomPrize.label,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
      });
    }

    scheduleNextHistory();
  }, delay);
};

onMounted(() => {
  if (!currentUserId.value) {
    router.push('/login');
    return;
  }
  luckValue.value = 48;
  scheduleNextHistory();
  autoRefreshTimer = setInterval(refreshAllData, 10000);
});

const handleLogout = () => {
  localStorage.removeItem('luckydraw_user');
  localStorage.removeItem('luckydraw_token');
  router.push('/login');
};

const openActivityModal = async () => {
  if (!activityModal.items.length) {
    await fetchActivityList();
  }
  activityModal.visible = true;
};

const closeActivityModal = () => {
  activityModal.visible = false;
};

const jumpActivity = (activityId) => {
  if (!activityId) return;
  activityModal.visible = false;
  router.push(`/luckydraw/${activityId}`);
};

const openRechargeModal = async () => {
  await fetchRechargeList(currentActivityId.value);
  selectedRecharge.value = rechargeModal.items?.[0] || null;
  paySelection.value = '微信支付';
  rechargeModal.visible = true;
};

const closeRechargeModal = () => {
  rechargeModal.visible = false;
};

const askRecharge = (item, channel) => {
  if (!item) return;
  rechargeConfirm.item = item;
  rechargeConfirm.channel = channel;
  rechargeConfirm.visible = true;
};

const submitRecharge = async () => {
  if (!rechargeConfirm.item?.tradeId) return;
  try {
    await api.doRecharge({
      activityId: currentActivityId.value,
      tradeId: rechargeConfirm.item.tradeId,
    });
    const luckBonus = Math.floor(Math.random() * 10) + 1;
    await addFortuneLuck(luckBonus);
    rewardModal.value = {
      visible: true,
      title: '🎉 充值发起成功 🎉',
      rewards: [
        `通过 ${rechargeConfirm.channel} 充值了：${rechargeConfirm.item.tradeName || rechargeConfirm.item.tradeValue || ''}`,
      ],
    };
    createConfetti();
    await fetchActivityAccountData(currentActivityId.value);
    await fetchActivityLuckData(currentActivityId.value);
    rechargeConfirm.visible = false;
    rechargeModal.visible = false;
  } catch (error) {
    showErrorModal(error?.message || '充值失败');
  }
};

const closeRechargeConfirm = () => {
  rechargeConfirm.visible = false;
  rechargeConfirm.item = null;
  rechargeConfirm.channel = '';
};
</script>

<style scoped>
:global(body) {
  margin: 0;
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.65), rgba(210, 220, 255, 0.4) 40%, rgba(185, 200, 255, 0.25) 70%, rgba(150, 170, 240, 0.18)),
    linear-gradient(135deg, #eef2ff 0%, #dee6ff 50%, #ccd6ff 100%);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  overflow-y: auto !important;
  overflow-x: hidden;
}

:global(html) {
  overflow-y: auto !important;
  overflow-x: hidden;
}

.luckydraw-page button:focus,
.luckydraw-page button:focus-visible {
  outline: none;
}

.luckydraw-page {
  position: relative;
  min-height: 100vh;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0rem 2.5rem 3rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  color: #1f2a44;
}

.activity-banner{
  position:relative;
  width:100vw;
  margin-left:calc(50% - 50vw);
  overflow:hidden;
  padding:0.55rem 0;
  background: rgba(124, 2, 185, 0.069);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
}

.banner-track{
  display:flex;
  width:max-content;
  white-space:nowrap;
  will-change:transform;
  animation:banner-marquee 24s linear infinite;
}

.banner-text{
  flex:0 0 auto;
  font-weight:800;
  color:#8a5a00;
  letter-spacing:0.02em;
  padding-right:3rem;
}

@keyframes banner-marquee{
  0%{transform:translateX(100%);}
  100%{transform:translateX(-100%);}
}

.page-header {
  text-align: center;
  color: #111b2b;
}

.top-area{
  position:relative;
  width:100vw;
  margin-left:calc(50% - 50vw);
}

.top-bar{
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap:12px;
  padding:0.6rem 0 0.2rem;
}

.page-brand{
  font-size:2.6rem;
  font-weight:1000;
  margin-left: 1.0rem;
  letter-spacing:0.04em;
  line-height:1.05;
  user-select:none;
  background:linear-gradient(135deg, #0f172a 0%, #2b3a67 45%, #4f6bff 100%);
  -webkit-background-clip:text;
  background-clip:text;
  color:transparent;
  text-shadow:0 14px 26px rgba(79, 107, 255, 0.16);
}

.page-actions{
  display:flex;
  align-items:center;
  gap:8px;
  margin-left:auto;
  margin-right: 1.0rem;
}

.action-btn {
  border: none;
  border-radius: 10px;
  padding: 0.5rem 0.75rem;
  background: linear-gradient(135deg, #f66b6b, #f88b4f);
  color: #fff;
  font-weight: 750;
  box-shadow: 0 10px 18px rgba(248, 107, 107, 0.25);
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, filter 0.16s ease;
}

.action-btn:hover{
  transform: translateY(-2px);
  background: linear-gradient(135deg, #ff7a7a, #ff9a5f);
  box-shadow: 0 14px 26px rgba(248, 107, 107, 0.38);
  filter: saturate(1.06);
}

.action-btn:active{
  transform: translateY(0);
  background: linear-gradient(135deg, #f45f5f, #f58449);
  box-shadow: 0 8px 16px rgba(248, 107, 107, 0.24);
  filter: saturate(1.02);
}

.confetti-canvas {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 1200;
}

.page-header h1 {
  margin: 0 0 0.3em 0;
  font-size: 5rem;
  letter-spacing: 0.02em;
  font-weight: 800;
}

.content-grid {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.user-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1.5rem;
}

.user-section > * {
  display: flex;
  flex-direction: column;
}

.user-panel,
.behavior-panel,
.convert-panel {
  display: flex;
  flex-direction: column;
}

.user-panel ul {
  list-style: none;
  padding: 0;
  margin: 1rem 0 0;
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
  flex: 1;
  justify-content: space-between;
}

.user-panel li {
  display: flex;
  justify-content: space-between;
  padding: 0.35rem 0;
  font-size: 0.95rem;
  color: #353b4a;
  border-bottom: 1px dashed rgba(0, 0, 0, 0.06);
}

.user-panel li:last-child {
  border-bottom: none;
}

.user-panel strong {
  font-weight: 800;
  color: #1a2333;
}

.behavior-panel .panel-title, .convert-panel .panel-title {
  text-align: center;
  margin-bottom: 1rem;
}

.card-grid,
.convert-grid,
.behavior-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(140px, 1fr));
  grid-auto-rows: 1fr;
  gap: 0.6rem;
}

.frost-card {
  background: rgba(255, 255, 255, 0.25);
  border-radius: 16px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.behavior-card:hover,
.convert-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

.convert-card .convert-name, .behavior-name {
  text-align: center;
  font-size: 0.92rem;
  font-weight: 600;
}

.card-btn {
  border: none;
  border-radius: 16px;
  padding: 0.6rem 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.convert-btn {
  background: linear-gradient(135deg, #4f6bff, #7b8dff);
  color: #fff;
  box-shadow: 0 8px 18px rgba(79, 107, 255, 0.32);
  outline: none;
}

.convert-btn:hover {
  transform: translateY(-2px);
  background: linear-gradient(135deg, #5f7cff, #8a9bff);
  box-shadow: 0 10px 22px rgba(79, 107, 255, 0.42);
}

.convert-btn:active {
  transform: translateY(0);
  box-shadow: 0 4px 12px rgba(79, 107, 255, 0.30);
  outline: none;
}

.behavior-btn {
  background: linear-gradient(135deg, #ffcc33, #ff8f1f);
  color: #fff;
  box-shadow: 0 8px 18px rgba(255, 159, 45, 0.32);
  outline: none;
}

.behavior-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  background: linear-gradient(135deg, #ffd34d, #ff9b33);
  box-shadow: 0 10px 22px rgba(255, 159, 45, 0.42);
}

.behavior-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 4px 12px rgba(255, 159, 45, 0.30);
}

.behavior-btn:disabled,
.behavior-btn.done {
  background: #9ba3b5;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
  outline: none;
}

.user-panel h2,
.behavior-panel h2,
.convert-panel h2 {
  margin: 0 0 1rem;
  text-align: center;
}

.behavior-panel .behavior-list {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  flex: 1;
  justify-content: space-between;
  width: 100%;
}

.core-section {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) minmax(360px, 480px) minmax(220px, 1fr);
  gap: 1.5rem;
  align-items: start;
}

.history-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  min-height: 340px;
  padding: 0 0.5rem;
  min-width: 260px;
  flex: 0 0 260px;
}

.history-list .panel-title {
  display: flex;
  font-size: 1.3rem;
  justify-content: center;
  width: 100%;
  text-align: center;
}

.history-list ul {
  list-style: none;
  padding: 0;
  margin: 1rem 0 0;
  overflow-y: auto;
  max-height: 360px;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  text-align: center;
  width: 100%;
}

.history-list li {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
  font-size: 0.9rem;
  color: #3d4f73;
}

.history-list.personal {
  align-items: flex-start;
}

.history-list.personal ul {
  text-align: left;
}

.history-list.personal li {
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
}

.history-list.personal li strong {
  flex: 1;
}

.history-list.personal li span {
  min-width: 180px;
  text-align: right;
  font-family: monospace;
}

.lottery-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1rem;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 120px);
  grid-template-rows: repeat(3, 120px);
  gap: 14px;
  padding: 1rem;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  box-shadow: 0 14px 28px rgba(20, 30, 60, 0.12);
}

.grid-item {
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #1f2a44;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.7), rgba(255, 255, 255, 0.4));
  box-shadow: 0 20px 32px rgba(20, 32, 58, 0.14);
  backdrop-filter: blur(16px);
}

.grid-item.active {
  box-shadow: 0 0 0 4px #ffe189, 0 12px 24px rgba(255, 225, 137, 0.5);
  transform: scale(1.06);
}

.grid-item.button {
  background: transparent;
  box-shadow: none;
}

.grid-button {
  border: none;
  width: 100%;
  height: 100%;
  border-radius: 20px;
  background: linear-gradient(140deg, #ff6fb1, #8b5cff);
  color: #fff;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
  box-shadow: 0 14px 26px rgba(139, 92, 255, 0.45);
}

.grid-button:disabled {
  opacity: 0.9;
  cursor: not-allowed;
  background: linear-gradient(140deg, #c2c8d5, #9ba3b5);
  box-shadow: none;
}

.grid-button:not(:disabled):hover {
  background: linear-gradient(140deg, #ff82be, #9a6dff);
  box-shadow: 0 18px 32px rgba(139, 92, 255, 0.55);
  transform: translateY(-2px);
}

.prize-card {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.6rem;
  text-align: center;
  overflow: visible;
  border-radius: 18px;
}

.prize-rate {
  position: absolute;
  top: 0.72rem;
  right: 0.2rem;
  background: linear-gradient(145deg, #ff6565, #ff8585);
  color: #fff;
  font-size: 0.56rem;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  pointer-events: none;
}

.lock-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.6rem;
  text-align: center;
  background: linear-gradient(165deg, rgba(25, 34, 56, 0.78), rgba(25, 34, 56, 0.6));
  color: #fdfdfd;
  font-weight: 700;
  border-radius: 20px;
  backdrop-filter: blur(1px);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.05);
  z-index: 1;
}

.prize-name {
  font-size: 0.95rem;
  text-align: center;
  padding: 0 0.5rem;
  font-weight: 700;
}

.behavior-list {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  width: 100%;
}

.behavior-list button {
  width: 100%;
  padding: 0.85rem 1rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: #2f3545;
  border-radius: 16px;

  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);

  border: none;
  cursor: pointer;

  transition: transform 0.18s ease, box-shadow 0.18s ease;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
}

.behavior-list button:not(.done):hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.15);
}

.behavior-list button:not(.done):active {
  transform: translateY(-1px);
}

.behavior-list button.done {
  background: rgba(200, 200, 200, 0.4);
  color: #777;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.bottom-area {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  padding-top: 0.5rem;
}

.progress-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.progress-bar {
  width: 100%;
  height: 18px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.7), rgba(232, 238, 250, 0.8));
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 14px 28px rgba(20, 30, 60, 0.12);
  overflow: visible;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff4b4b, #ff914d);
  border-radius: inherit;
  transition: width 0.6s ease;
  box-shadow: 0 10px 20px rgba(255, 75, 75, 0.35);
}

.progress-marks {
  position: absolute;
  inset: 0;
  pointer-events: auto;
}

.progress-mark {
  position: absolute;
  top: -34px;
  transform: translateX(-50%);
  text-align: center;
  color: #2f3b57;
  font-size: 0.72rem;
  font-weight: 700;
  min-width: 28px;
  cursor: pointer;
}

.progress-mark .mark-label {
  display: inline-block;
  padding: 4px 10px;
  margin-bottom: 6px;
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  cursor: pointer;
  position: relative;
}

.progress-mark:hover .mark-label {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.16);
}

.progress-mark .mark-label::after {
  content: attr(data-tip);
  position: absolute;
  left: 50%;
  bottom: calc(100% + 8px);
  transform: translateX(-50%) translateY(4px);
  white-space: nowrap;
  background: rgba(31, 42, 68, 0.92);
  color: #fff;
  padding: 6px 10px;
  border-radius: 10px;
  font-size: 0.72rem;
  font-weight: 700;
  opacity: 0;
  pointer-events: none;
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.2);
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.progress-mark .mark-label::before {
  content: "";
  position: absolute;
  left: 50%;
  bottom: calc(100% + 2px);
  transform: translateX(-50%);
  width: 8px;
  height: 8px;
  background: rgba(31, 42, 68, 0.92);
  transform-origin: center;
  clip-path: polygon(50% 0%, 0% 100%, 100% 100%);
  opacity: 0;
  transition: opacity 0.15s ease;
}

.progress-mark:hover .mark-label::after,
.progress-mark:hover .mark-label::before {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.progress-mark .mark-line {
  display: block;
  width: 2px;
  height: 30px;
  margin: 0 auto;
  background: linear-gradient(180deg, rgba(79, 107, 255, 0.75), rgba(123, 141, 255, 0.18));
  border-radius: 2px;
}

.reward-modal-overlay {
  position: fixed;
  inset: 0;
  background:radial-gradient(circle at 50% 40%,rgba(68,230,160,0.14),rgba(0,0,0,0.62) 62%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.reward-modal {
  width: min(420px, calc(100vw - 36px));
  background:linear-gradient(165deg,rgba(255,255,255,0.98),rgba(255,255,255,0.92));
  border-radius: 20px;
  padding: 1.25rem 1.25rem 1.1rem;
  box-shadow:
    0 22px 50px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.55) inset;
  position: relative;
  text-align: center;
  transform: translateY(4px);
}

.reward-modal::before {
  content: "";
  position: absolute;
  inset: -1px;
  border-radius: 22px;
  background: linear-gradient(135deg, rgba(58, 214, 145, 0.55), rgba(46, 179, 128, 0.22), rgba(120, 160, 255, 0.18));
  opacity:0.32;
  filter:blur(18px);
  z-index: -1;
}

.reward-modal::after {
  content: none !important;
  position: absolute;
  top: -64px;
  left: 50%;
  transform: translateX(-50%);
  width: 132px;
  height: 132px;
  background: radial-gradient(circle at 40% 35%, rgba(255, 255, 255, 0.95), rgba(255, 214, 102, 0.55), rgba(255, 160, 70, 0.15) 72%);
  border-radius: 999px;
  filter: blur(2px);
  opacity: 0.85;
  pointer-events: none;
}

.reward-modal h3 {
  margin: 0 0 0.85rem;
  color: #0f172a;
  font-weight: 900;
  letter-spacing: 0.02em;
  font-size: 1.5rem;
}

.reward-modal ul {
  list-style: none;
  padding: 0;
  margin: 0 0 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
  color: #0f172a;
  font-weight: bolder;
}

.reward-modal li {
  font-weight: 700;
}

.reward-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0.55rem 0.95rem;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(58, 214, 145, 0.18), rgba(255, 255, 255, 0.35));
  border: 1px solid rgba(46, 179, 128, 0.28);
  color: #1f2a44;
  box-shadow:
    0 10px 22px rgba(46, 179, 128, 0.14),
    0 0 0 1px rgba(255, 255, 255, 0.35) inset;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transform: translateY(0);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.reward-pill::before {
  content: "🎁";
  font-size: 0.95rem;
  transform: translateY(-1px);
}

.reward-modal li:hover .reward-pill {
  transform: translateY(-2px);
  box-shadow:
    0 14px 28px rgba(46, 179, 128, 0.20),
    0 0 0 1px rgba(255, 255, 255, 0.35) inset;
}

.modal-close {
  border: none;
  width: 100%;
  padding: 0.75rem 1.1rem;
  border-radius: 14px;
  font-weight: 900;
  letter-spacing: 0.02em;
  color: #ffffff;
  cursor: pointer;
  background: linear-gradient(135deg, #30c97c, #2aa56b);
  box-shadow: 0 14px 26px rgba(46, 179, 128, 0.35);
  transition: transform 0.18s ease, box-shadow 0.18s ease, filter 0.18s ease;
}

.modal-close:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 34px rgba(46, 179, 128, 0.45);
  filter: saturate(1.05);
}

.modal-close:active {
  transform: translateY(0);
  box-shadow: 0 10px 20px rgba(46, 179, 128, 0.30);
}

.fade-enter-active {
  animation: reward-pop 0.22s ease-out both;
}
.fade-leave-active {
  transition: opacity 0.16s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes reward-pop {
  0% {
    opacity: 0;
    transform: translateY(10px) scale(0.98);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.confetti-layer {
  contain: layout paint;
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  transform: translateZ(0);
  will-change: transform;
}

.confetti-piece {
  position: absolute;
  top: -12px;
  width: 12px;
  height: 18px;
  border-radius: 4px;
  animation: confetti-fall linear forwards;
  opacity: 0.95;
  will-change: transform, opacity;
  transform: translate3d(0, 0, 0) rotate(0deg);
  backface-visibility: hidden;
}

@keyframes confetti-fall {
  0% {
    transform: translate3d(0, 0, 0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translate3d(0, 160vh, 0) rotate(320deg);
    opacity: 0;
  }
}

/* 中奖展示 */
.raffle-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 998;
  backdrop-filter: blur(3px);
}

.raffle-modal {
  position: relative;
  min-width: 320px;
  padding: 1.6rem 1.8rem;
  border-radius: 20px;
  background: radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.95), rgba(230, 240, 255, 0.85));
  box-shadow: 0 18px 32px rgba(0, 0, 0, 0.18);
  text-align: center;
  overflow: hidden;
}

.raffle-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 50% 30%, rgba(255, 212, 99, 0.6), rgba(255, 158, 88, 0.25), transparent 65%);
  filter: blur(24px);
  z-index: 0;
}

.raffle-modal h3 {
  position: relative;
  margin: 0 0 0.7rem;
  color: #1f2a44;
  z-index: 1;
}

.raffle-prize {
  position: relative;
  margin: 0 0 1.1rem;
  font-size: 1.2rem;
  font-weight: 800;
  color: #ff6b6b;
  z-index: 1;
}

.raffle-close {
  position: relative;
  background: linear-gradient(135deg, #ffb347, #ff6b6b);
  box-shadow: 0 10px 24px rgba(255, 107, 107, 0.4);
  z-index: 1;
}


/* 错误弹窗 */
.error-modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 50% 40%, rgba(248, 113, 113, 0.16), rgba(0, 0, 0, 0.62) 62%);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  z-index: 2000;
}

.error-modal {
  position: relative;
  overflow: hidden;

  width: min(420px, calc(100vw - 36px));
  background: linear-gradient(165deg, rgba(255, 255, 255, 0.98), rgba(255, 255, 255, 0.92));
  border-radius: 20px;
  padding: 1.2rem 1.4rem;
  min-width: 240px;

  box-shadow:
    0 22px 50px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.55) inset;

  text-align: center;
  font-weight: 800;
  color: #b91c1c;

  transform: translateY(4px);
}

.error-modal::before {
  content: "";
  position: absolute;
  inset: -1px;
  border-radius: 22px;
  background: linear-gradient(
    135deg,
    rgba(248, 113, 113, 0.55),
    rgba(239, 68, 68, 0.22),
    rgba(255, 186, 186, 0.18)
  );
  opacity: 0.34;
  filter: blur(18px);
  z-index: -1;
}

.error-modal::after {
  content: none !important;
  position: absolute;
  top: -64px;
  left: 50%;
  transform: translateX(-50%);
  width: 132px;
  height: 132px;
  border-radius: 999px;
  background: radial-gradient(
    circle at 40% 35%,
    rgba(255, 255, 255, 0.95),
    rgba(248, 113, 113, 0.58),
    rgba(239, 68, 68, 0.16) 72%
  );
  filter: blur(2px);
  opacity: 0.9;
  pointer-events: none;
}

.error-modal.shaking {
  animation: error-shake 0.4s ease-in-out 0s 1;
}

@keyframes error-shake {
  0%, 100% { transform: translateX(0) translateY(4px); }
  20% { transform: translateX(-12px) translateY(4px); }
  40% { transform: translateX(12px) translateY(4px); }
  60% { transform: translateX(-8px) translateY(4px); }
  80% { transform: translateX(8px) translateY(4px); }
}

.error-close {
  margin-top: 0.8rem;
  border: none;
  width: 100%;
  padding: 0.75rem 1.1rem;
  border-radius: 14px;
  font-weight: 900;
  letter-spacing: 0.02em;
  color: #ffffff;
  cursor: pointer;

  background: linear-gradient(135deg, #ef4444, #b91c1c);
  box-shadow: 0 14px 26px rgba(239, 68, 68, 0.35);

  transition: transform 0.18s ease, box-shadow 0.18s ease, filter 0.18s ease;
}

.error-close:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 34px rgba(239, 68, 68, 0.45);
  filter: saturate(1.05);
}

.error-close:active {
  transform: translateY(0);
  box-shadow: 0 10px 20px rgba(239, 68, 68, 0.30);
}

/* ============ warn 弹窗（黄色系） ============ */
.warn-modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 50% 40%, rgba(251, 191, 36, 0.18), rgba(0, 0, 0, 0.62) 62%);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  z-index: 1001;
}

.warn-modal {
  position: relative;
  overflow: hidden;

  width: min(420px, calc(100vw - 36px));
  background: linear-gradient(165deg, rgba(255, 255, 255, 0.98), rgba(255, 255, 255, 0.92));
  border-radius: 20px;
  padding: 1.2rem 1.4rem;
  min-width: 240px;

  box-shadow:
    0 22px 50px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.55) inset;

  text-align: center;
  font-weight: 800;
  color: #92400e;

  transform: translateY(4px);
}

.warn-modal h3 {
  margin: 0 0 0.65rem;
  font-weight: 900;
  letter-spacing: 0.02em;
  color: #7c2d12;
}

.warn-modal p {
  margin: 0.35rem 0;
  font-weight: 800;
}

.warn-modal::before {
  content: "";
  position: absolute;
  inset: -1px;
  border-radius: 22px;
  background: linear-gradient(
    135deg,
    rgba(251, 191, 36, 0.55),
    rgba(245, 158, 11, 0.22),
    rgba(255, 237, 185, 0.18)
  );
  opacity: 0.34;
  filter: blur(18px);
  z-index: -1;
}

/* 不要顶部球体 */
.warn-modal::after {
  content: none !important;
}

.warn-modal.shaking {
  animation: warn-shake 0.4s ease-in-out 0s 1;
}

@keyframes warn-shake {
  0%, 100% { transform: translateX(0) translateY(4px); }
  20% { transform: translateX(-12px) translateY(4px); }
  40% { transform: translateX(12px) translateY(4px); }
  60% { transform: translateX(-8px) translateY(4px); }
  80% { transform: translateX(8px) translateY(4px); }
}

.warn-close {
  margin-top: 0.8rem;
  border: none;
  width: 100%;
  padding: 0.75rem 1.1rem;
  border-radius: 14px;
  font-weight: 900;
  letter-spacing: 0.02em;
  color: #ffffff;
  cursor: pointer;

  background: linear-gradient(135deg, #f59e0b, #d97706);
  box-shadow: 0 14px 26px rgba(245, 158, 11, 0.35);

  transition: transform 0.18s ease, box-shadow 0.18s ease, filter 0.18s ease;
}

.warn-close:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 34px rgba(245, 158, 11, 0.45);
  filter: saturate(1.05);
}

.warn-close:active {
  transform: translateY(0);
  box-shadow: 0 10px 20px rgba(245, 158, 11, 0.30);
}

/* 充值弹窗 */
.recharge-modal-overlay{
  position:fixed;
  inset:0;
  display:flex;
  align-items:center;
  justify-content:center;
  background: radial-gradient(circle at 50% 40%, rgba(46,179,128,0.16), rgba(0,0,0,0.62) 62%);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  z-index: 1001;
}

.recharge-modal{
  width:min(1080px, calc(100vw - 40px));
  max-height: 78vh;
  overflow:hidden;
  background: linear-gradient(155deg, rgba(255,255,255,0.98), rgba(242,246,255,0.94));
  border-radius: 20px;
  padding: 1.4rem 1.8rem 1.4rem;
  box-shadow:
    0 22px 50px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.55) inset;
  text-align:center;
  color:#0f172a;
}

.recharge-modal h3{
  margin: 0 0 0.35rem;
  font-weight: 900;
  font-size: 1.3rem;
}

.recharge-sub{
  margin: 0 0 0.8rem;
  color: #4b5563;
  font-weight: 600;
}

.recharge-list{
  display:flex;
  gap:0.8rem;
  padding:0.6rem 1.2rem;
  max-height: 34vh;
  overflow-x:auto;
  overflow-y:hidden;
  justify-content:space-between;
}

.recharge-card{
  min-width: 240px;
  background: rgba(255,255,255,0.96);
  border-radius: 16px;
  padding: 1.1rem 0.9rem;
  box-shadow: none;
  border: 1px solid rgba(0,0,0,0.05);
  display:flex;
  flex-direction:column;
  gap:0.8rem;
  align-items:center;
  transition: transform 0.16s ease, box-shadow 0.16s ease, border-color 0.16s ease;
}

.recharge-value{
  display:flex;
  flex-direction:column;
  gap:0.4rem;
  width:100%;
  align-items:center;
}

.recharge-money-plain{
  font-size: 1.35rem;
  font-weight: 900;
  color: #1f2a44;
}

.recharge-point-plain{
  font-size: 1rem;
  font-weight: 800;
  color: #1f2a44;
}

.recharge-actions{
  display:none;
}

.pill-btn{
  border:none;
  border-radius: 12px;
  padding: 0.5rem 0.75rem;
  color:#fff;
  font-weight:800;
  cursor:pointer;
  background: linear-gradient(135deg, #30c97c, #2aa56b);
  box-shadow: 0 14px 26px rgba(46, 179, 128, 0.35);
  transition: transform 0.18s ease, box-shadow 0.18s ease, filter 0.18s ease;
}

.pill-btn.ghost.danger{
  background: linear-gradient(135deg, #ef4444, #b91c1c);
  box-shadow: 0 14px 26px rgba(239, 68, 68, 0.35);
  color:#fff;
  transition: transform 0.18s ease, box-shadow 0.18s ease, filter 0.18s ease;
}

.pill-btn:hover{
  transform: translateY(-2px);
  box-shadow: 0 18px 34px rgba(46, 179, 128, 0.45);
  filter: saturate(1.05);
}

.pill-btn:active{
  transform: translateY(0);
  box-shadow: 0 10px 20px rgba(46, 179, 128, 0.30);
  filter: saturate(1.02);
}

.pill-btn.ghost.danger:hover{
  transform: translateY(-2px);
  box-shadow: 0 18px 34px rgba(239, 68, 68, 0.45);
  filter: saturate(1.05);
}

.pill-btn.ghost.danger:active{
  transform: translateY(0);
  box-shadow: 0 10px 20px rgba(239, 68, 68, 0.30);
  filter: saturate(1.02);
}

.recharge-card.active{
  border-color: rgba(79,107,255,0.85);
  box-shadow: 0 16px 28px rgba(79,107,255,0.25);
  border-width: 2px;
  transform: translateY(-2px);
}

.pay-row{
  display:flex;
  gap:0.8rem;
  justify-content:center;
  align-items:center;
  margin: 0.8rem 0 0.4rem;
  width:100%;
}

.pay-btn{
  border:none;
  background: rgba(255,255,255,0.92);
  border-radius: 14px;
  padding: 0.6rem 0.8rem;
  box-shadow: 0 10px 20px rgba(0,0,0,0.12);
  cursor:pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease;
  border: 2px solid transparent;
}

.pay-btn:hover{
  transform: translateY(-2px);
  box-shadow: 0 14px 26px rgba(0,0,0,0.16);
}

.pay-btn img{
  width: 96px;
  height: 40px;
  object-fit: contain;
  display:block;
}

.pay-btn.active{
  border-color: rgba(79,107,255,0.9);
  box-shadow: 0 14px 26px rgba(79,107,255,0.25);
}

.pay-submit-row{
  display:flex;
  justify-content:center;
  gap: 0.75rem;
  margin: 2.0rem 0 1rem;
}

.recharge-modal .modal-close{
  width: auto;
  min-width: 140px;
  margin: 0 auto;
  background: linear-gradient(135deg, #f66b6b, #f88b4f);
}

.pill-btn.ghost{
  background: rgba(255,255,255,0.75);
  color: #1f2a44;
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}

.recharge-confirm-overlay{
  position:fixed;
  inset:0;
  display:flex;
  align-items:center;
  justify-content:center;
  background: rgba(0,0,0,0.45);
  z-index: 1200;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.recharge-confirm{
  width: min(380px, 90vw);
  background: linear-gradient(165deg, rgba(255,255,255,0.98), rgba(255,255,255,0.92));
  border-radius: 18px;
  padding: 1.1rem 1rem 1rem;
  text-align:center;
  box-shadow: 0 18px 32px rgba(0,0,0,0.22);
}

.recharge-confirm h3{
  margin:0 0 0.4rem;
  font-weight: 900;
  font-size: 1.2rem;
}

.recharge-confirm p{
  margin:0 0 0.9rem;
  color:#1f2a44;
  font-weight: 700;
  line-height:1.5;
}

.confirm-actions{
  display:flex;
  gap:0.6rem;
  justify-content:center;
}
/* 活动列表弹窗 */
.activity-modal-overlay{
  position:fixed;
  inset:0;
  display:flex;
  align-items:center;
  justify-content:center;
  background: radial-gradient(circle at 50% 40%, rgba(79,107,255,0.14), rgba(0,0,0,0.62) 62%);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  z-index: 1001;
}

.activity-modal{
  width:min(520px, calc(100vw - 40px));
  max-height: 70vh;
  overflow:hidden;
  background: linear-gradient(165deg, rgba(255,255,255,0.98), rgba(255,255,255,0.92));
  border-radius: 20px;
  padding: 1.4rem 1.2rem 1.2rem;
  box-shadow:
    0 22px 50px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.55) inset;
  text-align:center;
  color:#0f172a;
}

.activity-modal h3{
  margin: 0 0 0.35rem;
  font-weight: 900;
  font-size: 1.3rem;
}

.activity-sub{
  margin: 0 0 0.8rem;
  color: #4b5563;
  font-weight: 600;
}

.activity-list{
  display:flex;
  flex-direction:column;
  gap:0.8rem;
  padding:0.4rem;
  max-height: 46vh;
  overflow-y:auto;
  margin-bottom: 1rem;
}

.activity-item{
  width:100%;
  text-align:left;
  border:none;
  border-radius:14px;
  padding:0.75rem 0.85rem;
  background:linear-gradient(135deg, rgba(79,107,255,0.14), rgba(123,141,255,0.10)),rgba(255,255,255,0.72);
  cursor:pointer;
  transition:transform 0.16s ease, box-shadow 0.16s ease, filter 0.16s ease;
}

.activity-item:hover{
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(79,107,255,0.25);
  filter: saturate(1.05);
}

.activity-item__name{
  font-weight: 800;
  font-size: 1.2rem;
  color: #1f2a44;
  margin-bottom: 0.25rem;
}

.activity-item__desc{
  color: #4b5563;
  font-size: 0.88rem;
  line-height: 1.4;
}
</style>
