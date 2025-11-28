package com.example.aula.mapper;

import com.example.aula.dto.PessoaDTO;
import com.example.aula.model.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class PessoaMapper {
    
    public PessoaDTO toDTO(Pessoa pessoa) {
        if (pessoa == null) return null;
        
        return PessoaDTO.builder()
                .id(pessoa.getId())
                .nome(pessoa.getNome())
                .endereco(pessoa.getEndereco())
                .tipoSanguineo(pessoa.getTipoSanguineo())
                .documentos(pessoa.getDocumentos())
                .filiacao(pessoa.getFiliacao())
                .contatos(pessoa.getContatos())
                .build();
    }
    
    public Pessoa toEntity(PessoaDTO dto) {
        if (dto == null) return null;
        
        Pessoa pessoa = new Pessoa();
        pessoa.setId(dto.getId());
        pessoa.setNome(dto.getNome());
        pessoa.setEndereco(dto.getEndereco());
        pessoa.setTipoSanguineo(dto.getTipoSanguineo());
        pessoa.setDocumentos(dto.getDocumentos());
        pessoa.setFiliacao(dto.getFiliacao());
        pessoa.setContatos(dto.getContatos());
        
        return pessoa;
    }
}