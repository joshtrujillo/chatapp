# ChatApp

ChatApp is a simple Java-based chatroom application using TCP sockets. It supports multiple clients and implements a custom silly chatroom protocol. This program was created as an assingment to learn more about TCP and multi-threaded programming.

## Features
- Broadcast messages to all users.
- Private messaging.
- View online users.

## Requirements
- **Java SE 23 or later**
  - The application may work with earlier versions (e.g., Java 17), but this has not been tested.
- **Gradle (optional, for building the project):**
  - Included in the repository via the Graddle Wrapper (`./gradlew`).

## Usage
### Build the project
```bash
./gradlew build
```
### Run the Server
```bash
./gradlew :server:run
```
### Run the Client
```bash
./gradlew :client:run --args='<server-ip>'
```
