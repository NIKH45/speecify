package com.speechify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ClientRepository {
    private final ObjectMapper objectMapper;
    private final JsonDb db;

    public ClientRepository(JsonDb db, ObjectMapper objectMapper) {
        this.db = db;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<Client> getById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ObjectNode root = db.readRoot();
                ArrayNode clients = (ArrayNode) root.get("clients");
                
                for (int i = 0; i < clients.size(); i++) {
                    ObjectNode clientNode = (ObjectNode) clients.get(i);
                    if (clientNode.get("id").asText().equals(id)) {
                        Client client = new Client();
                        client.setId(clientNode.get("id").asText());
                        client.setName(clientNode.get("name").asText());
                        return client;
                    }
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        });
    }

    public CompletableFuture<List<Client>> getAll() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ObjectNode root = db.readRoot();
                ArrayNode clients = (ArrayNode) root.get("clients");
                List<Client> clientList = new ArrayList<>();

                for (int i = 0; i < clients.size(); i++) {
                    ObjectNode clientNode = (ObjectNode) clients.get(i);
                    Client client = new Client();
                    client.setId(clientNode.get("id").asText());
                    client.setName(clientNode.get("name").asText());
                    clientList.add(client);
                }
                return clientList;
            } catch (Exception e) {
                return new ArrayList<>();
            }
        });
    }
} 
