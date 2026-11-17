(function () {
  'use strict';
  if (window.__vexusPageI18n) return;
  window.__vexusPageI18n = true;

  var SUPPORTED = ['zh-CN', 'zh-TW', 'en', 'ja', 'ko', 'fr', 'de', 'it', 'es', 'pt', 'ru', 'ar', 'tr', 'vi', 'th'];
  var map = {};
  var lang = 'en';

  function currentLang() {
    var stored = localStorage.getItem('vexusLang');
    if (stored && SUPPORTED.indexOf(stored) >= 0) return stored;
    var l = localStorage.getItem('localLan') || 'en';
    var m = { cht: 'zh-TW', jp: 'ja', cn: 'zh-CN' };
    if (m[l]) l = m[l];
    return SUPPORTED.indexOf(l) >= 0 ? l : 'en';
  }

  function applyTextNode(node) {
    if (!node || node.nodeType !== 3) return;
    var p = node.parentNode;
    if (!p) return;
    var tag = p.nodeName;
    if (tag === 'SCRIPT' || tag === 'STYLE' || tag === 'NOSCRIPT' || tag === 'TEXTAREA') return;
    if (p.closest && p.closest('[data-i18n]')) return;
    var raw = node.nodeValue;
    var key = raw.trim();
    if (!key || !map[key]) return;
    var lead = raw.match(/^\s*/)[0];
    var tail = raw.match(/\s*$/)[0];
    var next = lead + map[key] + tail;
    if (next === raw) return;
    node.nodeValue = next;
  }

  function walk(root) {
    if (!root) return;
    if (root.nodeType === 3) {
      applyTextNode(root);
      return;
    }
    if (root.nodeType !== 1) return;
    var walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, null, false);
    var n;
    var nodes = [];
    while ((n = walker.nextNode())) nodes.push(n);
    nodes.forEach(applyTextNode);
  }

  function applyAttrs() {
    document.querySelectorAll('[placeholder], [title], [alt]').forEach(function (el) {
      ['placeholder', 'title', 'alt'].forEach(function (attr) {
        var v = el.getAttribute(attr);
        if (v && map[v.trim()]) {
          el.setAttribute(attr, map[v.trim()]);
        }
      });
    });
  }

  function applyAll() {
    walk(document.body);
    applyAttrs();
    if (document.title && map[document.title.trim()]) {
      document.title = map[document.title.trim()];
    }
  }

  function load() {
    lang = currentLang();
    if (lang === 'zh-CN') return;
    fetch('/locales/' + lang + '/page-texts.json', { cache: 'no-cache' })
      .then(function (r) { if (!r.ok) throw new Error('page texts'); return r.json(); })
      .then(function (data) {
        map = data || {};
        applyAll();
        var mo = new MutationObserver(function (muts) {
          muts.forEach(function (m) {
            if (m.type === 'childList') {
              m.addedNodes.forEach(function (n) { walk(n); });
            } else if (m.type === 'characterData') {
              applyTextNode(m.target);
            }
          });
        });
        mo.observe(document.body, { childList: true, subtree: true, characterData: true });
      })
      .catch(function () {});
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', load);
  } else {
    load();
  }
})();
