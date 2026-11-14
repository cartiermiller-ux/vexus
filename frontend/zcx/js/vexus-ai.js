(function () {
  'use strict';
  if (window.__vexusAIInited) return;
  window.__vexusAIInited = true;

  var lang = localStorage.getItem('localLan') || 'en';
  var zh = lang === 'zh-CN' || lang === 'CN' || lang === 'cht';
  var T = zh ? {
    title: 'Vexus AI 助手',
    subtitle: '智能客服 · 7×24 在线',
    open: 'AI 客服',
    placeholder: '输入你的问题…',
    send: '发送',
    welcome: '你好！我是 Vexus AI 助手，可以帮你了解注册、KYC、充值、提现、交易和账户安全。请问有什么可以帮你？',
    disclaimer: 'AI 回答仅供参考，不构成投资建议；请勿发送私钥、助记词、密码或验证码。',
    thinking: '正在思考…',
    error: '网络异常，请稍后再试',
    config: 'AI 客服暂未配置，请联系管理员',
    quick: ['如何充值？', '如何提现？', 'KYC 认证怎么做？', '手续费是多少？']
  } : {
    title: 'Vexus AI Assistant',
    subtitle: '24/7 smart support',
    open: 'AI Support',
    placeholder: 'Ask a question…',
    send: 'Send',
    welcome: 'Hi! I am the Vexus AI assistant. I can help with registration, KYC, deposits, withdrawals, trading and account security. How can I help?',
    disclaimer: 'AI answers are for reference only and not financial advice. Never share private keys, seed phrases, passwords or 2FA codes.',
    thinking: 'Thinking…',
    error: 'Network error, please try again later',
    config: 'AI support is not configured yet. Please contact the administrator.',
    quick: ['How to deposit?', 'How to withdraw?', 'How does KYC work?', 'What are the fees?']
  };

  var root = document.createElement('div');
  root.id = 'vexus-ai-root';
  root.innerHTML =
    '<button id="vexus-ai-fab" type="button" aria-label="AI">' +
      '<span class="vexus-ai-fab-orb"></span><span class="vexus-ai-fab-label">' + T.open + '</span>' +
    '</button>' +
    '<div id="vexus-ai-panel" class="vexus-ai-hidden">' +
      '<div class="vexus-ai-head"><div class="vexus-ai-head-text"><strong>' + T.title + '</strong><small>' + T.subtitle + '</small></div>' +
        '<button id="vexus-ai-close" type="button" aria-label="close">×</button></div>' +
      '<div id="vexus-ai-msgs" class="vexus-ai-msgs"></div>' +
      '<div id="vexus-ai-quick" class="vexus-ai-quick"></div>' +
      '<div class="vexus-ai-input-wrap"><textarea id="vexus-ai-input" rows="1" maxlength="2000" placeholder="' + T.placeholder + '"></textarea>' +
        '<button id="vexus-ai-send" type="button">' + T.send + '</button></div>' +
      '<div class="vexus-ai-foot">' + T.disclaimer + '</div>' +
    '</div>';
  document.body.appendChild(root);

  var panel = document.getElementById('vexus-ai-panel');
  var msgs = document.getElementById('vexus-ai-msgs');
  var input = document.getElementById('vexus-ai-input');
  var sendBtn = document.getElementById('vexus-ai-send');
  var quick = document.getElementById('vexus-ai-quick');
  var history = [];
  var busy = false;

  function bubble(role, text) {
    var row = document.createElement('div');
    row.className = 'vexus-ai-row ' + (role === 'user' ? 'is-user' : 'is-bot');
    var avatar = document.createElement('div');
    avatar.className = 'vexus-ai-avatar';
    avatar.textContent = role === 'user' ? 'U' : 'AI';
    var body = document.createElement('div');
    body.className = 'vexus-ai-bubble';
    body.textContent = text;
    row.appendChild(avatar);
    row.appendChild(body);
    msgs.appendChild(row);
    msgs.scrollTop = msgs.scrollHeight;
    return body;
  }

  function addTyping() {
    var row = document.createElement('div');
    row.className = 'vexus-ai-row is-bot vexus-ai-typing';
    row.innerHTML = '<div class="vexus-ai-avatar">AI</div><div class="vexus-ai-bubble">' + T.thinking + '</div>';
    msgs.appendChild(row);
    msgs.scrollTop = msgs.scrollHeight;
    return row;
  }

  function setBusy(v) {
    busy = v;
    sendBtn.disabled = v;
    sendBtn.style.opacity = v ? '.55' : '1';
  }

  function send(text) {
    text = (text || '').trim();
    if (!text || busy) return;
    bubble('user', text);
    history.push({ role: 'user', content: text });
    if (history.length > 10) history = history.slice(history.length - 10);
    input.value = '';
    input.style.height = 'auto';
    setBusy(true);
    var typing = addTyping();
    var body = new URLSearchParams();
    body.set('message', text);
    var hist = history.slice(0, -1);
    if (hist.length) body.set('history', JSON.stringify(hist.slice(-8)));
    fetch('/api/ai!chat.action', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
      credentials: 'same-origin',
      body: body.toString()
    }).then(function (r) { return r.json(); }).then(function (res) {
      if (typing && typing.parentNode) typing.parentNode.removeChild(typing);
      if (res && res.code === '0' && res.data && res.data.reply) {
        bubble('assistant', res.data.reply);
        history.push({ role: 'assistant', content: res.data.reply });
        if (history.length > 10) history = history.slice(history.length - 10);
      } else {
        var m = res && res.msg ? res.msg : T.error;
        if (res && res.msg && res.msg.indexOf('尚未配置') >= 0) m = T.config;
        bubble('assistant', m);
      }
    }).catch(function () {
      if (typing && typing.parentNode) typing.parentNode.removeChild(typing);
      bubble('assistant', T.error);
    }).then(function () {
      setBusy(false);
      input.focus();
    });
  }

  T.quick.forEach(function (q) {
    var b = document.createElement('button');
    b.type = 'button';
    b.textContent = q;
    b.onclick = function () { send(q); };
    quick.appendChild(b);
  });

  document.getElementById('vexus-ai-fab').addEventListener('click', function () {
    panel.classList.toggle('vexus-ai-hidden');
    if (!panel.classList.contains('vexus-ai-hidden')) {
      if (!msgs.children.length) bubble('assistant', T.welcome);
      setTimeout(function () { input.focus(); }, 50);
    }
  });
  document.getElementById('vexus-ai-close').addEventListener('click', function () {
    panel.classList.add('vexus-ai-hidden');
  });
  sendBtn.addEventListener('click', function () { send(input.value); });
  input.addEventListener('keydown', function (e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      send(input.value);
    }
  });
  input.addEventListener('input', function () {
    input.style.height = 'auto';
    input.style.height = Math.min(96, input.scrollHeight) + 'px';
  });
})();
