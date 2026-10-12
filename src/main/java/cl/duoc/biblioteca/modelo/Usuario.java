package cl.duoc.biblioteca.modelo;

public class Usuario extends Persona {

    private String contrasena;
    private Rol rol;

    public Usuario() {
    }

    public Usuario(int id, String nombre, String rut, String correo, String contrasena, Rol rol) {
        super(id, nombre, rut, correo);
        this.contrasena = contrasena;
        this.rol = rol;
    }

    @Override
    public String obtenerRolDescriptivo() {
        if (rol == Rol.BIBLIOTECARIO) {
            return "Bibliotecario";
        }
        return "Estudiante";
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
