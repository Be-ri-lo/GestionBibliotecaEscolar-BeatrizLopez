package cl.duoc.biblioteca.modelo;

public enum Rol {
    BIBLIOTECARIO,
    ESTUDIANTE;

    public static Rol desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El rol está vacío.");
        }
        return Rol.valueOf(texto.trim().toUpperCase());
    }
}
