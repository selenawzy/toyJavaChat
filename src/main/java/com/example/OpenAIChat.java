package com.example;

import com.google.gson.Gson;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

public class OpenAIChat {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = System.getenv("OPENAI_API_KEY"); // Load API Key from environment variable

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Prompt for user input
        System.out.print("Enter your question: ");
        String userQuestion = scanner.nextLine();

        // Get response from OpenAI and print it
        String response = getResponseFromOpenAI(userQuestion);
        System.out.println(response);
    }

    private static String getResponseFromOpenAI(String userInput) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Add a system message to set the assistant's behavior
            Message systemMessage = new Message("system", "you are a tarot reader, user going to input a question, you will answer the question with a tarot card and reading");
            Message userMessage = new Message("user", userInput);
    
            // Prepare the JSON payload
            String payload = new Gson().toJson(new OpenAIRequest(
                    "gpt-3.5-turbo", // Specify the model
                    new Message[] { systemMessage, userMessage } // Include system and user messages
            ));
    
            // Create HTTP POST request
            HttpPost post = new HttpPost(API_URL);
            post.setHeader("Authorization", "Bearer " + API_KEY);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
    
            // Execute request and read response
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
    
                // Parse the JSON response
                OpenAIResponse openAIResponse = new Gson().fromJson(result.toString(), OpenAIResponse.class);
                if (openAIResponse == null || openAIResponse.choices == null || openAIResponse.choices.length == 0) {
                    return "Error: No valid response from OpenAI API.";
                }
    
                // Return the assistant's message content
                return openAIResponse.choices[0].message.content;
            }
        } catch (Exception e) {
            return "Error: Unable to connect to OpenAI API.";
        }
    }


    // Helper classes for request and response
    static class OpenAIRequest {
        String model;
        Message[] messages;

        public OpenAIRequest(String model, Message[] messages) {
            this.model = model;
            this.messages = messages;
        }
    }

    static class Message {
        String role;
        String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    static class OpenAIResponse {
        Choice[] choices;

        static class Choice {
            Message message;

            static class Message {
                String role;
                String content;
            }
        }
    }
}
