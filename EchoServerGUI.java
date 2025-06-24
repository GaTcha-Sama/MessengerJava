import java.awt.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public class EchoServerGUI extends JFrame {

    private JTextArea logArea;
    private ServerSocket serverSocket;
    private AtomicInteger clientCounter = new AtomicInteger(0);
    // Map pour stocker tous les clients connectés avec leurs pseudos
    private Map<Integer, ClientInfo> clients = new ConcurrentHashMap<>();
    // Formateur pour l'horodatage
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    // Historique des messages (limité à 100 messages pour éviter la surcharge mémoire)
    private List<String> messageHistory = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 100;

    private static class ClientInfo {
        PrintWriter out;
        String pseudo;
        
        ClientInfo(PrintWriter out, String pseudo) {
            this.out = out;
            this.pseudo = pseudo;
        }
    }

    public EchoServerGUI() {
        setTitle("Serveur Echo - Chat Multi-clients");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        setVisible(true);
        startServer();
    }

    // Méthode pour démarrer le serveur
    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(12345); 
                log("Serveur en ecoute sur le port 12345...");
                log("En attente de connexions clients...");

                while (true) {
                    Socket clientSocket = serverSocket.accept(); 
                    int clientNumber = clientCounter.incrementAndGet(); 
                    log("Client " + clientNumber + " connecte : " + clientSocket.getInetAddress());

                    new Thread(() -> handleClient(clientSocket, clientNumber)).start();
                }

            } catch (IOException e) {
                log("Erreur serveur : " + e.getMessage());
            }
        }).start();
    }

    // Méthode pour obtenir l'horodatage actuel
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(timeFormatter);
    }

    // Méthode pour ajouter un message à l'historique
    private void addToHistory(String message) {
        messageHistory.add(message);
        // Limiter la taille de l'historique
        if (messageHistory.size() > MAX_HISTORY_SIZE) {
            messageHistory.remove(0);
        }
    }

    // Méthode pour envoyer l'historique à un client
    private void sendHistory(int clientNumber) {
        ClientInfo clientInfo = clients.get(clientNumber);
        if (clientInfo != null && !messageHistory.isEmpty()) {
            String timestamp = getCurrentTimestamp();
            clientInfo.out.println("[" + timestamp + "] --- Historique de la conversation ---");
            
            for (String message : messageHistory) {
                clientInfo.out.println(message);
            }
            
            clientInfo.out.println("[" + timestamp + "] --- Fin de l'historique ---");
        }
    }

    // Méthode de gestion des messages du client
    private void handleClient(Socket clientSocket, int clientNumber) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            // Demander le pseudo au client
            out.println("REQUEST_PSEUDO");
            
            String pseudo = in.readLine();
            if (pseudo == null || pseudo.trim().isEmpty()) {
                pseudo = "Client" + clientNumber;
            }
            
            // Stocker les informations du client
            clients.put(clientNumber, new ClientInfo(out, pseudo));
            out.println("CLIENT_NUMBER:" + clientNumber);
            out.println("PSEUDO_ACCEPTED:" + pseudo);
            
            // Envoyer l'historique AVANT d'envoyer la liste des clients
            sendHistory(clientNumber);
            
            sendClientList(clientNumber);
            
            String timestamp = getCurrentTimestamp();
            String joinMessage = "[" + timestamp + "] " + pseudo + " a rejoint le chat";
            broadcastMessage(joinMessage, clientNumber);
            addToHistory(joinMessage); // Ajouter à l'historique
            log("[" + timestamp + "] " + pseudo + " (Client " + clientNumber + ") a rejoint le chat");
            
            String message;
            while ((message = in.readLine()) != null) {
                ClientInfo clientInfo = clients.get(clientNumber);
                if (clientInfo != null) {
                    String timestamp2 = getCurrentTimestamp();
                    String formattedMessage = "[" + timestamp2 + "] " + clientInfo.pseudo + " : " + message;
                    log(formattedMessage);

                    if (message.equalsIgnoreCase("exit")) {
                        String exitMessage = "[" + timestamp2 + "] " + clientInfo.pseudo + " a quitte la session.";
                        log(exitMessage);
                        break;
                    }

                    broadcastMessage(formattedMessage, clientNumber);
                    addToHistory(formattedMessage); // Ajouter à l'historique
                }
            }

            ClientInfo clientInfo = clients.remove(clientNumber);
            if (clientInfo != null) {
                String timestamp3 = getCurrentTimestamp();
                String leaveMessage = "[" + timestamp3 + "] " + clientInfo.pseudo + " a quitte le chat";
                broadcastMessage(leaveMessage, -1);
                addToHistory(leaveMessage); // Ajouter à l'historique
                broadcastClientList();
            }
            clientSocket.close();
            
        } catch (IOException e) {
            log("Erreur client " + clientNumber + " : " + e.getMessage());
            clients.remove(clientNumber);
        }
    }

    // Méthode pour envoyer la liste des clients à un client spécifique
    private void sendClientList(int clientNumber) {
        ClientInfo clientInfo = clients.get(clientNumber);
        if (clientInfo != null) {
            String timestamp = getCurrentTimestamp();
            StringBuilder clientList = new StringBuilder("[" + timestamp + "] Liste des clients connectes:");
            boolean hasOtherClients = false;
            
            for (Map.Entry<Integer, ClientInfo> entry : clients.entrySet()) {
                int num = entry.getKey();
                ClientInfo info = entry.getValue();
                if (num != clientNumber) { 
                    clientList.append(num).append(":").append(info.pseudo).append(",");
                    hasOtherClients = true;
                }
            }
            
            if (hasOtherClients) {
                if (clientList.charAt(clientList.length() - 1) == ',') {
                    clientList.setLength(clientList.length() - 1);
                }
            } else {
                clientList.append(" : aucun");
            }
            
            clientInfo.out.println(clientList.toString());
        }
    }

    // Méthode pour diffuser la liste des clients à tous les clients
    private void broadcastClientList() {
        for (Map.Entry<Integer, ClientInfo> entry : clients.entrySet()) {
            int clientNum = entry.getKey();
            sendClientList(clientNum);
        }
    }

    // Méthode pour diffuser un message à tous les clients
    private void broadcastMessage(String message, int senderClientNumber) {
        for (Map.Entry<Integer, ClientInfo> entry : clients.entrySet()) {
            int clientNum = entry.getKey();
            ClientInfo clientInfo = entry.getValue();
            
            try {
                clientInfo.out.println(message);
            } catch (Exception e) {
                log("Erreur envoi au client " + clientNum + " : " + e.getMessage());
                clients.remove(clientNum);
            }
        }
    }

    // Affichage dans l'interface
    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        new EchoServerGUI(); // Lance le serveur
    }
}
