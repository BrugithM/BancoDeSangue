package com.example.aula.controller;

import com.example.aula.enums.TipoSanguineo;
import com.example.aula.model.Pessoa;
import com.example.aula.service.BancoSangueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banco-sangue")
@CrossOrigin(origins = "*")
@Tag(name = "Gestão de Banco de Sangue", description = "Operações para controle de estoque, compatibilidade e relatórios do banco de sangue")
public class BancoSangueController {

    @Autowired
    private BancoSangueService bancoSangueService;

    @GetMapping("/estoque")
    @Operation(summary = "Consultar estoque completo", description = "Retorna o estoque atual de sangue por tipo sanguíneo")
    public Map<TipoSanguineo, Integer> getEstoque() {
        return bancoSangueService.getEstoquePorTipo();
    }

    @GetMapping("/estoque/baixo")
    @Operation(summary = "Consultar estoque baixo", description = "Retorna os tipos sanguíneos com estoque abaixo do nível mínimo")
    public List<TipoSanguineo> getEstoqueBaixo() {
        return bancoSangueService.getTiposComEstoqueBaixo();
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Obter estatísticas gerais", description = "Retorna estatísticas completas do banco de sangue")
    public Map<String, Object> getEstatisticas() {
        return bancoSangueService.getEstatisticas();
    }

    @PostMapping("/doacoes/{doacaoId}/adicionar-estoque")
    @Operation(summary = "Adicionar doação ao estoque", description = "Processa uma doação e adiciona ao estoque do banco de sangue")
    public ResponseEntity<String> adicionarDoacaoAoEstoque(@PathVariable Long doacaoId) {
        try {
            bancoSangueService.adicionarDoacaoAoEstoque(doacaoId);
            return ResponseEntity.ok("Doação adicionada ao estoque com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao adicionar doação ao estoque: " + e.getMessage());
        }
    }

    @GetMapping("/doadores/compatíveis/{tipoSanguineo}")
    @Operation(summary = "Buscar doadores compatíveis", description = "Retorna lista de doadores compatíveis com um tipo sanguíneo específico")
    public ResponseEntity<List<Pessoa>> getDoadoresCompativeis(@PathVariable String tipoSanguineo) {
        try {
            TipoSanguineo tipo = TipoSanguineo.fromString(tipoSanguineo);
            List<Pessoa> doadores = bancoSangueService.findDoadoresCompativeis(tipo);
            return ResponseEntity.ok(doadores);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/compatibilidade/detalhada/{tipoReceptor}")
    @Operation(summary = "Obter tipos compatíveis detalhados", description = "Retorna todos os tipos sanguíneos compatíveis com um tipo receptor")
    public ResponseEntity<List<TipoSanguineo>> getTiposCompativeis(@PathVariable String tipoReceptor) {
        try {
            TipoSanguineo receptor = TipoSanguineo.fromString(tipoReceptor);
            List<TipoSanguineo> tiposCompativeis = switch (receptor) {
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
            return ResponseEntity.ok(tiposCompativeis);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/alertas/estoque")
    @Operation(summary = "Verificar alertas de estoque", description = "Retorna alertas sobre estoque baixo e outras situações críticas")
    public ResponseEntity<Map<String, Object>> getAlertasEstoque() {
        try {
            List<TipoSanguineo> estoqueBaixo = bancoSangueService.getTiposComEstoqueBaixo();
            
            Map<String, Object> alertas = Map.of(
                "estoqueBaixo", estoqueBaixo,
                "totalTiposComAlerta", estoqueBaixo.size(),
                "estoqueTotal", bancoSangueService.getEstatisticas().get("totalEstoque"),
                "situacao", estoqueBaixo.isEmpty() ? "NORMAL" : "ALERTA"
            );
            
            return ResponseEntity.ok(alertas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}