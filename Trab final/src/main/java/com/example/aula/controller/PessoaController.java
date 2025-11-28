package com.example.aula.controller;

import com.example.aula.dto.PessoaDTO;
import com.example.aula.mapper.PessoaMapper;
import com.example.aula.model.Endereco;
import com.example.aula.model.Pessoa;
import com.example.aula.repository.PessoaRepository;
import com.example.aula.service.CepService;
import com.example.aula.service.PessoaService;
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
@RequestMapping("/api/pessoas")
@CrossOrigin(origins = "*")
@Tag(name = "Gestão de Pessoas", description = "Operações para cadastro e gestão de pessoas e doadores")
public class PessoaController {

    @Autowired
    private PessoaService pessoaService;
    
    @Autowired
    private PessoaMapper pessoaMapper;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private CepService cepService;

    @GetMapping
    @Operation(summary = "Listar todas as pessoas", description = "Retorna uma lista com todas as pessoas cadastradas")
    public List<PessoaDTO> getAllPessoas() {
        return pessoaService.findAll().stream()
                .map(pessoaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pessoa por ID", description = "Retorna os detalhes de uma pessoa específica")
    public ResponseEntity<PessoaDTO> getPessoaById(@PathVariable Long id) {
        Optional<Pessoa> pessoa = pessoaService.findById(id);
        return pessoa.map(p -> ResponseEntity.ok(pessoaMapper.toDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Cadastrar nova pessoa", description = "Cria um novo cadastro de pessoa/doador")
    public ResponseEntity<PessoaDTO> createPessoa(@Valid @RequestBody PessoaDTO pessoaDTO) {
        try {
            Pessoa pessoa = pessoaMapper.toEntity(pessoaDTO);
            Pessoa savedPessoa = pessoaService.save(pessoa);
            return ResponseEntity.ok(pessoaMapper.toDTO(savedPessoa));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pessoa", description = "Atualiza os dados de uma pessoa existente")
    public ResponseEntity<PessoaDTO> updatePessoa(@PathVariable Long id,
                                                  @Valid @RequestBody PessoaDTO pessoaDetails) {
        Optional<Pessoa> pessoaOptional = pessoaService.findById(id);

        if (pessoaOptional.isPresent()) {
            Pessoa pessoa = pessoaOptional.get();
            pessoa.setNome(pessoaDetails.getNome());
            pessoa.setEndereco(pessoaDetails.getEndereco());
            pessoa.setTipoSanguineo(pessoaDetails.getTipoSanguineo());
            pessoa.setDocumentos(pessoaDetails.getDocumentos());
            pessoa.setFiliacao(pessoaDetails.getFiliacao());
            pessoa.setContatos(pessoaDetails.getContatos());

            Pessoa updatedPessoa = pessoaService.save(pessoa);
            return ResponseEntity.ok(pessoaMapper.toDTO(updatedPessoa));
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir pessoa", description = "Remove uma pessoa do sistema")
    public ResponseEntity<Void> deletePessoa(@PathVariable Long id) {
        if (pessoaService.findById(id).isPresent()) {
            pessoaService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/buscar")
    @Operation(summary = "Buscar pessoas por nome", description = "Busca pessoas pelo nome (busca parcial)")
    public List<PessoaDTO> buscarPorNome(@RequestParam String nome) {
        return pessoaService.findByNome(nome).stream()
                .map(pessoaMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/tipo-sanguineo/{tipo}")
    @Operation(summary = "Buscar pessoas por tipo sanguíneo", description = "Retorna pessoas com um tipo sanguíneo específico")
    public List<PessoaDTO> buscarPorTipoSanguineo(@PathVariable String tipo) {
        return pessoaService.findByTipoSanguineo(tipo).stream()
                .map(pessoaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/cep/{cep}")
    @Operation(summary = "Consultar endereço por CEP", description = "Consulta os dados de endereço a partir de um CEP válido")
    public ResponseEntity<Endereco> consultarCep(@PathVariable String cep) {
        try {
            Endereco endereco = cepService.consultarCep(cep);
            if (endereco != null) {
                return ResponseEntity.ok(endereco);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/cidade/{cidade}")
    @Operation(summary = "Buscar pessoas por cidade", description = "Retorna pessoas cadastradas em uma cidade específica")
    public List<PessoaDTO> getPessoasPorCidade(@PathVariable String cidade) {
        return pessoaService.findAll().stream()
                .filter(pessoa -> pessoa.getEndereco() != null &&
                        cidade.equalsIgnoreCase(pessoa.getEndereco().getCidade()))
                .map(pessoaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Buscar pessoas por estado", description = "Retorna pessoas cadastradas em um estado específico")
    public List<PessoaDTO> getPessoasPorEstado(@PathVariable String estado) {
        return pessoaService.findAll().stream()
                .filter(pessoa -> pessoa.getEndereco() != null &&
                        estado.equalsIgnoreCase(pessoa.getEndereco().getEstado()))
                .map(pessoaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/documento/{documento}")
    @Operation(summary = "Buscar pessoa por documento", description = "Retorna uma pessoa específica pelo número do documento")
    public ResponseEntity<PessoaDTO> getPessoaPorDocumento(@PathVariable String documento) {
        // Buscar pessoa pelo documento
        try {
            Optional<Pessoa> pessoa = pessoaRepository.findByDocumentosNumero(documento);
                    
            return pessoa.map(p -> ResponseEntity.ok(pessoaMapper.toDTO(p)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}