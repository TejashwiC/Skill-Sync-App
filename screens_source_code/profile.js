window.loadProfile = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const snap = await getDoc(doc(db, "users", user.uid));
  const d = snap.data();
  if (el("vname"))      el("vname").innerText      = d.name     || "";
  if (el("vemail"))     el("vemail").innerText     = d.email    || "";
  if (el("vmobile"))    el("vmobile").innerText    = d.mobile   || "";
  if (el("vteach"))     el("vteach").innerText     = d.teach    || "";
  if (el("vlearn"))     el("vlearn").innerText     = d.learn    || "";
  if (el("vlang"))      el("vlang").innerText      = d.language || "";
  if (el("vcredits"))   el("vcredits").innerText   = d.credits  || 0;
  if (el("profileImg")) el("profileImg").src = d.photo && d.photo.length > 10 ? d.photo : "https://via.placeholder.com/120";
  if (el("homeCredits")) el("homeCredits").innerText = d.credits || 0;
  const teachSkills = d.teach ? d.teach.split(",") : [];
  if (el("homeSkills"))   el("homeSkills").innerText  = teachSkills.filter(s => s.trim() !== "").length;
  if (el("homeSessions")) el("homeSessions").innerText = d.sessions || 0;
  if (el("welcomeText"))  el("welcomeText").innerText  = "Welcome Back 👋 " + (d.name || "");
  const overviewList = el("skillsOverviewList");
  if (overviewList) {
    overviewList.innerHTML = "";
    teachSkills.filter(s => s.trim()).forEach(skill => {
      overviewList.innerHTML += `<div class="skill-card">${skill.trim()}</div>`;
    });
  }
};

window.saveProfile = async function () {
  const user = auth.currentUser;
  await updateDoc(doc(db, "users", user.uid), {
    name:     el("pname").value,
    email:    el("pemail").value,
    mobile:   el("pmobile").value,
    teach:    el("pteach").value,
    learn:    el("plearn").value,
    language: el("plang").value
  });
  alert("Profile updated!");
  backProfileDashboard();
  loadProfile();
};

window.openProfileScreenAndFill = async function () {
  const snap = await getDoc(doc(db, "users", auth.currentUser.uid));
  const d = snap.data();
  if (el("pname"))   el("pname").value   = d.name     || "";
  if (el("pemail"))  el("pemail").value  = d.email    || "";
  if (el("pmobile")) el("pmobile").value = d.mobile   || "";
  if (el("pteach"))  el("pteach").value  = d.teach    || "";
  if (el("plearn"))  el("plearn").value  = d.learn    || "";
  if (el("plang"))   el("plang").value   = d.language || "";
};

window.followUser = async function (targetId) {
  const user = auth.currentUser;
  if (!user) return;
  await updateDoc(doc(db, "users", user.uid),    { following: arrayUnion(targetId) });
  await updateDoc(doc(db, "users", targetId),    { followers: arrayUnion(user.uid) });
  alert("Followed successfully!");
};