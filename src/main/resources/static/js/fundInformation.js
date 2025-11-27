/* ================================================================
   🔥 API 없이: 이미 HTML에 있는 <tr>들로 페이지네이션 구현
   ================================================================ */

let rows = [];         // HTML에 이미 렌더링된 <tr>들
let currentPage = 1;
const itemsPerPage = 10;


/* ================================================================
   1) 초기 데이터 로딩 (DOM에서 tr 수집)
   ================================================================ */
function initFundInfo() {
    const tbody = document.getElementById("fund-list");
    rows = Array.from(tbody.querySelectorAll("tr"));

    console.log("총 데이터 개수:", rows.length);

    renderList();
    renderPagination();
}


/* ================================================================
   2) 현재 페이지에 맞게 tr 보여주기/숨기기
   ================================================================ */
function renderList() {
    const tbody = document.getElementById("fund-list");

    // 전체 tr 숨기기
    rows.forEach(row => row.style.display = "none");

    // 필요한 페이지만 보여주기
    const startIdx = (currentPage - 1) * itemsPerPage;
    const endIdx = startIdx + itemsPerPage;

    const pageRows = rows.slice(startIdx, endIdx);
    pageRows.forEach(row => row.style.display = "");
}


/* ================================================================
   3) 페이지네이션 버튼 생성
   ================================================================ */
function renderPagination() {
    const pagination = document.getElementById("pagination");
    pagination.innerHTML = "";

    const totalPages = Math.ceil(rows.length / itemsPerPage);
    if (totalPages <= 1) return;

    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement("button");
        btn.textContent = i;

        if (i === currentPage) btn.classList.add("active");

        btn.addEventListener("click", () => {
            currentPage = i;
            renderList();
            renderPagination();
        });

        pagination.appendChild(btn);
    }
}


/* ================================================================
   4) 거래조건 변경 모달
   ================================================================ */
function openChangeModal(text) {
    const overlay = document.getElementById("modal-overlay");
    const content = document.getElementById("modal-content");

    content.innerHTML = text || "변경 내역이 없습니다.";
    overlay.style.display = "flex";
}

document.getElementById("modal-close").addEventListener("click", () => {
    document.getElementById("modal-overlay").style.display = "none";
});


/* ================================================================
   5) 페이지 로드 시 실행
   ================================================================ */
document.addEventListener("DOMContentLoaded", initFundInfo);
