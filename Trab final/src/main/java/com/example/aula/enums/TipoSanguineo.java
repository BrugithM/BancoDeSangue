package com.example.aula.enums;

public enum TipoSanguineo {
    A_POSITIVO("A+"),
    A_NEGATIVO("A-"),
    B_POSITIVO("B+"),
    B_NEGATIVO("B-"),
    AB_POSITIVO("AB+"),
    AB_NEGATIVO("AB-"),
    O_POSITIVO("O+"),
    O_NEGATIVO("O-");

    private final String simbolo;

    TipoSanguineo(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public static TipoSanguineo fromString(String texto) {
        for (TipoSanguineo tipo : TipoSanguineo.values()) {
            if (tipo.simbolo.equalsIgnoreCase(texto)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo sanguíneo inválido: " + texto);
    }

    @Override
    public String toString() {
        return simbolo;
    }
}