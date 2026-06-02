document.addEventListener('DOMContentLoaded', async () => {
  renderNav('owners');

  const params = new URLSearchParams(window.location.search);
  const query = params.get('query') || '';
  const summary = document.getElementById('search-summary');
  const ownersResults = document.getElementById('owners-results');
  const petsResults = document.getElementById('pets-results');
  const vetsResults = document.getElementById('vets-results');

  summary.textContent = query ? `Showing results for "${query}".` : 'Enter a search term in the navigation bar.';

  if (!query.trim()) {
    renderEmptyState(ownersResults, 'No query provided.');
    renderEmptyState(petsResults, 'No query provided.');
    renderEmptyState(vetsResults, 'No query provided.');
    return;
  }

  try {
    const data = await apiFetch(`/api/search?query=${encodeURIComponent(query)}`);
    renderOwners(normalizeCollection(data?.owners || data?.ownerResults));
    renderPets(normalizeCollection(data?.pets || data?.petResults));
    renderVets(normalizeCollection(data?.vets || data?.vetResults));
  }
  catch (error) {
    showError(error.message);
    renderEmptyState(ownersResults, 'Unable to load owner results.');
    renderEmptyState(petsResults, 'Unable to load pet results.');
    renderEmptyState(vetsResults, 'Unable to load veterinarian results.');
  }

  function normalizeCollection(value) {
    if (Array.isArray(value)) {
      return value;
    }
    if (Array.isArray(value?.content)) {
      return value.content;
    }
    return [];
  }

  function renderOwners(owners) {
    if (!owners.length) {
      renderEmptyState(ownersResults, 'No owners matched your search.');
      return;
    }

    ownersResults.innerHTML = `
      <div class="table-responsive">
        <table class="table table-modern mb-0">
          <thead>
            <tr>
              <th>Name</th>
              <th>Address</th>
              <th>City</th>
              <th>Phone</th>
            </tr>
          </thead>
          <tbody>
            ${owners
              .map((owner) => `
                <tr>
                  <td><a href="/owner-detail.html?id=${owner.id}">${escapeHtml(`${owner.firstName || ''} ${owner.lastName || ''}`.trim())}</a></td>
                  <td>${escapeHtml(owner.address || '')}</td>
                  <td>${escapeHtml(owner.city || '')}</td>
                  <td>${escapeHtml(owner.telephone || '')}</td>
                </tr>
              `)
              .join('')}
          </tbody>
        </table>
      </div>
    `;
  }

  function renderPets(pets) {
    if (!pets.length) {
      renderEmptyState(petsResults, 'No pets matched your search.');
      return;
    }

    petsResults.innerHTML = `
      <div class="table-responsive">
        <table class="table table-modern mb-0">
          <thead>
            <tr>
              <th>Name</th>
              <th>Type</th>
              <th>Birth Date</th>
              <th>Owner</th>
            </tr>
          </thead>
          <tbody>
            ${pets
              .map((pet) => {
                const ownerLink = pet.owner?.id || pet.ownerId
                  ? `<a href="/owner-detail.html?id=${pet.owner?.id || pet.ownerId}">${escapeHtml(`${pet.owner?.firstName || ''} ${pet.owner?.lastName || ''}`.trim() || 'View Owner')}</a>`
                  : escapeHtml(`${pet.owner?.firstName || ''} ${pet.owner?.lastName || ''}`.trim() || '-');
                return `
                  <tr>
                    <td>${escapeHtml(pet.pet?.name || '')}</td>
                    <td>${escapeHtml(pet.pet?.type?.name || pet.typeName || '')}</td>
                    <td>${escapeHtml(pet.pet?.birthDate || '')}</td>
                    <td>${ownerLink}</td>
                  </tr>
                `;
              })
              .join('')}
          </tbody>
        </table>
      </div>
    `;
  }

  function renderVets(vets) {
    if (!vets.length) {
      renderEmptyState(vetsResults, 'No veterinarians matched your search.');
      return;
    }

    vetsResults.innerHTML = `
      <div class="table-responsive">
        <table class="table table-modern mb-0">
          <thead>
            <tr>
              <th>Name</th>
              <th>Specialties</th>
            </tr>
          </thead>
          <tbody>
            ${vets
              .map((vet) => `
                <tr>
                  <td>${escapeHtml(`${vet.firstName || ''} ${vet.lastName || ''}`.trim())}</td>
                  <td>${(vet.specialties || []).length ? vet.specialties.map((specialty) => `<span class="badge-specialty">${escapeHtml(specialty.name)}</span>`).join(' ') : '<span class="text-muted">none</span>'}</td>
                </tr>
              `)
              .join('')}
          </tbody>
        </table>
      </div>
    `;
  }

  function renderEmptyState(element, message) {
    element.innerHTML = `<p class="text-muted mb-0">${escapeHtml(message)}</p>`;
  }
});
