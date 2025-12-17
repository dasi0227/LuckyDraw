<template>
  <div class="notfound-page">
    <div class="glass-card">
      <div class="badge">404 NOT FOUND</div>
      <h1>页面飞走了</h1>
      <p>没找到你要的页面 😭 回去抽个奖试试手气吧 🥳</p>
      <div class="actions">
        <router-link class="btn primary" to="/login">返回登录</router-link>
        <router-link class="btn ghost" to="/luckydraw/10001">前往抽奖</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
:global(body) {
  margin: 0;
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.65), rgba(210, 220, 255, 0.4) 40%, rgba(185, 200, 255, 0.25) 70%, rgba(150, 170, 240, 0.18)),
    linear-gradient(135deg, #eef2ff 0%, #dee6ff 50%, #ccd6ff 100%);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  overflow: hidden;
}

.notfound-page {
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

.glass-card {
  width: min(520px, 92vw);
  background: rgba(255, 255, 255, 0.22);
  border-radius: 22px;
  padding: 2.2rem 1.8rem;
  text-align: center;
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  box-shadow: 0 18px 32px rgba(0, 0, 0, 0.18);
  color: #1f2a44;
}

.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.5rem 1rem;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff6b6b, #ffa940);
  color: #fff;
  font-weight: 900;
  box-shadow: 0 12px 24px rgba(255, 107, 107, 0.35);
  letter-spacing: 0.08em;
}

h1 {
  margin: 1.5rem 0 1.5rem;
  font-size: 2rem;
  font-weight: 800;
}

p {
  margin: 0 0 1.8rem;
  color: #4b5563;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  text-decoration: none;
  padding: 0.8rem 1.2rem;
  border-radius: 14px;
  font-weight: 800;
  transition: transform 0.18s ease, box-shadow 0.18s ease, filter 0.18s ease;
}

.btn.primary {
  background: linear-gradient(135deg, #4f6bff, #7b8dff);
  color: #fff;
  box-shadow: 0 10px 22px rgba(79, 107, 255, 0.35);
}

.btn.ghost {
  background: linear-gradient(135deg, #30c97c, #2aa56b);
  color: #fff;
  border: none;
  box-shadow: 0 12px 22px rgba(46, 179, 128, 0.25);
}

.btn:hover {
  transform: translateY(-2px);
  filter: saturate(1.05);
}
</style>
