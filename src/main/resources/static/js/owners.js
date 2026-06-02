document.addEventListener('DOMContentLoaded', () => {
  renderNav('owners');

  const form = document.getElementById('owners-search-form');
  const input = document.getElementById('lastName');
  const tableBody = document.getElementById('owners-table-body');
  const pagination = document.getElementById('owners-pagination');
  const summary = document.getElementById('owners-summary');
  const params = new URLSearchParams(window.location.search);
  const initialLastName = params.get('lastName') || '';
  const initialPage = Number.parseInt(params.get('page') || '0', 10) || 0;

  input.value = initialLastName;

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    loadOwners(0, input.value.trim());
  });

  loadOwners(initialPage, initialLastName);

  async function loadOwners(page, lastName) {
    try {
      tableBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Loading...</td></tr>';
      const searchParams = new URLSearchParams({ page: String(page), size: '10' });
      if (lastName) {
        searchParams.set('lastName', lastName);
      }

      const data = await apiFetch(`/api/owners?${searchParams.toString()}`);
      updateUrl(page, lastName);
      renderOwners(data?.content || []);
      renderPagination(data, lastName);
      renderSummary(data);
    }
    catch (error) {
      tableBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Unable to load owners.</td></tr>';
      pagination.innerHTML = '';
      summary.textContent = '';
      showError(error.message);
    }
  }

  function updateUrl(page, lastName) {
    const nextParams = new URLSearchParams();
    if (lastName) {
      nextParams.set('lastName', lastName);
    }
    nextParams.set('page', String(page));
    const nextUrl = `/owners.html?${nextParams.toString()}`;
    window.history.replaceState({}, '', nextUrl);
  }

  function renderOwners(owners) {
    if (!owners.length) {
      tableBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No owners found.</td></tr>';
      return;
    }

    tableBody.innerHTML = owners
      .map((owner) => {
        const pets = (owner.pets || []).map((pet) => escapeHtml(pet.name)).join(', ') || '-';
        return `
          <tr>
            <td><a href="/owner-detail.html?id=${owner.id}">${escapeHtml(owner.lastName)}</a></td>
            <td>${escapeHtml(owner.firstName)}</td>
            <td>${escapeHtml(owner.address)}</td>
            <td>${escapeHtml(owner.city)}</td>
            <td>${escapeHtml(owner.telephone)}</td>
            <td>${pets}</td>
          </tr>
        `;
      })
      .join('');
  }

  function renderPagination(data, lastName) {
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
        loadOwners(Number.parseInt(button.dataset.page, 10), lastName);
      });
    });
  }

  function renderSummary(data) {
    const totalElements = data?.totalElements || 0;
    const page = (data?.number || 0) + 1;
    const totalPages = Math.max(data?.totalPages || 0, totalElements ? 1 : 0);
    summary.textContent = totalElements ? `${totalElements} owner(s) found · Page ${page} of ${totalPages}` : '0 owner found';
  }
});
