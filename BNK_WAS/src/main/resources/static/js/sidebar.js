document.addEventListener("DOMContentLoaded", () => {
    const buttons = document.querySelectorAll(".menu-btn");

    buttons.forEach(btn => {
        btn.addEventListener("click", () => {
            const parent = btn.parentElement;
            const submenu = parent.querySelector(".submenu");
            const arrow = btn.querySelector(".arrow");

            // --- 다른 아코디언 모두 닫기 ---
            document.querySelectorAll(".menu-item").forEach(item => {
                if (item !== parent) {
                    item.classList.remove("open");

                    const otherSubmenu = item.querySelector(".submenu");
                    const otherBtn = item.querySelector(".menu-btn");
                    const otherArrow = item.querySelector(".arrow");

                    if (otherSubmenu) otherSubmenu.style.display = "none";
                    if (otherBtn) otherBtn.classList.remove("active");
                    if (otherArrow) otherArrow.textContent = "▶";
                }
            });

            // --- 현재 메뉴 토글 ---
            parent.classList.toggle("open");

            if (submenu.style.display === "block") {
                submenu.style.display = "none";
                btn.classList.remove("active");
                arrow.textContent = "▶";
            } else {
                submenu.style.display = "block";
                btn.classList.add("active");
                arrow.textContent = "▼";
            }
            // ===== 사이드바 섀도우 효과 =====
            window.addEventListener("scroll", () => {
                const sidebar = document.querySelector(".sidebar");

                if (window.scrollY > 150) {   // 원하는 기준 높이
                    sidebar.classList.add("sticky-shadow");
                } else {
                    sidebar.classList.remove("sticky-shadow");
                }
            });
        });
    });
});
