// dashboard.js (NO CONFLICT) - only db- elements
(function () {
  // ===== User dropdown (top-right)
  const dbUser = document.getElementById("dbUser");
  const dbUserBtn = document.getElementById("dbUserBtn");

  if (dbUser && dbUserBtn) {
    dbUserBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      dbUser.classList.toggle("open");
    });

    document.addEventListener("click", () => {
      dbUser.classList.remove("open");
    });
  }

  // ===== Application Details: Upload button demo
  const uploadBtn = document.getElementById("dbUploadBtn");
  if (uploadBtn) {
    uploadBtn.addEventListener("click", () => {
      alert("Demo: Upload will be connected with backend later.");
    });
  }

  // ===== EMI Schedule: demo rows (if tbody exists)
  const tbody = document.getElementById("dbEmiTbody");
  if (tbody) {
    const rows = [];
    let balance = 500000;

    for (let m = 1; m <= 36; m++) {
      const emi = 16488;
      const interest = Math.max(0, Math.round(balance * 0.115 / 12));
      const principal = Math.max(0, emi - interest);
      balance = Math.max(0, balance - principal);

      rows.push(`
        <tr>
          <td>${m}</td>
          <td>₹${emi.toLocaleString("en-IN")}</td>
          <td>₹${principal.toLocaleString("en-IN")}</td>
          <td>₹${interest.toLocaleString("en-IN")}</td>
          <td>₹${balance.toLocaleString("en-IN")}</td>
        </tr>
      `);
    }

    tbody.innerHTML = rows.join("");
  }
})();
