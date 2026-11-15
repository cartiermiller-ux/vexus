/* Icons: Lucide 1.72.0 (ISC License) - https://lucide.dev */
(function () {
  'use strict';
var VEXUS_ICON_PATHS = {"activity": "<path d=\"M22 12h-2.48a2 2 0 0 0-1.93 1.46l-2.35 8.36a.25.25 0 0 1-.48 0L9.24 2.18a.25.25 0 0 0-.48 0l-2.35 8.36A2 2 0 0 1 4.49 12H2\" />", "arrow-left-right": "<path d=\"M8 3 4 7l4 4\" />\n  <path d=\"M4 7h16\" />\n  <path d=\"m16 21 4-4-4-4\" />\n  <path d=\"M20 17H4\" />", "banknote": "<rect width=\"20\" height=\"12\" x=\"2\" y=\"6\" rx=\"2\" />\n  <circle cx=\"12\" cy=\"12\" r=\"2\" />\n  <path d=\"M6 12h.01M18 12h.01\" />", "book-open": "<path d=\"M12 5v16\" />\n  <path d=\"M20.001 19A2 2 0 0022 17V5a2 2 0 00-1.999-2L16 3.002A5 5 0 0012 5a5 5 0 00-4-2H4a2 2 0 00-2 2v12a2 2 0 001.999 2H8a5 5 0 014 2 5 5 0 014-2z\" />", "bot": "<path d=\"M12 8V4H8\" />\n  <rect width=\"16\" height=\"12\" x=\"4\" y=\"8\" rx=\"2\" />\n  <path d=\"M2 14h2\" />\n  <path d=\"M20 14h2\" />\n  <path d=\"M15 13v2\" />\n  <path d=\"M9 13v2\" />", "bot-message-square": "<path d=\"M12 6V2H8\" />\n  <path d=\"M15 11v2\" />\n  <path d=\"M2 12h2\" />\n  <path d=\"M20 12h2\" />\n  <path d=\"M20 16a2 2 0 0 1-2 2H8.828a2 2 0 0 0-1.414.586l-2.202 2.202A.71.71 0 0 1 4 20.286V8a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2z\" />\n  <path d=\"M9 11v2\" />", "building-2": "<path d=\"M10 12h4\" />\n  <path d=\"M10 8h4\" />\n  <path d=\"M14 21v-3a2 2 0 0 0-4 0v3\" />\n  <path d=\"M6 10H4a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-2\" />\n  <path d=\"M6 21V5a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v16\" />", "calendar-check": "<path d=\"M8 2v3\" />\n  <path d=\"M16 2v3\" />\n  <rect x=\"3\" y=\"3\" width=\"18\" height=\"18\" rx=\"2\" />\n  <path d=\"M3 9h18\" />\n  <path d=\"m9 15 2 2 4-4\" />", "calendar-clock": "<path d=\"M16 14v2.2l1.6 1\" />\n  <path d=\"M16 2v3\" />\n  <path d=\"M21 7.338V5a2 2 0 00-2-2H5a2 2 0 00-2 2v14a2 2 0 002 2h2.338\" />\n  <path d=\"M3 9h5.859\" />\n  <path d=\"M8 2v3\" />\n  <circle cx=\"16\" cy=\"16\" r=\"6\" />", "chart-candlestick": "<path d=\"M9 5v4\" />\n  <rect width=\"4\" height=\"6\" x=\"7\" y=\"9\" rx=\"1\" />\n  <path d=\"M9 15v2\" />\n  <path d=\"M17 3v2\" />\n  <rect width=\"4\" height=\"8\" x=\"15\" y=\"5\" rx=\"1\" />\n  <path d=\"M17 13v3\" />\n  <path d=\"M3 3v16a2 2 0 0 0 2 2h16\" />", "chart-line": "<path d=\"M3 3v16a2 2 0 0 0 2 2h16\" />\n  <path d=\"m19 9-5 5-4-4-3 3\" />", "chart-pie": "<path d=\"M21 12c.552 0 1.005-.449.95-.998a10 10 0 0 0-8.953-8.951c-.55-.055-.998.398-.998.95v8a1 1 0 0 0 1 1z\" />\n  <path d=\"M21.21 15.89A10 10 0 1 1 8 2.83\" />", "clock-3": "<circle cx=\"12\" cy=\"12\" r=\"10\" />\n  <path d=\"M12 6v6h4\" />", "coins": "<path d=\"M13.744 17.736a6 6 0 1 1-7.48-7.48\" />\n  <path d=\"M15 6h1v4\" />\n  <path d=\"m6.134 14.768.866-.5 2 3.464\" />\n  <circle cx=\"16\" cy=\"8\" r=\"6\" />", "dog": "<path d=\"M11.25 16.25h1.5L12 17z\" />\n  <path d=\"M16 14v.5\" />\n  <path d=\"M4.42 11.247A13.152 13.152 0 0 0 4 14.556C4 18.728 7.582 21 12 21s8-2.272 8-6.444a11.702 11.702 0 0 0-.493-3.309\" />\n  <path d=\"M8 14v.5\" />\n  <path d=\"M8.5 8.5c-.384 1.05-1.083 2.028-2.344 2.5-1.931.722-3.576-.297-3.656-1-.113-.994 1.177-6.53 4-7 1.923-.321 3.651.845 3.651 2.235A7.497 7.497 0 0 1 14 5.277c0-1.39 1.844-2.598 3.767-2.277 2.823.47 4.113 6.006 4 7-.08.703-1.725 1.722-3.656 1-1.261-.472-1.855-1.45-2.239-2.5\" />", "file-search": "<path d=\"M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z\" />\n  <path d=\"M14 2v5a1 1 0 0 0 1 1h5\" />\n  <circle cx=\"11.5\" cy=\"14.5\" r=\"2.5\" />\n  <path d=\"M13.3 16.3 15 18\" />", "flag": "<path d=\"M4 22V4a1 1 0 0 1 .4-.8A6 6 0 0 1 8 2c3 0 5 2 7.333 2q2 0 3.067-.8A1 1 0 0 1 20 4v10a1 1 0 0 1-.4.8A6 6 0 0 1 16 16c-3 0-5-2-8-2a6 6 0 0 0-4 1.528\" />", "flask-conical": "<path d=\"M14 2v6a2 2 0 0 0 .245.96l5.51 10.08A2 2 0 0 1 18 22H6a2 2 0 0 1-1.755-2.96l5.51-10.08A2 2 0 0 0 10 8V2\" />\n  <path d=\"M6.453 15h11.094\" />\n  <path d=\"M8.5 2h7\" />", "gem": "<path d=\"M10.5 3 8 9l4 13 4-13-2.5-6\" />\n  <path d=\"M17 3a2 2 0 0 1 1.6.8l3 4a2 2 0 0 1 .013 2.382l-7.99 10.986a2 2 0 0 1-3.247 0l-7.99-10.986A2 2 0 0 1 2.4 7.8l2.998-3.997A2 2 0 0 1 7 3z\" />\n  <path d=\"M2 9h20\" />", "gift": "<path d=\"M12 7v14\" />\n  <path d=\"M20 11v8a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-8\" />\n  <path d=\"M7.5 7a1 1 0 0 1 0-5A4.8 8 0 0 1 12 7a4.8 8 0 0 1 4.5-5 1 1 0 0 1 0 5\" />\n  <rect x=\"3\" y=\"7\" width=\"18\" height=\"4\" rx=\"1\" />", "globe": "<circle cx=\"12\" cy=\"12\" r=\"10\" />\n  <path d=\"M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20\" />\n  <path d=\"M2 12h20\" />", "handshake": "<path d=\"m11 17 2 2a1 1 0 1 0 3-3\" />\n  <path d=\"m14 14 2.5 2.5a1 1 0 1 0 3-3l-3.88-3.88a3 3 0 0 0-4.24 0l-.88.88a1 1 0 1 1-3-3l2.81-2.81a5.79 5.79 0 0 1 7.06-.87l.47.28a2 2 0 0 0 1.42.25L21 4\" />\n  <path d=\"m21 3 1 11h-2\" />\n  <path d=\"M3 3 2 14l6.5 6.5a1 1 0 1 0 3-3\" />\n  <path d=\"M3 4h8\" />", "heart-handshake": "<path d=\"M19.414 14.414C21 12.828 22 11.5 22 9.5a5.5 5.5 0 0 0-9.591-3.676.6.6 0 0 1-.818.001A5.5 5.5 0 0 0 2 9.5c0 2.3 1.5 4 3 5.5l5.535 5.362a2 2 0 0 0 2.879.052 2.12 2.12 0 0 0-.004-3 2.124 2.124 0 1 0 3-3 2.124 2.124 0 0 0 3.004 0 2 2 0 0 0 0-2.828l-1.881-1.882a2.41 2.41 0 0 0-3.409 0l-1.71 1.71a2 2 0 0 1-2.828 0 2 2 0 0 1 0-2.828l2.823-2.762\" />", "landmark": "<path d=\"M10 18v-7\" />\n  <path d=\"M11.119 2.205a2 2 0 0 1 1.762 0l7.84 3.846A.5.5 0 0 1 20.5 7h-17a.5.5 0 0 1-.22-.949z\" />\n  <path d=\"M14 18v-7\" />\n  <path d=\"M18 18v-7\" />\n  <path d=\"M3 22h18\" />\n  <path d=\"M6 18v-7\" />", "layers": "<path d=\"M12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0l8.58-3.9a1 1 0 0 0 0-1.83z\" />\n  <path d=\"M2 12a1 1 0 0 0 .58.91l8.6 3.91a2 2 0 0 0 1.65 0l8.58-3.9A1 1 0 0 0 22 12\" />\n  <path d=\"M2 17a1 1 0 0 0 .58.91l8.6 3.91a2 2 0 0 0 1.65 0l8.58-3.9A1 1 0 0 0 22 17\" />", "line-chart": "<path d=\"M3 3v16a2 2 0 0 0 2 2h16\" />\n  <path d=\"m19 9-5 5-4-4-3 3\" />", "list-checks": "<path d=\"M13 5h8\" />\n  <path d=\"M13 12h8\" />\n  <path d=\"M13 19h8\" />\n  <path d=\"m3 17 2 2 4-4\" />\n  <path d=\"m3 7 2 2 4-4\" />", "megaphone": "<path d=\"M11 6a13 13 0 0 0 8.4-2.8A1 1 0 0 1 21 4v12a1 1 0 0 1-1.6.8A13 13 0 0 0 11 14H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z\" />\n  <path d=\"M6 14a12 12 0 0 0 2.4 7.2 2 2 0 0 0 3.2-2.4A8 8 0 0 1 10 14\" />\n  <path d=\"M8 6v8\" />", "newspaper": "<path d=\"M15 18h-5\" />\n  <path d=\"M18 14h-8\" />\n  <path d=\"M4 22h16a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v16a2 2 0 0 1-4 0v-9a2 2 0 0 1 2-2h2\" />\n  <rect width=\"8\" height=\"4\" x=\"10\" y=\"6\" rx=\"1\" />", "package": "<path d=\"M11 21.73a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73z\" />\n  <path d=\"M12 22V12\" />\n  <polyline points=\"3.29 7 12 12 20.71 7\" />\n  <path d=\"m7.5 4.27 9 5.15\" />", "pickaxe": "<path d=\"m14 13-8.381 8.38a1 1 0 0 1-3.001-3L11 9.999\" />\n  <path d=\"M15.973 4.027A13 13 0 0 0 5.902 2.373c-1.398.342-1.092 2.158.277 2.601a19.9 19.9 0 0 1 5.822 3.024\" />\n  <path d=\"M16.001 11.999a19.9 19.9 0 0 1 3.024 5.824c.444 1.369 2.26 1.676 2.603.278A13 13 0 0 0 20 8.069\" />\n  <path d=\"M18.352 3.352a1.205 1.205 0 0 0-1.704 0l-5.296 5.296a1.205 1.205 0 0 0 0 1.704l2.296 2.296a1.205 1.205 0 0 0 1.704 0l5.296-5.296a1.205 1.205 0 0 0 0-1.704z\" />", "piggy-bank": "<path d=\"M11 17h3v2a1 1 0 0 0 1 1h2a1 1 0 0 0 1-1v-3a3.16 3.16 0 0 0 2-2h1a1 1 0 0 0 1-1v-2a1 1 0 0 0-1-1h-1a5 5 0 0 0-2-4V3a4 4 0 0 0-3.2 1.6l-.3.4H11a6 6 0 0 0-6 6v1a5 5 0 0 0 2 4v3a1 1 0 0 0 1 1h2a1 1 0 0 0 1-1z\" />\n  <path d=\"M16 10h.01\" />\n  <path d=\"M2 8v1a2 2 0 0 0 2 2h1\" />", "repeat-2": "<path d=\"m2 9 3-3 3 3\" />\n  <path d=\"M13 18H7a2 2 0 0 1-2-2V6\" />\n  <path d=\"m22 15-3 3-3-3\" />\n  <path d=\"M11 6h6a2 2 0 0 1 2 2v10\" />", "rocket": "<path d=\"M12 15v5s3.03-.55 4-2c1.08-1.62 0-5 0-5\" />\n  <path d=\"M4.5 16.5c-1.5 1.26-2 5-2 5s3.74-.5 5-2c.71-.84.7-2.13-.09-2.91a2.18 2.18 0 0 0-2.91-.09\" />\n  <path d=\"M9 12a22 22 0 0 1 2-3.95A12.88 12.88 0 0 1 22 2c0 2.72-.78 7.5-6 11a22.4 22.4 0 0 1-4 2z\" />\n  <path d=\"M9 12H4s.55-3.03 2-4c1.62-1.08 5 .05 5 .05\" />", "scale": "<path d=\"M12 3v18\" />\n  <path d=\"m19 8 3 8a5 5 0 0 1-6 0zV7\" />\n  <path d=\"M3 7h1a17 17 0 0 0 8-2 17 17 0 0 0 8 2h1\" />\n  <path d=\"m5 8 3 8a5 5 0 0 1-6 0zV7\" />\n  <path d=\"M7 21h10\" />", "shield-check": "<path d=\"M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z\" />\n  <path d=\"m9 12 2 2 4-4\" />", "shuffle": "<path d=\"m18 14 4 4-4 4\" />\n  <path d=\"m18 2 4 4-4 4\" />\n  <path d=\"M2 18h1.973a4 4 0 0 0 3.3-1.7l5.454-8.6a4 4 0 0 1 3.3-1.7H22\" />\n  <path d=\"M2 6h1.972a4 4 0 0 1 3.6 2.2\" />\n  <path d=\"M22 18h-6.041a4 4 0 0 1-3.3-1.8l-.359-.45\" />", "ticket": "<path d=\"M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z\" />\n  <path d=\"M13 5v2\" />\n  <path d=\"M13 17v2\" />\n  <path d=\"M13 11v2\" />", "trending-up": "<path d=\"M16 7h6v6\" />\n  <path d=\"m22 7-8.5 8.5-5-5L2 17\" />", "user-plus": "<path d=\"M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2\" />\n  <circle cx=\"9\" cy=\"7\" r=\"4\" />\n  <line x1=\"19\" x2=\"19\" y1=\"8\" y2=\"14\" />\n  <line x1=\"22\" x2=\"16\" y1=\"11\" y2=\"11\" />", "users": "<path d=\"M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2\" />\n  <path d=\"M16 3.128a4 4 0 0 1 0 7.744\" />\n  <path d=\"M22 21v-2a4 4 0 0 0-3-3.87\" />\n  <circle cx=\"9\" cy=\"7\" r=\"4\" />", "wallet": "<path d=\"M19 7V4a1 1 0 0 0-1-1H5a2 2 0 0 0 0 4h15a1 1 0 0 1 1 1v4h-3a2 2 0 0 0 0 4h3a1 1 0 0 0 1-1v-2a1 1 0 0 0-1-1\" />\n  <path d=\"M3 5v14a2 2 0 0 0 2 2h15a1 1 0 0 0 1-1v-4\" />", "circle": "<circle cx=\"12\" cy=\"12\" r=\"9\"/>"};
var VEXUS_ICON_BY_LABEL = {"现": "wallet", "链": "dog", "杠": "trending-up", "闪": "repeat-2", "ETF": "chart-pie", "盘": "clock-3", "W3": "globe", "机": "bot", "跟": "users", "跨": "shuffle", "OTC": "landmark", "RFQ": "file-search", "合": "chart-candlestick", "CFD": "line-chart", "事": "calendar-clock", "期": "scale", "统": "layers", "入": "book-open", "活": "gift", "拟": "package", "US": "building-2", "HK": "landmark", "KR": "flag", "JP": "flag", "合约": "chart-candlestick", "代币": "coins", "IPO": "rocket", "DeFi": "banknote", "组合": "chart-pie", "基金": "piggy-bank", "矿池": "pickaxe", "闻": "newspaper", "善": "heart-handshake", "C2C": "users", "讯": "megaphone", "AI": "bot-message-square", "邀": "user-plus", "证": "shield-check", "FX": "arrow-left-right", "指数": "line-chart", "商品": "package", "金": "gem", "签": "calendar-check", "任": "list-checks", "活动": "ticket", "股票": "chart-line", "交割": "calendar-clock", "永续": "chart-candlestick"};
function vexusIconSvg(name) {
  var p = VEXUS_ICON_PATHS[name] || VEXUS_ICON_PATHS['circle'] || '';
  return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">' + p + '</svg>';
}
  if (window.__vexusNavInited) return;
  window.__vexusNavInited = true;

  var lang = localStorage.getItem('localLan') || 'en';
  var zh = lang === 'zh-CN' || lang === 'CN' || lang === 'cht';
  var L = zh ? {
    buy: '买币', futures: '合约', stocks: '股票', community: '币圈', ai: 'AI',
    more: '更多', tradfi: 'TradFi', rewards: '福利中心', earn: '理财', mining: '矿池锁仓',
    spot: '现货交易', flash: '闪兑', c2c: 'C2C', fund: '基金理财', promote: '推广中心',
    soon: '该板块正在开发中，敬请期待'
  } : {
    buy: 'Buy Crypto', futures: 'Futures', stocks: 'Stocks', community: 'Community', ai: 'AI',
    more: 'More', tradfi: 'TradFi', rewards: 'Rewards', earn: 'Earn', mining: 'Mining Pool',
    spot: 'Spot', flash: 'Flash Exchange', c2c: 'C2C', fund: 'Fund Management', promote: 'Promotion',
    soon: 'This module is under development'
  };

  function mark(el) {
    el.setAttribute('data-v-33877ad3', '');
    var kids = el.children || [];
    for (var i = 0; i < kids.length; i++) mark(kids[i]);
    return el;
  }

  function toast(msg) {
    var t = document.getElementById('vexus-nav-toast');
    if (!t) {
      t = document.createElement('div');
      t.id = 'vexus-nav-toast';
      document.body.appendChild(t);
    }
    t.textContent = msg;
    t.classList.add('show');
    clearTimeout(window.__vexusNavToastTimer);
    window.__vexusNavToastTimer = setTimeout(function () { t.classList.remove('show'); }, 2400);
  }

  function go(path) {
    location.hash = '#' + path;
    setTimeout(function () { window.scrollTo(0, 0); }, 50);
  }

  function item(label, cls) {
    var d = document.createElement('div');
    d.className = 'dropdown vexus-nav-item ' + (cls || '');
    d.setAttribute('data-vexus-nav', '1');
    var b = document.createElement('button');
    b.className = 'dropbtn';
    b.textContent = label;
    d.appendChild(b);
    return mark(d);
  }

  var TRADE_MENU = [
    { title: zh ? '基础交易' : 'Basic', items: [
      { t: zh ? '现货交易' : 'Spot', d: zh ? '代币兑换，零费用，无滑点' : 'Token exchange, zero fee, no slippage', i: '现', go: '/trade/btc' },
      { t: zh ? '交割合约' : 'Delivery Futures', d: zh ? '简单操作，专业交易' : 'Simple and professional trading', i: '交割', go: '/trade/btc?RouterName=delivery' },
      { t: zh ? '永续合约' : 'Perpetual Futures', d: zh ? '数字货币，专业交易' : 'Digital currency trading', i: '永续', go: '/trade/btc?RouterName=sustainable' },
      { t: '打金狗', d: zh ? '查看链上代币行情' : 'On-chain token market', i: '链', go: '/market' },
      { t: '杠杆交易', d: zh ? '了解杠杆现货交易' : 'Leveraged spot trading', i: '杠', go: '/trade/btc' },
      { t: '闪兑与定投', d: zh ? '查看币种兑换报价' : 'Convert and auto-invest', i: '闪', go: '/exchange' },
      { t: 'ETF', d: zh ? '查看杠杆 ETF 产品' : 'Leveraged ETF products', i: 'ETF', href: '/tradfi/' },
      { t: '盘前交易', d: zh ? '了解上市前交易' : 'Pre-market trading', i: '盘', href: '/stocks/global/' }
    ]},
    { title: zh ? '进阶交易' : 'Advanced', items: [
      { t: 'Vexus Web3', d: zh ? '查看链上市场与钱包工具' : 'On-chain market and wallet tools', i: 'W3', go: '/wallet' },
      { t: zh ? '交易机器人' : 'Trading Bot', d: zh ? '创建和管理自动交易策略' : 'Automated trading strategies', i: '机', href: '/paper/' },
      { t: '跟单', d: zh ? '发现交易员与跟单策略' : 'Copy trading strategies', i: '跟', go: '/promote' },
      { t: '跨所交易', d: zh ? '了解跨平台交易' : 'Cross-exchange trading', i: '跨', tag: 'New', go: '/exchange' },
      { t: 'OTC 银行转账', d: zh ? '了解银行转账买卖币' : 'Buy/sell via bank transfer', i: 'OTC', go: '/c2c' },
      { t: 'OTC RFQ', d: zh ? '了解大宗交易询价' : 'Block trade quotation', i: 'RFQ', go: '/c2c' }
    ]}
  ];

  var FUTURES_MENU = [
    { title: zh ? '合约交易' : 'Futures', items: [
      { t: '合约', d: zh ? '永续合约市场' : 'Perpetual futures market', i: '合', go: '/trade/btc?RouterName=sustainable' },
      { t: 'CFD 合约', d: zh ? '一站式交易全球传统资产' : 'Trade global traditional assets', i: 'CFD', tag: zh ? '黄金' : 'Gold', href: '/tradfi/' },
      { t: '事件合约', d: zh ? '预测涨跌方向，把握市场机会' : 'Predict market direction', i: '事', tag: 'New', href: '/tradfi/' },
      { t: '期权', d: zh ? '欧式 T 型报价，丰富的到期日和行权价' : 'European options', i: '期', tag: 'Hot', href: '/tradfi/' },
      { t: '统一账户', d: zh ? '实现最大化资金效率' : 'Unified trading account', i: '统', go: '/contractAccounts' },
      { t: '合约入门', d: zh ? '从零开始掌握合约交易技巧' : 'Futures trading guide', i: '入', go: '/help' },
      { t: '合约活动', d: zh ? '查看合约活动与参与规则' : 'Futures campaigns', i: '活', href: '/events/' },
      { t: '模拟交易', d: zh ? '使用模拟资金，体验交易' : 'Paper trading', i: '拟', href: '/paper/' }
    ]}
  ];

  var TRADFI_MENU = [
    { title: 'TradFi', items: [
      { t: zh ? '外汇' : 'Forex', d: zh ? '全球主要货币对交易' : 'Major global currency pairs', i: 'FX', href: '/tradfi/' },
      { t: zh ? '全球指数' : 'Global Indices', d: zh ? '美股/港股/欧洲主要指数' : 'US, HK and Europe indices', i: '指数', href: '/tradfi/' },
      { t: zh ? '大宗商品' : 'Commodities', d: zh ? '黄金、原油等商品行情' : 'Gold, oil and commodities', i: '商品', href: '/tradfi/' },
      { t: zh ? '黄金' : 'Gold', d: zh ? '现货黄金与贵金属' : 'Spot gold and metals', i: '金', href: '/tradfi/' },
      { t: zh ? '美股' : 'US Stocks', d: zh ? '美股市场热门标的' : 'Popular US equities', i: 'US', href: '/stocks/' },
      { t: zh ? '市场资讯' : 'Market News', d: zh ? '全球市场新闻与数据' : 'Global market news and data', i: '讯', href: '/news/' }
    ]}
  ];

  var AI_MENU = [
    { title: 'AI', items: [
      { t: zh ? 'AI 客服' : 'AI Support', d: zh ? '智能问答与平台使用指引' : 'Smart Q&A and platform guidance', i: 'AI', action: 'ai' },
      { t: zh ? '交易机器人' : 'Trading Bot', d: zh ? '智能策略，自动交易' : 'Automated trading strategies', i: '机', href: '/paper/' },
      { t: '跟单', d: zh ? '跟随专家实现收益增长' : 'Copy expert strategies', i: '跟', go: '/promote' },
      { t: zh ? '分享好友' : 'Invite Friends', d: zh ? '邀请好友，共享返佣' : 'Invite friends and earn rewards', i: '邀', go: '/promote' },
      { t: zh ? '合约入门' : 'Futures Guide', d: zh ? '从零开始掌握合约交易技巧' : 'Learn futures trading', i: '入', go: '/help' },
      { t: zh ? '储备金证明' : 'Proof of Reserves', d: zh ? '客户负债与链上储备对照' : 'Liabilities vs on-chain reserves', i: '证', href: '/reserves/' }
    ]}
  ];

  var WELFARE_MENU = [
    { title: zh ? '福利中心' : 'Rewards', items: [
      { t: zh ? '推广中心' : 'Promotion Center', d: zh ? '邀请返佣与推广数据' : 'Referral and promotion center', i: '邀', go: '/promote' },
      { t: zh ? '分享好友' : 'Invite Friends', d: zh ? '邀请好友，获得返佣奖励' : 'Invite friends and earn commission', i: '邀', go: '/promote' },
      { t: zh ? '合约活动' : 'Futures Campaigns', d: zh ? '查看合约活动与参与规则' : 'Futures events and rules', i: '合约', href: '/events/' },
      { t: zh ? '股票活动' : 'Stock Campaigns', d: zh ? '查看股票活动与参与规则' : 'Stock events and rules', i: '股票', href: '/events/' },
      { t: zh ? '每日签到' : 'Daily Check-in', d: zh ? '每日签到领取积分奖励' : 'Daily rewards', i: '签', href: '/events/' },
      { t: zh ? '任务中心' : 'Task Center', d: zh ? '完成任务领取福利' : 'Complete tasks for rewards', i: '任', href: '/events/' }
    ]}
  ];

  var COMMUNITY_MENU = [
    { title: zh ? '币圈' : 'Community', items: [
      { t: zh ? '更多新闻' : 'More News', d: zh ? '全网交易市场数据' : 'Market-wide news and data', i: '闻', href: '/news/' },
      { t: zh ? '慈善' : 'Charity', d: zh ? '帮助贡献，回馈社会' : 'Give back to the community', i: '善', href: '/news/' },
      { t: 'C2C', d: zh ? '点对点买卖加密货币' : 'Peer-to-peer crypto market', i: 'C2C', go: '/c2c' },
      { t: zh ? '行情资讯' : 'Market News', d: zh ? '查看市场行情与公告' : 'Markets and announcements', i: '讯', href: '/news/' }
    ]}
  ];

  var EARN_MENU = [
    { title: zh ? '理财' : 'Earn', items: [
      { t: 'DeFi 借贷', d: zh ? '借贷交易，放大收益' : 'Lending and borrowing', i: 'DeFi', go: '/fundMa' },
      { t: zh ? '投资组合' : 'Portfolio', d: zh ? '涉及两种加密货币的组合收益' : 'Dual-asset structured yield', i: '组合', go: '/financialAccounts' },
      { t: zh ? '基金理财' : 'Fund Management', d: zh ? '活期/定期理财与申购' : 'Flexible and fixed-term products', i: '基金', go: '/fundMa' },
      { t: zh ? '矿池锁仓' : 'Mining Pool', d: zh ? '锁仓获取矿池收益' : 'Lock assets for mining rewards', i: '矿池', go: '/trade/btc' }
    ]}
  ];

  var STOCKS_MENU = [
    { title: zh ? '股票市场' : 'Stocks', items: [
      { t: 'CFD', d: zh ? '股票差价合约衍生产品' : 'Stock CFD derivatives', i: 'CFD', href: '/tradfi/' },
      { t: zh ? '美股' : 'US Stocks', d: zh ? '美股市场与热门股票 ETF' : 'US market, stocks and ETFs', i: 'US', href: '/stocks/' },
      { t: zh ? '港股' : 'HK Stocks', d: zh ? '香港股票市场与资产信息' : 'Hong Kong stock market', i: 'HK', href: '/stocks/' },
      { t: zh ? '韩股' : 'KR Stocks', d: zh ? '韩国股票市场与热门标的' : 'Korea stock market', i: 'KR', tag: zh ? 'SK海力士' : 'SK Hynix', href: '/stocks/global/' },
      { t: zh ? '日股' : 'JP Stocks', d: zh ? '热门日股一站汇聚，了解日股市场' : 'Japan stock market', i: 'JP', href: '/stocks/global/' },
      { t: zh ? '股票合约' : 'Stock Futures', d: zh ? '高杠杆，7×24 连续交易' : 'Leveraged 24/7 trading', i: '合约', tag: '7×24h', href: '/stocks/global/' },
      { t: zh ? '股票活动' : 'Stock Campaigns', d: zh ? '查看股票活动与参与规则' : 'Stock campaigns and rules', i: '活动', href: '/events/' },
      { t: zh ? '股票代币' : 'Tokenized Stocks', d: zh ? '锚定真实股票资产发行' : 'Tokenized real-world equities', i: '代币', href: '/stocks/global/' },
      { t: zh ? '直通 IPO' : 'IPO Access', d: zh ? '了解全球股票发行市场' : 'Global IPO market access', i: 'IPO', href: '/news/' }
    ]}
  ];

  function megaItem(x) {
    var d = document.createElement('div');
    d.className = 'vexus-mega-item';
    d.setAttribute('data-v-33877ad3', '');
    var icon = document.createElement('div');
    icon.className = 'vexus-mega-icon';
    var iconName = VEXUS_ICON_BY_LABEL[x.i] || 'circle';
    icon.innerHTML = vexusIconSvg(iconName);
    var txt = document.createElement('div');
    txt.className = 'vexus-mega-text';
    var title = document.createElement('div');
    title.className = 'vexus-mega-title';
    title.textContent = x.t;
    if (x.tag) {
      var tag = document.createElement('span');
      tag.className = 'vexus-mega-tag ' + (x.tag === 'Hot' ? 'is-hot' : x.tag === 'New' ? 'is-new' : 'is-gold');
      tag.textContent = x.tag;
      title.appendChild(tag);
    }
    var desc = document.createElement('div');
    desc.className = 'vexus-mega-desc';
    desc.textContent = x.d || '';
    txt.appendChild(title);
    txt.appendChild(desc);
    d.appendChild(icon);
    d.appendChild(txt);
    d.addEventListener('click', function () {
      if (x.action === 'ai') {
        var chatBtn = document.querySelector('.service-box img');
        if (chatBtn) { chatBtn.click(); } else { toast(zh ? '在线客服加载中，请稍后' : 'Support is loading'); }
        return;
      }
      if (x.soon) { toast(L.soon); return; }
      if (x.href) { location.href = x.href; return; }
      if (x.go) { go(x.go); }
    });
    return d;
  }

  function makeMega(cols, single) {
    var wrap = document.createElement('div');
    wrap.className = 'vexus-mega' + (single ? ' is-single' : '');
    cols.forEach(function (col) {
      var c = document.createElement('div');
      c.className = 'vexus-mega-col';
      var h = document.createElement('div');
      h.className = 'vexus-mega-col-title';
      h.textContent = col.title;
      c.appendChild(h);
      col.items.forEach(function (x) { c.appendChild(megaItem(x)); });
      wrap.appendChild(c);
    });
    return wrap;
  }

  function attachMega(dropdown, cols, single) {
    if (!dropdown) return;
    var content = dropdown.querySelector('.dropdown-content');
    if (!content) {
      content = document.createElement('div');
      content.className = 'dropdown-content';
      dropdown.appendChild(content);
    }
    content.className = 'dropdown-content vexus-mega-wrap';
    content.innerHTML = '';
    content.appendChild(makeMega(cols, single));
    mark(content);
    dropdown.setAttribute('data-vexus-mega', '1');
  }

  function closeNavMenus() {
    var open = document.querySelectorAll('.vexus-open');
    for (var i = 0; i < open.length; i++) open[i].classList.remove('vexus-open');
  }

  function bindClickMenu(dropdown) {
    if (!dropdown || dropdown.getAttribute('data-vexus-click') === '1') return;
    dropdown.setAttribute('data-vexus-click', '1');
    var btn = dropdown.querySelector('.dropbtn');
    if (!btn) return;
    btn.addEventListener('click', function (e) {
      e.preventDefault();
      e.stopPropagation();
      var wasOpen = dropdown.classList.contains('vexus-open');
      closeNavMenus();
      if (!wasOpen) dropdown.classList.add('vexus-open');
      btn.blur();
    });
  }

  function build() {
    var header = document.querySelector('.page-header-content.page-header-box');
    if (!header) return false;
    if (header.querySelector('[data-vexus-nav]')) return true;

    var direct = [];
    for (var i = 0; i < header.children.length; i++) {
      var c = header.children[i];
      if (c.classList && c.classList.contains('dropdown')) direct.push(c);
    }
    var first = direct[0] || null;
    var transaction = direct[2] || direct[direct.length - 1] || null;
    var left = header.querySelector('.left-page-header');
    var right = header.querySelector('.right-page-header');

    // buy crypto before Markets
    var buy = item(L.buy);
    buy.querySelector('.dropbtn').addEventListener('click', function () { go('/purchase'); });
    if (first) header.insertBefore(buy, first); else if (left) header.insertBefore(buy, left); else header.appendChild(buy);

    // futures / stocks after Transaction
    var futures = item(L.futures, 'vexus-mega-drop');
    futures.querySelector('.dropbtn').insertAdjacentHTML('beforeend', '<span class="icon-arrow-down"></span>');
    attachMega(futures, FUTURES_MENU, true);
    var stocks = item(L.stocks, 'vexus-mega-drop');
    stocks.querySelector('.dropbtn').insertAdjacentHTML('beforeend', '<span class="icon-arrow-down"></span>');
    attachMega(stocks, STOCKS_MENU, true);
    if (transaction && transaction.nextSibling) {
      header.insertBefore(stocks, transaction.nextSibling);
      header.insertBefore(futures, stocks);
    } else {
      header.appendChild(futures);
      header.appendChild(stocks);
    }
    // Replace the existing Transaction dropdown with the full two-column menu
    attachMega(transaction, TRADE_MENU, false);

    // community / AI / More / TradFi / rewards before existing fund+mining area
    var group = document.createDocumentFragment();
    var earn = item(L.earn, 'vexus-mega-drop');
    earn.querySelector('.dropbtn').insertAdjacentHTML('beforeend', '<span class="icon-arrow-down"></span>');
    attachMega(earn, EARN_MENU, true);
    var community = item(L.community, 'vexus-mega-drop');
    community.querySelector('.dropbtn').insertAdjacentHTML('beforeend', '<span class="icon-arrow-down"></span>');
    attachMega(community, COMMUNITY_MENU, true);
    var ai = item(L.ai, 'vexus-nav-ai vexus-mega-drop');
    ai.querySelector('.dropbtn').insertAdjacentHTML('beforeend', '<span class="icon-arrow-down"></span>');
    attachMega(ai, AI_MENU, true);

    var tradfi = item(L.tradfi, 'vexus-mega-drop');
    tradfi.querySelector('.dropbtn').insertAdjacentHTML('beforeend', '<span class="icon-arrow-down"></span>');
    attachMega(tradfi, TRADFI_MENU, true);
    var rewards = item(L.rewards, 'vexus-mega-drop');
    rewards.querySelector('.dropbtn').insertAdjacentHTML('beforeend', '<span class="icon-arrow-down"></span>');
    attachMega(rewards, WELFARE_MENU, true);

    [earn, community, ai, tradfi, rewards].forEach(function (el) {
      if (left) header.insertBefore(el, left); else header.appendChild(el);
    });
    [transaction, futures, stocks, earn, community, ai, tradfi, rewards].forEach(bindClickMenu);
    if (!window.__vexusNavOutsideBound) {
      window.__vexusNavOutsideBound = true;
      document.addEventListener('click', function (e) {
        if (!e.target || !e.target.closest || !e.target.closest('.dropdown')) closeNavMenus();
      });
      // only one menu may stay open; hovering another nav item closes the previous click-open menu
      document.addEventListener('mouseover', function (e) {
        var target = e.target;
        var d = target && target.closest ? target.closest('.dropdown') : null;
        var open = document.querySelectorAll('.dropdown.vexus-open');
        if (d) {
          for (var i = 0; i < open.length; i++) {
            if (open[i] !== d) open[i].classList.remove('vexus-open');
          }
        } else if (target && target.closest && !target.closest('.page-header-content')) {
          closeNavMenus();
        }
      });
    }
    // fragment unused, keep for compatibility
    return true;
  }

  var tries = 0;
  var timer = setInterval(function () {
    tries++;
    if (build() || tries > 40) clearInterval(timer);
  }, 250);

  // Vue may re-render the header on language change/route; re-add if removed
  var mo = new MutationObserver(function () {
    if (!document.querySelector('[data-vexus-nav]')) build();
  });
  var root = document.getElementById('app') || document.body;
  mo.observe(root, { childList: true, subtree: true });
})();
