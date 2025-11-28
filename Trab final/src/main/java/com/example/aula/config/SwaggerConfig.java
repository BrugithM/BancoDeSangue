package com.example.aula.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Banco de Sangue - Gestão Completa")
                        .version("2.0")
                        .description("""
                            ## 🩸 API REST para Sistema de Banco de Sangue
                            
                            ### 📋 Funcionalidades Principais:
                            
                            #### 👥 Gestão de Pessoas
                            - Cadastro completo de pessoas com documentos, contatos e filiação
                            - Busca por nome e tipo sanguíneo
                            - CRUD completo de pessoas
                            
                            #### 💉 Gestão de Doações
                            - Registro de doações usando **documento do doador** (CPF/RG)
                            - Controle de status das doações (DISPONIVEL, UTILIZADO, VENCIDO)
                            - Busca de doações por documento do doador
                            - Verificação de compatibilidade do doador
                            
                            #### 📊 Gestão de Estoque e Relatórios
                            - Controle de estoque por tipo sanguíneo
                            - Estatísticas gerais do banco de sangue
                            - Alertas de estoque baixo
                            - Verificação de compatibilidade sanguínea
                            - Processamento automático de doações vencidas
                            
                            ### 🩺 Tipos Sanguíneos Suportados:
                            - **A+**, **A-**, **B+**, **B-**, **AB+**, **AB-**, **O+**, **O-**
                            
                            ### 🔄 Fluxo de Trabalho:
                            1. **Cadastrar Pessoa** → POST `/api/pessoas`
                            2. **Registrar Doação** → POST `/api/doacoes` (usando documento)
                            3. **Processar Estoque** → POST `/api/banco-sangue/doacoes/{id}/adicionar-estoque`
                            4. **Consultar Estoque** → GET `/api/banco-sangue/estoque`
                            
                            ### 📝 Exemplo de Cadastro de Doação:
                            ```json
                            {
                              "documentoDoador": "123.456.789-00",
                              "quantidade": 450.0
                            }
                            ```
                            *O tipo sanguíneo é obtido automaticamente do cadastro da pessoa!*
                            
                            ### ⚠️ Regras de Negócio:
                            - Sangue tem validade de 42 dias
                            - Doações são convertidas em bolsas (450ml = 1 bolsa)
                            - Verificação automática de compatibilidade
                            - Alertas para estoque abaixo do mínimo
                            """)
                        .contact(new Contact()
                                .name("Suporte Técnico - Banco de Sangue")
                                .email("suporte@bancodesangue.com")
                                .url("https://www.bancodesangue.com.br"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("🛠️ Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.bancodesangue.com")
                                .description("🚀 Servidor de Produção")
                ));
    }
}