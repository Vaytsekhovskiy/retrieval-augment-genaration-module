package org.vced.ragutillib.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// stand by...
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

}
