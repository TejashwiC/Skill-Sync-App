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