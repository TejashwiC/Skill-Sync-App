import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import {
  getAuth,
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  onAuthStateChanged,
  signOut
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
    await signInWithEmailAndPassword(auth, el("email").value, el("password").value);
    window.location.href = "dashboard.html";
  } catch (err) { alert("Login Error: " + err.message); }
};

/* ================ AUTH STATE ================ */
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

/* ================ LOGOUT ================ */
window.logout = async function () {
  await signOut(auth);
  window.location.href = "login.html";
};

/* ================ NAV ================ */
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
  alert("Followed successfully!");
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
  onSnapshot(collection(db, "users"), (snap) => {
    container.innerHTML = "";
    snap.forEach((docSnap) => {
      const userId = docSnap.id;
      const data   = docSnap.data();
      if (!auth.currentUser || userId === auth.currentUser.uid) return;
      container.innerHTML += `
        <div class="user-box">
          <b>${data.name || "No Name"}</b><br>
          <small>${data.email || ""}</small><br>
          <button onclick="followUser('${userId}')">Follow</button>
          <button onclick="viewUserProfile('${userId}')">View Profile</button>
        </div>`;
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
      container.innerHTML += `<div class="user-box">${u.data()?.name || "User"}</div>`;
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
      container.innerHTML += `<div class="user-box">${u.data()?.name || "User"}</div>`;
    }
    if (!data.following?.length) container.innerHTML = `<p style="color:#888;text-align:center;">Not following anyone yet</p>`;
  });
};

window.loadSuggestedUsers = function () {
  const container = el("suggestedList");
  if (!container) return;
  onSnapshot(collection(db, "users"), (snap) => {
    container.innerHTML = "";
    snap.forEach((docSnap) => {
      const userId = docSnap.id;
      const data   = docSnap.data();
      if (!auth.currentUser || userId === auth.currentUser.uid) return;
      container.innerHTML += `
        <div class="user-box">
          <b>${data.name || "User"}</b><br>
          <small>${data.email || ""}</small><br>
          <button onclick="followUser('${userId}')">Follow</button>
        </div>`;
    });
  });
};

window.searchUsers = function () {
  const query_text = el("searchInput")?.value?.toLowerCase() || "";
  const container  = el("searchResults");
  if (!container || !query_text) { container.innerHTML = ""; return; }
  getDocs(collection(db, "users")).then(snap => {
    container.innerHTML = "";
    snap.forEach(docSnap => {
      const d = docSnap.data();
      if (docSnap.id === auth.currentUser?.uid) return;
      if ((d.name || "").toLowerCase().includes(query_text) || (d.email || "").toLowerCase().includes(query_text)) {
        container.innerHTML += `
          <div class="user-box">
            <b>${d.name}</b><br><small>${d.email}</small><br>
            <button onclick="followUser('${docSnap.id}')">Follow</button>
            <button onclick="viewUserProfile('${docSnap.id}')">View</button>
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
  if (el("u_mobile"))  el("u_mobile").innerText  = d.mobile   || "";
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
  onSnapshot(collection(db, "users"), (snapshot) => {
    container.innerHTML = "";
    snapshot.forEach((docSnap) => {
      const data   = docSnap.data();
      const userId = docSnap.id;
      if (!auth.currentUser || userId === auth.currentUser.uid) return;
      container.innerHTML += `
        <div class="chat-user" onclick="openChat('${userId}', '${data.name}')"
             style="display:flex;align-items:center;gap:12px;padding:12px;border-bottom:1px solid #eee;cursor:pointer;">
          <img src="${data.photo && data.photo.length > 10 ? data.photo : 'https://ui-avatars.com/api/?name=' + encodeURIComponent(data.name || 'User')}"
               style="width:45px;height:45px;border-radius:50%;object-fit:cover;">
          <div><b>${data.name || "No Name"}</b><br><small style="color:#888;">${data.email || ""}</small></div>
        </div>`;
    });
  });
};

window.loadChatUserList = function () {
  const container = el("chatUserList");
  if (!container) return;
  onSnapshot(collection(db, "users"), (snapshot) => {
    container.innerHTML = "<p style='padding:10px;color:#888;font-size:13px;'>👇 Select a user to chat</p>";
    snapshot.forEach((docSnap) => {
      const data   = docSnap.data();
      const userId = docSnap.id;
      if (!auth.currentUser || userId === auth.currentUser.uid) return;
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

window.selectChatUser = function (userId, name) {
  activeChatUser = userId;
  activeChatName = name;
  if (el("chatTitle")) el("chatTitle").innerText = name;
  loadMessages();
};

window.openChat = function (userId, name) {
  activeChatUser = userId;
  activeChatName = name;
  openChatScreen("chatMainScreen");
  if (el("chatTitle")) el("chatTitle").innerText = name;
  loadMessages();
};

window.sendMessage = async function () {
  const msgInput = el("messageInput");
  if (!msgInput) return;
  const msg = msgInput.value.trim();
  if (!msg) return;
  if (!activeChatUser) { alert("Select a user first!"); return; }
  const user = auth.currentUser;
  if (!user) { alert("Not logged in!"); return; }
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
      const time = d.time ? formatTime(d.time) : "";
      if (d.sender === user.uid) {
        box.innerHTML += `
          <div style="display:flex;justify-content:flex-end;margin:4px 10px;">
            <div style="background:#25D366;color:#fff;padding:8px 14px;border-radius:18px 18px 4px 18px;max-width:70%;word-wrap:break-word;">
              <div style="font-size:14px;">${d.text}</div>
              <div style="font-size:10px;color:rgba(255,255,255,0.8);text-align:right;margin-top:3px;">${time} ✓✓</div>
            </div>
          </div>`;
      } else {
        box.innerHTML += `
          <div style="display:flex;justify-content:flex-start;margin:4px 10px;">
            <div style="background:#fff;color:#000;padding:8px 14px;border-radius:18px 18px 18px 4px;max-width:70%;word-wrap:break-word;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
              <div style="font-size:14px;">${d.text}</div>
              <div style="font-size:10px;color:#999;text-align:right;margin-top:3px;">${time}</div>
            </div>
          </div>`;
      }
    });
    box.scrollTop = box.scrollHeight;
  });
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
  onSnapshot(collection(db, "users"), async (snapshot) => {
    const mySnap  = await getDoc(doc(db, "users", user.uid));
    const blocked = mySnap.data()?.blocked || [];
    if (deleteContainer) deleteContainer.innerHTML = "";
    if (blockContainer)  blockContainer.innerHTML  = "";
    snapshot.forEach(docSnap => {
      const data = docSnap.data();
      const uid  = docSnap.id;
      if (uid === user.uid) return;
      if (deleteContainer) {
        deleteContainer.innerHTML += `
          <div style="display:flex;justify-content:space-between;align-items:center;padding:10px;border-bottom:1px solid #eee;">
            <span><b>${data.name || "User"}</b></span>
            <button onclick="deleteChatWith('${uid}')"
                    style="background:#e74c3c;color:#fff;border:none;padding:6px 14px;border-radius:8px;cursor:pointer;font-size:13px;">🗑 Delete</button>
          </div>`;
      }
      if (blockContainer) {
        const isBlocked = blocked.includes(uid);
        blockContainer.innerHTML += `
          <div style="display:flex;justify-content:space-between;align-items:center;padding:10px;border-bottom:1px solid #eee;">
            <span><b>${data.name || "User"}</b></span>
            ${isBlocked
              ? `<button onclick="unblockUser('${uid}')" style="background:#27ae60;color:#fff;border:none;padding:6px 14px;border-radius:8px;cursor:pointer;font-size:13px;">✅ Unblock</button>`
              : `<button onclick="blockUser('${uid}')"   style="background:#e74c3c;color:#fff;border:none;padding:6px 14px;border-radius:8px;cursor:pointer;font-size:13px;">🚫 Block</button>`
            }
          </div>`;
      }
    });
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
  meet:    "Google Meet",
  teams:   "Microsoft Teams",
  webex:   "Webex",
  jitsi:   "Jitsi",
  whereby: "Whereby",
  other:   "Other"
};

function generateCode() {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  let code = "";
  for (let i = 0; i < 6; i++) code += chars[Math.floor(Math.random() * chars.length)];
  return code;
}

function formatDuration(startTime) {
  const mins = Math.floor((Date.now() - startTime) / 60000);
  if (mins < 60) return mins + " min";
  return Math.floor(mins / 60) + "h " + (mins % 60) + "m";
}

function openMeetingLink(link, platform) {
  if (!link) { alert("No meeting link available."); return; }
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

window.startSession = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const name     = el("sessionName")?.value?.trim();
  const skill    = el("sessionSkill")?.value?.trim();
  const platform = el("sessionPlatform")?.value;
  const link     = el("sessionLink")?.value?.trim();
  if (!name)     { alert("Enter session name"); return; }
  if (!platform) { alert("Select a platform"); return; }
  if (!link)     { alert("Paste the meeting link"); return; }
  if (!link.startsWith("http")) { alert("Meeting link must start with http:// or https://"); return; }
  const existing = await getDocs(
    query(collection(db, "sessions"), where("hostId", "==", user.uid), where("status", "==", "live"))
  );
  if (!existing.empty) { alert("You already have a live session! End it first."); return; }
  const snap     = await getDoc(doc(db, "users", user.uid));
  const hostName = snap.data()?.name || "Host";
  const code     = generateCode();
  const now      = new Date();
  await addDoc(collection(db, "sessions"), {
    hostId: user.uid, hostName, name, skill: skill || "", platform,
    platformLabel: PLATFORM_LABELS[platform] || platform,
    meetingLink: link, code, status: "live", startTime: Date.now(),
    startTimeStr: now.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
    startDateStr: now.toLocaleDateString(),
    participants: [], feedback: [], ratings: []
  });
  await updateDoc(doc(db, "users", user.uid), { sessions: (snap.data()?.sessions || 0) + 1 });
  if (el("generatedCode"))  el("generatedCode").innerText      = code;
  if (el("sessionCodeBox")) el("sessionCodeBox").style.display = "block";
  loadProfile();
};

window.endSession = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const snap = await getDocs(
    query(collection(db, "sessions"), where("hostId", "==", user.uid), where("status", "==", "live"))
  );
  if (snap.empty) { alert("No active session found."); return; }
  const sessionDoc   = snap.docs[0];
  const d            = sessionDoc.data();
  const now          = new Date();
  const durationMins = Math.floor((Date.now() - d.startTime) / 60000);
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
    query(collection(db, "sessions"), where("hostId", "==", user.uid), where("status", "==", "live")),
    snap => {
      container.innerHTML = "";
      if (snap.empty) {
        container.innerHTML = `<p style="color:#888;text-align:center;padding:20px;">No active session right now.</p>`;
        return;
      }
      const d = snap.docs[0].data();
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
    query(collection(db, "sessions"), where("code", "==", code), where("status", "==", "live"))
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
  setTimeout(() => openMeetingLink(d.meetingLink, d.platform), 1000);
  if (codeInput) codeInput.value = "";
};

window.loadLiveSessions = function () {
  const container = el("liveSessionContainer");
  if (!container) return;
  onSnapshot(query(collection(db, "sessions"), where("status", "==", "live")), snap => {
    container.innerHTML = "";
    if (snap.empty) {
      container.innerHTML = `<div style="text-align:center;padding:50px 20px;color:#888;"><div style="font-size:48px;margin-bottom:12px;">🎙</div><p style="font-size:16px;font-weight:600;margin:0 0 6px;">No live sessions right now</p></div>`;
      return;
    }
    container.innerHTML = `
      <div style="background:#fff8e1;border:1.5px solid #f39c12;border-radius:10px;padding:12px 16px;margin-bottom:16px;display:flex;align-items:flex-start;gap:10px;">
        <span style="font-size:20px;margin-top:1px;">🔐</span>
        <div>
          <p style="margin:0 0 2px;font-weight:700;color:#7d5a00;font-size:13px;">Private Sessions</p>
          <p style="margin:0;font-size:12px;color:#7d5a00;line-height:1.5;">You need a secret 6-digit code from the host to join.<br>Ask the host to send you the code via <b>Chat</b>, then go to <b>Join Session</b>.</p>
        </div>
      </div>`;
    const platformIcons = { zoom:"🟦", meet:"🟢", teams:"🟣", webex:"🔵", jitsi:"🟩", whereby:"🟧", other:"📹" };
    snap.forEach(docSnap => {
      const d      = docSnap.data();
      const user   = auth.currentUser;
      const isHost = user && d.hostId === user.uid;
      const icon   = platformIcons[d.platform] || "📹";
      container.innerHTML += `
        <div class="card" style="border-left:4px solid ${isHost ? '#3498db' : '#e74c3c'};margin-bottom:14px;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px;">
            <h3 style="margin:0;font-size:16px;">${d.name}</h3>
            ${isHost ? `<span style="background:#3498db;color:#fff;padding:3px 10px;border-radius:20px;font-size:11px;font-weight:700;">👑 YOUR SESSION</span>`
                     : `<span style="background:#e74c3c;color:#fff;padding:3px 10px;border-radius:20px;font-size:11px;font-weight:700;">🔴 LIVE</span>`}
          </div>
          <p style="margin:5px 0;color:#555;font-size:13px;">🧑‍🏫 Host: <b>${d.hostName}</b></p>
          <p style="margin:5px 0;color:#555;font-size:13px;">📚 Skill: ${d.skill || "—"}</p>
          <p style="margin:5px 0;color:#555;font-size:13px;">${icon} Platform: ${d.platformLabel || "—"}</p>
          <p style="margin:5px 0;color:#555;font-size:13px;">🕐 Started: ${d.startTimeStr} · ${d.startDateStr}</p>
          <p style="margin:5px 0;color:#555;font-size:13px;">⏱ Running: ${formatDuration(d.startTime)}</p>
          <p style="margin:5px 0;color:#555;font-size:13px;">👥 Participants: ${d.participants?.length || 0}</p>
          ${isHost
            ? `<div style="margin-top:14px;background:#eaf4fb;border-radius:10px;padding:14px;text-align:center;">
                 <p style="margin:0 0 4px;font-size:12px;color:#3498db;font-weight:700;letter-spacing:1px;">YOUR SECRET CODE</p>
                 <p style="margin:0;font-size:32px;font-weight:800;letter-spacing:10px;color:#2c3e50;">${d.code}</p>
                 <p style="margin:8px 0 10px;font-size:12px;color:#888;">Send this to your student via Chat</p>
                 <button onclick="openMeetingAsHost('${docSnap.id}')" style="background:#3498db;color:#fff;border:none;padding:10px 20px;border-radius:8px;cursor:pointer;font-size:13px;font-weight:700;width:100%;">🎥 Rejoin / Open Meeting</button>
               </div>`
            : `<div style="margin-top:14px;background:#f8f9fa;border-radius:10px;padding:14px;text-align:center;border:1.5px dashed #ddd;">
                 <p style="margin:0 0 4px;font-size:22px;">🔒</p>
                 <p style="margin:0;font-size:13px;color:#555;line-height:1.5;"><b>Private Session</b><br>Ask <b>${d.hostName}</b> for the code via Chat<br>then use <b>Join Session</b></p>
               </div>`
          }
        </div>`;
    });
  });
};

window.openMeetingAsHost = async function (sessionId) {
  const snap = await getDoc(doc(db, "sessions", sessionId));
  if (!snap.exists()) { alert("Session not found."); return; }
  const d = snap.data();
  openMeetingLink(d.meetingLink, d.platform);
};

window.loadSessionHistory = function () {
  const container = el("historyContainer");
  if (!container) return;
  onSnapshot(query(collection(db, "sessions"), where("status", "==", "ended")), snap => {
    container.innerHTML = "";
    if (snap.empty) {
      container.innerHTML = `<div style="text-align:center;padding:50px 20px;color:#888;"><div style="font-size:40px;">📋</div><p>No past sessions yet</p></div>`;
      return;
    }
    const docs = snap.docs.sort((a, b) => (b.data().endTime || 0) - (a.data().endTime || 0));
    docs.forEach(docSnap => {
      const d = docSnap.data();
      const avgRating = d.ratings?.length ? (d.ratings.reduce((a, b) => a + b.stars, 0) / d.ratings.length).toFixed(1) : null;
      const stars = avgRating ? "⭐".repeat(Math.round(avgRating)) + ` <b>${avgRating}/5</b>` : `<span style="color:#bbb;">No ratings yet</span>`;
      container.innerHTML += `
        <div class="card" style="border-left:4px solid #3498db;margin-bottom:14px;">
          <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px;">
            <h3 style="margin:0;">${d.name}</h3>
            <span style="background:#eaf4fb;color:#3498db;padding:3px 10px;border-radius:20px;font-size:12px;font-weight:600;">ENDED</span>
          </div>
          <p style="margin:5px 0;color:#555;">Host: <b>${d.hostName}</b></p>
          <p style="margin:5px 0;color:#555;">Skill: ${d.skill || "—"}</p>
          <p style="margin:5px 0;color:#555;">Platform: ${d.platformLabel || "—"}</p>
          <p style="margin:5px 0;color:#555;">${d.startDateStr} · ${d.startTimeStr} → ${d.endTimeStr || "—"}</p>
          <p style="margin:5px 0;color:#555;">Duration: ${d.durationMins != null ? d.durationMins + " min" : "—"}</p>
          <p style="margin:5px 0;color:#555;">Participants: ${d.participants?.length || 0}</p>
          <p style="margin:5px 0;">Rating: ${stars}</p>
        </div>`;
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

window.submitFeedback = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const sessionId = el("feedbackSessionSelect")?.value;
  const text      = el("feedbackText")?.value?.trim();
  if (!sessionId) { alert("Select a session"); return; }
  if (!text)      { alert("Write your feedback"); return; }
  const snap = await getDoc(doc(db, "users", user.uid));
  const name = snap.data()?.name || "User";
  await updateDoc(doc(db, "sessions", sessionId), {
    feedback: arrayUnion({ userId: user.uid, name, text, time: Date.now() })
  });
  alert("Feedback submitted!");
  if (el("feedbackText")) el("feedbackText").value = "";
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
    snap.forEach(docSnap => {
      const d = docSnap.data();
      if (!d.ratings?.length) return;
      const avg = (d.ratings.reduce((a, b) => a + b.stars, 0) / d.ratings.length).toFixed(1);
      let ratingsHtml = d.ratings.map(r =>
        `<div style="display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid #f0f0f0;">
          <span>${r.name}</span>
          <span style="color:#f39c12;">${"★".repeat(r.stars)}${"☆".repeat(5 - r.stars)}</span>
        </div>`
      ).join("");
      container.innerHTML += `
        <div class="card" style="margin-bottom:12px;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
            <h3 style="margin:0;">${d.name}</h3>
            <span style="font-size:18px;color:#f39c12;font-weight:700;">⭐ ${avg}/5</span>
          </div>
          <p style="margin:0 0 8px;color:#888;font-size:13px;">By ${d.hostName} · ${d.ratings.length} rating(s)</p>
          ${ratingsHtml}
        </div>`;
    });
    if (!container.innerHTML) container.innerHTML = `<div style="text-align:center;padding:30px;color:#888;">No ratings yet</div>`;
  });
};

/* ================ PDF ================ */
window.loadPDFs = function () {
  const pdfList = el("pdfList");
  if (!pdfList) return;
  onSnapshot(collection(db, "pdfs"), (snapshot) => {
    pdfList.innerHTML = "";
    snapshot.forEach((docSnap) => {
      const data = docSnap.data();
      pdfList.innerHTML += `
        <div class="user-box">
          <b>${data.fileName}</b><br>
          <small>By: ${data.uploadedBy}</small><br><br>
          <a href="${data.fileURL}" target="_blank">👁 View</a><br>
          <a href="${data.fileURL}" download>📥 Download</a>
        </div>`;
    });
    if (!pdfList.innerHTML) pdfList.innerHTML = `<p style="color:#888;text-align:center;">No PDFs uploaded yet</p>`;
  });
};

/* ================================================
   NOTIFICATION SETTINGS
   — Saves each toggle to Firestore manually on click
   — Loads current status when screen opens
   — Buttons visually reflect active/inactive state
   ================================================ */

// Call this when Notification Settings screen opens to load saved values
window.loadNotificationSettings = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const snap = await getDoc(doc(db, "users", user.uid));
  const d    = snap.data() || {};

  // follow
  const followOn  = d.followNotifications !== false;
  updateNotifUI("follow", followOn);

  // sound
  const soundOn   = d.soundNotifications !== false;
  updateNotifUI("sound", soundOn);

  // email
  const emailOn   = d.emailNotifications !== false;
  updateNotifUI("email", emailOn);

  // session
  const sessionOn = d.sessionAlerts !== false;
  updateNotifUI("session", sessionOn);
};

// Updates button styles and status text
function updateNotifUI(type, isOn) {
  const statusMap = {
    follow:  { status: "followNotifStatus",  on: "followNotifON",  off: "followNotifOFF"  },
    sound:   { status: "soundStatus",         on: "soundON",         off: "soundOFF"         },
    email:   { status: "emailStatus",         on: "emailON",         off: "emailOFF"         },
    session: { status: "sessionStatus",       on: "sessionNotifON",  off: "sessionNotifOFF"  }
  };
  const ids = statusMap[type];
  if (!ids) return;
  if (el(ids.status)) el(ids.status).innerText = isOn ? "ON" : "OFF";
  if (el(ids.on))  el(ids.on).style.background  = isOn ? "#25D366" : "#ccc";
  if (el(ids.off)) el(ids.off).style.background = isOn ? "#ccc"    : "#e74c3c";
}

window.setNotification = async function (type, status) {
  const user = auth.currentUser;
  if (!user) return;
  const ref = doc(db, "users", user.uid);

  const fieldMap = {
    follow:  "followNotifications",
    sound:   "soundNotifications",
    email:   "emailNotifications",
    session: "sessionAlerts"
  };
  const field = fieldMap[type];
  if (!field) return;

  await updateDoc(ref, { [field]: status });
  updateNotifUI(type, status);
  alert(`${type.charAt(0).toUpperCase() + type.slice(1)} notifications turned ${status ? "ON" : "OFF"}`);
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

window.createTest = async function () {
  const user = auth.currentUser;
  if (!user) return;
  const title   = el("testTitle")?.value?.trim();
  const skill   = el("testSkill")?.value?.trim();
  const credits = el("testCredits")?.value?.trim();
  if (!title)   { alert("Enter test title"); return; }
  if (!skill)   { alert("Enter skill name"); return; }
  if (!credits) { alert("Enter credits"); return; }
  const snap        = await getDoc(doc(db, "users", user.uid));
  const creatorName = snap.data()?.name || "Unknown";
  await addDoc(collection(db, "tests"), {
    creatorId: user.uid, creatorName, title, skill,
    credits: Number(credits), createdAt: serverTimestamp()
  });
  alert("✅ Test Created!");
  if (el("testTitle"))   el("testTitle").value   = "";
  if (el("testSkill"))   el("testSkill").value   = "";
  if (el("testCredits")) el("testCredits").value = "";
  loadMyTests();
};

window.loadMyTests = function () {
  const user = auth.currentUser;
  if (!user) return;
  const testList   = el("myTestsList") || el("testList");
  const testSelect = el("testSelect");
  onSnapshot(
    query(collection(db, "tests"), where("creatorId", "==", user.uid)),
    snap => {
      if (testList)   testList.innerHTML   = "";
      if (testSelect) testSelect.innerHTML = "<option value=''>-- Select Test --</option>";
      if (snap.empty) {
        if (testList) testList.innerHTML = `<p style="color:#888;text-align:center;padding:20px;">No tests created yet</p>`;
        return;
      }
      snap.forEach(docSnap => {
        const d  = docSnap.data();
        const id = docSnap.id;
        if (testList) {
          testList.innerHTML += `
            <div class="card" style="border-left:4px solid #3498db;margin-bottom:12px;">
              <div style="display:flex;justify-content:space-between;align-items:center;">
                <div>
                  <h3 style="margin:0 0 4px;">${d.title}</h3>
                  <p style="margin:2px 0;color:#555;font-size:13px;">📚 Skill: ${d.skill}</p>
                  <p style="margin:2px 0;color:#27ae60;font-size:13px;">🏆 Credits: ${d.credits}</p>
                </div>
                <button onclick="deleteTest('${id}')"
                        style="background:#e74c3c;color:#fff;border:none;padding:8px 14px;border-radius:8px;cursor:pointer;font-size:13px;font-weight:600;">
                  🗑 Delete
                </button>
              </div>
            </div>`;
        }
        if (testSelect) {
          testSelect.innerHTML += `<option value="${id}">${d.title}</option>`;
        }
      });
    }
  );
};

window.deleteTest = async function (testId) {
  if (!confirm("Delete this test and ALL its questions?")) return;
  const qSnap = await getDocs(query(collection(db, "questions"), where("testId", "==", testId)));
  await Promise.all(qSnap.docs.map(d => deleteDoc(d.ref)));
  await deleteDoc(doc(db, "tests", testId));
  alert("✅ Test deleted!");
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
  if (!question)      { alert("Enter a question"); return; }
  if (!option1 || !option2 || !option3 || !option4) { alert("Fill all 4 options"); return; }
  if (!correctAnswer) { alert("Enter the correct answer"); return; }
  await addDoc(collection(db, "questions"), {
    testId, creatorId: user.uid,
    question, option1, option2, option3, option4,
    correctAnswer, createdAt: serverTimestamp()
  });
  alert("✅ Question Added!");
  ["question","option1","option2","option3","option4","correctAnswer"]
    .forEach(id => { if (el(id)) el(id).value = ""; });
  loadMyQuestions();
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
                      style="background:#e74c3c;color:#fff;border:none;padding:6px 12px;border-radius:8px;cursor:pointer;font-size:12px;">
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
  container.innerHTML = `<div style="text-align:center;padding:30px;color:#888;"><p>Loading tests...</p></div>`;
  const mySnap    = await getDoc(doc(db, "users", user.uid));
  const myData    = mySnap.data();
  const following = myData?.following || [];
  const followers = myData?.followers || [];
  const mutualConnections = following.filter(uid => followers.includes(uid));
  if (!mutualConnections.length) {
    container.innerHTML = `
      <div style="text-align:center;padding:40px 20px;color:#888;">
        <div style="font-size:48px;margin-bottom:12px;">🤝</div>
        <p style="font-size:16px;font-weight:600;margin:0 0 6px;color:#555;">No mutual connections yet</p>
        <p style="font-size:13px;margin:0;">Follow someone and have them follow you back to see their tests here.</p>
      </div>`;
    return;
  }
  const chunks = [];
  for (let i = 0; i < mutualConnections.length; i += 10) chunks.push(mutualConnections.slice(i, i + 10));
  let allTests = [];
  for (const chunk of chunks) {
    const snap = await getDocs(query(collection(db, "tests"), where("creatorId", "in", chunk)));
    snap.forEach(docSnap => allTests.push({ id: docSnap.id, ...docSnap.data() }));
  }
  container.innerHTML = "";
  if (!allTests.length) {
    container.innerHTML = `
      <div style="text-align:center;padding:40px 20px;color:#888;">
        <div style="font-size:48px;margin-bottom:12px;">📝</div>
        <p style="font-size:15px;font-weight:600;margin:0 0 6px;color:#555;">No tests available yet</p>
        <p style="font-size:13px;margin:0;">Your connections haven't created any tests yet.</p>
      </div>`;
    return;
  }
  allTests.forEach(test => {
    container.innerHTML += `
      <div class="card" style="border-left:4px solid #27ae60;margin-bottom:14px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
          <h3 style="margin:0;">${test.title}</h3>
          <span style="background:#e8f5e9;color:#27ae60;padding:3px 10px;border-radius:20px;font-size:12px;font-weight:600;">📝 TEST</span>
        </div>
        <p style="margin:4px 0;color:#555;font-size:13px;">📚 Skill: <b>${test.skill}</b></p>
        <p style="margin:4px 0;color:#555;font-size:13px;">👤 By: <b>${test.creatorName || "Unknown"}</b></p>
        <p style="margin:4px 0;color:#27ae60;font-weight:600;font-size:13px;">🏆 Earn ${test.credits} Credits on completion</p>
        <button onclick="startTest('${test.id}')"
                style="margin-top:12px;background:#3498db;color:#fff;border:none;padding:12px 20px;border-radius:8px;cursor:pointer;font-size:14px;font-weight:600;width:100%;">
          🚀 Start Test
        </button>
      </div>`;
  });
};

window.startTest = async function (testId) {
  const user = auth.currentUser;
  if (!user) return;
  currentTestId   = testId;
  userAnswers     = {};

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
      <h2 style="margin:0 0 4px;">${currentTestData?.title || "Test"}</h2>
      <p style="margin:0;opacity:0.85;font-size:13px;">${qSnap.size} question(s) · 🏆 ${currentTestData?.credits || 0} credits available</p>
    </div>`;

  let qIndex = 0;
  qSnap.forEach(docSnap => {
    qIndex++;
    const q   = docSnap.data();
    const qId = docSnap.id;
    const options = [q.option1, q.option2, q.option3, q.option4];

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
        <h3 style="margin:0 0 14px;font-size:15px;color:#2c3e50;line-height:1.4;">${qIndex}. ${q.question}</h3>
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
};

window.completeTest = async function () {
  const user = auth.currentUser;
  if (!user || !currentTestId) return;
  const qSnap = await getDocs(query(collection(db, "questions"), where("testId", "==", currentTestId)));
  let score = 0, total = 0;
  const results = [];
  qSnap.forEach(docSnap => {
    total++;
    const q        = docSnap.data();
    const selected = userAnswers[docSnap.id];
    const isCorrect = selected === q.correctAnswer;
    if (isCorrect) score++;
    results.push({ question: q.question, selected: selected || "Not answered", correct: q.correctAnswer, isCorrect });
  });
  const earnedCredits = score * 10;
  const percentage    = total > 0 ? Math.round((score / total) * 100) : 0;
  const ref  = doc(db, "users", user.uid);
  const snap = await getDoc(ref);
  await updateDoc(ref, {
    credits: (snap.data().credits || 0) + earnedCredits,
    tests:   (snap.data().tests   || 0) + 1
  });
  await addDoc(collection(db, "testAttempts"), {
    userId: user.uid, testId: currentTestId,
    testTitle: currentTestData?.title || "",
    score, total, percentage, earnedCredits,
    attemptedAt: serverTimestamp()
  });

  const questionsPanel = el("testQuestions");
  const resultsPanel   = el("resultsContainer");
  if (questionsPanel) questionsPanel.style.display = "none";
  if (resultsPanel)   resultsPanel.style.display   = "block";

  if (resultsPanel) {
    const emoji = percentage >= 80 ? "🎉" : percentage >= 50 ? "👍" : "📚";
    const color = percentage >= 80 ? "#27ae60" : percentage >= 50 ? "#f39c12" : "#e74c3c";
    const resultRows = results.map(r => `
      <div style="padding:10px 12px;margin:6px 0;border-radius:8px;
                  background:${r.isCorrect ? '#f0fdf4' : '#fff5f5'};
                  border-left:4px solid ${r.isCorrect ? '#27ae60' : '#e74c3c'};">
        <p style="margin:0 0 4px;font-weight:600;font-size:13px;color:#2c3e50;">${r.question}</p>
        <p style="margin:0;font-size:12px;color:#555;">Your answer: <b>${r.selected}</b> ${r.isCorrect ? '✅' : '❌'}</p>
        ${!r.isCorrect ? `<p style="margin:2px 0 0;font-size:12px;color:#27ae60;">Correct: <b>${r.correct}</b></p>` : ""}
      </div>`).join("");
    resultsPanel.innerHTML = `
      <div style="background:#fff;border-radius:14px;padding:24px;box-shadow:0 2px 16px rgba(0,0,0,0.08);">
        <div style="text-align:center;margin-bottom:20px;">
          <div style="font-size:60px;line-height:1;margin-bottom:8px;">${emoji}</div>
          <h2 style="margin:0 0 4px;color:#2c3e50;">Test Completed!</h2>
          <p style="margin:0;color:#888;font-size:13px;">${currentTestData?.title || ""}</p>
        </div>
        <div style="display:flex;justify-content:center;gap:12px;margin-bottom:22px;flex-wrap:wrap;">
          <div style="background:#f8f9fa;padding:14px 20px;border-radius:12px;text-align:center;min-width:80px;">
            <div style="font-size:30px;font-weight:800;color:${color};">${score}/${total}</div>
            <div style="font-size:11px;color:#888;margin-top:2px;text-transform:uppercase;">Score</div>
          </div>
          <div style="background:#f8f9fa;padding:14px 20px;border-radius:12px;text-align:center;min-width:80px;">
            <div style="font-size:30px;font-weight:800;color:${color};">${percentage}%</div>
            <div style="font-size:11px;color:#888;margin-top:2px;text-transform:uppercase;">Accuracy</div>
          </div>
          <div style="background:#e8f5e9;padding:14px 20px;border-radius:12px;text-align:center;min-width:80px;">
            <div style="font-size:30px;font-weight:800;color:#27ae60;">+${earnedCredits}</div>
            <div style="font-size:11px;color:#888;margin-top:2px;text-transform:uppercase;">Credits</div>
          </div>
        </div>
        <h4 style="margin:0 0 10px;color:#2c3e50;font-size:14px;">📋 Answer Review</h4>
        ${resultRows}
        <button onclick="backTestDashboard()"
                style="width:100%;padding:13px;background:#3498db;color:#fff;border:none;border-radius:10px;cursor:pointer;font-size:15px;font-weight:600;margin-top:16px;">
          ← Back to Tests
        </button>
      </div>`;
  }
  loadProfile();
  currentTestId   = null;
  currentTestData = null;
  userAnswers     = {};
};

/* ================================================
   MY RESULTS — logged-in user's attempts only
   ================================================ */
window.loadMyResults = function () {
  const user = auth.currentUser;
  if (!user) return;
  const container = el("myResultsContainer");
  if (!container) return;
  container.innerHTML = `<div style="text-align:center;padding:30px;color:#888;">Loading...</div>`;
  onSnapshot(
    query(collection(db, "testAttempts"), where("userId", "==", user.uid)),
    snap => {
      container.innerHTML = "";
      if (snap.empty) {
        container.innerHTML = `
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
        container.innerHTML += `
          <div class="card" style="border-left:4px solid ${color};margin-bottom:12px;">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
              <h3 style="margin:0;font-size:15px;">${emoji} ${d.testTitle || "Unnamed Test"}</h3>
              <span style="font-size:20px;font-weight:800;color:${color};">${d.percentage}%</span>
            </div>
            <p style="margin:3px 0;color:#555;font-size:13px;">Score: <b>${d.score}/${d.total}</b></p>
            <p style="margin:3px 0;color:#27ae60;font-size:13px;font-weight:600;">+${d.earnedCredits} Credits earned</p>
            <p style="margin:3px 0;color:#aaa;font-size:12px;">📅 ${date}</p>
          </div>`;
      });
    }
  );
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
        userStats[d.userId] = { userId: d.userId, totalScore: 0, totalQuestions: 0, testsAttempted: 0, totalCredits: 0 };
      }
      userStats[d.userId].totalScore      += d.score || 0;
      userStats[d.userId].totalQuestions  += d.total || 0;
      userStats[d.userId].testsAttempted  += 1;
      userStats[d.userId].totalCredits    += d.earnedCredits || 0;
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

    // Sort by total credits earned (highest first)
    const sorted = userIds
      .map(uid => ({
        ...userStats[uid],
        name: nameMap[uid],
        overallPct: userStats[uid].totalQuestions > 0
          ? Math.round((userStats[uid].totalScore / userStats[uid].totalQuestions) * 100)
          : 0
      }))
      .sort((a, b) => b.totalCredits - a.totalCredits);

    container.innerHTML = "";
    const medals = ["🥇", "🥈", "🥉"];

    sorted.forEach((u, i) => {
      const rank     = i + 1;
      const medal    = medals[i] || `#${rank}`;
      const barColor = i === 0 ? "#f39c12" : i === 1 ? "#7f8c8d" : i === 2 ? "#cd6133" : "#3498db";
      const barWidth = sorted[0].totalCredits > 0 ? Math.round((u.totalCredits / sorted[0].totalCredits) * 100) : 0;
      const isMe     = auth.currentUser && u.userId === auth.currentUser.uid;

      container.innerHTML += `
        <div class="card" style="margin-bottom:12px;border-left:4px solid ${barColor};${isMe ? 'background:#fffbea;' : ''}">
          <div style="display:flex;align-items:center;gap:14px;">
            <div style="font-size:28px;min-width:36px;text-align:center;">${medal}</div>
            <div style="flex:1;">
              <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:4px;">
                <b style="font-size:15px;">${u.name}${isMe ? ' <span style="background:#3498db;color:#fff;font-size:10px;padding:2px 6px;border-radius:10px;font-weight:600;">YOU</span>' : ''}</b>
                <span style="font-weight:700;color:${barColor};font-size:14px;">+${u.totalCredits} credits</span>
              </div>
              <div style="font-size:12px;color:#888;margin-bottom:6px;">
                ${u.testsAttempted} test${u.testsAttempted !== 1 ? 's' : ''} attempted &nbsp;·&nbsp; Avg: ${u.overallPct}%
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
  if (screenId === "liveSessionScreen")     loadLiveSessions();
  if (screenId === "historySessionScreen")  loadSessionHistory();
  if (screenId === "ratingsSessionScreen")  { loadRatingsSessions(); loadRatingsContainer(); }
  if (screenId === "feedbackSessionScreen") loadFeedbackSessions();
  if (screenId === "endSessionScreen")      loadMyActiveSession();
};
window.backSessionDashboard = function () {
  if (el("sessionDashboard")) el("sessionDashboard").style.display = "grid";
  document.querySelectorAll("#session .screen").forEach(s => s.style.display = "none");
};
window.openTestScreen = function (screenId) {
  if (el("testDashboard")) el("testDashboard").style.display = "none";
  document.querySelectorAll("#testSection .screen").forEach(s => s.style.display = "none");
  if (el(screenId)) el(screenId).style.display = "block";
  if (screenId === "myQuestionsScreen") loadMyQuestions();
  if (screenId === "createTestScreen")  loadMyTests();
  if (screenId === "myTestsScreen")     loadMyTests();
  if (screenId === "resultsScreen")     loadMyResults();     // ← My Results
  if (screenId === "leaderboardScreen") loadLeaderboard();   // ← Leaderboard
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
window.openNotesScreen = function (screenId) {
  if (el("notesDashboard")) el("notesDashboard").style.display = "none";
  document.querySelectorAll("#notes .screen").forEach(s => s.style.display = "none");
  if (el(screenId)) el(screenId).style.display = "block";
};
window.backNotesDashboard = function () {
  if (el("notesDashboard")) el("notesDashboard").style.display = "grid";
  document.querySelectorAll("#notes .screen").forEach(s => s.style.display = "none");
};
window.openSettingsScreen = function (screenId) {
  if (el("settingsDashboard")) el("settingsDashboard").style.display = "none";
  document.querySelectorAll("#settings .screen").forEach(s => s.style.display = "none");
  if (el(screenId)) el(screenId).style.display = "block";
  if (screenId === "notificationSettingsScreen") loadNotificationSettings(); // ← load saved state
};
window.backSettingsDashboard = function () {
  if (el("settingsDashboard")) el("settingsDashboard").style.display = "grid";
  document.querySelectorAll("#settings .screen").forEach(s => s.style.display = "none");
};

window.openMeetingLink = openMeetingLink;