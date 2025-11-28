package com.example.aula.repositorySQLPuro;

import com.example.aula.model.Pessoa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepositoryPessoaSQLPuro {

    // Você precisará configurar a conexão com o banco de dados
    private Connection getConnection() throws SQLException {
        // Exemplo com MySQL - ajuste para seu banco de dados
        String url = "jdbc:mysql://localhost:3306/seu_banco";
        String user = "usuario";
        String password = "senha";
        return DriverManager.getConnection(url, user, password);
    }

    public Pessoa inserirPessoa(Pessoa pessoa) {
        String sql = "INSERT INTO pessoa (nome, tipo_sanguineo, outros_campos) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, pessoa.getNome());
            stmt.setString(2, pessoa.getTipoSanguineo().toString());
            // stmt.setXXX(3, pessoa.getOutroCampo()); // ajuste conforme seus campos

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        pessoa.setId(generatedKeys.getLong(1));
                    }
                }
            }

            return pessoa;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir pessoa", e);
        }
    }

    public boolean deletePessoa(Long id) {
        String sql = "DELETE FROM pessoa WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar pessoa", e);
        }
    }

    public List<Pessoa> findPessoaNome(String nome) {
        String sql = "SELECT * FROM pessoa WHERE nome LIKE ?";
        List<Pessoa> pessoas = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nome + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Pessoa pessoa = mapResultSetToPessoa(rs);
                pessoas.add(pessoa);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pessoa por nome", e);
        }

        return pessoas;
    }

    public List<Pessoa> findPessoaTipoSanguineo(String tipoSanguineo) {
        String sql = "SELECT * FROM pessoa WHERE tipo_sanguineo = ?";
        List<Pessoa> pessoas = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipoSanguineo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Pessoa pessoa = mapResultSetToPessoa(rs);
                pessoas.add(pessoa);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pessoa por tipo sanguíneo", e);
        }

        return pessoas;
    }

    private Pessoa mapResultSetToPessoa(ResultSet rs) throws SQLException {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(rs.getLong("id"));
        pessoa.setNome(rs.getString("nome"));
        // pessoa.setTipoSanguineo(TipoSanguineo.valueOf(rs.getString("tipo_sanguineo")));
        // Configure os outros campos conforme sua model
        return pessoa;
    }
}