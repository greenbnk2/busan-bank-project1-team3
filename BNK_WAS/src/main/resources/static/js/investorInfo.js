document.addEventListener("DOMContentLoaded", () => {

    // “결과받기” 버튼 클릭 이벤트
    document.getElementById("sendResult").addEventListener("click", () => {
        alert("입력하신 이메일로 결과를 전송했습니다.");
    });

    // “분석하기” 버튼 클릭 이벤트
    document.getElementById("btnAnalyze").addEventListener("click", () => {
        alert("투자성향 분석을 시작합니다.\n(※ 실제 분석 로직은 추후 DB 연동 예정)");
    });

});
