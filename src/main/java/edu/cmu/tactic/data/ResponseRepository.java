package edu.cmu.tactic.data;

import java.util.List;

import org.springframework.data.document.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface ResponseRepository extends CrudRepository<Response, String> {
	@Query("{ 'server.address':?0 }")
	List<Response> findByServerAddress(String ip);
}
