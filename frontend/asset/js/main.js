document.addEventListener('DOMContentLoaded', () => {
    console.log('%c✅ Fresh Mart loaded successfully!', 'color: #15803d; font-size: 14px; font-weight: bold');
    
    loadHeaderFooter();
    initCarousel();
    initForms();
});

// ==================== LOAD HEADER & FOOTER ====================
function getTemplateBasePath() {
    const path = window.location.pathname.replace(/\\/g, '/').toLowerCase();
    if (path.includes('/html_login_register/')) {
        return '';
    }
    if (path.includes('/html_main/') || path.includes('/html_person/') || path.includes('/html/index/')) {
        return '../html_login_register/';
    }
    return 'html_login_register/';
}

function loadHeaderFooter() {
    const basePath = getTemplateBasePath();

    // Load Header
    fetch(basePath + 'header.html')
        .then(response => response.text())
        .then(data => {
            const headerContainer = document.getElementById('header-container');
            if (headerContainer) headerContainer.innerHTML = data;
        })
        .catch(error => console.error('Error loading header:', error));
    fetch(basePath + 'header_chinh.html')
        .then(response => response.text())
        .then(data => {
            const headerContainer = document.getElementById('headermain-container');
            if (headerContainer) headerContainer.innerHTML = data;
        })
        .catch(error => console.error('Error loading header:', error));

    // Load Footer
    fetch(basePath + 'footer.html')
        .then(response => response.text())
        .then(data => {
            const footerContainer = document.getElementById('footer-container');
            if (footerContainer) footerContainer.innerHTML = data;
        })
        .catch(error => console.error('Error loading footer:', error));
}

// ==================== CAROUSEL ====================
function initCarousel() {
    const carouselTrack = document.getElementById('carouselTrack');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');

    if (!carouselTrack) return;

    let currentIndex = 0;

    function updateCarousel() {
        carouselTrack.style.transform = `translateX(-${currentIndex * 25}%)`;
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            if (currentIndex < 1) {  // 8 cards - 4 visible
                currentIndex++;
                updateCarousel();
            }
        });
    }

    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            if (currentIndex > 0) {
                currentIndex--;
                updateCarousel();
            }
        });
    }
}

// ==================== FORM VALIDATION ====================
function initForms() {
    // Register Form
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            if (validateRegisterForm()) {
                alert('🎉 Đăng ký tài khoản thành công!');
                window.location.href = 'index.html';
            }
        });
    }

    // Login Form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            if (validateLoginForm()) {
                alert('✅ Đăng nhập thành công!');
                window.location.href = 'index.html';
            }
        });
    }
}

// Validation Register
function validateRegisterForm() {
    let isValid = true;
    clearErrors();

    const ho = document.getElementById('ho').value.trim();
    const ten = document.getElementById('ten').value.trim();
    const email = document.getElementById('email').value.trim();
    const sdt = document.getElementById('sdt').value.trim();
    const password = document.getElementById('password').value;
    const confirm = document.getElementById('confirmPassword').value;

    if (!ho) { showError('ho', 'Họ không được để trống'); isValid = false; }
    if (!ten) { showError('ten', 'Tên không được để trống'); isValid = false; }
    
    if (!email) { 
        showError('email', 'Email không được để trống'); isValid = false; 
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { 
        showError('email', 'Email không hợp lệ'); isValid = false; 
    }

    if (!sdt) { 
        showError('sdt', 'Số điện thoại không được để trống'); isValid = false; 
    } else if (!/^[0-9]{10}$/.test(sdt.replace(/\s+/g, ''))) { 
        showError('sdt', 'Số điện thoại phải là 10 số'); isValid = false; 
    }

    if (!password) { 
        showError('password', 'Mật khẩu không được để trống'); isValid = false; 
    } else if (password.length < 6) { 
        showError('password', 'Mật khẩu phải có ít nhất 6 ký tự'); isValid = false; 
    }

    if (!confirm) { 
        showError('confirmPassword', 'Vui lòng nhập lại mật khẩu'); isValid = false; 
    } else if (password !== confirm) { 
        showError('confirmPassword', 'Mật khẩu xác nhận không khớp'); isValid = false; 
    }

    return isValid;
}

// Validation Login
function validateLoginForm() {
    let isValid = true;
    clearErrors();

    const email = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPassword').value;

    if (!email) { 
        showError('loginEmail', 'Email không được để trống'); 
        isValid = false; 
    }
    if (!password) { 
        showError('loginPassword', 'Mật khẩu không được để trống'); 
        isValid = false; 
    }

    return isValid;
}

// Helper functions
function showError(fieldId, message) {
    const field = document.getElementById(fieldId);
    if (!field) return;

    let errorDiv = field.parentElement.querySelector('.error-message');
    if (!errorDiv) {
        errorDiv = document.createElement('div');
        errorDiv.className = 'error-message';
        field.parentElement.appendChild(errorDiv);
    }
    errorDiv.textContent = message;
    field.classList.add('border-red-500');
}

function clearErrors() {
    document.querySelectorAll('.error-message').forEach(el => el.remove());
    document.querySelectorAll('input').forEach(input => {
        input.classList.remove('border-red-500');
    });
}
