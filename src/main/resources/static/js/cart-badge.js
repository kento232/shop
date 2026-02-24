/* cart-badge.js */
(function(global) {
	'use strict';


	// 設定（必要に応じて変更）

	var CONFIG = {
		countUrl: '/cart/api/count',
		//addUrl: '/api/cart/items',
		badgeSelector: '#cart-count-badge',
		addButtonSelector: '',  // 既存のクラスならここを置換
		qtyAttr: 'data-qty',
		productIdAttr: 'data-product-id',
		showZero: false,       // 0 のとき非表示（false）/ 表示（true）
		capText: '99+',        // 100以上の表示
		capAt: 99,
		useHiddenAttribute: true, // true: hidden属性で制御 / false: style.visibility
		includeCredentials: true, // Cookie/Session を送る
		retryCount: 1,         // 失敗時の再試行回数
		retryDelayMs: 400
	};


	// ユーティリティ

	function $(sel) { return document.querySelector(sel); }
	function $all(sel) { return Array.prototype.slice.call(document.querySelectorAll(sel)); }

	function getCsrf() {
		var tokenEl = document.querySelector('meta[name="_csrf"]');
		var headerEl = document.querySelector('meta[name="_csrf_header"]');
		return tokenEl && headerEl ? { header: headerEl.getAttribute('content'), token: tokenEl.getAttribute('content') } : null;
	}

	function sleep(ms) { return new Promise(function(r) { setTimeout(r, ms); }); }

	function request(url, options, attempt) {
		if (attempt === void 0) attempt = 0;

		options = options || {};
		if (CONFIG.includeCredentials) {
			options.credentials = 'include';
		}

		var headers = options.headers || {};
		headers['Content-Type'] = headers['Content-Type'] || 'application/json';

		// CSRF
		var csrf = getCsrf();
		if (csrf && !headers[csrf.header]) {
			headers[csrf.header] = csrf.token;
		}
		options.headers = headers;

		return fetch(url, options).then(function(res) {
			if (!res.ok) {
				// 再試行
				if (attempt < CONFIG.retryCount && (res.status >= 500 || res.status === 0)) {
					return sleep(CONFIG.retryDelayMs).then(function() {
						return request(url, options, attempt + 1);
					});
				}
				var err = new Error('HTTP ' + res.status);
				err.response = res;
				throw err;
			}
			return res.json().catch(function() {
				// JSONでない場合
				return {};
			});
		}).catch(function(err) {
			if (attempt < CONFIG.retryCount) {
				return sleep(CONFIG.retryDelayMs).then(function() {
					return request(url, options, attempt + 1);
				});
			}
			throw err;
		});
	}


	// バッジ描画・制御

	function setBadgeVisibility(badgeEl, visible) {
		if (!badgeEl) return;
		if (CONFIG.useHiddenAttribute) {
			if (visible) {
				badgeEl.removeAttribute('hidden');
			} else {
				badgeEl.setAttribute('hidden', 'hidden');
			}
		} else {
			badgeEl.style.visibility = visible ? 'visible' : 'hidden';
		}
	}

	function renderCount(count) {
		var badgeEl = $(CONFIG.badgeSelector);
		if (!badgeEl) return;

		var num = parseInt(count, 10);
		if (isNaN(num) || num < 0) { num = 0; }

		var text = (num > CONFIG.capAt) ? CONFIG.capText : String(num);
		badgeEl.textContent = text;

		var visible = CONFIG.showZero ? true : (num > 0);
		setBadgeVisibility(badgeEl, visible);
	}


	// API 呼び出し

	function fetchCountAndRender() {
		return request(CONFIG.countUrl, { method: 'GET' })
			.then(function(json) {
				renderCount(json && typeof json.count !== 'undefined' ? json.count : 0);
				return json;
			})
			.catch(function(err) {
				// 初期描画失敗時はバッジを非表示に
				renderCount(0);
				console.warn('Failed to fetch cart count:', err && err.message ? err.message : err);
			});
	}

	function addToCart(productId, qty) {
		if (!productId) return Promise.resolve();
		qty = (qty && qty > 0) ? qty : 1;

		var body = JSON.stringify({ productId: Number(productId), quantity: Number(qty) });
		return request(CONFIG.addUrl, { method: 'POST', body: body })
			.then(function(json) {
				// サーバーが最新 count を返す前提
				if (json && typeof json.count !== 'undefined') {
					renderCount(json.count);
				} else {
					// 念のため再取得
					return fetchCountAndRender();
				}
			})
			.catch(function(err) {
				alert('カート追加に失敗しました。時間をおいて再度お試しください。');
				console.error('addToCart error:', err);
			});
	}


	// イベント・初期化

	function bindAddButtons() {
		if (!CONFIG.addButtonSelector) return;
		$all(CONFIG.addButtonSelector).forEach(function(btn) {
			if (btn.__cartBound) return;
			btn.__cartBound = true;

			btn.addEventListener('click', function(e) {
				// 二重連打防止
				if (btn.disabled) return;
				btn.disabled = true;

				var pid = btn.getAttribute(CONFIG.productIdAttr);
				var qtyAttr = btn.getAttribute(CONFIG.qtyAttr);
				var qty = qtyAttr ? parseInt(qtyAttr, 10) : 1;

				addToCart(pid, qty).finally(function() {
					// 少し待って解除（APIの連打を抑制）
					setTimeout(function() { btn.disabled = false; }, 300);
				});
			});
		});
	}

	function init() {
		// 初期表示
		fetchCountAndRender();
		// 既存ボタンをバインド
		bindAddButtons();

		// 商品カードが増える場合（MutationObserver）
		if ('MutationObserver' in window) {
			var mo = new MutationObserver(function() {
				bindAddButtons();
			});
			mo.observe(document.documentElement || document.body, { childList: true, subtree: true });
		}

		// 外部からリフレッシュしたい場合
		window.addEventListener('cart:refresh', function() {
			fetchCountAndRender();
		});
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init);
	} else {
		init();
	}


	// 公開API

	var CartBadge = {
		refresh: fetchCountAndRender,
		add: addToCart,
		render: renderCount,
		config: CONFIG
	};

	if (typeof module !== 'undefined' && module.exports) {
		module.exports = CartBadge;
	} else {
		global.CartBadge = CartBadge;
	}
	

})(this);