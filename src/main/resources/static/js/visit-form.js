document.addEventListener('DOMContentLoaded', () => {
  renderNav('owners');

  const params = new URLSearchParams(window.location.search);
  const ownerId = params.get('ownerId');
  const petId = params.get('petId');
  const form = document.getElementById('visit-form');
  const backToOwnerLink = document.getElementById('back-to-owner-link');

  if (!ownerId || !petId) {
    showError('Owner id and pet id are required.');
    return;
  }

  backToOwnerLink.href = `/owner-detail.html?id=${ownerId}`;

  form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const payload = {
      date: document.getElementById('date').value,
      description: document.getElementById('description').value.trim()
    };

    try {
      await apiFetch(`/api/owners/${ownerId}/pets/${petId}/visits`, {
        method: 'POST',
        body: payload
      });
      showSuccess('Visit added successfully.');
      window.location.href = `/owner-detail.html?id=${ownerId}`;
    }
    catch (error) {
      showError(error.message);
    }
  });
});
