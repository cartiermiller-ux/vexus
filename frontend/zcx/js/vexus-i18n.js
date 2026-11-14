(function () {
  'use strict';
  if (window.__vexusI18nInited) return;
  window.__vexusI18nInited = true;

  var SUPPORTED = ['zh-CN', 'zh-TW', 'en', 'ja', 'ko', 'fr', 'de', 'it', 'es', 'pt', 'ru', 'ar', 'tr', 'vi', 'th'];
  var LABELS = {
    'zh-CN': '简体中文', 'zh-TW': '繁體中文', 'en': 'English', 'ja': '日本語', 'ko': '한국어',
    'fr': 'Français', 'de': 'Deutsch', 'it': 'Italiano', 'es': 'Español', 'pt': 'Português',
    'ru': 'Русский', 'ar': 'العربية', 'tr': 'Türkçe', 'vi': 'Tiếng Việt', 'th': 'ไทย'
  };
  var VUE_SUPPORTED = ['zh-CN', 'zh-TW', 'en', 'fr', 'de', 'it', 'ja', 'ko'];

  function currentLang() {
    var stored = localStorage.getItem('vexusLang');
    if (stored && SUPPORTED.indexOf(stored) >= 0) return stored;
    var l = localStorage.getItem('localLan') || 'en';
    var map = { 'cht': 'zh-TW', 'jp': 'ja', 'cn': 'zh-CN', 'CN': 'zh-CN' };
    if (map[l]) l = map[l];
    return SUPPORTED.indexOf(l) >= 0 ? l : 'en';
  }

  function vueLang(code) {
    if (VUE_SUPPORTED.indexOf(code) >= 0) {
      if (code === 'zh-TW') return 'cht';
      if (code === 'ja') return 'jp';
      return code;
    }
    return 'en';
  }

  function fetchLocale(code) {
    return fetch('/locales/' + code + '/common.json', { cache: 'no-cache' }).then(function (r) {
      if (!r.ok) throw new Error('locale ' + code);
      return r.json();
    });
  }

  function applyToDom() {
    if (!window.i18next) return;
    document.querySelectorAll('[data-i18n]').forEach(function (el) {
      var key = el.getAttribute('data-i18n');
      var text = window.i18next.t(key);
      if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
        el.setAttribute('placeholder', text);
      } else {
        el.textContent = text;
      }
    });
    document.dispatchEvent(new CustomEvent('vexus-i18n-ready', { detail: { lang: window.i18next.language } }));
  }

  function addSelector() {
    if (document.getElementById('vexus-lang-select')) return;
    var sel = document.createElement('select');
    sel.id = 'vexus-lang-select';
    sel.setAttribute('aria-label', 'Language');
    SUPPORTED.forEach(function (code) {
      var o = document.createElement('option');
      o.value = code;
      o.textContent = LABELS[code];
      sel.appendChild(o);
    });
    sel.value = currentLang();
    sel.style.cssText = 'height:30px;margin-left:10px;padding:0 6px;border-radius:7px;border:1px solid rgba(255,255,255,.22);' +
      'background:rgba(20,16,38,.75);color:#fff;font-size:12px;outline:none;cursor:pointer;max-width:118px';
    sel.addEventListener('change', function () {
      var code = sel.value;
      localStorage.setItem('vexusLang', code);
      localStorage.setItem('localLan', vueLang(code));
      location.reload();
    });
    var right = document.querySelector('.right-page-header');
    if (right) {
      right.insertBefore(sel, right.firstChild);
    } else {
      sel.style.position = 'fixed';
      sel.style.top = '10px';
      sel.style.right = '12px';
      sel.style.zIndex = '10000';
      document.body.appendChild(sel);
    }
  }

  function init() {
    if (!window.i18next) {
      setTimeout(init, 120);
      return;
    }
    var lang = currentLang();
    var enPromise = fetchLocale('en');
    var langPromise = lang === 'en' ? enPromise : fetchLocale(lang).catch(function () { return null; });
    Promise.all([enPromise, langPromise]).then(function (arr) {
      var en = arr[0];
      var selected = arr[1] || en;
      window.i18next.init({
        lng: lang,
        fallbackLng: 'en',
        ns: ['common'],
        defaultNS: 'common',
        resources: (function () {
          var r = { en: { common: en } };
          r[lang] = { common: selected };
          return r;
        })(),
        interpolation: { escapeValue: false }
      }, function () {
        window.VexusI18n = {
          t: function (k, o) { return window.i18next.t(k, o); },
          lang: lang,
          setLang: function (code) {
            localStorage.setItem('vexusLang', code);
            localStorage.setItem('localLan', vueLang(code));
            location.reload();
          }
        };
        applyToDom();
        addSelector();
      });
    }).catch(function () {
      addSelector();
    });
  }

  init();
})();
