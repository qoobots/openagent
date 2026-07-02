/* ============================================
   教育培训智能体 - 主 JS 模块
   API封装 · Alpine Stores · 工具函数
   ============================================ */

// === API 封装 ===
const api = {
  _token: localStorage.getItem('edu_token') || '',
  _refreshToken: localStorage.getItem('edu_refresh_token') || '',

  getToken() { return this._token; },
  setToken(access, refresh) {
    this._token = access;
    localStorage.setItem('edu_token', access);
    if (refresh) { this._refreshToken = refresh; localStorage.setItem('edu_refresh_token', refresh); }
  },
  clearToken() {
    this._token = ''; this._refreshToken = '';
    localStorage.removeItem('edu_token');
    localStorage.removeItem('edu_refresh_token');
  },

  headers() {
    const h = { 'Content-Type': 'application/json' };
    if (this._token) h['Authorization'] = 'Bearer ' + this._token;
    const stage = localStorage.getItem('edu_stage') || 'middle';
    h['X-Education-Stage'] = stage;
    h['X-Request-Id'] = crypto.randomUUID ? crypto.randomUUID() : Date.now().toString(36);
    return h;
  },

  async handleResponse(response) {
    if (response.ok) return response.json();
    if (response.status === 401) {
      const refreshed = await this.refreshToken();
      if (refreshed) return this.handleResponse(response); // retry
      this.clearToken();
      window.location.href = '/education-agent/login';
      throw new Error('登录已过期');
    }
    if (response.status === 403) { toast.error('权限不足'); throw new Error('权限不足'); }
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || `请求失败 (${response.status})`);
  },

  async refreshToken() {
    if (!this._refreshToken) return false;
    try {
      const res = await fetch('/education-agent/api/auth/refresh', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: this._refreshToken })
      });
      if (!res.ok) return false;
      const data = await res.json();
      this.setToken(data.data?.accessToken, data.data?.refreshToken);
      return true;
    } catch { return false; }
  },

  async get(url) { return this.handleResponse(await fetch('/education-agent' + url, { method: 'GET', headers: this.headers() })); },
  async post(url, body) { return this.handleResponse(await fetch('/education-agent' + url, { method: 'POST', headers: this.headers(), body: JSON.stringify(body) })); },
  async put(url, body) { return this.handleResponse(await fetch('/education-agent' + url, { method: 'PUT', headers: this.headers(), body: JSON.stringify(body) })); },
  async delete(url) { return this.handleResponse(await fetch('/education-agent' + url, { method: 'DELETE', headers: this.headers() })); },

  async stream(url, body, onMessage, onDone, onError) {
    try {
      const response = await fetch('/education-agent' + url, {
        method: 'POST', headers: this.headers(), body: JSON.stringify(body)
      });
      if (!response.ok) { const err = await response.json().catch(()=>({})); throw new Error(err.message || '请求失败'); }
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim();
            if (data === '[DONE]') { onDone && onDone(); return; }
            try { const parsed = JSON.parse(data); onMessage && onMessage(parsed); } catch { onMessage && onMessage({ content: data }); }
          }
        }
      }
      onDone && onDone();
    } catch (err) { onError && onError(err); }
  }
};

// === Toast 通知系统 ===
const toast = {
  _container: null,
  _getContainer() {
    if (!this._container) {
      this._container = document.createElement('div');
      this._container.className = 'toast-container';
      document.body.appendChild(this._container);
    }
    return this._container;
  },
  _show(message, type = 'info', duration = 3000) {
    const container = this._getContainer();
    const icons = { success: '✅', error: '❌', warning: '⚠️', info: 'ℹ️' };
    const el = document.createElement('div');
    el.className = `toast toast-${type}`;
    el.innerHTML = `<span>${icons[type] || ''}</span><span>${message}</span>`;
    container.appendChild(el);
    setTimeout(() => { el.classList.add('toast-out'); setTimeout(() => el.remove(), 300); }, duration);
  },
  success(msg) { this._show(msg, 'success'); },
  error(msg) { this._show(msg, 'error', 5000); },
  warning(msg) { this._show(msg, 'warning', 4000); },
  info(msg) { this._show(msg, 'info'); }
};

// === Alpine.js 初始化 ===
document.addEventListener('alpine:init', () => {
  // 用户状态 Store
  Alpine.store('user', {
    isLoggedIn: !!api.getToken(),
    info: JSON.parse(localStorage.getItem('edu_user') || '{}'),
    stage: localStorage.getItem('edu_stage') || 'middle',
    role: localStorage.getItem('edu_role') || 'student',

    login(data) {
      this.info = data;
      this.role = data.role || 'student';
      this.stage = data.stage || this.stage;
      this.isLoggedIn = true;
      localStorage.setItem('edu_user', JSON.stringify(data));
      localStorage.setItem('edu_stage', this.stage);
      localStorage.setItem('edu_role', this.role);
    },
    logout() {
      this.isLoggedIn = false; this.info = {};
      api.clearToken();
      localStorage.removeItem('edu_user');
      window.location.href = '/education-agent/login';
    },
    switchStage(newStage) { this.stage = newStage; },
    applyStage() {
      localStorage.setItem('edu_stage', this.stage);
      document.documentElement.setAttribute('data-stage', this.stage);
    },
    getStageName() {
      const names = { elementary:'小学', middle:'初中', high:'高中', university:'大学', vocational:'职业培训', adult:'成人学习' };
      return names[this.stage] || '初中';
    },
    getRoleName() {
      const names = { student:'学生', teacher:'教师', parent:'家长', admin:'管理员' };
      return names[this.role] || '学生';
    }
  });

  // UI 状态 Store
  Alpine.store('ui', {
    sidebarCollapsed: window.innerWidth < 1024,
    mobileSidebarOpen: false,
    toggleSidebar() { this.sidebarCollapsed = !this.sidebarCollapsed; },
    toggleMobileSidebar() { this.mobileSidebarOpen = !this.mobileSidebarOpen; }
  });

  // 通知 Store
  Alpine.store('notification', {
    count: 0, items: [],
    async fetchCount() {
      try { const res = await api.get('/api/notification/count'); this.count = res.data || 0; } catch { /* ignore */ }
    }
  });
});

// === 工具函数 ===
function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
}
function formatDuration(minutes) {
  if (minutes < 60) return `${minutes}分钟`;
  const h = Math.floor(minutes/60), m = minutes%60;
  return m > 0 ? `${h}小时${m}分钟` : `${h}小时`;
}
function debounce(fn, delay = 300) {
  let timer; return function(...args) { clearTimeout(timer); timer = setTimeout(() => fn.apply(this, args), delay); };
}
function throttle(fn, limit = 300) {
  let inThrottle; return function(...args) { if (!inThrottle) { fn.apply(this, args); inThrottle = true; setTimeout(() => inThrottle = false, limit); } };
}
async function copyToClipboard(text) {
  try { await navigator.clipboard.writeText(text); toast.success('已复制到剪贴板'); } catch { toast.error('复制失败'); }
}

// === 页面初始化 ===
document.addEventListener('DOMContentLoaded', () => {
  // 应用学段主题
  const stage = localStorage.getItem('edu_stage') || 'middle';
  document.documentElement.setAttribute('data-stage', stage);
  // 响应式监听
  window.addEventListener('resize', () => {
    if (window.innerWidth >= 1024) Alpine.store('ui').mobileSidebarOpen = false;
  });
  // 获取通知数量
  if (api.getToken()) Alpine.store('notification').fetchCount();
});
