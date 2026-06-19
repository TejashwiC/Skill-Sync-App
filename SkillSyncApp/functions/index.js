const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendChatNotification = functions.firestore
  .document("chats/{chatId}/messages/{messageId}")
  .onCreate(async (snap, context) => {
    const messageData = snap.data();
    const chatId = context.params.chatId;
    const senderId = messageData.sender;
    const senderName = messageData.senderName || "Someone";
    let text = messageData.text;

    if (!text) {
        if (messageData.audioUrl) text = "🎤 Voice Message";
        else if (messageData.pdfUrl) text = "📄 PDF Document";
        else text = "New message";
    }

    // Determine recipient
    // chatId for private chat is uid1_uid2
    let recipientId = null;
    if (chatId.includes("_")) {
        const ids = chatId.split("_");
        recipientId = ids[0] === senderId ? ids[1] : ids[0];
    } else {
        // It's a group chat (chatId = groupId)
        // For groups, we'd need to fetch members and send to all except sender
        console.log("Group notifications not fully implemented in this basic function yet");
        return null;
    }

    if (!recipientId) return null;

    // Fetch recipient's FCM token
    const userDoc = await admin.firestore().collection("users").doc(recipientId).get();
    if (!userDoc.exists) return null;

    const userData = userDoc.data();
    const fcmToken = userData.fcmToken;

    if (!fcmToken) {
        console.log("No FCM token found for user:", recipientId);
        return null;
    }

    // Send FCM Notification
    const payload = {
      notification: {
        title: senderName,
        body: text,
      },
      data: {
        chatId: chatId,
        type: "chat_message"
      }
    };

    try {
      await admin.messaging().sendToDevice(fcmToken, payload);
      console.log("Notification sent successfully");
    } catch (error) {
      console.error("Error sending notification:", error);
    }

    return null;
  });
