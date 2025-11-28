package com.example.aula.repository;

import com.example.aula.model.Doacao;
import com.example.aula.enums.StatusDoacao;
import com.example.aula.enums.TipoSanguineo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    List<Doacao> findByDoadorId(Long doadorId);

    List<Doacao> findByTipoSanguineoAndStatus(TipoSanguineo tipoSanguineo, StatusDoacao status);

    List<Doacao> findByStatus(StatusDoacao status);

    @Query("SELECT d FROM Doacao d WHERE d.validoAte < :data AND d.status = 'DISPONIVEL'")
    List<Doacao> findDoacoesVencidas(@Param("data") LocalDate data);

    @Query("SELECT SUM(d.quantidade) FROM Doacao d WHERE d.tipoSanguineo = :tipoSanguineo AND d.status = 'DISPONIVEL'")
    Double getQuantidadeTotalPorTipo(@Param("tipoSanguineo") TipoSanguineo tipoSanguineo);

    // Novo método para buscar doações por doador e status
    List<Doacao> findByDoadorIdAndStatus(Long doadorId, StatusDoacao status);
}