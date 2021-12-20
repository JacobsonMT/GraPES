package com.jacobsonmt.mags.ui.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class Utils {

    public static String jsonToCsv(JsonNode node) throws JsonProcessingException {
        Builder csvSchemaBuilder = CsvSchema.builder();
        node.fieldNames().forEachRemaining(csvSchemaBuilder::addColumn);
        CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();

        CsvMapper csvMapper = new CsvMapper();
        return csvMapper.writerFor(JsonNode.class)
            .with(csvSchema)
            .withFeatures(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS)
            .writeValueAsString(node);
    }

    public static ResponseEntity<ByteArrayResource> downloadAsFile(String filename, String content) {
        HttpHeaders headers = new HttpHeaders(); headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        byte[] bytes = content.getBytes();
        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(bytes.length)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }

}
