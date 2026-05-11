package com.example.artistlan.Conector.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponseDTO {

    private Boolean login;
    private Boolean requires2FA;
    private String temporaryToken;
    private String token;
    private UsuariosDTO user;

    // Campos legacy en raiz para compatibilidad.
    private Integer idUsuario;
    private Integer id;
    private String usuario;
    private String correo;
    private String nombreCompleto;
    private String contrasena;
    private String rol;
    private String descripcion;
    private String fotoPerfil;
    private String telefono;
    private String redesSociales;
    private String fechaNacimiento;
    private String ubicacion;
    @SerializedName(value = "twoFactorEnabled", alternate = {"two_factor_enabled"})
    private Boolean twoFactorEnabled;

    public UsuariosDTO getEffectiveUser() {
        if (user != null) {
            user.setTwoFactorEnabled(Boolean.TRUE.equals(user.getTwoFactorEnabled()) || Boolean.TRUE.equals(getTwoFactorEnabled()));
            return user;
        }

        Integer effectiveId = idUsuario != null ? idUsuario : id;
        if (effectiveId == null) {
            return null;
        }

        UsuariosDTO legacyUser = new UsuariosDTO();
        legacyUser.setIdUsuario(effectiveId);
        legacyUser.setUsuario(usuario);
        legacyUser.setCorreo(correo);
        legacyUser.setNombreCompleto(nombreCompleto);
        legacyUser.setRol(rol);
        legacyUser.setDescripcion(descripcion);
        legacyUser.setFotoPerfil(fotoPerfil);
        legacyUser.setTelefono(telefono);
        legacyUser.setRedesSociales(redesSociales);
        legacyUser.setFechaNacimiento(fechaNacimiento);
        legacyUser.setUbicacion(ubicacion);
        legacyUser.setTwoFactorEnabled(getTwoFactorEnabled());
        return legacyUser;
    }

    public Boolean getLogin() {
        return login;
    }

    public void setLogin(Boolean login) {
        this.login = login;
    }

    public Boolean getRequires2FA() {
        return requires2FA;
    }

    public void setRequires2FA(Boolean requires2FA) {
        this.requires2FA = requires2FA;
    }

    public String getTemporaryToken() {
        return temporaryToken;
    }

    public void setTemporaryToken(String temporaryToken) {
        this.temporaryToken = temporaryToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UsuariosDTO getUser() {
        return user;
    }

    public void setUser(UsuariosDTO user) {
        this.user = user;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRedesSociales() {
        return redesSociales;
    }

    public void setRedesSociales(String redesSociales) {
        this.redesSociales = redesSociales;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Boolean getTwoFactorEnabled() {
        return Boolean.TRUE.equals(twoFactorEnabled);
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }
}
