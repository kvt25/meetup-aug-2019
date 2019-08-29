package com.wizeline.meetup.backendservice;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface MessageRepository extends
        PagingAndSortingRepository<Message, String> {

    List<Message> findByName(String name);

}

