package com.clinica.clinica.cuenta.projection;

import com.clinica.clinica.cuenta.dto.CargoAdicionalResponse;

import java.math.BigDecimal;
import java.util.List;

public sealed interface CuentaProjection permits CuentaResumen, CuentaConCargos, CuentaParaCaja {}

record CuentaResumen(Long id, String pacienteNombre, String nroHistoriaClinica, String estado, BigDecimal totalCargos) implements CuentaProjection {}

record CuentaConCargos(Long id, String pacienteNombre, List<CargoAdicionalResponse> cargos) implements CuentaProjection {}

record CuentaParaCaja(Long id, String pacienteNombre, String nroHistoriaClinica, BigDecimal totalCargos, boolean estaPagada) implements CuentaProjection {}
