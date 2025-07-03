import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserManager {
    private static final String USERS_FILE = "users.txt";
    private static final String MESSAGES_FILE = "messages.txt";
    private Map<String, User> users = new HashMap<>();
    private List<Message> messages = new ArrayList<>();
    
    public UserManager() {
        loadUsers();
        loadMessages();
    }
    
    public static class User {
        private String username;
        private String passwordHash;
        private boolean isOnline;
        private Date lastSeen;
        
        public User(String username, String passwordHash) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.isOnline = false;
            this.lastSeen = new Date();
        }
        
        // Getters et setters
        public String getUsername() { return username; }
        public String getPasswordHash() { return passwordHash; }
        public boolean isOnline() { return isOnline; }
        public void setOnline(boolean online) { this.isOnline = online; }
        public Date getLastSeen() { return lastSeen; }
        public void setLastSeen(Date lastSeen) { this.lastSeen = lastSeen; }
    }
    
    public static class Message {
        private String sender;
        private String content;
        private Date timestamp;
        private String type; // "public", "private", "conference"
        private String recipient; // pour les messages privés
        
        public Message(String sender, String content, String type) {
            this.sender = sender;
            this.content = content;
            this.timestamp = new Date();
            this.type = type;
        }
        
        // Getters
        public String getSender() { return sender; }
        public String getContent() { return content; }
        public Date getTimestamp() { return timestamp; }
        public String getType() { return type; }
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password; // Fallback
        }
    }
    
    public boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false; // Utilisateur déjà existant
        }
        
        String passwordHash = hashPassword(password);
        User user = new User(username, passwordHash);
        users.put(username, user);
        saveUsers();
        return true;
    }
    
    public boolean authenticateUser(String username, String password) {
        User user = users.get(username);
        if (user == null) return false;
        
        String passwordHash = hashPassword(password);
        return user.getPasswordHash().equals(passwordHash);
    }
    
    public void setUserOnline(String username, boolean online) {
        User user = users.get(username);
        if (user != null) {
            user.setOnline(online);
            user.setLastSeen(new Date());
            saveUsers();
        }
    }
    
    public List<String> getOnlineUsers() {
        List<String> onlineUsers = new ArrayList<>();
        for (User user : users.values()) {
            if (user.isOnline()) {
                onlineUsers.add(user.getUsername());
            }
        }
        return onlineUsers;
    }
    
    public void saveMessage(String sender, String content, String type) {
        Message message = new Message(sender, content, type);
        messages.add(message);
        saveMessages();
    }
    
    public void savePrivateMessage(String sender, String recipient, String content) {
        Message message = new Message(sender, content, "private");
        message.setRecipient(recipient);
        messages.add(message);
        saveMessages();
    }
    
    public List<Message> getRecentMessages(int count) {
        int start = Math.max(0, messages.size() - count);
        return messages.subList(start, messages.size());
    }
    
    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    User user = new User(parts[0], parts[1]);
                    if (parts.length >= 3) {
                        user.setOnline(Boolean.parseBoolean(parts[2]));
                    }
                    users.put(parts[0], user);
                }
            }
        } catch (IOException e) {
            // Fichier n'existe pas encore, c'est normal
        }
    }
    
    private void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User user : users.values()) {
                writer.println(user.getUsername() + "|" + user.getPasswordHash() + "|" + user.isOnline());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadMessages() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MESSAGES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    Message message = new Message(parts[0], parts[1], parts[2]);
                    if (parts.length >= 4) {
                        message.setRecipient(parts[3]);
                    }
                    messages.add(message);
                }
            }
        } catch (IOException e) {
            // Fichier n'existe pas encore, c'est normal
        }
    }
    
    private void saveMessages() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(MESSAGES_FILE))) {
            for (Message message : messages) {
                String line = message.getSender() + "|" + message.getContent() + "|" + message.getType();
                if (message.getRecipient() != null) {
                    line += "|" + message.getRecipient();
                }
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 