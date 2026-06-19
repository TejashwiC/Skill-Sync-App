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