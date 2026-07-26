import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import {
  getAuth,
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  onAuthStateChanged,
  signOut,
  updatePassword,
  updateProfile,
  deleteUser
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";
import {
  getFirestore,
  doc,
  setDoc,
  getDoc,
  updateDoc,
  deleteDoc,
  collection,
  onSnapshot,
  addDoc,
  query,
  orderBy,
  arrayUnion,
  arrayRemove,
  getDocs,
  where,
  serverTimestamp
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";
import { getStorage, ref, uploadBytesResumable, getDownloadURL } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-storage.js";

const firebaseConfig = {
  apiKey: "AIzaSyDcvwPSTgRqQB9gSzpk4u3eBJerNpgUr7U",
  authDomain: "skillsync-dc26c.firebaseapp.com",
  projectId: "skillsync-dc26c",
  storageBucket: "skillsync-dc26c.appspot.com",
  messagingSenderId: "45687336565",
  appId: "1:45687336565:web:43432cf08eebef1088523c"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db   = getFirestore(app);
const storage = getStorage(app);

function el(id) { return document.getElementById(id); }

/* ================ REGISTER ================ */
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

/* ================ LOGIN ================ */
window.login = async function () {
  try {
    const emailVal = el("email")?.value?.trim() || "";
    const passwordVal = el("password")?.value?.trim() || "";
    if (!emailVal || !passwordVal) {
      alert("Please enter both Email and Password.");
      return;
    }
    await signInWithEmailAndPassword(auth, emailVal, passwordVal);
    window.location.href = "dashboard.html";
  } catch (err) {
    if (auth.currentUser) {
      window.location.href = "dashboard.html";
      return;
    }
    alert("Login Error: " + err.message);
  }
};

/* ================ AUTH STATE ================ */
window.loadUser = function () {
  onAuthStateChanged(auth, (user) => {
    if (!user) {
      if (location.pathname.includes("dashboard")) window.location.href = "login.html";
      return;
    }
    updateDoc(doc(db, "users", user.uid), { });
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
    loadCalendarSessions();
    loadGroupChatMembers();
    loadGroupChats();
    // Auto-update home stats live
    listenHomeStats();
    
  });
};

window.addEventListener("beforeunload", () => {
  const user = auth.currentUser;
  if (user) {
    updateDoc(doc(db, "users", user.uid), { });
  }
});

/* ================ LOGOUT ================ */
window.logout = async function () {
  const user = auth.currentUser;
  if (user) {
    await updateDoc(doc(db, "users", user.uid), { });
  }
  await signOut(auth);
  window.location.href = "login.html";
};

/* ================ NAV ================ */
window.showSection = function (id) {
  document.querySelectorAll(".section").forEach(s => s.style.display = "none");
  if (el(id)) el(id).style.display = "block";
  if (id === "calendar") {
    loadCalendarSessions();
  } else if (id === "session") {
    loadHostSessionPanel();
  } else if (id === "testSection") {
    if (typeof backTestDashboard === "function") backTestDashboard();
  } else if (id === "chat") {
    loadChatUsers();
    loadChatUserList();
  } else if (id === "notes") {
    loadPDFs();
  }
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

  // Listen to completed sessions created by this user (real-time)
  onSnapshot(
    query(collection(db, "sessions"), where("hostId", "==", user.uid), where("status", "==", "ended")),
    snap => {
      if (el("homeSessions")) el("homeSessions").innerText = snap.size;
    }
  );
}

/* ================ PROFILE ================ */
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
  // Reload whichever list is visible
  if (el("usersList")) loadUsers();
  if (el("suggestedList")) loadSuggestedUsers();
};

window.unfollowUser = async function (targetId) {
  const user = auth.currentUser;
  if (!user) return;
  await updateDoc(doc(db, "users", user.uid),    { following: arrayRemove(targetId) });
  await updateDoc(doc(db, "users", targetId),    { followers: arrayRemove(user.uid) });
  // Reload whichever list is visible
  if (el("usersList")) loadUsers();
  if (el("suggestedList")) loadSuggestedUsers();
};

/* ================ SKILLS ================ */
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

/* ================ USERS ================ */
window.loadUsers = function () {
  const container = el("usersList");
  if (!container) return;
  const user = auth.currentUser;
  if (!user) return;
  onSnapshot(doc(db, "users", user.uid), (mySnap) => {
    const myData = mySnap.data();
    const following = myData?.following || [];
    
    onSnapshot(collection(db, "users"), (snap) => {
      container.innerHTML = "";
      snap.forEach((docSnap) => {
        const userId = docSnap.id;
        const data   = docSnap.data();
        if (userId === user.uid) return;
        const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(data.name || 'User')}&background=random&color=fff&size=50`;
        const isFollowing = following.includes(userId);
        const actionBtn = isFollowing
          ? `<button class="btn-follow" onclick="unfollowUser('${userId}')" style="background:#e74c3c;"><i class="fas fa-user-minus"></i> Unfollow</button>`
          : `<button class="btn-follow" onclick="followUser('${userId}')"><i class="fas fa-user-plus"></i> Follow</button>`;
          
        container.innerHTML += `
          <div class="user-card">
            <div class="user-card-info">
              <img src="${data.photo && data.photo.length > 10 ? data.photo : avatarUrl}">
              <div class="user-card-details">
                <span class="user-card-name">${data.name || "No Name"}</span>
                <span class="user-card-email"><i class="fas fa-envelope"></i> ${data.email || ""}</span>
              </div>
            </div>
            <div class="user-card-actions">
              ${actionBtn}
              <button class="btn-view" onclick="viewUserProfile('${userId}')"><i class="fas fa-eye"></i> View</button>
            </div>
          </div>`;
      });
    });
  });
};

window.loadFollowers = function () {
  const container = el("followersList");
  if (!container) return;
  onSnapshot(doc(db, "users", auth.currentUser.uid), async (snap) => {
    const data = snap.data();
    container.innerHTML = "";
    for (let uid of data.followers || []) {
      const u = await getDoc(doc(db, "users", uid));
      const udata = u.data() || {};
      const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(udata.name || 'User')}&background=random&color=fff&size=50`;
      container.innerHTML += `
        <div class="user-card">
          <div class="user-card-info">
            <img src="${udata.photo && udata.photo.length > 10 ? udata.photo : avatarUrl}">
            <div class="user-card-details">
              <span class="user-card-name">${udata.name || "User"}</span>
              <span class="user-card-email"><i class="fas fa-envelope"></i> ${udata.email || ""}</span>
            </div>
          </div>
          <div class="user-card-actions">
            <button class="btn-view" onclick="viewUserProfile('${uid}')"><i class="fas fa-eye"></i> View</button>
          </div>
        </div>`;
    }
    if (!data.followers?.length) container.innerHTML = `<p style="color:#888;text-align:center;">No followers yet</p>`;
  });
};

window.loadFollowing = function () {
  const container = el("followingList");
  if (!container) return;
  onSnapshot(doc(db, "users", auth.currentUser.uid), async (snap) => {
    const data = snap.data();
    container.innerHTML = "";
    for (let uid of data.following || []) {
      const u = await getDoc(doc(db, "users", uid));
      const udata = u.data() || {};
      const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(udata.name || 'User')}&background=random&color=fff&size=50`;
      container.innerHTML += `
        <div class="user-card">
          <div class="user-card-info">
            <img src="${udata.photo && udata.photo.length > 10 ? udata.photo : avatarUrl}">
            <div class="user-card-details">
              <span class="user-card-name">${udata.name || "User"}</span>
              <span class="user-card-email"><i class="fas fa-envelope"></i> ${udata.email || ""}</span>
            </div>
          </div>
          <div class="user-card-actions">
            <button class="btn-view" onclick="viewUserProfile('${uid}')"><i class="fas fa-eye"></i> View</button>
          </div>
        </div>`;
    }
    if (!data.following?.length) container.innerHTML = `<p style="color:#888;text-align:center;">Not following anyone yet</p>`;
  });
};

window.loadSuggestedUsers = function () {
  const container = el("suggestedList");
  if (!container) return;
  const user = auth.currentUser;
  if (!user) return;
  onSnapshot(doc(db, "users", user.uid), (mySnap) => {
    const myData = mySnap.data();
    const following = myData?.following || [];
    
    onSnapshot(collection(db, "users"), (snap) => {
      container.innerHTML = "";
      snap.forEach((docSnap) => {
        const userId = docSnap.id;
        const data   = docSnap.data();
        if (userId === user.uid) return;
        const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(data.name || 'User')}&background=random&color=fff&size=50`;
        const isFollowing = following.includes(userId);
        const actionBtn = isFollowing
          ? `<button class="btn-follow" onclick="unfollowUser('${userId}')" style="background:#e74c3c;"><i class="fas fa-user-minus"></i> Unfollow</button>`
          : `<button class="btn-follow" onclick="followUser('${userId}')"><i class="fas fa-user-plus"></i> Follow</button>`;
          
        container.innerHTML += `
          <div class="user-card">
            <div class="user-card-info">
              <img src="${data.photo && data.photo.length > 10 ? data.photo : avatarUrl}">
              <div class="user-card-details">
                <span class="user-card-name">${data.name || "User"}</span>
                <span class="user-card-email"><i class="fas fa-envelope"></i> ${data.email || ""}</span>
              </div>
            </div>
            <div class="user-card-actions">
              ${actionBtn}
            </div>
          </div>`;
      });
    });
  });
};

window.searchUsers = async function () {
  const query_text = el("searchInput")?.value?.toLowerCase() || "";
  const container  = el("searchResults");
  if (!container || !query_text) { container.innerHTML = ""; return; }
  
  const user = auth.currentUser;
  if (!user) return;
  const mySnap = await getDoc(doc(db, "users", user.uid));
  const following = mySnap.data()?.following || [];
  
  getDocs(collection(db, "users")).then(snap => {
    container.innerHTML = "";
    snap.forEach(docSnap => {
      const d = docSnap.data();
      const userId = docSnap.id;
      if (userId === user.uid) return;
      if ((d.name || "").toLowerCase().includes(query_text) || (d.email || "").toLowerCase().includes(query_text)) {
        const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(d.name || 'User')}&background=random&color=fff&size=50`;
        const isFollowing = following.includes(userId);
        const actionBtn = isFollowing
          ? `<button class="btn-follow" onclick="unfollowUser('${userId}')" style="background:#e74c3c;"><i class="fas fa-user-minus"></i> Unfollow</button>`
          : `<button class="btn-follow" onclick="followUser('${userId}')"><i class="fas fa-user-plus"></i> Follow</button>`;
          
        container.innerHTML += `
          <div class="user-card">
            <div class="user-card-info">
              <img src="${d.photo && d.photo.length > 10 ? d.photo : avatarUrl}">
              <div class="user-card-details">
                <span class="user-card-name">${d.name}</span>
                <span class="user-card-email"><i class="fas fa-envelope"></i> ${d.email}</span>
              </div>
            </div>
            <div class="user-card-actions">
              ${actionBtn}
              <button class="btn-view" onclick="viewUserProfile('${userId}')"><i class="fas fa-eye"></i> View</button>
            </div>
          </div>`;
      }
    });
    if (!container.innerHTML) container.innerHTML = `<p style="color:#888;">No users found</p>`;
  });
};

window.viewUserProfile = async function (userId) {
  const snap = await getDoc(doc(db, "users", userId));
  const d = snap.data();
  if (!d) return;
  openUserScreen("userProfileScreen");
  if (el("u_profileImg")) el("u_profileImg").src = d.photo && d.photo.length > 10 ? d.photo : "https://via.placeholder.com/120";
  if (el("u_name"))    el("u_name").innerText    = d.name     || "";
  if (el("u_email"))   el("u_email").innerText   = d.email    || "";
  if (el("u_mobile")) {
    if (userId === auth.currentUser?.uid) {
      el("u_mobile").innerText = d.mobile || "";
      el("u_mobile").parentElement.style.display = "block";
    } else {
      el("u_mobile").innerText = "";
      el("u_mobile").parentElement.style.display = "none";
    }
  }
  if (el("u_teach"))   el("u_teach").innerText   = d.teach    || "";
  if (el("u_learn"))   el("u_learn").innerText   = d.learn    || "";
  if (el("u_lang"))    el("u_lang").innerText    = d.language || "";
  if (el("u_credits")) el("u_credits").innerText = d.credits  || 0;
};

/* ================================================
   CHAT SYSTEM
   ================================================ */
let activeChatUser = null;
let activeChatName = "";

function getChatId(a, b) { return a < b ? a + "_" + b : b + "_" + a; }

function timeAgo(timestamp) {
  const now  = Date.now();
  const diff = now - timestamp;
  const mins = Math.floor(diff / 60000);
  const hrs  = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  if (mins < 1)  return "Just now";
  if (mins < 60) return mins + " min ago";
  if (hrs < 24)  return hrs  + " hr ago";
  return days + " day ago";
}

function formatTime(timestamp) {
  const d    = new Date(timestamp);
  let h      = d.getHours();
  let m      = d.getMinutes();
  const ampm = h >= 12 ? "PM" : "AM";
  h = h % 12 || 12;
  m = m < 10 ? "0" + m : m;
  return h + ":" + m + " " + ampm;
}

window.loadChatUsers = function () {
  const container = el("requestContainer");
  if (!container) return;
  const user = auth.currentUser;
  if (!user) return;
  onSnapshot(doc(db, "users", user.uid), (mySnap) => {
    const myData = mySnap.data();
    const followers = myData?.followers || [];
    const following = myData?.following || [];
    // Show only mutual connections (both follow each other)
    const mutual = followers.filter(uid => following.includes(uid));
    
    onSnapshot(collection(db, "users"), (snapshot) => {
      container.innerHTML = "";
      snapshot.forEach((docSnap) => {
        const data   = docSnap.data();
        const userId = docSnap.id;
        if (userId === user.uid) return;
        if (!mutual.includes(userId)) return;
        container.innerHTML += `
          <div class="chat-user" onclick="openChat('${userId}', '${data.name}')"
               style="display:flex;align-items:center;gap:12px;padding:12px;border-bottom:1px solid #eee;cursor:pointer;">
            <img src="${data.photo && data.photo.length > 10 ? data.photo : 'https://ui-avatars.com/api/?name=' + encodeURIComponent(data.name || 'User')}"
                 style="width:45px;height:45px;border-radius:50%;object-fit:cover;">
            <div><b>${data.name || "No Name"}</b><br><small style="color:#888;">${data.email || ""}</small></div>
          </div>`;
      });
      if (!container.innerHTML) {
        container.innerHTML = "<p style='padding:10px;color:#999;font-size:13px;text-align:center;'>No mutual connections yet — follow each other to chat</p>";
      }
    });
  });
};

window.loadChatUserList = function () {
  const container = el("chatUserList");
  if (!container) return;
  const user = auth.currentUser;
  if (!user) return;
  onSnapshot(doc(db, "users", user.uid), (mySnap) => {
    const myData = mySnap.data();
    const followers = myData?.followers || [];
    const following = myData?.following || [];
    // Show only mutual connections (both follow each other)
    const mutual = followers.filter(uid => following.includes(uid));
    
    onSnapshot(collection(db, "users"), (snapshot) => {
      container.innerHTML = "<p style='padding:10px;color:#888;font-size:13px;'>👇 Select a contact to message:</p>";
      snapshot.forEach((docSnap) => {
        const data   = docSnap.data();
        const userId = docSnap.id;
        if (userId === user.uid) return;
        if (!mutual.includes(userId)) return;
        container.innerHTML += `
          <div onclick="selectChatUser('${userId}', '${data.name}')"
               style="display:flex;align-items:center;gap:12px;padding:10px 12px;border-bottom:1px solid #eee;cursor:pointer;">
            <img src="${data.photo && data.photo.length > 10 ? data.photo : 'https://ui-avatars.com/api/?name=' + encodeURIComponent(data.name || 'User')}"
                 style="width:40px;height:40px;border-radius:50%;object-fit:cover;">
            <div>
              <b style="font-size:14px;">${data.name || "No Name"}</b><br>
              <small style="color:#888;font-size:12px;">${data.email || ""}</small>
            </div>
          </div>`;
      });
      if (container.innerHTML === "<p style='padding:10px;color:#888;font-size:13px;'>👇 Select a contact to message:</p>") {
        container.innerHTML += "<p style='padding:10px;color:#999;font-size:12px;text-align:center;'>No mutual connections — follow each other to chat</p>";
      }
    });
  });
};

window.loadInbox = async function () {
  const container = el("inboxContainer");
  if (!container) return;
  const user = auth.currentUser;
  if (!user) return;
  const usersSnap = await getDocs(collection(db, "users"));
  container.innerHTML = "";
  for (const userDoc of usersSnap.docs) {
    const otherUserId   = userDoc.id;
    const otherUserData = userDoc.data();
    if (otherUserId === user.uid) continue;
    const chatId  = getChatId(user.uid, otherUserId);
    const q       = query(collection(db, "chats", chatId, "messages"), orderBy("time"));
    const msgSnap = await getDocs(q);
    if (msgSnap.empty) continue;
    const lastMsg  = msgSnap.docs[msgSnap.docs.length - 1].data();
    const lastText = lastMsg.text || "";
    const lastTime = lastMsg.time ? timeAgo(lastMsg.time) : "";
    const isMine   = lastMsg.sender === user.uid;
    container.innerHTML += `
      <div onclick="openChat('${otherUserId}', '${otherUserData.name}')"
           style="display:flex;align-items:center;gap:12px;padding:14px 12px;border-bottom:1px solid #eee;cursor:pointer;background:#fff;">
        <img src="${otherUserData.photo && otherUserData.photo.length > 10
          ? otherUserData.photo
          : 'https://ui-avatars.com/api/?name=' + encodeURIComponent(otherUserData.name || 'User')}"
             style="width:50px;height:50px;border-radius:50%;object-fit:cover;">
        <div style="flex:1;">
          <div style="display:flex;justify-content:space-between;">
            <b style="font-size:15px;">${otherUserData.name || "User"}</b>
            <small style="color:#999;font-size:11px;">${lastTime}</small>
          </div>
          <div style="color:#888;font-size:13px;margin-top:2px;">
            ${isMine ? "You: " : ""}${lastText.length > 35 ? lastText.substring(0, 35) + "..." : lastText}
          </div>
        </div>
      </div>`;
  }
  if (!container.innerHTML) {
    container.innerHTML = `<div style="text-align:center;padding:40px;color:#888;">No conversations yet</div>`;
  }
};

let targetStatusUnsubscribe = null;
function listenTargetUserStatus(targetUserId) {
  // Online/offline status removed
  if (targetStatusUnsubscribe) { targetStatusUnsubscribe(); targetStatusUnsubscribe = null; }
  const statusEl = el("chatUserStatus");
  if (statusEl) statusEl.style.display = "none";
}

window.selectChatUser = function (userId, name) {
  activeChatUser = userId;
  activeChatName = name;
  activeGroupChat = null; // Unset group chat
  if (el("chatTitle")) el("chatTitle").innerText = name;
  listenTargetUserStatus(userId);
  const savedTheme = localStorage.getItem(`chat_theme_${userId}`) || "Default";
  if (el("chatThemeSelect")) {
    el("chatThemeSelect").value = savedTheme;
    changeChatTheme();
  }
  loadMessages();
};

window.openChat = function (userId, name) {
  activeChatUser = userId;
  activeChatName = name;
  activeGroupChat = null; // Unset group chat
  openChatScreen("chatMainScreen");
  if (el("chatTitle")) el("chatTitle").innerText = name;
  listenTargetUserStatus(userId);
  const savedTheme = localStorage.getItem(`chat_theme_${userId}`) || "Default";
  if (el("chatThemeSelect")) {
    el("chatThemeSelect").value = savedTheme;
    changeChatTheme();
  }
  loadMessages();
};

window.deleteActiveChat = async function () {
  if (!activeChatUser) return;
  if (!confirm(`Delete entire chat with ${activeChatName}? This cannot be undone.`)) return;
  const user = auth.currentUser;
  if (!user) return;
  const chatId = getChatId(user.uid, activeChatUser);
  try {
    const msgSnap = await getDocs(collection(db, "chats", chatId, "messages"));
    await Promise.all(msgSnap.docs.map(d => deleteDoc(d.ref)));
    alert("Chat deleted!");
    loadInbox();
    backChatDashboard();
  } catch (err) { alert("Error deleting chat: " + err.message); }
};

window.changeChatTheme = function () {
  const theme = el("chatThemeSelect")?.value || "Default";
  const box = el("chatBox");
  if (!box) return;
  
  const themes = {
    Pink: "#FFE4E1",
    Blue: "#E0F7FA",
    Dark: "#303030",
    Green: "#E8F5E9",
    Default: "#f4f6f8"
  };
  box.style.backgroundColor = themes[theme] || themes.Default;
  if (activeChatUser) {
    localStorage.setItem(`chat_theme_${activeChatUser}`, theme);
  }
};

window.sendMessage = async function () {
  const msgInput = el("messageInput");
  if (!msgInput) return;
  const msg = msgInput.value.trim();
  if (!msg) return;
  const user = auth.currentUser;
  if (!user) { alert("Not logged in!"); return; }

  if (activeGroupChat) {
    const mySnap = await getDoc(doc(db, "users", user.uid));
    const senderName = mySnap.data()?.name || "Member";
    try {
      await addDoc(collection(db, "groups", activeGroupChat, "messages"), {
        text: msg, sender: user.uid, senderName: senderName, time: Date.now()
      });
      await updateDoc(doc(db, "groups", activeGroupChat), {
        lastMessage: `${senderName}: ${msg}`,
        lastMessageTime: Date.now()
      });
      msgInput.value = "";
    } catch (err) { alert("Failed to send group message: " + err.message); }
    return;
  }

  if (!activeChatUser) { alert("Select a user first!"); return; }
  const mySnap    = await getDoc(doc(db, "users", user.uid));
  const otherSnap = await getDoc(doc(db, "users", activeChatUser));
  const myBlocked    = mySnap.data()?.blocked    || [];
  const theirBlocked = otherSnap.data()?.blocked || [];
  if (myBlocked.includes(activeChatUser))  { alert("You have blocked this user."); return; }
  if (theirBlocked.includes(user.uid))     { alert("You cannot send messages to this user."); return; }
  try {
    const chatId = getChatId(user.uid, activeChatUser);
    await addDoc(collection(db, "chats", chatId, "messages"), {
      text: msg, sender: user.uid, time: Date.now()
    });
    msgInput.value = "";
    loadInbox();
  } catch (err) { alert("Failed to send: " + err.message); }
};

window.handleEnterKey = function (event) {
  if (event.key === "Enter") sendMessage();
};

window.loadMessages = function () {
  if (!activeChatUser) return;
  const user = auth.currentUser;
  if (!user) return;
  const chatId = getChatId(user.uid, activeChatUser);
  const q = query(collection(db, "chats", chatId, "messages"), orderBy("time"));
  onSnapshot(q, (snapshot) => {
    const box = el("chatBox");
    if (!box) return;
    box.innerHTML = "";
    snapshot.forEach((docSnap) => {
      const d    = docSnap.data();
      const msgId = docSnap.id;
      const time = d.time ? formatTime(d.time) : "";
      const isMine = d.sender === user.uid;
      
      let attachments = "";
      if (d.imageUrl) {
        attachments += `<img src="${d.imageUrl}" onclick="event.stopPropagation(); openImageModal('${d.imageUrl.replace(/'/g, "\\'")}')" style="max-width:100%; border-radius:8px; display:block; margin-top:5px; max-height:220px; object-fit:cover; cursor:pointer; border:1px solid rgba(0,0,0,0.1);" title="Click to view full photo">`;
      }
      if (d.pdfUrl) {
        attachments += `<a href="${d.pdfUrl}" target="_blank" style="display:flex; align-items:center; gap:8px; background:#f0f0f0; padding:8px 12px; border-radius:6px; text-decoration:none; color:#333; margin-top:5px; font-size:12px; font-weight:700;"><i class="fas fa-file-pdf" style="color:#ef4444; font-size:18px;"></i> View Document</a>`;
      }
      
      const pinIndicator = d.isPinned ? "📌 " : "";

      if (selectModeActive && isMine) {
        // SELECT MODE: show checkbox
        box.innerHTML += `
          <div class="chat-msg-wrap" style="display:flex;justify-content:flex-end;margin:4px 10px;position:relative;align-items:center;gap:8px;" onclick="this.querySelector('.msg-select-cb').click()">
            <div style="background:#25D366;color:#fff;padding:8px 14px;border-radius:18px 18px 4px 18px;max-width:65%;word-wrap:break-word;">
              <div style="font-size:14px;">${pinIndicator}${d.text}</div>
              ${attachments}
              <div style="font-size:10px;color:rgba(255,255,255,0.8);text-align:right;margin-top:3px;">${time}</div>
            </div>
            <input type="checkbox" class="msg-select-cb" data-msg-id="${msgId}" onclick="event.stopPropagation()" style="width:18px;height:18px;cursor:pointer;accent-color:#7c3aed;">
          </div>`;
      } else if (selectModeActive && !isMine) {
        // SELECT MODE: show checkbox for others' messages too
        box.innerHTML += `
          <div class="chat-msg-wrap" style="display:flex;justify-content:flex-start;margin:4px 10px;position:relative;align-items:center;gap:8px;" onclick="this.querySelector('.msg-select-cb').click()">
            <input type="checkbox" class="msg-select-cb" data-msg-id="${msgId}" onclick="event.stopPropagation()" style="width:18px;height:18px;cursor:pointer;accent-color:#7c3aed;">
            <div style="background:#fff;color:#000;padding:8px 14px;border-radius:18px 18px 18px 4px;max-width:65%;word-wrap:break-word;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
              <div style="font-size:14px;">${pinIndicator}${d.text}</div>
              ${attachments}
              <div style="font-size:10px;color:#999;text-align:right;margin-top:3px;">${time}</div>
            </div>
          </div>`;
      } else if (isMine) {
        const deleteBtn = `<div class="msg-actions" style="display:none; position:absolute; top:-32px; right:0; background:#fff; border-radius:8px; box-shadow:0 2px 10px rgba(0,0,0,0.2); overflow:hidden; z-index:99; white-space:nowrap;">
          <button onclick="event.stopPropagation(); deleteChatMessage('${msgId}')" style="background:none; border:none; padding:6px 12px; color:#e74c3c; font-size:12px; font-weight:700; cursor:pointer;">🗑 Delete</button>
          <button onclick="event.stopPropagation(); togglePinMessage('${msgId}', ${d.isPinned})" style="background:none; border:none; padding:6px 12px; color:#555; font-size:12px; font-weight:700; cursor:pointer;">${d.isPinned ? '📌 Unpin' : '📌 Pin'}</button>
        </div>`;
        box.innerHTML += `
          <div class="chat-msg-wrap" style="display:flex;justify-content:flex-end;margin:4px 10px;position:relative;" onclick="toggleMsgActions(this)">
            ${deleteBtn}
            <div style="background:#25D366;color:#fff;padding:8px 14px;border-radius:18px 18px 4px 18px;max-width:70%;word-wrap:break-word;">
              <div style="font-size:14px;">${pinIndicator}${d.text}</div>
              ${attachments}
              <div style="font-size:10px;color:rgba(255,255,255,0.8);text-align:right;margin-top:3px;">${time}</div>
            </div>
          </div>`;
      } else {
        box.innerHTML += `
          <div class="chat-msg-wrap" style="display:flex;justify-content:flex-start;margin:4px 10px;position:relative;">
            <div style="background:#fff;color:#000;padding:8px 14px;border-radius:18px 18px 18px 4px;max-width:70%;word-wrap:break-word;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
              <div style="font-size:14px;">${pinIndicator}${d.text}</div>
              ${attachments}
              <div style="font-size:10px;color:#999;text-align:right;margin-top:3px;">${time}</div>
            </div>
          </div>`;
      }
    });
    box.scrollTop = box.scrollHeight;
  });
};

// Toggle message action menu (WhatsApp style tap)
window.toggleMsgActions = function (wrap) {
  // Hide all other open menus first
  document.querySelectorAll('.msg-actions').forEach(m => m.style.display = 'none');
  const menu = wrap.querySelector('.msg-actions');
  if (menu) menu.style.display = menu.style.display === 'flex' ? 'none' : 'flex';
};

// Hide menus when clicking outside
document.addEventListener('click', function (e) {
  if (!e.target.closest('.chat-msg-wrap')) {
    document.querySelectorAll('.msg-actions').forEach(m => m.style.display = 'none');
  }
});

window.deleteChatMessage = async function (msgId) {
  if (!activeChatUser) return;
  const user = auth.currentUser;
  if (!user) return;
  if (!confirm("Delete this message?")) return;
  try {
    const chatId = getChatId(user.uid, activeChatUser);
    await deleteDoc(doc(db, "chats", chatId, "messages", msgId));
  } catch (err) { alert("Delete failed: " + err.message); }
};

window.deleteGroupMessage = async function (msgId) {
  if (!activeGroupChat) return;
  if (!confirm("Delete this message?")) return;
  try {
    await deleteDoc(doc(db, "groups", activeGroupChat, "messages", msgId));
  } catch (err) { alert("Delete failed: " + err.message); }
};

// ---- SELECT MODE (WhatsApp-style multi-select delete) ----
let selectModeActive = false;

window.toggleSelectMode = function () {
  selectModeActive = !selectModeActive;
  const selectBtn = el("selectMsgBtn");
  const deleteBtn = el("deleteSelectedBtn");
  if (selectModeActive) {
    selectBtn.style.background = "#7c3aed";
    selectBtn.style.color = "#fff";
    deleteBtn.style.display = "flex";
    // Re-render messages with checkboxes
    if (activeChatUser) loadMessages();
    else if (activeGroupChat) loadGroupMessages(activeGroupChat);
  } else {
    selectBtn.style.background = "none";
    selectBtn.style.color = "#7c3aed";
    deleteBtn.style.display = "none";
    // Re-render without checkboxes
    if (activeChatUser) loadMessages();
    else if (activeGroupChat) loadGroupMessages(activeGroupChat);
  }
};

window.deleteSelectedMessages = async function () {
  const checked = document.querySelectorAll(".msg-select-cb:checked");
  if (!checked.length) { alert("Select at least one message first."); return; }
  if (!confirm(`Delete ${checked.length} selected message(s)?`)) return;
  const user = auth.currentUser;
  if (!user) return;
  const ids = Array.from(checked).map(cb => cb.dataset.msgId);
  try {
    if (activeChatUser) {
      const chatId = getChatId(user.uid, activeChatUser);
      await Promise.all(ids.map(id => deleteDoc(doc(db, "chats", chatId, "messages", id))));
    } else if (activeGroupChat) {
      await Promise.all(ids.map(id => deleteDoc(doc(db, "groups", activeGroupChat, "messages", id))));
    }
    // Exit select mode after deletion
    selectModeActive = false;
    if (el("selectMsgBtn")) { el("selectMsgBtn").style.background = "none"; el("selectMsgBtn").style.color = "#7c3aed"; }
    if (el("deleteSelectedBtn")) el("deleteSelectedBtn").style.display = "none";
  } catch (err) { alert("Delete failed: " + err.message); }
};

window.togglePinMessage = async function (messageId, currentlyPinned) {
  if (!activeChatUser) return;
  const user = auth.currentUser;
  if (!user) return;
  const chatId = getChatId(user.uid, activeChatUser);
  await updateDoc(doc(db, "chats", chatId, "messages", messageId), {
    isPinned: !currentlyPinned
  });
  filterChatMessages();
};

window.filterChatMessages = function () {
  const queryText = el("chatMessageSearch")?.value?.toLowerCase() || "";
  const user = auth.currentUser;
  if (!user || !activeChatUser) return;
  const chatId = getChatId(user.uid, activeChatUser);
  const q = query(collection(db, "chats", chatId, "messages"), orderBy("time"));
  getDocs(q).then(snapshot => {
    const box = el("chatBox");
    if (!box) return;
    box.innerHTML = "";
    snapshot.forEach((docSnap) => {
      const d = docSnap.data();
      const msgId = docSnap.id;
      if (queryText && !d.text.toLowerCase().includes(queryText)) return;
      
      const time = d.time ? formatTime(d.time) : "";
      const isMine = d.sender === user.uid;
      let attachments = "";
      if (d.imageUrl) {
        attachments += `<img src="${d.imageUrl}" onclick="event.stopPropagation(); openImageModal('${d.imageUrl.replace(/'/g, "\\'")}')" style="max-width:100%; border-radius:8px; display:block; margin-top:5px; max-height:220px; object-fit:cover; cursor:pointer; border:1px solid rgba(0,0,0,0.1);" title="Click to view full photo">`;
      }
      if (d.pdfUrl) {
        attachments += `<a href="${d.pdfUrl}" target="_blank" style="display:flex; align-items:center; gap:8px; background:#f0f0f0; padding:8px 12px; border-radius:6px; text-decoration:none; color:#333; margin-top:5px; font-size:12px; font-weight:700;"><i class="fas fa-file-pdf" style="color:#ef4444; font-size:18px;"></i> View Document</a>`;
      }
      const pinIndicator = d.isPinned ? "📌 " : "";
      const deleteBtn = isMine ? `<div class="msg-actions" style="display:none; position:absolute; top:-32px; right:0; background:#fff; border-radius:8px; box-shadow:0 2px 10px rgba(0,0,0,0.2); overflow:hidden; z-index:99; white-space:nowrap;">
        <button onclick="event.stopPropagation(); deleteChatMessage('${msgId}')" style="background:none; border:none; padding:6px 12px; color:#e74c3c; font-size:12px; font-weight:700; cursor:pointer;">🗑 Delete</button>
        <button onclick="event.stopPropagation(); togglePinMessage('${msgId}', ${d.isPinned})" style="background:none; border:none; padding:6px 12px; color:#555; font-size:12px; font-weight:700; cursor:pointer;">${d.isPinned ? '📌 Unpin' : '📌 Pin'}</button>
      </div>` : "";
      
      if (isMine) {
        box.innerHTML += `
          <div class="chat-msg-wrap" style="display:flex;justify-content:flex-end;margin:4px 10px;position:relative;" onclick="toggleMsgActions(this)">
            ${deleteBtn}
            <div style="background:#25D366;color:#fff;padding:8px 14px;border-radius:18px 18px 4px 18px;max-width:70%;word-wrap:break-word;">
              <div style="font-size:14px;">${pinIndicator}${d.text}</div>
              ${attachments}
              <div style="font-size:10px;color:rgba(255,255,255,0.8);text-align:right;margin-top:3px;">${time}</div>
            </div>
          </div>`;
      } else {
        box.innerHTML += `
          <div class="chat-msg-wrap" style="display:flex;justify-content:flex-start;margin:4px 10px;position:relative;">
            <div style="background:#fff;color:#000;padding:8px 14px;border-radius:18px 18px 18px 4px;max-width:70%;word-wrap:break-word;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
              <div style="font-size:14px;">${pinIndicator}${d.text}</div>
              ${attachments}
              <div style="font-size:10px;color:#999;text-align:right;margin-top:3px;">${time}</div>
            </div>
          </div>`;
      }
    });
    box.scrollTop = box.scrollHeight;
  });
};

window.openImageModal = function (imgSrc) {
  let modal = el("imagePreviewModal");
  if (!modal) {
    modal = document.createElement("div");
    modal.id = "imagePreviewModal";
    modal.style.cssText = "position:fixed; top:0; left:0; width:100vw; height:100vh; background:rgba(0,0,0,0.88); backdrop-filter:blur(6px); z-index:999999; display:flex; flex-direction:column; align-items:center; justify-content:center; padding:20px; box-sizing:border-box;";
    modal.innerHTML = `
      <div style="position:absolute; top:20px; right:20px; display:flex; gap:12px; z-index:1000000;">
        <button id="downloadImageBtn" style="background:#27ae60; color:#fff; border:none; padding:10px 18px; border-radius:8px; font-weight:700; cursor:pointer; font-size:14px; display:flex; align-items:center; gap:6px;"><i class="fas fa-download"></i> Download</button>
        <button onclick="closeImageModal()" style="background:#e74c3c; color:#fff; border:none; padding:10px 16px; border-radius:8px; font-weight:700; cursor:pointer; font-size:16px;">✕ Close</button>
      </div>
      <img id="imagePreviewTarget" src="" style="max-width:90vw; max-height:85vh; border-radius:12px; box-shadow:0 8px 32px rgba(0,0,0,0.6); object-fit:contain;">
    `;
    document.body.appendChild(modal);
  }
  
  const imgTarget = el("imagePreviewTarget");
  const dlBtn = el("downloadImageBtn");
  if (imgTarget) imgTarget.src = imgSrc;
  if (dlBtn) {
    dlBtn.onclick = function() {
      const a = document.createElement("a");
      a.href = imgSrc;
      a.download = "chat_photo_" + Date.now() + ".png";
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    };
  }
  modal.style.display = "flex";
};

window.closeImageModal = function () {
  const modal = el("imagePreviewModal");
  if (modal) modal.style.display = "none";
};

window.uploadChatAttachment = async function () {
  const fileInput = el("chatFileInput");
  if (!fileInput || !fileInput.files.length) return;
  const file = fileInput.files[0];
  const user = auth.currentUser;
  if (!user) return;

  if (!activeGroupChat && !activeChatUser) {
    alert("Open a chat first before attaching a file.");
    return;
  }

  // Limit file size to 900KB to stay within Firestore 1MB doc limit
  if (file.size > 900 * 1024) {
    alert("File too large. Please select a file smaller than 900KB.");
    fileInput.value = "";
    return;
  }

  el("chatUploadProgressWrap").style.display = "block";
  el("chatUploadPct").innerText = "Reading...";
  el("chatUploadProgress").value = 50;

  try {
    // Convert file to base64 data URL
    const base64 = await new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });

    el("chatUploadPct").innerText = "Saving...";
    el("chatUploadProgress").value = 80;

    const ext = file.name ? file.name.split('.').pop().toLowerCase() : "";
    const isImage = file.type.startsWith("image/") || ["jpg", "jpeg", "png", "gif", "webp", "heic", "bmp"].includes(ext);
    const msgText = isImage ? "[Photo Attachment]" : `[Document Attachment] ${file.name}`;

    if (activeGroupChat) {
      const mySnap = await getDoc(doc(db, "users", user.uid));
      const senderName = mySnap.data()?.name || "Member";
      await addDoc(collection(db, "groups", activeGroupChat, "messages"), {
        text: msgText,
        sender: user.uid,
        senderName: senderName,
        time: Date.now(),
        imageUrl: isImage ? base64 : null,
        pdfUrl: !isImage ? base64 : null,
        fileName: file.name
      });
      await updateDoc(doc(db, "groups", activeGroupChat), {
        lastMessage: `${senderName} shared an attachment`,
        lastMessageTime: Date.now()
      });
    } else {
      const chatId = getChatId(user.uid, activeChatUser);
      await addDoc(collection(db, "chats", chatId, "messages"), {
        text: msgText,
        sender: user.uid,
        time: Date.now(),
        imageUrl: isImage ? base64 : null,
        pdfUrl: !isImage ? base64 : null,
        fileName: file.name,
        isPinned: false
      });
      loadInbox();
    }

    el("chatUploadProgressWrap").style.display = "none";
    el("chatUploadProgress").value = 100;
    fileInput.value = "";

  } catch (err) {
    console.error("Upload failed:", err);
    alert("Upload failed: " + err.message);
    el("chatUploadProgressWrap").style.display = "none";
    fileInput.value = "";
  }
};

/* ================================================
   CHAT SETTINGS
   ================================================ */
window.loadChatSettingsPanel = function () {
  const user = auth.currentUser;
  if (!user) return;
  getDoc(doc(db, "users", user.uid)).then(snap => {
    const notif = snap.data()?.chatNotifications !== false;
    if (el("chatNotifStatus")) el("chatNotifStatus").innerText = notif ? "ON" : "OFF";
  });
  const deleteContainer = el("deleteChatUserList");
  const blockContainer  = el("blockUserList");
  if (!deleteContainer && !blockContainer) return;

  // Load only mutual connections (followers AND following)
  getDoc(doc(db, "users", user.uid)).then(async (mySnap) => {
    const myData  = mySnap.data() || {};
    const followers = myData.followers || [];
    const following = myData.following || [];
    const blocked   = myData.blocked   || [];
    // Mutual = in both followers AND following
    const mutual = followers.filter(uid => following.includes(uid));

    if (deleteContainer) deleteContainer.innerHTML = "";
    if (blockContainer)  blockContainer.innerHTML  = "";

    if (!mutual.length) {
      const empty = "<p style='color:#999;font-size:13px;text-align:center;padding:10px;'>No mutual connections yet</p>";
      if (deleteContainer) deleteContainer.innerHTML = empty;
      if (blockContainer)  blockContainer.innerHTML  = empty;
      return;
    }

    for (const uid of mutual) {
      const uSnap = await getDoc(doc(db, "users", uid));
      if (!uSnap.exists()) continue;
      const data = uSnap.data();

      if (deleteContainer) {
        deleteContainer.innerHTML += `
          <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 4px;border-bottom:1px solid #f0f0f0;">
            <span style="font-size:14px;font-weight:600;">${data.name || "User"}</span>
            <button onclick="deleteChatWith('${uid}')" title="Delete chat"
                    style="background:none;border:1px solid #e74c3c;color:#e74c3c;width:30px;height:30px;border-radius:50%;cursor:pointer;font-size:13px;display:flex;align-items:center;justify-content:center;">
              <i class="fas fa-trash-can"></i>
            </button>
          </div>`;
      }
      if (blockContainer) {
        const isBlocked = blocked.includes(uid);
        blockContainer.innerHTML += `
          <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 4px;border-bottom:1px solid #f0f0f0;">
            <span style="font-size:14px;font-weight:600;">${data.name || "User"}</span>
            ${isBlocked
              ? `<button onclick="unblockUser('${uid}')" title="Unblock" style="background:none;border:1px solid #27ae60;color:#27ae60;width:30px;height:30px;border-radius:50%;cursor:pointer;font-size:13px;display:flex;align-items:center;justify-content:center;"><i class="fas fa-user-check"></i></button>`
              : `<button onclick="blockUser('${uid}')"   title="Block"   style="background:none;border:1px solid #e74c3c;color:#e74c3c;width:30px;height:30px;border-radius:50%;cursor:pointer;font-size:13px;display:flex;align-items:center;justify-content:center;"><i class="fas fa-user-slash"></i></button>`
            }
          </div>`;
      }
    }
  });
};

window.setChatNotification = async function (status) {
  const user = auth.currentUser;
  if (!user) return;
  await updateDoc(doc(db, "users", user.uid), { chatNotifications: status });
  if (el("chatNotifStatus")) el("chatNotifStatus").innerText = status ? "ON" : "OFF";
  alert("Chat notifications turned " + (status ? "ON" : "OFF"));
};

window.deleteChatWith = async function (otherUserId) {
  const user = auth.currentUser;
  if (!user) return;
  if (!confirm("Delete entire chat with this user? This cannot be undone.")) return;
  const chatId = getChatId(user.uid, otherUserId);
  try {
    const msgSnap = await getDocs(collection(db, "chats", chatId, "messages"));
    await Promise.all(msgSnap.docs.map(d => deleteDoc(d.ref)));
    alert("Chat deleted!");
  } catch (err) { alert("Error deleting chat: " + err.message); }
};

window.blockUser = async function (targetId) {
  const user = auth.currentUser;
  if (!user) return;
  if (!confirm("Block this user? They will not be able to message you.")) return;
  await updateDoc(doc(db, "users", user.uid), { blocked: arrayUnion(targetId) });
  alert("User blocked!");
  loadChatSettingsPanel();
  loadBlockedUsers();
};

window.unblockUser = async function (targetId) {
  const user = auth.currentUser;
  if (!user) return;
  await updateDoc(doc(db, "users", user.uid), { blocked: arrayRemove(targetId) });
  alert("User unblocked!");
  loadChatSettingsPanel();
  loadBlockedUsers();
};

window.loadBlockedUsers = function () {
  const container = el("blockedUsersList");
  if (!container) return;
  const user = auth.currentUser;
  if (!user) return;
  onSnapshot(doc(db, "users", user.uid), async (snap) => {
    const blocked = snap.data()?.blocked || [];
    container.innerHTML = "";
    if (!blocked.length) {
      container.innerHTML = `<p style="color:#888;text-align:center;padding:20px;">No blocked users</p>`;
      return;
    }
    for (const uid of blocked) {
      const uSnap = await getDoc(doc(db, "users", uid));
      const d     = uSnap.data();
      container.innerHTML += `
        <div class="user-box" style="display:flex;justify-content:space-between;align-items:center;">
          <div><b>${d?.name || "User"}</b><br><small>${d?.email || ""}</small></div>
          <button onclick="unblockUser('${uid}')"
                  style="background:#27ae60;color:#fff;border:none;padding:6px 14px;border-radius:8px;cursor:pointer;">✅ Unblock</button>
        </div>`;
    }
  });
};

/* ================================================
   SESSION SYSTEM
   ================================================ */
let selectedRating = 0;

const PLATFORM_LABELS = {
  zoom:    "Zoom",
  meet:    "Google Meet"
};

function generateCode() {
  const chars = "0123456789";
  let code = "";
  for (let i = 0; i < 6; i++) code += chars[Math.floor(Math.random() * chars.length)];
  return code;
}

function parseSessionDate(startTime) {
  if (!startTime) return new Date();
  if (typeof startTime.toDate === 'function') {
    return startTime.toDate();
  }
  if (startTime.seconds) {
    return new Date(startTime.seconds * 1000);
  }
  return new Date(startTime);
}

function getSessionTimeMs(timeVal, fallback = 0) {
  if (!timeVal) return fallback;
  if (typeof timeVal === "number" && !isNaN(timeVal)) return timeVal;
  if (typeof timeVal === "object" && timeVal !== null) {
    if (timeVal.seconds) return timeVal.seconds * 1000;
    if (typeof timeVal.toDate === "function") return timeVal.toDate().getTime();
  }
  if (typeof timeVal === "string") {
    const parsed = Date.parse(timeVal);
    if (!isNaN(parsed)) return parsed;
  }
  return fallback;
}

function isSessionExpired(d) {
  if (!d || d.status === "ended") return true;
  
  const today = new Date();
  const todayDateStr = today.toLocaleDateString();
  const todayDateOnly = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime();

  // 1. Check startDateStr if present
  if (d.startDateStr) {
    const sessionDate = new Date(d.startDateStr);
    if (!isNaN(sessionDate.getTime())) {
      const sessionDateOnly = new Date(sessionDate.getFullYear(), sessionDate.getMonth(), sessionDate.getDate()).getTime();
      if (sessionDateOnly < todayDateOnly) return true; // Created on a previous day!
    } else if (d.startDateStr !== todayDateStr && !d.isScheduled) {
      const parsePrev = Date.parse(d.startDateStr);
      if (!isNaN(parsePrev) && parsePrev < todayDateOnly) return true;
    }
  }

  // 2. Check startTime timestamp
  const startTimeMs = getSessionTimeMs(d.startTime, 0);
  if (startTimeMs === 0 && !d.isScheduled) {
    return true; // Invalid or missing start time on non-scheduled session -> expire
  }

  const durationMins = Number(d.durationMins || 60);
  const durationMs = durationMins * 60 * 1000;
  const endTimeMs = d.endTime ? getSessionTimeMs(d.endTime, startTimeMs + durationMs) : (startTimeMs + durationMs);

  const now = Date.now();
  if (now >= endTimeMs) return true;
  if (startTimeMs > 0 && (now - startTimeMs > 24 * 60 * 60 * 1000)) return true;

  return false;
}

function formatDuration(startTime) {
  const startMs = getSessionTimeMs(startTime);
  const mins = Math.floor((Date.now() - startMs) / 60000);
  if (mins < 0) return "0 min";
  if (mins < 60) return mins + " min";
  return Math.floor(mins / 60) + "h " + (mins % 60) + "m";
}

function openMeetingLink(link, platform, endTime, sessionId, sessionData) {
  if (!link) { alert("No meeting link available."); return; }
  
  if ((endTime && Date.now() >= endTime) || (sessionData && isSessionExpired(sessionData))) {
    alert("Session Expired: The scheduled time limit for this session is over.");
    if (sessionId) {
      updateDoc(doc(db, "sessions", sessionId), { status: "ended", endTime: Date.now() }).catch(() => {});
    }
    return;
  }

  if (platform === "zoom" || link.includes("zoom.us")) {
    try {
      const url = new URL(link);
      const pathParts = url.pathname.split("/");
      const meetingId = pathParts[pathParts.length - 1];
      const pwd       = url.searchParams.get("pwd") || "";
      const deepLink  = `zoommtg://zoom.us/join?confno=${meetingId}&pwd=${pwd}`;
      const iframe = document.createElement("iframe");
      iframe.style.display = "none";
      iframe.src = deepLink;
      document.body.appendChild(iframe);
      setTimeout(() => { document.body.removeChild(iframe); window.open(link, "_blank"); }, 1500);
    } catch(e) { window.open(link, "_blank"); }
  } else if (platform === "teams" || link.includes("teams.microsoft.com")) {
    const teamsDeep = link.replace("https://teams.microsoft.com", "msteams://teams.microsoft.com");
    const iframe = document.createElement("iframe");
    iframe.style.display = "none";
    iframe.src = teamsDeep;
    document.body.appendChild(iframe);
    setTimeout(() => { document.body.removeChild(iframe); window.open(link, "_blank"); }, 1500);
  } else if (platform === "webex" || link.includes("webex.com")) {
    const iframe = document.createElement("iframe");
    iframe.style.display = "none";
    iframe.src = link.replace("https://", "webex://");
    document.body.appendChild(iframe);
    setTimeout(() => { document.body.removeChild(iframe); window.open(link, "_blank"); }, 1500);
  } else {
    window.open(link, "_blank");
  }
}

window.copySessionCode = function () {
  const code = el("generatedCode")?.innerText;
  if (!code) return;
  navigator.clipboard.writeText(code).then(() => {
    const btn = el("copyCodeBtn");
    if (btn) { btn.innerText = "✅ Copied!"; setTimeout(() => btn.innerText = "📋 Copy Code", 1500); }
  });
};

function formatHHMM(date) {
  const h = String(date.getHours()).padStart(2, "0");
  const m = String(date.getMinutes()).padStart(2, "0");
  return `${h}:${m}`;
}

function isTimeInPast(timeStr) {
  if (!timeStr) return false;
  const now = new Date();
  const todayStr = now.toISOString().split("T")[0];
  const timeMs = Date.parse(`${todayStr}T${timeStr}`);
  return !isNaN(timeMs) && timeMs < now.getTime() - 60000;
}

window.toggleScheduleContainer = function () {
  const isChecked = el("sessionScheduleToggle")?.checked;
  const container = el("scheduleFieldsContainer");
  if (container) container.style.display = isChecked ? "block" : "none";
  if (el("btnStartSubmit")) {
    el("btnStartSubmit").innerHTML = isChecked ? '<i class="fas fa-calendar-plus"></i> Schedule Session' : '<i class="fas fa-circle-play"></i> Start Session & Get Code';
  }
  
  if (isChecked) {
    const now = new Date();
    const todayStr = now.toISOString().split("T")[0];
    const dateInput = el("sessionScheduleDate");
    if (dateInput) {
      dateInput.min = todayStr;
      if (!dateInput.value || dateInput.value < todayStr) {
        dateInput.value = todayStr;
      }
    }

    const startTimeInput = el("sessionScheduleTime");
    const endTimeInput   = el("sessionScheduleEndTime");
    
    if (startTimeInput) {
      if (!startTimeInput.value || (dateInput?.value === todayStr && isTimeInPast(startTimeInput.value))) {
        const futureStart = new Date(now.getTime() + 5 * 60000);
        startTimeInput.value = formatHHMM(futureStart);
      }
    }
    
    if (endTimeInput && startTimeInput?.value) {
      const selectedDate = dateInput?.value || todayStr;
      const startMs = Date.parse(`${selectedDate}T${startTimeInput.value}`);
      if (!isNaN(startMs)) {
        const futureEnd = new Date(startMs + 60 * 60000);
        endTimeInput.value = formatHHMM(futureEnd);
      }
    }
  }
};

window.onScheduleTimeChange = function () {
  const dateVal = el("sessionScheduleDate")?.value || new Date().toISOString().split("T")[0];
  const startTimeVal = el("sessionScheduleTime")?.value;
  const endTimeInput = el("sessionScheduleEndTime");
  if (startTimeVal && endTimeInput) {
    const startMs = Date.parse(`${dateVal}T${startTimeVal}`);
    if (!isNaN(startMs)) {
      const futureEnd = new Date(startMs + 60 * 60000);
      endTimeInput.value = formatHHMM(futureEnd);
    }
  }
};

window.startSession = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const name         = el("sessionName")?.value?.trim();
  const skill        = el("sessionSkill")?.value?.trim();
  const platform     = el("sessionPlatform")?.value;
  const link         = el("sessionLink")?.value?.trim();

  if (!name)     { alert("Enter session name"); return; }
  if (!platform) { alert("Select a platform"); return; }
  if (platform !== "zoom" && platform !== "meet") { alert("Only Zoom and Google Meet are allowed."); return; }
  if (!link)     { alert("Paste the meeting link"); return; }
  if (!link.startsWith("http")) { alert("Meeting link must start with http:// or https://"); return; }

  const isScheduled = el("sessionScheduleToggle")?.checked;
  let startTimeMs = Date.now();
  let endTimeMs   = 0;
  
  if (isScheduled) {
    const dateVal    = el("sessionScheduleDate")?.value;
    const timeVal    = el("sessionScheduleTime")?.value;
    const endTimeVal = el("sessionScheduleEndTime")?.value;
    if (!dateVal || !timeVal) { alert("Please select both Date and Start Time for scheduling."); return; }
    
    const todayStr = new Date().toISOString().split("T")[0];
    if (dateVal < todayStr) {
      alert("Cannot schedule a session for a past date. Please select today or a future date.");
      return;
    }

    startTimeMs = Date.parse(`${dateVal}T${timeVal}`);
    if (isNaN(startTimeMs)) { alert("Invalid start date/time"); return; }
    
    const now = Date.now();
    if (dateVal === todayStr && startTimeMs < now - 60 * 1000) {
      const nowStr = new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
      alert(`Cannot schedule a session for a past time. Current time is ${nowStr}. Please pick a future start time.`);
      return;
    }

    if (endTimeVal) {
      endTimeMs = Date.parse(`${dateVal}T${endTimeVal}`);
      if (isNaN(endTimeMs) || endTimeMs <= startTimeMs) {
        alert("End Time must be after Start Time.");
        return;
      }
      if (endTimeMs <= now) {
        alert("End Time has already passed. Please pick a future end time.");
        return;
      }
    } else {
      const durationMins = Number(el("sessionDuration")?.value || 60);
      endTimeMs = startTimeMs + durationMins * 60 * 1000;
    }
  } else {
    const durationMins = Number(el("sessionDuration")?.value || 60);
    startTimeMs = Date.now();
    endTimeMs   = startTimeMs + durationMins * 60 * 1000;
  }

  const durationMins = Math.round((endTimeMs - startTimeMs) / 60000);

  if (!isScheduled) {
    const existing = await getDocs(
      query(collection(db, "sessions"), where("hostId", "==", user.uid), where("status", "==", "live"))
    );
    if (!existing.empty) { alert("You already have a live session! End it first."); return; }
  }

  const snap        = await getDoc(doc(db, "users", user.uid));
  const hostName    = snap.data()?.name || "Host";
  const code        = generateCode();
  
  const startDateObj = new Date(startTimeMs);
  const endDateObj   = new Date(endTimeMs);

  const formatAMPM = (date) => {
    return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  };

  const startTimeStr = formatAMPM(startDateObj);
  const endTimeStr   = formatAMPM(endDateObj);
  const timeRangeStr = `${startTimeStr} – ${endTimeStr}`;

  await addDoc(collection(db, "sessions"), {
    hostId: user.uid, hostName, name, skill: skill || "", platform,
    platformLabel: PLATFORM_LABELS[platform] || platform,
    meetingLink: link, code, status: isScheduled ? "scheduled" : "live", 
    startTime: startTimeMs,
    endTime: endTimeMs,
    durationMins: durationMins,
    startTimeStr: startTimeStr,
    endTimeStr: endTimeStr,
    timeRangeStr: timeRangeStr,
    startDateStr: startDateObj.toLocaleDateString(),
    isScheduled: isScheduled,
    scheduledTime: startTimeMs,
    participants: [], feedback: [], ratings: []
  });

  if (!isScheduled) {
    await updateDoc(doc(db, "users", user.uid), { sessions: (snap.data()?.sessions || 0) + 1 });
    if (el("generatedCode"))  el("generatedCode").innerText      = code;
    if (el("sessionCodeBox")) el("sessionCodeBox").style.display = "block";
    loadProfile();
  } else {
    alert(`Session scheduled successfully for ${timeRangeStr}!`);
    el("sessionName").value = "";
    el("sessionSkill").value = "";
    el("sessionPlatform").value = "";
    el("sessionLink").value = "";
    el("sessionScheduleToggle").checked = false;
    toggleScheduleContainer();
    loadCalendarSessions();
  }
};

window.endSession = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const snap = await getDocs(
    query(collection(db, "sessions"), where("hostId", "==", user.uid), where("status", "in", ["live", "scheduled"]))
  );
  if (snap.empty) { alert("No active session found."); return; }
  const sessionDoc   = snap.docs[0];
  const d            = sessionDoc.data();
  const now          = new Date();
  const durationMins = Math.floor((Date.now() - (d.startTime || Date.now())) / 60000);
  await updateDoc(doc(db, "sessions", sessionDoc.id), {
    status: "ended", endTime: Date.now(),
    endTimeStr: now.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
    endDateStr: now.toLocaleDateString(), durationMins
  });
  if (el("sessionCodeBox")) el("sessionCodeBox").style.display = "none";
  if (el("sessionName"))    el("sessionName").value    = "";
  if (el("sessionSkill"))   el("sessionSkill").value   = "";
  if (el("sessionPlatform")) el("sessionPlatform").value = "";
  if (el("sessionLink"))    el("sessionLink").value    = "";
  alert(`Session ended! Duration: ${durationMins} min. Saved to history.`);
};

function loadMyActiveSession() {
  const container = el("myActiveSession");
  if (!container) return;
  const user = auth.currentUser;
  if (!user) return;
  onSnapshot(
    query(collection(db, "sessions"), where("hostId", "==", user.uid), where("status", "in", ["live", "scheduled"])),
    snap => {
      container.innerHTML = "";
      const now = Date.now();
      const activeDoc = snap.docs.find(docSnap => {
        const d = docSnap.data();
        const startTimeMs = (d.startTime && d.startTime.seconds) ? d.startTime.seconds * 1000 : Number(d.startTime || d.scheduledTime || 0);
        const durationMs  = (d.durationMins || 60) * 60 * 1000;
        const endTimeMs   = d.endTime || (startTimeMs + durationMs);
        return now >= startTimeMs && now < endTimeMs;
      });

      if (!activeDoc) {
        container.innerHTML = `<p style="color:#888;text-align:center;padding:20px;">No active session right now.</p>`;
        return;
      }
      const d = activeDoc.data();
      container.innerHTML = `
        <div class="card" style="border-left:4px solid #e74c3c;margin-bottom:16px;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px;">
            <h3 style="margin:0;">${d.name}</h3>
            <span style="background:#e74c3c;color:#fff;padding:3px 10px;border-radius:20px;font-size:12px;font-weight:600;">🔴 LIVE</span>
          </div>
          <p style="margin:4px 0;color:#555;">Skill: ${d.skill || "—"}</p>
          <p style="margin:4px 0;color:#555;">Platform: <b>${d.platformLabel || "—"}</b></p>
          <p style="margin:4px 0;color:#555;">Code: <b style="font-size:22px;letter-spacing:6px;color:#2c3e50;">${d.code}</b></p>
          <p style="margin:4px 0;color:#555;">Started: ${d.startTimeStr} on ${d.startDateStr}</p>
          <p style="margin:4px 0;color:#555;">Running: ${formatDuration(d.startTime)}</p>
          <p style="margin:4px 0;color:#555;">Participants: ${d.participants?.length || 0}</p>
        </div>`;
    }
  );
}
window.loadMyActiveSession = loadMyActiveSession;

window.joinSession = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const codeInput = el("joinCodeInput");
  const code      = codeInput?.value?.trim()?.toUpperCase();
  if (!code || code.length < 6) { alert("Enter the 6-character code your teacher/host shared with you."); return; }
  const resultBox = el("joinResult");
  if (resultBox) resultBox.innerHTML = `<div style="text-align:center;padding:20px;color:#888;"><p style="margin:0;font-weight:600;">Verifying your code...</p></div>`;
  const snap = await getDocs(
    query(collection(db, "sessions"), where("code", "==", code), where("status", "in", ["live", "scheduled"]))
  );
  if (snap.empty) {
    if (resultBox) resultBox.innerHTML = `
      <div style="background:#fdecea;padding:20px;border-radius:12px;border-left:4px solid #e74c3c;text-align:center;">
        <p style="margin:0;color:#c0392b;font-weight:700;font-size:15px;">❌ Invalid or Expired Code</p>
        <p style="margin:8px 0 0;color:#888;font-size:13px;">Ask your host/teacher to resend the correct code via Chat.</p>
      </div>`;
    return;
  }
  const sessionDoc = snap.docs[0];
  const d          = sessionDoc.data();
  const hostSnap    = await getDoc(doc(db, "users", d.hostId));
  const hostBlocked = hostSnap.data()?.blocked || [];
  if (hostBlocked.includes(user.uid)) {
    if (resultBox) resultBox.innerHTML = `
      <div style="background:#fdecea;padding:20px;border-radius:12px;border-left:4px solid #e74c3c;text-align:center;">
        <p style="margin:0;color:#c0392b;font-weight:700;">🚫 Access Denied</p>
      </div>`;
    return;
  }
  
  const mySnap = await getDoc(doc(db, "users", user.uid));
  const myData = mySnap.data();
  const following = myData?.following || [];
  const followers = myData?.followers || [];
  const isRelated = d.hostId === user.uid || following.includes(d.hostId) || followers.includes(d.hostId);

  if (!isRelated) {
    if (resultBox) resultBox.innerHTML = `
      <div style="background:#fdecea;padding:20px;border-radius:12px;border-left:4px solid #e74c3c;text-align:center;">
        <p style="margin:0;color:#c0392b;font-weight:700;">🚫 You must be following or followed by the host to join this session.</p>
      </div>`;
    return;
  }

  // Check if session time limit is over
  const startTimeMs = (d.startTime && d.startTime.seconds) ? d.startTime.seconds * 1000 : Number(d.startTime || d.scheduledTime || 0);
  const durationMs  = (d.durationMins || 60) * 60 * 1000;
  const endTimeMs   = d.endTime || (startTimeMs + durationMs);
  const now         = Date.now();
  if (now >= endTimeMs) {
    alert("Session time limit is over.");
    await updateDoc(doc(db, "sessions", sessionDoc.id), { status: "ended" });
    return;
  }
  
  if (d.status === "scheduled" && now >= startTimeMs) {
    await updateDoc(doc(db, "sessions", sessionDoc.id), { status: "live" }).catch(() => {});
  }

  await updateDoc(doc(db, "sessions", sessionDoc.id), { participants: arrayUnion(user.uid) });
  const platformIcons = { zoom:"🟦", meet:"🟢", teams:"🟣", webex:"🔵", jitsi:"🟩", whereby:"🟧", other:"📹" };
  const icon = platformIcons[d.platform] || "📹";
  if (resultBox) resultBox.innerHTML = `
    <div style="background:linear-gradient(135deg,#e8f5e9,#f0fff4);padding:24px;border-radius:14px;border-left:4px solid #27ae60;text-align:center;">
      <div style="font-size:44px;margin-bottom:10px;">✅</div>
      <h3 style="margin:0 0 4px;color:#27ae60;font-size:18px;">Code Verified! Joining...</h3>
      <div style="background:#fff;border-radius:10px;padding:14px;text-align:left;margin-bottom:14px;">
        <p style="margin:4px 0;color:#2c3e50;font-size:14px;"><b>Session:</b> ${d.name}</p>
        <p style="margin:4px 0;color:#2c3e50;font-size:14px;"><b>Host:</b> ${d.hostName}</p>
        <p style="margin:4px 0;color:#2c3e50;font-size:14px;"><b>Platform:</b> ${icon} ${d.platformLabel || "—"}</p>
      </div>
    </div>`;
  setTimeout(() => openMeetingLink(d.meetingLink, d.platform, endTimeMs, sessionDoc.id), 1000);
  if (codeInput) codeInput.value = "";
};

window.loadLiveSessions = function () {
  const container = el("liveSessionContainer");
  if (!container) return;
  const user = auth.currentUser;
  if (!user) return;
  
  onSnapshot(doc(db, "users", user.uid), (mySnap) => {
    const myData = mySnap.data() || {};
    const following = myData.following || [];
    const followers = myData.followers || [];
    
    onSnapshot(query(collection(db, "sessions"), where("status", "in", ["live", "scheduled"])), snap => {
      container.innerHTML = "";
      const now = Date.now();
      const visibleDocs = snap.docs.filter(docSnap => {
        const d = docSnap.data();
        const startTimeMs = (d.startTime && d.startTime.seconds) ? d.startTime.seconds * 1000 : Number(d.startTime || d.scheduledTime || 0);
        const durationMs  = (d.durationMins || 60) * 60 * 1000;
        const endTimeMs   = d.endTime || (startTimeMs + durationMs);
        
        // Auto-end session if time is over
        if (now >= endTimeMs) {
          if (d.status !== "ended") {
            updateDoc(doc(db, "sessions", docSnap.id), { status: "ended", endTime: now }).catch(() => {});
          }
          return false;
        }

        // Must be live now: status == "live" OR (status == "scheduled" and start time has arrived)
        const isLiveNow = d.status === "live" || (d.status === "scheduled" && now >= startTimeMs);
        if (!isLiveNow) return false;

        // Automatically promote scheduled session to "live" status when start time arrives
        if (d.status === "scheduled" && now >= startTimeMs) {
          updateDoc(doc(db, "sessions", docSnap.id), { status: "live" }).catch(() => {});
        }
        
        // Only visible to Host, Followers, and Following
        return d.hostId === user.uid || following.includes(d.hostId) || followers.includes(d.hostId);
      });
      
      if (visibleDocs.length === 0) {
        container.innerHTML = `<div style="text-align:center;padding:50px 20px;color:#888;"><div style="font-size:48px;margin-bottom:12px;">🎙</div><p style="font-size:16px;font-weight:600;margin:0 0 6px;">No live sessions right now</p></div>`;
        return;
      }
      
      container.innerHTML = `
        <div style="background:#fff8e1;border:1.5px solid #f39c12;border-radius:10px;padding:12px 16px;margin-bottom:16px;display:flex;align-items:flex-start;gap:10px;">
          <span style="font-size:20px;margin-top:1px;">🔐</span>
          <div>
            <p style="margin:0 0 2px;font-weight:700;color:#7d5a00;font-size:13px;">Followers & Following Live Sessions</p>
            <p style="margin:0;font-size:12px;color:#7d5a00;line-height:1.5;">Only live sessions from your followers or people you follow appear here.</p>
          </div>
        </div>`;
        
      const platformIcons = { zoom:"🟦", meet:"🟢" };
      visibleDocs.forEach(docSnap => {
        const d      = docSnap.data();
        const isHost = d.hostId === user.uid;
        const icon   = platformIcons[d.platform] || "📹";
        const startTimeMs = (d.startTime && d.startTime.seconds) ? d.startTime.seconds * 1000 : Number(d.startTime || d.scheduledTime || 0);
        const durationMs  = (d.durationMins || 60) * 60 * 1000;
        const endTimeMs   = d.endTime || (startTimeMs + durationMs);
        
        const clickAction = isHost
          ? `openMeetingAsHost('${docSnap.id}', ${endTimeMs}, ${startTimeMs})`
          : `joinSessionDirect('${docSnap.id}', '${d.meetingLink}', '${d.platform}', ${endTimeMs}, ${startTimeMs})`;
          
        container.innerHTML += `
          <div class="card" onclick="${clickAction}" style="border-left:4px solid ${isHost ? '#3498db' : '#e74c3c'};margin-bottom:14px;cursor:pointer;transition:transform 0.2s, box-shadow 0.2s;" onmouseover="this.style.transform='translateY(-2px)';this.style.boxShadow='0 4px 12px rgba(0,0,0,0.1)';" onmouseout="this.style.transform='none';this.style.boxShadow='none';">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px;">
              <h3 style="margin:0;font-size:16px;">${d.name}</h3>
              ${isHost ? `<span style="background:#3498db;color:#fff;padding:3px 10px;border-radius:20px;font-size:11px;font-weight:700;">👑 YOUR SESSION</span>`
                       : `<span style="background:#e74c3c;color:#fff;padding:3px 10px;border-radius:20px;font-size:11px;font-weight:700;">🔴 LIVE</span>`}
            </div>
            <p style="margin:5px 0;color:#555;font-size:13px;">🧑‍🏫 Host: <b>${d.hostName}</b></p>
            <p style="margin:5px 0;color:#555;font-size:13px;">📚 Skill: ${d.skill || "—"}</p>
            <p style="margin:5px 0;color:#555;font-size:13px;">⏱ Duration: <b>${d.durationMins || 60} mins</b></p>
            <p style="margin:5px 0;color:#555;font-size:13px;">${icon} Platform: ${d.platformLabel || "—"}</p>
            <p style="margin:5px 0;color:#555;font-size:13px;">🕐 Started: ${d.startTimeStr} · ${d.startDateStr}</p>
            <p style="margin:5px 0;color:#555;font-size:13px;">⏱ Running: ${formatDuration(d.startTime)}</p>
            <p style="margin:5px 0;color:#555;font-size:13px;">👥 Participants: ${d.participants?.length || 0}</p>
            <div style="margin-top:14px;background:#eaf4fb;border-radius:10px;padding:14px;text-align:center;">
              <p style="margin:0;font-size:14px;color:#27ae60;font-weight:700;"><i class="fas fa-arrow-up-right-from-square"></i> Click Session to Open Meeting Link Directly</p>
            </div>
          </div>`;
      });
    });
  });
};

window.joinSessionDirect = async function (sessionId, meetingLink, platform, endTime, startTime) {
  const user = auth.currentUser;
  if (!user) return;
  const now = Date.now();
  if (startTime && now < startTime) {
    const timeStr = new Date(startTime).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    alert(`Session has not started yet. Scheduled start time is ${timeStr}.`);
    return;
  }
  if (endTime && now >= endTime) {
    alert("Session time limit is over.");
    await updateDoc(doc(db, "sessions", sessionId), { status: "ended", endTime: now }).catch(() => {});
    return;
  }
  try {
    await updateDoc(doc(db, "sessions", sessionId), { participants: arrayUnion(user.uid) });
    openMeetingLink(meetingLink, platform, endTime, sessionId);
  } catch (err) {
    alert("Error joining session: " + err.message);
  }
};

window.openMeetingAsHost = async function (sessionId, endTime, startTime) {
  const now = Date.now();
  if (startTime && now < startTime) {
    const timeStr = new Date(startTime).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    alert(`Session has not started yet. Scheduled start time is ${timeStr}.`);
    return;
  }
  if (endTime && now >= endTime) {
    alert("Session time limit is over.");
    await updateDoc(doc(db, "sessions", sessionId), { status: "ended", endTime: now }).catch(() => {});
    return;
  }
  const snap = await getDoc(doc(db, "sessions", sessionId));
  if (!snap.exists()) { alert("Session not found."); return; }
  const d = snap.data();
  openMeetingLink(d.meetingLink, d.platform, endTime, sessionId);
};

window.loadSessionHistory = function () {
  const container = el("historyContainer");
  if (!container) return;
  const user = auth.currentUser;
  if (!user) return;
  
  onSnapshot(doc(db, "users", user.uid), (mySnap) => {
    const myData = mySnap.data() || {};
    const following = myData?.following || [];
    const followers = myData?.followers || [];
    
    onSnapshot(collection(db, "sessions"), snap => {
      container.innerHTML = "";
      
      let attendedCount = 0;
      let hostedCount = 0;
      let skippedCount = 0;
      const historyList = [];
      const now = Date.now();
      
      snap.forEach(docSnap => {
        const d = docSnap.data();
        const id = docSnap.id;
        const isHost = d.hostId === user.uid;
        const isParticipant = d.participants && d.participants.includes(user.uid);
        const isRelated = isHost || following.includes(d.hostId) || followers.includes(d.hostId);

        if (!isRelated) return;

        const startTimeMs = (d.startTime && d.startTime.seconds) ? d.startTime.seconds * 1000 : Number(d.startTime || 0);
        const durationMs  = (d.durationMins || 60) * 60 * 1000;
        const endTimeMs   = d.endTime || (startTimeMs + durationMs);
        const isExpired   = now >= endTimeMs || d.status === "ended";

        if (!isExpired) return;

        let userStatus = "";
        let badgeColor = "";
        
        if (isHost) {
          userStatus = "👑 Hosted";
          badgeColor = "#3b82f6";
          hostedCount++;
        } else if (isParticipant) {
          userStatus = "✅ Attended";
          badgeColor = "#27ae60";
          attendedCount++;
        } else {
          userStatus = "⏭️ Skipped";
          badgeColor = "#ef4444";
          skippedCount++;
        }

        historyList.push({ id, ...d, userStatus, badgeColor, endTimeMs });
      });
      
      if (historyList.length === 0) {
        container.innerHTML = `<div style="text-align:center;padding:40px 20px;color:#888;"><div style="font-size:40px;">📋</div><p>No session history available yet.</p></div>`;
        return;
      }
      
      historyList.sort((a, b) => b.endTimeMs - a.endTimeMs);

      let html = `
        <div style="background:#fff; border-radius:12px; padding:16px; margin-bottom:16px; border:1px solid #e2e8f0; display:grid; grid-template-columns:1fr 1fr 1fr; gap:10px; text-align:center;">
          <div style="background:#e8f5e9; padding:10px; border-radius:8px;">
            <div style="font-size:22px; font-weight:800; color:#27ae60;">${attendedCount}</div>
            <div style="font-size:11px; font-weight:700; color:#555;">Attended</div>
          </div>
          <div style="background:#eff6ff; padding:10px; border-radius:8px;">
            <div style="font-size:22px; font-weight:800; color:#3b82f6;">${hostedCount}</div>
            <div style="font-size:11px; font-weight:700; color:#555;">Hosted</div>
          </div>
          <div style="background:#fef2f2; padding:10px; border-radius:8px;">
            <div style="font-size:22px; font-weight:800; color:#ef4444;">${skippedCount}</div>
            <div style="font-size:11px; font-weight:700; color:#555;">Skipped</div>
          </div>
        </div>`;

      historyList.forEach(d => {
        const avgRating = d.ratings?.length ? (d.ratings.reduce((a, b) => a + b.stars, 0) / d.ratings.length).toFixed(1) : null;
        const stars = avgRating ? "⭐".repeat(Math.round(avgRating)) + ` <b>${avgRating}/5</b>` : `<span style="color:#bbb;">No ratings</span>`;
        
        html += `
          <div class="card" style="border-left:4px solid ${d.badgeColor}; margin-bottom:14px;">
            <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:8px;">
              <h3 style="margin:0; font-size:16px;">${d.name}</h3>
              <span style="background:${d.badgeColor}; color:#fff; padding:3px 10px; border-radius:20px; font-size:11px; font-weight:700;">${d.userStatus}</span>
            </div>
            <p style="margin:4px 0; color:#555; font-size:13px;">Host: <b>${d.hostName}</b></p>
            <p style="margin:4px 0; color:#555; font-size:13px;">Skill: ${d.skill || "—"}</p>
            <p style="margin:4px 0; color:#555; font-size:13px;">Platform: ${d.platformLabel || "—"}</p>
            <p style="margin:4px 0; color:#555; font-size:13px;">${d.startDateStr} · ${d.startTimeStr}</p>
            <p style="margin:4px 0; color:#555; font-size:13px;">Duration: ${d.durationMins || 60} mins</p>
            <p style="margin:4px 0; color:#555; font-size:13px;">Rating: ${stars}</p>
          </div>`;
      });

      container.innerHTML = html;
    });
  });
};

window.loadFeedbackSessions = function () {
  const select = el("feedbackSessionSelect");
  if (!select) return;
  onSnapshot(query(collection(db, "sessions"), where("status", "==", "ended")), snap => {
    select.innerHTML = "<option value=''>-- Select Session --</option>";
    snap.forEach(docSnap => {
      const d = docSnap.data();
      select.innerHTML += `<option value="${docSnap.id}">${d.name} (${d.hostName})</option>`;
    });
  });
};

window.submitFeedbackAndRating = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const sessionId = el("feedbackSessionSelect")?.value;
  const text      = el("feedbackText")?.value?.trim();
  if (!sessionId) { alert("Select a session"); return; }
  if (!selectedRating) { alert("Select star rating (1-5) by clicking the stars"); return; }
  if (!text) { alert("Please write feedback comments"); return; }

  const userSnap = await getDoc(doc(db, "users", user.uid));
  const name = userSnap.data()?.name || "User";

  const sessSnap = await getDoc(doc(db, "sessions", sessionId));
  if (!sessSnap.exists()) return;
  const existing = (sessSnap.data()?.ratings || []).find(r => r.userId === user.uid);
  if (existing) { alert("You have already rated/reviewed this session."); return; }

  try {
    await updateDoc(doc(db, "sessions", sessionId), {
      ratings: arrayUnion({ userId: user.uid, name, stars: selectedRating, time: Date.now() }),
      feedback: arrayUnion({ userId: user.uid, name, text, time: Date.now() })
    });
    alert("Review submitted successfully! Thank you!");
    
    // Clear fields
    selectedRating = 0;
    for (let i = 1; i <= 5; i++) { const s = el("star" + i); if (s) s.innerText = "☆"; }
    if (el("feedbackText")) el("feedbackText").value = "";
    loadRatingsContainer();
  } catch (err) {
    alert("Submission failed: " + err.message);
  }
};

window.setRating = function (stars) {
  selectedRating = stars;
  for (let i = 1; i <= 5; i++) {
    const star = el("star" + i);
    if (star) star.innerText = i <= stars ? "★" : "☆";
  }
};

window.loadRatingsSessions = function () {
  const select = el("rateSessionSelect");
  if (!select) return;
  onSnapshot(collection(db, "sessions"), snap => {
    select.innerHTML = "<option value=''>-- Select Session --</option>";
    snap.forEach(docSnap => {
      const d = docSnap.data();
      select.innerHTML += `<option value="${docSnap.id}">${d.name} by ${d.hostName}</option>`;
    });
  });
};

window.submitRating = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const sessionId = el("rateSessionSelect")?.value;
  if (!sessionId)      { alert("Select a session"); return; }
  if (!selectedRating) { alert("Select star rating (1-5)"); return; }
  const snap     = await getDoc(doc(db, "users", user.uid));
  const name     = snap.data()?.name || "User";
  const sessSnap = await getDoc(doc(db, "sessions", sessionId));
  const existing = (sessSnap.data()?.ratings || []).find(r => r.userId === user.uid);
  if (existing) { alert("You have already rated this session."); return; }
  await updateDoc(doc(db, "sessions", sessionId), {
    ratings: arrayUnion({ userId: user.uid, name, stars: selectedRating, time: Date.now() })
  });
  alert("Rating submitted! ⭐ " + selectedRating + "/5");
  selectedRating = 0;
  for (let i = 1; i <= 5; i++) { const s = el("star" + i); if (s) s.innerText = "☆"; }
  loadRatingsContainer();
};

window.loadRatingsContainer = function () {
  const container = el("ratingsContainer");
  if (!container) return;
  onSnapshot(collection(db, "sessions"), snap => {
    container.innerHTML = "";
    let count = 0;
    snap.forEach(docSnap => {
      const d = docSnap.data();
      if (!d.ratings?.length) return;
      count++;
      const avg = (d.ratings.reduce((a, b) => a + b.stars, 0) / d.ratings.length).toFixed(1);
      
      const feedbacks = d.feedback || [];
      let reviewsHtml = "";
      d.ratings.forEach(r => {
        const textFeedback = feedbacks.find(f => f.userId === r.userId)?.text || "";
        reviewsHtml += `
          <div style="padding:10px; border-bottom:1px solid #f0f0f0; background:#fbfbfb; border-radius:8px; margin-bottom:8px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
              <span style="font-weight:700; font-size:13px; color:#333;">${r.name}</span>
              <span style="color:#f39c12; font-size:13px;">${"★".repeat(r.stars)}${"☆".repeat(5 - r.stars)}</span>
            </div>
            ${textFeedback ? `<p style="margin:0; font-size:13px; color:#555; line-height:1.4;">"${textFeedback}"</p>` : ''}
          </div>`;
      });
      
      container.innerHTML += `
        <div class="card" style="margin-bottom:16px; border:1px solid #e2e8f0; border-radius:12px; padding:16px; background:#fff; box-shadow:0 2px 6px rgba(0,0,0,0.01);">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; border-bottom:1.5px solid #f1f5f9; padding-bottom:8px;">
            <h4 style="margin:0; font-size:15px; color:#2c3e50;">${d.name} <small style="color:#7f8c8d; font-weight:normal;">by ${d.hostName}</small></h4>
            <span style="background:#fef3c7; color:#d97706; padding:3px 8px; border-radius:12px; font-size:11px; font-weight:700;">⭐ Avg: ${avg}/5</span>
          </div>
          <div>${reviewsHtml}</div>
        </div>`;
    });
    if (count === 0) {
      container.innerHTML = `<div style="text-align:center;padding:30px;color:#888;">No ratings yet</div>`;
    }
  });
};

/* ================ PDF ================ */
window.loadPDFs = function () {
  const pdfList = el("pdfList");
  if (!pdfList) return;
  const user = auth.currentUser;
  if (!user) return;
  
  onSnapshot(doc(db, "users", user.uid), (mySnap) => {
    const myData = mySnap.data() || {};
    const following = myData.following || [];
    const followers = myData.followers || [];
    
    onSnapshot(collection(db, "pdfs"), (snapshot) => {
      pdfList.innerHTML = "";
      let count = 0;
      snapshot.forEach((docSnap) => {
        const data = docSnap.data();
        const id = docSnap.id;
        
        // Only visible to uploader themselves, followers, and following
        const isRelated = data.uploaderId === user.uid || following.includes(data.uploaderId) || followers.includes(data.uploaderId);
        if (!isRelated) return;
        
        count++;
        const isMine = data.uploaderId === user.uid;
        const isDownloaded = data.downloads && data.downloads.includes(user.uid);
        
        const badgeText = isMine ? "📤 Sent" : (isDownloaded ? "📥 Opened" : "🆕 New");
        const badgeBg = isMine ? "#e0f2fe" : (isDownloaded ? "#ecfdf5" : "#fff7ed");
        const badgeColor = isMine ? "#0369a1" : (isDownloaded ? "#047857" : "#c2410c");
        
        const deleteBtn = isMine 
          ? `<button onclick="deletePDF('${id}')" style="background:#fee2e2; color:#ef4444; border:none; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;"><i class="fas fa-trash"></i> Delete</button>`
          : "";
          
        pdfList.innerHTML += `
          <div class="card" style="margin-bottom:12px; border-left:4px solid ${isMine ? '#3b82f6' : '#10b981'}; padding:16px; background:#f8f9fa; border:1px solid #e9ecef; border-radius:10px; display:flex; flex-direction:column; gap:8px;">
            <div style="display:flex; justify-content:space-between; align-items:center;">
              <b style="font-size:14px; color:#2c3e50; word-break:break-all;">${data.fileName}</b>
              <span style="background:${badgeBg}; color:${badgeColor}; padding:3px 8px; border-radius:12px; font-size:10px; font-weight:700;">${badgeText}</span>
            </div>
            <div style="font-size:12px; color:#7f8c8d;">
              Uploaded by: ${data.uploadedBy}
            </div>
            <div style="display:flex; justify-content:space-between; align-items:center; margin-top:4px;">
              <div style="display:flex; gap:8px;">
                <button onclick="viewPDF('${id}')" style="background:#3b82f6; color:#fff; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; border:none; cursor:pointer; width:auto !important;"><i class="fas fa-eye"></i> View</button>
                <button onclick="downloadPDF('${id}')" style="background:#10b981; color:#fff; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; border:none; cursor:pointer; width:auto !important;"><i class="fas fa-download"></i> Download</button>
              </div>
              ${deleteBtn}
            </div>
          </div>`;
      });
      if (count === 0) {
        pdfList.innerHTML = `<p style="color:#888;text-align:center;padding:20px;">No PDFs shared with you yet</p>`;
      }
    });
  });
};

window.viewPDF = async function (pdfId) {
  try {
    const snap = await getDoc(doc(db, "pdfs", pdfId));
    if (!snap.exists()) { alert("PDF file not found."); return; }
    const data = snap.data();
    trackPDFDownload(pdfId);
    
    let fileURL = data.fileURL;
    if (fileURL.startsWith("data:")) {
      const parts = fileURL.split(";base64,");
      const contentType = parts[0].split(":")[1] || "application/pdf";
      const raw = window.atob(parts[1]);
      const uInt8Array = new Uint8Array(raw.length);
      for (let i = 0; i < raw.length; i++) uInt8Array[i] = raw.charCodeAt(i);
      const blob = new Blob([uInt8Array], { type: contentType });
      fileURL = URL.createObjectURL(blob);
    }
    const win = window.open(fileURL, "_blank");
    if (!win) {
      alert("Pop-up blocked! Please allow pop-ups for this site to view the PDF.");
    }
  } catch (err) {
    alert("Error opening PDF: " + err.message);
  }
};

window.downloadPDF = async function (pdfId) {
  try {
    const snap = await getDoc(doc(db, "pdfs", pdfId));
    if (!snap.exists()) { alert("PDF file not found."); return; }
    const data = snap.data();
    trackPDFDownload(pdfId);

    let fileURL = data.fileURL;
    const fileName = data.fileName || "note.pdf";

    if (fileURL.startsWith("data:")) {
      const parts = fileURL.split(";base64,");
      const contentType = parts[0].split(":")[1] || "application/pdf";
      const raw = window.atob(parts[1]);
      const uInt8Array = new Uint8Array(raw.length);
      for (let i = 0; i < raw.length; i++) uInt8Array[i] = raw.charCodeAt(i);
      const blob = new Blob([uInt8Array], { type: contentType });
      fileURL = URL.createObjectURL(blob);
    }

    const a = document.createElement("a");
    a.href = fileURL;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  } catch (err) {
    alert("Error downloading PDF: " + err.message);
  }
};

window.uploadPDF = function () {
  const fileInput = el("pdfFile");
  if (!fileInput || !fileInput.files.length) {
    alert("Please select a PDF file first.");
    return;
  }
  const file = fileInput.files[0];
  if (file.type !== "application/pdf") {
    alert("Only PDF files are allowed.");
    return;
  }
  if (file.size > 700 * 1024) {
    alert("PDF notes are limited to 700 KB. Please select a smaller PDF file.");
    return;
  }
  
  const user = auth.currentUser;
  if (!user) return;
  
  const progressWrap = el("uploadProgressWrap");
  const progressBar = el("uploadProgress");
  const btn = el("uploadPDFBtn");
  
  if (progressWrap) progressWrap.style.display = "block";
  if (btn) btn.disabled = true;
  if (progressBar) progressBar.value = 10;
  
  const reader = new FileReader();
  reader.readAsDataURL(file);
  
  reader.onprogress = (event) => {
    if (event.lengthComputable && progressBar) {
      const percent = (event.loaded / event.total) * 60;
      progressBar.value = 10 + percent;
    }
  };
  
  reader.onload = async () => {
    try {
      if (progressBar) progressBar.value = 80;
      const base64Data = reader.result;
      
      await addDoc(collection(db, "pdfs"), {
        fileName: file.name,
        uploadedBy: user.displayName || user.email || "Unknown",
        uploaderId: user.uid,
        fileURL: base64Data,
        uploadedAt: serverTimestamp(),
        downloads: []
      });
      
      if (progressBar) progressBar.value = 100;
      setTimeout(() => {
        alert("PDF uploaded successfully!");
        fileInput.value = "";
        if (progressWrap) progressWrap.style.display = "none";
        if (btn) btn.disabled = false;
        loadPDFs();
      }, 300);
      
    } catch (err) {
      alert("Error saving record: " + err.message);
      if (progressWrap) progressWrap.style.display = "none";
      if (btn) btn.disabled = false;
    }
  };
  
  reader.onerror = () => {
    alert("Error reading file.");
    if (progressWrap) progressWrap.style.display = "none";
    if (btn) btn.disabled = false;
  };
};

window.deletePDF = async function (pdfId) {
  if (!confirm("Are you sure you want to delete this PDF?")) return;
  try {
    await deleteDoc(doc(db, "pdfs", pdfId));
    alert("PDF deleted successfully!");
  } catch(e) {
    alert("Error: " + e.message);
  }
};

window.trackPDFDownload = async function (pdfId) {
  const user = auth.currentUser;
  if (!user) return;
  try {
    await updateDoc(doc(db, "pdfs", pdfId), {
      downloads: arrayUnion(user.uid)
    });
  } catch (e) {
    console.error("Error tracking PDF download:", e);
  }
};



/* ================================================
   TEST SYSTEM
   ================================================ */
let currentTestId   = null;
let currentTestData = null;
let userAnswers     = {};

window.recordAnswer = function (input) {
  const qId = input.getAttribute("data-qid");
  userAnswers[qId] = input.value;
  const card = document.getElementById("qcard_" + qId);
  if (card) {
    card.querySelectorAll("label").forEach(l => {
      l.style.borderColor = "#e8ecf0";
      l.style.background  = "#fff";
    });
    const lbl = input.closest("label");
    if (lbl) {
      lbl.style.borderColor = "#3498db";
      lbl.style.background  = "#eaf4fb";
    }
  }
};

window.openTestScreen = function (screenId) {
  if (el("testDashboard")) el("testDashboard").style.display = "none";
  document.querySelectorAll("#testSection .screen").forEach(s => s.style.display = "none");
  const screen = el(screenId);
  if (screen) screen.style.display = "block";
  if (screenId === "prepareTestScreen") {
    if (typeof onQbSkillOrDiffChange === "function") onQbSkillOrDiffChange();
  } else if (screenId === "attendTestScreen") {
    const availablePanel = el("availableTests");
    const questionsPanel = el("testQuestions");
    const resultsPanel   = el("resultsContainer");
    if (availablePanel) availablePanel.style.display = "block";
    if (questionsPanel) questionsPanel.style.display  = "none";
    if (resultsPanel)   resultsPanel.style.display    = "none";
    loadAvailableTests();
  } else if (screenId === "testHistoryScreen") {
    loadTestHistory();
  }
};

window.backTestDashboard = function () {
  document.querySelectorAll("#testSection .screen").forEach(s => s.style.display = "none");
  const dash = el("testDashboard");
  if (dash) dash.style.display = "grid";
};

window.createTest = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const title = el("testTitle")?.value?.trim();
  const skill = el("testSkill")?.value?.trim();
  if (!title) { alert("Enter test title"); return; }
  if (!skill) { alert("Enter skill name"); return; }
  const snap = await getDoc(doc(db, "users", user.uid));
  const creatorName = snap.data()?.name || "Unknown";
  await addDoc(collection(db, "tests"), {
    creatorId: user.uid, creatorName, title, skill,
    createdAt: serverTimestamp()
  });
  alert("✅ Test Created!");
  if (el("testTitle")) el("testTitle").value = "";
  if (el("testSkill")) el("testSkill").value = "";
  loadMyTests();
  loadTestHistory();
};

window.loadMyTests = function () {
  const user = auth.currentUser;
  if (!user) return;
  const testList   = el("myTestsList") || el("testList");
  const testSelect = el("testSelect");
  onSnapshot(
    query(collection(db, "tests"), where("creatorId", "==", user.uid)),
    async snap => {
      if (testList)   testList.innerHTML   = "";
      if (testSelect) testSelect.innerHTML = "<option value=''>-- Select Test --</option>";
      if (snap.empty) {
        if (testList) testList.innerHTML = `<p style="color:#888;text-align:center;padding:20px;">No tests created yet</p>`;
        return;
      }
      for (const docSnap of snap.docs) {
        const d  = docSnap.data();
        const id = docSnap.id;
        const qSnap = await getDocs(query(collection(db, "questions"), where("testId", "==", id)));
        const count = qSnap.size;

        listHtml += `
          <div class="card" style="border-left:4px solid #3498db;margin-bottom:12px;display:flex;justify-content:space-between;align-items:center;padding:16px;">
            <div style="flex:1;">
              <h3 style="margin:0 0 4px;font-size:16px;color:#2c3e50;font-weight:700;">${d.title}</h3>
              <p style="margin:2px 0;color:#555;font-size:13px;">📚 Skill: ${d.skill}</p>
              <p style="margin:2px 0;color:#27ae60;font-weight:700;font-size:12px;">❓ Questions: ${count} / 5 max</p>
            </div>
            <button onclick="deleteTest('${id}')"
                    style="width:auto !important;max-width:120px;flex-shrink:0;margin-left:12px;background:#fee2e2;color:#ef4444;border:1px solid #fca5a5;padding:8px 14px;border-radius:8px;cursor:pointer;font-size:12px;font-weight:700;">
              🗑 Delete
            </button>
          </div>`;
        
        selectHtml += `<option value="${id}">${d.title} (${count}/5 Qs)</option>`;
      }
      if (testList) testList.innerHTML = listHtml;
      if (testSelect) testSelect.innerHTML = selectHtml;
    }
  );
};

window.deleteTest = async function (testId) {
  if (!confirm("Are you sure you want to delete this test and all its questions?")) return;
  try {
    const qSnap = await getDocs(query(collection(db, "questions"), where("testId", "==", testId)));
    await Promise.all(qSnap.docs.map(d => deleteDoc(d.ref)));
    await deleteDoc(doc(db, "tests", testId));
    alert("✅ Test deleted successfully!");
    if (typeof loadMyTests === "function") loadMyTests();
    if (typeof loadTestHistory === "function") loadTestHistory();
    if (typeof loadAvailableTests === "function") loadAvailableTests();
  } catch (err) {
    alert("Error deleting test: " + err.message);
  }
};

window.addQuestion = async function () {
  const user          = auth.currentUser;
  if (!user) return;
  const testId        = el("testSelect")?.value;
  const question      = el("question")?.value?.trim();
  const option1       = el("option1")?.value?.trim();
  const option2       = el("option2")?.value?.trim();
  const option3       = el("option3")?.value?.trim();
  const option4       = el("option4")?.value?.trim();
  const correctAnswer = el("correctAnswer")?.value?.trim();
  if (!testId)        { alert("Select a test first"); return; }

  // Check max 5 questions limit
  const existingSnap = await getDocs(query(collection(db, "questions"), where("testId", "==", testId)));
  if (existingSnap.size >= 5) {
    alert("Maximum limit of 5 questions per test reached!");
    return;
  }

  if (!question)      { alert("Enter a question"); return; }
  if (!option1 || !option2 || !option3 || !option4) { alert("Fill all 4 options"); return; }
  if (!correctAnswer) { alert("Select the correct answer"); return; }

  await addDoc(collection(db, "questions"), {
    testId, creatorId: user.uid,
    question, option1, option2, option3, option4,
    correctAnswer, createdAt: serverTimestamp()
  });
  alert(`✅ Question Added! (${existingSnap.size + 1} / 5 max)`);
  ["question","option1","option2","option3","option4","correctAnswer"]
    .forEach(id => { if (el(id)) el(id).value = ""; });
  loadMyTests();
};

window.loadMyQuestions = function () {
  const user = auth.currentUser;
  if (!user) return;
  const container = el("myQuestions");
  if (!container) return;
  onSnapshot(
    query(collection(db, "questions"), where("creatorId", "==", user.uid)),
    snap => {
      container.innerHTML = "";
      if (snap.empty) {
        container.innerHTML = `<p style="color:#888;text-align:center;padding:20px;">No questions added yet</p>`;
        return;
      }
      snap.forEach(docSnap => {
        const d  = docSnap.data();
        const id = docSnap.id;
        container.innerHTML += `
          <div class="card" style="margin-bottom:12px;">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;">
              <div style="flex:1;padding-right:10px;">
                <h3 style="margin:0 0 8px;font-size:15px;">${d.question}</h3>
                <p style="margin:3px 0;color:#555;font-size:13px;">A: ${d.option1}</p>
                <p style="margin:3px 0;color:#555;font-size:13px;">B: ${d.option2}</p>
                <p style="margin:3px 0;color:#555;font-size:13px;">C: ${d.option3}</p>
                <p style="margin:3px 0;color:#555;font-size:13px;">D: ${d.option4}</p>
                <p style="margin:6px 0 0;color:#27ae60;font-weight:600;font-size:13px;">✅ Correct: ${d.correctAnswer}</p>
              </div>
              <button onclick="deleteQuestion('${id}')"
                      style="width:auto !important;max-width:120px;flex-shrink:0;background:#fee2e2;color:#ef4444;border:1px solid #fca5a5;padding:6px 12px;border-radius:8px;cursor:pointer;font-size:12px;">
                🗑 Delete
              </button>
            </div>
          </div>`;
      });
    }
  );
};

window.deleteQuestion = async function (questionId) {
  if (!confirm("Delete this question?")) return;
  await deleteDoc(doc(db, "questions", questionId));
  alert("Question deleted!");
};

window.loadAvailableTests = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const container = el("availableTests");
  if (!container) return;
  container.innerHTML = `<div style="text-align:center;padding:30px;color:#888;"><p>Loading tests from your connections...</p></div>`;
  
  try {
    // Exclude tests already attempted by current user so they disappear after attending!
    const attemptsSnap = await getDocs(query(collection(db, "testAttempts"), where("userId", "==", user.uid)));
    const attemptedTestIds = new Set(attemptsSnap.docs.map(d => d.data().testId));

    const mySnap    = await getDoc(doc(db, "users", user.uid));
    const myData    = mySnap.data() || {};
    const following = myData.following || [];
    const followers = myData.followers || [];

    // Tests created by users the current user follows or who follow current user (EXCLUDING self)
    const allowedCreators = Array.from(new Set([...following, ...followers])).filter(id => id !== user.uid);

    if (!allowedCreators.length) {
      container.innerHTML = `
        <div style="text-align:center;padding:40px 20px;color:#888;">
          <div style="font-size:48px;margin-bottom:12px;">🤝</div>
          <p style="font-size:16px;font-weight:600;margin:0 0 6px;color:#555;">No connection tests available</p>
          <p style="font-size:13px;margin:0;">Follow users or get followers to see and attend their skill tests. Your own tests appear in Created Tests & History.</p>
        </div>`;
      return;
    }

    const chunks = [];
    for (let i = 0; i < allowedCreators.length; i += 10) chunks.push(allowedCreators.slice(i, i + 10));
    
    let allTests = [];
    for (const chunk of chunks) {
      const snap = await getDocs(query(collection(db, "tests"), where("creatorId", "in", chunk)));
      snap.forEach(docSnap => {
        // Exclude tests created by self OR already completed/attended!
        const testData = docSnap.data();
        if (testData.creatorId !== user.uid && !attemptedTestIds.has(docSnap.id)) {
          allTests.push({ id: docSnap.id, ...testData });
        }
      });
    }

    container.innerHTML = "";
    let validTestsCount = 0;

    for (const test of allTests) {
      const qSnap = await getDocs(query(collection(db, "questions"), where("testId", "==", test.id)));
      const qCount = qSnap.size;
      
      // Skip tests with 0 questions
      if (qCount === 0) continue;
      
      validTestsCount++;
      container.innerHTML += `
        <div class="card" style="border-left:4px solid #27ae60;margin-bottom:14px;background:#f9fbf9;border:1px solid #e2e8f0;padding:16px;border-radius:12px;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
            <h3 style="margin:0;font-size:16px;color:#2c3e50;">${test.title}</h3>
            <span style="background:#e0f2fe;color:#0369a1;padding:3px 10px;border-radius:20px;font-size:11px;font-weight:700;">👥 CONNECTION TEST</span>
          </div>
          <p style="margin:4px 0;color:#555;font-size:13px;">📚 Skill Topic: <b>${test.skill}</b></p>
          <p style="margin:4px 0;color:#555;font-size:13px;">👤 Created By: <b>${test.creatorName || "Host"}</b></p>
          <p style="margin:4px 0;color:#27ae60;font-weight:600;font-size:13px;">❓ Questions: ${qCount} questions (Max 5)</p>
          <button onclick="startTest('${test.id}')"
                  style="margin-top:12px;background:#27ae60;color:#fff;border:none;padding:12px 20px;border-radius:8px;cursor:pointer;font-size:14px;font-weight:700;width:100%;">
            🚀 Start Test
          </button>
        </div>`;
    }

    if (validTestsCount === 0) {
      container.innerHTML = `
        <div style="text-align:center;padding:40px 20px;color:#888;">
          <div style="font-size:48px;margin-bottom:12px;">📝</div>
          <p style="font-size:15px;font-weight:600;margin:0 0 6px;color:#555;">No tests available</p>
          <p style="font-size:13px;margin:0;">You have attended all available tests or your connections haven't published any tests with questions yet.</p>
        </div>`;
    }
  } catch (err) {
    container.innerHTML = `<p style="color:#ef4444;">Error loading tests: ${err.message}</p>`;
  }
};

/* ================================================
   QUESTION BANK & PREPARE TEST MODULE
   ================================================ */
let preparedQuestionsBank = [];
let currentTestStartTime = null;

window.onQbSkillOrDiffChange = function () {
  const preview = el("qbQuestionsPreview");
  if (preview) preview.style.display = "none";
  preparedQuestionsBank = [];
};

window.loadRandom5Questions = async function () {
  const skill = el("qbSkillSelect")?.value;
  const diffRadio = document.querySelector('input[name="qbDifficulty"]:checked');
  const difficulty = diffRadio ? diffRadio.value : "Basic";

  if (!skill) {
    alert("Please select a skill first.");
    return;
  }

  const listContainer = el("qbQuestionsList");
  const previewContainer = el("qbQuestionsPreview");
  if (!listContainer || !previewContainer) return;

  listContainer.innerHTML = `<div style="text-align:center;padding:24px;color:#3b82f6;font-weight:700;"><i class="fas fa-spinner fa-spin"></i> Fetching 5 random questions from QuestionBank/${skill}/${difficulty}.json...</div>`;
  previewContainer.style.display = "block";

  try {
    const jsonUrl = `./QuestionBank/${encodeURIComponent(skill)}/${difficulty}.json`;
    const resp = await fetch(jsonUrl);
    if (!resp.ok) {
      throw new Error(`Could not load ${jsonUrl}`);
    }
    const allQuestions = await resp.json();
    if (!Array.isArray(allQuestions) || allQuestions.length === 0) {
      throw new Error("Question Bank file is empty.");
    }

    // Shuffle and select 5 distinct non-duplicate questions
    const shuffled = [...allQuestions].sort(() => 0.5 - Math.random());
    preparedQuestionsBank = shuffled.slice(0, 5);

    listContainer.innerHTML = "";
    preparedQuestionsBank.forEach((q, idx) => {
      const optionsHtml = q.options.map(opt => `
        <div style="background:#f8fafc; border:1px solid #e2e8f0; padding:8px 12px; border-radius:6px; margin:4px 0; font-size:13px; color:#334155; ${opt === q.answer ? 'border-left:4px solid #10b981; font-weight:700; background:#f0fdf4;' : ''}">
          ${opt} ${opt === q.answer ? '<span style="color:#10b981; float:right; font-size:11px;">✓ Correct Answer</span>' : ''}
        </div>
      `).join("");

      listContainer.innerHTML += `
        <div class="card" style="border-left:4px solid #3b82f6; margin-bottom:14px; background:#ffffff; padding:16px; border-radius:12px; border:1px solid #e2e8f0;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
            <span style="font-weight:700; color:#3b82f6; font-size:14px;">Question ${idx + 1} of 5</span>
            <span style="background:#e0f2fe; color:#0369a1; padding:2px 8px; border-radius:12px; font-size:11px; font-weight:700;">Topic: ${q.topic || skill}</span>
          </div>
          <p style="font-weight:700; color:#1e293b; margin:0 0 10px; font-size:14px;">${q.question}</p>
          ${optionsHtml}
          <p style="margin:10px 0 0; font-size:12px; color:#64748b; background:#f1f5f9; padding:8px 12px; border-radius:6px; border-left:3px solid #3b82f6;">
            💡 <b>Explanation:</b> ${q.explanation}
          </p>
        </div>`;
    });

  } catch (err) {
    listContainer.innerHTML = `<p style="color:#ef4444; font-weight:700; text-align:center; padding:16px;">Error loading Question Bank: ${err.message}</p>`;
  }
};

window.publishPreparedTest = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const skill = el("qbSkillSelect")?.value;
  const diffRadio = document.querySelector('input[name="qbDifficulty"]:checked');
  const difficulty = diffRadio ? diffRadio.value : "Basic";

  if (!skill || !preparedQuestionsBank || preparedQuestionsBank.length !== 5) {
    alert("Please fetch 5 random questions first before publishing.");
    return;
  }

  try {
    const userSnap = await getDoc(doc(db, "users", user.uid));
    const creatorName = userSnap.data()?.name || user.displayName || "Creator";
    const testTitle = `${skill} (${difficulty}) Test`;

    // 1. Save Test Document to Firestore
    const testRef = await addDoc(collection(db, "tests"), {
      creatorId: user.uid,
      creatorName: creatorName,
      title: testTitle,
      skill: skill,
      difficulty: difficulty,
      questionCount: 5,
      createdAt: serverTimestamp()
    });

    // 2. Save each of the 5 questions to Firestore `questions` collection
    for (const q of preparedQuestionsBank) {
      await addDoc(collection(db, "questions"), {
        testId: testRef.id,
        creatorId: user.uid,
        question: q.question,
        option1: q.options[0] || "",
        option2: q.options[1] || "",
        option3: q.options[2] || "",
        option4: q.options[3] || "",
        correctAnswer: q.answer,
        explanation: q.explanation || "",
        difficulty: difficulty,
        skill: skill,
        topic: q.topic || "",
        createdAt: serverTimestamp()
      });
    }

    alert(`✅ Test Published Successfully!\n"${testTitle}" with 5 Question Bank questions is published and visible to your connected followers.`);
    
    // Reset form & state
    preparedQuestionsBank = [];
    if (el("qbQuestionsPreview")) el("qbQuestionsPreview").style.display = "none";
    if (el("qbSkillSelect")) el("qbSkillSelect").value = "";
    
    if (typeof loadMyTests === "function") loadMyTests();
    if (typeof loadTestHistory === "function") loadTestHistory();

    openTestScreen("testHistoryScreen");
  } catch (err) {
    alert("Error publishing test: " + err.message);
  }
};

window.startTest = async function (testId) {
  const user = auth.currentUser;
  if (!user) return;
  currentTestId   = testId;
  userAnswers     = {};
  currentTestStartTime = Date.now();

  const testSnap  = await getDoc(doc(db, "tests", testId));
  currentTestData = testSnap.data();
  const qSnap = await getDocs(query(collection(db, "questions"), where("testId", "==", testId)));

  const availablePanel = el("availableTests");
  const questionsPanel = el("testQuestions");
  const resultsPanel   = el("resultsContainer");

  if (availablePanel) availablePanel.style.display = "none";
  if (resultsPanel)   resultsPanel.style.display   = "none";
  if (questionsPanel) questionsPanel.style.display  = "block";

  if (!questionsPanel) return;

  if (qSnap.empty) {
    questionsPanel.innerHTML = `
      <div style="text-align:center;padding:40px;color:#888;">
        <p>This test has no questions yet.</p>
        <button onclick="backToTestList()"
                style="margin-top:12px;background:#3498db;color:#fff;border:none;padding:10px 20px;border-radius:8px;cursor:pointer;">
          ← Back
        </button>
      </div>`;
    return;
  }

  questionsPanel.innerHTML = `
    <div style="background:linear-gradient(135deg,#3498db,#2c3e50);color:#fff;padding:18px;border-radius:12px;margin-bottom:18px;text-align:center;">
      <h2 style="margin:0 0 4px;">${currentTestData?.title || "Skill Test"}</h2>
      <p style="margin:0;opacity:0.85;font-size:13px;">Skill: <b>${currentTestData?.skill || "General"}</b> | Level: <b>${currentTestData?.difficulty || "Basic"}</b> | ${qSnap.size} Question(s)</p>
    </div>`;

  let qIndex = 0;
  qSnap.forEach(docSnap => {
    qIndex++;
    const q   = docSnap.data();
    const qId = docSnap.id;
    const options = [q.option1, q.option2, q.option3, q.option4].filter(Boolean);

    const optionItems = options.map(opt => {
      const safeValue = opt.replace(/&/g, "&amp;").replace(/"/g, "&quot;").replace(/'/g, "&#39;");
      return `
        <label style="display:flex;align-items:center;gap:10px;padding:11px 14px;margin:6px 0;border:2px solid #e8ecf0;border-radius:8px;cursor:pointer;font-size:14px;">
          <input type="radio"
                 name="q_${qId}"
                 value="${safeValue}"
                 data-qid="${qId}"
                 onclick="recordAnswer(this)"
                 style="accent-color:#3498db;width:16px;height:16px;flex-shrink:0;">
          <span>${opt}</span>
        </label>`;
    }).join("");

    questionsPanel.innerHTML += `
      <div class="card" style="margin-bottom:16px;border-left:3px solid #3498db;" id="qcard_${qId}">
        <div style="display:flex; justify-content:space-between; margin-bottom:6px;">
          <span style="font-size:12px; font-weight:700; color:#3b82f6;">Question ${qIndex}</span>
          ${q.topic ? `<span style="font-size:11px; background:#e0f2fe; color:#0369a1; padding:2px 8px; border-radius:10px; font-weight:700;">${q.topic}</span>` : ''}
        </div>
        <h3 style="margin:0 0 14px;font-size:15px;color:#2c3e50;line-height:1.4;">${q.question}</h3>
        ${optionItems}
      </div>`;
  });

  questionsPanel.innerHTML += `
    <button onclick="completeTest()"
            style="width:100%;padding:15px;background:linear-gradient(135deg,#27ae60,#2ecc71);color:#fff;border:none;border-radius:10px;font-size:16px;font-weight:700;cursor:pointer;margin-top:8px;margin-bottom:24px;">
      ✅ Submit Test
    </button>`;
};

window.backToTestList = function () {
  const availablePanel = el("availableTests");
  const questionsPanel = el("testQuestions");
  const resultsPanel   = el("resultsContainer");
  if (questionsPanel) questionsPanel.style.display = "none";
  if (resultsPanel)   resultsPanel.style.display   = "none";
  if (availablePanel) availablePanel.style.display  = "block";
  currentTestId   = null;
  currentTestData = null;
  userAnswers     = {};
  loadAvailableTests();
  loadTestHistory();
};

window.completeTest = async function () {
  const user = auth.currentUser;
  if (!user || !currentTestId) return;
  
  const endTime = Date.now();
  const timeTakenSec = Math.max(1, Math.round((endTime - (currentTestStartTime || endTime)) / 1000));
  const mins = Math.floor(timeTakenSec / 60);
  const secs = timeTakenSec % 60;
  const timeTakenStr = mins > 0 ? `${mins}m ${secs}s` : `${secs}s`;

  const qSnap = await getDocs(query(collection(db, "questions"), where("testId", "==", currentTestId)));
  let score = 0, total = 0;
  const results = [];
  qSnap.forEach(docSnap => {
    total++;
    const q        = docSnap.data();
    const selected = userAnswers[docSnap.id];
    const isCorrect = selected === q.correctAnswer;
    if (isCorrect) score++;
    results.push({
      question: q.question,
      selected: selected || "Not answered",
      correct: q.correctAnswer,
      explanation: q.explanation || "No explanation available.",
      isCorrect
    });
  });

  const wrongCount = total - score;
  const percentage = total > 0 ? Math.round((score / total) * 100) : 0;
  
  let userName = user.displayName || "User";
  try {
    const uSnap = await getDoc(doc(db, "users", user.uid));
    if (uSnap.exists() && uSnap.data()?.name) userName = uSnap.data().name;
  } catch (e) {}

  await addDoc(collection(db, "testAttempts"), {
    userId: user.uid,
    userName: userName,
    testId: currentTestId,
    testTitle: currentTestData?.title || "Skill Test",
    skill: currentTestData?.skill || "General",
    difficulty: currentTestData?.difficulty || "Basic",
    score,
    total,
    correctAnswers: score,
    wrongAnswers: wrongCount,
    percentage,
    timeTakenSec,
    timeTakenStr,
    attemptedAt: serverTimestamp()
  });

  const questionsPanel = el("testQuestions");
  const resultsPanel   = el("resultsContainer");
  if (questionsPanel) questionsPanel.style.display = "none";
  if (resultsPanel)   resultsPanel.style.display   = "block";

  if (resultsPanel) {
    const resultRows = results.map(r => `
      <div style="padding:12px 14px;margin:8px 0;border-radius:10px;
                  background:${r.isCorrect ? '#f0fdf4' : '#fff5f5'};
                  border-left:4px solid ${r.isCorrect ? '#27ae60' : '#e74c3c'}; border:1px solid ${r.isCorrect ? '#bbf7d0' : '#fecaca'};">
        <p style="margin:0 0 6px;font-weight:700;font-size:14px;color:#2c3e50;">${r.question}</p>
        <p style="margin:0 0 4px;font-size:13px;color:#555;">Your answer: <b>${r.selected}</b> ${r.isCorrect ? '✅' : '❌'}</p>
        ${!r.isCorrect ? `<p style="margin:2px 0 4px;font-size:13px;color:#27ae60;">Correct answer: <b>${r.correct}</b></p>` : ""}
        <p style="margin:6px 0 0;font-size:12px;color:#64748b;background:#ffffff;padding:8px;border-radius:6px;border:1px dashed #cbd5e1;">
          💡 <b>Explanation:</b> ${r.explanation}
        </p>
      </div>`).join("");

    resultsPanel.innerHTML = `
      <div style="background:#fff;border-radius:16px;padding:24px;box-shadow:0 4px 20px rgba(0,0,0,0.08);border:2px solid #27ae60;text-align:center;">
        <div style="font-size:56px;line-height:1;margin-bottom:10px;">🎉</div>
        <h2 style="margin:0 0 6px;color:#27ae60;font-size:24px;font-weight:800;">Test Completed!</h2>
        <p style="margin:0 0 16px;color:#7f8c8d;font-size:14px;">Test: <b>${currentTestData?.title || "Skill Test"}</b></p>
        
        <div style="display:grid; grid-template-columns: repeat(4, 1fr); gap:10px; margin-bottom:22px;">
          <div style="background:#f8f9fa;padding:10px;border-radius:10px;border:1px solid #e9ecef;">
            <div style="font-size:20px;font-weight:800;color:#2c3e50;">${total}</div>
            <div style="font-size:10px;color:#888;font-weight:700;margin-top:2px;">TOTAL Qs</div>
          </div>
          <div style="background:#f0fdf4;padding:10px;border-radius:10px;border:1px solid #bbf7d0;">
            <div style="font-size:20px;font-weight:800;color:#166534;">${score}</div>
            <div style="font-size:10px;color:#15803d;font-weight:700;margin-top:2px;">CORRECT</div>
          </div>
          <div style="background:#fef2f2;padding:10px;border-radius:10px;border:1px solid #fecaca;">
            <div style="font-size:20px;font-weight:800;color:#991b1b;">${wrongCount}</div>
            <div style="font-size:10px;color:#b91c1c;font-weight:700;margin-top:2px;">WRONG</div>
          </div>
          <div style="background:#eff6ff;padding:10px;border-radius:10px;border:1px solid #bfdbfe;">
            <div style="font-size:20px;font-weight:800;color:#1e40af;">${percentage}%</div>
            <div style="font-size:10px;color:#1d4ed8;font-weight:700;margin-top:2px;">ACCURACY</div>
          </div>
        </div>

        <div style="background:#f8fafc; padding:10px; border-radius:8px; margin-bottom:18px; text-align:center; font-size:13px; font-weight:700; color:#3b82f6;">
          ⏱️ Time Taken: ${timeTakenStr}
        </div>

        <div style="text-align:left;">
          <h4 style="margin:0 0 10px;color:#2c3e50;font-size:15px;"><i class="fas fa-list-check"></i> Detailed Review & Explanations</h4>
          ${resultRows}
        </div>

        <button onclick="backToTestList()"
                style="width:100%;padding:14px;background:#27ae60;color:#fff;border:none;border-radius:10px;cursor:pointer;font-size:15px;font-weight:700;margin-top:20px;">
          ← Back to Available Tests
        </button>
      </div>`;
  }
  currentTestId   = null;
  currentTestData = null;
  userAnswers     = {};
  loadAvailableTests();
  loadTestHistory();
};

let unsubCreatedHistory = null;
let unsubAttendedHistory = null;

window.loadTestHistory = function () {
  const user = auth.currentUser;
  if (!user) return;
  const container = el("testHistoryContainer");
  if (!container) return;

  if (unsubCreatedHistory) { unsubCreatedHistory(); unsubCreatedHistory = null; }
  if (unsubAttendedHistory) { unsubAttendedHistory(); unsubAttendedHistory = null; }

  container.innerHTML = `<div style="text-align:center;padding:30px;color:#888;">Loading history...</div>`;

  let latestCreatedDocs = [];
  let latestAttendedDocs = [];

  async function renderHistory() {
    let createdHtml = "";
    if (!latestCreatedDocs.length) {
      createdHtml = `<p style="color:#888;font-size:13px;padding:10px 0;">No tests created by you yet.</p>`;
    } else {
      const createdAttemptsList = await Promise.all(latestCreatedDocs.map(async docSnap => {
        const testId = docSnap.id;
        const qSnap = await getDocs(query(collection(db, "questions"), where("testId", "==", testId)));
        const qCount = qSnap.size;
        const attemptsSnap = await getDocs(query(collection(db, "testAttempts"), where("testId", "==", testId)));
        
        const attendees = await Promise.all(attemptsSnap.docs.map(async aDoc => {
          const aData = aDoc.data();
          let name = aData.userName;
          if (!name && aData.userId) {
            try {
              const uSnap = await getDoc(doc(db, "users", aData.userId));
              name = uSnap.data()?.name || "User";
            } catch { name = "User"; }
          }
          const dateStr = aData.attemptedAt?.toDate ? aData.attemptedAt.toDate().toLocaleDateString() : "Recently";
          const timeStr = aData.timeTakenStr || (aData.timeTakenSec ? `${aData.timeTakenSec}s` : "N/A");
          return {
            attendeeName: name || "User",
            score: aData.score ?? 0,
            total: aData.total ?? 5,
            percentage: aData.percentage ?? 0,
            timeTaken: timeStr,
            dateStr: dateStr
          };
        }));

        return { qCount, attendees };
      }));

      latestCreatedDocs.forEach((docSnap, index) => {
        const d = docSnap.data();
        const id = docSnap.id;
        const info = createdAttemptsList[index];
        const dateStr = d.createdAt?.toDate ? d.createdAt.toDate().toLocaleDateString() : "Recently";

        let attendeesHtml = "";
        if (!info.attendees.length) {
          attendeesHtml = `<p style="font-size:12px; color:#888; margin:8px 0 0;"><i class="fas fa-info-circle"></i> No users have attended this test yet.</p>`;
        } else {
          const attendeeRows = info.attendees.map(a => `
            <div style="display:flex; justify-content:space-between; align-items:center; background:#f8fafc; padding:8px 12px; border-radius:6px; border:1px solid #e2e8f0; font-size:12px; margin-top:4px;">
              <div>
                <b style="color:#1e293b;"><i class="fas fa-user"></i> ${a.attendeeName}</b>
                <span style="color:#64748b; font-size:11px; margin-left:8px;">📅 ${a.dateStr}</span>
              </div>
              <div>
                <span style="background:#f0fdf4; color:#166534; font-weight:700; padding:3px 8px; border-radius:10px; margin-right:6px;">Score: ${a.score}/${a.total} (${a.percentage}%)</span>
                <span style="background:#eff6ff; color:#1d4ed8; font-weight:700; padding:3px 8px; border-radius:10px;">⏱️ ${a.timeTaken}</span>
              </div>
            </div>
          `).join("");

          attendeesHtml = `
            <div style="margin-top:10px; background:#ffffff; border:1px solid #cbd5e1; border-radius:8px; padding:10px;">
              <b style="font-size:12px; color:#3b82f6; display:block; margin-bottom:4px;"><i class="fas fa-users"></i> Test Attendees (${info.attendees.length}):</b>
              ${attendeeRows}
            </div>`;
        }

        createdHtml += `
          <div style="background:#f8fafc; border-radius:12px; padding:14px 16px; border:1px solid #e2e8f0; margin-bottom:14px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
              <div>
                <b style="font-size:16px; color:#1e293b;">${d.title}</b>
                <span style="font-size:12px; color:#64748b; margin-left:10px;">Skill: <b>${d.skill || "General"}</b> (${d.difficulty || "Basic"}) | ❓ ${info.qCount} Qs</span>
              </div>
              <button onclick="deleteTest('${id}')" style="width:auto !important; max-width:100px; flex-shrink:0; background:#fee2e2; color:#ef4444; border:1px solid #fca5a5; padding:5px 10px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">
                🗑 Delete
              </button>
            </div>
            <div style="font-size:11px; color:#94a3b8;">📅 Created: ${dateStr}</div>
            ${attendeesHtml}
          </div>`;
      });
    }

    let attendedHtml = "";
    if (!latestAttendedDocs.length) {
      attendedHtml = `<p style="color:#888;font-size:13px;padding:10px 0;">No tests attended by you yet.</p>`;
    } else {
      const sortedDocs = [...latestAttendedDocs].sort((a, b) => {
        const aTime = a.data().attemptedAt?.seconds || 0;
        const bTime = b.data().attemptedAt?.seconds || 0;
        return bTime - aTime;
      });

      sortedDocs.forEach(docSnap => {
        const d = docSnap.data();
        const dateStr = d.attemptedAt?.toDate ? d.attemptedAt.toDate().toLocaleDateString() : "Recently";
        const correct = d.correctAnswers ?? d.score ?? 0;
        const wrong = d.wrongAnswers ?? ((d.total || 5) - correct);
        const timeTaken = d.timeTakenStr || (d.timeTakenSec ? `${d.timeTakenSec}s` : "N/A");

        attendedHtml += `
          <div style="background:#f0fdf4; border-radius:12px; padding:14px; border:1px solid #bbf7d0; margin-bottom:12px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
              <b style="font-size:15px; color:#166534;">${d.testTitle || "Skill Test"}</b>
              <span style="background:#27ae60; color:#fff; padding:2px 8px; border-radius:12px; font-size:11px; font-weight:700;">${d.percentage}% Accuracy</span>
            </div>
            <div style="display:grid; grid-template-columns: repeat(4, 1fr); gap:6px; text-align:center; font-size:12px; margin:8px 0;">
              <div style="background:#fff; padding:6px; border-radius:6px; border:1px solid #bbf7d0;">
                <span style="color:#64748b; font-size:10px; display:block; font-weight:700;">SCORE</span>
                <b style="color:#15803d;">${d.score} / ${d.total || 5}</b>
              </div>
              <div style="background:#fff; padding:6px; border-radius:6px; border:1px solid #bbf7d0;">
                <span style="color:#64748b; font-size:10px; display:block; font-weight:700;">CORRECT / WRONG</span>
                <b style="color:#166534;">${correct} ✅</b> | <b style="color:#b91c1c;">${wrong} ❌</b>
              </div>
              <div style="background:#fff; padding:6px; border-radius:6px; border:1px solid #bbf7d0;">
                <span style="color:#64748b; font-size:10px; display:block; font-weight:700;">TIME TAKEN</span>
                <b style="color:#0284c7;">⏱️ ${timeTaken}</b>
              </div>
              <div style="background:#fff; padding:6px; border-radius:6px; border:1px solid #bbf7d0;">
                <span style="color:#64748b; font-size:10px; display:block; font-weight:700;">DATE</span>
                <b style="color:#475569;">📅 ${dateStr}</b>
              </div>
            </div>
          </div>`;
      });
    }

    container.innerHTML = `
      <div style="margin-bottom:24px;">
        <h3 style="margin:0 0 12px; color:#2c3e50; font-size:16px;"><i class="fas fa-file-signature" style="color:#3b82f6;"></i> Created Test History & Attendees (${latestCreatedDocs.length})</h3>
        ${createdHtml}
      </div>
      <div>
        <h3 style="margin:0 0 12px; color:#27ae60; font-size:16px;"><i class="fas fa-user-check" style="color:#27ae60;"></i> Attended Test History (${latestAttendedDocs.length})</h3>
        ${attendedHtml}
      </div>`;
  }

  unsubCreatedHistory = onSnapshot(query(collection(db, "tests"), where("creatorId", "==", user.uid)), snap => {
    latestCreatedDocs = snap.docs;
    renderHistory();
  }, err => console.error("History created error:", err));

  unsubAttendedHistory = onSnapshot(query(collection(db, "testAttempts"), where("userId", "==", user.uid)), snap => {
    latestAttendedDocs = snap.docs;
    renderHistory();
  }, err => console.error("History attended error:", err));
};

/* ================================================
   MY RESULTS — logged-in user's attempts only
   ================================================ */
window.loadMyResults = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const container = el("myResultsContainer");
  if (!container) return;
  container.innerHTML = `<div style="text-align:center;padding:30px;color:#888;">Loading...</div>`;
  
  try {
    // 1. Get tests created by the user
    const createdSnap = await getDocs(query(collection(db, "tests"), where("creatorId", "==", user.uid)));
    const createdCount = createdSnap.size;
    
    // 2. Listen in real-time to tests attended by the user
    onSnapshot(
      query(collection(db, "testAttempts"), where("userId", "==", user.uid)),
      snap => {
        container.innerHTML = `
          <div style="display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-bottom:20px;">
            <div style="background:#e8f4fd; border:1px solid #b3d7ff; border-radius:12px; padding:16px; text-align:center;">
              <div style="font-size:24px; margin-bottom:4px;">✍️</div>
              <div style="font-size:12px; color:#555; font-weight:600; text-transform:uppercase; letter-spacing:0.5px;">Tests Created</div>
              <div style="font-size:28px; font-weight:800; color:#0056b3;">${createdCount}</div>
            </div>
            <div style="background:#ecfdf5; border:1px solid #a7f3d0; border-radius:12px; padding:16px; text-align:center;">
              <div style="font-size:24px; margin-bottom:4px;">📝</div>
              <div style="font-size:12px; color:#555; font-weight:600; text-transform:uppercase; letter-spacing:0.5px;">Tests Attended</div>
              <div style="font-size:28px; font-weight:800; color:#047857;">${snap.size}</div>
            </div>
          </div>
          <h3 style="margin:20px 0 12px; color:#2c3e50;"><i class="fas fa-history"></i> Attended Tests History</h3>
          <div id="resultsHistoryList"></div>`;
        
        const historyList = el("resultsHistoryList");
        if (snap.empty) {
          historyList.innerHTML = `
            <div style="text-align:center;padding:40px 20px;color:#888;">
              <div style="font-size:48px;margin-bottom:12px;">📋</div>
              <p style="font-size:15px;font-weight:600;margin:0 0 6px;color:#555;">No test attempts yet</p>
              <p style="font-size:13px;margin:0;">Go to <b>Attend Test</b> to attempt your first test.</p>
            </div>`;
          return;
        }
        
        const docs = snap.docs.sort((a, b) => {
          const aTime = a.data().attemptedAt?.seconds || 0;
          const bTime = b.data().attemptedAt?.seconds || 0;
          return bTime - aTime;
        });
        
        docs.forEach(docSnap => {
          const d = docSnap.data();
          const color = d.percentage >= 80 ? "#27ae60" : d.percentage >= 50 ? "#f39c12" : "#e74c3c";
          const emoji = d.percentage >= 80 ? "🎉" : d.percentage >= 50 ? "👍" : "📚";
          const date  = d.attemptedAt?.toDate
            ? d.attemptedAt.toDate().toLocaleDateString()
            : "—";
            
          historyList.innerHTML += `
            <div class="card" style="border-left:4px solid ${color};margin-bottom:12px;">
              <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
                <h3 style="margin:0;font-size:15px;">${emoji} ${d.testTitle || "Unnamed Test"}</h3>
                <span style="font-size:20px;font-weight:800;color:${color};">${d.percentage}%</span>
              </div>
              <p style="margin:3px 0;color:#555;font-size:13px;">Score: <b>${d.score}/${d.total}</b></p>
              <p style="margin:3px 0;color:#aaa;font-size:12px;">📅 ${date}</p>
            </div>`;
        });
      }
    );
  } catch (err) {
    console.error("Error loading results:", err);
  }
};

/* ================================================
   LEADERBOARD — all users who attempted tests
   ================================================ */
window.loadLeaderboard = async function () {
  const container = el("leaderboardContainer");
  if (!container) return;
  container.innerHTML = `<div style="text-align:center;padding:30px;color:#888;">Loading leaderboard...</div>`;
  try {
    const attemptsSnap = await getDocs(collection(db, "testAttempts"));
    if (attemptsSnap.empty) {
      container.innerHTML = `
        <div style="text-align:center;padding:40px 20px;color:#888;">
          <div style="font-size:48px;margin-bottom:12px;">🏆</div>
          <p style="font-size:15px;font-weight:600;margin:0 0 6px;color:#555;">No attempts yet</p>
          <p style="font-size:13px;margin:0;">Be the first to complete a test!</p>
        </div>`;
      return;
    }

    // Aggregate stats per user
    const userStats = {};
    attemptsSnap.forEach(docSnap => {
      const d = docSnap.data();
      if (!d.userId) return;
      if (!userStats[d.userId]) {
        userStats[d.userId] = { userId: d.userId, totalScore: 0, totalQuestions: 0, testsAttempted: 0 };
      }
      userStats[d.userId].totalScore      += d.score || 0;
      userStats[d.userId].totalQuestions  += d.total || 0;
      userStats[d.userId].testsAttempted  += 1;
    });

    // Fetch user names
    const userIds   = Object.keys(userStats);
    const nameMap   = {};
    await Promise.all(userIds.map(async uid => {
      try {
        const uSnap = await getDoc(doc(db, "users", uid));
        nameMap[uid] = uSnap.data()?.name || "Unknown User";
      } catch { nameMap[uid] = "Unknown User"; }
    }));

    // Sort by total score (highest first)
    const sorted = userIds
      .map(uid => ({
        ...userStats[uid],
        name: nameMap[uid],
        overallPct: userStats[uid].totalQuestions > 0
          ? Math.round((userStats[uid].totalScore / userStats[uid].totalQuestions) * 100)
          : 0
      }))
      .sort((a, b) => b.totalScore - a.totalScore);

    container.innerHTML = "";
    const medals = ["🥇", "🥈", "🥉"];

    sorted.forEach((u, i) => {
      const rank     = i + 1;
      const medal    = medals[i] || `#${rank}`;
      const barColor = i === 0 ? "#f39c12" : i === 1 ? "#7f8c8d" : i === 2 ? "#cd6133" : "#3498db";
      const barWidth = sorted[0].totalScore > 0 ? Math.round((u.totalScore / sorted[0].totalScore) * 100) : 0;
      const isMe     = auth.currentUser && u.userId === auth.currentUser.uid;

      container.innerHTML += `
        <div class="card" style="margin-bottom:12px;border-left:4px solid ${barColor};${isMe ? 'background:#fffbea;' : ''}">
          <div style="display:flex;align-items:center;gap:14px;">
            <div style="font-size:28px;min-width:36px;text-align:center;">${medal}</div>
            <div style="flex:1;">
              <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:4px;">
                <b style="font-size:15px;">${u.name}${isMe ? ' <span style="background:#3498db;color:#fff;font-size:10px;padding:2px 6px;border-radius:10px;font-weight:600;">YOU</span>' : ''}</b>
                <span style="font-weight:700;color:${barColor};font-size:14px;">Score: ${u.totalScore}/${u.totalQuestions}</span>
              </div>
              <div style="font-size:12px;color:#888;margin-bottom:6px;">
                ${u.testsAttempted} test${u.testsAttempted !== 1 ? 's' : ''} attempted &nbsp;·&nbsp; Accuracy: ${u.overallPct}%
              </div>
              <div style="background:#f0f0f0;border-radius:20px;height:8px;overflow:hidden;">
                <div style="background:${barColor};height:8px;border-radius:20px;width:${barWidth}%;transition:width 0.5s;"></div>
              </div>
            </div>
          </div>
        </div>`;
    });
  } catch (err) {
    container.innerHTML = `<p style="color:#e74c3c;text-align:center;">Error loading leaderboard: ${err.message}</p>`;
  }
};

/* ================================================
   SCREEN NAVIGATION
   ================================================ */
window.openUserScreen = function (screenId) {
  if (el("usersDashboard")) el("usersDashboard").style.display = "none";
  document.querySelectorAll("#users .screen").forEach(s => s.style.display = "none");
  if (el(screenId)) el(screenId).style.display = "block";
  if (screenId === "suggestedScreen") loadSuggestedUsers();
};
window.backUsersDashboard = function () {
  if (el("usersDashboard")) el("usersDashboard").style.display = "grid";
  document.querySelectorAll("#users .screen").forEach(s => s.style.display = "none");
};
window.openProfileScreen = function (screenId) {
  if (el("profileDashboard")) el("profileDashboard").style.display = "none";
  document.querySelectorAll("#profile .screen").forEach(s => s.style.display = "none");
  if (el(screenId)) el(screenId).style.display = "block";
  if (screenId === "editProfileScreen") openProfileScreenAndFill();
};
window.backProfileDashboard = function () {
  if (el("profileDashboard")) el("profileDashboard").style.display = "grid";
  document.querySelectorAll("#profile .screen").forEach(s => s.style.display = "none");
};
window.openChatScreen = function (screenId) {
  if (el("chatDashboard")) el("chatDashboard").style.display = "none";
  document.querySelectorAll("#chat .screen").forEach(s => s.style.display = "none");
  if (el(screenId)) el(screenId).style.display = "block";
  if (screenId === "chatSettingsScreen")  loadChatSettingsPanel();
  if (screenId === "inboxScreen")         loadInbox();
  if (screenId === "blockedUsersScreen")  loadBlockedUsers();
};
window.backChatDashboard = function () {
  if (el("chatDashboard")) el("chatDashboard").style.display = "grid";
  document.querySelectorAll("#chat .screen").forEach(s => s.style.display = "none");
  activeChatUser = null;
  activeChatName = "";
};
window.openSessionScreen = function (screenId) {
  if (el("sessionDashboard")) el("sessionDashboard").style.display = "none";
  document.querySelectorAll("#session .screen").forEach(s => s.style.display = "none");
  if (el(screenId)) el(screenId).style.display = "block";
  if (screenId === "hostSessionScreen")     loadHostSessionPanel();
  if (screenId === "liveSessionScreen")     loadLiveSessions();
  if (screenId === "feedbackSessionScreen") { loadFeedbackSessions(); loadRatingsContainer(); }
  if (screenId === "sessionHistoryScreen")  loadWebSessionHistory();
};

let currentWebSessionTab = "created";

window.switchWebSessionTab = function (tab) {
  currentWebSessionTab = tab;
  const btnCreated  = el("tabCreatedSessions");
  const btnAttended = el("tabAttendedSessions");

  if (tab === "created") {
    if (btnCreated) {
      btnCreated.style.background = "#fff";
      btnCreated.style.color = "#1e293b";
      btnCreated.style.boxShadow = "0 2px 4px rgba(0,0,0,0.05)";
    }
    if (btnAttended) {
      btnAttended.style.background = "transparent";
      btnAttended.style.color = "#64748b";
      btnAttended.style.boxShadow = "none";
    }
  } else {
    if (btnAttended) {
      btnAttended.style.background = "#fff";
      btnAttended.style.color = "#1e293b";
      btnAttended.style.boxShadow = "0 2px 4px rgba(0,0,0,0.05)";
    }
    if (btnCreated) {
      btnCreated.style.background = "transparent";
      btnCreated.style.color = "#64748b";
      btnCreated.style.boxShadow = "none";
    }
  }
  loadWebSessionHistory();
};

window.loadWebSessionHistory = function () {
  const container = el("webSessionHistoryContainer");
  if (!container) return;
  const user = auth.currentUser;
  if (!user) return;

  container.innerHTML = `<div style="text-align:center;padding:30px;color:#888;"><i class="fas fa-spinner fa-spin fa-2x"></i><p style="margin-top:10px;font-weight:600;">Loading Session History...</p></div>`;

  onSnapshot(
    query(collection(db, "sessions"), where("status", "==", "ended")),
    snap => {
      container.innerHTML = "";
      const historyList = [];

      snap.forEach(docSnap => {
        const d = docSnap.data();
        const id = docSnap.id;
        const isHost = d.hostId === user.uid;
        const isParticipant = d.participants && Array.isArray(d.participants) && d.participants.includes(user.uid);

        if (currentWebSessionTab === "created" && isHost) {
          historyList.push({ id, ...d });
        } else if (currentWebSessionTab === "attended" && isParticipant && !isHost) {
          historyList.push({ id, ...d });
        }
      });

      if (historyList.length === 0) {
        container.innerHTML = `
          <div style="text-align:center;padding:50px 20px;background:#fff;border-radius:12px;border:1px solid #e2e8f0;">
            <div style="font-size:42px;margin-bottom:10px;color:#cbd5e1;"><i class="fas fa-history"></i></div>
            <p style="font-size:15px;font-weight:700;color:#64748b;margin:0;">No ${currentWebSessionTab === "created" ? "created" : "attended"} sessions history found.</p>
          </div>`;
        return;
      }

      // Sort by end time / start time descending
      historyList.sort((a, b) => {
        const timeA = a.endTime || a.startTime || 0;
        const timeB = b.endTime || b.startTime || 0;
        return timeB - timeA;
      });

      let html = "";
      historyList.forEach(d => {
        const dateStr = d.startDateStr || d.endDateStr || (d.startTime ? new Date(d.startTime).toLocaleDateString() : "—");
        const startTimeStr = d.startTimeStr || (d.startTime ? new Date(d.startTime).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) : "—");
        const endTimeStr = d.endTimeStr || (d.endTime ? new Date(d.endTime).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) : "—");
        const durationMins = d.durationMins || 0;
        const attendeesCount = d.participants ? d.participants.length : 0;

        if (currentWebSessionTab === "created") {
          html += `
            <div class="card" style="background:#fff; border-radius:12px; border:1px solid #e2e8f0; border-left:5px solid #3b82f6; padding:18px; margin-bottom:14px; box-shadow:0 2px 6px rgba(0,0,0,0.03);">
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
                <h3 style="margin:0; font-size:17px; color:#1e293b; font-weight:700;">${d.name || "Untitled Session"}</h3>
                <span style="background:#22c55e; color:#fff; padding:4px 12px; border-radius:20px; font-size:12px; font-weight:800;"><i class="fas fa-circle-check"></i> Completed</span>
              </div>
              <div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap:10px; margin-top:10px;">
                <p style="margin:0; font-size:13px; color:#475569;"><strong>📚 Skill Name:</strong> ${d.skill || "—"}</p>
                <p style="margin:0; font-size:13px; color:#475569;"><strong>📅 Date:</strong> ${dateStr}</p>
                <p style="margin:0; font-size:13px; color:#475569;"><strong>⏰ Start Time:</strong> ${startTimeStr}</p>
                <p style="margin:0; font-size:13px; color:#475569;"><strong>⏳ End Time:</strong> ${endTimeStr}</p>
                <p style="margin:0; font-size:13px; color:#475569;"><strong>⏱ Duration:</strong> ${durationMins} Minutes</p>
                <p style="margin:0; font-size:13px; color:#475569;"><strong>👥 Attendees:</strong> ${attendeesCount}</p>
              </div>
            </div>`;
        } else {
          html += `
            <div class="card" style="background:#fff; border-radius:12px; border:1px solid #e2e8f0; border-left:5px solid #8b5cf6; padding:18px; margin-bottom:14px; box-shadow:0 2px 6px rgba(0,0,0,0.03);">
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
                <h3 style="margin:0; font-size:17px; color:#1e293b; font-weight:700;">${d.name || "Untitled Session"}</h3>
                <span style="background:#22c55e; color:#fff; padding:4px 12px; border-radius:20px; font-size:12px; font-weight:800;"><i class="fas fa-circle-check"></i> Completed</span>
              </div>
              <div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap:10px; margin-top:10px;">
                <p style="margin:0; font-size:13px; color:#475569;"><strong>📚 Skill Name:</strong> ${d.skill || "—"}</p>
                <p style="margin:0; font-size:13px; color:#475569;"><strong>🧑‍🏫 Session Creator:</strong> Hosted by: ${d.hostName || "Host"}</p>
                <p style="margin:0; font-size:13px; color:#475569;"><strong>📅 Date:</strong> ${dateStr}</p>
                <p style="margin:0; font-size:13px; color:#475569;"><strong>⏰ Start Time:</strong> ${startTimeStr}</p>
                <p style="margin:0; font-size:13px; color:#475569;"><strong>⏳ End Time:</strong> ${endTimeStr}</p>
                <p style="margin:0; font-size:13px; color:#475569;"><strong>⏱ Duration:</strong> ${durationMins} Minutes</p>
              </div>
            </div>`;
        }
      });

      container.innerHTML = html;
    }
  );
};
window.backSessionDashboard = function () {
  if (el("sessionDashboard")) el("sessionDashboard").style.display = "grid";
  document.querySelectorAll("#session .screen").forEach(s => s.style.display = "none");
};
window.openTestScreen = function (screenId) {
  if (el("testDashboard")) el("testDashboard").style.display = "none";
  document.querySelectorAll("#testSection .screen").forEach(s => s.style.display = "none");
  if (el(screenId)) el(screenId).style.display = "block";
  if (screenId === "prepareTestScreen") onQbSkillOrDiffChange();
  if (screenId === "createTestScreen")  loadMyTests();
  if (screenId === "myTestsScreen")     loadMyTests();
  if (screenId === "testHistoryScreen") loadTestHistory();
  if (screenId === "resultsScreen")     loadMyResults();     // ← My Results
  if (screenId === "attendTestScreen") {
    const availablePanel = el("availableTests");
    const questionsPanel = el("testQuestions");
    const resultsPanel   = el("resultsContainer");
    if (availablePanel) availablePanel.style.display = "block";
    if (questionsPanel) questionsPanel.style.display  = "none";
    if (resultsPanel)   resultsPanel.style.display    = "none";
    loadAvailableTests();
  }
};
window.backTestDashboard = function () {
  if (el("testDashboard")) el("testDashboard").style.display = "grid";
  document.querySelectorAll("#testSection .screen").forEach(s => s.style.display = "none");
  currentTestId   = null;
  currentTestData = null;
  userAnswers     = {};
};

window.fillAccountSettings = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const nameInput = el("updateNameInput");
  const emailInput = el("updateEmailInput");
  if (emailInput) emailInput.value = user.email || "";
  
  if (nameInput) {
    const snap = await getDoc(doc(db, "users", user.uid));
    nameInput.value = snap.data()?.name || user.displayName || "";
  }
};

window.updateAccountDetails = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const newName = el("updateNameInput")?.value?.trim();
  if (!newName) {
    alert("Please enter a valid name.");
    return;
  }
  try {
    await updateProfile(user, { displayName: newName });
    await updateDoc(doc(db, "users", user.uid), { name: newName });
    alert("✅ Account name updated successfully!");
    if (el("userName")) el("userName").innerText = newName;
    if (typeof loadProfile === "function") loadProfile();
  } catch (err) {
    alert("Error updating name: " + err.message);
  }
};

window.changePassword = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const newPass = el("newPasswordInput")?.value?.trim();
  const confirmPass = el("confirmPasswordInput")?.value?.trim();

  if (!newPass) {
    alert("Please enter a new password.");
    return;
  }
  if (newPass.length < 6) {
    alert("Password must be at least 6 characters long.");
    return;
  }
  if (newPass !== confirmPass) {
    alert("New password and confirm password do not match.");
    return;
  }

  try {
    await updatePassword(user, newPass);
    alert("✅ Password updated successfully!");
    if (el("newPasswordInput")) el("newPasswordInput").value = "";
    if (el("confirmPasswordInput")) el("confirmPasswordInput").value = "";
    if (el("currentPasswordInput")) el("currentPasswordInput").value = "";
  } catch (err) {
    if (err.code === "auth/requires-recent-login") {
      alert("Security requirement: Please log out and log back in before updating your password.");
    } else {
      alert("Error updating password: " + err.message);
    }
  }
};

window.openSettingsScreen = function (screenId) {
  if (el("settingsDashboard")) el("settingsDashboard").style.display = "none";
  document.querySelectorAll("#settings .screen").forEach(s => s.style.display = "none");
  if (el(screenId)) el(screenId).style.display = "block";
  if (screenId === "accountSettingsScreen") fillAccountSettings();
};
window.backSettingsDashboard = function () {
  if (el("settingsDashboard")) el("settingsDashboard").style.display = "grid";
  document.querySelectorAll("#settings .screen").forEach(s => s.style.display = "none");
};

window.deleteUserAccount = async function () {
  const user = auth.currentUser;
  if (!user) return;
  
  if (!confirm("⚠️ Are you sure you want to permanently delete your account? Your profile and data will be permanently removed from SkillSync and you will no longer appear in user lists.")) return;

  const doubleCheck = prompt("Type DELETE to confirm permanent account deletion:");
  if (doubleCheck !== "DELETE") {
    alert("Account deletion cancelled.");
    return;
  }

  try {
    const uid = user.uid;
    
    // 1. Delete user document from Firestore users collection
    await deleteDoc(doc(db, "users", uid));

    // 2. Delete user account from Firebase Auth
    await deleteUser(user);

    alert("Your account and profile have been permanently deleted.");
    window.location.href = "login.html";
  } catch (err) {
    if (err.code === "auth/requires-recent-login") {
      alert("For security, please log out, log back in, and click Delete Account again.");
    } else {
      alert("Error deleting account: " + err.message);
    }
  }
};

/* ================= CALENDAR & GROUPS SYSTEM ================= */
let calendarMonth = new Date();
let calendarSessions = [];
let selectedCalendarDate = new Date();
let activeGroupChat = null;

window.loadCalendarSessions = function () {
  const user = auth.currentUser;
  if (!user) return;
  onSnapshot(doc(db, "users", user.uid), (mySnap) => {
    const myData = mySnap.data() || {};
    const following = myData.following || [];
    const followers = myData.followers || [];
    
    onSnapshot(collection(db, "sessions"), async (snap) => {
      calendarSessions = [];
      const now = Date.now();

      for (const docSnap of snap.docs) {
        const d = docSnap.data();
        const id = docSnap.id;

        // Auto-end expired session in Firestore
        if (isSessionExpired(d)) {
          if (d.status !== "ended") {
            try {
              await updateDoc(doc(db, "sessions", id), {
                status: "ended",
                endTime: now,
                endTimeStr: new Date(now).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
                endDateStr: new Date(now).toLocaleDateString()
              });
            } catch (e) {}
          }
          continue;
        }

        // Host sees own, followers/following see active sessions
        const isRelated = d.hostId === user.uid || following.includes(d.hostId) || followers.includes(d.hostId);
        if (isRelated) {
          calendarSessions.push({ id, ...d });
        }
      }

      drawCalendar();
      updateCalendarSessionsList();
    });
  });
};

window.changeMonth = function (offset) {
  calendarMonth.setMonth(calendarMonth.getMonth() + offset);
  drawCalendar();
};

window.drawCalendar = function () {
  const grid = el("calendarDaysGrid");
  const label = el("calendarMonthYear");
  if (!grid || !label) return;
  
  label.innerText = calendarMonth.toLocaleDateString([], { month: "long", year: "numeric" });
  grid.innerHTML = "";
  
  const year = calendarMonth.getFullYear();
  const month = calendarMonth.getMonth();
  const firstDayIndex = new Date(year, month, 1).getDay();
  const totalDays = new Date(year, month + 1, 0).getDate();
  
  for (let i = 0; i < firstDayIndex; i++) {
    grid.innerHTML += `<div class="calendar-day-cell empty"></div>`;
  }
  
  const today = new Date();
  
  for (let day = 1; day <= totalDays; day++) {
    const currentDate = new Date(year, month, day);
    const isToday = currentDate.toDateString() === today.toDateString();
    const isSelected = currentDate.toDateString() === selectedCalendarDate.toDateString();
    
    const hasSession = calendarSessions.some(s => {
      const sessionDate = parseSessionDate(s.startTime);
      return sessionDate.toDateString() === currentDate.toDateString();
    });
    
    const cellClass = `calendar-day-cell${isToday ? ' today' : ''}${isSelected ? ' selected' : ''}${hasSession ? ' has-session' : ''}`;
    grid.innerHTML += `
      <div class="${cellClass}" onclick="selectCalendarDate(${day})">
        ${day}
      </div>`;
  }
};

window.selectCalendarDate = function (day) {
  selectedCalendarDate = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), day);
  drawCalendar();
  updateCalendarSessionsList();
};

window.deleteSession = async function (sessionId) {
  const user = auth.currentUser;
  if (!user) return;
  
  if (!confirm("Are you sure you want to delete this session?")) {
    return;
  }
  
  try {
    const docSnap = await getDoc(doc(db, "sessions", sessionId));
    if (docSnap.exists()) {
      const d = docSnap.data();
      if (d.hostId !== user.uid) {
        alert("Only the host can delete this session.");
        return;
      }
    }
    
    await deleteDoc(doc(db, "sessions", sessionId));
    alert("Session deleted successfully!");
    
    if (typeof loadCalendarSessions === "function") loadCalendarSessions();
    if (typeof loadHostSessionPanel === "function") loadHostSessionPanel();
    if (typeof loadLiveSessions === "function") loadLiveSessions();
  } catch (err) {
    alert("Error deleting session: " + err.message);
  }
};

window.updateCalendarSessionsList = function () {
  const container = el("calendarSessionsList");
  const header = el("selectedDateHeader");
  if (!container || !header) return;
  
  header.innerText = `Sessions on ${selectedCalendarDate.toLocaleDateString([], { month: "long", day: "numeric", year: "numeric" })}`;
  container.innerHTML = "";
  
  const filtered = calendarSessions.filter(s => {
    const sessionDate = parseSessionDate(s.startTime);
    return sessionDate.toDateString() === selectedCalendarDate.toDateString();
  });
  
  if (filtered.length === 0) {
    container.innerHTML = `<p style="color:#888; text-align:center; margin-top:40px;">No sessions scheduled for this day.</p>`;
    return;
  }
  
  filtered.forEach(s => {
    const isHost = s.hostId === auth.currentUser?.uid;
    const isExpired = isSessionExpired(s);
    const startObj = parseSessionDate(s.startTime);
    const endObj = s.endTime ? parseSessionDate(s.endTime) : new Date(startObj.getTime() + (s.durationMins || 60) * 60000);
    
    const startTimeAMPM = startObj.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    const endTimeAMPM = endObj.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    const displayTimeRange = s.timeRangeStr || `${startTimeAMPM} – ${endTimeAMPM}`;

    let badgeText = "";
    let badgeColor = "";

    if (isExpired || s.status === 'ended') {
      badgeText = '🏁 EXPIRED';
      badgeColor = '#e74c3c';
    } else if (s.status === 'live') {
      badgeText = '🔴 LIVE';
      badgeColor = '#27ae60';
    } else {
      badgeText = '📅 SCHEDULED';
      badgeColor = '#9b59b6';
    }
    
    let actionBtn = "";
    const deleteBtn = isHost ? `<button onclick="event.stopPropagation(); deleteSession('${s.id}')" style="background:#fee2e2; color:#ef4444; border:1px solid #fca5a5; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer; margin-top:4px;">🗑️ Delete</button>` : '';

    if (isExpired || s.status === 'ended') {
      actionBtn = `<span style="background:#fdecea; color:#c0392b; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; display:inline-block; margin-top:4px;"><i class="fas fa-circle-exclamation"></i> Session Expired</span>`;
    } else if (s.status === 'scheduled' && isHost) {
      actionBtn = `<button onclick="event.stopPropagation(); joinOrLaunchCalendarSession('${s.id}')" style="background:#3b82f6; color:#fff; border:none; padding:8px 14px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer; margin-top:4px;">🚀 Launch Session</button>`;
    } else if (s.status === 'scheduled' && !isHost) {
      actionBtn = `<button onclick="event.stopPropagation(); joinOrLaunchCalendarSession('${s.id}')" style="background:#27ae60; color:#fff; border:none; padding:8px 14px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer; margin-top:4px;">Join Session</button>`;
    } else if (s.status === 'live') {
      actionBtn = `<button onclick="event.stopPropagation(); joinOrLaunchCalendarSession('${s.id}')" style="background:#e74c3c; color:#fff; border:none; padding:8px 14px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer; margin-top:4px;">🔴 Enter Live Session</button>`;
    }
    
    const clickAttr = isExpired
      ? `onclick="alert('Session Expired: This session was scheduled for ${displayTimeRange} and is now ended.');"`
      : `onclick="joinOrLaunchCalendarSession('${s.id}')"`;

    container.innerHTML += `
      <div ${clickAttr} style="background:#f8f9fa; border-radius:10px; padding:12px; border:1px solid #e9ecef; display:flex; flex-direction:column; gap:6px; cursor:${isExpired ? 'default' : 'pointer'}; margin-bottom:10px; transition: transform 0.2s;" onmouseover="this.style.transform='translateY(-2px)'" onmouseout="this.style.transform='none'">
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <b style="font-size:14px; color:#2c3e50;">${s.name}</b>
          <span style="background:${badgeColor}; color:#fff; padding:2px 8px; border-radius:12px; font-size:10px; font-weight:700;">${badgeText}</span>
        </div>
        <div style="font-size:12px; color:#7f8c8d;">
          Topic: ${s.skill || "—"} | Host: ${s.hostName}<br>
          ⏰ Time: <b style="color:#2c3e50;">${displayTimeRange}</b> | Platform: ${s.platformLabel}<br>
          ${!isExpired && s.status === 'live' ? '<span style="color:#27ae60; font-weight:700;"><i class="fas fa-arrow-up-right-from-square"></i> Click to Open Meeting Link</span>' : ''}
          ${!isExpired && s.status === 'scheduled' ? '<span style="color:#3498db; font-weight:700;"><i class="fas fa-calendar-check"></i> Scheduled Session</span>' : ''}
          ${isExpired ? '<span style="color:#e74c3c; font-weight:700;"><i class="fas fa-ban"></i> Time limit over. Session Expired.</span>' : ''}
        </div>
        <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;">
          <div>${actionBtn}</div>
          ${deleteBtn}
        </div>
      </div>`;
  });
};



window.loadGroupChatMembers = async function () {
  const container = el("groupMembersList");
  if (!container) return;
  const user = auth.currentUser;
  if (!user) return;
  
  const mySnap = await getDoc(doc(db, "users", user.uid));
  const myData = mySnap.data();
  const followers = myData?.followers || [];
  
  getDocs(collection(db, "users")).then(snap => {
    container.innerHTML = "";
    snap.forEach(docSnap => {
      const userId = docSnap.id;
      if (userId === user.uid) return;
      if (!followers.includes(userId)) return;
      container.innerHTML += `
        <label style="display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; cursor:pointer;">
          <input type="checkbox" name="groupMember" value="${userId}" style="width:auto; margin:0;">
          <span>${docSnap.data().name}</span>
        </label>`;
    });
    if (container.innerHTML === "") {
      container.innerHTML = "<p style='color:#888;font-size:13px;text-align:center;'>No followers yet — people who follow you will appear here</p>";
    }
  });
};

window.createGroupChat = async function () {
  const nameInput = el("groupNameInput");
  if (!nameInput || !nameInput.value.trim()) return alert("Enter group name");
  const checkboxes = document.querySelectorAll('input[name="groupMember"]:checked');
  if (checkboxes.length === 0) return alert("Select at least one member to join the group");
  
  const members = [auth.currentUser.uid];
  checkboxes.forEach(cb => members.push(cb.value));
  
  const groupName = nameInput.value.trim();
  try {
    await addDoc(collection(db, "groups"), {
      name: groupName,
      adminId: auth.currentUser.uid,
      members: members,
      lastMessage: "Group created",
      lastMessageTime: Date.now(),
      createdAt: Date.now()
    });
    nameInput.value = "";
    checkboxes.forEach(cb => cb.checked = false);
    alert("Group Chat created!");
    loadGroupChats();
  } catch (err) { alert("Failed to create group: " + err.message); }
};

window.loadGroupChats = function () {
  const container = el("groupChatsContainer");
  if (!container) return;
  const user = auth.currentUser;
  if (!user) return;
  
  onSnapshot(collection(db, "groups"), (snap) => {
    container.innerHTML = "";
    let count = 0;
    snap.forEach(docSnap => {
      const d = docSnap.data();
      const id = docSnap.id;
      if (d.members && d.members.includes(user.uid)) {
        count++;
        container.innerHTML += `
          <div onclick="openGroupChat('${id}', '${d.name}')"
               style="display:flex; align-items:center; gap:12px; padding:12px; border-bottom:1px solid #eee; cursor:pointer; background:#fff; border-radius:10px; margin-bottom:8px; border:1px solid #ddd;">
            <div style="width:40px; height:40px; border-radius:50%; background:#8b5cf6; color:#fff; display:flex; align-items:center; justify-content:center; font-weight:bold; font-size:18px;">👥</div>
            <div>
              <b>${d.name}</b><br>
              <small style="color:#888;">${d.lastMessage}</small>
            </div>
          </div>`;
      }
    });
    if (count === 0) {
      container.innerHTML = `<p style="color:#888; text-align:center;">No active group chats</p>`;
    }
  });
};

window.openGroupChat = function (groupId, name) {
  activeGroupChat = groupId;
  activeChatUser = null;
  openChatScreen("chatMainScreen");
  if (el("chatTitle")) el("chatTitle").innerText = `Group: ${name}`;
  if (el("chatUserStatus")) el("chatUserStatus").style.display = "none";
  if (el("chatThemeSelect")) el("chatThemeSelect").value = "Default";
  changeChatTheme();
  
  loadGroupMessages(groupId);
};

window.loadGroupMessages = function (groupId) {
  const q = query(collection(db, "groups", groupId, "messages"), orderBy("time"));
  onSnapshot(q, (snapshot) => {
    const box = el("chatBox");
    if (!box) return;
    box.innerHTML = "";
    snapshot.forEach((docSnap) => {
      const d = docSnap.data();
      const msgId = docSnap.id;
      const time = d.time ? formatTime(d.time) : "";
      const isMine = d.sender === auth.currentUser.uid;
      
      let attachments = "";
      if (d.imageUrl) {
        attachments += `<img src="${d.imageUrl}" onclick="event.stopPropagation(); openImageModal('${d.imageUrl.replace(/'/g, "\\'")}')" style="max-width:100%; border-radius:8px; display:block; margin-top:5px; max-height:220px; object-fit:cover; cursor:pointer; border:1px solid rgba(0,0,0,0.1);" title="Click to view full photo">`;
      }
      if (d.pdfUrl) {
        attachments += `<a href="${d.pdfUrl}" target="_blank" style="display:flex; align-items:center; gap:8px; background:#f0f0f0; padding:8px 12px; border-radius:6px; text-decoration:none; color:#333; margin-top:5px; font-size:12px; font-weight:700;"><i class="fas fa-file-pdf" style="color:#ef4444; font-size:18px;"></i> View Document</a>`;
      }
      
      const deleteBtn = isMine ? `<div class="msg-actions" style="display:none; position:absolute; top:-32px; right:0; background:#fff; border-radius:8px; box-shadow:0 2px 10px rgba(0,0,0,0.2); overflow:hidden; z-index:99; white-space:nowrap;">
        <button onclick="event.stopPropagation(); deleteGroupMessage('${msgId}')" style="background:none; border:none; padding:6px 12px; color:#e74c3c; font-size:12px; font-weight:700; cursor:pointer;">🗑 Delete</button>
      </div>` : "";
      
      if (isMine) {
        box.innerHTML += `
          <div class="chat-msg-wrap" style="display:flex; justify-content:flex-end; margin:4px 10px; position:relative;" onclick="toggleMsgActions(this)">
            ${deleteBtn}
            <div style="background:#25D366; color:#fff; padding:8px 14px; border-radius:18px 18px 4px 18px; max-width:70%; word-wrap:break-word;">
              <div style="font-size:14px;">${d.text}</div>
              ${attachments}
              <div style="font-size:10px; color:rgba(255,255,255,0.8); text-align:right; margin-top:3px;">${time}</div>
            </div>
          </div>`;
      } else {
        box.innerHTML += `
          <div class="chat-msg-wrap" style="display:flex; justify-content:flex-start; margin:4px 10px; position:relative;">
            <div style="background:#fff; color:#000; padding:8px 14px; border-radius:18px 18px 18px 4px; max-width:70%; word-wrap:break-word; box-shadow:0 1px 3px rgba(0,0,0,0.1);">
              <small style="color:#8b5cf6; font-weight:bold; display:block; margin-bottom:4px;">${d.senderName}</small>
              <div style="font-size:14px;">${d.text}</div>
              ${attachments}
              <div style="font-size:10px; color:#999; text-align:right; margin-top:3px;">${time}</div>
            </div>
          </div>`;
      }
    });
    box.scrollTop = box.scrollHeight;
  });
};

window.loadHostSessionPanel = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const container = el("hostSessionContainer");
  if (!container) return;
  
  const liveSnap = await getDocs(
    query(collection(db, "sessions"), where("hostId", "==", user.uid), where("status", "==", "live"))
  );
  
  const schedSnap = await getDocs(
    query(collection(db, "sessions"), where("hostId", "==", user.uid), where("status", "==", "scheduled"))
  );
  
  let html = "";
  
  // 1. If user has a live session running
  if (!liveSnap.empty) {
    const d = liveSnap.docs[0].data();
    const id = liveSnap.docs[0].id;
    html += `
      <div style="background:#fff; border-radius:14px; padding:24px; border:1.5px solid #ffccd5; text-align:center; box-shadow:0 4px 12px rgba(0,0,0,0.02); margin-bottom:20px;">
        <div style="font-size:52px; margin-bottom:12px;">🎙</div>
        <h3 style="margin:0 0 6px; color:#2c3e50; font-size:18px;">You have a Live Session Running</h3>
        <div style="background:#fff8f8; border-radius:10px; padding:16px; text-align:left; margin-bottom:20px; border:1px solid #ffe3e3;">
          <p style="margin:4px 0; font-size:14px; color:#2c3e50;"><b>Name:</b> ${d.name}</p>
          <p style="margin:4px 0; font-size:14px; color:#2c3e50;"><b>Skill:</b> ${d.skill || "—"}</p>
          <p style="margin:4px 0; font-size:14px; color:#2c3e50;"><b>Platform:</b> ${d.platformLabel || "—"}</p>
          <p style="margin:4px 0; font-size:14px; color:#2c3e50;"><b>Secret Code:</b> <b style="font-size:18px; color:#c0392b;">${d.code}</b></p>
        </div>
        <button onclick="endSession('${id}')" style="background:#e74c3c; color:#fff; width:100%; padding:14px; font-size:15px; font-weight:700; border:none; border-radius:10px; cursor:pointer;">
          <i class="fas fa-circle-stop"></i> End Current Session
        </button>
      </div>`;
  }
  
  // 2. If user has scheduled sessions
  if (!schedSnap.empty) {
    html += `
      <div style="background:#fff; border-radius:14px; padding:20px; border:1.5px solid #e2e8f0; margin-bottom:20px;">
        <h3 style="margin:0 0 12px; color:#2c3e50; font-size:16px;"><i class="fas fa-calendar-alt" style="color:#3b82f6;"></i> Your Scheduled Sessions</h3>
        <div style="display:flex; flex-direction:column; gap:12px;">`;
    
    schedSnap.forEach(docSnap => {
      const s = docSnap.data();
      const id = docSnap.id;
      const timeStr = parseSessionDate(s.startTime).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
      const dateStr = parseSessionDate(s.startTime).toLocaleDateString([], { month: "short", day: "numeric", year: "numeric" });
      html += `
        <div style="background:#f8f9fa; border-radius:10px; padding:12px; border:1px solid #e9ecef; display:flex; justify-content:space-between; align-items:center;">
          <div>
            <b style="font-size:14px; color:#2c3e50;">${s.name}</b>
            <div style="font-size:12px; color:#7f8c8d; margin-top:2px;">
              Topic: ${s.skill || "—"} | Platform: ${s.platformLabel}<br>
              Scheduled: ${dateStr} at ${timeStr}
            </div>
          </div>
          <div style="display:flex; gap:8px; align-items:center;">
            <button onclick="joinOrLaunchCalendarSession('${id}')" 
                    style="background:#3b82f6; color:#fff; border:none; padding:8px 14px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">
              🚀 Launch
            </button>
            <button onclick="deleteSession('${id}')" 
                    style="background:#fee2e2; color:#ef4444; border:1px solid #fca5a5; padding:8px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">
              🗑️ Delete
            </button>
          </div>
        </div>`;
    });
    
    html += `</div></div>`;
  }
  
  // 3. Start Session Creation Form (only if no live session is running)
  if (liveSnap.empty) {
    html += `
      <div style="background:#f0f4ff;border-radius:14px;padding:18px;margin-bottom:18px;border:1.5px solid #c7d7ff;">
        <h3 style="margin:0 0 4px;color:#3a5bd9;font-size:15px;"><span style="background:#3a5bd9;color:#fff;border-radius:50%;width:22px;height:22px;display:inline-flex;align-items:center;justify-content:center;font-size:12px;margin-right:8px;">1</span>Choose a Platform & Get Meeting Link</h3>
        <p style="margin:4px 0 14px;color:#666;font-size:13px;">Create a meeting in Zoom or Google Meet → copy the invite link → paste in Step 2</p>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;">
          <a href="https://zoom.us/start/videomeeting" target="_blank" style="display:flex;flex-direction:column;align-items:center;gap:6px;padding:14px 10px;background:#fff;border:2px solid #2D8CFF;border-radius:12px;text-decoration:none;color:#2D8CFF;font-weight:700;font-size:13px;text-align:center;"><span style="font-size:28px;">🟦</span>Zoom</a>
          <a href="https://meet.google.com/new" target="_blank" style="display:flex;flex-direction:column;align-items:center;gap:6px;padding:14px 10px;background:#fff;border:2px solid #00897B;border-radius:12px;text-decoration:none;color:#00897B;font-weight:700;font-size:13px;text-align:center;"><span style="font-size:28px;">🟢</span>Google Meet</a>
        </div>
      </div>
      <div style="background:#f8fff8;border-radius:14px;padding:18px;border:1.5px solid #b2dfdb;">
        <h3 style="margin:0 0 14px;color:#27ae60;font-size:15px;"><span style="background:#27ae60;color:#fff;border-radius:50%;width:22px;height:22px;display:inline-flex;align-items:center;justify-content:center;font-size:12px;margin-right:8px;">2</span>Fill Session Details</h3>
        <input type="text" id="sessionName" placeholder="Session Name (e.g. Python Basics)" style="margin-bottom:10px;">
        <input type="text" id="sessionSkill" placeholder="Skill Topic (e.g. Python, React)" style="margin-bottom:10px;">
        <label style="font-size:13px;color:#555;font-weight:600;display:block;margin-bottom:6px;">Select Platform:</label>
        <select id="sessionPlatform" style="margin-bottom:10px;width:100%;padding:10px;border-radius:8px;border:1.5px solid #ddd;">
          <option value="">-- Select Platform --</option>
          <option value="zoom">Zoom</option>
          <option value="meet">Google Meet</option>
        </select>
        <input type="url" id="sessionLink" placeholder="Paste Meeting Link here (https://...)" style="margin-bottom:14px;">
        
        <!-- Schedule Toggle -->
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; padding:8px 0; border-bottom:1px solid #eee;">
          <span style="font-size:14px; font-weight:600; color:#555;"><i class="fas fa-calendar-alt"></i> Schedule for Future Date</span>
          <label class="switch-container" style="position:relative; display:inline-block; width:50px; height:26px;">
            <input type="checkbox" id="sessionScheduleToggle" onchange="toggleScheduleContainer()" style="opacity:0; width:0; height:0;">
            <span class="slider" style="position:absolute; cursor:pointer; top:0; left:0; right:0; bottom:0; background-color:#ccc; transition:.4s; border-radius:34px;"></span>
          </label>
        </div>
        
        <!-- Schedule Fields -->
        <div id="scheduleFieldsContainer" style="display:none; margin-bottom:14px; background:#f5f6fa; padding:12px; border-radius:10px; border:1px solid #e2e8f0;">
          <div style="margin-bottom:8px;">
            <label style="font-size:12px; font-weight:600; color:#666; display:block; margin-bottom:4px;">Date:</label>
            <input type="date" id="sessionScheduleDate" style="width:100%; padding:8px; border-radius:6px; border:1px solid #ccc; box-sizing:border-box;">
          </div>
          <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px;">
            <div>
              <label style="font-size:12px; font-weight:600; color:#666; display:block; margin-bottom:4px;">Start Time (AM/PM):</label>
              <input type="time" id="sessionScheduleTime" style="width:100%; padding:8px; border-radius:6px; border:1px solid #ccc; box-sizing:border-box;">
            </div>
            <div>
              <label style="font-size:12px; font-weight:600; color:#666; display:block; margin-bottom:4px;">End Time (AM/PM):</label>
              <input type="time" id="sessionScheduleEndTime" style="width:100%; padding:8px; border-radius:6px; border:1px solid #ccc; box-sizing:border-box;">
            </div>
          </div>
        </div>
        
        <button id="btnStartSubmit" onclick="startSession()" style="background:linear-gradient(135deg,#27ae60,#2ecc71);color:#fff;width:100%;padding:14px;font-size:15px;font-weight:700;border:none;border-radius:10px;cursor:pointer;">
          <i class="fas fa-circle-play"></i> Start Session & Get Code
        </button>
      </div>
      <div id="sessionCodeBox" style="display:none;margin-top:20px;background:linear-gradient(135deg,#e8f5e9,#f0fff4);border:2px dashed #27ae60;border-radius:14px;padding:24px;text-align:center;">
        <p style="margin:0 0 6px;color:#27ae60;font-weight:700;font-size:14px;">🔐 Your Secret Session Code</p>
        <h1 id="generatedCode" style="margin:0;font-size:52px;letter-spacing:14px;color:#2c3e50;font-weight:800;"></h1>
        <p style="margin:10px 0 4px;color:#555;font-size:13px;">Session is LIVE 🟢</p>
        <button id="copyCodeBtn" onclick="copySessionCode()" style="margin-top:14px;background:#2c3e50;color:#fff;border:none;padding:10px 24px;border-radius:8px;cursor:pointer;font-size:13px;font-weight:600;">📋 Copy Code</button>
      </div>`;
  }
  
  container.innerHTML = html;
};

window.joinOrLaunchCalendarSession = async function (sessionId) {
  const user = auth.currentUser;
  if (!user) return;
  try {
    const docSnap = await getDoc(doc(db, "sessions", sessionId));
    if (!docSnap.exists()) { alert("Session not found."); return; }
    const d = docSnap.data();
    
    if (isSessionExpired(d)) {
      alert("Session time completed.");
      await updateDoc(doc(db, "sessions", sessionId), { status: "ended", endTime: Date.now() });
      loadCalendarSessions();
      loadHostSessionPanel();
      return;
    }
    
    const startTimeMs = getSessionTimeMs(d.startTime);
    
    if (d.status === "scheduled" && Date.now() < startTimeMs) {
      alert("Session not started yet. Please wait until the scheduled time.");
      return;
    }
    
    if (d.hostId === user.uid) {
      if (d.status === "scheduled") {
        await updateDoc(doc(db, "sessions", sessionId), { status: "live", startTime: Date.now() });
      }
      openMeetingLink(d.meetingLink, d.platform, null, sessionId, d);
    } else {
      await updateDoc(doc(db, "sessions", sessionId), { participants: arrayUnion(user.uid) });
      openMeetingLink(d.meetingLink, d.platform, null, sessionId, d);
    }
    loadCalendarSessions();
    loadHostSessionPanel();
  } catch (err) {
    alert("Error: " + err.message);
  }
};

// Trigger Selenium test suite run - July 23