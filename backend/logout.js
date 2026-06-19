window.logout = async function () {
  await signOut(auth);
  window.location.href = "login.html";
};