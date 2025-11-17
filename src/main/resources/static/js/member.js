// ==================== Member 페이지 통합 스크립트 ====================
"use strict";

// 이메일 인증 성공 여부를 추적하는 전역 플래그
let isEmailVerified = false;

document.addEventListener('DOMContentLoaded', function() {
    console.log('Member 페이지가 로드되었습니다.');

    // 약관동의 페이지 기능 초기화 (terms.html 용)
    initTermsPage();

    // 회원가입 폼 기능 초기화 (register.html 용)
    initRegisterPage();

    // 이메일 도메인 선택 기능 초기화 (register.html 용)
    initEmailDomainSelect();
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
    // 약관 페이지 확인
    const termsContainer = document.querySelector('.terms-container');
    if (!termsContainer) {
        return; // 약관 페이지가 아니면 이 함수를 종료 (회원가입 페이지 등에서 오작동 방지)
    }

    // '다음' 버튼이 있는 페이지(terms.html)에서만 실행
    const btnNext = document.querySelector('.btn-next');
    const btnCancel = document.querySelector('.btn-cancel');

    if (!btnNext || !btnCancel) {
        return; // 해당 버튼이 없으면(register.html 등) 종료
    }

    // '다음' 버튼 클릭 시 약관 검증
    btnNext.addEventListener('click', function(e) {
        e.preventDefault(); // 폼 자동 제출 방지
        if (validateTermsAgreement()) { // validateTermsAgreement 함수 호출
            window.location.href = '/bnk/member/register'; // 검증 통과 시 페이지 이동
        }
    });

    // '취소' 버튼
    btnCancel.addEventListener('click', function() {
        if (confirm('회원가입을 취소하시겠습니까?')) {
            window.history.back();
        }
    });
}


// ==================== 회원가입 폼 기능 초기화 ====================
function initRegisterPage() {
    const form = document.getElementById('registerForm');
    if (!form) return;

    // 각 입력 필드에 실시간 검증(blur) 이벤트 추가
    addBlurValidation(form, 'name', validateName);
    addBlurValidation(form, 'userId', validateUserId);
    addBlurValidation(form, 'password', validatePassword);
    addBlurValidation(form, 'passwordConfirm', validatePasswordConfirm);
    addBlurValidation(form, 'phone', validatePhone);
    addBlurValidation(form, 'birthdate', validateBirthdate);
    addBlurValidation(form, 'zipcode', validateAddress); // 주소는 우편번호 기준
    addBlurValidation(form, 'address2', validateAddress); // 상세주소
    addBlurValidation(form, 'accountNumber', validateAccountNumber);
    addBlurValidation(form, 'accountPassword', validateAccountPassword);

    // 이메일은 ID와 Domain 필드 모두에 검증 연결
    addBlurValidation(form, 'emailId', validateEmail);
    addBlurValidation(form, 'emailDomain', validateEmail);

    // 핸드폰 번호 자동 하이픈 추가
    const phoneInput = form.querySelector('input[name="phone"]');
    if (phoneInput) {
        phoneInput.addEventListener('input', function () {
            this.value = formatPhoneNumber(this.value);
        });
    }

    // 폼 제출 시 전체 검증
    form.addEventListener('submit',  async function (e) {

        // 유효성 검사 완료 전까지 항상 제출 방지
        e.preventDefault();

        // await로 마스터 유효성 검사 실행
        const isFormValid = await validateMasterForm();

        if (!isFormValid) {
            // 유효성 검사 실패 시 (alert는 validateMasterForm에서 처리)
            return false;
        }

        // 검증 통과 시 폼 제출
        form.submit();
    });
}

/**
 * 필드에 blur 이벤트 리스너 추가 (헬퍼 함수)
 */
function addBlurValidation(form, fieldName, validationFunction) {
    const field = form.querySelector(`input[name="${fieldName}"]`);
    if (field) {
        field.addEventListener('blur', function () {
            validationFunction();
        });
    }
}

// ==================== 마스터 유효성 검사 (Submit) ====================
/**
 * 폼 제출 시 모든 유효성 검사 실행
 */
async function validateMasterForm() {
    const isUserIdValid = await validateUserId();

    // 개별 유효성 검사를 모두 실행 (결과를 allValid에 누적)
    const allValid = [
        validateName(),
        isUserIdValid, // await로 받은 결과 사용
        validateUserId(),
        validatePassword(),
        validatePasswordConfirm(),
        validatePhone(),
        validateAddress(),
        validateEmail(),
        validateAccountNumber(),
        validateAccountPassword(),
        validateBirthdate()
    ].every(isValid => isValid); // 모든 검사가 true여야 함

    if (!allValid) {
        alert('입력 항목을 다시 확인해주세요. (빨간색 표시)');
        return false;
    }

    // 이메일 인증 여부 확인
    if (!isEmailVerified) {
        alert('이메일 인증을 완료해주세요.');
        document.querySelector('input[name="emailId"]').focus();
        showError('emailId', '이메일 인증을 완료해주세요.');
        return false;
    }

    // 모든 검증 통과
    return true;
}

// ==================== 개별 필드 유효성 검사  ====================
// 각 함수는 [유효하면 true, 아니면 false]를 반환합니다.

function validateName() {
    const field = document.querySelector('input[name="name"]');
    if (!field) return true; // 필드가 없으면 통과

    if (field.value.trim() === "") {
        return showError('name', '성명(실명)을 입력해주세요.');
    }
    return clearError('name');
}

async function validateUserId() {
    const field = document.querySelector('input[name="userId"]');
    if (!field) return true;

    // 1. 형식 유효성 검사
    const regex = /^[a-zA-Z0-9]{6,20}$/;
    if (field.value.trim() === "") {
        return showError('userId', '회원아이디를 입력해주세요.');
    }
    if (!regex.test(field.value)) {
        return showError('userId', '아이디는 6~20자의 영문 또는 숫자만 가능합니다.');
    }

    // 2. 형식 검사 통과 시, 서버에 중복 확인
    try {
        // CSRF 토큰 (이메일 인증과 동일)
        const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        const response = await fetch('/bnk/member/api/check-userid', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token // CSRF 토큰 추가
            },
            body: JSON.stringify({ userId: field.value })
        });

        if (!response.ok) {
            throw new Error('서버 응답 오류');
        }

        const data = await response.json();

        // 3. 서버 응답 결과에 따라 메시지 표시
        if (data.available) {
            // 사용 가능 (초록색)
            return showSuccess('userId', data.message);
        } else {
            // 중복 (빨간색)
            return showError('userId', data.message);
        }

    } catch (error) {
        console.error('Error:', error);
        // 서버 통신 자체 실패 시 (빨간색)
        return showError('userId', '아이디 중복 확인 중 오류가 발생했습니다.');
    }
}

function validatePassword() {
    const field = document.querySelector('input[name="password"]');
    if (!field) return true;

    // (예시) 최소 8자, 문자, 숫자, 특수문자 포함
    const regex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    if (field.value.trim() === "") {
        return showError('password', '비밀번호를 입력해주세요.');
    }
    if (!regex.test(field.value)) {
        return showError('password', '비밀번호는 8자 이상이며, 문자, 숫자, 특수문자를 포함해야 합니다.');
    }
    // 비밀번호 확인 필드도 이어서 검증
    validatePasswordConfirm();
    return clearError('password');
}

function validatePasswordConfirm() {
    const passField = document.querySelector('input[name="password"]');
    const confirmField = document.querySelector('input[name="passwordConfirm"]');
    if (!confirmField || !passField) return true;

    if (confirmField.value.trim() === "") {
        return showError('passwordConfirm', '비밀번호 확인을 입력해주세요.');
    }
    if (passField.value !== confirmField.value) {
        return showError('passwordConfirm', '비밀번호가 일치하지 않습니다.');
    }
    return clearError('passwordConfirm');
}

function validatePhone() {
    const field = document.querySelector('input[name="phone"]');
    if (!field) return true;

    const regex = /^010-\d{4}-\d{4}$/;
    if (field.value.trim() === "") {
        return showError('phone', '핸드폰 번호를 입력해주세요.');
    }
    if (!regex.test(field.value)) {
        return showError('phone', '핸드폰 번호 형식이 올바르지 않습니다. (예: 010-1234-5678)');
    }
    return clearError('phone');
}

function validateAddress() {
    const zipField = document.querySelector('input[name="zipcode"]');
    const addr2Field = document.querySelector('input[name="address2"]');
    if (!zipField || !addr2Field) return true;

    let isValid = true;
    if (zipField.value.trim() === "") {
        showError('zipcode', '우편번호 찾기를 완료해주세요.'); // 에러는 zipcode에 표시
        isValid = false;
    } else {
        clearError('zipcode');
    }

    if (addr2Field.value.trim() === "") {
        showError('address2', '상세주소를 입력해주세요.'); // 에러는 address2에 표시
        isValid = false;
    } else {
        clearError('address2');
    }
    return isValid;
}

function validateEmail() {
    const idField = document.querySelector('input[name="emailId"]');
    const domainField = document.querySelector('input[name="emailDomain"]');
    if (!idField || !domainField) return true;

    const idVal = idField.value.trim();
    const domainVal = domainField.value.trim();

    // 1. 이메일이 비어있는지 검사 (필수 항목)
    if (idVal === "" || domainVal === "") {
        return showError('emailId', 'E-mail은 필수 항목입니다.');
    }

    // 2. 이메일 아이디가 비어있지 않으면, 형식 검사
    const idRegex = /^[a-zA-Z0-9._+-]+$/;
    const domainRegex = /^[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!idRegex.test(idVal)) {
        return showError('emailId', '이메일 아이디 형식이 올바르지 않습니다.');
    }

    if (!domainRegex.test(domainVal)) {
        return showError('emailId', '이메일 도메인 형식이 올바르지 않습니다.');
    }

    return clearError('emailId');
}

function validateAccountNumber() {
    const field = document.querySelector('input[name="accountNumber"]');
    if (!field) return true;

    if (field.value.trim() === "") {
        return showError('accountNumber', '계좌번호를 입력해주세요.');
    }
    // (필요시 계좌번호 형식 정규식 추가)
    return clearError('accountNumber');
}

function validateAccountPassword() {
    const field = document.querySelector('input[name="accountPassword"]');
    if (!field) return true;

    const regex = /^\d{4}$/;
    if (field.value.trim() === "") {
        return showError('accountPassword', '계좌비밀번호 4자리를 입력해주세요.');
    }
    if (!regex.test(field.value)) {
        return showError('accountPassword', '계좌비밀번호 4자리 숫자를 입력해주세요.');
    }
    return clearError('accountPassword');
}

function validateBirthdate() {
    const field = document.querySelector('input[name="birthdate"]');
    if (!field) return true;

    if (field.value.trim() === "") {
        return showError('birthdate', '생년월일을 입력해주세요.');
    }
    return clearError('birthdate');
}

// ==================== 에러 메시지 표시/제거 ====================
/**
 * 폼 테이블(`<td>`) 내부에 에러 메시지를 표시
 */
function showError(fieldName, message) {
    const field = document.querySelector(`input[name="${fieldName}"]`);
    if (!field) return;

    // 필드를 감싸는 <td> 찾기
    const container = field.closest('td');
    if (!container) return;

    // 기존 에러 메시지 제거
    clearError(fieldName);

    // 에러 메시지 <div.error-message> 생성
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    errorDiv.setAttribute('data-error-for', fieldName); // 제거를 위한 식별자

    // <td> 내부에 에러 메시지 추가
    container.appendChild(errorDiv);

    // 입력 필드에 에러 클래스 추가
    field.classList.add('input-error');

    return false; // 유효성 검사 실패(false) 반환
}
/**
 * 성공 메시지를 표시
 */
function showSuccess(fieldName, message) {
    const field = document.querySelector(`input[name="${fieldName}"]`);
    if (!field) return;

    const container = field.closest('td');
    if (!container) return;

    // ※ 기존 메시지(성공/실패) 모두 제거
    clearError(fieldName);

    const successDiv = document.createElement('div');
    successDiv.className = 'success-message'; // ⭐️ 초록색 클래스
    successDiv.textContent = message;
    successDiv.setAttribute('data-error-for', fieldName); // clearError로 지울 수 있게

    container.appendChild(successDiv);

    // 성공 시에는 input-error 클래스 제거
    field.classList.remove('input-error');

    return true; // 유효성 검사 성공(true) 반환
}

/**
 * 에러 메시지 및 스타일 제거
 */
function clearError(fieldName) {
    const field = document.querySelector(`input[name="${fieldName}"]`);
    if (field) {
        field.classList.remove('input-error');
    }

    const errorMsg = document.querySelector(`[data-error-for="${fieldName}"]`);
    if (errorMsg) {
        errorMsg.remove();
    }

    return true; // 유효성 검사 성공(true) 반환
}

// ==================== 이메일 인증 ====================
/**
 * 이메일 인증 발송 (Async/Fetch 스타일로 수정)
 */
async function sendEmailVerification() {
    if (isEmailVerified) {
        alert('이미 이메일 인증이 완료되었습니다.');
        return;
    }

    const emailId = document.querySelector('input[name="emailId"]');
    const emailDomain = document.querySelector('input[name="emailDomain"]');

    // 이메일 필드 유효성 검사
    if (emailId.value.trim() === "" || emailDomain.value.trim() === "") {
        showError('emailId', '이메일을 올바르게 입력해주세요.');
        return;
    }

    const email = emailId.value + '@' + emailDomain.value;
    const btnVerify = document.querySelector('button[onclick="sendEmailVerification()"]');

    btnVerify.disabled = true;
    btnVerify.textContent = '전송중...';

    try {
        // [주의] 이 API 엔드포인트는 서버에 구현해야 합니다.
        // CSRF 토큰을 헤더에 추가해야 할 수도 있습니다.
        const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        const response = await fetch('/bnk/member/api/send-email-code', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token // CSRF 토큰 추가
            },
            body: JSON.stringify({ email: email })
        });

        if (!response.ok) {
            throw new Error('서버 응답 오류: ' + response.statusText);
        }

        const data = await response.json();

        // data.status === "success" 또는 "fail" (중복 등)
        if (data.status === "success") {
            alert('인증번호가 발송되었습니다. 이메일을 확인해주세요.');
            document.getElementById('verificationRow').style.display = 'table-row';

            // 60초 타이머 (재전송 방지)
            let countdown = 60;
            const timer = setInterval(() => {
                btnVerify.textContent = `재전송 (${countdown}초)`;
                countdown--;
                if (countdown < 0) {
                    clearInterval(timer);
                    btnVerify.disabled = false;
                    btnVerify.textContent = '이메일 인증';
                }
            }, 1000);

        } else {
            alert(data.message || '이미 사용중인 이메일입니다.');
            btnVerify.disabled = false;
            btnVerify.textContent = '이메일 인증';
        }

    } catch (error) {
        console.error('Error:', error);
        alert('인증번호 발송 중 오류가 발생했습니다.');
        btnVerify.disabled = false;
        btnVerify.textContent = '이메일 인증';
    }
}

/**
 * 이메일 인증번호 확인 (Async/Fetch 스타일로 수정)
 */
async function verifyEmailCode() {
    const code = document.querySelector('input[name="verificationCode"]').value;
    const btnOk = document.querySelector('button[onclick="verifyEmailCode()"]');

    if (!code.trim()) {
        alert('인증번호를 입력해주세요.');
        return;
    }

    // CSRF 토큰
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    try {
        // [주의] 이 API 엔드포인트는 서버에 구현해야 합니다.
        const response = await fetch('/bnk/member/api/verify-email-code', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token // CSRF 토큰 추가
            },
            body: JSON.stringify({ code: code })
        });

        const data = await response.json();

        if (data.status === "success") {
            alert('이메일 인증이 완료되었습니다.');
            isEmailVerified = true; // ★ 인증 성공 플래그 설정
            clearError('emailId'); // 이메일 필드의 오류 메시지 제거

            // 이메일 관련 필드 읽기 전용으로 변경
            document.querySelector('input[name="emailId"]').readOnly = true;
            document.querySelector('input[name="emailDomain"]').readOnly = true;
            document.querySelector('select[name="emailDomainSelect"]').disabled = true;
            document.querySelector('input[name="verificationCode"]').readOnly = true;

            // 버튼 비활성화
            btnOk.disabled = true;
            btnOk.textContent = '인증완료';

            // 재전송 버튼도 비활성화
            const btnVerify = document.querySelector('button[onclick="sendEmailVerification()"]');
            btnVerify.disabled = true;

        } else {
            alert(data.message || '인증번호가 일치하지 않습니다.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('인증번호 확인 중 오류가 발생했습니다.');
    }
}

// ==================== 약관 동의 검증 ====================
function validateTermsAgreement() {
    // 첫 번째 약관 (회원약관)
    const terms1Agree = document.querySelector('input[name="terms1"][value="agree"]');
    // 두 번째 약관 (개인정보처리위탁방침)
    const terms2Agree = document.querySelector('input[name="terms2"][value="agree"]');

    // 두 약관 모두 체크되었는지 확인
    if (!terms1Agree || !terms1Agree.checked) {
        alert('회원약관에 동의해주세요.');
        if(terms1Agree) terms1Agree.focus();
        return false;
    }

    if (!terms2Agree || !terms2Agree.checked) {
        alert('개인정보처리위탁방침에 동의해주세요.');
        if(terms2Agree) terms2Agree.focus();
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

// 핸드폰 번호 자동 하이픈
function formatPhoneNumber(value) {
    if (!value) return value;
    value = value.replace(/[^\d]/g, ""); // 숫자만 추출
    if (value.length > 11) {
        value = value.substring(0, 11);
    }

    if (value.length > 7) {
        return value.replace(/(\d{3})(\d{4})(\d+)/, '$1-$2-$3');
    } else if (value.length > 3) {
        return value.replace(/(\d{3})(\d+)/, '$1-$2');
    } else {
        return value;
    }
}

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


// 회원가입 취소
function cancelRegister() {
    if (confirm('회원가입을 취소하시겠습니까?')) {
        window.history.back();
    }
}

// 이메일 도메인 선택 변경
function initEmailDomainSelect() {
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
            // 도메인 변경 시 이메일 유효성 검사 즉시 수행
            validateEmail();
        });
    }
}


// ==================== 우편번호 찾기 (카카오 API) ====================

/**
 * 개인정보 주소 - 우편번호 찾기
 */
function postCode() {
    new daum.Postcode({
        oncomplete: function(data) {
            // 팝업에서 검색결과 항목을 클릭했을때 실행할 코드를 작성하는 부분.
            var addr = ''; // 주소 변수
            var extraAddr = ''; // 참고항목 변수

            // 사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
            if (data.userSelectedType === 'R') { // 도로명 주소
                addr = data.roadAddress;
            } else { // 지번 주소
                addr = data.jibunAddress;
            }

            // 도로명 주소일 때 참고항목을 조합한다.
            if(data.userSelectedType === 'R'){
                // 법정동명이 있을 경우 추가한다.
                if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                    extraAddr += data.bname;
                }
                // 건물명이 있고, 공동주택일 경우 추가한다.
                if(data.buildingName !== '' && data.apartment === 'Y'){
                    extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }
                // 표시할 참고항목이 있을 경우, 괄호까지 추가한 최종 문자열을 만든다.
                if(extraAddr !== ''){
                    extraAddr = ' (' + extraAddr + ')';
                }
                // 주소에 참고항목 추가
                addr += extraAddr;
            }

            // 우편번호와 주소 정보를 해당 필드에 넣는다.
            document.getElementById('mem_zip').value = data.zonecode;
            document.getElementById('mem_addr1').value = addr;
            // 커서를 상세주소 필드로 이동한다.
            document.getElementById('mem_addr2').focus();

            // ★★★ 수정된 부분 ★★★
            // 주소 유효성 검사를 다시 실행하여 오류 메시지를 지웁니다.
            validateAddress();
        }
    }).open();
}

/**
 * 직장정보 주소 - 우편번호 찾기
 */
function postCodeJob() {
    new daum.Postcode({
        oncomplete: function(data) {
            var addr = ''; // 주소 변수
            var extraAddr = ''; // 참고항목 변수

            // 사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
            if (data.userSelectedType === 'R') { // 도로명 주소
                addr = data.roadAddress;
            } else { // 지번 주소
                addr = data.jibunAddress;
            }

            // 도로명 주소일 때 참고항목을 조합한다.
            if(data.userSelectedType === 'R'){
                // 법정동명이 있을 경우 추가한다.
                if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                    extraAddr += data.bname;
                }
                // 건물명이 있고, 공동주택일 경우 추가한다.
                if(data.buildingName !== '' && data.apartment === 'Y'){
                    extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }
                // 표시할 참고항목이 있을 경우, 괄호까지 추가한 최종 문자열을 만든다.
                if(extraAddr !== ''){
                    extraAddr = ' (' + extraAddr + ')';
                }
                // 주소에 참고항목 추가
                addr += extraAddr;
            }

            // 우편번호와 주소 정보를 해당 필드에 넣는다.
            document.getElementById('job_zip').value = data.zonecode;
            document.getElementById('job_addr1').value = addr;
            // 커서를 상세주소 필드로 이동한다.
            document.getElementById('job_addr2').focus();

            // (직장 주소는 필수 항목이 아니므로 유효성 검사 함수 호출은 생략합니다.)
        }
    }).open();
}