package org.dpppt.backend.sdk.data;

import org.dpppt.backend.sdk.model.ExposeeDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExposeeRepository extends MongoRepository<ExposeeDoc, String> {
}
