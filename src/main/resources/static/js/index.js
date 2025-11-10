// ==================== BNK 부산은행 펀드 통합 스크립트 ====================

document.addEventListener('DOMContentLoaded', function() {
    console.log('BNK 펀드 페이지가 로드되었습니다.');

    // 초기화 함수 실행
    initSidebar();
    // initSearch();
    // initMenu();
});

// ==================== 사이드바 메뉴 토글 기능 ====================
function initSidebar() {
    const menuButtons = document.querySelectorAll('.menu-btn');

    if (menuButtons.length === 0) {
        // 사이드바가 없는 페이지면 종료
        return;
    }

    menuButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const parentItem = this.parentElement;

            // 다른 열린 메뉴 닫기
            document.querySelectorAll('.menu-item').forEach(item => {
                if (item !== parentItem) {
                    item.classList.remove('open');
                    const innerBtn = item.querySelector('.menu-btn');
                    if (innerBtn) {
                        innerBtn.classList.remove('active');
                    }
                }
            });

            // 현재 메뉴 토글
            parentItem.classList.toggle('open');
            this.classList.toggle('active');
        });
    });
}

// ==================== 검색 기능 (향후 구현) ====================
function initSearch() {
    const searchButton = document.querySelector('.btn-search');
    if (searchButton) {
        searchButton.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('검색 기능 실행');
            // 검색 모달 또는 검색 페이지로 이동
        });
    }
}

// ==================== 전체 메뉴 기능 (향후 구현) ====================
function initMenu() {
    const menuButton = document.querySelector('.btn-allmenu');
    if (menuButton) {
        menuButton.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('전체 메뉴 열기');
            // 전체 메뉴 모달 표시
        });
    }
}

// ==================== 히어로 섹션 링크 처리 (향후 로그인 분기) ====================
function handleHeroClick() {
    const heroLink = document.querySelector('.hero-link');
    if (heroLink) {
        heroLink.addEventListener('click', function(e) {
            // 로그인 여부 확인 후 분기 처리
            // const isLoggedIn = checkLoginStatus();
            // if (isLoggedIn) {
            //     window.location.href = '/fund/signup';
            // } else {
            //     window.location.href = '/login';
            // }
        });
    }
}

// ==================== 유틸리티 함수 ====================

// 로그인 상태 확인 (향후 구현)
function checkLoginStatus() {
    // 세션 또는 로컬 스토리지에서 로그인 정보 확인
    return false;
}

// 쿠키 가져오기
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

// 쿠키 설정
function setCookie(name, value, days) {
    const expires = new Date();
    expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
    document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/`;
}