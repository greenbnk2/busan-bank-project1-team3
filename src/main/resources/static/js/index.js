// ==================== BNK 부산은행 펀드 통합 스크립트 ====================

document.addEventListener('DOMContentLoaded', function() {
    console.log('BNK Index.html loaded');

    // 초기화 함수 실행
    initSidebar();
    initSearch();

});

// ==================== 로그인 타이머 기능 ====================

let mainSessionTimerInterval;       // 메인 헤더 타이머 (예: 20:00 -> 19:59)
let modalWarningTimerInterval;    // 모달 팝업 타이머 (예: 02:00 -> 01:59)
let remainingTimeInSeconds = 1200;  // 서버에서 받은 전체 시간 (초)
let sessionModal = null;            // 모달 DOM 엘리먼트

const WARNING_TIME_SECONDS = 120; // 2분(120초) 전에 팝업을 띄움

/**
 * 메인 헤더의 타이머 디스플레이를 업데이트
 * @param {number} seconds - 남은 총 시간 (초)
 */
function updateMainTimerDisplay(seconds) {
    const timerDisplay = document.getElementById('sessionTimer');
    if (!timerDisplay) return;

    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    timerDisplay.textContent =
        `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
}

/**
 * 모달 팝업 내부의 타이머를 업데이트
 * @param {number} seconds - 남은 시간 (초, 120초부터 0까지)
 */
function updateModalTimerDisplay(seconds) {
    const modalTimerDisplay = document.getElementById('sessionModalTimer');
    if (!modalTimerDisplay) return;

    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    modalTimerDisplay.textContent =
        `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
}

/**
 * 강제 로그아웃 실행
 */
function forceLogout() {
    clearInterval(mainSessionTimerInterval);
    clearInterval(modalWarningTimerInterval);
    alert('세션이 만료되어 자동으로 로그아웃됩니다.');

    // CSRF 보호용 POST 폼 로그아웃
    const logoutForm = document.querySelector('form[th\\:action="@{/logout}"], form[action*="/logout"]');
    if (logoutForm) {
        logoutForm.submit();
    } else {
        // 폼을 못찾을 경우 대비
        window.location.href = '/bnk/';
    }
}

/**
 * 세션 연장 팝업창을 표시
 */
function showSessionWarningModal() {
    if (sessionModal) {
        sessionModal.style.display = 'flex';
    }

    // 팝업 내부 타이머 시작 (WARNING_TIME_SECONDS부터 0까지)
    let modalRemainingTime = WARNING_TIME_SECONDS;
    updateModalTimerDisplay(modalRemainingTime);

    modalWarningTimerInterval = setInterval(() => {
        modalRemainingTime--;
        updateModalTimerDisplay(modalRemainingTime);

        if (modalRemainingTime <= 0) {
            // 팝업이 떴는데도 2분간 연장을 안 누르면 메인 타이머가 0이 됨
            clearInterval(modalWarningTimerInterval);
        }
    }, 1000);
}

/**
 * 세션 연장 팝업창을 숨김
 */
function hideSessionWarningModal() {
    if (sessionModal) {
        sessionModal.style.display = 'none';
    }
    clearInterval(modalWarningTimerInterval); // 팝업 타이머 중지
}

/**
 * 서버에 세션 연장 요청 (API 호출)
 */
function extendSession() {
    // HTML의 meta 태그에서 CSRF 토큰과 헤더 이름을 읽어옴
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // CSRF 토큰을 헤더에 추가합니다.
    fetch('/bnk/api/session/extend', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token  // [헤더 이름]: 토큰 값 (예: 'X-CSRF-TOKEN': '...abc123...')
        }
    })
        .then(response => {
            // 세션이 만료되어 로그인 페이지로 리디렉션되었는지 확인
            if (response.redirected && response.url.includes("/member/login")) {
                throw new Error('Session expired and redirected to login');
            }
            // 403(CSRF 거부) 또는 500 등 다른 에러가 발생했는지 확인
            if (!response.ok) {
                throw new Error(`Server responded with status: ${response.status}`);
            }
            // 응답이 JSON이 맞는지 확인
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.indexOf("application/json") !== -1) {
                return response.json();
            } else {
                throw new Error('Invalid response from server: not JSON');
            }
        })
        .then(data => {
            if (data.status === 'ok') {
                console.log('세션이 연장되었습니다.');
                // (하드코딩된 값 1200을 사용)
                remainingTimeInSeconds = 1200;
                updateMainTimerDisplay(remainingTimeInSeconds);
                hideSessionWarningModal(); // 팝업 닫기
            } else {
                throw new Error('Server returned an error status');
            }
        })
        .catch(error => {
            console.error('Fetch error:', error);
            // 에러 메시지 수정 (사용자가 보게 될 메시지)
            alert('세션 연장에 실패했습니다. 다시 로그인해주세요.');
            forceLogout(); // 즉시 로그아웃 처리
        });
}


/**
 * 세션 타이머를 초기화하고 시작하는 메인 함수
 * @param {number} timeout - 서버에서 받은 세션 만료 시간 (초)
 */
function initSessionTimer(timeout) {
    remainingTimeInSeconds = timeout;
    sessionModal = document.getElementById('sessionModal');

    // 팝업(모달) 창의 버튼들
    const modalExtendBtn = document.getElementById('sessionModalExtendBtn');
    const modalLogoutBtn = document.getElementById('sessionModalLogoutBtn');

    // 헤더의 [연장] 버튼 찾기
    const headerExtendBtn = document.getElementById('sessionExtendBtn');

    // 팝업창 DOM이 로드되었는지 확인
    if (!sessionModal || !modalExtendBtn || !modalLogoutBtn || !headerExtendBtn) {
        console.warn('세션 타이머에 필요한 DOM(모달 또는 헤더 버튼)을 찾을 수 없습니다.');
    }

    // 헤더 [연장] 버튼에도 클릭 이벤트 연결
    if (headerExtendBtn) {
        headerExtendBtn.addEventListener('click', extendSession);
    }

    // 팝업(모달)의 [세션 연장] 버튼 클릭
    if (modalExtendBtn) {
        modalExtendBtn.addEventListener('click', extendSession);
    }

    // 팝업(모달)의 [로그아웃] 버튼 클릭
    if (modalLogoutBtn) {
        modalLogoutBtn.addEventListener('click', forceLogout);
    }

    // 1초마다 메인 타이머 실행
    mainSessionTimerInterval = setInterval(() => {
        remainingTimeInSeconds--;
        updateMainTimerDisplay(remainingTimeInSeconds);

        // 1. 경고 시간에 도달했는지 확인
        if (remainingTimeInSeconds === WARNING_TIME_SECONDS) {
            showSessionWarningModal();
        }

        // 2. 시간이 0이 되면 강제 로그아웃
        if (remainingTimeInSeconds <= 0) {
            clearInterval(mainSessionTimerInterval);
            forceLogout();
        }
    }, 1000);

    // 초기 표시
    updateMainTimerDisplay(remainingTimeInSeconds);
}

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

// ==================== 검색 모달 기능 ====================

function initSearch() {
    const searchButton = document.querySelector('.btn-search');
    const modal = document.getElementById('searchLayer');

    // 헤더의 검색 버튼 클릭 시
    if (searchButton && modal) {
        searchButton.addEventListener('click', function(e) {
            e.preventDefault();
            openSearchModal();
        });
    }
}

function openSearchModal() {
    const modal = document.getElementById('searchLayer');
    if(modal) {
        modal.style.display = 'flex';
        // 모달 열릴 때 입력창에 바로 포커스
        const input = modal.querySelector('.search-input');
        if(input) input.focus();
    }
}

function closeSearchModal() {
    const modal = document.getElementById('searchLayer');
    if(modal) {
        modal.style.display = 'none';
    }
}

// ESC 키 누르면 모달 닫기 (UX 추가)
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeSearchModal();
    }
});