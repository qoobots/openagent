/* ============================================
   教育培训智能体 - 对话页 Alpine.js 组件
   SSE 流式输出 · 会话管理 · 消息渲染
   ============================================ */

function chatComponent() {
  return {
    sessionId: null,
    messages: [],
    inputText: '',
    isLoading: false,
    isStreaming: false,
    streamingContent: '',
    suggestedQuestions: [],
    references: [],
    sessions: [],
    showSessionList: false,
    relatedCourse: '',
    relatedKnowledge: 'auto',
    abortController: null,

    get welcomeMessage() {
      const stage = Alpine.store('user')?.stage || 'middle';
      const msgs = {
        elementary: '你好呀！🌟 我是你的AI学习小伙伴！今天想学什么呢？可以问我任何问题哦～',
        middle: '你好！我是你的AI学习助手 📚 有什么需要帮忙的吗？我可以帮你讲解知识点、做题练习。',
        high: '你好！我是你的AI辅导助手。可以帮你讲解知识点、分析题目、梳理考点。请问今天学什么？',
        university: '你好！我可以协助你进行学术探讨、论文分析和专业问题解答。请问需要什么帮助？',
        vocational: '你好！我可以帮你解决职业技能学习中的问题，提供实操指导和案例分析。',
        adult: '你好！我是你的AI学习助手。有什么想了解的？我会简洁高效地为你解答。'
      };
      return msgs[stage] || msgs.middle;
    },

    init() {
      this.sessionId = this.$refs.sessionId?.value || null;
      if (this.sessionId) { this.loadHistory(); }
      else { this.suggestedQuestions = this._getDefaultSuggestions(); }
      // 恢复本地草稿
      const draft = localStorage.getItem('chat_draft');
      if (draft) this.inputText = draft;
    },

    async sendMessage() {
      const text = this.inputText.trim();
      if (!text || this.isLoading || this.isStreaming) return;
      if (text.length > 2000) { toast.warning('消息不能超过2000字'); return; }

      // 添加用户消息
      this.messages.push({ role: 'user', content: text, time: new Date().toISOString() });
      this.inputText = '';
      localStorage.removeItem('chat_draft');
      this.isLoading = true;
      this.suggestedQuestions = [];
      this.references = [];

      // 创建新会话
      if (!this.sessionId) {
        try {
          const res = await api.post('/api/chat/session', { title: text.slice(0, 30) });
          this.sessionId = res.data?.sessionId;
        } catch (e) { /* ignore */ }
      }

      // 滚动到底部
      this._scrollToBottom();

      // SSE 流式请求
      this.isStreaming = true;
      this.streamingContent = '';
      let fullContent = '';

      await api.stream('/api/chat/message', {
        sessionId: this.sessionId,
        content: text,
        course: this.relatedCourse || undefined,
        knowledgeId: this.relatedKnowledge === 'auto' ? undefined : this.relatedKnowledge
      },
      (data) => {
        // 流式消息回调
        if (data.content) {
          fullContent += data.content;
          this.streamingContent = fullContent;
          this._scrollToBottom();
        }
        if (data.suggestions) { this.suggestedQuestions = data.suggestions.slice(0, 3); }
        if (data.references) { this.references = data.references; }
      },
      () => {
        // 完成回调
        this.messages.push({ role: 'assistant', content: fullContent, time: new Date().toISOString(), references: this.references });
        this.streamingContent = '';
        this.isStreaming = false;
        this.isLoading = false;
        this._scrollToBottom();
      },
      (err) => {
        this.isStreaming = false;
        this.isLoading = false;
        if (fullContent) {
          this.messages.push({ role: 'assistant', content: fullContent + '\n\n⚠️ 响应中断', time: new Date().toISOString() });
        } else {
          toast.error(err.message || 'AI 响应失败，请重试');
        }
        this.streamingContent = '';
      });
    },

    sendSuggested(question) {
      this.inputText = question;
      this.sendMessage();
    },

    stopStreaming() {
      if (this.abortController) { this.abortController.abort(); }
      this.isStreaming = false;
      this.isLoading = false;
      if (this.streamingContent) {
        this.messages.push({ role: 'assistant', content: this.streamingContent + '\n\n_[已停止]_', time: new Date().toISOString() });
        this.streamingContent = '';
      }
    },

    async newSession() {
      this.sessionId = null;
      this.messages = [];
      this.suggestedQuestions = this._getDefaultSuggestions();
      this.references = [];
      this.streamingContent = '';
    },

    async loadSessions() {
      try {
        const res = await api.get('/api/chat/sessions?page=1&size=20');
        this.sessions = res.data?.records || [];
      } catch { this.sessions = []; }
    },

    async switchSession(sid) {
      this.sessionId = sid;
      this.showSessionList = false;
      this.messages = [];
      this.suggestedQuestions = [];
      await this.loadHistory();
    },

    async loadHistory() {
      if (!this.sessionId) return;
      try {
        const res = await api.get(`/api/chat/session/${this.sessionId}/messages?page=1&size=50`);
        this.messages = (res.data?.records || []).map(m => ({
          role: m.role === 'user' ? 'user' : 'assistant',
          content: m.content,
          time: m.createdAt,
          references: m.references || []
        })).reverse();
      } catch { /* ignore */ }
    },

    handleKeydown(event) {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        this.sendMessage();
      }
    },

    saveDraft() { localStorage.setItem('chat_draft', this.inputText); },

    _getDefaultSuggestions() {
      const stage = Alpine.store('user')?.stage || 'middle';
      const suggestions = {
        elementary: ['帮我讲讲分数的加法', '什么是面积？', '100以内的加减法练习'],
        middle: ['请讲解一次函数的图像', '帮我分析这道几何题', '英语时态怎么区分？'],
        high: ['二次函数顶点式怎么求？', '三角函数诱导公式有哪些？', '概率统计怎么入门？'],
        university: ['帮我分析这篇论文的方法论', '解释一下机器学习中的过拟合', '线性代数的核心概念有哪些？'],
        vocational: ['项目管理有哪些常用工具？', '帮我写一份数据分析报告', 'Python基础语法梳理'],
        adult: ['如何高效利用碎片时间学习？', '帮我制定一个英语学习计划', '职场沟通技巧有哪些？']
      };
      return suggestions[stage] || suggestions.middle;
    },

    _scrollToBottom() {
      this.$nextTick(() => {
        const el = this.$refs.messagesContainer;
        if (el) el.scrollTop = el.scrollHeight;
      });
    }
  };
}
