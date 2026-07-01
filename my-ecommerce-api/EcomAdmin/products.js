import { auth } from "./auth.js"; //   Auth  
import { onAuthStateChanged } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import { getFirestore, collection, getDocs, deleteDoc, doc, query, where } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";

 
onAuthStateChanged(auth, (user) => {
    if (!user) {
        window.location.href = "login.html";  
    }
});

const firebaseConfig = {
    apiKey: "AIzaSyCuWZ4IlWULMCoAZSZoOrQlbV0-_W6aJBw",
    authDomain: "ecommerse-43441.firebaseapp.com",
    projectId: "ecommerse-43441",
    storageBucket: "ecommerse-43441.firebasestorage.app",
    messagingSenderId: "89507986306",
    appId: "1:89507986306:web:8fa152a1fb54c877672d9b"
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

const tableBody = document.getElementById('productTableBody');
const loader = document.getElementById('loader');

async function loadProducts() {
    if (!tableBody) return;  
    loader.classList.remove('hidden');
    tableBody.innerHTML = ""; 

    try {
        const querySnapshot = await getDocs(collection(db, "ShowAll"));
        
        querySnapshot.forEach((productDoc) => {
            const product = productDoc.data();
            
            const tagHTML = product.isNew ? 
                '<span class="bg-green-100 text-green-700 px-2 py-1 rounded text-[10px] font-bold">NEW</span>' : 
                '<span class="bg-orange-100 text-orange-700 px-2 py-1 rounded text-[10px] font-bold">POPULAR</span>';

            const row = `
                <tr class="hover:bg-gray-50 border-b border-gray-100 transition">
                    <td class="px-6 py-4"><img src="${product.img_url}" class="h-10 w-10 object-cover rounded shadow-sm"></td>
                    <td class="px-6 py-4 font-medium text-gray-800">${product.name}</td>
                    <td class="px-6 py-4 text-gray-500 text-sm">${product.type || 'General'}</td>
                    <td class="px-6 py-4 text-blue-600 font-bold">LKR ${product.price}</td>
                    <td class="px-6 py-4">${tagHTML}</td>
                    <td class="px-6 py-4">
                        <button onclick="deleteProductFull('${productDoc.id}', '${product.name}')" class="text-red-400 hover:text-red-600">
                            <i class="fas fa-trash-alt"></i>
                        </button>
                    </td>
                </tr>
            `;
            tableBody.innerHTML += row;
        });
    } catch (e) { console.error(e); }
    finally { if (loader) loader.classList.add('hidden'); }
}

// Global Delete Function
window.deleteProductFull = async (docId, productName) => {
    if (confirm(`Are you sure you want to remove "${productName}"?`)) {
        try {
            await deleteDoc(doc(db, "ShowAll", docId));
            const cols = ["AllProducts", "NewProducts", "PopularProducts"];
            for (const c of cols) {
                const q = query(collection(db, c), where("name", "==", productName));
                const snap = await getDocs(q);
                snap.forEach(async (d) => await deleteDoc(doc(db, c, d.id)));
            }
            alert("Removed successfully!");
            loadProducts();
        } catch (err) { alert(err.message); }
    }
};

loadProducts();