package com.example.aula.dto;

import com.example.aula.enums.StatusDoacao;
import com.example.aula.enums.TipoSanguineo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoacaoDTO {
    private Long id;

    @NotBlank(message = "O documento do doador é obrigatório")
    private String documentoDoador;

    private TipoSanguineo tipoSanguineo;

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser positiva")
    private Double quantidade;

    private LocalDateTime dataDoacao;
    private LocalDate validoAte;
    private StatusDoacao status;

    private String doadorNome;
    private String doadorTipoSanguineo;
}