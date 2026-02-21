package com.balmaya.repo;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface VersionedRepository<T, ID> extends JpaRepository<T, ID> {

  @Query("select t from #{#entityName} t where t.logicalId = :logicalId and t.current = true")
  Optional<T> findCurrentByLogicalId(@Param("logicalId") UUID logicalId);

  @Query("select t from #{#entityName} t where t.logicalId = :logicalId order by t.version desc")
  List<T> findAllVersions(@Param("logicalId") UUID logicalId);
}

