(() => {
  function getAlertContainer() {
    return document.getElementById('alert-container');
  }

  function escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/\"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function renderAlert(message, type) {
    const container = getAlertContainer();
    if (!container) {
      return;
    }

    container.innerHTML = `
      <div class="alert alert-${type} alert-dismissible fade show" role="alert">
        ${escapeHtml(message)}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
      </div>
    `;
  }

  async function apiFetch(path, options = {}) {
    const headers = {
      Accept: 'application/json',
      ...(options.headers || {})
    };
    const config = {
      ...options,
      headers
    };

    if (config.body && typeof config.body !== 'string' && !(config.body instanceof FormData)) {
      if (!headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
      }
      config.body = JSON.stringify(config.body);
    }

    const response = await fetch(path, config);
    let body = null;

    try {
      body = await response.json();
    }
    catch (error) {
      body = null;
    }

    if (!response.ok || body?.success === false) {
      throw new Error(body?.error?.message || response.statusText || 'Request failed');
    }

    return body?.data;
  }

  window.apiFetch = apiFetch;
  window.showError = (message) => renderAlert(message, 'danger');
  window.showSuccess = (message) => renderAlert(message, 'success');
  window.escapeHtml = escapeHtml;
})();
