/***************************************************
 * ⭐ 1) 약관/설명서 다운로드 링크 설정
 ***************************************************/
const SERVER_BASE = "http://34.50.37.11:8080/bnk";

function setPdfLinks(product){
    const termsBtn = document.getElementById("termsBtn");
    const investBtn = document.getElementById("investBtn");
    const summaryBtn = document.getElementById("summaryBtn");

    const fundCode = product.fundCode;

    if(!fundCode){
        termsBtn.href = "#";
        investBtn.href = "#";
        summaryBtn.href = "#";
        return;
    }

    termsBtn.href   = `${SERVER_BASE}/upload/terms/${fundCode}_약관.pdf`;
    investBtn.href  = `${SERVER_BASE}/upload/invest/${fundCode}_투자설명서.pdf`;
    summaryBtn.href = `${SERVER_BASE}/upload/summary/${fundCode}_간이투자설명서.pdf`;
}


/***************************************************
 * ====================== 초기 로딩 ======================
 ***************************************************/
document.addEventListener("DOMContentLoaded", function () {

    const fundCode = document.getElementById("fundCode").value;

    // PDF 링크 설정
    setPdfLinks({ fundCode: fundCode });

    // 기본 탭 내용 표시 (상품개요)
    document.getElementById("tab-content").innerHTML = tabData.overview;

    // 상단 기준가 차트 로딩
    fetch(`/bnk/fund/nav/${fundCode}`)
        .then(res => res.json())
        .then(json => {
            const ctx = document.getElementById('fundTrend').getContext('2d');

            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: json.labels,
                    datasets: [{
                        label: '기준가 (원)',
                        data: json.data,
                        borderColor: '#b22222',
                        backgroundColor: 'rgba(178,34,34,0.15)',
                        borderWidth: 2,
                        fill: true,
                        tension: 0.3
                    }]
                },
                options: {
                    plugins: { legend: { display: false } },
                    scales: {
                        x: { ticks: { maxTicksLimit: 5, maxRotation: 0, minRotation: 0 } }
                    }
                }
            });
        });
});


/***************************************************
 * 성별 / 연령대 차트
 ***************************************************/
new Chart(document.getElementById('genderChart'), {
    type: 'doughnut',
    data: {
        labels: ['남성(31%)', '여성(69%)'],
        datasets: [{ data: [31, 69], backgroundColor: ['#4e79a7', '#f28e2b'] }]
    },
    options: { plugins: { legend: { position: 'bottom' } } }
});

new Chart(document.getElementById('ageChart'), {
    type: 'bar',
    data: {
        labels: ['10대', '20대', '30대', '40대', '50대', '60대'],
        datasets: [
            { label: '남성', data: [3, 5, 8, 14, 21, 28], backgroundColor: '#4e79a7' },
            { label: '여성', data: [2, 7, 9, 13, 22, 31], backgroundColor: '#f28e2b' }
        ]
    },
    options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
});


/***************************************************
 * ⭐ 성과분석 – 최근 1년 NAV 차트 로딩 함수
 ***************************************************/
let performanceChart = null;

function loadPerformanceChart(fundCode) {

    fetch(`/bnk/api/fund/nav/year/${fundCode}`)
        .then(res => res.json())
        .then(json => {

            const canvas = document.getElementById("yearPerformanceChart");
            if (!canvas) {
                console.warn("yearPerformanceChart canvas not found yet.");
                return;
            }

            const ctx = canvas.getContext("2d");

            // 기존 차트 제거
            if (performanceChart) {
                performanceChart.destroy();
            }

            performanceChart = new Chart(ctx, {
                type: "line",
                data: {
                    labels: json.labels,
                    datasets: [{
                        label: "최근 1년 기준가",
                        data: json.data,
                        borderColor: "#b22222",
                        backgroundColor: "rgba(178,34,34,0.15)",
                        borderWidth: 2,
                        fill: true,
                        tension: 0.4
                    }]
                },
                options: {
                    responsive: true,
                    plugins: { legend: { position: "top" } }
                }
            });
        })
        .catch(err => console.error("성과 차트 로딩 실패:", err));
}


/***************************************************
 * ====================== 탭 전환 ======================
 ***************************************************/
const tabs = document.querySelectorAll(".detail-tab");
const content = document.getElementById("tab-content");

tabs.forEach(tab => {
    tab.addEventListener("click", () => {

        tabs.forEach(t => t.classList.remove("active"));
        tab.classList.add("active");

        const key = tab.dataset.tab;
        content.innerHTML = tabData[key];

        // ⭐ 성과분석 탭 클릭 시 차트 생성 (DOM 생성 후 50ms 뒤)
        if (key === "performance") {
            const fundCode = document.getElementById("fundCode").value;

            setTimeout(() => {
                loadPerformanceChart(fundCode);
            }, 50);
        }
    });
});


/***************************************************
 * ====================== 탭 데이터 ======================
 ***************************************************/
const tabData = {
    overview: `
        <div class="fund-overview-section">

            <h4 class="ov-title">상품개요</h4>
            <p class="ov-text">
                급격하게 성장하고 변화하는 산업 패러다임에 적극적으로 대응할 수 있는 기술과 경쟁력을 지닌 기업에 투자합니다.<br>
                - IT, 반도체, AI, 전기차 등 미래 성장 산업 중심<br>
                - 장기 수익률과 자본이득을 목표로 합니다.
            </p>

            <h4 class="ov-title">출금(환매)방법</h4>
            <p class="ov-text">
                15시 30분 이전 : 2영업일 기준가 적용 4영업일 연계계좌로 지급<br>
                15시 30분 경과 후 : 3영업일 기준가 적용 4영업일 연계계좌로 지급
            </p>

            <h4 class="ov-title">거래방법</h4>
            <p class="ov-text">
                15시 30분 이전 : 2영업일 기준가 적용 2영업일 입금<br>
                15시 30분 경과 후 : 3영업일 기준가 적용 3영업일 입금
            </p>

            <h4 class="ov-title">펀드유형</h4>
            <p class="ov-text">주식형</p>

            <h4 class="ov-title">투자대상유형</h4>
            <p class="ov-text">일반</p>

            <h4 class="ov-title">투자지역</h4>
            <p class="ov-text">국내</p>

            <h4 class="ov-title">가입방법</h4>
            <p class="ov-text">인터넷뱅킹, 모바일뱅킹</p>

            <h4 class="ov-title">신탁재산운용</h4>
            <p class="ov-text">
                주식 : 신탁재산의 60% 이상 투자<br>
                채권 : 신탁재산의 40% 미만 투자
            </p>

            <h4 class="ov-title">납입방법</h4>
            <p class="ov-text">일시식, 거치식, 적립식</p>

            <h4 class="ov-title">선취수수료</h4>
            <p class="ov-text">납입금액의 0.5%</p>

            <h3 class="caution-title">유의사항</h3>

            <div class="caution-wrapper">
                <table class="caution-table">
                    <tr><th>주요투자위험</th><td>위험관리와 초과수익을 위해 파생상품에 투자할 수 있으며 위험성이 높습니다.</td></tr>
                    <tr><th>상품의 이익 및 손실 발생 구조</th><td>기술혁신성과 사업모델이 우수한 기업에 투자하며 시장 변동에 따라 손익이 달라집니다.</td></tr>
                    <tr><th>원금손실위험</th><td>원금이 보장되지 않으며 손실이 발생할 수 있습니다.</td></tr>
                    <tr><th>예금자보호</th><td>본 상품은 예금자보호법의 보호 대상이 아닙니다.</td></tr>
                    <tr><th>자기책임원칙</th><td>투자 책임은 투자자에게 있으며 설명서를 반드시 읽어야 합니다.</td></tr>
                    <tr><th>금융소비자 권리안내</th><td>불완전판매 시 구제를 받을 수 있으며 금융감독원(1332)에 민원 접수 가능합니다.</td></tr>
                </table>
            </div>
        </div>
    `,

    performance: `
        <h3>성과분석</h3>
        <canvas id="yearPerformanceChart" style="width:100%; height:350px;"></canvas>
    `,
    chart: `<h3>차트분석</h3><p>차트 분석 내용</p>`,
    risk: `<h3>위험분석</h3><p>위험 분석 내용</p>`,
    portfolio: `<h3>포트폴리오분석</h3><p>포트폴리오 분석 내용</p>`,
    bou: `<h3>보유내역</h3><p>보유내역</p>`
};
