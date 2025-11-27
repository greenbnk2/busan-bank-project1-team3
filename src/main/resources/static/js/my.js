
// HTML 문서가 모두 로드되었을 때 이 함수를 실행합니다.
document.addEventListener('DOMContentLoaded', function() {

    console.log('MY.JS: 새 스크립트가 로드되었습니다.');

    // 1. '.my-sidebar__menu' 안의 모든 아코디언 버튼을 찾습니다.
    //    (<a> 링크가 아닌 <button> 태그만 해당됩니다)
    const accordionButtons = document.querySelectorAll('.my-sidebar__menu button.my-sidebar__button');

    console.log(`MY.JS: 아코디언 버튼 ${accordionButtons.length}개를 찾았습니다.`);

    // 2. 각 버튼에 클릭 이벤트를 추가합니다.
    accordionButtons.forEach(function(button) {

        button.addEventListener('click', function() {
            // 3. 클릭된 버튼의 부모인 <li> (.my-sidebar__item)를 찾습니다.
            const parentItem = button.closest('.my-sidebar__item');

            if (parentItem) {
                console.log('MY.JS: 버튼 클릭됨! "is-open" 클래스를 토글합니다.');
                // 4. 부모 <li>에 'is-open' 클래스를 토글(추가/제거)합니다.
                parentItem.classList.toggle('is-open');
            }
        });
    });
});


// ==================== 우편번호 찾기 (카카오 API) ====================

/**
 * 개인정보 주소 - 우편번호 찾기
 */
function postCode() {
    // Daum 라이브러리가 로드되었는지 확인
    if (typeof daum === 'undefined') {
        alert('다음 우편번호 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
        return;
    }

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
                if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                    extraAddr += data.bname;
                }
                if(data.buildingName !== '' && data.apartment === 'Y'){
                    extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }
                if(extraAddr !== ''){
                    extraAddr = ' (' + extraAddr + ')';
                }
                addr += extraAddr;
            }

            // 우편번호와 주소 정보를 해당 필드에 넣는다.
            // info.html의 ID(mem_zip, mem_addr1)와 일치해야 합니다.
            document.getElementById('mem_zip').value = data.zonecode;
            document.getElementById('mem_addr1').value = addr;

            // 커서를 상세주소 필드로 이동한다.
            document.getElementById('mem_addr2').focus();
        }
    }).open();
}