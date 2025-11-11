// ==================== Member 페이지 통합 스크립트 ====================

document.addEventListener('DOMContentLoaded', function() {
    console.log('Member 페이지가 로드되었습니다.');

    // 약관동의 페이지 기능 초기화
    initTermsPage();
});

// ==================== 안내 박스 토글 기능 ====================
function toggleInfoBox() {
    const infoBox = document.querySelector('.info-box');
    if (infoBox) {
        infoBox.classList.toggle('collapsed');
    }
}

// ==================== 약관동의 페이지 기능 ====================
function initTermsPage() {
    const btnNext = document.querySelector('.btn-next');
    const btnCancel = document.querySelector('.btn-cancel');

    // 약관동의 페이지가 아니면 종료
    if (!btnNext || !btnCancel) {
        return;
    }

    // 다음 버튼 클릭 이벤트
    btnNext.addEventListener('click', function() {
        if (validateTermsAgreement()) {
            // 모든 약관에 동의한 경우
            alert('약관에 동의하셨습니다. 다음 단계로 이동합니다.');
            // 다음 페이지로 이동 (URL을 실제 다음 페이지로 변경하세요)
            // window.location.href = '/bnk/member/register';
        }
    });

    // 취소 버튼 클릭 이벤트
    btnCancel.addEventListener('click', function() {
        if (confirm('회원가입을 취소하시겠습니까?')) {
            // 이전 페이지로 이동
            window.history.back();
            // 또는 특정 페이지로 이동
            // window.location.href = '/bnk/member/registerType';
        }
    });
}

// ==================== 약관 동의 검증 ====================
function validateTermsAgreement() {
    // 첫 번째 약관 (회원약관)
    const terms1Agree = document.querySelector('input[name="terms1"][value="agree"]');
    // 두 번째 약관 (개인정보처리위탁방침)
    const terms2Agree = document.querySelector('input[name="terms2"][value="agree"]');

    // 두 약관 모두 체크되었는지 확인
    if (!terms1Agree.checked) {
        alert('회원약관에 동의해주세요.');
        terms1Agree.focus();
        return false;
    }

    if (!terms2Agree.checked) {
        alert('개인정보처리위탁방침에 동의해주세요.');
        terms2Agree.focus();
        return false;
    }

    // 모든 약관에 동의한 경우
    return true;
}

// ==================== 유틸리티 함수 ====================

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

// ==================== 회원가입 페이지 기능 ====================

// 직장정보 토글
function toggleJobInfo(show) {
    const jobInfoDetail = document.getElementById('jobInfoDetail');
    if (jobInfoDetail) {
        jobInfoDetail.style.display = show ? 'block' : 'none';
    }
}

// 자동차 번호 입력 토글
function toggleCarNumber(show) {
    const carNumberGroup = document.getElementById('carNumberGroup');
    const carNumber = document.getElementById('carNumber');

    if (carNumberGroup) {
        carNumberGroup.style.display = show ? 'inline-flex' : 'none';
    }

    if (carNumber && !show) {
        carNumber.value = '';
    }
}

// 이메일 인증 발송
function sendEmailVerification() {
    const emailId = document.querySelector('input[name="emailId"]').value;
    const emailDomain = document.querySelector('input[name="emailDomain"]').value;

    if (!emailId || !emailDomain) {
        alert('이메일을 입력해주세요.');
        return;
    }

    const email = emailId + '@' + emailDomain;

    // 이메일 형식 검증
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        alert('올바른 이메일 형식이 아닙니다.');
        return;
    }

    // 서버에 인증 이메일 발송 요청 (백엔드 연동 필요)
    // fetch('/api/send-verification-email', { ... })

    // 임시: 인증번호 입력란 표시
    alert('인증번호가 발송되었습니다. 이메일을 확인해주세요.');
    document.getElementById('verificationRow').style.display = 'table-row';
}

// 이메일 인증번호 확인
function verifyEmailCode() {
    const verificationCode = document.querySelector('input[name="verificationCode"]').value;

    if (!verificationCode) {
        alert('인증번호를 입력해주세요.');
        return;
    }

    // 서버에 인증번호 확인 요청 (백엔드 연동 필요)
    // fetch('/api/verify-email-code', { ... })

    // 임시: 인증 성공
    alert('이메일 인증이 완료되었습니다.');
}

// 회원가입 취소
function cancelRegister() {
    if (confirm('회원가입을 취소하시겠습니까?')) {
        window.history.back();
    }
}

// 이메일 도메인 선택 변경
document.addEventListener('DOMContentLoaded', function() {
    const emailDomainSelect = document.querySelector('select[name="emailDomainSelect"]');
    const emailDomainInput = document.querySelector('input[name="emailDomain"]');

    if (emailDomainSelect && emailDomainInput) {
        emailDomainSelect.addEventListener('change', function() {
            if (this.value === 'direct') {
                emailDomainInput.value = '';
                emailDomainInput.readOnly = false;
                emailDomainInput.focus();
            } else {
                emailDomainInput.value = this.value;
                emailDomainInput.readOnly = true;
            }
        });
    }

    // 폼 제출 이벤트
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();

            // 비밀번호 확인
            const password = document.querySelector('input[name="password"]').value;
            const passwordConfirm = document.querySelector('input[name="passwordConfirm"]').value;

            if (password !== passwordConfirm) {
                alert('비밀번호가 일치하지 않습니다.');
                return;
            }

            // 폼 제출 (백엔드 연동 필요)
            alert('회원가입이 완료되었습니다.');
            // window.location.href = '/bnk/member/complete';
        });
    }
});