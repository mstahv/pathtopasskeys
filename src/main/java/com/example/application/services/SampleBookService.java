package com.example.application.services;

import com.example.application.data.SampleBook;
import com.example.application.data.SampleBookRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class SampleBookService {

    private final SampleBookRepository repository;

    public SampleBookService(SampleBookRepository repository) {
        this.repository = repository;
    }

    public Optional<SampleBook> get(Long id) {
        return repository.findById(id);
    }

    public SampleBook save(SampleBook entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<SampleBook> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<SampleBook> list(Pageable pageable, Specification<SampleBook> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
