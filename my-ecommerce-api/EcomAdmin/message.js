// Firebase Configuration  
const firebaseConfig = {
    apiKey: "AIzaSyCuWZ4IlWULMCoAZSZoOrQlbV0-_W6aJBw",
    authDomain: "ecommerse-43441.firebaseapp.com",
    projectId: "ecommerse-43441",
    storageBucket: "ecommerse-43441.firebasestorage.app",
    messagingSenderId: "89507986306",
    appId: "1:89507986306:web:8fa152a1fb54c877672d9b"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);
const db = firebase.firestore();

let selectedUserId = null;
let firstLoad = true; 
const notificationSound = new Audio('https://browser-default-notification-sound.s3.amazonaws.com/notification.mp3'); 

 
const defaultAvatar = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23cbd5e1'%3E%3Cpath d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z'/%3E%3C/svg%3E";
 
function loadChatList() {
    db.collection("Chats").orderBy("timestamp", "desc").onSnapshot((snapshot) => {
        const userChatList = document.getElementById('userChatList');
        const badge = document.getElementById('msgCount');
        
        const count = snapshot.size;
        if (badge) {
            if (count > 0) {
                badge.innerText = count;
                badge.classList.remove('hidden');
            } else {
                badge.classList.add('hidden');
            }
        }

        if (!firstLoad && !snapshot.metadata.hasPendingWrites) {
            notificationSound.play().catch(e => console.log("Audio blocked"));
        }
        firstLoad = false;

        userChatList.innerHTML = "";
        if (snapshot.empty) {
            userChatList.innerHTML = `<div class="p-10 text-center text-gray-400 font-medium">No chats found.</div>`;
            return;
        }

        snapshot.forEach((doc) => {
            const chatData = doc.data();
            const userId = doc.id; 
            const isActive = userId === selectedUserId ? 'active-user' : '';

            
            db.collection("CurrentUser").doc(userId).get().then((userDoc) => {
                let displayName = userId; 
                let imgHtml = "";  

                if (userDoc.exists) {
                    displayName = userDoc.data().userName || userId;
                    
                    
                    const base64Img = userDoc.data().profileImage;
                    
                    if (base64Img) {
                        
                        imgHtml = `<img src="data:image/jpeg;base64,${base64Img}" alt="Avatar" class="w-full h-full rounded-full object-cover shadow-sm">`;
                    } else {
                         
                        imgHtml = `<img src="${defaultAvatar}" alt="Default Avatar" class="w-full h-full rounded-full object-cover">`;
                    }
                } else {
                   
                     imgHtml = `<img src="${defaultAvatar}" alt="Default Avatar" class="w-full h-full rounded-full object-cover">`;
                }

       
                userChatList.innerHTML += `
                    <div id="item-${userId}" class="user-item p-4 border-b transition ${isActive}" onclick="selectUser('${userId}', '${displayName}')">
                        <div class="flex items-center">
                            <div class="w-10 h-10 rounded-full flex items-center justify-center mr-3 flex-shrink-0 bg-slate-100">
                                ${imgHtml}
                            </div>
                            <div class="flex-1 overflow-hidden">
                                <h4 class="font-bold text-gray-800 truncate text-sm">${displayName}</h4>
                                <p class="text-xs text-gray-400 truncate mt-1">${chatData.lastMessage || 'Message...'}</p>
                            </div>
                        </div>
                    </div>`;
            });
        });
    });
}
 
window.selectUser = function(userId, userName) {
    selectedUserId = userId;
    document.getElementById('chatInputArea').classList.remove('hidden');
    
   
    db.collection("CurrentUser").doc(userId).get().then((userDoc) => {
        let headerImgHtml = "";
        if (userDoc.exists && userDoc.data().profileImage) {
            headerImgHtml = `<img src="data:image/jpeg;base64,${userDoc.data().profileImage}" class="w-10 h-10 rounded-full object-cover mr-3 shadow-sm">`;
        } else {
            headerImgHtml = `<img src="${defaultAvatar}" class="w-10 h-10 rounded-full object-cover mr-3">`;
        }

        document.getElementById('chatHeader').innerHTML = `
            <div class="flex items-center justify-between w-full">
                <div class="flex items-center">
                    ${headerImgHtml}
                    <div>
                        <b class="text-gray-900 block text-base">${userName}</b>
                        <div class="flex items-center text-xs text-green-600">
                            <div class="w-1.5 h-1.5 bg-green-500 rounded-full mr-1.5 animate-pulse"></div>
                            Online
                        </div>
                    </div>
                </div>
                <button onclick="deleteAllMessages('${userId}')" class="text-red-500 hover:text-red-700 transition p-2" title="Delete All Messages">
                    <i class="fas fa-trash-alt"></i>
                </button>
            </div>`;
    });
    
    loadMessages(userId);
}

 
function loadMessages(userId) {
    db.collection("Chats").doc(userId).collection("Messages")
        .orderBy("timestamp", "asc")
        .onSnapshot((snapshot) => {
            const display = document.getElementById('messageDisplay');
            display.innerHTML = "";

            snapshot.forEach((doc) => {
                const msg = doc.data();
                const msgId = doc.id;
                const isAdmin = msg.isAdmin;
                
                display.innerHTML += `
                    <div class="flex ${isAdmin ? 'justify-end' : 'justify-start'} mb-2 group">
                        <div class="relative px-4 py-2 rounded-2xl max-w-[80%] shadow-sm text-sm ${
                            isAdmin ? 'bg-blue-600 text-white rounded-tr-none' : 'bg-white text-gray-800 border rounded-tl-none'
                        }">
                            ${msg.text}
                            <button onclick="deleteSingleMessage('${userId}', '${msgId}')" 
                                    class="absolute -top-2 ${isAdmin ? '-left-2' : '-right-2'} bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity shadow-lg">
                                <i class="fas fa-times text-[10px]"></i>
                            </button>
                        </div>
                    </div>`;
            });
            display.scrollTop = display.scrollHeight;
        });
}

 
window.deleteSingleMessage = function(userId, msgId) {
    if (confirm("Are you sure you want to delete this message?")) {
        db.collection("Chats").doc(userId).collection("Messages").doc(msgId).delete();
    }
}

//  DELETE  MESSAGES 
window.deleteAllMessages = function(userId) {
    if (confirm("this will delete all messages with this user. Are you sure?")) {
        db.collection("Chats").doc(userId).collection("Messages").get().then((snapshot) => {
            const batch = db.batch();
            snapshot.forEach((doc) => {
                batch.delete(doc.ref);
            });
            return batch.commit();
        }).then(() => {
            return db.collection("Chats").doc(userId).delete();
        }).then(() => {
            alert("All messages deleted successfully.");
            selectedUserId = null;
            document.getElementById('chatInputArea').classList.add('hidden');
            document.getElementById('messageDisplay').innerHTML = '<p class="text-center text-gray-400 mt-20">Click on a user to start chatting</p>';
            document.getElementById('chatHeader').innerText = 'Select a User';
        }).catch((error) => {
            alert("Error: " + error.message);
        });
    }
}

 
document.getElementById('btnSendReply').onclick = function() {
    const input = document.getElementById('adminReplyInput');
    const text = input.value.trim();

    if (text && selectedUserId) {
        const msgData = {
            text: text,
            senderId: "admin",
            isAdmin: true,
            timestamp: firebase.firestore.FieldValue.serverTimestamp()
        };

        db.collection("Chats").doc(selectedUserId).collection("Messages").add(msgData).then(() => {
            input.value = "";
            db.collection("Chats").doc(selectedUserId).set({
                lastMessage: text,
                timestamp: firebase.firestore.FieldValue.serverTimestamp(),
                userId: selectedUserId
            }, { merge: true });
        });
    }
};

loadChatList();