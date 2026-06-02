document.addEventListener('DOMContentLoaded', async () => {
  renderNav('owners');

  const params = new URLSearchParams(window.location.search);
  const ownerId = params.get('ownerId');
  const petId = params.get('petId');
  const isEditMode = Boolean(petId);
  const form = document.getElementById('pet-form');
  const typeSelect = document.getElementById('typeId');
  const pageTitle = document.getElementById('page-title');
  const pageDescription = document.getElementById('page-description');
  const submitButton = document.getElementById('submit-button');
  const backToOwnerLink = document.getElementById('back-to-owner-link');

  if (!ownerId) {
    showError('Owner id is required.');
    submitButton.disabled = true;
    return;
  }

  backToOwnerLink.href = `/owner-detail.html?id=${ownerId}`;

  if (isEditMode) {
    pageTitle.textContent = 'Update Pet';
    pageDescription.textContent = 'Update pet details for this owner.';
    submitButton.textContent = 'Update Pet';
  }

  try {
    const [petTypes, owner] = await Promise.all([
      apiFetch('/api/pet-types'),
      apiFetch(`/api/owners/${ownerId}`)
    ]);

    renderPetTypes(petTypes || []);

    if (isEditMode) {
      const pet = (owner.pets || []).find((item) => String(item.id) === petId);
      if (!pet) {
        throw new Error('Pet not found for this owner.');
      }
      document.getElementById('name').value = pet.name || '';
      document.getElementById('birthDate').value = pet.birthDate || '';
      typeSelect.value = pet.type?.id ? String(pet.type.id) : '';
    }
  }
  catch (error) {
    submitButton.disabled = true;
    showError(error.message);
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const payload = {
      name: document.getElementById('name').value.trim(),
      birthDate: document.getElementById('birthDate').value,
      type: {
        id: Number(typeSelect.value)
      }
    };

    if (isEditMode) {
      payload.id = Number(petId);
    }

    try {
      submitButton.disabled = true;
      await apiFetch(isEditMode ? `/api/owners/${ownerId}/pets/${petId}` : `/api/owners/${ownerId}/pets`, {
        method: isEditMode ? 'PUT' : 'POST',
        body: payload
      });
      showSuccess(`Pet ${isEditMode ? 'updated' : 'created'} successfully.`);
      window.location.href = `/owner-detail.html?id=${ownerId}`;
    }
    catch (error) {
      submitButton.disabled = false;
      showError(error.message);
    }
  });

  function renderPetTypes(petTypes) {
    if (!petTypes.length) {
      typeSelect.innerHTML = '<option value="">No pet types available</option>';
      return;
    }

    typeSelect.innerHTML = '<option value="">Select a type</option>' + petTypes
      .map((petType) => `<option value="${petType.id}">${escapeHtml(petType.name)}</option>`)
      .join('');
  }
});
