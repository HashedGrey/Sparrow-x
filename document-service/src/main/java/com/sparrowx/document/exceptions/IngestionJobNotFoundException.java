package com.sparrowx.document.exceptions;

import buildingblocks.shared.exceptions.NotFoundException;

public class IngestionJobNotFoundException extends NotFoundException {

    public IngestionJobNotFoundException(String ingestionJobId) {
        super("Ingestion job not found: " + ingestionJobId);
    }
}