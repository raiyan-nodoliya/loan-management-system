// admin.js (NO CONFLICT) - only adm- elements
(function () {
  // ===== Mobile sidebar toggle
  const mobBtn = document.getElementById("admMobBtn");
  const overlay = document.getElementById("admOverlay");

  function closeSidebar(){
    document.body.classList.remove("adm-open");
  }
  function openSidebar(){
    document.body.classList.add("adm-open");
  }

  if (mobBtn) mobBtn.addEventListener("click", openSidebar);
  if (overlay) overlay.addEventListener("click", closeSidebar);

  // ===== User dropdown
  const user = document.getElementById("admUser");
  const userBtn = document.getElementById("admUserBtn");
  if (user && userBtn) {
    userBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      user.classList.toggle("open");
    });
    document.addEventListener("click", () => user.classList.remove("open"));
  }

  // ===== Active link highlight (based on URL path)
  const links = document.querySelectorAll(".adm-link");
  const path = window.location.pathname || "";

  links.forEach(a => {
    const href = a.getAttribute("href") || "";
    if (href && path.includes(href.replace(/\/$/, ""))) {
      links.forEach(x => x.classList.remove("active"));
      a.classList.add("active");
    }
  });
})();
