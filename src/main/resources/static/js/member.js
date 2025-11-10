document.addEventListener('DOMContentLoaded', function() {
    console.log('Member 페이지가 로드되었습니다.');
});

// 안내 박스 토글 기능
function toggleInfoBox() {
    const infoBox = document.querySelector('.info-box');
    const toggleIcon = document.querySelector('.toggle-icon');

    infoBox.classList.toggle('collapsed');

    // 화살표 방향 변경
    if (infoBox.classList.contains('collapsed')) {
        toggleIcon.textContent = '▼';
    } else {
        toggleIcon.textContent = '▲';
    }
}