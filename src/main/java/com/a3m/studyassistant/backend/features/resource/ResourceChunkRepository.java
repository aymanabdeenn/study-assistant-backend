package com.a3m.studyassistant.backend.features.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceChunkRepository extends JpaRepository<ResourceChunk, UUID> {

   @Query(
           value = "SELECT * FROM resource_chunks rc " +
                   "WHERE rc.resource_id = :resourceId " +
                   "ORDER BY rc.embedding <=> cast(:queryEmbedding as vector) " +
                   "LIMIT :limit",
           nativeQuery = true)
   List<ResourceChunkProjection> findSimilarChunks(
            @Param("resourceId") UUID resourceId,
            @Param("queryEmbedding") String queryEmbedding,
            @Param("limit") int limit
   );

   @Query(value = "SELECT content FROM resource_chunks WHERE resource_id = :resourceId", nativeQuery = true)
   List<String> findAllContentByResourceId(@Param("resourceId") UUID resourceId);

}

