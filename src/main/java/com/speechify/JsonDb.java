package com.speechify;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonDb {
    private final File dbFile;
    private final ObjectMapper mapper;

    public JsonDb(String dbFilePath, ObjectMapper mapper) {
        this.dbFile = new File(dbFilePath);
        this.mapper = mapper;
    }

    public ObjectNode readRoot() throws IOException {
        if (!dbFile.exists()) {
            throw new IOException("db.json not found");
        }
        return (ObjectNode) mapper.readTree(dbFile);
    }

    public void writeRoot(ObjectNode root) throws IOException {
        mapper.writeValue(dbFile, root);
    }
}
