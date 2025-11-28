package com.example.aula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.aula.model.Pessoa;
import com.example.aula.repository.PessoaRepository;
import com.example.aula.service.PessoaService;

@ExtendWith(MockitoExtension.class)
class PessoaServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private PessoaService pessoaService;

    @Test
    void testSavePessoa() {
        Pessoa pessoa = new Pessoa();
        pessoa.setNome("João Teste");
        
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        Pessoa resultado = pessoaService.save(pessoa);

        assertEquals("João Teste", resultado.getNome());
        verify(pessoaRepository).save(pessoa);
    }
}