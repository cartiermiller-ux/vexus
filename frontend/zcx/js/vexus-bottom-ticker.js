(function () {
  'use strict';
  if (window.__vexusBottomTicker) return;
  window.__vexusBottomTicker = true;

  var H = 44;
  var style = document.createElement('style');
  style.textContent =
    'body{padding-bottom:' + H + 'px!important}' +
    '#vexus-bottom-ticker{position:fixed;left:0;right:0;bottom:0;width:100%;height:' + H + 'px;z-index:9999;' +
    'display:flex;align-items:center;overflow:hidden;padding:0 10px;' +
    'background:linear-gradient(90deg,rgba(14,11,30,.94),rgba(23,18,45,.88),rgba(14,11,30,.94));' +
    'backdrop-filter:blur(10px);-webkit-backdrop-filter:blur(10px);' +
    'box-shadow:0 -6px 24px rgba(0,0,0,.35);border-top:1px solid rgba(255,255,255,.08);' +
    '-webkit-mask-image:linear-gradient(90deg,transparent 0,#000 3%,#000 97%,transparent 100%);' +
    'mask-image:linear-gradient(90deg,transparent 0,#000 3%,#000 97%,transparent 100%)}' +
    '#vexus-bottom-ticker .vx-bt-track{display:inline-flex;align-items:center;gap:28px;white-space:nowrap;' +
    'will-change:transform;animation:vxBottomMarquee 30s linear infinite}' +
    '#vexus-bottom-ticker:hover .vx-bt-track{animation-play-state:paused}' +
    '#vexus-bottom-ticker .vx-bt-item{display:inline-flex;align-items:center;gap:8px;font-size:12px;line-height:1}' +
    '#vexus-bottom-ticker .vx-bt-sym{color:#cfc9e0;font-weight:700;letter-spacing:.2px}' +
    '#vexus-bottom-ticker .vx-bt-price{color:#fff;font-variant-numeric:tabular-nums}' +
    '#vexus-bottom-ticker .vx-bt-pct{font-variant-numeric:tabular-nums;font-weight:700}' +
    '#vexus-bottom-ticker .vx-bt-pct.up{color:#0ECB81}' +
    '#vexus-bottom-ticker .vx-bt-pct.down{color:#F6465D}' +
    '#vexus-bottom-ticker .vx-bt-sep{color:rgba(255,255,255,.18)}' +
    '.service-box{bottom:' + (H + 12) + 'px!important}' +
    '@keyframes vxBottomMarquee{from{transform:translateX(0)}to{transform:translateX(-50%)}}' +
    '@media(max-width:600px){#vexus-bottom-ticker{height:40px}body{padding-bottom:40px!important}' +
    '#vexus-bottom-ticker .vx-bt-item{font-size:11px;gap:6px}}';
  document.head.appendChild(style);

  var symbols = ['BTCUSDT', 'ETHUSDT', 'SOLUSDT', 'BNBUSDT', 'XRPUSDT', 'DOGEUSDT', 'ADAUSDT', 'AVAXUSDT', 'LINKUSDT', 'DOTUSDT'];
  var names = {
    BTCUSDT: 'BTC/USDT', ETHUSDT: 'ETH/USDT', SOLUSDT: 'SOL/USDT', BNBUSDT: 'BNB/USDT',
    XRPUSDT: 'XRP/USDT', DOGEUSDT: 'DOGE/USDT', ADAUSDT: 'ADA/USDT', AVAXUSDT: 'AVAX/USDT',
    LINKUSDT: 'LINK/USDT', DOTUSDT: 'DOT/USDT'
  };
  var fallback = {
    BTCUSDT: [63755.82, -6.68], ETHUSDT: [1753.62, -9.13], SOLUSDT: [69.21, -11.48],
    BNBUSDT: [575.37, -8.72], XRPUSDT: [0.9009, -10.89], DOGEUSDT: [0.05586, -12.71],
    ADAUSDT: [0.1455, -13.70], AVAXUSDT: [7.06, -14.27], LINKUSDT: [9.54, -12.18], DOTUSDT: [2.12, -11.67]
  };

  var bar = document.createElement('div');
  bar.id = 'vexus-bottom-ticker';
  bar.innerHTML = '<div class="vx-bt-track"></div>';
  document.body.appendChild(bar);
  var track = bar.querySelector('.vx-bt-track');

  function fmt(n) {
    if (n >= 1000) return n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    if (n >= 1) return n.toFixed(3);
    return n.toFixed(5);
  }

  function render(data) {
    if (!track.getAttribute('data-built')) {
      var html = '';
      symbols.forEach(function (s) {
        html += '<span class="vx-bt-item"><span class="vx-bt-sym">' + names[s] + '</span>' +
          '<span class="vx-bt-price">--</span>' +
          '<span class="vx-bt-pct">--%</span>' +
          '<span class="vx-bt-sep">|</span></span>';
      });
      track.innerHTML = html + html;
      track.setAttribute('data-built', '1');
    }
    var items = track.querySelectorAll('.vx-bt-item');
    for (var i = 0; i < items.length; i++) {
      var s = symbols[i % symbols.length];
      var q = data[s] || fallback[s];
      var pct = Number(q[1]);
      var item = items[i];
      item.querySelector('.vx-bt-price').textContent = '$' + fmt(Number(q[0]));
      var pctEl = item.querySelector('.vx-bt-pct');
      pctEl.className = 'vx-bt-pct ' + (pct >= 0 ? 'up' : 'down');
      pctEl.textContent = (pct >= 0 ? '+' : '') + pct.toFixed(2) + '%';
    }
  }

  render({});
  fetch('https://api.binance.com/api/v3/ticker/24hr?symbols=' + encodeURIComponent(JSON.stringify(symbols)))
    .then(function (r) { return r.json(); })
    .then(function (list) {
      if (!Array.isArray(list)) return;
      var data = {};
      list.forEach(function (q) {
        data[q.symbol] = [Number(q.lastPrice), Number(q.priceChangePercent)];
      });
      render(data);
    })
    .catch(function () {});
  setInterval(function () {
    fetch('https://api.binance.com/api/v3/ticker/24hr?symbols=' + encodeURIComponent(JSON.stringify(symbols)))
      .then(function (r) { return r.json(); })
      .then(function (list) {
        if (!Array.isArray(list)) return;
        var data = {};
        list.forEach(function (q) { data[q.symbol] = [Number(q.lastPrice), Number(q.priceChangePercent)]; });
        render(data);
      }).catch(function () {});
  }, 15000);
})();
