package com.sparrowx.document.retrieval;

import com.sparrowx.document.domain.models.RetrievalEvidence;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class RetrievalDeduplicator {

    public List<RetrievalEvidence> deduplicateByChunkId(List<RetrievalEvidence> evidence) {
        if (evidence == null || evidence.isEmpty()) {
            return List.of();
        }

        Map<String, RetrievalEvidence> bestByChunkId = evidence.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.chunkId() != null)
                .collect(Collectors.toMap(
                        item -> item.chunkId().value(),
                        item -> item,
                        (left, right) -> left.relevanceScore() >= right.relevanceScore()
                                ? left
                                : right
                ));

        return bestByChunkId.values()
                .stream()
                .sorted(Comparator.comparingDouble(RetrievalEvidence::relevanceScore).reversed())
                .toList();
    }
}