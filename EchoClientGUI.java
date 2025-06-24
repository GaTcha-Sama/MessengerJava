import java.awt.*;
import java.io.*;
import java.net.*;
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

    public EchoClientGUI() {
        setTitle("Client Echo"); 
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        inputField = new JTextField();

        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(e -> sendMessage());

        connectToServer();

        setVisible(true);
    }

    // Connexion au serveur
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345); 
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            chatArea.append("Connecte au serveur.\n");

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
                                    chatArea.append("Pseudo defini : " + pseudo + "\n");
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
                                    setTitle(pseudo); 
                                    chatArea.append("Numero de client attribue : " + clientNumber + "\n");
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
                                setTitle(pseudo);
                                chatArea.append("Pseudo accepte : " + pseudo + "\n");
                                chatArea.append("Vous pouvez maintenant communiquer !\n");
                            }
                        } else {
                            chatArea.append(response + "\n");
                        }
                    }
                } catch (IOException ex) {
                    chatArea.append("Deconnecte du serveur.\n");
                }
            }).start();

        } catch (IOException e) {
            chatArea.append("Erreur de connexion : " + e.getMessage() + "\n");
        }
    }

    private void sendMessage() {
        if (!pseudoSet) {
            chatArea.append("Veuillez attendre que votre pseudo soit accepte.\n");
            return;
        }
        
        String message = inputField.getText();
        if (message.isEmpty()) return;

        out.println(message); 
        inputField.setText("");

        if (message.equalsIgnoreCase("exit")) {
            try {
                socket.close(); 
                chatArea.append("Deconnexion.\n");
            } catch (IOException e) {
                chatArea.append("Erreur de fermeture : " + e.getMessage() + "\n");
            }
        }
    }

    public static void main(String[] args) {
        new EchoClientGUI(); // Lance le client
    }
}
