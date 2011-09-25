package edu.cmu.tactic.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ResponseRepository extends PagingAndSortingRepository<Response, String> {
	@Query("{ 'server.address':?0 }")
	Page<Response> findByServerAddress(String ip, Pageable pageable);
}
