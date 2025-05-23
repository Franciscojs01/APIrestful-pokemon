package org.example.exceptions;

public class PokemonDuplicadoException extends RuntimeException {
    public PokemonDuplicadoException(String nome) {
        super("O pokémon " + nome + " Já foi cadastrado!");
    }
}
