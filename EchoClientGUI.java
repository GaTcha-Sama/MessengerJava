import java.awt.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;

public class EchoClientGUI extends JFrame {

    private JTextArea chatArea;
    private JTextField inputField;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private int clientNumber = 0;
    private String pseudo = "";
    private boolean pseudoSet = false;
    // Formateur pour l'horodatage
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    // Nouveau: Map pour stocker les clients disponibles
    private java.util.Map<Integer, String> availableClients = new java.util.HashMap<>();

    public EchoClientGUI() {
        setTitle("Client Echo"); 
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Zone de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        
        // Panel de contrôle
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        // Zone de saisie
        inputField = new JTextField();
        
        // Panel des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        // Bouton pour afficher la liste des clients
        JButton listClientsButton = new JButton("Liste Clients");
        listClientsButton.addActionListener(e -> requestClientList());
        
        buttonPanel.add(listClientsButton);
        
        controlPanel.add(inputField, BorderLayout.CENTER);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(mainPanel);

        inputField.addActionListener(e -> sendMessage());

        connectToServer();

        setVisible(true);
    }

    // Nouvelle méthode pour demander la liste des clients
    private void requestClientList() {
        if (out != null) {
            out.println("/list");
        }
    }

    // Connexion au serveur
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345); 
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String timestamp = getCurrentTimestamp();
            chatArea.append("[" + timestamp + "] Connecte au serveur.\n");

            // Thread pour lire les messages entrants
            new Thread(() -> {
                String response;
                try {
                    while ((response = in.readLine()) != null) {
                        System.out.println("Message recu: '" + response + "'"); 
                        
                        if (response.equals("REQUEST_PSEUDO")) {
                            // Demander le pseudo à l'utilisateur
                            SwingUtilities.invokeLater(() -> {
                                String userPseudo = JOptionPane.showInputDialog(
                                    this,
                                    "Entrez votre pseudo :",
                                    "Pseudo",
                                    JOptionPane.QUESTION_MESSAGE
                                );
                                
                                if (userPseudo != null && !userPseudo.trim().isEmpty()) {
                                    pseudo = userPseudo.trim();
                                    out.println(pseudo);
                                    String timestamp2 = getCurrentTimestamp();
                                    chatArea.append("[" + timestamp2 + "] Pseudo defini : " + pseudo + "\n");
                                } else {
                                    out.println("Client");
                                }
                            });
                        } else if (response.startsWith("CLIENT_NUMBER:")) {
                            try {
                                String[] parts = response.split(":");
                                if (parts.length >= 2) {
                                    String numberStr = parts[1].trim();
                                    clientNumber = Integer.parseInt(numberStr);
                                    setTitle(pseudo + " (Client " + clientNumber + ")"); 
                                    String timestamp3 = getCurrentTimestamp();
                                    chatArea.append("[" + timestamp3 + "] Numero de client attribue : " + clientNumber + "\n");
                                } else {
                                    chatArea.append("Format invalide pour CLIENT_NUMBER: " + response + "\n");
                                }
                            } catch (NumberFormatException e) {
                                chatArea.append("Erreur parsing numero client: " + response + "\n");
                                e.printStackTrace(); 
                            }
                        } else if (response.startsWith("PSEUDO_ACCEPTED:")) {
                            String[] parts = response.split(":");
                            if (parts.length >= 2) {
                                pseudo = parts[1].trim();
                                pseudoSet = true;
                                setTitle(pseudo + " (Client " + clientNumber + ")");
                                String timestamp4 = getCurrentTimestamp();
                                chatArea.append("[" + timestamp4 + "] Pseudo accepte : " + pseudo + "\n");
                                chatArea.append("[" + timestamp4 + "] Vous pouvez maintenant communiquer !\n");
                                
                                // Demander automatiquement la liste des clients après connexion
                                SwingUtilities.invokeLater(() -> requestClientList());
                            }
                        } else if (response.startsWith("[" + getCurrentTimestamp() + "] Liste des clients connectes:")) {
                            // Mettre à jour la liste des clients disponibles
                            updateAvailableClients(response);
                        } else if (response.contains("Liste des clients connectes:")) {
                            // Mettre à jour la liste des clients disponibles
                            updateAvailableClients(response);
                            // Afficher la liste dans le chat pour debug
                            chatArea.append(response + "\n");
                        } else {
                            chatArea.append(response + "\n");
                        }
                    }
                } catch (IOException ex) {
                    String timestamp5 = getCurrentTimestamp();
                    chatArea.append("[" + timestamp5 + "] Deconnecte du serveur.\n");
                }
            }).start();

        } catch (IOException e) {
            String timestamp6 = getCurrentTimestamp();
            chatArea.append("[" + timestamp6 + "] Erreur de connexion : " + e.getMessage() + "\n");
        }
    }

    // Nouvelle méthode pour mettre à jour la liste des clients disponibles
    private void updateAvailableClients(String clientListMessage) {
        availableClients.clear();
        String[] parts = clientListMessage.split(":");
        if (parts.length >= 2) {
            String clientsPart = parts[1].trim();
            if (!clientsPart.equals("aucun")) {
                String[] clientEntries = clientsPart.split(",");
                for (String entry : clientEntries) {
                    String[] clientInfo = entry.trim().split(":");
                    if (clientInfo.length >= 2) {
                        try {
                            int clientNum = Integer.parseInt(clientInfo[0].trim());
                            String clientPseudo = clientInfo[1].trim();
                            availableClients.put(clientNum, clientPseudo);
                        } catch (NumberFormatException e) {
                            // Ignorer les entrées invalides
                        }
                    }
                }
            }
        }
    }

    // Méthode pour obtenir l'horodatage actuel
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(timeFormatter);
    }

    private void sendMessage() {
        if (!pseudoSet) {
            String timestamp = getCurrentTimestamp();
            chatArea.append("[" + timestamp + "] Veuillez attendre que votre pseudo soit accepte.\n");
            return;
        }
        
        String message = inputField.getText();
        if (message.isEmpty()) return;

        // Envoyer directement le message au serveur (plus de conversion)
        out.println(message);
        inputField.setText("");

        if (message.equalsIgnoreCase("exit")) {
            try {
                socket.close(); 
                String timestamp2 = getCurrentTimestamp();
                chatArea.append("[" + timestamp2 + "] Deconnexion.\n");
            } catch (IOException e) {
                String timestamp3 = getCurrentTimestamp();
                chatArea.append("[" + timestamp3 + "] Erreur de fermeture : " + e.getMessage() + "\n");
            }
        }
    }

    public static void main(String[] args) {
        new EchoClientGUI(); // Lance le client
    }
}
