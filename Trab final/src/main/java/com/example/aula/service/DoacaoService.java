package com.example.aula.service;

import com.example.aula.model.Doacao;
import com.example.aula.model.Pessoa;
import com.example.aula.enums.StatusDoacao;
import com.example.aula.enums.TipoSanguineo;
import com.example.aula.repository.DoacaoRepository;
import com.example.aula.repository.PessoaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoacaoService {

    @Autowired
    private DoacaoRepository doacaoRepository;

     @Autowired
    private PessoaRepository pessoaRepository;

    public List<Doacao> findAll() {
        return doacaoRepository.findAll();
    }

    public Optional<Doacao> findById(Long id) {
        return doacaoRepository.findById(id);
    }

    public Doacao save(Doacao doacao) {
        // Validações
        if (doacao.getDoador() == null) {
            throw new RuntimeException("Doador não encontrado");
        }

        if (doacao.getTipoSanguineo() == null) {
            throw new RuntimeException("Tipo sanguíneo é obrigatório");
        }

        if (doacao.getQuantidade() == null || doacao.getQuantidade() <= 0) {
            throw new RuntimeException("Quantidade deve ser maior que zero");
        }

        return doacaoRepository.save(doacao);
    }

    public void deleteById(Long id) {
        doacaoRepository.deleteById(id);
    }

    public List<Doacao> findByDoadorId(Long doadorId) {
        return doacaoRepository.findByDoadorId(doadorId);
    }

    public List<Doacao> findByTipoSanguineo(TipoSanguineo tipoSanguineo) {
        return doacaoRepository.findByTipoSanguineoAndStatus(tipoSanguineo, StatusDoacao.DISPONIVEL);
    }

    public List<Doacao> findByStatus(StatusDoacao status) {
        return doacaoRepository.findByStatus(status);
    }

    public List<Doacao> findByDocumentoDoador(String documento) {
        Pessoa doador = pessoaRepository.findByDocumentosNumero(documento)
                .orElseThrow(() -> new RuntimeException("Doador não encontrado com documento: " + documento));
        
        return doacaoRepository.findByDoadorId(doador.getId());
    }
}