package com.api.apartamentos.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.api.apartamentos.exceptions.RecursoNoEncontradoException;
import com.api.apartamentos.models.ApartamentoModel;
import com.api.apartamentos.models.ClienteModel;
import com.api.apartamentos.repositories.IApartamentoRepository;
import com.api.apartamentos.repositories.IClienteRepository;
import com.api.apartamentos.repositories.IReservacionRepository;

@Service
public class ApartamentoService {

    private static final Set<String> TIPOS = Set.of("HABITACION", "APARTAMENTO", "PISO");
    private static final Set<String> ESTADOS = Set.of("ACTIVO", "INACTIVO");

    private final IApartamentoRepository apartamentoRepository;
    private final IClienteRepository clienteRepository;
    private final IReservacionRepository reservacionRepository;

    public ApartamentoService(IApartamentoRepository apartamentoRepository,
                              IClienteRepository clienteRepository,
                              IReservacionRepository reservacionRepository) {
        this.apartamentoRepository = apartamentoRepository;
        this.clienteRepository = clienteRepository;
        this.reservacionRepository = reservacionRepository;
    }

    public List<ApartamentoModel> listar() {
        return apartamentoRepository.findAll();
    }

    public ApartamentoModel obtener(Long id) {
        return apartamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un apartamento con id " + id));
    }

    public List<ApartamentoModel> disponibles(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new IllegalArgumentException(
                    "Indica 'inicio' y 'fin' (yyyy-MM-dd) y que 'fin' sea posterior a 'inicio'.");
        }
        return apartamentoRepository.buscarDisponibles(inicio, fin);
    }

    public ApartamentoModel crear(ApartamentoModel apartamento) {
        apartamento.setId(null);
        validar(apartamento);
        return apartamentoRepository.save(apartamento);
    }

    public ApartamentoModel actualizar(Long id, ApartamentoModel datos) {
        ApartamentoModel existente = obtener(id);
        validar(datos);
        existente.setTipo(datos.getTipo());
        existente.setTitulo(datos.getTitulo());
        existente.setDireccion(datos.getDireccion());
        existente.setDescripcion(datos.getDescripcion());
        existente.setHabitaciones(datos.getHabitaciones());
        existente.setBanos(datos.getBanos());
        existente.setMetrosCuadrados(datos.getMetrosCuadrados());
        existente.setPrecioPorDia(datos.getPrecioPorDia());
        existente.setEstado(datos.getEstado());
        existente.setPropietario(datos.getPropietario());
        return apartamentoRepository.save(existente);
    }

    public void eliminar(Long id) {
        obtener(id);
        if (reservacionRepository.existsByApartamento_Id(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar el apartamento porque tiene reservaciones. Puedes marcarlo como INACTIVO.");
        }
        apartamentoRepository.deleteById(id);
    }

    // Valida los datos y deja en el objeto los valores ya normalizados
    private void validar(ApartamentoModel a) {
        requerido(a.getTitulo(), "titulo");
        requerido(a.getDireccion(), "direccion");
        requerido(a.getTipo(), "tipo");

        String tipo = a.getTipo().trim().toUpperCase();
        if (!TIPOS.contains(tipo)) {
            throw new IllegalArgumentException("Valor inválido para 'tipo'. Permitidos: " + TIPOS);
        }
        a.setTipo(tipo);

        String estado = (a.getEstado() == null || a.getEstado().isBlank()) ? "ACTIVO" : a.getEstado().trim().toUpperCase();
        if (!ESTADOS.contains(estado)) {
            throw new IllegalArgumentException("Valor inválido para 'estado'. Permitidos: " + ESTADOS);
        }
        a.setEstado(estado);

        if (a.getPrecioPorDia() == null || a.getPrecioPorDia().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El 'precioPorDia' es obligatorio y debe ser mayor a 0.");
        }
        noNegativo(a.getHabitaciones(), "habitaciones");
        noNegativo(a.getBanos(), "banos");
        noNegativo(a.getMetrosCuadrados(), "metrosCuadrados");

        if (a.getPropietario() == null || a.getPropietario().getId() == null) {
            throw new IllegalArgumentException("Se requiere 'propietario.id'.");
        }
        Long propietarioId = a.getPropietario().getId();
        ClienteModel propietario = clienteRepository.findById(propietarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un cliente con id " + propietarioId));
        if (!"PROPIETARIO".equals(propietario.getTipo())) {
            throw new IllegalArgumentException("El cliente indicado no es de tipo PROPIETARIO.");
        }
        a.setPropietario(propietario);
    }

    private void noNegativo(Integer valor, String campo) {
        if (valor != null && valor < 0) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede ser negativo.");
        }
    }

    private void requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        }
    }
}
