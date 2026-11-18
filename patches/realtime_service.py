import json
import os
import random
import threading
import time

from flask import Flask, Response, jsonify, request

CONFIG_PATH = "/www/wwwroot/vexus/site/realtime-config.json"
DEFAULT_CONFIG = {
    "enabled": True,
    "interval_ms": 1000,
    "instruments": {
        "BTCUSDT": {"type": "crypto", "base": 64500.0, "decimals": 2, "volatility": 0.00035, "spread": 0.0002},
        "ETHUSDT": {"type": "crypto", "base": 1753.0, "decimals": 2, "volatility": 0.00045, "spread": 0.0002},
        "SOLUSDT": {"type": "crypto", "base": 69.21, "decimals": 3, "volatility": 0.0006, "spread": 0.0003},
        "EURUSD": {"type": "forex", "base": 1.09895, "decimals": 5, "volatility": 0.00012, "spread": 0.00012},
        "GBPUSD": {"type": "forex", "base": 1.27400, "decimals": 5, "volatility": 0.00014, "spread": 0.00012},
        "USDJPY": {"type": "forex", "base": 155.20, "decimals": 3, "volatility": 0.00014, "spread": 0.012},
        "AUDUSD": {"type": "forex", "base": 0.65500, "decimals": 5, "volatility": 0.00016, "spread": 0.00012},
        "USDCAD": {"type": "forex", "base": 1.37000, "decimals": 5, "volatility": 0.00014, "spread": 0.00014},
        "USDCHF": {"type": "forex", "base": 0.88500, "decimals": 5, "volatility": 0.00014, "spread": 0.00014},
        "NZDUSD": {"type": "forex", "base": 0.59500, "decimals": 5, "volatility": 0.00016, "spread": 0.00012},
        "EURJPY": {"type": "forex", "base": 170.50, "decimals": 3, "volatility": 0.00016, "spread": 0.015},
    },
}

app = Flask(__name__)
lock = threading.Lock()
quotes = {}
state = {"snapshot": {"time": 0, "quotes": {}}, "interval_ms": 1000, "enabled": True}
config = DEFAULT_CONFIG


def load_config():
    global config
    try:
        with open(CONFIG_PATH, "r", encoding="utf-8") as f:
            data = json.load(f)
        if not data.get("instruments"):
            data["instruments"] = DEFAULT_CONFIG["instruments"]
        config = data
    except Exception:
        config = DEFAULT_CONFIG


def _now():
    return int(time.time())


def _init_quotes():
    for symbol, inst in config.get("instruments", {}).items():
        base = float(inst.get("base", 1.0))
        quotes[symbol] = {"mid": base, "prev": base, "open": base, "high": base, "low": base,
                          "volume": random.randint(1000, 9000)}


def _round(value, decimals):
    return round(float(value), int(decimals))


def _make_book(symbol, mid, decimals, spread, side_seed):
    step = max(spread, 10 ** (-int(decimals)))
    rnd = random.Random(f"{symbol}-{_now()}-{side_seed}")
    asks = []
    bids = []
    for i in range(1, 6):
        asks.append({"price": _round(mid + step * i, decimals), "qty": round(0.1 + rnd.random() * 2.2, 2)})
        bids.append({"price": _round(mid - step * i, decimals), "qty": round(0.1 + rnd.random() * 2.2, 2)})
    return asks, bids


def _make_trades(symbol, mid, decimals, side_seed):
    step = max(10 ** (-int(decimals)), 0.00001)
    rnd = random.Random(f"{symbol}-{_now()}-{side_seed}-trades")
    out = []
    for i in range(10):
        out.append({
            "price": _round(mid + (rnd.random() - 0.5) * step * 5, decimals),
            "qty": round(0.1 + rnd.random() * 2.0, 2),
            "side": "buy" if rnd.random() > 0.45 else "sell",
            "time": time.strftime("%H:%M:%S", time.localtime(_now() - i * 3)),
        })
    return out


def tick_loop():
    global config, quotes
    load_config()
    with lock:
        _init_quotes()
    last_interval = 0
    while True:
        try:
            # Reload config every second so admin changes take effect immediately.
            load_config()
            interval = max(500, min(5000, int(config.get("interval_ms", 1000))))
            enabled = bool(config.get("enabled", True))
            instruments = config.get("instruments", {})
            now = {"time": _now(), "quotes": {}}
            with lock:
                for symbol, inst in instruments.items():
                    q = quotes.get(symbol)
                    if not q:
                        q = {"mid": float(inst.get("base", 1.0)), "prev": float(inst.get("base", 1.0)),
                             "open": float(inst.get("base", 1.0)), "high": float(inst.get("base", 1.0)),
                             "low": float(inst.get("base", 1.0)), "volume": random.randint(1000, 9000)}
                        quotes[symbol] = q
                    base = float(inst.get("base", q["mid"]))
                    decimals = int(inst.get("decimals", 5))
                    volatility = float(inst.get("volatility", 0.0002))
                    spread = float(inst.get("spread", max(10 ** (-decimals), 0.0001)))
                    if enabled:
                        drift = random.uniform(-volatility, volatility)
                        q["mid"] = max(base * 0.9, min(base * 1.1, q["mid"] * (1.0 + drift))) if symbol.endswith("USDT") else max(base * 0.97, min(base * 1.03, q["mid"] + base * drift))
                        q["high"] = max(q["high"], q["mid"])
                        q["low"] = min(q["low"], q["mid"])
                        q["volume"] += random.randint(1, 40)
                    mid = _round(q["mid"], decimals)
                    bid = _round(mid - spread / 2, decimals)
                    ask = _round(mid + spread / 2, decimals)
                    asks, bids = _make_book(symbol, mid, decimals, spread, 1)
                    now["quotes"][symbol] = {
                        "symbol": symbol,
                        "type": inst.get("type", "crypto"),
                        "mid": mid,
                        "bid": bid,
                        "ask": ask,
                        "open": _round(q["open"], decimals),
                        "high": _round(q["high"], decimals),
                        "low": _round(q["low"], decimals),
                        "volume": q["volume"],
                        "change": _round((mid - q["open"]) / q["open"] * 100, 2) if q["open"] else 0,
                        "asks": asks,
                        "bids": bids,
                        "trades": _make_trades(symbol, mid, decimals, 1),
                    }
                state["snapshot"] = now
                state["interval_ms"] = interval
                state["enabled"] = enabled
            time.sleep(interval / 1000.0)
        except Exception:
            time.sleep(1)


@app.route("/health")
def health():
    return jsonify({"status": "ok", "time": _now(), "enabled": state["enabled"], "interval_ms": state["interval_ms"]})


@app.route("/snapshot")
def snapshot():
    with lock:
        data = state["snapshot"]
    return Response(json.dumps(data, ensure_ascii=False), mimetype="application/json",
                    headers={"Cache-Control": "no-store", "Access-Control-Allow-Origin": "*"})


@app.route("/config", methods=["GET", "POST"])
def config_api():
    if request.method == "POST":
        try:
            data = request.get_json(force=True)
            with open(CONFIG_PATH, "w", encoding="utf-8") as f:
                json.dump(data, f, ensure_ascii=False, indent=2)
            load_config()
            return jsonify({"code": "0", "msg": "saved"})
        except Exception as e:
            return jsonify({"code": "1", "msg": str(e)})
    load_config()
    return jsonify(config)


@app.route("/stream")
def stream():
    def generate():
        last = 0
        while True:
            with lock:
                data = state["snapshot"]
                interval = state["interval_ms"]
            if data.get("time") != last:
                last = data.get("time")
                yield "data: " + json.dumps(data, ensure_ascii=False) + "\n\n"
            time.sleep(max(0.25, interval / 1000.0))
    return Response(generate(), mimetype="text/event-stream",
                    headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no",
                             "Access-Control-Allow-Origin": "*"})


if __name__ == "__main__":
    load_config()
    threading.Thread(target=tick_loop, daemon=True).start()
    app.run(host="127.0.0.1", port=5006, threaded=True)
