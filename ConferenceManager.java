import java.util.*;

public class ConferenceManager {
    private Map<String, Conference> conferences = new HashMap<>();
    
    public static class Conference {
        private String name;
        private String moderator;
        private Set<String> participants;
        private List<String> messages;
        private boolean isActive;
        
        public Conference(String name, String moderator) {
            this.name = name;
            this.moderator = moderator;
            this.participants = new HashSet<>();
            this.messages = new ArrayList<>();
            this.isActive = true;
            this.participants.add(moderator);
        }
        
        // Getters et setters
        public String getName() { return name; }
        public String getModerator() { return moderator; }
        public Set<String> getParticipants() { return participants; }
        public List<String> getMessages() { return messages; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        
        public boolean addParticipant(String username) {
            if (isActive) {
                participants.add(username);
                return true;
            }
            return false;
        }
        
        public boolean removeParticipant(String username) {
            if (isActive && !username.equals(moderator)) {
                participants.remove(username);
                return true;
            }
            return false;
        }
        
        public void addMessage(String message) {
            if (isActive) {
                messages.add(message);
            }
        }
        
        public boolean isParticipant(String username) {
            return participants.contains(username);
        }
        
        public boolean isModerator(String username) {
            return moderator.equals(username);
        }
    }
    
    public String createConference(String name, String moderator) {
        if (conferences.containsKey(name)) {
            return null; // Conférence déjà existante
        }
        
        Conference conference = new Conference(name, moderator);
        conferences.put(name, conference);
        return name;
    }
    
    public boolean addParticipantToConference(String conferenceName, String username) {
        Conference conference = conferences.get(conferenceName);
        if (conference != null && conference.isActive()) {
            return conference.addParticipant(username);
        }
        return false;
    }
    
    public boolean removeParticipantFromConference(String conferenceName, String username) {
        Conference conference = conferences.get(conferenceName);
        if (conference != null && conference.isActive()) {
            return conference.removeParticipant(username);
        }
        return false;
    }
    
    public void addMessageToConference(String conferenceName, String message) {
        Conference conference = conferences.get(conferenceName);
        if (conference != null && conference.isActive()) {
            conference.addMessage(message);
        }
    }
    
    public List<String> getConferenceParticipants(String conferenceName) {
        Conference conference = conferences.get(conferenceName);
        if (conference != null) {
            return new ArrayList<>(conference.getParticipants());
        }
        return new ArrayList<>();
    }
    
    public List<String> getConferenceMessages(String conferenceName) {
        Conference conference = conferences.get(conferenceName);
        if (conference != null) {
            return new ArrayList<>(conference.getMessages());
        }
        return new ArrayList<>();
    }
    
    public List<String> getUserConferences(String username) {
        List<String> userConferences = new ArrayList<>();
        for (Map.Entry<String, Conference> entry : conferences.entrySet()) {
            if (entry.getValue().isParticipant(username)) {
                userConferences.add(entry.getKey());
            }
        }
        return userConferences;
    }
    
    public boolean isModerator(String conferenceName, String username) {
        Conference conference = conferences.get(conferenceName);
        return conference != null && conference.isModerator(username);
    }
    
    public void closeConference(String conferenceName, String moderator) {
        Conference conference = conferences.get(conferenceName);
        if (conference != null && conference.isModerator(moderator)) {
            conference.setActive(false);
        }
    }
    
    public List<String> getActiveConferences() {
        List<String> activeConferences = new ArrayList<>();
        for (Map.Entry<String, Conference> entry : conferences.entrySet()) {
            if (entry.getValue().isActive()) {
                activeConferences.add(entry.getKey());
            }
        }
        return activeConferences;
    }
} 