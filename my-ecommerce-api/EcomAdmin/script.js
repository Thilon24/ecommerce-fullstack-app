import { auth } from "./auth.js";
import { onAuthStateChanged } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";


onAuthStateChanged(auth, (user) => {
    if (!user) {
        window.location.href = "login.html";
    }
});

//img BB
async function uploadToImgBB(file) {
    const apiKey = "4530d74db6ceb81a53b62e24f50ce3f6";
    const formData = new FormData();
    formData.append("image", file);

    try {
        const response = await fetch("https://api.imgbb.com/1/upload?key=" + apiKey, {
            method: "POST",
            body: formData
        }); 
        if (!response.ok) throw new Error(`ImgBB error! status: ${response.status}`);

        const data = await response.json();
        if (data.success) return data.data.url;
        else throw new Error(data.error.message || "Image Upload Failed");

    } catch (err) {
        console.error("ImgBB Error:", err);
        throw new Error("Image upload failed. Please try again.");
    }
}

const addForm = document.getElementById('addProductForm');

if (addForm) {
    addForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const fileInput = document.getElementById('pImgFile');
        const file = fileInput.files[0];
        const categoryName = document.getElementById('pCategory').value;
        const productType = document.getElementById('pType').value;
        const submitBtn = document.getElementById('submitBtn');

        if (!file) {
            alert("Please select an image!");
            return;
        }

        try {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i> Uploading Image...';

            // upload image
            const uploadedUrl = await uploadToImgBB(file);

            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i> Processing API...';


            let firestoreType = "";
            const cat = categoryName.toLowerCase();
            if (cat.includes("watch")) firestoreType = "watch";
            else if (cat.includes("kids")) firestoreType = "kids";
            else if (cat.includes("camera")) firestoreType = "camera";
            else if (cat.includes("shoe")) firestoreType = "shoes";
            else if (cat.includes("women")) firestoreType = "women";
            else if (cat.includes("men")) firestoreType = "men";
            else firestoreType = cat;

            // Data insert to Firestore via Custom API
            const productData = {
                name: document.getElementById('pName').value,
                price: parseInt(document.getElementById('pPrice').value),
                description: document.getElementById('pDesc').value,
                img_url: uploadedUrl,
                type: firestoreType,
                rating: "4.7",
                isNew: (productType === "New Products"),
                isPopular: (productType === "Popular Products")
            };

            // Call Custom API to save product
            const response = await fetch("http://localhost:3000/api/add-product", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(productData)
            });

            const result = await response.json();

            if (result.success) {
                alert("Product added successfully via Custom API!");
                addForm.reset();
            } else {
                throw new Error(result.error || "Failed to save product.");
            }

        } catch (error) {
            console.error("Submit Error:", error);
            alert("Error: " + error.message);
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-plus-circle mr-2"></i> Add Product Now';
        }
    });
}