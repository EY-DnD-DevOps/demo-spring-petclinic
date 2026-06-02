document.addEventListener('DOMContentLoaded', async () => {
  renderNav('owners');

  const params = new URLSearchParams(window.location.search);
  const ownerId = params.get('id');
  const isEditMode = Boolean(ownerId);
  const form = document.getElementById('owner-form');
  const pageTitle = document.getElementById('page-title');
  const pageDescription = document.getElementById('page-description');
  const submitButton = document.getElementById('submit-button');

  if (isEditMode) {
    pageTitle.textContent = 'Update Owner';
    pageDescription.textContent = 'Update owner profile details.';
    submitButton.textContent = 'Update Owner';

    try {
      const owner = await apiFetch(`/api/owners/${ownerId}`);
      document.getElementById('firstName').value = owner.firstName || '';
      document.getElementById('lastName').value = owner.lastName || '';
      document.getElementById('address').value = owner.address || '';
      document.getElementById('city').value = owner.city || '';
      document.getElementById('telephone').value = owner.telephone || '';
    }
    catch (error) {
      showError(error.message);
    }
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const payload = {
      firstName: document.getElementById('firstName').value.trim(),
      lastName: document.getElementById('lastName').value.trim(),
      address: document.getElementById('address').value.trim(),
      city: document.getElementById('city').value.trim(),
      telephone: document.getElementById('telephone').value.trim()
    };

    try {
      submitButton.disabled = true;
      const data = await apiFetch(isEditMode ? `/api/owners/${ownerId}` : '/api/owners', {
        method: isEditMode ? 'PUT' : 'POST',
        body: isEditMode ? { id: Number(ownerId), ...payload } : payload
      });
      const nextOwnerId = data?.id || ownerId;
      showSuccess(`Owner ${isEditMode ? 'updated' : 'created'} successfully.`);
      window.location.href = `/owner-detail.html?id=${nextOwnerId}`;
    }
    catch (error) {
      submitButton.disabled = false;
      showError(error.message);
    }
  });
});
