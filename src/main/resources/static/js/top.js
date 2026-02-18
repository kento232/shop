// top.js
// ページ遷移なしでカテゴリ

const $ = (sel) => document.querySelector(sel);

const keywordInput = $('#keywordInput');       // <input id="keywordInput">
const categorySelect = $('#categorySelect');   // <select id="categorySelect">
const searchButton = $('#searchButton');       // <button id="searchButton">検索</button>（無ければ null でOK）

const productGrid = $('#productGrid');         // <div id="productGrid">
const messageArea = $('#messageArea');         // <div id="messageArea">

const prevBtn = $('#prevBtn');                 // <button id="prevBtn">
const nextBtn = $('#nextBtn');                 // <button id="nextBtn">
const pageInfo = $('#pageInfo');               // <span id="pageInfo">

// Spring Security CSRF（POSTフォーム用、例：カート追加）
const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
const csrfParam = document.querySelector('meta[name="_csrf_parameter"]')?.content || '_csrf';
const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
// ページング状態
let currentPage = 1;
const pageSize = 20;

// ユーティリティ 
function escapeHtml(str) {
	return String(str ?? '').replace(/[&<>"']/g, s => ({
		'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
	}[s]));
}

function formatYen(n) {
	const num = Number(n || 0);
	return num.toLocaleString('ja-JP') + ' 円';
}

function setLoading(on) {
	if (!productGrid) return;
	productGrid.classList.toggle('loading', !!on);
}

// API 呼び出し 
async function fetchProducts(page = 1) {
	const params = new URLSearchParams();
	const kw = keywordInput?.value?.trim();
	const cat = categorySelect?.value;

	if (kw) params.set('keyword', kw);
	if (cat) params.set('categoryId', cat);
	params.set('page', String(page));
	params.set('size', String(pageSize));

	const url = `/api/products/search?${params.toString()}`;

	setLoading(true);
	if (messageArea) messageArea.textContent = '';

	try {
		const res = await fetch(url, { credentials: 'same-origin' });
		if (!res.ok) {
			throw new Error('検索に失敗しました（サーバエラー）');
		}
		const data = await res.json();
		renderProducts(data.items || []);

		// 更新
		currentPage = data.page || 1;
		if (pageInfo) pageInfo.textContent = String(currentPage);
		if (prevBtn) prevBtn.disabled = currentPage <= 1;
		if (nextBtn) nextBtn.disabled = !data.hasNext;

		// 件数0メッセージ
		if (!data.items || data.items.length === 0) {
			if (messageArea) messageArea.textContent = '該当する商品がありません。';
		}
	} catch (e) {
		console.error(e);
		if (messageArea) {
			messageArea.innerHTML = `<span class="error">${escapeHtml(e.message || '検索に失敗しました')}</span>`;
		}
	} finally {
		setLoading(false);
	}
}

//カテゴリ一覧をAPIから再取得したい場合に使用
async function fetchCategories() {
	try {
		const res = await fetch('/api/categories', { credentials: 'same-origin' });
		if (!res.ok) return;
		const cats = await res.json();
		if (!categorySelect) return;

		const current = categorySelect.value;
		categorySelect.innerHTML = '<option value="">すべてのカテゴリー</option>';
		for (const c of cats) {
			const opt = document.createElement('option');
			opt.value = c.id;
			opt.textContent = c.name;
			categorySelect.appendChild(opt);
		}
		// 選択状態を維持
		if (current) categorySelect.value = current;
	} catch (e) {
		console.warn('カテゴリ取得に失敗:', e);
	}
}

//描画 

function renderProducts(items) {
	if (!productGrid) return;
	productGrid.innerHTML = '';

	for (const it of items) {
		const id = escapeHtml(it.product_id);
		const name = escapeHtml(it.product_name);
		const img = escapeHtml(it.product_image);
		const price = Number(it.product_price || 0);
		const discount = Number(it.product_discount_price || 0);
		const now = price - discount;

		const article = document.createElement('article');
		article.className = 'product-card';
		article.innerHTML = `
      <a class="product-thumb" href="/products/${id}">
        <img src="/images/products/${img}" alt="${name}" loading="lazy">
      </a>
      <h3 class="name">${name}</h3>
      <div class="price-area">
        ${discount > 0 ? `<span class="price-old">${formatYen(price)}</span>` : ''}
        <span class="price-now">${formatYen(now)}</span>

</div>
      <form action="/cart/add" method="post">
        <input type="hidden" name="productId" value="${id}">
        <input type="hidden" name="qty" value="1">
        <input type="hidden" name="_csrf" value="${csrfToken}">
        <!-- hidden CSRF は SSR がないケースに備え、meta 補完するので未挿入でもOK -->
        <button
          type="submit"
          class="js-add-to-cart-btn"
          data-product-id="${id}"
        >
          カートに追加
        </button>
      </form>
    `;
		productGrid.appendChild(article);
	}
}
// カテゴリ変更で即時検索
categorySelect?.addEventListener('change', () => fetchProducts(1));

// 検索ボタン（ある場合）
searchButton?.addEventListener('click', (e) => {
	e.preventDefault();
	fetchProducts(1);
});

// Enter キーで検索（キーワード入力）
keywordInput?.addEventListener('keydown', (e) => {
	if (e.key === 'Enter') {
		e.preventDefault();
		fetchProducts(1);
	}
});

// 入力のデバウンス（タイピング停止後 500ms で検索）
let debounceId = null;
keywordInput?.addEventListener('input', () => {
	if (debounceId) clearTimeout(debounceId);
	debounceId = setTimeout(() => fetchProducts(1), 500);
});


prevBtn?.addEventListener('click', () => {
	if (currentPage > 1) fetchProducts(currentPage - 1);
});
nextBtn?.addEventListener('click', () => {
	fetchProducts(currentPage + 1);
});


document.addEventListener('DOMContentLoaded', () => {

// 初期表示：カテゴリ一覧をロード → 1ページ目の検索
 fetchCategories()
   .then(() => fetchProducts(1))
   .catch((e) => {
     console.warn('初期化でカテゴリ取得に失敗:', e);
     // カテゴリ取得が失敗しても検索自体は実行（キーワードのみなど）
     fetchProducts(1);
   });
});


/**
 * クリックされた .js-add-to-cart-btn の属する form から
 * application/x-www-form-urlencoded で /cart/add にPOSTする
 */
// --- カート追加（document へのイベント委譲で確実に拾う／LOG付き） ---
 

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', bindCartAddHandler);
} else {
  bindCartAddHandler();
}