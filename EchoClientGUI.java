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
    
    // Nouveaux composants pour l'interface améliorée
    private JButton sendButton;
    private JButton disconnectButton;
    private JButton conferenceButton;
    private JButton groupButton;
    private JComboBox<String> chatTypeCombo;
    private JComboBox<String> recipientCombo;
    private JPanel topPanel;
    private JPanel bottomPanel;

    public EchoClientGUI() {
        setTitle("Chat Client - Connexion");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Afficher d'abord l'interface de connexion
        showLoginInterface();
        
        // Rendre la fenetre visible
        setVisible(true);
    }

    private void showLoginInterface() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("Chat Client - Connexion");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        JLabel usernameLabel = new JLabel("Nom d'utilisateur:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        loginPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        loginPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Mot de passe:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        loginPanel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("Se connecter");
        JButton registerButton = new JButton("S'inscrire");

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (login(username, password)) {
                pseudo = username;
                showChatInterface();
            }
        });

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (register(username, password)) {
                JOptionPane.showMessageDialog(this, "Inscription reussie ! Vous pouvez maintenant vous connecter.", "Succes", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginPanel.add(buttonPanel, gbc);

        add(loginPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private boolean login(String username, String password) {
        // Verifier que les champs ne sont pas vides
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un nom d'utilisateur", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un mot de passe", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Pour l'instant, accepter toute connexion avec des champs valides
        // Plus tard, vous pourrez implementer la vraie authentification
        return true;
    }

    private boolean register(String username, String password) {
        // Verifier que les champs ne sont pas vides
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un nom d'utilisateur", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un mot de passe", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Verifier la longueur minimale du mot de passe
        if (password.length() < 3) {
            JOptionPane.showMessageDialog(this, "Le mot de passe doit contenir au moins 3 caracteres", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Pour l'instant, accepter toute inscription avec des champs valides
        // Plus tard, vous pourrez implementer la vraie inscription
        return true;
    }

    private void showChatInterface() {
        getContentPane().removeAll();
        // Titre avec seulement le nom d'utilisateur
        setTitle(pseudo);
        setLayout(new BorderLayout());

        // Panel superieur avec les controles
        topPanel = new JPanel(new FlowLayout());
        chatTypeCombo = new JComboBox<>(new String[]{"Chat general", "Message prive", "Conference"});
        recipientCombo = new JComboBox<>();
        conferenceButton = new JButton("Nouvelle conference");
        groupButton = new JButton("Nouveau groupe");

        topPanel.add(new JLabel("Type de chat:"));
        topPanel.add(chatTypeCombo);
        topPanel.add(new JLabel("Destinataire:"));
        topPanel.add(recipientCombo);
        topPanel.add(conferenceButton);
        topPanel.add(groupButton);

        // Zone de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        // Panel inferieur avec saisie et boutons
        bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Envoyer");
        disconnectButton = new JButton("Deconnecter");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton listClientsButton = new JButton("Liste Clients");
        
        buttonPanel.add(sendButton);
        buttonPanel.add(listClientsButton);
        buttonPanel.add(disconnectButton);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Ajout des composants
        add(topPanel, BorderLayout.NORTH);
        add(chatScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Event listeners
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        disconnectButton.addActionListener(e -> disconnect());
        listClientsButton.addActionListener(e -> requestClientList());
        conferenceButton.addActionListener(e -> createConference());
        groupButton.addActionListener(e -> createGroup());

        // Connexion au serveur
        connectToServer();

        revalidate();
        repaint();
    }

    private void createConference() {
        String conferenceName = JOptionPane.showInputDialog(this, "Nom de la conference:", "Nouvelle conference", JOptionPane.QUESTION_MESSAGE);
        if (conferenceName != null && !conferenceName.trim().isEmpty()) {
            if (out != null) {
                out.println("CREATE_CONFERENCE:" + conferenceName);
            }
        }
    }

    private void createGroup() {
        String groupName = JOptionPane.showInputDialog(this, "Nom du groupe:", "Nouveau groupe", JOptionPane.QUESTION_MESSAGE);
        if (groupName != null && !groupName.trim().isEmpty()) {
            if (out != null) {
                out.println("CREATE_GROUP:" + groupName);
            }
        }
    }

    private void disconnect() {
        try {
            if (out != null) {
                out.println("DISCONNECT");
            }
            if (socket != null) {
                socket.close();
            }
            String timestamp = getCurrentTimestamp();
            chatArea.append("[" + timestamp + "] Deconnecte du serveur.\n");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                            // Envoyer seulement le pseudo, pas la commande LOGIN
                            out.println(pseudo);
                        } else if (response.startsWith("CLIENT_NUMBER:")) {
                            try {
                                String[] parts = response.split(":");
                                if (parts.length >= 2) {
                                    String numberStr = parts[1].trim();
                                    clientNumber = Integer.parseInt(numberStr);
                                    // Titre avec seulement le nom d'utilisateur
                                    setTitle(pseudo); 
                                    String timestamp3 = getCurrentTimestamp();
                                    chatArea.append("[" + timestamp3 + "] Numero de client attribue : " + clientNumber + "\n");
                                }
                            } catch (NumberFormatException e) {
                                chatArea.append("Erreur parsing numero client: " + response + "\n");
                            }
                        } else if (response.startsWith("PSEUDO_ACCEPTED:")) {
                            String[] parts = response.split(":");
                            if (parts.length >= 2) {
                                pseudo = parts[1].trim();
                                pseudoSet = true;
                                // Titre avec seulement le nom d'utilisateur
                                setTitle(pseudo);
                                String timestamp4 = getCurrentTimestamp();
                                chatArea.append("[" + timestamp4 + "] Pseudo accepte : " + pseudo + "\n");
                                chatArea.append("[" + timestamp4 + "] Vous pouvez maintenant communiquer !\n");
                                
                                SwingUtilities.invokeLater(() -> requestClientList());
                            }
                        } else if (response.contains("Liste des clients connectes:")) {
                            updateAvailableClients(response);
                            chatArea.append(response + "\n");
                        } else if (response.startsWith("CONFERENCE_CREATED:")) {
                            String[] parts = response.split(":");
                            if (parts.length >= 2) {
                                String confName = parts[1];
                                chatArea.append("[" + getCurrentTimestamp() + "] Conference creee : " + confName + "\n");
                            }
                        } else if (response.startsWith("GROUP_CREATED:")) {
                            String[] parts = response.split(":");
                            if (parts.length >= 2) {
                                String groupName = parts[1];
                                chatArea.append("[" + getCurrentTimestamp() + "] Groupe cree : " + groupName + "\n");
                            }
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
        recipientCombo.removeAllItems();
        recipientCombo.addItem("Tous");
        
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
                            recipientCombo.addItem(clientPseudo);
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
