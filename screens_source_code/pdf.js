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