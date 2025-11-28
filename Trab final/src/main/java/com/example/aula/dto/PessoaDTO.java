package com.example.aula.dto;

import com.example.aula.enums.TipoSanguineo;
import com.example.aula.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PessoaDTO {
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Valid
    @NotNull(message = "O endereço é obrigatório")
    private Endereco endereco;

    private TipoSanguineo tipoSanguineo;

    @Builder.Default
    private List<@Valid Contato> contatos = new ArrayList<>();

    @Builder.Default
    private List<@Valid Documento> documentos = new ArrayList<>();

    @Valid
    @NotNull(message = "A filiação é obrigatória")
    private Filiacao filiacao;

    public String getEnderecoCompleto() {
        return endereco != null ? endereco.getEnderecoCompleto() : null;
    }

    public String getCidadeEstado() {
        return endereco != null ? endereco.getCidadeEstado() : null;
    }
}