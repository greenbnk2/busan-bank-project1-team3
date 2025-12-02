// ===== 상단 기준가 추이 =====
// ====================== 상단 기준가 추이 (DB 연동) ======================
document.addEventListener("DOMContentLoaded", function () {

    const fundCode = document.getElementById("fundCode").value;

    fetch(`/bnk/fund/nav/${fundCode}`)
        .then(res => res.json())
        .then(json => {
            const ctx = document.getElementById('fundTrend').getContext('2d');

            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: json.labels,          // ['2025-01-01', '2025-01-02', ...]
                    datasets: [{
                        label: '기준가 (원)',
                        data: json.data,          // [10200.33, 10210.50 ...]
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
                        x: {
                            ticks: {
                                maxTicksLimit: 5,
                                maxRotation: 0,
                                minRotation: 0
                            }
                        }
                    }
                }
            });
        })
        .catch(err => console.error("차트 데이터 로딩 실패:", err));
});

// ===== 성별 분석 =====
new Chart(document.getElementById('genderChart'), {
    type: 'doughnut',
    data: {
        labels: ['남성(31%)', '여성(69%)'],
        datasets: [{ data: [31, 69], backgroundColor: ['#4e79a7', '#f28e2b'] }]
    },
    options: { plugins: { legend: { position: 'bottom' } } }
});

// ===== 연령대별 분석 =====
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

// ===== 탭 전환 =====
const tabs = document.querySelectorAll(".detail-tab");
const content = document.getElementById("tab-content");

const tabData = {
    overview: `
        <h3>상품개요</h3>
        <p>급격하게 성장하고 변화하는 산업 패러다임에 적극적으로 대응할 수 있는 기술과 경쟁력을 지닌 기업에 투자합니다.<br>
        - IT, 반도체, AI, 전기차 등 미래 성장 산업 중심<br>
        - 장기 수익률과 자본이득을 목표로 합니다.</p>`,
    nav: `<h3>펀드기준가</h3><p>DB 연동으로 최근 기준가 데이터를 불러올 예정입니다.</p>`,
    performance: `<h3>성과분석</h3><p>최근 6개월 수익률: +94.51%, 1년 수익률: +79.68%</p>`,
    chart: `<h3>차트분석</h3><p>기준가 변동 및 기간별 수익률 그래프 예정</p>`,
    risk: `<h3>위험분석</h3><p>변동성, 손실률 등 리스크 지표 표시</p>`,
    portfolio: `<h3>포트폴리오분석</h3><p>보유 자산 비율 시각화 예정</p>`,
    bou: `<h3>보유내역</h3><p>사용자 보유 펀드 내역 표시</p>`
};

tabs.forEach(tab => {
    tab.addEventListener("click", () => {
        tabs.forEach(t => t.classList.remove("active"));
        tab.classList.add("active");
        content.innerHTML = tabData[tab.dataset.tab];
    });
});
