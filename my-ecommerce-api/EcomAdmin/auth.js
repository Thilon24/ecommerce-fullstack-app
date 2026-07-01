import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import { getAuth, signInWithEmailAndPassword, onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";

 
const firebaseConfig = {
    apiKey: "AIzaSyCuWZ4IlWULMCoAZSZoOrQlbV0-_W6aJBw",
    authDomain: "ecommerse-43441.firebaseapp.com",
    projectId: "ecommerse-43441",
    storageBucket: "ecommerse-43441.firebasestorage.app",
    messagingSenderId: "89507986306",
    appId: "1:89507986306:web:8fa152a1fb54c877672d9b"
};

 
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

 
const loginForm = document.getElementById('loginForm');
const loginBtn = document.getElementById('loginBtn');
const errorMsg = document.getElementById('errorMsg');

if (loginForm) {
    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        
        loginBtn.disabled = true;
        loginBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i> Authenticating...';
        errorMsg.classList.add('hidden');

        // Firebase Login
        signInWithEmailAndPassword(auth, email, password)
            .then((userCredential) => {
                 
                window.location.href = "dashboard.html"; 
            })
            .catch((error) => {
                 
                loginBtn.disabled = false;
                loginBtn.innerHTML = 'Login to Dashboard';
                errorMsg.innerText = "Invalid login details. Please try again.";
                errorMsg.classList.remove('hidden');
                console.error(error.message);
            });
    });
}

 
export { app, auth };