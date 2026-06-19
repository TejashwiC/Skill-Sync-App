window.addSkill = async function () {
  const input = el("skillInput");
  if (!input || !input.value.trim()) return alert("Enter a skill name");
  const userRef = doc(db, "users", auth.currentUser.uid);
  const snap = await getDoc(userRef);
  let teachSkills = (snap.data().teach || "").split(",").map(s => s.trim()).filter(Boolean);
  teachSkills.push(input.value.trim());
  await updateDoc(userRef, { teach: teachSkills.join(",") });
  input.value = "";
  alert("Skill added!");
  loadProfile();
};

window.editSkill = async function () {
  const oldInput = el("oldSkillInput");
  const newInput = el("newSkillInput");
  if (!oldInput?.value.trim() || !newInput?.value.trim()) return alert("Fill both fields");
  const userRef = doc(db, "users", auth.currentUser.uid);
  const snap = await getDoc(userRef);
  let skills = (snap.data().teach || "").split(",").map(s => s.trim()).filter(Boolean);
  const idx = skills.indexOf(oldInput.value.trim());
  if (idx === -1) return alert("Skill not found");
  skills[idx] = newInput.value.trim();
  await updateDoc(userRef, { teach: skills.join(",") });
  alert("Skill updated!");
  oldInput.value = ""; newInput.value = "";
  loadProfile();
};

window.deleteSkill = async function () {
  const input = el("deleteSkillInput");
  if (!input || !input.value.trim()) return alert("Enter skill name");
  const userRef = doc(db, "users", auth.currentUser.uid);
  const snap = await getDoc(userRef);
  let teachSkills = (snap.data().teach || "").split(",").map(s => s.trim()).filter(s => s !== input.value.trim());
  await updateDoc(userRef, { teach: teachSkills.join(",") });
  alert("Skill deleted!");
  input.value = "";
  loadProfile();
};