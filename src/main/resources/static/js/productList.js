let fundData = [];

// ====== DB 데이터 로드 ======
async function loadFundData() {
    try {
        const res = await fetch("/api/fund/list");
        fundData = await res.json();

        console.log("DB에서 받은 데이터:", fundData);

        renderFundList("all");
    } catch (error) {
        console.error("펀드 데이터 불러오기 실패", error);
    }
}

// ====== 테이블 렌더링 ======
function renderFundList(category = "all") {
    const tbody = document.getElementById("fund-list");
    tbody.innerHTML = "";

    const filtered =
        category === "all"
            ? fundData
            : fundData.filter(f => f.category === category);

    if (filtered.length === 0) {
        tbody.innerHTML =
            `<tr><td colspan="6">해당 조건에 맞는 펀드가 없습니다.</td></tr>`;
        return;
    }

    filtered.forEach(fund => {
        tbody.innerHTML += `
          <tr>
            <td class="fund-name">
              ${fund.tag ? `<span class="tag">${fund.tag}</span>` : ""}
              <a href="/fund/productDetail?fundNo=${fund.fundNo}" target="_blank">
                ${fund.fundName}
              </a>
              <div class="desc">${fund.desc || ""}</div>
            </td>
            <td>${fund.perf1M}</td>
            <td>${fund.perf3M}</td>
            <td>${fund.perf6M}</td>
            <td>${fund.perf12M}</td>
            <td>
              <button class="btn-join"
                onclick="location.href='/fund/join?fundNo=${fund.fundNo}'">
                인터넷가입
              </button>
              <span class="sub-btn">스마트폰가입</span>
            </td>
          </tr>`;
    });
}

// ====== 상단 탭 클릭 이벤트 ======
document.querySelectorAll(".tab").forEach(tab => {
    tab.addEventListener("click", () => {
        document.querySelectorAll(".tab").forEach(t => t.classList.remove("active"));
        tab.classList.add("active");

        const type = tab.dataset.type;
        document.getElementById("title").textContent = tab.textContent;

        if (type === "fund") {
            document.getElementById("fund-filter").style.display = "flex";
            renderFundList("all");
        } else {
            document.getElementById("fund-filter").style.display = "none";
            document.getElementById("fund-list").innerHTML =
                `<tr><td colspan='6'>${tab.textContent} DB 조회 필요</td></tr>`;
        }
    });
});

// ====== 필터 버튼 클릭 이벤트 ======
document.querySelectorAll("#fund-filter button").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll("#fund-filter button")
            .forEach(b => b.classList.remove("active"));
        btn.classList.add("active");

        const category = btn.dataset.category;
        renderFundList(category);
    });
});

// ====== 페이지 로드 시 DB 데이터 호출 ======
document.addEventListener("DOMContentLoaded", loadFundData);
