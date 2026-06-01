document.addEventListener('DOMContentLoaded', () => {
    console.log('%c✅ Fresh Mart hệ thống kết nối Backend loaded successfully!', 'color: #15803d; font-size: 14px; font-weight: bold');

    loadHeaderFooter();
    initGlobalSearch(); // Kích hoạt tính năng tìm kiếm đồng bộ cho tất cả các trang

    // Kiểm tra trang hiện tại dựa trên các ID đặc trưng để kích hoạt hàm tương ứng
    if (document.getElementById('carouselTrack') && document.getElementById('all-products-grid')) {
        initHomePage();
    }

    if (document.getElementById('products-grid') && document.getElementById('cat-all')) {
        fetchStoreData();
    }

    if (document.getElementById('main-product-img') && document.getElementById('thumbnail-container')) {
        loadProductsData();
    }
});

// Cấu hình URL cơ sở kết nối đến API Spring Boot
const BASE_API_URL = 'http://localhost:8080/api';

// ==================== LOAD HEADER & FOOTER ====================
function getTemplateBasePath() {
    const path = window.location.pathname.replace(/\\/g, '/').toLowerCase();

    if (path.includes('/html_login_register/')) {
        return '';                              // cùng thư mục
    }
    if (path.includes('/html_main/') || path.includes('/html_person/')) {
        return '../html_login_register/';       // lên 1 cấp
    }
    if (path.includes('/index/')) {
        return '../html_login_register/';       // ← THÊM: html/index/ lên 1 cấp
    }
    if (path.includes('/admin/')) {
        return '../html/html_login_register/';  // ← THÊM: admin/ lên rồi vào html/
    }
    return 'html_login_register/';             // fallback gốc project
}

function loadHeaderFooter() {
    const basePath = getTemplateBasePath();

    // Load Header thường
    fetch(basePath + 'header.html')
        .then(response => response.text())
        .then(data => {
            const headerContainer = document.getElementById('header-container');
            if (headerContainer) headerContainer.innerHTML = data;
        })
        .catch(error => console.error('Error loading header:', error));

    // Load Header chính
    fetch(basePath + 'header_chinh.html')
        .then(response => response.text())
        .then(data => {
            const headerContainer = document.getElementById('headermain-container');
            if (headerContainer) headerContainer.innerHTML = data;
        })
        .catch(error => console.error('Error loading main header:', error));

    // Load Footer
    fetch(basePath + 'footer.html')
        .then(response => response.text())
        .then(data => {
            const footerContainer = document.getElementById('footer-container');
            if (footerContainer) footerContainer.innerHTML = data;
        })
        .catch(error => console.error('Error loading footer:', error));
}

// ==================== ENGINE TÌM KIẾM TOÀN CỤC ĐỒNG BỘ ====================
function initGlobalSearch() {
    setTimeout(() => {
        const searchInput = document.getElementById('search-input');
        const searchBtn = searchInput ? searchInput.parentElement.querySelector('button') : null;

        if (!searchInput) return;

        const urlParams = new URLSearchParams(window.location.search);
        const searchParam = urlParams.get('search');

        if (searchParam && document.getElementById('products-grid')) {
            searchInput.value = searchParam;
            setTimeout(() => { filterProducts(); }, 100);
        }

        searchInput.addEventListener('keypress', (event) => {
            if (event.key === 'Enter') {
                executeSearch(searchInput.value);
            }
        });

        if (searchBtn) {
            searchBtn.addEventListener('click', () => {
                executeSearch(searchInput.value);
            });
        }
    }, 300);
}

function executeSearch(keyword) {
    const isProductPage = document.getElementById('products-grid') && document.getElementById('cat-all');

    if (isProductPage) {
        filterProducts();
    } else {
        window.location.href = 'trang_san_pham.html?search=' + encodeURIComponent(keyword.trim());
    }
}


// ==================== LOGIC PHÂN TRANG 1: TRANG_CHU.HTML ====================
let rawProductsDatabase = [];
let currentIndex = 0;

async function initHomePage() {
    try {
        // ĐỔI: Gọi API lấy toàn bộ sản phẩm từ Spring Boot thay vì file json cũ
        const response = await fetch(`${BASE_API_URL}/products`);
        if (!response.ok) throw new Error("Không thể kết nối tới API Backend Spring Boot");
        rawProductsDatabase = await response.json();

        renderBestPriceCarousel();
        renderAllStoreProducts();
    } catch (error) {
        console.error("Lỗi đồng bộ dữ liệu trang chủ với Backend:", error);
    }
}

function renderBestPriceCarousel() {
    const track = document.getElementById('carouselTrack');
    if (!track) return;
    const top7LastProducts = rawProductsDatabase.slice(-7);

    track.innerHTML = '';
    top7LastProducts.forEach(item => {
        // ĐỔI: Sử dụng item.imageUrl thay vì item.image để khớp Entity Product.java
        track.insertAdjacentHTML('beforeend', `
            <div class="product-card min-w-[calc(25%-18px)] md:min-w-[calc(25%-18px)] sm:min-w-[calc(50%-12px)] border rounded-3xl overflow-hidden flex flex-col shadow-sm">
                <div class="h-52 overflow-hidden bg-slate-50 flex items-center justify-center p-4 relative">
                    <a href="chi_tiet_san_pham.html?id=${item.id}" class="w-full h-full flex items-center justify-center">
                        <img src="${item.imageUrl || 'default-product.jpg'}" class="max-h-full max-w-full object-contain hover:scale-105 transition-transform duration-500" alt="${item.name}">
                    </a>
                    <span class="absolute top-3 left-3 bg-red-500 text-white font-black text-[9px] uppercase px-2 py-0.5 rounded shadow-sm tracking-widest italic">Giá sốc</span>
                </div>
                <div class="product-info p-5 flex-grow flex flex-col justify-between space-y-2">
                    <div>
                        <p class="text-green-600 font-bold text-xs uppercase tracking-wider">${item.category || 'Nông Sản'}</p>
                        <h4 class="font-bold text-slate-800 text-base line-clamp-1"><a href="chi_tiet_san_pham.html?id=${item.id}" class="hover:text-green-700">${item.name}</a></h4>
                    </div>
                    <div>
                        <p class="text-slate-400 text-[10px] uppercase font-bold tracking-widest">${item.unit || 'Túi 1KG'}</p>
                        <p class="text-green-700 font-[1000] text-xl mt-1">${item.price.toLocaleString('vi-VN')} đ</p>
                        <button onclick="globalAddToCart('${item.id}', 1)" class="mt-4 w-full bg-green-700 hover:bg-green-800 text-white py-3 rounded-2xl font-bold text-xs uppercase tracking-wider shadow-md transition-all">
                            Thêm vào giỏ
                        </button>
                    </div>
                </div>
            </div>
        `);
    });
}

function renderAllStoreProducts() {
    const grid = document.getElementById('all-products-grid');
    if (!grid) return;
    grid.innerHTML = '';

    rawProductsDatabase.forEach(item => {
        // ĐỔI: Đổi item.image thành item.imageUrl
        grid.insertAdjacentHTML('beforeend', `
            <div class="product-card p-4 rounded-2xl flex flex-col border border-slate-100 shadow-sm hover:shadow-md transition-all duration-300">
                <div class="bg-slate-50/50 rounded-xl h-44 flex items-center justify-center relative mb-4 overflow-hidden p-3">
                    <a href="chi_tiet_san_pham.html?id=${item.id}">
                        <img src="${item.imageUrl || 'default-product.jpg'}" class="max-h-36 object-contain hover:scale-110 transition-transform duration-500">
                    </a>
                    <span class="absolute top-2 left-2 bg-[#2f6b2f] text-white text-[9px] px-2 py-0.5 rounded font-black uppercase tracking-widest italic shadow-sm">Fresh</span>
                </div>
                <div class="flex-grow space-y-1">
                    <div class="flex justify-between items-center text-[10px] font-bold">
                        <span class="text-green-600 uppercase font-black tracking-wider">${item.category || 'Nông Sản'}</span>
                        <span class="bg-yellow-50 text-yellow-600 px-1.5 py-0.5 rounded flex items-center gap-1">4.8 <i class="fas fa-star text-[8px]"></i></span>
                    </div>
                    <h4 class="font-bold text-sm text-slate-800 line-clamp-1">
                        <a href="chi_tiet_san_pham.html?id=${item.id}" class="hover:text-green-700 transition-colors">${item.name}</a>
                    </h4>
                    <p class="text-[9px] font-bold text-gray-400 uppercase tracking-widest">${item.unit || 'Đơn vị'}</p>
                    <p class="text-base font-[1000] text-green-700 pt-1 tracking-tighter">${item.price.toLocaleString('vi-VN')} đ</p>
                </div>
                <button onclick="globalAddToCart('${item.id}', 1)" class="mt-4 bg-[#6b9460] py-2.5 text-white rounded-xl text-xs font-black uppercase tracking-wider hover:bg-green-700 transition shadow-sm">
                    <i class="fas fa-shopping-cart mr-1"></i> Thêm vào giỏ
                </button>
            </div>
        `);
    });
}

function moveCarousel(direction) {
    const track = document.getElementById('carouselTrack');
    if (!track) return;
    const totalItems = 7;
    const itemsVisible = window.innerWidth < 640 ? 1 : (window.innerWidth < 1024 ? 2 : 4);
    const maxIndex = totalItems - itemsVisible;

    currentIndex += direction;
    if (currentIndex < 0) currentIndex = 0;
    if (currentIndex > maxIndex) currentIndex = maxIndex;

    const itemWidth = 100 / itemsVisible;
    track.style.transform = `translateX(-${currentIndex * (itemWidth + 0.6)}%)`;
}

function redirectToCategory(catName) {
    window.location.href = `trang_san_pham.html?category=${encodeURIComponent(catName)}`;
}


// ==================== LOGIC PHÂN TRANG 2: TRANG_SAN_PHAM.HTML ====================
let rawProductsData = [];
let minPriceFilter = 0;
let maxPriceFilter = 99999999;
let activeCategory = 'all';

async function fetchStoreData() {
    try {
        // ĐỔI: Lấy dữ liệu sản phẩm từ API Backend thay vì file json cũ
        const response = await fetch(`${BASE_API_URL}/products`);
        if (!response.ok) throw new Error("Lỗi đọc dữ liệu từ Server API");
        rawProductsData = await response.json();

        const urlParams = new URLSearchParams(window.location.search);

        const searchParam = urlParams.get('search');
        const catParam = urlParams.get('category');

        if (searchParam) {
            const searchInput = document.getElementById('search-input');
            if (searchInput) searchInput.value = searchParam;
            filterProducts();
        } else if (catParam) {
            changeCategory(catParam);
        } else {
            filterProducts();
        }
    } catch (error) {
        console.error("Lỗi liên kết dữ liệu danh sách sản phẩm:", error);
    }
}

function filterProducts() {
    const grid = document.getElementById('products-grid');
    if (!grid) return;
    const searchInput = document.getElementById('search-input');
    const searchKeyword = searchInput ? searchInput.value.toLowerCase().trim() : '';

    let result = rawProductsData;

    if (activeCategory !== 'all') {
        result = result.filter(p => p.category === activeCategory);
    }
    result = result.filter(p => p.price >= minPriceFilter && p.price <= maxPriceFilter);

    if (searchKeyword) {
        result = result.filter(p => p.name.toLowerCase().includes(searchKeyword));
    }

    const countEl = document.getElementById('product-count');
    if (countEl) countEl.innerText = `Tìm thấy: ${result.length} sản phẩm phù hợp`;

    grid.innerHTML = '';

    if (result.length === 0) {
        grid.innerHTML = `<div class="col-span-full bg-white py-24 rounded-2xl text-center text-slate-400 italic border font-medium">Không có thực phẩm nào khớp với bộ lọc.</div>`;
        return;
    }

    result.forEach(item => {
        // ĐỔI: Đổi item.image thành item.imageUrl
        grid.insertAdjacentHTML('beforeend', `
            <div class="product-card p-4 rounded-2xl flex flex-col border border-slate-100 shadow-sm hover:shadow-md transition-all duration-300">
                <div class="bg-slate-50/40 rounded-xl h-44 flex items-center justify-center relative mb-4 overflow-hidden p-3">
                    <a href="chi_tiet_san_pham.html?id=${item.id}">
                        <img src="${item.imageUrl || 'default-product.jpg'}" class="max-h-36 object-contain hover:scale-110 transition-transform duration-500">
                    </a>
                    <span class="absolute top-2 left-2 bg-[#2f6b2f] text-white text-[9px] px-2 py-0.5 rounded font-black uppercase tracking-widest italic shadow-sm">Fresh</span>
                </div>
                <div class="flex-grow space-y-1">
                    <div class="flex justify-between items-center text-[10px] font-bold">
                        <span class="text-green-600 uppercase font-black tracking-wider">${item.category || 'Nông Sản'}</span>
                        <span class="bg-yellow-50 text-yellow-600 px-1.5 py-0.5 rounded flex items-center gap-1">4.8 <i class="fas fa-star text-[8px]"></i></span>
                    </div>
                    <h4 class="font-bold text-sm text-slate-800 line-clamp-1">
                        <a href="chi_tiet_san_pham.html?id=${item.id}" class="hover:text-green-700 transition-colors">${item.name}</a>
                    </h4>
                    <p class="text-[9px] font-bold text-gray-400 uppercase tracking-widest">${item.unit || 'Đơn vị'}</p>
                    <p class="text-base font-[1000] text-green-700 pt-1 tracking-tighter">${item.price.toLocaleString('vi-VN')} đ</p>
                </div>
                <button onclick="globalAddToCart('${item.id}', 1)" class="mt-4 bg-[#6b9460] py-2.5 text-white rounded-xl text-xs font-black uppercase tracking-wider hover:bg-green-700 transition active:scale-95 shadow-sm">
                    <i class="fas fa-shopping-cart mr-1"></i> Thêm vào giỏ
                </button>
            </div>
        `);
    });
}

function changeCategory(categoryName) {
    activeCategory = categoryName;

    document.querySelectorAll('.category-item').forEach(btn => {
        btn.classList.remove('category-item-active');
    });

    let targetId = 'cat-all';
    if (categoryName === 'Thịt cá') targetId = 'cat-thit-ca';
    else if (categoryName === 'Rau củ' || categoryName === 'Rau Củ Quả') targetId = 'cat-rau-cu';
    else if (categoryName === 'Trái cây' || categoryName === 'Trái Cây') targetId = 'cat-trai-cay';
    else if (categoryName === 'Hạt') targetId = 'cat-hat';

    const activeBtn = document.getElementById(targetId);
    if (activeBtn) activeBtn.classList.add('category-item-active');

    filterProducts();
}

function changePriceFilter(min, max) {
    if(min === 'all') {
        minPriceFilter = 0;
        maxPriceFilter = 99999999;
    } else {
        minPriceFilter = min;
        maxPriceFilter = max;
    }
    filterProducts();
}


// ==================== LOGIC PHÂN TRANG 3: CHI_TIET_SAN_PHAM.HTML ====================
let productsDatabase = [];
let activeProduct = null;

async function loadProductsData() {
    try {
        // ĐỔI: Đồng bộ lấy dữ liệu chi tiết qua API Backend
        const response = await fetch(`${BASE_API_URL}/products`);
        if (!response.ok) throw new Error('Gặp sự cố khi đọc dữ liệu sản phẩm từ Server API');
        productsDatabase = await response.json();

        initProductPage();
    } catch (error) {
        console.error("Lỗi đồng bộ dữ liệu chi tiết:", error);
    }
}

function initProductPage() {
    const urlParams = new URLSearchParams(window.location.search);
    const productId = urlParams.get('id');

    // Chuyển đổi ID sang kiểu số hoặc chuỗi tùy thiết kế Backend (ở đây parse Long/String tự động)
    activeProduct = productsDatabase.find(p => p.id == productId);
    if (!activeProduct) return;

    if(document.getElementById('page-title')) document.getElementById('page-title').innerText = `${activeProduct.name} - Fresh Mart`;
    if(document.getElementById('breadcrumb-title')) document.getElementById('breadcrumb-title').innerText = activeProduct.name;
    if(document.getElementById('breadcrumb-category')) document.getElementById('breadcrumb-category').innerText = activeProduct.category || 'Nông Sản';
    if(document.getElementById('product-title')) document.getElementById('product-title').innerText = activeProduct.name;
    if(document.getElementById('product-unit')) document.getElementById('product-unit').innerText = activeProduct.unit || 'Túi / KG';
    if(document.getElementById('product-price')) document.getElementById('product-price').innerText = activeProduct.price.toLocaleString('vi-VN') + " đ";
    if(document.getElementById('main-product-img')) document.getElementById('main-product-img').src = activeProduct.imageUrl || 'default-product.jpg';
    if(document.getElementById('db-unit')) document.getElementById('db-unit').innerText = activeProduct.unit || 'KG';

    if(document.getElementById('product-short-desc')) document.getElementById('product-short-desc').innerText = activeProduct.description || 'Nông sản hữu cơ sạch đạt chuẩn vệ sinh thực phẩm.';
    if(document.getElementById('product-full-desc')) document.getElementById('product-full-desc').innerText = activeProduct.description || 'Thông tin mô tả chi tiết sản phẩm đang được cập nhật.';

    const dbSource = document.getElementById('db-source');
    if (dbSource) {
        if (activeProduct.category === "Gia vị tươi") {
            dbSource.innerText = "Vùng chuyên canh rau gia vị hữu cơ nội địa";
        } else if (activeProduct.category === "Trái cây" && activeProduct.name.includes("New Zealand")) {
            dbSource.innerText = "Nhập khẩu chính ngạch - Đạt chuẩn quốc tế";
        } else {
            dbSource.innerText = "Hợp tác xã nông trại tiêu chuẩn VietGAP / GlobalGAP";
        }
    }

    const thumbContainer = document.getElementById('thumbnail-container');
    if (thumbContainer) {
        thumbContainer.innerHTML = `
            <img src="${activeProduct.imageUrl || 'default-product.jpg'}" onclick="changeMainImage(this.src)" class="w-16 h-16 rounded-lg border-2 border-green-500 p-1 object-cover cursor-pointer shadow-sm bg-white">
            <img src="https://images.unsplash.com/photo-1596436889106-be35e843f974?q=80&w=150" onclick="changeMainImage(this.src)" class="w-16 h-16 rounded-lg border border-gray-200 p-1 object-cover cursor-pointer hover:border-green-500 shadow-sm opacity-70 hover:opacity-100">
        `;
    }

    renderRelatedProducts();
}

function changeQuantity(delta) {
    const input = document.getElementById('quantity-input');
    if (!input) return;
    let val = parseInt(input.value) + delta;
    if (val < 1) val = 1;
    input.value = val;
}

function changeMainImage(src) {
    const mainImg = document.getElementById('main-product-img');
    if (mainImg) mainImg.src = src;
}

function renderRelatedProducts() {
    const container = document.getElementById('related-products-container');
    if (!container) return;

    let list = productsDatabase.filter(p => p.category === activeProduct.category && p.id !== activeProduct.id);
    if (list.length < 4) {
        const extra = productsDatabase.filter(p => p.id !== activeProduct.id && p.category !== activeProduct.category);
        list = list.concat(extra);
    }

    const shuffleList = list.slice(0, 4);
    container.innerHTML = '';

    shuffleList.forEach(item => {
        // ĐỔI: Đổi item.image thành item.imageUrl
        container.insertAdjacentHTML('beforeend', `
            <div class="product-card p-4 rounded-2xl flex flex-col border border-gray-100 shadow-sm hover:shadow-md transition-all duration-300">
                <div class="bg-slate-50/50 rounded-xl h-40 flex items-center justify-center relative mb-4 overflow-hidden p-2">
                    <a href="chi_tiet_san_pham.html?id=${item.id}">
                        <img src="${item.imageUrl || 'default-product.jpg'}" class="max-h-32 object-contain hover:scale-110 transition-transform duration-500">
                    </a>
                    <span class="absolute top-2 left-2 bg-[#2f6b2f] text-white text-[9px] px-2 py-0.5 rounded font-bold uppercase tracking-wider">Fresh</span>
                </div>
                <div class="flex-grow space-y-1">
                    <div class="flex justify-between items-center text-[10px] font-bold">
                        <span class="text-green-600 uppercase italic">${item.category || 'Nông Sản'}</span>
                        <span class="bg-yellow-50 text-yellow-600 px-1.5 py-0.5 rounded flex items-center gap-1">4.8 <i class="fas fa-star text-[8px]"></i></span>
                    </div>
                    <h4 class="font-bold text-sm text-slate-800 line-clamp-1">
                        <a href="chi_tiet_san_pham.html?id=${item.id}">${item.name}</a>
                    </h4>
                    <p class="text-[9px] font-bold text-gray-400 uppercase tracking-widest">${item.unit || 'Đơn vị'}</p>
                    <p class="text-base font-black text-green-700 pt-1">${item.price.toLocaleString('vi-VN')} đ</p>
                </div>
                <button onclick="globalAddToCart('${item.id}', 1)" class="mt-4 bg-[#6b9460] py-2.5 text-white rounded-xl text-xs font-black uppercase tracking-wider hover:bg-green-700 transition">
                    <i class="fas fa-shopping-cart mr-1"></i> + Giỏ hàng
                </button>
            </div>
        `);
    });
}


// ==================== ENGINE NGHIỆP VỤ GIỎ HÀNG TOÀN CỤC ====================
async function globalAddToCart(id, inputQty = null) {
    let qty = inputQty ? parseInt(inputQty) : 1;
    const qtyInput = document.getElementById('quantity-input');
    if (!inputQty && qtyInput) {
        qty = parseInt(qtyInput.value) || 1;
    }

    try {
        // ĐỔI: Gọi API Backend lấy thông tin sản phẩm thay vì file json cũ
        const response = await fetch(`${BASE_API_URL}/products`);
        const db = await response.json();
        const pInfo = db.find(p => p.id == id);

        if (!pInfo) return;

        let localCart = JSON.parse(localStorage.getItem('fresh_mart_cart')) || [];
        let existed = localCart.find(item => item.id == id);

        if (existed) {
            existed.quantity += qty;
        } else {
            localCart.push({
                id: pInfo.id,
                name: pInfo.name,
                price: pInfo.price,
                image: pInfo.imageUrl, // Đồng bộ trường hình ảnh với database
                unit: pInfo.unit || 'KG',
                quantity: qty,
                selected: true
            });
        }

        localStorage.setItem('fresh_mart_cart', JSON.stringify(localCart));
        triggerToast(`Đã thêm ${qty} x ${pInfo.name} vào giỏ hàng thành công!`);
    } catch (e) {
        console.error("Lỗi đồng bộ giỏ hàng:", e);
    }
}

async function globalBuyNow() {
    if (!activeProduct) return;

    const buyNowOrder = [{
        id: activeProduct.id,
        name: activeProduct.name,
        price: activeProduct.price,
        image: activeProduct.imageUrl, // Đổi sang imageUrl
        unit: activeProduct.unit || 'KG',
        quantity: parseInt(document.getElementById('quantity-input').value) || 1
    }];

    sessionStorage.setItem('checkout_type', 'buynow');
    sessionStorage.setItem('checkout_data', JSON.stringify(buyNowOrder));
    
    triggerToast("Đang kết nối luồng thanh toán cấp tốc...");
    setTimeout(() => {
        window.location.href = '../index/DienThongTin.html';
    }, 800);
}

// ==================== UTILITIES GLOBAL ====================
function triggerToast(msg) {
    const toast = document.getElementById('toast');
    const msgEl = document.getElementById('toast-message');
    if (!toast || !msgEl) return;
    msgEl.innerText = msg;
    toast.style.opacity = '1'; 
    toast.style.transform = 'translate(-50%, -20px)';
    setTimeout(() => { 
        toast.style.opacity = '0'; 
        toast.style.transform = 'translate(-50%, 40px)'; 
    }, 2000);
}