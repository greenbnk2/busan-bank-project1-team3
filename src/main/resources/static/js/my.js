
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