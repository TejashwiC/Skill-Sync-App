window.login = async function () {
  try {
    await signInWithEmailAndPassword(auth, el("email").value, el("password").value);
    window.location.href = "dashboard.html";
  } catch (err) { alert("Login Error: " + err.message); }
};