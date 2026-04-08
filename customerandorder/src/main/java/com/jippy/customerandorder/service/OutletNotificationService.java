package com.jippy.customerandorder.service;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

@Service
public class OutletNotificationService {
        public void sendNewOrderNotification(String restaurantToken, String orderId) {
            Message message = Message.builder()
                    .setToken(restaurantToken)
                    .setNotification(Notification.builder()
                            .setTitle("New Order!")
                            .setBody("Order #" + orderId + " is ready for review.")
                            .build())
                    .putData("orderId", orderId) // Data payload for the app to process
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            try {
                String response = FirebaseMessaging.getInstance().send(message);
                System.out.println("Successfully sent message: " + response);
            } catch (FirebaseMessagingException e) {
                System.err.println("Error sending FCM message: " + e.getMessage());
            }
        }


    /*@PostMapping("/place-order")
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest order) {
        // 1. Save order to Postgres
        // 2. Fetch restaurant's FCM token from DB
        String token = restaurantRepo.findFcmTokenById(order.getRestaurantId());

        // 3. Trigger Notification
        notificationService.sendNewOrderNotification(token, order.getId());

        return ResponseEntity.ok("Order placed and restaurant notified!");
    }*/

}
