package org.example.Repository;

import org.example.Domain.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Long> {
    Long countByJogadorId_JogadorId(Long JogadorId);
}
