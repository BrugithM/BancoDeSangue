package com.example.aula.repository;

import com.example.aula.model.Pessoa;
import com.example.aula.enums.TipoSanguineo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    List<Pessoa> findByNomeContainingIgnoreCase(String nome);
    
    List<Pessoa> findByTipoSanguineo(TipoSanguineo tipoSanguineo);
    
    List<Pessoa> findByTipoSanguineoIn(List<TipoSanguineo> tiposSanguineos);

    @Query("SELECT p FROM Pessoa p JOIN p.documentos d WHERE d.numero = :numero")
    Optional<Pessoa> findByDocumentosNumero(@Param("numero") String numero);
}