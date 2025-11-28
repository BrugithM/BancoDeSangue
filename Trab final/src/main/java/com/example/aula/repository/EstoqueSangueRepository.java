package com.example.aula.repository;

import com.example.aula.model.EstoqueSangue;
import com.example.aula.enums.TipoSanguineo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstoqueSangueRepository extends JpaRepository<EstoqueSangue, Long> {

    Optional<EstoqueSangue> findByTipoSanguineo(TipoSanguineo tipoSanguineo);

    @Modifying
    @Query("UPDATE EstoqueSangue e SET e.quantidade = e.quantidade + :quantidade WHERE e.tipoSanguineo = :tipoSanguineo")
    void adicionarEstoque(@Param("tipoSanguineo") TipoSanguineo tipoSanguineo,
                          @Param("quantidade") Integer quantidade);

    @Modifying
    @Query("UPDATE EstoqueSangue e SET e.quantidade = e.quantidade - :quantidade WHERE e.tipoSanguineo = :tipoSanguineo")
    void removerEstoque(@Param("tipoSanguineo") TipoSanguineo tipoSanguineo,
                        @Param("quantidade") Integer quantidade);

    @Query("SELECT SUM(e.quantidade) FROM EstoqueSangue e")
    Integer getTotalEstoque();
}