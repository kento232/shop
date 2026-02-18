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
        <!-- hidden CSRF は SSR がないケースに備え、meta 補完するので未挿入でもOK -->
        <button
          type="button"
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
function bindCartAddHandler() {
  document.addEventListener('click', async (e) => {
    // クリックが拾えているかの一次確認
    // console.log('[cart] click captured on document', e.target);

    const btn = e.target.closest && e.target.closest('.js-add-to-cart-btn');
    if (!btn) return; // 他のクリックは無視
    // ここに来ていればボタンは拾えている
    console.log('[cart] button matched', btn);

    if (btn.disabled) {
      console.log('[cart] button is disabled → return');
      return;
    }

    const form = btn.closest('form');
    if (!(form instanceof HTMLFormElement)) {
      console.warn('[cart] no form found for button');
      return;
    }

    const action = form.getAttribute('action') || '';
    if (!/\/cart\/add(?:$|\?)/.test(action)) {
      console.warn('[cart] action not match:', action);
      return;
    }

    // UIロック
    btn.disabled = true;
    const originalText = btn.textContent || 'カートに追加';
    btn.textContent = '追加中…';
    btn.classList.add('is-loading');
    btn.setAttribute('aria-busy', 'true');

    try {
      // FormData → x-www-form-urlencoded
      const fd = new FormData(form);

      // CSRF（hidden が無い場合は meta から補完）
      const metaToken = document.querySelector('meta[name="_csrf"]')?.content || '';
      const metaParam  = document.querySelector('meta[name="_csrf_parameter"]')?.content || '_csrf';
      if (metaToken && !fd.has(metaParam)) {
        fd.append(metaParam, metaToken);
      }

      const body = new URLSearchParams();
      for (const [k, v] of fd.entries()) {
        body.append(k, typeof v === 'string' ? v : '');
      }

      // ヘッダ（CSRF はヘッダにも載せる）
      const headers = {
        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
        'Accept': 'application/json',
      };
      const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
      if (metaToken && csrfHeaderName) headers[csrfHeaderName] = metaToken;

      console.log('[cart] POST', action, { headers: { ...headers, 'X-CSRF-TOKEN': undefined }});

      const res = await fetch(action, {
        method: 'POST',
        credentials: 'same-origin',
        headers,
        body
      });

      // 成功/失敗判定
      if (res.status === 403) {
        throw new Error('セキュリティによりブロックされました（CSRF）。ページを再読み込みして再試行してください。');
      }

      if (res.redirected) {
        const u = new URL(res.url, location.origin);
        console.log('[cart] redirected to', u.href);
        if (u.pathname.startsWith('/login')) {
          throw new Error('ログインが必要です。');
        } else if (u.pathname.startsWith('/cart')) {
          // 正常系：フォーム送信相当の 302→/cart
          // Ajax上では成功扱いにして画面はそのまま
        } else {
          throw new Error('想定外の遷移が発生しました。');
        }
      } else if (!res.ok) {
        throw new Error(`カート追加に失敗しました（HTTP ${res.status}）`);
      } else {
        const ct = res.headers.get('content-type') || '';
        if (ct.includes('application/json')) {
          const json = await res.json();
          console.log('[cart] response json', json);
          if (json?.success === false) {
            throw new Error(json?.message || 'カート追加に失敗しました。');
          }
        }
      }

      // 成功UI
      btn.textContent = 'カート追加済み';
      btn.classList.remove('is-loading');
      btn.classList.add('is-added');
      btn.setAttribute('aria-pressed', 'true');
      btn.removeAttribute('aria-busy');
      btn.disabled = true;

      if (messageArea) messageArea.textContent = 'カートに追加しました';
      if (window.CartBadge && typeof window.CartBadge.refresh === 'function') {
        window.CartBadge.refresh();
      }

    } catch (err) {
      console.error('[cart] error', err);
      // 失敗UIへ戻す
      btn.disabled = false;
      btn.classList.remove('is-loading');
      btn.removeAttribute('aria-busy');
      btn.textContent = originalText;
      if (messageArea) {
        messageArea.innerHTML = `<span class="error">${escapeHtml(err.message || 'カートに追加できませんでした')}</span>`;
      }
      // 未ログイン時にログインへ誘導したい場合の例：
      // if ((err.message || '').includes('ログインが必要')) location.href = '/login';
    }
  });
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', bindCartAddHandler);
} else {
  bindCartAddHandler();
}