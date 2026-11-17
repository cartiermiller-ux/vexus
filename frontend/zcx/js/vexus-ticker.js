(function () {
  'use strict';
  if (window.__vexusTickerInited) return;
  window.__vexusTickerInited = true;

  var style = document.createElement('style');
  style.textContent =
    '#vexus-tv-ticker{display:none!important}' +
    '.vexus-ticker-card{width:230px!important;height:262px!important;box-sizing:border-box!important;background:linear-gradient(180deg,#1a1738,#21253a)!important;border-radius:15px!important;padding:20px 0!important;margin:0 5px!important}' +
    '.vexus-ticker-card .moveListBodytop{display:flex;align-items:center;font-size:18px;line-height:30px;font-weight:500}' +
    '.vexus-ticker-card .moveListBodyLeftBot{font-size:24px}' +
    '.vexus-ticker-card .moveListBodybottom{margin-top:16px}' +
    '.vexus-ticker-card .vexus-spark{width:200px;height:70px;display:block;margin-top:4px}' +
    '.vexus-ticker-card{position:relative!important}' +
    '.vexus-ticker-card [data-vx-time]{position:absolute!important;left:0!important;right:0!important;bottom:18px!important;margin:0!important;font-size:12px!important;line-height:1!important;text-align:center!important}' +
    '.moveList .content-view-box{overflow-x:auto!important;scrollbar-width:none}' +
    '.moveList .content-view-box::-webkit-scrollbar{display:none}' +
    '.currency .more img{border-radius:0}';
  document.head.appendChild(style);

  var ticker = [
    { sym: 'BTC', pair: 'BTCUSDT', icon: 'btc' },
    { sym: 'ETH', pair: 'ETHUSDT', icon: 'eth' },
    { sym: 'SOL', pair: 'SOLUSDT', icon: 'sol' },
    { sym: 'BNB', pair: 'BNBUSDT', icon: 'bnb' },
    { sym: 'XRP', pair: 'XRPUSDT', icon: 'xrp' }
  ];

  function sparkline(up) {
    var color = up ? '#25d6a2' : '#ff6b8b';
    return '<svg class="vexus-spark" viewBox="0 0 180 40" preserveAspectRatio="none">' +
      '<path d="M0 30 L18 26 L36 29 L54 20 L72 24 L90 16 L108 21 L126 12 L144 17 L162 9 L180 14" fill="none" stroke="' + color + '" stroke-width="1.6" opacity=".9"/></svg>';
  }

  function syncCardSizes() {
    var box = document.querySelector('.moveList .content-view-box');
    if (!box) return;
    var sample = box.querySelector('.moveListBody:not([data-vexus-ticker-card])');
    if (!sample) return;
    var cs = getComputedStyle(sample);
    var h = sample.getBoundingClientRect().height;
    var w = sample.getBoundingClientRect().width;
    if (h < 80) return;
    box.querySelectorAll('[data-vexus-ticker-card]').forEach(function (card) {
      card.style.setProperty('height', h + 'px', 'important');
      card.style.setProperty('min-height', h + 'px', 'important');
      card.style.setProperty('width', w + 'px', 'important');
      card.style.setProperty('background', cs.background, 'important');
      card.style.setProperty('border-radius', cs.borderRadius, 'important');
      var spark = card.querySelector('.vexus-spark');
      if (spark) spark.style.setProperty('height', '76px', 'important');
    });
  }

  function buildCards() {
    var box = document.querySelector('.moveList .content-view-box');
    if (!box) return false;
    if (box.querySelector('[data-vexus-ticker-card]')) return true;
    ticker.slice().reverse().forEach(function (c) {
      var el = document.createElement('div');
      el.className = 'moveListBody vexus-ticker-card';
      el.setAttribute('data-vexus-ticker-card', c.sym);
      el.innerHTML =
        '<div class="moveListBodytop"><img class="el-img-style" style="margin-right:12px;width:28px;height:28px" src="/symbol/' + c.icon + '.png" alt=""> ' + c.sym + '/USDT</div>' +
        '<div class="moveListBodyLeftBot" data-vx-price>--</div>' +
        '<div class="moveListBodyright moveListBodyrightTop" data-vx-pct><span>-- %</span></div>' +
        '<div class="moveListBodybottom" style="padding:0"><div class="moveListBodyrightBot">' + sparkline(false) + '</div></div>' +
        '<div style="font-size:12px;margin-top:15px;text-align:center" data-vx-time>--</div>';
      box.insertBefore(el, box.firstChild);
    });
    box.scrollLeft = 0;
    syncCardSizes();
    setTimeout(syncCardSizes, 800);
    return true;
  }

  function updateCards() {
    var pairs = ticker.map(function (c) { return c.pair; });
    var url = 'https://api.binance.com/api/v3/ticker/24hr?symbols=' + encodeURIComponent(JSON.stringify(pairs));
    fetch(url).then(function (r) { return r.json(); }).then(function (list) {
      if (!Array.isArray(list)) return;
      list.forEach(function (q) {
        var card = document.querySelector('[data-vexus-ticker-card="' + q.symbol.replace('USDT', '') + '"]');
        if (!card) return;
        var price = Number(q.lastPrice);
        var pct = Number(q.priceChangePercent);
        card.querySelector('[data-vx-price]').textContent = '$' + price.toLocaleString('en-US', { maximumFractionDigits: 6 });
        var pctEl = card.querySelector('[data-vx-pct]');
        pctEl.className = 'moveListBodyright ' + (pct < 0 ? 'moveListBodyrightTop' : 'moveListBodyrightTopUp');
        pctEl.querySelector('span').textContent = (pct > 0 ? '+' : '') + pct.toFixed(2) + ' %';
        card.querySelector('[data-vx-time]').textContent = new Date().toLocaleString('sv-SE');
        var spark = card.querySelector('.vexus-spark');
        if (spark) spark.outerHTML = sparkline(pct >= 0);
      });
    }).catch(function () {});
  }

  function fixCoinIcons() {
    var imgs = document.querySelectorAll('.currency .more img');
    for (var i = 0; i < imgs.length; i++) {
      var img = imgs[i];
      if (img.getAttribute('src') && img.getAttribute('src').indexOf('more-icon-real') < 0) {
        img.setAttribute('src', '/img/more-icon-real.svg?v=20261201');
      }
    }
  }

  function autoScroll() {
    var box = document.querySelector('.moveList .content-view-box');
    if (box && box.getBoundingClientRect().bottom > 0 && box.getBoundingClientRect().top < window.innerHeight) {
      var max = box.scrollWidth - box.clientWidth;
      if (max > 0) {
        box.scrollLeft += 1.1;
        if (box.scrollLeft >= max - 1) box.scrollLeft = 0;
      }
    }
    requestAnimationFrame(autoScroll);
  }

  function ensure() {
    var old = document.getElementById('vexus-tv-ticker');
    if (old) old.remove();
    buildCards();
    fixCoinIcons();
  }

  setInterval(ensure, 800);
  setInterval(updateCards, 8000);
  setTimeout(updateCards, 1500);
  requestAnimationFrame(autoScroll);
  var mo = new MutationObserver(function () { ensure(); });
  mo.observe(document.body, { childList: true, subtree: true });
})();
