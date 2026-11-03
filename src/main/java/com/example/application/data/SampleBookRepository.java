package com.example.application.data;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleBookRepository implements PanacheRepository<SampleBook> {
}
