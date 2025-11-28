package com.example.aula.service;

import com.example.aula.enums.StatusDoacao;
import com.example.aula.enums.TipoSanguineo;
import com.example.aula.model.*;
import com.example.aula.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BancoSangueService {

    @Autowired
    private EstoqueSangueRepository estoqueRepository;

    @Autowired
    private DoacaoRepository doacaoRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    // Gerenciamento de Estoque
    @Transactional
    public void adicionarDoacaoAoEstoque(Long doacaoId) {
        Doacao doacao = doacaoRepository.findById(doacaoId)
                .orElseThrow(() -> new RuntimeException("Doação não encontrada"));

        if (doacao.getStatus() != StatusDoacao.DISPONIVEL) {
            throw new RuntimeException("Doação não está disponível");
        }

        EstoqueSangue estoque = estoqueRepository.findByTipoSanguineo(doacao.getTipoSanguineo())
                .orElse(new EstoqueSangue(null, doacao.getTipoSanguineo(), 0, LocalDate.now(), 10));

        // Converter ml para bolsas (1 bolsa ≈ 450ml)
        int bolsas = (int) Math.round(doacao.getQuantidade() / 450.0);
        if (bolsas == 0) {
            throw new RuntimeException("Quantidade insuficiente para formar uma bolsa (mínimo 450ml)");
        }
        estoque.setQuantidade(estoque.getQuantidade() + bolsas);

        estoqueRepository.save(estoque);
        doacao.setStatus(StatusDoacao.UTILIZADO);
        doacaoRepository.save(doacao);
    }

    // Relatórios e Consultas
    public Map<TipoSanguineo, Integer> getEstoquePorTipo() {
        Map<TipoSanguineo, Integer> estoque = new HashMap<>();
        for (TipoSanguineo tipo : TipoSanguineo.values()) {
            Integer quantidade = estoqueRepository.findByTipoSanguineo(tipo)
                    .map(EstoqueSangue::getQuantidade)
                    .orElse(0);
            estoque.put(tipo, quantidade);
        }
        return estoque;
    }

    public List<TipoSanguineo> getTiposComEstoqueBaixo() {
        return estoqueRepository.findAll().stream()
                .filter(e -> e.getQuantidade() <= e.getQuantidadeMinima())
                .map(EstoqueSangue::getTipoSanguineo)
                .toList();
    }

    public Map<String, Object> getEstatisticas() {
        Map<String, Object> estatisticas = new HashMap<>();

        estatisticas.put("totalEstoque", estoqueRepository.getTotalEstoque());
        estatisticas.put("totalDoacoes", doacaoRepository.count());
        estatisticas.put("doacoesDisponiveis",
                doacaoRepository.findByStatus(StatusDoacao.DISPONIVEL).size());

        Map<TipoSanguineo, Long> doacoesPorTipo = new HashMap<>();
        for (TipoSanguineo tipo : TipoSanguineo.values()) {
            long count = doacaoRepository.findByTipoSanguineoAndStatus(tipo, StatusDoacao.DISPONIVEL).size();
            doacoesPorTipo.put(tipo, count);
        }
        estatisticas.put("doacoesPorTipo", doacoesPorTipo);

        return estatisticas;
    }

    // Agendamento para verificar doações vencidas
    @Scheduled(cron = "0 0 6 * * ?") // Executa diariamente às 6h
    @Transactional
    public void verificarDoacoesVencidas() {
        List<Doacao> doacoesVencidas = doacaoRepository.findDoacoesVencidas(LocalDate.now());

        for (Doacao doacao : doacoesVencidas) {
            doacao.setStatus(StatusDoacao.VENCIDO);
        }

        doacaoRepository.saveAll(doacoesVencidas);
    }

    // Buscar doadores compatíveis
    public List<Pessoa> findDoadoresCompativeis(TipoSanguineo tipoReceptor) {
        List<TipoSanguineo> tiposCompatíveis = getTiposCompativeis(tipoReceptor);
        return pessoaRepository.findByTipoSanguineoIn(tiposCompatíveis);
    }

    private List<TipoSanguineo> getTiposCompativeis(TipoSanguineo tipoReceptor) {
        return switch (tipoReceptor) {
            case A_POSITIVO -> List.of(TipoSanguineo.A_POSITIVO, TipoSanguineo.A_NEGATIVO,
                    TipoSanguineo.O_POSITIVO, TipoSanguineo.O_NEGATIVO);
            case A_NEGATIVO -> List.of(TipoSanguineo.A_NEGATIVO, TipoSanguineo.O_NEGATIVO);
            case B_POSITIVO -> List.of(TipoSanguineo.B_POSITIVO, TipoSanguineo.B_NEGATIVO,
                    TipoSanguineo.O_POSITIVO, TipoSanguineo.O_NEGATIVO);
            case B_NEGATIVO -> List.of(TipoSanguineo.B_NEGATIVO, TipoSanguineo.O_NEGATIVO);
            case AB_POSITIVO -> List.of(TipoSanguineo.values()); // Receptor universal
            case AB_NEGATIVO -> List.of(TipoSanguineo.A_NEGATIVO, TipoSanguineo.B_NEGATIVO,
                    TipoSanguineo.AB_NEGATIVO, TipoSanguineo.O_NEGATIVO);
            case O_POSITIVO -> List.of(TipoSanguineo.O_POSITIVO, TipoSanguineo.O_NEGATIVO);
            case O_NEGATIVO -> List.of(TipoSanguineo.O_NEGATIVO); // Doador universal
        };
    }
}