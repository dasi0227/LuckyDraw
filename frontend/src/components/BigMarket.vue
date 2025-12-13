<template>
  <div class="bigmarket-page">
    <div v-if="activityBannerText" class="activity-banner">
      <div class="banner-track">
        <span class="banner-text">{{ activityBannerText }}</span>
      </div>
    </div>

    <!-- Header -->
    <header class="page-header">
      <h1>Dasi 抽奖系统</h1>
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
            @click="showLuckDetail(mark)"
          >
            <span class="mark-label">{{ mark.label ?? mark.value }}</span>
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
          <h3>{{ '🎉 恭喜获得奖励 🎉' }}</h3>
          <ul>
            <li v-for="(text, idx) in rewardModal.rewards" :key="idx">
              <span class="reward-pill">{{ text }}</span>
            </li>
          </ul>
          <button class="modal-close" @click="closeRewardModal">OK</button>
        </div>
        <div class="confetti-layer">
          <span
            v-for="piece in confettiPieces"
            :key="piece.id"
            class="confetti-piece"
            :style="{
              left: piece.left,
              animationDelay: piece.delay,
              animationDuration: piece.duration,
              background: piece.color,
              transform: `rotate(${piece.rotate})`
            }"
          ></span>
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

  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { formatDateTime } from "../utils/utils.js";
import { namePool } from '../utils/name.js';
import api from '../request/api.js';

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
const confettiPieces = ref([]);
const warnModal = ref({ visible: false, title: '提示', lines: [], shake: false });
const errorModal = ref({ visible: false, message: '', shake: false });
const activityInfo = ref(null);
const behaviors = ref([]);

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
  if (!currentActivityId.value || !currentUserId.value) return;
  try {
    const resp = await api.doConvert({
      activityId: currentActivityId.value,
      userId: currentUserId.value,
      tradeId: item.id,
    });
    const desc = resp?.tradeDesc || '兑换成功';
    rewardModal.value = {
      visible: true,
      title: '🎉 兑换成功，恭喜获得奖励 🎉',
      rewards: [desc],
    };
    createConfetti();
    setTimeout(() => {
      fetchActivityAccountData(currentActivityId.value, currentUserId.value);
    }, 400);
  } catch (error) {
    showErrorModal(error?.message || '积分兑换失败');
  }
};

const createConfetti = () => {
  // 颜色
  const colors = ['#ff6b6b', '#feca57', '#1dd1a1', '#54a0ff', '#5f27cd'];
  // 总数量
  const TOTAL = 120;
  // 批次
  const BATCH = 40;
  // 最短掉落时长
  const MIN_DUR = 2.0;
  // 随机时长
  const DUR_RANGE = 2.0;
  // 清空时间
  const LIFETIME = 9000;

  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    return;
  }

  const base = Date.now();
  let created = 0;

  const pushBatch = () => {
    const n = Math.min(BATCH, TOTAL - created);
    const batch = Array.from({ length: n }).map((_, idx) => ({
      id: `c-${base}-${created + idx}`,
      left: `${Math.random() * 100}%`,
      delay: `${Math.random() * 0.25}s`,
      duration: `${MIN_DUR + Math.random() * DUR_RANGE}s`,
      color: colors[Math.floor(Math.random() * colors.length)],
      rotate: `${Math.random() * 360}deg`,
    }));

    confettiPieces.value = confettiPieces.value.concat(batch);
    created += n;

    if (created < TOTAL) {
      requestAnimationFrame(pushBatch);
    } else {
      window.clearTimeout(createConfetti._t);
      createConfetti._t = window.setTimeout(() => {
        confettiPieces.value = [];
      }, LIFETIME);
    }
  };

  confettiPieces.value = [];
  requestAnimationFrame(pushBatch);
};

const runBehavior = async (action) => {
  if (!currentActivityId.value || !currentUserId.value) return;
  try {
    const resp = await api.doBehavior({
      activityId: currentActivityId.value,
      userId: currentUserId.value,
      behaviorType: action.type || action.behaviorType,
    });
    const rewards = Array.isArray(resp?.rewardDescList) ? resp.rewardDescList : [];
    rewardModal.value = {
      visible: true,
      title: '🎉 互动成功，恭喜获得奖励 🎉',
      rewards: rewards.length ? rewards : ['感谢参与'],
    };
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
  confettiPieces.value = [];
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

const showLuckDetail = (mark) => {
  const detailText = mark?.detail || mark?.label || mark?.value || '暂无详情';
  window.alert(`阈值 ${mark?.value ?? ''}: ${detailText}`);
};

const route = useRoute();
const currentActivityId = ref(route.params.activityId || 'default');
const currentUserId = ref('wyw');

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

const fetchActivityAccountData = async (activityId, userId) => {
  if (!activityId || !userId) return;
  try {
    const account = await api.queryActivityAccount({ activityId, userId });

    Object.assign(userStats, {
      name: 'wyw',
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

const fetchActivityLuckData = async (activityId, userId) => {
  if (!activityId || !userId) return;
  try {
    const resp = await api.queryActivityLuck({ activityId, userId });
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

const fetchActivityBehaviorData = async (activityId, userId) => {
  if (!activityId || !userId) return;
  try {
    const resp = await api.queryActivityBehavior({ activityId, userId });
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

const fetchActivityAwardData = async (activityId, userId) => {
  if (!activityId) return;
  try {
    const awards = await api.queryActivityAward({ activityId, userId });
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

const fetchUserAwardData = async (activityId, userId) => {
  if (!activityId || !userId) return;
  try {
    const awards = await api.queryUserAward({ activityId, userId });
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

watch(
  () => route.params.activityId,
  async (newActivityId) => {
    currentActivityId.value = newActivityId;
    fetchActivityConvertData(currentActivityId.value);
    await fetchActivityAccountData(currentActivityId.value, currentUserId.value);
    await fetchActivityLuckData(currentActivityId.value, currentUserId.value);
    fetchUserAwardData(currentActivityId.value, currentUserId.value);
    fetchActivityBehaviorData(currentActivityId.value, currentUserId.value);
    fetchActivityAwardData(currentActivityId.value, currentUserId.value);
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

const startGridLottery = async () => {
  const prizeCount = Math.min(highlightSequence.length, wheelPrizes.value.length);
  if (isRolling.value || !prizeCount) return;
  isRolling.value = true;

  let resultOrderIndex = Math.floor(Math.random() * prizeCount);
  let raffleFlags = { isLock: false, isEmpty: false, awardName: '' };

  try {
    const resp = await api.doRaffle({
      activityId: currentActivityId.value,
      userId: currentUserId.value,
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

  if (raffleFlags.isLock) {
    isRolling.value = false;
    showErrorModal(`当前奖品没有解锁，请多多参与抽奖！\n获取兜底奖品：${raffleFlags.awardName || '--'}`);
    return;
  }

  if (raffleFlags.isEmpty) {
    isRolling.value = false;
    showErrorModal(`当前奖品已被抽完，请尽早参与活动！\n获取兜底奖品：${raffleFlags.awardName || '---'}`);
    return;
  }

  let pointer = highlightSequence.indexOf(highlightIndex.value);
  if (pointer === -1) pointer = 0;

  const seqLength = highlightSequence.length;
  const extraSteps = (resultOrderIndex - pointer + seqLength) % seqLength;
  const totalSteps = seqLength * 3 + extraSteps + 1;
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

    // ✅ 停止滚动
    isRolling.value = false;

    const prize = wheelPrizes.value[resultOrderIndex] || { label: '敬请期待', rate: 0 };
    const finalName = raffleFlags.awardName || prize.label;

    // ✅ 列表用后端返回的 awardName（兜底也能对上）
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

    highlightIndex.value = 4;
    fetchActivityAwardData(currentActivityId.value, currentUserId.value);
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
});

const scheduleNextHistory = () => {
  const delay = 1000 + Math.random() * 9000; // 1s ~ 10s

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

    scheduleNextHistory(); // 继续下一次
  }, delay);
};

onMounted(() => {
  luckValue.value = 48;
  scheduleNextHistory();
});
</script>

<style scoped>
:global(body) {
  margin: 0;
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.65), rgba(210, 220, 255, 0.4) 40%, rgba(185, 200, 255, 0.25) 70%, rgba(150, 170, 240, 0.18)),
    linear-gradient(135deg, #eef2ff 0%, #dee6ff 50%, #ccd6ff 100%);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.bigmarket-page button:focus,
.bigmarket-page button:focus-visible {
  outline: none;
}

.bigmarket-page {
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

.convert-card .convert-name,
.behavior-name {
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
}

.progress-mark:hover .mark-label {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.16);
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
  box-shadow: 0 6px 10px rgba(0, 0, 0, 0.18);
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
  z-index: 1001;
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
</style>
