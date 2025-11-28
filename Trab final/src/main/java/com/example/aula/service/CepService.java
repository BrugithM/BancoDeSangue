package com.example.aula.service;

import com.example.aula.model.Endereco;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CepService {

    public Endereco consultarCep(String cep) {
        try {
            String url = "https://viacep.com.br/ws/" + cep + "/json/";
            RestTemplate restTemplate = new RestTemplate();
            ViaCepResponse response = restTemplate.getForObject(url, ViaCepResponse.class);

            if (response != null && !response.isErro()) {
                return new Endereco(
                        response.getCep(),
                        response.getLogradouro(),
                        "",
                        response.getComplemento(),
                        response.getBairro(),
                        response.getLocalidade(),
                        response.getUf(),
                        "Brasil"
                );
            }
        } catch (Exception e) {
            System.err.println("Erro ao consultar CEP: " + e.getMessage());
        }
        return null;
    }

    @Data
    private static class ViaCepResponse {
        private String cep;
        private String logradouro;
        private String complemento;
        private String bairro;
        private String localidade;
        private String uf;
        private boolean erro;
    }
}