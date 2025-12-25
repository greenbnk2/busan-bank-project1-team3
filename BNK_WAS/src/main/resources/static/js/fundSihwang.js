document.addEventListener("DOMContentLoaded", () => {

    // 검색 버튼 클릭 이벤트
    const btnSearch = document.getElementById("btnSearch");
    btnSearch.addEventListener("click", () => {
        const keyword = document.querySelector(".search-box input").value;
        if (keyword.trim() === "") {
            alert("검색어를 입력하세요.");
            return;
        }
        alert(`"${keyword}"에 대한 검색 결과를 표시합니다. (DB 연동 예정)`);
    });

    // 페이지네이션 클릭 예시
    const pages = document.querySelectorAll(".pagination button:not(:disabled)");
    pages.forEach(page => {
        page.addEventListener("click", () => {
            alert(`페이지 ${page.textContent} 로 이동 (추후 코드 작성 예정)`);
        });
    });

});
