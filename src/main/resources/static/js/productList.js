/* ================================================================
   공통 유틸
================================================================ */
function formatYield(v) {
    if (v === null || v === undefined) return "-";
    return v.toFixed(2) + "%";
}

let fundData = [];        // 전체 펀드 데이터
let filteredData = [];    // 화면에 표시할 데이터
let bestData = [];        // BEST 탭 데이터
let currentTab = "fund";  // 현재 활성 탭
let currentPage = 1;
const itemsPerPage = 10;

/* ================================================================
   1) DB 데이터 로드 + 카테고리 매핑
================================================================ */
async function loadFundData() {
    try {
        let rawData = [];

        if (window.serverFundList && window.serverFundList.length > 0) {
            rawData = window.serverFundList;
        } else {
            const res = await fetch("/bnk/api/fund/list");
            rawData = await res.json();
        }

        // 카테고리 매핑
        fundData = rawData.map(f => {
            let category = "all";
            const grade = f.investgrade ? f.investgrade.trim() : "";

            switch (grade) {
                case "매우 낮은 위험": category = "safe"; break;
                case "낮은 위험": category = "stable"; break;
                case "중간 위험": category = "neutral"; break;
                case "높은 위험": category = "dividend"; break;
                case "매우 높은 위험": category = "ipo"; break;
            }

            return { ...f, category };
        });

        filteredData = fundData;
        renderFundList();
        renderPagination();

    } catch (error) {
        console.error("펀드 데이터 로드 실패", error);
    }
}

/* ================================================================
   2) 테이블 렌더링
================================================================ */
function renderFundList(category = null) {
    const tbody = document.getElementById("fund-list");
    tbody.innerHTML = "";

    if (category !== null) {
        filteredData =
            category === "all" ? fundData : fundData.filter(f => f.category === category);
        currentPage = 1;
    }

    if (filteredData.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6">해당 펀드 유형에 투자성향이 적합하지 않아 비공개처리하였습니다.</td></tr>`;


        return;
    }

    const startIdx = (currentPage - 1) * itemsPerPage;
    const pageData = filteredData.slice(startIdx, startIdx + itemsPerPage);

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
              <div class="desc">${fund.content ?? ""}</div>

              <button class="wishlist-btn"
                onclick="${currentTab === 'interest'
            ? `deleteWish('${fund.fundcode}')`
            : `addWish('${fund.fundcode}')`}">
                ${currentTab === 'interest' ? "삭제" : "관심상품 등록"}
              </button>
            </td>

            <td>${formatYield(fund.perf1M)}</td>
            <td>${formatYield(fund.perf3M)}</td>
            <td>${formatYield(fund.perf6M)}</td>
            <td>${formatYield(fund.perf12M)}</td>

            <td>
              <button class="btn-join" onclick="location.href='/fund/join?fundNo=${fund.fundcode}'">
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
    if (totalPages <= 1) return;

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
   4) 탭 이벤트 처리
================================================================ */
document.querySelectorAll(".tab").forEach(tab => {
    tab.addEventListener("click", async () => {

        document.querySelectorAll(".tab").forEach(t => t.classList.remove("active"));
        tab.classList.add("active");

        const type = tab.dataset.type;
        currentTab = type;
        document.getElementById("title").textContent = tab.textContent;

        document.getElementById("best-filter").style.display = "none";

        /* --- 펀드상품 탭 --- */
        if (type === "fund") {
            document.getElementById("fund-filter").style.display = "flex";
            filteredData = fundData;
            currentPage = 1;
            renderFundList();
            renderPagination();
        }

        /* --- 추천펀드 탭 --- */
        else if (type === "recommend") {
            document.getElementById("fund-filter").style.display = "none";
            filteredData = fundData.filter(f => f.category === "neutral");
            currentPage = 1;
            renderFundList();
            renderPagination();
        }

        /* --- BEST 탭 --- */
        else if (type === "best") {
            document.getElementById("fund-filter").style.display = "none";

            const res = await fetch("/bnk/api/fund/best");
            bestData = await res.json();

            filteredData = bestData;
            document.getElementById("best-filter").style.display = "flex";

            currentPage = 1;
            renderFundList();
            renderPagination();
        }

        /* --- 관심상품 탭 --- */
        else if (type === "interest") {
            document.getElementById("fund-filter").style.display = "none";

            const res = await fetch("/bnk/api/fund/wishlist");
            filteredData = await res.json();

            currentPage = 1;
            renderFundList();
            renderPagination();
        }

    });
});

/* ================================================================
   5) 소분류 필터
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
   6) BEST 기간 정렬
================================================================ */
document.querySelectorAll("#best-filter .tab-yield").forEach(btn => {
    btn.addEventListener("click", () => {

        document.querySelectorAll("#best-filter .tab-yield")
            .forEach(b => b.classList.remove("active"));

        btn.classList.add("active");

        const key = {
            "1M": "perf1M",
            "3M": "perf3M",
            "6M": "perf6M",
            "12M": "perf12M"
        }[btn.dataset.yield];

        if (key) {
            filteredData = [...bestData]
                .filter(f => f[key] != null)
                .sort((a, b) => b[key] - a[key]);
        }

        currentPage = 1;
        renderFundList();
        renderPagination();
    });
});

/* ================================================================
   7) 관심상품 등록
================================================================ */
function addWish(fundCode) {

    const csrfToken = document.querySelector("meta[name='_csrf']").content;
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").content;

    fetch(`/bnk/api/fund/wishlist/add?fundCode=${fundCode}`, {
        method: "POST",
        headers: {
            [csrfHeader]: csrfToken
        }
    })
        .then(res => res.json())
        .then(data => {
            if (data.result === "exists") {
                alert("이미 관심상품에 등록된 펀드입니다.");
            } else {
                alert("관심상품에 등록되었습니다.");
            }
        });
}

/* ================================================================
   8) 관심상품 삭제
================================================================ */
function deleteWish(fundCode) {

    const csrfToken = document.querySelector("meta[name='_csrf']").content;
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").content;

    fetch(`/bnk/api/fund/wishlist/delete?fundCode=${fundCode}`, {
        method: "DELETE",
        headers: {
            [csrfHeader]: csrfToken
        }
    })
        .then(res => res.json())
        .then(() => {
            alert("삭제되었습니다.");
            if (currentTab === "interest") {
                document.querySelector(".tab[data-type='interest']").click();
            }
        });
}

/* ================================================================
   9) 초기 실행
================================================================ */
document.addEventListener("DOMContentLoaded", loadFundData);
