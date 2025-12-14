let instance = null;
let loader = null;

const COLORS = [
  '#ff4d4f',
  '#ffa940',
  '#ffd666',
  '#73d13d',
  '#36cfc9',
  '#40a9ff',
  '#597ef7',
  '#9254de',
  '#f759ab',
  '#ffffff',
];

const loadConfettiLib = () => {
  if (loader) return loader;
  loader = new Promise((resolve, reject) => {
    if (window.confetti) {
      resolve(window.confetti);
      return;
    }
    const script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/canvas-confetti@1.9.3/dist/confetti.browser.min.js';
    script.async = true;
    script.onload = () => resolve(window.confetti);
    script.onerror = reject;
    document.head.appendChild(script);
  });
  return loader;
};

export const initConfetti = async (canvasEl) => {
  if (instance || !canvasEl) return instance;
  const lib = await loadConfettiLib().catch(() => null);
  if (!lib) return null;
  instance = lib.create(canvasEl, { resize: true, useWorker: true });
  return instance;
};

export const burstConfetti = async (canvasEl, origin = { x: 0.5, y: 0.35 }) => {
  const confetti = await initConfetti(canvasEl);
  if (!confetti) return;

  const clamp01 = (n) => Math.max(0, Math.min(1, n));
  const ox = clamp01(origin.x ?? 0.5);
  const oy = clamp01(origin.y ?? 0.35);

  // 简单的性能分级：低端设备或移动端降低粒子数量与持续时间
  const isLowPerf =
    (navigator.deviceMemory && navigator.deviceMemory <= 4) ||
    /iphone|ipad|android/i.test(navigator.userAgent);
  const scale = isLowPerf ? 0.6 : 1;

  const burst = (options) =>
    confetti({
      ...options,
      particleCount: Math.max(20, Math.round((options.particleCount || 80) * scale)),
      ticks: Math.round((options.ticks || 200) * (isLowPerf ? 0.85 : 1)),
      scalar: (options.scalar || 1) * (isLowPerf ? 0.9 : 1),
      origin: { x: ox, y: oy },
    });

  burst({
    particleCount: 180,
    startVelocity: 55,
    spread: 80,
    ticks: 280,
    decay: 0.9,
    gravity: 1.05,
    scalar: 1.35,
    colors: COLORS,
    shapes: ['square'],
  });

  burst({
    particleCount: 120,
    startVelocity: 38,
    spread: 120,
    ticks: 260,
    decay: 0.92,
    gravity: 1.2,
    scalar: 1.0,
    colors: COLORS,
    shapes: ['circle'],
  });
};

export const resetConfetti = () => {
  if (instance && typeof instance.reset === 'function') {
    instance.reset();
  }
};
