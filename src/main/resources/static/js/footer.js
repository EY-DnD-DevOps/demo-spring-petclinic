(() => {
  function renderFooter() {
    const year = document.getElementById('footer-year');
    if (year) {
      year.textContent = new Date().getFullYear();
    }

    const updated = document.getElementById('footer-updated');
    if (!updated) {
      return;
    }

    fetch('/api/app-info', { headers: { Accept: 'application/json' } })
      .then((response) => response.json())
      .then((body) => {
        if (body && body.success && body.data && body.data.lastUpdatedAt) {
          updated.textContent = ` · Last updated at ${body.data.lastUpdatedAt}`;
        }
      })
      .catch(() => {
        // Footer info is best-effort; ignore failures.
      });
  }

  renderFooter();
})();
