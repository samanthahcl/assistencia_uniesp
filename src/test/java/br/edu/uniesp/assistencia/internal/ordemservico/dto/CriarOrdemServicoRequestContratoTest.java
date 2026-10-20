package br.edu.uniesp.assistencia.internal.ordemservico.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Contrato dos DTOs de abertura de OS")
class CriarOrdemServicoRequestContratoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void criarValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fechar() {
        factory.close();
    }

    private Set<String> camposInvalidos(CriarOrdemServicoRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("request válido não tem violações")
    void valido() {
        assertThat(camposInvalidos(new CriarOrdemServicoRequest(1L, 2L, "Não liga"))).isEmpty();
    }

    @Test
    @DisplayName("ids nulos e descrição nula/vazia/em branco são inválidos")
    void invalidos() {
        assertThat(camposInvalidos(new CriarOrdemServicoRequest(null, null, null)))
                .containsExactlyInAnyOrder("clienteId", "equipamentoId", "descricaoProblema");
        assertThat(camposInvalidos(new CriarOrdemServicoRequest(1L, 2L, ""))).containsExactly("descricaoProblema");
        assertThat(camposInvalidos(new CriarOrdemServicoRequest(1L, 2L, "     "))).containsExactly("descricaoProblema");
    }

    @Test
    @DisplayName("descrição acima de 2000 caracteres é inválida")
    void descricaoMuitoLonga() {
        assertThat(camposInvalidos(new CriarOrdemServicoRequest(1L, 2L, "x".repeat(2001))))
                .containsExactly("descricaoProblema");
    }

    @Test
    @DisplayName("o request só expõe clienteId, equipamentoId e descricaoProblema (sem status/data/protocolo)")
    void requestNaoExpoeCamposControladosPeloSistema() {
        assertThat(Arrays.stream(CriarOrdemServicoRequest.class.getRecordComponents()).map(RecordComponent::getName))
                .containsExactly("clienteId", "equipamentoId", "descricaoProblema");
    }

    @Test
    @DisplayName("o response é um record com id, numeroProtocolo, status e dataAbertura (não é entidade JPA)")
    void responseEhRecordSemEntidade() {
        assertThat(CriarOrdemServicoResponse.class.isRecord()).isTrue();
        assertThat(Arrays.stream(CriarOrdemServicoResponse.class.getRecordComponents()).map(RecordComponent::getName))
                .containsExactly("id", "numeroProtocolo", "status", "dataAbertura");
        assertThat(CriarOrdemServicoResponse.class.isAnnotationPresent(jakarta.persistence.Entity.class)).isFalse();
    }
}
