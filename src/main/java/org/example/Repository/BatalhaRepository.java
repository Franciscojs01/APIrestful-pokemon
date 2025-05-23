package org.example.Repository;

import org.example.Domain.Batalha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface BatalhaRepository extends JpaRepository<Batalha, Long> {

}
