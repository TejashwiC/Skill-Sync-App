window.showSection = function (id) {
  document.querySelectorAll(".section").forEach(s => s.style.display = "none");
  el(id).style.display = "block";
};

/* ================================================
   HOME — Auto-update Tests Created & Tests Completed
   ================================================ */
function listenHomeStats() {
  const user = auth.currentUser;
  if (!user) return;

  // Listen to tests created by this user (real-time)
  onSnapshot(
    query(collection(db, "tests"), where("creatorId", "==", user.uid)),
    snap => {
      if (el("homeTests")) el("homeTests").innerText = snap.size;
    }
  );

  // Listen to test attempts completed by this user (real-time)
  onSnapshot(
    query(collection(db, "testAttempts"), where("userId", "==", user.uid)),
    snap => {
      if (el("homeTestsCompleted")) el("homeTestsCompleted").innerText = snap.size;
    }
  );
}