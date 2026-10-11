package cl.duoc.mspedidos.service;

import cl.duoc.mspedidos.client.PagosClient;
import cl.duoc.mspedidos.exception.RecursoNoEncontradoException;
import cl.duoc.mspedidos.model.DetallePedido;
import cl.duoc.mspedidos.model.Pedido;
import cl.duoc.mspedidos.repository.DetallePedidoRepository;
import cl.duoc.mspedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final PagosClient pagosClient;

    public PedidoService(PedidoRepository pedidoRepository,
                         DetallePedidoRepository detallePedidoRepository,
                         PagosClient pagosClient) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.pagosClient = pagosClient;
    }

    public Pedido crear(Pedido pedido) {
        pedido.setId(null);
        pedido.setFecha(LocalDateTime.now());
        if (pedido.getEstado() == null || pedido.getEstado().isBlank()) {
            pedido.setEstado("PENDIENTE");
        }
        Pedido guardado = pedidoRepository.save(pedido);

        boolean pagoRegistrado = pagosClient.registrarPago(
                guardado.getId(), guardado.getTotal(), "TARJETA");
        if (!pagoRegistrado) {
            guardado.setEstado("PAGO_NO_REGISTRADO");
            guardado = pedidoRepository.save(guardado);
        }
        return guardado;
    }

    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> listarPorEstado(String estado) {
        return pedidoRepository.findByEstado(estado);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con id " + id));
    }

    public Pedido actualizar(Long id, Pedido datos) {
        Pedido existente = buscarPorId(id);
        existente.setTotal(datos.getTotal());
        if (datos.getEstado() != null && !datos.getEstado().isBlank()) {
            existente.setEstado(datos.getEstado());
        }
        return pedidoRepository.save(existente);
    }

    public void eliminar(Long id) {
        Pedido existente = buscarPorId(id);
        pedidoRepository.delete(existente);
    }

    public DetallePedido agregarDetalle(Long pedidoId, DetallePedido detalle) {
        Pedido pedido = buscarPorId(pedidoId);
        detalle.setId(null);
        detalle.setPedido(pedido);
        return detallePedidoRepository.save(detalle);
    }

    public List<DetallePedido> listarDetalles(Long pedidoId) {
        buscarPorId(pedidoId);
        return detallePedidoRepository.findByPedidoId(pedidoId);
    }
}