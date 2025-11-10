(function () {
    const TYPES = ["ASSET", "REGION", "STRATEGY", "THEME", "TAG"];
    const seed = [
        { id: 1, code: "CAT-001", nameKo: "펀드", slug: "fund", type: "THEME", parentId: null, parentName: null, depth: 0, sortOrder: 10, active: true, usage: 0, createdAt: "2025-11-10" },
        { id: 10, code: "CAT-010", nameKo: "주식형", slug: "equity", type: "ASSET", parentId: 1, parentName: "펀드", depth: 1, sortOrder: 10, active: true, usage: 14, createdAt: "2025-11-10" },
        { id: 11, code: "CAT-011", nameKo: "채권형", slug: "bond", type: "ASSET", parentId: 1, parentName: "펀드", depth: 1, sortOrder: 20, active: true, usage: 9, createdAt: "2025-11-10" },
        { id: 20, code: "CAT-020", nameKo: "국내", slug: "kr", type: "REGION", parentId: 1, parentName: "펀드", depth: 1, sortOrder: 10, active: true, usage: 21, createdAt: "2025-11-10" },
        { id: 21, code: "CAT-021", nameKo: "글로벌", slug: "global", type: "REGION", parentId: 1, parentName: "펀드", depth: 1, sortOrder: 20, active: true, usage: 18, createdAt: "2025-11-10" },
        { id: 30, code: "CAT-030", nameKo: "액티브", slug: "active", type: "STRATEGY", parentId: 1, parentName: "펀드", depth: 1, sortOrder: 10, active: true, usage: 15, createdAt: "2025-11-10" },
        { id: 31, code: "CAT-031", nameKo: "패시브", slug: "passive", type: "STRATEGY", parentId: 1, parentName: "펀드", depth: 1, sortOrder: 20, active: true, usage: 9, createdAt: "2025-11-10" },
        { id: 40, code: "CAT-040", nameKo: "AI·반도체", slug: "ai-semi", type: "THEME", parentId: 1, parentName: "펀드", depth: 1, sortOrder: 10, active: true, usage: 7, createdAt: "2025-11-10" }
    ];

    const state = {
        data: seed.map((item) => ({ ...item })),
        filtered: [],
        keyword: "",
        type: "",
        activeOnly: false,
        usageZeroOnly: false
    };

    const rowsEl = document.getElementById("categoryRows");
    const rowCountEl = document.getElementById("rowCount");
    const summaryTotalEl = document.getElementById("summaryTotal");
    const summaryActiveEl = document.getElementById("summaryActive");
    const summaryInactiveEl = document.getElementById("summaryInactive");
    const summaryUsageEl = document.getElementById("summaryUsage");
    const summaryUpdatedEl = document.getElementById("summaryUpdated");

    const keywordEl = document.getElementById("filterKeyword");
    const typeEl = document.getElementById("filterType");
    const activeOnlyEl = document.getElementById("filterActiveOnly");
    const usageZeroEl = document.getElementById("filterUsageZero");

    const modalOverlay = document.getElementById("categoryModal");
    const modalTitle = document.getElementById("modalTitle");
    const modalForm = document.getElementById("modalForm");
    const modalParent = document.getElementById("modalParent");
    const modalType = document.getElementById("modalType");
    const modalName = document.getElementById("modalName");
    const modalSlug = document.getElementById("modalSlug");
    const modalSort = document.getElementById("modalSort");
    const modalActive = document.getElementById("modalActive");
    const slugAutoBtn = document.getElementById("slugAutoBtn");
    const openModalBtn = document.getElementById("openModalBtn");
    const modalCancel = document.getElementById("modalCancel");

    let editing = null;

    function init() {
        TYPES.forEach((type) => {
            const option = document.createElement("option");
            option.value = type;
            option.textContent = type;
            typeEl.appendChild(option.cloneNode(true));
            modalType.appendChild(option);
        });

        keywordEl.addEventListener("input", handleKeyword);
        typeEl.addEventListener("change", handleTypeFilter);
        activeOnlyEl.addEventListener("change", handleActiveFilter);
        usageZeroEl.addEventListener("change", handleUsageFilter);
        rowsEl.addEventListener("click", handleRowAction);
        openModalBtn.addEventListener("click", () => openModal());
        modalCancel.addEventListener("click", closeModal);
        modalOverlay.addEventListener("click", (event) => {
            if (event.target === modalOverlay) closeModal();
        });
        modalActive.addEventListener("click", () => toggleActiveButton(modalActive.dataset.value !== "true"));
        slugAutoBtn.addEventListener("click", () => setSlug(autoSlugFromName(modalName.value)));
        modalForm.addEventListener("submit", handleSubmit);

        updateSummary();
        applyFilters();
    }

    function handleKeyword() {
        state.keyword = keywordEl.value.trim().toLowerCase();
        applyFilters();
    }

    function handleTypeFilter() {
        state.type = typeEl.value;
        applyFilters();
    }

    function handleActiveFilter() {
        state.activeOnly = activeOnlyEl.checked;
        applyFilters();
    }

    function handleUsageFilter() {
        state.usageZeroOnly = usageZeroEl.checked;
        applyFilters();
    }

    function applyFilters() {
        state.filtered = state.data
            .filter((item) => {
                if (state.type && item.type !== state.type) return false;
                if (state.activeOnly && !item.active) return false;
                if (state.usageZeroOnly && item.usage !== 0) return false;
                if (state.keyword) {
                    const haystack = `${item.nameKo}${item.slug}${item.code}`.toLowerCase();
                    if (!haystack.includes(state.keyword)) return false;
                }
                return true;
            })
            .sort((a, b) => a.sortOrder - b.sortOrder);

        renderTable();
    }

    function renderTable() {
        if (state.filtered.length === 0) {
            rowsEl.innerHTML = '<tr><td colspan="8" class="empty-row">조건에 맞는 항목이 없습니다.</td></tr>';
            rowCountEl.textContent = "0";
            return;
        }

        const rows = state.filtered
            .map((item) => {
                const indent = item.depth > 0 ? `<span class="indent">${"⎯".repeat(item.depth)}</span>` : "";
                const parent = item.parentName || "-";
                const toggleClass = item.active ? "toggle-pill on" : "toggle-pill";
                const toggleLabel = item.active ? "ON" : "OFF";
                return `
                    <tr data-id="${item.id}">
                        <td class="code">${item.code}</td>
                        <td class="name">${indent}<span>${item.nameKo}</span></td>
                        <td><span class="type-badge">${item.type}</span></td>
                        <td>${parent}</td>
                        <td>${item.slug}</td>
                        <td>${item.usage}</td>
                        <td><button class="${toggleClass}" data-action="toggle">${toggleLabel}</button></td>
                        <td>
                            <div class="row-actions">
                                <button data-action="edit">수정</button>
                                <button data-action="child">하위 추가</button>
                                <button data-action="delete" class="danger">삭제</button>
                            </div>
                        </td>
                    </tr>
                `;
            })
            .join("");

        rowsEl.innerHTML = rows;
        rowCountEl.textContent = String(state.filtered.length);
    }

    function handleRowAction(event) {
        const button = event.target.closest("button");
        if (!button) return;

        const row = button.closest("tr");
        const id = Number(row.dataset.id);
        const item = state.data.find((d) => d.id === id);
        if (!item) return;

        const { action } = button.dataset;
        if (action === "toggle") {
            item.active = !item.active;
            item.updatedAt = new Date().toISOString();
            updateSummary();
            applyFilters();
            return;
        }

        if (action === "edit") {
            openModal(item);
            return;
        }

        if (action === "child") {
            openModal({ parentId: item.id });
            return;
        }

        if (action === "delete") {
            deleteCategory(item);
        }
    }

    function deleteCategory(item) {
        if (item.usage > 0) {
            alert("사용 중인 카테고리는 삭제할 수 없습니다. 숨김(노출 OFF)으로 전환하세요.");
            return;
        }
        if (!confirm(`삭제할까요? (${item.nameKo})`)) return;
        state.data = state.data.filter((d) => d.id !== item.id);
        updateSummary();
        applyFilters();
    }

    function openModal(item) {
        populateParentOptions();

        if (item && item.id) {
            editing = { ...item };
            modalTitle.textContent = "카테고리 수정";
            modalParent.value = item.parentId ?? "";
            modalType.value = item.type;
            modalName.value = item.nameKo;
            setSlug(item.slug);
            modalSort.value = item.sortOrder;
            toggleActiveButton(item.active);
        } else {
            editing = { id: 0, parentId: item?.parentId ?? null };
            modalTitle.textContent = "카테고리 등록";
            modalParent.value = item?.parentId ?? "";
            modalType.value = "ASSET";
            modalName.value = "";
            setSlug("");
            modalSort.value = "10";
            toggleActiveButton(true);
        }

        modalOverlay.classList.add("show");
        modalName.focus();
    }

    function closeModal() {
        modalOverlay.classList.remove("show");
        editing = null;
        modalForm.reset();
        toggleActiveButton(true);
    }

    function handleSubmit(event) {
        event.preventDefault();

        const name = modalName.value.trim();
        const slug = modalSlug.value.trim();
        if (name.length < 2) {
            alert("카테고리명은 2자 이상 입력해주세요.");
            modalName.focus();
            return;
        }
        if (!/^[a-z0-9-]+$/.test(slug)) {
            alert("slug는 소문자/숫자/하이픈만 사용할 수 있습니다.");
            modalSlug.focus();
            return;
        }

        const parentId = modalParent.value ? Number(modalParent.value) : null;
        if (!isNameUniqueWithinParent(name, parentId, editing?.id ?? 0)) {
            alert("동일 상위에 같은 이름이 존재합니다.");
            modalName.focus();
            return;
        }

        if (!isSlugUnique(slug, editing?.id ?? 0)) {
            alert("slug가 중복되었습니다.");
            modalSlug.focus();
            return;
        }

        const parent = parentId ? state.data.find((d) => d.id === parentId) : null;
        const record = {
            id: editing?.id ?? 0,
            code: editing?.code ?? "",
            nameKo: name,
            slug,
            type: modalType.value,
            parentId,
            parentName: parent?.nameKo ?? null,
            depth: parent ? parent.depth + 1 : 0,
            sortOrder: Number(modalSort.value) || 10,
            active: modalActive.dataset.value === "true",
            usage: editing?.usage ?? 0,
            createdAt: editing?.createdAt ?? new Date().toISOString(),
            updatedAt: new Date().toISOString()
        };

        if (record.id === 0) {
            const nextId = state.data.reduce((maxId, current) => Math.max(maxId, current.id), 0) + 1;
            record.id = nextId;
            record.code = `CAT-${String(nextId).padStart(3, "0")}`;
            state.data.push(record);
        } else {
            state.data = state.data.map((d) => (d.id === record.id ? record : d));
        }

        updateSummary();
        applyFilters();
        closeModal();
    }

    function populateParentOptions() {
        modalParent.innerHTML = '<option value="">(없음)</option>';
        state.data
            .filter((item) => item.parentId === null)
            .sort((a, b) => a.sortOrder - b.sortOrder)
            .forEach((item) => {
                const option = document.createElement("option");
                option.value = String(item.id);
                option.textContent = item.nameKo;
                modalParent.appendChild(option);
            });
    }

    function toggleActiveButton(on) {
        modalActive.dataset.value = on ? "true" : "false";
        modalActive.classList.toggle("on", on);
        modalActive.textContent = on ? "ON" : "OFF";
    }

    function setSlug(value) {
        modalSlug.value = value;
    }

    function autoSlugFromName(source) {
        const base = (source || "")
            .trim()
            .toLowerCase()
            .replace(/\s+/g, "-")
            .replace(/[^a-z0-9-]/g, "");
        let candidate = base || "category";
        let counter = 2;
        while (!isSlugUnique(candidate, editing?.id ?? 0)) {
            candidate = `${base || "category"}-${counter++}`;
        }
        return candidate;
    }

    function isSlugUnique(slug, excludeId) {
        return !state.data.some((item) => item.slug === slug && item.id !== excludeId);
    }

    function isNameUniqueWithinParent(name, parentId, excludeId) {
        return !state.data.some(
            (item) => item.nameKo === name && item.parentId === parentId && item.id !== excludeId
        );
    }

    function updateSummary() {
        const total = state.data.length;
        const active = state.data.filter((item) => item.active).length;
        const usageSum = state.data.reduce((sum, item) => sum + (item.usage || 0), 0);
        const latest = state.data.reduce((latestTs, item) => {
            const timestamp = item.updatedAt || item.createdAt;
            return timestamp && timestamp > latestTs ? timestamp : latestTs;
        }, "");

        summaryTotalEl.textContent = String(total);
        summaryActiveEl.textContent = String(active);
        summaryInactiveEl.textContent = String(total - active);
        summaryUsageEl.textContent = String(usageSum);
        summaryUpdatedEl.textContent = latest ? latest.substring(0, 10) : "-";
    }

    document.addEventListener("DOMContentLoaded", init);
})();
