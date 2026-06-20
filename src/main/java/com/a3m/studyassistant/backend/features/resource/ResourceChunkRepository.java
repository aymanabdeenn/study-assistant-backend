package com.a3m.studyassistant.backend.features.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

   @Modifying
   @Transactional
   @Query(value = "INSERT into resource_chunks (id, resource_id, chunk_index, page_number, content, embedding) " +
           "VALUES (:id, :resourceId, :chunkIndex, :pageNumber, :content, cast(:embeddingString as vector))",
           nativeQuery = true)
   void insertChunkWithVector(
           @Param("id") UUID id,
           @Param("resourceId") UUID resourceId,
           @Param("chunkIndex") int chunkIndex,
           @Param("pageNumber") int pageNumber,
           @Param("content") String content,
           @Param("embeddingString") String embeddingString // 👈 Transmitted cleanly as a vector string
   );

}

