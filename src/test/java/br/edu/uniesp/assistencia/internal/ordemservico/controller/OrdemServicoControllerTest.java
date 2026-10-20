package br.edu.uniesp.assistencia.internal.ordemservico.controller;

import br.edu.uniesp.assistencia.config.exception.GlobalExceptionHandler;
import br.edu.uniesp.assistencia.internal.ordemservico.dto.CriarOrdemServicoRequest;
import br.edu.uniesp.assistencia.internal.ordemservico.dto.CriarOrdemServicoResponse;
import br.edu.uniesp.assistencia.internal.ordemservico.entity.StatusOrdemServico;
import br.edu.uniesp.assistencia.internal.ordemservico.service.OrdemServicoService;
import br.edu.uniesp.assistencia.shared.exception.RecursoNaoEncontradoException;
import br.edu.uniesp.assistencia.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("POST /ordens")
class OrdemServicoControllerTest {

    private static final String VALIDO = """
            {"clienteId": 1, "equipamentoId": 2, "descricaoProblema": "Notebook não liga após queda de energia elétrica."}
            """;

    @Mock OrdemServicoService ordemServicoService;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OrdemServicoController(ordemServicoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------- Cenário 1 ----------
    @Test
    @DisplayName("Cenário 1: 201 com id, protocolo, status RECEBIDA e dataAbertura")
    void criaComSucesso() throws Exception {
        when(ordemServicoService.abrirOrdemServico(any(CriarOrdemServicoRequest.class)))
                .thenReturn(new CriarOrdemServicoResponse(1L, "OS-20261015-9F2B", StatusOrdemServico.RECEBIDA,
                        LocalDateTime.of(2026, 10, 15, 20, 30)));

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.numeroProtocolo").value("OS-20261015-9F2B"))
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.dataAbertura").exists());
    }

    @Test
    @DisplayName("payload com status/dataAbertura/protocolo não influencia a OS: o serviço só recebe os 3 campos")
    void clienteNaoDefineCamposDoSistema() throws Exception {
        when(ordemServicoService.abrirOrdemServico(any(CriarOrdemServicoRequest.class)))
                .thenReturn(new CriarOrdemServicoResponse(1L, "OS-20261015-9F2B", StatusOrdemServico.RECEBIDA,
                        LocalDateTime.now()));

        String malicioso = """
                {"clienteId": 1, "equipamentoId": 2, "descricaoProblema": "Não liga",
                 "status": "CONCLUIDA", "dataAbertura": "2000-01-01T00:00:00", "numeroProtocolo": "OS-00000000-HACK"}
                """;

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(malicioso))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.numeroProtocolo").value("OS-20261015-9F2B"));

        verify(ordemServicoService).abrirOrdemServico(new CriarOrdemServicoRequest(1L, 2L, "Não liga"));
    }

    // ---------- Cenário 2 ----------
    @Test
    @DisplayName("Cenário 2: ids ausentes/nulos -> 400 ProblemDetail com os campos inválidos; serviço não é chamado")
    void idsInvalidos() throws Exception {
        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\": null, \"descricaoProblema\": \"Não liga\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Dados de requisição inválidos"))
                .andExpect(content().string(containsString("clienteId")))
                .andExpect(content().string(containsString("equipamentoId")));

        verifyNoInteractions(ordemServicoService);
    }

    @Test
    @DisplayName("Cenário 2: descrição vazia -> 400")
    void descricaoVazia() throws Exception {
        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\": 1, \"equipamentoId\": 2, \"descricaoProblema\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("descricaoProblema")));

        verifyNoInteractions(ordemServicoService);
    }

    @Test
    @DisplayName("Cenário 2: descrição só com espaços -> 400")
    void descricaoSomenteEspacos() throws Exception {
        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\": 1, \"equipamentoId\": 2, \"descricaoProblema\": \"     \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("descricaoProblema")));

        verify(ordemServicoService, never()).abrirOrdemServico(any());
    }

    @Test
    @DisplayName("Cenário 2: corpo vazio ou JSON malformado -> 400 sem expor detalhes internos")
    void corpoMalformado() throws Exception {
        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content("{ nao-e-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("at com."))));

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordemServicoService);
    }

    // ---------- Cenário 3 ----------
    @Test
    @DisplayName("Cenário 3: cliente/equipamento inexistente -> 404 ProblemDetail")
    void recursoInexistente() throws Exception {
        when(ordemServicoService.abrirOrdemServico(any(CriarOrdemServicoRequest.class)))
                .thenThrow(new RecursoNaoEncontradoException("Cliente", 1L));

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Cliente não encontrado para o identificador: 1"));
    }

    // ---------- Cenário 4 ----------
    @Test
    @DisplayName("Cenário 4: cliente inativo -> 422 com a mensagem exata da RN01")
    void clienteInativo() throws Exception {
        when(ordemServicoService.abrirOrdemServico(any(CriarOrdemServicoRequest.class)))
                .thenThrow(new RegraNegocioException("Não é permitido abrir Ordem de Serviço para um cliente inativo."));

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.detail").value("Não é permitido abrir Ordem de Serviço para um cliente inativo."));
    }

    // ---------- Cenário 5 ----------
    @Test
    @DisplayName("Cenário 5: equipamento de outro cliente -> 422 com a mensagem exata da RN02")
    void equipamentoDeOutroCliente() throws Exception {
        when(ordemServicoService.abrirOrdemServico(any(CriarOrdemServicoRequest.class)))
                .thenThrow(new RegraNegocioException("O equipamento informado não pertence ao cliente indicado."));

        mockMvc.perform(post("/ordens").contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.detail").value("O equipamento informado não pertence ao cliente indicado."));
    }
}
