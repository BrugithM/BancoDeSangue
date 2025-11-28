package com.example.aula.mapper;

import com.example.aula.dto.DoacaoDTO;
import com.example.aula.model.Doacao;
import com.example.aula.model.Pessoa;
import com.example.aula.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DoacaoMapper {

    @Autowired
    private PessoaRepository pessoaRepository;

    public DoacaoDTO toDTO(Doacao doacao) {
        if (doacao == null) return null;

        return DoacaoDTO.builder()
                .id(doacao.getId())
                .documentoDoador(doacao.getDoador() != null ? 
                        getDocumentoPrincipal(doacao.getDoador()) : null)
                .doadorNome(doacao.getDoador() != null ? doacao.getDoador().getNome() : null)
                .doadorTipoSanguineo(doacao.getDoador() != null && doacao.getDoador().getTipoSanguineo() != null ?
                        doacao.getDoador().getTipoSanguineo().getSimbolo() : null)
                .tipoSanguineo(doacao.getTipoSanguineo())
                .quantidade(doacao.getQuantidade())
                .dataDoacao(doacao.getDataDoacao())
                .validoAte(doacao.getValidoAte())
                .status(doacao.getStatus())
                .build();
    }

    public Doacao toEntity(DoacaoDTO dto) {
        if (dto == null) return null;

        Doacao doacao = new Doacao();
        doacao.setId(dto.getId());

        if (dto.getDocumentoDoador() != null && !dto.getDocumentoDoador().trim().isEmpty()) {
            Pessoa doador = findPessoaByDocumento(dto.getDocumentoDoador());
            doacao.setDoador(doador);

            // Usar o tipo sanguíneo do doador
            if (doador.getTipoSanguineo() != null) {
                doacao.setTipoSanguineo(doador.getTipoSanguineo());
            } else {
                throw new RuntimeException("Doador não possui tipo sanguíneo cadastrado");
            }
        } else {
            throw new RuntimeException("Documento do doador é obrigatório");
        }

        doacao.setQuantidade(dto.getQuantidade());
        doacao.setDataDoacao(dto.getDataDoacao());
        doacao.setValidoAte(dto.getValidoAte());
        doacao.setStatus(dto.getStatus() != null ? dto.getStatus() : com.example.aula.enums.StatusDoacao.DISPONIVEL);

        return doacao;
    }

    private String getDocumentoPrincipal(Pessoa pessoa) {
        if (pessoa.getDocumentos() != null && !pessoa.getDocumentos().isEmpty()) {
            // Priorizar CPF, depois RG, depois o primeiro documento disponível
            return pessoa.getDocumentos().stream()
                    .filter(doc -> "CPF".equalsIgnoreCase(doc.getTipo()))
                    .findFirst()
                    .orElseGet(() ->
                            pessoa.getDocumentos().stream()
                                    .filter(doc -> "RG".equalsIgnoreCase(doc.getTipo()))
                                    .findFirst()
                                    .orElse(pessoa.getDocumentos().get(0))
                    )
                    .getNumero();
        }
        return null;
    }

    private Pessoa findPessoaByDocumento(String documento) {
        return pessoaRepository.findByDocumentosNumero(documento)
                .orElseThrow(() -> new RuntimeException("Doador não encontrado com o documento: " + documento));
    }
}