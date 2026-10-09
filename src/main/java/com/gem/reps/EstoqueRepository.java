package com.gem.reps;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gem.model.Estoque;

public interface EstoqueRepository extends JpaRepository<Estoque, Integer> {
    
}
