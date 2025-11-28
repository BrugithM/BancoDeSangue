package com.example.aula.controller;

import com.example.aula.dto.DoacaoDTO;
import com.example.aula.mapper.DoacaoMapper;
import com.example.aula.model.Doacao;
import com.example.aula.service.DoacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doacoes")
@CrossOrigin(origins = "*")
@Tag(name = "Gestão de Doações", description = "Operações para registro e gestão de doações de sangue")
public class DoacaoController {

    @Autowired
    private DoacaoService doacaoService;

    @Autowired
    private DoacaoMapper doacaoMapper;

    @GetMapping
    @Operation(summary = "Listar todas as doações", description = "Retorna uma lista com todas as doações registradas")
    public List<DoacaoDTO> getAllDoacoes() {
        return doacaoService.findAll().stream()
                .map(doacaoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar doação por ID", description = "Retorna os detalhes de uma doação específica")
    public ResponseEntity<DoacaoDTO> getDoacaoById(@PathVariable Long id) {
        Optional<Doacao> doacao = doacaoService.findById(id);
        return doacao.map(d -> ResponseEntity.ok(doacaoMapper.toDTO(d)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Registrar nova doação", description = "Cadastra uma nova doação de sangue usando o documento do doador")
    public ResponseEntity<?> createDoacao(@Valid @RequestBody DoacaoDTO doacaoDTO) {
        try {
            Doacao doacao = doacaoMapper.toEntity(doacaoDTO);
            Doacao savedDoacao = doacaoService.save(doacao);
            return ResponseEntity.ok(doacaoMapper.toDTO(savedDoacao));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao cadastrar doação: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar doação", description = "Atualiza os dados de uma doação existente")
    public ResponseEntity<DoacaoDTO> updateDoacao(@PathVariable Long id,
                                                  @Valid @RequestBody DoacaoDTO doacaoDetails) {
        Optional<Doacao> doacaoOptional = doacaoService.findById(id);

        if (doacaoOptional.isPresent()) {
            Doacao doacao = doacaoOptional.get();

            // Atualizar campos permitidos (não permite alterar doador via documento)
            doacao.setQuantidade(doacaoDetails.getQuantidade());
            doacao.setStatus(doacaoDetails.getStatus());

            Doacao updatedDoacao = doacaoService.save(doacao);
            return ResponseEntity.ok(doacaoMapper.toDTO(updatedDoacao));
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir doação", description = "Remove uma doação do sistema")
    public ResponseEntity<Void> deleteDoacao(@PathVariable Long id) {
        if (doacaoService.findById(id).isPresent()) {
            doacaoService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/documento/{documento}")
    @Operation(summary = "Buscar doações por documento do doador", description = "Retorna todas as doações de um doador específico usando seu documento")
    public List<DoacaoDTO> getDoacoesPorDocumento(@PathVariable String documento) {
        return doacaoService.findByDocumentoDoador(documento).stream()
                .map(doacaoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar doações por status", description = "Retorna doações filtradas por status")
    public List<DoacaoDTO> getDoacoesPorStatus(@PathVariable String status) {
        try {
            com.example.aula.enums.StatusDoacao statusDoacao =
                    com.example.aula.enums.StatusDoacao.valueOf(status.toUpperCase());
            return doacaoService.findByStatus(statusDoacao).stream()
                    .map(doacaoMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @GetMapping("/doador/{documento}/compatibilidade")
    @Operation(summary = "Verificar compatibilidade do doador", description = "Verifica se o doador pode doar baseado em suas doações recentes")
    public ResponseEntity<String> verificarCompatibilidadeDoador(@PathVariable String documento) {
        try {
            List<Doacao> doacoesRecentes = doacaoService.findByDocumentoDoador(documento).stream()
                    .filter(d -> d.getStatus() == com.example.aula.enums.StatusDoacao.DISPONIVEL)
                    .toList();

            if (doacoesRecentes.isEmpty()) {
                return ResponseEntity.ok("Doador apto para nova doação");
            } else {
                return ResponseEntity.badRequest().body("Doador possui " + doacoesRecentes.size() + " doação(ões) em estoque. Aguarde a utilização.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao verificar compatibilidade: " + e.getMessage());
        }
    }
}