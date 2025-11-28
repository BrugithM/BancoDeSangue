package com.example.aula.service;

import com.example.aula.model.Pessoa;
import com.example.aula.enums.TipoSanguineo;
import com.example.aula.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PessoaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    public List<Pessoa> findAll() {
        return pessoaRepository.findAll();
    }

    public Optional<Pessoa> findById(Long id) {
        return pessoaRepository.findById(id);
    }

    public Pessoa save(Pessoa pessoa) {
        return pessoaRepository.save(pessoa);
    }

    public void deleteById(Long id) {
        pessoaRepository.deleteById(id);
    }

    public List<Pessoa> findByNome(String nome) {
        return pessoaRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Pessoa> findByTipoSanguineo(String tipo) {
        try {
            TipoSanguineo tipoSanguineo = TipoSanguineo.fromString(tipo);
            return pessoaRepository.findByTipoSanguineo(tipoSanguineo);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public Optional<Pessoa> findByDocumento(String documento) {
        return pessoaRepository.findByDocumentosNumero(documento);
    }
}