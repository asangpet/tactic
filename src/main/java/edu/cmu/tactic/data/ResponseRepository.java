package edu.cmu.tactic.data;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ResponseRepository extends PagingAndSortingRepository<Response, String> {
	@Query("{ 'server.address':?0 }")
	Page<Response> findByServerAddress(String ip, Pageable pageable);
	
	@Query("{ 'protocol':?0 }")
	Page<Response> findByProtocol(String protocol, Pageable pageable);
	
	@Query("{ 'protocol':?0 }")
	List<Response> findAllByProtocol(String protocol);
}
