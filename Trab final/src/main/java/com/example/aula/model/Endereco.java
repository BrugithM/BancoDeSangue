package com.example.aula.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    @NotBlank(message = "O CEP é obrigatório")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve estar no formato 00000-000")
    private String cep;

    @NotBlank(message = "O logradouro é obrigatório")
    @Size(max = 200, message = "O logradouro deve ter no máximo 200 caracteres")
    private String logradouro;

    @NotBlank(message = "O número é obrigatório")
    @Size(max = 10, message = "O número deve ter no máximo 10 caracteres")
    private String numero;

    @Size(max = 100, message = "O complemento deve ter no máximo 100 caracteres")
    private String complemento;

    @NotBlank(message = "O bairro é obrigatório")
    @Size(max = 100, message = "O bairro deve ter no máximo 100 caracteres")
    private String bairro;

    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
    private String cidade;

    @NotBlank(message = "O estado é obrigatório")
    @Size(min = 2, max = 2, message = "O estado deve ter 2 caracteres (UF)")
    private String estado;

    @Size(max = 100, message = "O país deve ter no máximo 100 caracteres")
    private String pais = "Brasil";

    // Métodos utilitários
    public String getEnderecoCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append(logradouro).append(", ").append(numero);

        if (complemento != null && !complemento.trim().isEmpty()) {
            sb.append(" - ").append(complemento);
        }

        sb.append(" - ").append(bairro)
                .append(" - ").append(cidade)
                .append("/").append(estado)
                .append(" - CEP: ").append(cep);

        if (pais != null && !pais.equals("Brasil")) {
            sb.append(" - ").append(pais);
        }

        return sb.toString();
    }

    public boolean isEnderecoBrasileiro() {
        return "Brasil".equalsIgnoreCase(pais);
    }

    public String getCidadeEstado() {
        return cidade + "/" + estado;
    }
}