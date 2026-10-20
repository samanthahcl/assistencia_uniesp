package br.edu.uniesp.assistencia.internal.ordemservico.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RN04 - formato do protocolo")
class GeradorNumeroProtocoloTest {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final GeradorNumeroProtocolo gerador = new GeradorNumeroProtocolo();

    @Test
    @DisplayName("gera OS-yyyyMMdd-XXXX com a data do dia e sufixo alfanumérico maiúsculo de 4 caracteres")
    void respeitaFormato() {
        String antes = LocalDate.now().format(DATA);
        for (int i = 0; i < 500; i++) {
            String protocolo = gerador.gerar();
            String depois = LocalDate.now().format(DATA);

            assertThat(protocolo).matches("^OS-\\d{8}-[A-Z0-9]{4}$");
            String data = protocolo.substring(3, 11);
            assertThat(data).isIn(antes, depois); // tolera virada de dia durante o teste
        }
    }

    @Test
    @DisplayName("o sufixo não é fixo (usa aleatoriedade)")
    void sufixoVaria() {
        long distintos = java.util.stream.IntStream.range(0, 50)
                .mapToObj(i -> gerador.gerar())
                .distinct()
                .count();

        assertThat(distintos).isGreaterThan(1);
    }
}
