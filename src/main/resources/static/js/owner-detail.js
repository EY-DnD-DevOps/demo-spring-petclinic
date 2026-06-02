document.addEventListener('DOMContentLoaded', async () => {
  renderNav('owners');

  const params = new URLSearchParams(window.location.search);
  const ownerId = params.get('id');
  const ownerName = document.getElementById('owner-name');
  const ownerAddress = document.getElementById('owner-address');
  const ownerCity = document.getElementById('owner-city');
  const ownerPhone = document.getElementById('owner-phone');
  const petsTableBody = document.getElementById('pets-table-body');
  const editOwnerLink = document.getElementById('edit-owner-link');
  const addPetLink = document.getElementById('add-pet-link');

  if (!ownerId) {
    showError('Owner id is required.');
    petsTableBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Owner id is missing.</td></tr>';
    return;
  }

  editOwnerLink.href = `/owner-form.html?id=${ownerId}`;
  addPetLink.href = `/pet-form.html?ownerId=${ownerId}`;

  try {
    const owner = await apiFetch(`/api/owners/${ownerId}`);
    ownerName.textContent = `${owner.firstName || ''} ${owner.lastName || ''}`.trim() || '-';
    ownerAddress.textContent = owner.address || '-';
    ownerCity.textContent = owner.city || '-';
    ownerPhone.textContent = owner.telephone || '-';
    renderPets(owner.pets || [], ownerId);
  }
  catch (error) {
    petsTableBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Unable to load owner details.</td></tr>';
    showError(error.message);
  }

  function renderPets(pets, currentOwnerId) {
    if (!pets.length) {
      petsTableBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No pets registered for this owner.</td></tr>';
      return;
    }

    petsTableBody.innerHTML = pets
      .map((pet) => {
        const visits = (pet.visits || []).length
          ? `<ul class="mb-0 ps-3">${pet.visits
              .map((visit) => `<li>${escapeHtml(visit.date)} - ${escapeHtml(visit.description)}</li>`)
              .join('')}</ul>`
          : '<span class="text-muted">No visits</span>';

        return `
          <tr>
            <td>${escapeHtml(pet.name)}</td>
            <td>${escapeHtml(pet.birthDate)}</td>
            <td>${escapeHtml(pet.type?.name || '')}</td>
            <td>${visits}</td>
            <td>
              <div class="d-flex gap-2 flex-wrap">
                <a class="btn btn-sm btn-outline-primary" href="/pet-form.html?ownerId=${escapeHtml(String(currentOwnerId))}&petId=${escapeHtml(String(pet.id))}">Edit Pet</a>
                <a class="btn btn-sm btn-primary" href="/visit-form.html?ownerId=${escapeHtml(String(currentOwnerId))}&petId=${escapeHtml(String(pet.id))}">Add Visit</a>
              </div>
            </td>
          </tr>
        `;
      })
      .join('');
  }
});
