const express = require('express');
const admin = require('firebase-admin');
const cors = require('cors'); 

const app = express();
app.use(express.json());
app.use(cors());

// Firebase Admin Setup
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Custom API Endpoint - Product Adding
app.post('/api/add-product', async (req, res) => {
    try {
        const productData = req.body; 

        
        const currentTime = new Date().toLocaleString();
        console.log("\n======================================");
        console.log(` NEW REQUEST RECEIVED [${currentTime}]`);
        console.log(" Product Name :", productData.name);
        console.log(" Price        :", productData.price);
        console.log(" Category     :", productData.type);
        console.log("  New?      :", productData.isNew ? "YES" : "NO");
        console.log("  Popular?  :", productData.isPopular ? "YES" : "NO");
        console.log("======================================\n");

        
        const promises = [
            db.collection("AllProducts").add(productData),
            db.collection("ShowAll").add(productData)
        ];

        if (productData.isNew) {
            promises.push(db.collection("NewProducts").add(productData));
        }
        
        if (productData.isPopular) {
             promises.push(db.collection("PopularProducts").add(productData));
        }

         
        await Promise.all(promises);
        
        console.log(" Successfully saved to Firebase Firestore!");

        res.status(201).send({ 
            success: true, 
            message: "All product data saved successfully to Firestore." 
        });

    } catch (error) {
        console.error(" ERROR OCCURRED:", error);
        res.status(500).send({ success: false, error: error.message });
    }
});

const PORT = 3000;
app.listen(PORT, () => {
    console.log("---------------------------------------");
    console.log(` API Server is running on port: ${PORT}`);
    console.log(` Endpoint: http://localhost:${PORT}/api/add-product`);
    console.log("---------------------------------------");
});