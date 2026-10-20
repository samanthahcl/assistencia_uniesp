package br.edu.uniesp.assistencia.internal.ordemservico.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * RN04: gera protocolos legíveis no formato {@code OS-yyyyMMdd-XXXX}, onde XXXX são 4 caracteres
 * alfanuméricos maiúsculos (36^4 = 1.679.616 combinações por dia).
 * <p>
 * Este componente apenas minimiza colisões. A garantia de unicidade é da constraint
 * {@code uk_ordens_servico_numero_protocolo} no banco; em caso de colisão o serviço gera outro protocolo.
 */
@Component
public class GeradorNumeroProtocolo {

    private static final String PREFIXO = "OS-";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final char[] ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final int TAMANHO_SUFIXO = 4;

    private final SecureRandom random = new SecureRandom();

    public String gerar() {
        StringBuilder sufixo = new StringBuilder(TAMANHO_SUFIXO);
        for (int i = 0; i < TAMANHO_SUFIXO; i++) {
            sufixo.append(ALFABETO[random.nextInt(ALFABETO.length)]);
        }
        return PREFIXO + LocalDate.now().format(FORMATO_DATA) + "-" + sufixo;
    }
}
