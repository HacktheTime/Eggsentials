package de.hype.eggsentials.client.common.api;

public class Discord {
    public static void sendWebhookMessage(String message) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
//                sendWebhookMessageNoThread(message);
            }
        });
        thread.start();
    }


//    public static void sendWebhookMessageNoThread(String message) {
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        String WEBHOOK_URL = "https://discord.com/api/v8/webhooks/1127524566407860276/" + BBsentials.getConfig().getApiKey();
//
//
//        try {
//            HttpPost httpPost = new HttpPost(WEBHOOK_URL);
//
//            StringEntity jsonEntity = new StringEntity("{\"content\": \"" + message + "\"}", StandardCharsets.UTF_8);
//            jsonEntity.setContentType("application/json");
//            httpPost.setEntity(jsonEntity);
//
//            CloseableHttpResponse response = httpClient.execute(httpPost);
//            HttpEntity responseEntity = response.getEntity();
//
//            if (responseEntity != null) {
//                InputStream inputStream = responseEntity.getContent();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                StringBuilder responseBuilder = new StringBuilder();
//                while ((line = reader.readLine()) != null) {
//                    responseBuilder.append(line);
//                }
//
//                String responseString = responseBuilder.toString();
//                Chat.sendPrivateMessageToSelfInfo(responseString);
//            }
//
//            response.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                httpClient.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
