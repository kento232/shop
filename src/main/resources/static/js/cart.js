// cart.js
class CartQtyUpdater {
	constructor(options = {}) {
		// オプション（必要に応じて拡張可）
		this.endpointBase = options.endpointBase || '/cart/api/items'; // CartController配下のAPI
		this.qtySelector = options.qtySelector || '.js-cart-qty';
		this.itemSelector = options.itemSelector || '.item';

		// CSRF（Thymeleaf×Spring Security）
		this.csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
		this.csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

		this.bind();
	}

	bind() {
		document.querySelectorAll(this.qtySelector).forEach(sel => {
			sel.addEventListener('change', (e) => this.onChange(e));
		});
	}

	format(num) {
		return (num ?? 0).toLocaleString('ja-JP');
	}

	async onChange(e) {
		const itemEl = e.target.closest(this.itemSelector);
		const productId = itemEl?.getAttribute('data-product-id');
		const quantity = parseInt(e.target.value, 10);

		if (!productId || !Number.isFinite(quantity)) return;

		try {
			const res = await fetch(`${this.endpointBase}/${encodeURIComponent(productId)}/quantity`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					...(this.csrfToken && this.csrfHeader ? { [this.csrfHeader]: this.csrfToken } : {})
				},
				body: JSON.stringify({ quantity })
			});

			if (!res.ok) {
				const msg = await res.text();
				throw new Error(msg || '数量更新に失敗しました');
			}

			const data = await res.json();

			// 行小計（表示している場合のみ）
			const line = itemEl.querySelector('.line-subtotal');
			if (line && typeof data.itemSubtotal === 'number') {
				line.textContent = this.format(data.itemSubtotal);
			}

			// サマリー（小計・送料・合計）を反映
			const sub = document.getElementById('summary-subtotal');
			const shp = document.getElementById('summary-shipping');
			const ttl = document.getElementById('summary-total');
			if (sub) sub.textContent = this.format(data.cartSubtotal);
			if (shp) shp.textContent = this.format(data.shipping);
			if (ttl) ttl.textContent = this.format(data.cartTotal);

			// ヘッダーのカート件数（あれば）
			const badge = document.querySelector('.cart-count-badge, .js-cart-count');
			if (badge && typeof data.cartCount === 'number') {
				badge.textContent = this.format(data.cartCount);
			}
			window.dispatchEvent(new Event('cart:refresh'));

		} catch (err) {
			alert(err.message || '数量更新に失敗しました');
			// 必要であれば、元の値に戻す等のロールバックをここに実装
		}
	}
	async updateCartQuantity(productId, quantity, itemEl){
		try{
			const res  = await fetch(`${this.endpointBase}/${encodeURIComponent(productId)}/quantity`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					...(this.csrfToken && this.csrfHeader ? { [this.csrfHeader]: this.csrfToken } : {})
				},
				body : JSON.stringify({ quantity})
			});
			if(!res.ok){
				const msg = await res.text();
				throw new Error(msg || '数量更新に失敗しました');
			}
			
			const data = await res.json();
			
			const line = itemEl.querySelector('.line-subtotal');
			if (line && typeof data.itemSubtotal === 'number'){
				line.textContent = this.format(data.itemSubtotal);
			}
			const sub = document.getElementById('summary-subtotal');
			const shp = document.getElementById('summary-shipping');
			const ttl = document.getElementById('summary-total');
			if(sub) sub.textContent = this.format(data.cartSubtotal);
			if(shp) shp.textContent = this.format(data.shipping);
			if(ttl) ttl.textContent = this.format(data.cartTotal);
			
			window.dispatchEvent(new Event('cart:refresh'));
		}catch(err){
			alert(err.message || '数量更新に失敗しました');
		}
	}
}

// 自動起動（cart.html にこのスクリプトが読み込まれたとき
document.addEventListener('DOMContentLoaded', () => {
	new CartQtyUpdater();
});
