package com.example.application.services;

import com.example.application.data.SampleBook;
import com.example.application.data.SampleBookRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

// Transactional also for reads: PostgreSQL can only read the images (large objects) in a transaction
@ApplicationScoped
@Transactional
public class SampleBookService {

    private final SampleBookRepository repository;

    public SampleBookService(SampleBookRepository repository) {
        this.repository = repository;
    }

    public Optional<SampleBook> get(Long id) {
        return repository.findByIdOptional(id);
    }

    /**
     * Saves the changes of a detached book.
     *
     * @throws jakarta.persistence.OptimisticLockException if somebody else saved it in between
     */
    public SampleBook save(SampleBook entity) {
        SampleBook merged = repository.getEntityManager().merge(entity);
        repository.flush();
        return merged;
    }

    public List<SampleBook> list(Page page, Sort sort) {
        return repository.findAll(sort).page(page).list();
    }
}
