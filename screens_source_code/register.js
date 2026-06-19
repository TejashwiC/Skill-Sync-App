window.register = async function () {
  const email    = el("email")?.value;
  const password = el("password")?.value;
  const name     = el("name")?.value;
  try {
    const user = await createUserWithEmailAndPassword(auth, email, password);
    await setDoc(doc(db, "users", user.user.uid), {
      name, email, mobile: "", teach: "", learn: "",
      language: "", credits: 100, skills: [], photo: "",
      followers: [], following: [], blocked: [],
      chatNotifications: true,
      followNotifications: true,
      soundNotifications: true,
      emailNotifications: true,
      sessionAlerts: true
    });
    alert("Registered!");
    window.location.href = "login.html";
  } catch (err) { alert("Register Error: " + err.message); }
};