(function () {
  'use strict';
  if (window.__vexusMarketHero) return;
  window.__vexusMarketHero = true;

  var style = document.createElement('style');
  style.textContent =
    '#vexus-market-hero{position:relative;z-index:2;width:100%;height:100vh;min-height:680px;overflow:hidden;' +
    'background:#02040a;margin:0}' +
    '#vexus-market-hero video{position:absolute;inset:0;width:100%;height:100%;object-fit:cover;' +
    'z-index:0;pointer-events:none}' +
    '#vexus-market-hero .vmh-mask{position:absolute;inset:0;z-index:1;pointer-events:none;' +
    'background:linear-gradient(180deg,rgba(2,4,10,.94) 0%,rgba(2,4,10,.45) 38%,rgba(2,4,10,.12) 62%,rgba(2,4,10,.82) 100%)}' +
    '#vexus-market-hero .vmh-copy{position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);z-index:2;' +
    'width:min(900px,92vw);text-align:center;color:#fff}' +
    '#vexus-market-hero .vmh-eyebrow{font-size:12px;letter-spacing:3px;color:#8b95ad;text-transform:uppercase;margin-bottom:18px}' +
    '#vexus-market-hero h1{font-size:42px;line-height:1.15;margin:0 0 16px;font-weight:800;letter-spacing:.5px}' +
    '#vexus-market-hero p{font-size:15px;line-height:1.7;color:rgba(255,255,255,.72);margin:0 auto 24px;max-width:620px}' +
    '#vexus-market-hero a{display:inline-flex;align-items:center;gap:8px;height:44px;padding:0 26px;border-radius:10px;' +
    'background:#fff;color:#0b0b12;font-weight:700;font-size:14px;text-decoration:none;box-shadow:0 10px 30px rgba(0,0,0,.3)}' +
    '#vexus-market-hero a:hover{background:#e9e6ff}' +
    '#vexus-market-hero .vmh-note{display:block;margin-top:12px;font-size:11px;color:rgba(255,255,255,.42)}' +
    '#vexus-market-hero .vmh-scroll{position:absolute;left:50%;bottom:18px;transform:translateX(-50%);z-index:2;' +
    'color:rgba(255,255,255,.5);font-size:18px;animation:vmhBounce 2s infinite}' +
    '@keyframes vmhBounce{0%,100%{transform:translate(-50%,0)}50%{transform:translate(-50%,6px)}}' +
    '@media(max-width:700px){#vexus-market-hero{height:480px}' +
    '#vexus-market-hero .vmh-copy{top:46%}' +
    '#vexus-market-hero h1{font-size:28px}' +
    '#vexus-market-hero p{font-size:13px}}';
  document.head.appendChild(style);

  function isMarket() {
    var t = document.body ? (document.body.innerText || '') : '';
    return /Top Gainers|All Cryptos|Spot Markets|漲幅榜|涨幅榜|所有幣種|所有币种|幣幣行情|币币行情/.test(t) ||
      (location.hash || '').toLowerCase().indexOf('/market') >= 0;
  }

  function langPack() {
    var lang = localStorage.getItem('localLan') || 'en';
    if (lang === 'en') {
      return {
        h1: "The world's markets, in one place",
        p: 'Join traders worldwide and track crypto, stocks and global markets on one platform.',
        cta: 'Start trading',
        note: 'Real-time markets · Multi-market coverage · Secure trading'
      };
    }
    if (lang === 'cht' || lang === 'zh-TW') {
      return {
        h1: '世界市場，一處掌握',
        p: '加入全球交易者的行列，在同一個平台查看加密資產、股票與全球市場行情。',
        cta: '開始交易',
        note: '即時行情 · 多市場覆蓋 · 安全交易'
      };
    }
    if (lang !== 'zh-CN') {
      return {
        h1: "The world's markets, in one place",
        p: 'Join traders worldwide and track crypto, stocks and global markets on one platform.',
        cta: 'Start trading',
        note: 'Real-time markets · Multi-market coverage · Secure trading'
      };
    }
    return {
      h1: '世界市场，一处掌握',
      p: '加入全球交易者的行列，在同一个平台查看加密资产、股票与全球市场行情。',
      cta: '开始交易',
      note: '实时行情 · 多市场覆盖 · 安全交易'
    };
  }

  function applyHeroLang(hero) {
    if (!hero) return;
    var pack = langPack();
    var t = window.VexusI18n && window.VexusI18n.t;
    var h1 = hero.querySelector('.vmh-h1');
    var p = hero.querySelector('.vmh-p');
    var a = hero.querySelector('.vmh-cta');
    var note = hero.querySelector('.vmh-note');
    if (h1) h1.textContent = t ? t('hero.h1') : pack.h1;
    if (p) p.textContent = t ? t('hero.p') : pack.p;
    if (a) a.textContent = t ? t('hero.cta') : pack.cta;
    if (note) note.textContent = t ? t('hero.note') : pack.note;
  }

  function ensure() {
    if (!isMarket()) return;
    var existing = document.getElementById('vexus-market-hero');
    if (existing) {
      applyHeroLang(existing);
      var ps0 = document.getElementById('vexus-platform-section');
      if (ps0 && ps0.parentNode) ps0.parentNode.removeChild(ps0);
      var hb0 = document.querySelector('.page-header-background');
      var hr0 = hb0 ? hb0.parentNode : null;
      if (hr0 && hr0.parentNode && hr0.nextElementSibling !== existing) {
        hr0.parentNode.insertBefore(existing, hr0.nextSibling);
      }
      return;
    }
    var view = document.querySelector('.view');
    if (!view) return;
    var hero = document.createElement('section');
    hero.id = 'vexus-market-hero';
    hero.innerHTML =
      '<video muted loop playsinline autoplay preload="metadata" crossorigin="anonymous">' +
      '<source src="https://static.tradingview.com/static/bundles/earth.hvc1.8b593d5a601c93b15aa1.mp4" type="video/mp4;codecs=hvc1.1.0.L150.b0">' +
      '<source src="https://static.tradingview.com/static/bundles/earth.a31bd806ce06fca5ae44.webm" type="video/webm">' +
      '<source src="https://static.tradingview.com/static/bundles/earth.avc1.f6a145c79bffd1ef7941.mp4" type="video/mp4;codecs=avc1">' +
      '</video>' +
      '<div class="vmh-mask"></div>' +
      '<div class="vmh-copy"><div class="vmh-eyebrow">VEXUS · GLOBAL MARKETS</div>' +
      '<h1 class="vmh-h1"></h1>' +
      '<p class="vmh-p"></p>' +
      '<a class="vmh-cta" href="/#/trade/btc_usdt"></a>' +
      '<span class="vmh-note"></span></div>' +
      '<div class="vmh-scroll">⌄</div>';
    var hb = document.querySelector('.page-header-background');
    var headerRoot = hb ? hb.parentNode : null;
    if (headerRoot && headerRoot.parentNode) {
      headerRoot.parentNode.insertBefore(hero, headerRoot.nextSibling);
    } else {
      view.insertBefore(hero, view.firstChild);
    }

    var oldSec = document.getElementById('vexus-platform-section');
    if (oldSec && oldSec.parentNode) oldSec.parentNode.removeChild(oldSec);

    applyHeroLang(hero);
    var v = hero.querySelector('video');
    v.play && v.play().catch(function () {});
    v.addEventListener('error', function () { hero.style.background = '#02040a'; }, true);
  }

  document.addEventListener('vexus-i18n-ready', function () { var h = document.getElementById('vexus-market-hero'); if (h) applyHeroLang(h); });
  setInterval(ensure, 800);
  var mo = new MutationObserver(function () { ensure(); });
  mo.observe(document.body, { childList: true, subtree: true });
})();
