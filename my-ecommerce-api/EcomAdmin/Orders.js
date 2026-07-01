import { auth } from "./auth.js"; 
import { onAuthStateChanged } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import { getFirestore, collectionGroup, getDocs, doc, deleteDoc } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";

onAuthStateChanged(auth, (user) => {
    if (!user) { window.location.href = "login.html"; }
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

const orderTableBody = document.getElementById('orderTableBody');
const loader = document.getElementById('loader');

 
const formatDateTime = (dateVal) => {
    if (!dateVal) return { date: "New Order", time: "" };
    
    let fullStr = "";
    if (typeof dateVal === 'string') {
        fullStr = dateVal;
    } else if (dateVal.seconds) {
        fullStr = new Date(dateVal.seconds * 1000).toLocaleString();
    } else {
        return { date: "Recent Order", time: "" };
    }

     
    const parts = fullStr.split(' at ');
    return {
        date: parts[0],
        time: parts[1] ? parts[1].split(' ')[0] : ""  
    };
};

window.openInMaps = (addressText) => {
    try {
        let cleanStr = addressText.split(/Tel:/i)[0].split(',').slice(1).join(',').trim();
        const mapUrl = `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(cleanStr)}`;
        window.open(mapUrl, '_blank');
    } catch (e) { console.error(e); }
};

window.shareLocation = (name, phone, address, product) => {
    const mapUrl = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(address)}`;
    const shareMessage = `📦 *NEW DELIVERY ASSIGNMENT*\n\n👤 *Customer:* ${name}\n📞 *Phone:* ${phone}\n🛍️ *Product:* ${product}\n📍 *Address:* ${address}\n\n🗺️ *Map Link:* ${mapUrl}`;
    if (navigator.share && /Android|iPhone|iPad/i.test(navigator.userAgent)) {
        navigator.share({ title: 'Delivery Details', text: shareMessage }).catch(console.error);
    } else {
        window.open(`https://web.whatsapp.com/send?text=${encodeURIComponent(shareMessage)}`, '_blank');
    }
};

window.sendWhatsApp = (addressText, orderId, productName) => {
    const phoneMatch = addressText.match(/(\d{9,10})/);
    let phoneNumber = phoneMatch ? phoneMatch[0] : "";
    if (phoneNumber) {
        if (phoneNumber.startsWith("0")) phoneNumber = "94" + phoneNumber.substring(1);
        const message = `Halo, You ordered ${productName} (ID: ${orderId}). We received it. Thank you!-STHUB-`;
        window.open(`https://api.whatsapp.com/send?phone=${phoneNumber}&text=${encodeURIComponent(message)}`, '_blank');
    } else { alert("Phone number not found!"); }
};

async function loadOrders() {
    if(!orderTableBody) return;
    if (loader) loader.classList.remove('hidden');
    orderTableBody.innerHTML = "";

    try {
        const querySnapshot = await getDocs(collectionGroup(db, "MyOrders"));
        
        querySnapshot.forEach((orderDoc) => {
            const order = orderDoc.data();
            let fullAddress = order.deliveryAddress || "No Details Found";
            
             
            const dateTime = formatDateTime(order.orderDate);
            
            const phoneMatch = fullAddress.match(/(\d{9,10})/);
            let customerPhone = phoneMatch ? phoneMatch[0] : "N/A";
            let customerName = fullAddress.split(',')[0].trim();
            if(customerName.length > 25) customerName = "Customer";

            let temp = fullAddress.replace(/https?:\/\/\S+/gi, "").split(/Tel:/i)[0].split(/Post/i)[0].trim();
            let parts = temp.split(',').map(p => p.trim()).filter(p => p.length > 0);
            let cleanAddress = parts.length > 1 ? parts.slice(1).join(', ') : parts[0];

            const mapEmbedUrl = `https://maps.google.com/maps?q=${encodeURIComponent(cleanAddress)}&output=embed`;

            const row = `
                <tr class="hover:bg-gray-50 transition border-b border-gray-100">
                    <td class="px-6 py-4 text-xs font-mono text-gray-400">#${order.orderId ? order.orderId.substring(order.orderId.length - 6) : orderDoc.id.substring(0,6)}</td>
                    
                    <td class="px-6 py-4">
                        <div class="text-sm font-bold text-gray-800 mb-1">${customerName}</div>
                        
                        
                        <div class="bg-gray-100 px-2 py-1 rounded-md inline-block mb-1">
                             <div class="text-[12px] text-gray-900 font-extrabold">
                                <i class="far fa-calendar-alt mr-1 text-blue-600"></i>${dateTime.date}
                             </div>
                             <div class="text-[10px] text-gray-500 font-medium">
                                <i class="far fa-clock mr-1"></i>${dateTime.time}
                             </div>
                        </div>

                        <div class="text-xs text-blue-500 font-semibold"><i class="fas fa-phone mr-1"></i> ${customerPhone}</div>
                    </td>

                    <td class="px-6 py-4 text-xs text-gray-600">
                        <div class="line-clamp-2 mb-2">${cleanAddress}</div>
                        <button onclick="openInMaps('${fullAddress.replace(/'/g, "\\'")}')" 
                                class="text-blue-600 font-bold hover:underline">View on Map</button>
                    </td>

                    <td class="px-6 py-4">
                        <div class="flex items-center">
                            <img src="${order.productImage || ''}" class="h-10 w-10 object-cover rounded mr-3 border shadow-sm" onerror="this.src='https://via.placeholder.com/50'">
                            <span class="text-xs font-medium text-gray-700 max-w-[120px] truncate">${order.productName || 'Product'}</span>
                        </div>
                    </td>

                    <td class="px-6 py-4 font-bold text-gray-800 text-sm">LKR ${Number(order.totalAmount || 0).toLocaleString()}</td>

                    <td class="px-6 py-4 text-center">
                        <div class="flex items-center justify-center space-x-3">
                            <button onclick="sendWhatsApp('${fullAddress.replace(/'/g, "\\'")}', '${order.orderId || ''}', '${(order.productName || '').replace(/'/g, "\\'")}')" 
                                    class="text-green-500 text-xl hover:scale-110 transition">
                                <i class="fab fa-whatsapp"></i>
                            </button>
                            <button onclick="shareLocation('${customerName.replace(/'/g, "\\'")}', '${customerPhone}', '${cleanAddress.replace(/'/g, "\\'")}', '${(order.productName || '').replace(/'/g, "\\'")}')" 
                                    class="text-blue-500 text-xl hover:scale-110 transition">
                                <i class="fas fa-paper-plane"></i>
                            </button>
                            <button onclick="deleteOrder('${orderDoc.ref.path}')" 
                                    class="text-red-200 hover:text-red-500 transition">
                                <i class="fas fa-trash-alt"></i>
                            </button>
                        </div>
                    </td>
                </tr>`;
            orderTableBody.innerHTML += row;
        });
    } catch (e) { console.error(e); } finally { if (loader) loader.classList.add('hidden'); }
}

window.deleteOrder = async (path) => { 
    if(confirm("Are you sure?")) { 
        try { await deleteDoc(doc(db, path)); loadOrders(); } catch (error) { alert("Failed"); }
    } 
};

loadOrders();