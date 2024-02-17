package com.inf5190.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import javax.annotation.PostConstruct;

import com.inf5190.chat.auth.filter.AuthFilter;
import com.inf5190.chat.messages.MessageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;

import com.inf5190.chat.auth.session.SessionDataAccessor;
import com.inf5190.chat.auth.session.SessionManager;
import org.springframework.stereotype.Component;

@Component
@SpringBootApplication
@PropertySource("classpath:firebase.properties")
@PropertySource("classpath:cors.properties")
public class ChatApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatApplication.class);

    @Value("${firebase.project.id}")
    private String firebaseProjectId;

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

    @Value("${cors.allowedOrigins}")
    private String allowedOriginsProperty;

    @Value("${firebase.storage.bucket.name}")
    private String storageBucketNameProperty;

    @PostConstruct
    public void initialiseFirebase() throws IOException {
        if (FirebaseApp.getApps().size() == 0) {

            String projectId = Optional.ofNullable(System.getenv("GOOGLE_CLOUD_PROJECT"))
                    .orElse(this.firebaseProjectId);

            final FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder().setProjectId(projectId);
            File f = new File("/home/kaped/IdeaProjects/5190_Messaging_Platform_1/backend/firebase-key.json");
            if (f.exists()) {
                FileInputStream serviceAccount = new FileInputStream("/home/kaped/IdeaProjects/5190_Messaging_Platform_1/backend/firebase-key.json");
                optionsBuilder.setCredentials(GoogleCredentials.fromStream(serviceAccount));
            } else {
                optionsBuilder.setCredentials(GoogleCredentials.getApplicationDefault());
            }

            LOGGER.info("Initializing Firebase application.");
            FirebaseApp.initializeApp(optionsBuilder.build());

        } else {
            LOGGER.info("Firebase application already initialized.");
        }
    }

    @Bean
    public Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    @Bean
    public StorageClient getCloudStorage() {
        return StorageClient.getInstance();
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

//        Problem with the AuthFilter class that uses sessionDataAccessor
    @Bean
    public FilterRegistrationBean<AuthFilter> authenticationFilter(
            SessionDataAccessor sessionDataAccessor, SessionManager sessionManager) {
        FilterRegistrationBean<AuthFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new AuthFilter(sessionDataAccessor, sessionManager));
        registrationBean.addUrlPatterns("/messages", "/auth/logout");

        return registrationBean;
    }

    @Bean("allowedOrigins")
    public String[] getAllowedOrigins() {
        return Optional.ofNullable(System.getenv("ALLOWED_ORIGINS"))
                .orElse(this.allowedOriginsProperty).split(",");
    }

    @Bean("storageBucketName")
    public String getStorageBucketName() {
        return Optional.ofNullable(System.getenv("STORAGE_BUCKET_NAME"))
                .orElse(this.storageBucketNameProperty);
    }
}