/* ================================================================
   🔥 펀드상품 리스트 + 위험등급 매핑 + 필터 + 탭 + 페이지네이션 (10개씩)
   ================================================================ */
function formatYield(v) {
    if (v === null || v === undefined) return "-";
    return v.toFixed(2) + "%";
}

let fundData = [];        // DB 전체 데이터
let filteredData = [];    // 필터 적용된 데이터
let currentPage = 1;      // 현재 페이지
const itemsPerPage = 10;  // 페이지마다 10개


/* ================================================================
   1) DB 데이터 로드 + 위험등급 카테고리 매핑
   ================================================================ */
async function loadFundData() {
    try {
        const res = await fetch("/bnk/api/fund/list");
        const rawData = await res.json();

        console.log("DB에서 받은 데이터(raw):", rawData);

        fundData = rawData.map(f => {
            let category = "all";

            switch (f.investgrade) {
                case "매우 낮은 위험":
                    category = "safe";
                    break;
                case "낮은 위험":
                    category = "stable";
                    break;
                case "중간 위험":
                    category = "neutral";
                    break;
                case "높은 위험":
                    category = "dividend";
                    break;
                case "매우 높은 위험":
                    category = "ipo";
                    break;
                default:
                    category = "all";
            }

            return { ...f, category };
        });

        filteredData = fundData; // 초기에는 전체 목록

        console.log("카테고리 변환 후:", fundData);

        renderFundList();
        renderPagination();
    } catch (error) {
        console.error("펀드 데이터 불러오기 실패", error);
    }
}


/* ================================================================
   2) 테이블 렌더링 (페이지네이션 적용)
   ================================================================ */
function renderFundList(category = null) {
    const tbody = document.getElementById("fund-list");
    tbody.innerHTML = "";

    // 🔥 필터 변경 시 filteredData 갱신
    if (category !== null) {
        filteredData =
            category === "all"
                ? fundData
                : fundData.filter(f => f.category === category);

        currentPage = 1; // 필터 바뀌면 첫 페이지로 이동
    }

    if (filteredData.length === 0) {
        tbody.innerHTML =
            `<tr><td colspan="6">해당 조건의 펀드가 없습니다.</td></tr>`;
        return;
    }

    // 🔥 페이지네이션 slice
    const startIdx = (currentPage - 1) * itemsPerPage;
    const endIdx = startIdx + itemsPerPage;
    const pageData = filteredData.slice(startIdx, endIdx);

    pageData.forEach(fund => {
        tbody.innerHTML += `
          <tr>
            <td class="fund-name">

              <a href="/bnk/fund/productDetail/${fund.fundcode}">
                ${fund.fundName ?? fund.fundNm ?? fund.fundshortcode ?? fund.fundcode}
              </a>

              <div class="tag-wrap">
                <span class="tag">${fund.investgrade || ""}</span>
              </div>

              <div class="desc">${fund.fundfeature || ""}</div>

            </td>

           <td>${formatYield(fund.perf1M)}</td>
            <td>${formatYield(fund.perf3M)}</td>
            <td>${formatYield(fund.perf6M)}</td>
            <td>${formatYield(fund.perf12M)}</td>

            <td>
              <button class="btn-join"
                onclick="location.href='/fund/join?fundNo=${fund.fundcode}'">
                인터넷가입
              </button>
              <span class="sub-btn">스마트폰가입</span>
            </td>
          </tr>`;
    });
}


/* ================================================================
   3) 페이지네이션 렌더링
   ================================================================ */
function renderPagination() {
    const pagination = document.getElementById("pagination");
    pagination.innerHTML = "";

    const totalPages = Math.ceil(filteredData.length / itemsPerPage);

    if (totalPages <= 1) return; // 1페이지면 렌더 안 함

    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement("button");
        btn.textContent = i;

        if (currentPage === i) btn.classList.add("active");

        btn.addEventListener("click", () => {
            currentPage = i;
            renderFundList();
            renderPagination();
        });

        pagination.appendChild(btn);
    }
}


/* ================================================================
   4) 상단 탭 버튼 이벤트
   ================================================================ */
document.querySelectorAll(".tab").forEach(tab => {
    tab.addEventListener("click", () => {

        document.querySelectorAll(".tab").forEach(t => t.classList.remove("active"));
        tab.classList.add("active");

        const type = tab.dataset.type;
        document.getElementById("title").textContent = tab.textContent;

        if (type === "fund") {
            document.getElementById("fund-filter").style.display = "flex";

            filteredData = fundData;
            currentPage = 1;

            renderFundList();
            renderPagination();

        } else {
            document.getElementById("fund-filter").style.display = "none";

            document.getElementById("fund-list").innerHTML =
                `<tr><td colspan='6'>${tab.textContent} DB 조회 필요</td></tr>`;

            document.getElementById("pagination").innerHTML = "";
        }
    });
});


/* ================================================================
   5) 소분류 필터 버튼 이벤트
   ================================================================ */
document.querySelectorAll("#fund-filter button").forEach(btn => {
    btn.addEventListener("click", () => {

        document.querySelectorAll("#fund-filter button")
            .forEach(b => b.classList.remove("active"));
        btn.classList.add("active");

        const category = btn.dataset.category;

        renderFundList(category);
        renderPagination();
    });
});


/* ================================================================
   6) 페이지 로드 시 실행
   ================================================================ */
document.addEventListener("DOMContentLoaded", loadFundData);
