# 📱 Application de Chat Multi-clients avec Interface Graphique

## �� Description

Cette application est un système de chat en temps réel développé en Java avec une interface graphique Swing. Elle permet à plusieurs utilisateurs de communiquer simultanément via un serveur centralisé, avec des fonctionnalités avancées comme les messages privés, les conférences et la gestion des groupes.

## 🏗️ Architecture

L'application suit une architecture client-serveur classique :

- **Serveur** (`EchoServerGUI.java`) : Gère les connexions multiples et la diffusion des messages
- **Client** (`EchoClientGUI.java`) : Interface utilisateur pour la communication
- **Gestionnaire d'utilisateurs** (`UserManager.java`) : Gestion de l'authentification et des comptes
- **Gestionnaire de conférences** (`ConferenceManager.java`) : Gestion des conférences et groupes

## 🚀 Fonctionnalités

### ✅ Fonctionnalités principales
- **Chat en temps réel** avec horodatage
- **Interface graphique intuitive** avec Swing
- **Authentification utilisateur** (inscription/connexion)
- **Messages privés** avec syntaxe `@pseudo message`
- **Liste des utilisateurs connectés** en temps réel
- **Historique des conversations** (limité à 100 messages)
- **Gestion des conférences et groupes**

### �� Fonctionnalités avancées
- **Système de pseudo** unique par utilisateur
- **Diffusion de messages** à tous les clients
- **Gestion des déconnexions** propre
- **Interface de connexion** séparée
- **Validation des entrées** utilisateur

## 📁 Structure du projet 

socket/
├── socket_cmd_v1/ # Version en ligne de commande
│ ├── EchoClient.java
│ ├── EchoClient.class
│ ├── EchoServer.java
│ └── EchoServer.class
└── socket_GUI/ # Version avec interface graphique
├── EchoClientGUI.java # Interface client
├── EchoServerGUI.java # Interface serveur
├── UserManager.java # Gestion des utilisateurs
└── ConferenceManager.java # Gestion des conférences

## 🛠️ Prérequis

- **Java JDK 8** ou supérieur
- **Java Swing** (inclus dans le JDK)
- **Réseau local** ou localhost pour les tests

## ⚙️ Installation et compilation

### 1. Compilation des fichiers Java

```bash
# Compiler le serveur
javac socket_GUI/EchoServerGUI.java

# Compiler le client
javac socket_GUI/EchoClientGUI.java

# Compiler les gestionnaires
javac socket_GUI/UserManager.java
javac socket_GUI/ConferenceManager.java
```

### 2. Lancement de l'application

```bash
# Démarrer le serveur (dans un premier terminal)
java -cp socket_GUI EchoServerGUI

# Démarrer le client (dans un second terminal)
java -cp socket_GUI EchoClientGUI
```

## 🎮 Guide d'utilisation

### Démarrage du serveur
1. Lancez `EchoServerGUI.java`
2. Le serveur se met en écoute sur le port **12345**
3. L'interface affiche les connexions et messages en temps réel

### Connexion d'un client
1. Lancez `EchoClientGUI.java`
2. Remplissez le formulaire de connexion :
   - **Nom d'utilisateur** : Votre pseudo
   - **Mot de passe** : Au moins 3 caractères
3. Cliquez sur "S'inscrire" ou "Se connecter"

### Interface de chat
Une fois connecté, vous avez accès à :

- **Zone de chat** : Affichage des messages avec horodatage
- **Champ de saisie** : Tapez votre message et appuyez sur Entrée
- **Boutons de contrôle** :
  - **Envoyer** : Envoyer le message
  - **Liste Clients** : Voir les utilisateurs connectés
  - **Déconnecter** : Quitter l'application

### Commandes disponibles

| Commande | Description |
|----------|-------------|
| `@pseudo message` | Envoyer un message privé |
| `/list` | Afficher la liste des clients |
| `exit` | Quitter l'application |

## 🔐 Système d'authentification

### Inscription
- Nom d'utilisateur requis
- Mot de passe minimum 3 caractères
- Hachage SHA-256 des mots de passe

### Connexion
- Validation des champs
- Vérification des identifiants
- Attribution d'un numéro de client unique

## 📊 Gestion des données

### Fichiers de données
- `users.txt` : Stockage des utilisateurs et mots de passe hachés
- `messages.txt` : Historique des messages

### Format des données

# users.txt
username|passwordHash|isOnline

# messages.txt
sender|content|type|recipient

## 🌐 Configuration réseau

### Paramètres par défaut
- **Port serveur** : 12345
- **Adresse** : localhost (127.0.0.1)
- **Protocole** : TCP Socket

### Modification du port
Pour changer le port, modifiez la ligne dans `EchoServerGUI.java` :
```java
serverSocket = new ServerSocket(12345); // Changez 12345 par votre port
```

Et dans `EchoClientGUI.java` :
```java
socket = new Socket("localhost", 12345); // Changez 12345 par votre port
```

##  Dépannage

### Problèmes courants

**Le serveur ne démarre pas**
- Vérifiez que le port 12345 n'est pas utilisé
- Assurez-vous d'avoir les permissions nécessaires

**Le client ne peut pas se connecter**
- Vérifiez que le serveur est démarré
- Contrôlez votre pare-feu
- Vérifiez l'adresse et le port

**Messages privés ne fonctionnent pas**
- Vérifiez la syntaxe : `@pseudo message`
- Assurez-vous que le pseudo existe

##  Développement

### Structure des classes

#### EchoServerGUI
- Gestion des connexions multiples
- Diffusion des messages
- Gestion des messages privés
- Historique des conversations

#### EchoClientGUI
- Interface utilisateur
- Gestion de la connexion
- Affichage des messages
- Gestion des événements

#### UserManager
- Authentification
- Gestion des comptes
- Persistance des données

#### ConferenceManager
- Création de conférences
- Gestion des participants
- Messages de conférence

### Extensions possibles

1. **Chiffrement des messages** (SSL/TLS)
2. **Base de données** pour les utilisateurs
3. **Transfert de fichiers**
4. **Notifications push**
5. **Interface web** (HTML/CSS/JavaScript)
6. **Support multi-langues**

## 📝 Exemples d'utilisation

### Créer une conférence
```java
// Dans le client
conferenceButton.addActionListener(e -> createConference());
```

### Envoyer un message privé
```java
// Format: @pseudo message
out.println("@Alice Bonjour, comment ça va ?");
```

### Lister les clients connectés
```java
// Commande pour obtenir la liste
out.println("/list");
```

##  Contribution

Pour contribuer au projet :
1. Forkez le repository
2. Créez une branche pour votre fonctionnalité
3. Committez vos changements
4. Poussez vers la branche
5. Créez une Pull Request

## 📞 Support

Pour toute question ou problème :
- Vérifiez la section dépannage
- Consultez les commentaires dans le code
- Créez une issue sur le repository

##  Licence

Ce projet est fourni à des fins éducatives. Vous êtes libre de le modifier et de l'utiliser selon vos besoins.

---

**Version** : 1.0  
**Dernière mise à jour** : 2024  
**Auteur** : Développeur Java  
**Langage** : Java  
**Interface** : Swing GUI

