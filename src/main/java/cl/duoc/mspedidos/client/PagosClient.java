package cl.duoc.mspedidos.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PagosClient {

    private final RestClient pagosRestClient;

    public PagosClient(RestClient pagosRestClient) {
        this.pagosRestClient = pagosRestClient;
    }

    public boolean registrarPago(Long pedidoId, BigDecimal monto, String metodo) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("pedidoId", pedidoId);
        cuerpo.put("monto", monto);
        cuerpo.put("metodo", metodo);
        try {
            pagosRestClient.post()
                    .uri("/api/pagos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException ex) {
            return false;
        }
    }
}