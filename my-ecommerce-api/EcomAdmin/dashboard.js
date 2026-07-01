import { app } from "./auth.js";
import { getFirestore, collectionGroup, getDocs, collection } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";
import { auth } from "./auth.js";
import { signOut } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";

//   Logout Function  
const logoutBtn = document.getElementById('logoutBtn');

if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
        if (confirm(" Are you sure you want to logout?")) {
            signOut(auth).then(() => {
                 
                window.location.href = "login.html";
            }).catch((error) => {
                alert("Logout failed: " + error.message);
            });
        }
    });
}
const db = getFirestore(app);

async function loadDashboard() {
    try {
        const orderSnapshot = await getDocs(collectionGroup(db, "MyOrders"));
        let totalSales = 0, allOrders = [], dailySales = {};

        orderSnapshot.forEach((doc) => {
            const data = doc.data();
            const amount = parseFloat(data.totalAmount || 0);
            totalSales += amount;
            allOrders.push(data);

            if (data.orderDate) {
                const dateLabel = data.orderDate.toDate().toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
                dailySales[dateLabel] = (dailySales[dateLabel] || 0) + amount;
            }
        });

        const productSnapshot = await getDocs(collection(db, "AllProducts"));
        
        document.getElementById('totalSalesText').innerText = `LKR ${totalSales.toLocaleString()}`;
        document.getElementById('totalOrdersText').innerText = orderSnapshot.size;
        document.getElementById('totalProductsText').innerText = productSnapshot.size;

        // Recent Orders Table
        const recentTable = document.getElementById('recentOrderTable');
        const sorted = allOrders.sort((a, b) => (b.orderDate || 0) - (a.orderDate || 0)).slice(0, 5);
        recentTable.innerHTML = sorted.map(order => `
            <tr class="border-b border-gray-50 italic text-xs">
                <td class="py-3 font-mono text-blue-500">#${order.orderId?.slice(-5) || '---'}</td>
                <td class="py-3 font-semibold">${order.productName}</td>
                <td class="py-3 text-right font-bold text-gray-800">LKR ${order.totalAmount}</td>
            </tr>
        `).join('');

        // Chart Logic
        renderChart(dailySales);

    } catch (err) { console.error(err); }
}

function renderChart(dailySales) {
    const ctx = document.getElementById('salesChart').getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: Object.keys(dailySales),
            datasets: [{
                label: 'Revenue',
                data: Object.values(dailySales),
                borderColor: '#3b82f6',
                tension: 0.4,
                fill: true,
                backgroundColor: 'rgba(59, 130, 246, 0.05)'
            }]
        },
        options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } } }
    });
}

loadDashboard();