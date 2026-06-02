(() => {
  function isActive(activePage, page) {
    return activePage === page ? 'active' : '';
  }

  function renderNav(activePage) {
    const nav = document.getElementById('main-nav');
    if (!nav) {
      return;
    }

    const currentQuery = new URLSearchParams(window.location.search).get('query') || '';

    nav.innerHTML = `
      <nav class="navbar navbar-expand-lg navbar-dark" role="navigation">
        <div class="container-fluid">
          <a class="navbar-brand" href="/">
            <span class="fa fa-paw" aria-hidden="true"></span>
            PetClinic
          </a>
          <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#pcNavbar" aria-controls="pcNavbar" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="pcNavbar">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
              <li class="nav-item ${isActive(activePage, 'home')}">
                <a class="nav-link ${isActive(activePage, 'home')}" href="/">Home</a>
              </li>
              <li class="nav-item ${isActive(activePage, 'owners')}">
                <a class="nav-link ${isActive(activePage, 'owners')}" href="/owners.html">Find Owners</a>
              </li>
              <li class="nav-item ${isActive(activePage, 'vets')}">
                <a class="nav-link ${isActive(activePage, 'vets')}" href="/vets.html">Veterinarians</a>
              </li>
              <li class="nav-item ${isActive(activePage, 'error')}">
                <a class="nav-link ${isActive(activePage, 'error')}" href="/oups">Error</a>
              </li>
            </ul>
            <form class="d-flex ms-lg-auto" action="/search.html" method="get" role="search">
              <input class="form-control me-2" type="search" name="query" placeholder="Search" aria-label="Search" value="${window.escapeHtml(currentQuery)}">
              <button class="btn btn-outline-light" type="submit">Search</button>
            </form>
          </div>
        </div>
      </nav>
    `;
  }

  window.renderNav = renderNav;
})();
