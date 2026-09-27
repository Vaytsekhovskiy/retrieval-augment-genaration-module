package org.vced.ragutillib.authority;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository для работы с таблицей authority_mapping
 */
@Repository
public interface AuthorityMappingRepository extends JpaRepository<AuthorityMapping, Integer> {

    Optional<AuthorityMapping> findByExternalName(String externalName);
}

