package com.example.aula.model;

import com.example.aula.enums.StatusDoacao;
import com.example.aula.enums.TipoSanguineo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "doacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doador_id", nullable = false)
    private Pessoa doador;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sanguineo", nullable = false, length = 20)
    private TipoSanguineo tipoSanguineo;

    @Column(nullable = false)
    private Double quantidade;

    @Column(name = "data_doacao", nullable = false)
    private LocalDateTime dataDoacao;

    @Column(name = "valido_ate")
    private LocalDate validoAte;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusDoacao status = StatusDoacao.DISPONIVEL;

    @PrePersist
    public void prePersist() {
        if (dataDoacao == null) {
            dataDoacao = LocalDateTime.now();
        }
        if (validoAte == null) {
            validoAte = LocalDate.now().plusDays(42);
        }
    }
}