package com.example.artistlan.Conector.model;

import java.util.List;

public class PerfilPublicoArtistaDTO {
    private Integer idUsuario;
    private Integer idUsuarioConsulta;
    private Integer idUsuarioConsultado;
    private String usuario;
    private String nombreUsuario;
    private String nombreCompleto;
    private String fotoPerfil;
    private String descripcion;
    private String ubicacion;
    private String ubicacionPerfil;
    private String fechaNacimiento;
    private String redesSociales;
    private String redes;
    private String categoria;
    private String ocupacion;
    private List<String> categorias;
    private List<ObraDTO> obras;
    private List<ServicioDTO> servicios;

    public Integer getIdUsuario() { return idUsuario; }

    public Integer getIdUsuarioConsulta() {
        return idUsuarioConsulta != null ? idUsuarioConsulta : idUsuarioConsultado;
    }

    public String getUsuario() {
        if (usuario != null && !usuario.trim().isEmpty()) {
            return usuario;
        }
        return nombreUsuario;
    }

    public String getNombreCompleto() { return nombreCompleto; }

    public String getFotoPerfil() { return fotoPerfil; }

    public String getDescripcion() { return descripcion; }

    public String getUbicacion() {
        if (ubicacion != null && !ubicacion.trim().isEmpty()) {
            return ubicacion;
        }
        return ubicacionPerfil;
    }

    public String getFechaNacimiento() { return fechaNacimiento; }

    public String getRedesSociales() {
        if (redesSociales != null && !redesSociales.trim().isEmpty()) {
            return redesSociales;
        }
        return redes;
    }

    public String getOcupacion() {
        if (ocupacion != null && !ocupacion.trim().isEmpty()) {
            return ocupacion;
        }
        if (categoria != null && !categoria.trim().isEmpty()) {
            return categoria;
        }
        if (categorias != null) {
            for (String item : categorias) {
                if (item != null && !item.trim().isEmpty()) {
                    return item.trim();
                }
            }
        }
        return null;
    }

    public List<String> getCategorias() { return categorias; }

    public List<ObraDTO> getObras() { return obras; }

    public List<ServicioDTO> getServicios() { return servicios; }
}