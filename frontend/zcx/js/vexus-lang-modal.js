(function () {
  'use strict';
  if (window.__vexusLangModal) return;
  window.__vexusLangModal = true;

  var langs = [
    { label: '简体中文', code: 'zh-CN', vue: 'zh-CN' },
    { label: 'English', code: 'en', vue: 'en' },
    { label: '繁体中文', code: 'zh-TW', vue: 'cht' },
    { label: 'Français', code: 'fr', vue: 'fr' },
    { label: 'Deutsch', code: 'de', vue: 'de' },
    { label: 'Italiano', code: 'it', vue: 'it' },
    { label: 'Español', code: 'es', vue: 'en' },
    { label: 'Português', code: 'pt', vue: 'en' },
    { label: 'Русский', code: 'ru', vue: 'en' },
    { label: 'العربية', code: 'ar', vue: 'en' },
    { label: 'Türkçe', code: 'tr', vue: 'en' },
    { label: 'Tiếng Việt', code: 'vi', vue: 'en' },
    { label: 'ไทย', code: 'th', vue: 'en' },
    { label: '日本語', code: 'ja', vue: 'jp' },
    { label: '한국 사람', code: 'ko', vue: 'ko' }
  ];

  var byLabel = {};
  langs.forEach(function (l) { byLabel[l.label] = l; });

  function findBox() {
    var boxes = document.querySelectorAll('.css-cbzd9 .css-q2wk8b');
    for (var i = 0; i < boxes.length; i++) {
      if (boxes[i].querySelector('button')) return boxes[i];
    }
    return null;
  }

  function makeButton(template, label) {
    var b = template.cloneNode(true);
    b.removeAttribute('id');
    b.style.width = '125px';
    b.classList.remove('rest_css-1szzq31');
    var div = b.querySelector('.css-1hv3jiv');
    if (div) div.textContent = label;
    else b.textContent = label;
    var svg = b.querySelector('svg');
    if (svg) svg.style.display = 'none';
    b.addEventListener('click', function (e) {
      e.preventDefault();
      e.stopPropagation();
      var meta = byLabel[label];
      if (!meta) return;
      localStorage.setItem('vexusLang', meta.code);
      localStorage.setItem('localLan', meta.vue);
      location.reload();
    });
    return b;
  }

  function enhance() {
    var box = findBox();
    if (!box) return;
    var btns = Array.prototype.slice.call(box.querySelectorAll('button'));
    var labels = {};
    btns.forEach(function (b) { labels[b.innerText.trim()] = true; });
    if (!box.__vxClick) {
      box.__vxClick = true;
      box.addEventListener('click', function (e) {
        var btn = e.target.closest ? e.target.closest('button') : null;
        if (!btn) return;
        var label = btn.innerText.trim();
        var meta = byLabel[label];
        if (meta) {
          localStorage.setItem('vexusLang', meta.code);
          localStorage.setItem('localLan', meta.vue);
        }
      }, true);
    }
    var template = btns[0];
    if (!template) return;
    langs.forEach(function (l) {
      if (labels[l.label]) return;
      var b = makeButton(template, l.label);
      box.appendChild(b);
      labels[l.label] = true;
    });
  }

  var timer = setInterval(enhance, 600);
  var mo = new MutationObserver(function () { enhance(); });
  mo.observe(document.body, { childList: true, subtree: true });
})();
