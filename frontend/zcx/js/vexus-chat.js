(function () {
  'use strict';
  if (window.__vexusChatQuickInited) return;
  window.__vexusChatQuickInited = true;

  var lang = localStorage.getItem('localLan') || 'en';
  var zh = lang === 'zh-CN' || lang === 'CN' || lang === 'cht';
  var questions = zh ? [
    ['如何充值？', '如何充值 USDT？'],
    ['如何提现？', '如何提现？'],
    ['KYC 认证怎么做？', 'KYC 认证怎么做？'],
    ['手续费多少？', '现货和合约的手续费是多少？'],
    ['如何联系人工？', '请帮我转人工客服']
  ] : [
    ['How to deposit?', 'How do I deposit USDT?'],
    ['How to withdraw?', 'How do I withdraw?'],
    ['KYC?', 'How does KYC verification work?'],
    ['Fees?', 'What are the spot and futures fees?'],
    ['Human support', 'Please transfer me to a human agent']
  ];

  var style = document.createElement('style');
  style.textContent =
    '.vexus-chat-quick{display:flex;flex-wrap:wrap;gap:6px;padding:8px 12px 2px;background:transparent}' +
    '.vexus-chat-quick button{height:26px;padding:0 10px;border-radius:13px;border:1px solid rgba(167,139,250,.35);' +
    'background:rgba(124,92,255,.16);color:#d9ccff;font-size:11px;cursor:pointer;white-space:nowrap}' +
    '.vexus-chat-quick button:hover{background:rgba(124,92,255,.32);color:#fff}' +
    '.vexus-chat-ai-badge{display:inline-flex;align-items:center;height:16px;padding:0 5px;margin-right:4px;border-radius:4px;' +
    'background:rgba(124,92,255,.35);color:#e2d6ff;font-size:9px;vertical-align:middle}';
  document.head.appendChild(style);

  function sendText(kefu, text) {
    var input = kefu.querySelector('.input-box input[type=text]');
    if (!input) return false;
    try {
      var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
      setter.call(input, text);
      input.dispatchEvent(new Event('input', { bubbles: true }));
      input.dispatchEvent(new Event('change', { bubbles: true }));
      input.focus();
      ['keydown', 'keypress', 'keyup'].forEach(function (type) {
        input.dispatchEvent(new KeyboardEvent(type, {
          key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true, cancelable: true
        }));
      });
      var box = kefu.querySelector('.input-box');
      var imgs = box ? box.querySelectorAll('img') : [];
      if (imgs.length) {
        setTimeout(function () { imgs[imgs.length - 1].click(); }, 60);
      }
      return true;
    } catch (e) {
      return false;
    }
  }

  function ensureQuick() {
    var kefu = document.querySelector('.kefu');
    if (!kefu) return;
    var chat = kefu.querySelector('.chat') || kefu;
    if (chat.querySelector('.vexus-chat-quick')) return;
    var inputBox = chat.querySelector('.input-box');
    if (!inputBox) return;
    var row = document.createElement('div');
    row.className = 'vexus-chat-quick';
    questions.forEach(function (q) {
      var b = document.createElement('button');
      b.type = 'button';
      b.textContent = q[0];
      b.addEventListener('click', function () { sendText(kefu, q[1]); });
      row.appendChild(b);
    });
    inputBox.parentNode.insertBefore(row, inputBox);
  }

  setInterval(ensureQuick, 600);
  var mo = new MutationObserver(function () { ensureQuick(); });
  mo.observe(document.body, { childList: true, subtree: true });
})();
