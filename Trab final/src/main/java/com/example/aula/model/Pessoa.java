package com.example.aula.model;

import com.example.aula.enums.TipoSanguineo;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pessoas")
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @Valid // Adiciona validação cascata para o objeto embutido
    @Embedded
    @NotNull(message = "O endereço é obrigatório")
    private Endereco endereco;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sanguineo", length = 20)
    private TipoSanguineo tipoSanguineo;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "pessoa_documentos",
            joinColumns = @JoinColumn(name = "pessoa_id"),
            foreignKey = @ForeignKey(name = "fk_documentos_pessoa"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"pessoa_id", "tipo"})
    )
    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
    private List<@Valid Documento> documentos = new ArrayList<>();

    @Valid
    @Embedded
    @NotNull(message = "A filiação é obrigatória")
    private Filiacao filiacao;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "pessoa_contatos",
            joinColumns = @JoinColumn(name = "pessoa_id"),
            foreignKey = @ForeignKey(name = "fk_contatos_pessoa")
    )
    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
    private List<@Valid Contato> contatos = new ArrayList<>();

    // Construtores
    public Pessoa() {}

    public Pessoa(String nome, Endereco endereco, TipoSanguineo tipoSanguineo, Filiacao filiacao) {
        this.nome = nome;
        this.endereco = endereco;
        this.tipoSanguineo = tipoSanguineo;
        this.filiacao = filiacao;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public TipoSanguineo getTipoSanguineo() { return tipoSanguineo; }
    public void setTipoSanguineo(TipoSanguineo tipoSanguineo) { this.tipoSanguineo = tipoSanguineo; }

    public List<Documento> getDocumentos() { return documentos; }
    public void setDocumentos(List<Documento> documentos) { this.documentos = documentos; }

    public Filiacao getFiliacao() { return filiacao; }
    public void setFiliacao(Filiacao filiacao) { this.filiacao = filiacao; }

    public List<Contato> getContatos() { return contatos; }
    public void setContatos(List<Contato> contatos) { this.contatos = contatos; }

    // Métodos utilitários para manipular as listas
    public void adicionarDocumento(Documento documento) {
        if (this.documentos == null) {
            this.documentos = new ArrayList<>();
        }
        this.documentos.add(documento);
    }

    public void removerDocumento(Documento documento) {
        if (this.documentos != null) {
            this.documentos.remove(documento);
        }
    }

    public void adicionarContato(Contato contato) {
        if (this.contatos == null) {
            this.contatos = new ArrayList<>();
        }
        this.contatos.add(contato);
    }

    public void removerContato(Contato contato) {
        if (this.contatos != null) {
            this.contatos.remove(contato);
        }
    }

    public Documento buscarDocumentoPorTipo(String tipo) {
        if (this.documentos == null) return null;

        return this.documentos.stream()
                .filter(doc -> doc.getTipo().equalsIgnoreCase(tipo))
                .findFirst()
                .orElse(null);
    }

    public Contato buscarContatoPorTipo(String tipo) {
        if (this.contatos == null) return null;

        return this.contatos.stream()
                .filter(contato -> contato.getTipo().equalsIgnoreCase(tipo))
                .findFirst()
                .orElse(null);
    }

    // toString, equals e hashCode
    @Override
    public String toString() {
        return "Pessoa{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", tipoSanguineo=" + tipoSanguineo +
                ", cidade='" + (endereco != null ? endereco.getCidade() : "N/A") + '\'' +
                ", estado='" + (endereco != null ? endereco.getEstado() : "N/A") + '\'' +
                '}';
    }

    public String getLocalizacao() {
        if (endereco != null) {
            return endereco.getCidadeEstado();
        }
        return "Localização não informada";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pessoa pessoa = (Pessoa) o;

        if (id != null ? !id.equals(pessoa.id) : pessoa.id != null) return false;
        return nome != null ? nome.equals(pessoa.nome) : pessoa.nome == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (nome != null ? nome.hashCode() : 0);
        return result;
    }
}