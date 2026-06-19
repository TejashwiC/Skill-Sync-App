window.loadUser = function () {
  onAuthStateChanged(auth, (user) => {
    if (!user) {
      if (location.pathname.includes("dashboard")) window.location.href = "login.html";
      return;
    }
    loadProfile();
    loadUsers();
    loadFollowers();
    loadFollowing();
    loadPDFs();
    loadChatUsers();
    loadChatUserList();
    loadInbox();
    loadChatSettingsPanel();
    loadBlockedUsers();
    loadLiveSessions();
    loadSessionHistory();
    loadRatingsContainer();
    loadMyActiveSession();
    // Auto-update home stats live
    listenHomeStats();
  });
};