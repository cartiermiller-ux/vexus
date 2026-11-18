(function () {
  'use strict';
  if (window.__vexusRealtimeInited) return;
  window.__vexusRealtimeInited = true;

  var store = { quotes: {}, time: 0, connected: false };
  var listeners = [];
  var es = null;
  var pollTimer = null;

  function dispatch() {
    try {
      var ev = new CustomEvent('vexus-realtime', { detail: store });
      document.dispatchEvent(ev);
    } catch (e) {
      var ev2 = document.createEvent('CustomEvent');
      ev2.initCustomEvent('vexus-realtime', true, true, store);
      document.dispatchEvent(ev2);
    }
    listeners.forEach(function (fn) { try { fn(store); } catch (e) {} });
  }

  function applySnapshot(data) {
    if (!data || !data.quotes) return;
    store.quotes = data.quotes;
    store.time = data.time || Math.floor(Date.now() / 1000);
    store.connected = true;
    dispatch();
  }

  function fetchSnapshot() {
    fetch('/realtime/snapshot', { cache: 'no-store' }).then(function (r) { return r.json(); })
      .then(applySnapshot).catch(function () { store.connected = false; });
  }

  function connect() {
    if (!window.EventSource) { fetchSnapshot(); return; }
    try {
      es = new EventSource('/realtime/stream');
      es.onopen = function () { store.connected = true; };
      es.onmessage = function (e) {
        try { applySnapshot(JSON.parse(e.data)); } catch (err) {}
      };
      es.onerror = function () {
        store.connected = false;
        if (es) { es.close(); es = null; }
        setTimeout(connect, 3000);
      };
    } catch (e) {
      fetchSnapshot();
    }
  }

  function startPollingFallback() {
    if (pollTimer) return;
    pollTimer = setInterval(function () {
      if (!store.connected) fetchSnapshot();
    }, 1500);
  }

  window.VexusRealtime = {
    store: store,
    get: function (symbol) { return store.quotes && store.quotes[symbol]; },
    quotes: store.quotes,
    on: function (fn) { listeners.push(fn); return fn; },
    refresh: fetchSnapshot
  };

  fetchSnapshot();
  connect();
  startPollingFallback();
  document.addEventListener('visibilitychange', function () { if (!document.hidden) fetchSnapshot(); });
})();
