document.addEventListener('DOMContentLoaded', () => {
  renderNav('vets');

  const tableBody = document.getElementById('vets-table-body');
  const pagination = document.getElementById('vets-pagination');
  const params = new URLSearchParams(window.location.search);
  const initialPage = Number.parseInt(params.get('page') || '0', 10) || 0;

  loadVets(initialPage);

  async function loadVets(page) {
    try {
      tableBody.innerHTML = '<tr><td colspan="2" class="text-center text-muted">Loading...</td></tr>';
      const data = await apiFetch(`/api/vets?page=${page}&size=10`);
      renderVets(data?.content || []);
      renderPagination(data);
      updateUrl(page);
    }
    catch (error) {
      tableBody.innerHTML = '<tr><td colspan="2" class="text-center text-muted">Unable to load veterinarians.</td></tr>';
      pagination.innerHTML = '';
      showError(error.message);
    }
  }

  function updateUrl(page) {
    window.history.replaceState({}, '', `/vets.html?page=${page}`);
  }

  function renderVets(vets) {
    if (!vets.length) {
      tableBody.innerHTML = '<tr><td colspan="2" class="text-center text-muted">No veterinarians found.</td></tr>';
      return;
    }

    tableBody.innerHTML = vets
      .map((vet) => {
        const specialties = (vet.specialties || []).length
          ? vet.specialties.map((specialty) => `<span class="badge-specialty">${escapeHtml(specialty.name)}</span>`).join(' ')
          : '<span class="text-muted">none</span>';

        return `
          <tr>
            <td>${escapeHtml(`${vet.firstName || ''} ${vet.lastName || ''}`.trim())}</td>
            <td>${specialties}</td>
          </tr>
        `;
      })
      .join('');
  }

  function renderPagination(data) {
    const page = data?.number || 0;
    const totalPages = data?.totalPages || 0;
    const totalElements = data?.totalElements || 0;

    if (!totalElements) {
      pagination.innerHTML = '';
      return;
    }

    const previousDisabled = page <= 0 ? 'disabled' : '';
    const nextDisabled = page >= totalPages - 1 ? 'disabled' : '';

    pagination.innerHTML = `
      <button class="btn btn-outline-secondary" ${previousDisabled} data-page="${page - 1}">Previous</button>
      <span class="pc-page-num pc-page-current">${page + 1}</span>
      <span class="pc-page-num">/ ${Math.max(totalPages, 1)}</span>
      <button class="btn btn-outline-secondary" ${nextDisabled} data-page="${page + 1}">Next</button>
    `;

    pagination.querySelectorAll('button[data-page]').forEach((button) => {
      button.addEventListener('click', () => {
        loadVets(Number.parseInt(button.dataset.page, 10));
      });
    });
  }
});
