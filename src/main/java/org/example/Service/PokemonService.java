package org.example.Service;

import org.example.Domain.Jogador;
import org.example.Repository.JogadorRepository;
import org.example.dto.PokemonDTO;
import org.example.Domain.Pokemon;
import org.example.Repository.PokemonRepository;
import org.example.dto.PokemonEscolhaDTO;
import org.example.dto.PokemonSelecionadoDTO;
import org.example.exceptions.JogadorNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PokemonService {
    @Autowired
    PokeApiService pokeApiService;

    @Autowired
    PokemonRepository pokemonRepository;

    private final Random random = new Random();
    @Autowired
    private JogadorRepository jogadorRepository;

    public List<PokemonDTO> gerarPokemonsParaEscolha() {
        List<PokemonDTO> pokemonsEscolha = new ArrayList<>();

        while (pokemonsEscolha.size() < 8) {
            int randomPokemonId = random.nextInt(898) + 1;
            PokemonDTO response = pokeApiService.getPokemonNome(String.valueOf(randomPokemonId));

            boolean pokemonJaExiste = pokemonsEscolha.stream()
                    .anyMatch(pokemonEscolha -> pokemonEscolha.getNome().equalsIgnoreCase(response.getNome()));
            if (pokemonJaExiste) {
                continue;
            }
            PokemonDTO pokemonEscolhaDTO = new PokemonDTO(
                    response.getNome(),
                    response.getTipo(),
                    response.getNivel(),
                    response.getHp(),
                    response.getAtaque(),
                    response.getDefesa()
            );

            pokemonsEscolha.add(pokemonEscolhaDTO);
        }

        return pokemonsEscolha;
    }

    public void cadastrarPokemonsEscolhidos(PokemonSelecionadoDTO dto) {
        if (dto.getPokemonEscolhidos().size() != 2) {
            throw new IllegalArgumentException("Você deve escolher exatamente 2 Pokémon.");
        }

        for (PokemonEscolhaDTO escolhido : dto.getPokemonEscolhidos()) {
            Long jogadorId = escolhido.getJogadorId();

            Jogador jogador = jogadorRepository.findById(jogadorId)
                    .orElseThrow(() -> new JogadorNotFoundException("Jogador com ID " + jogadorId + "não existe."));

            Long quantidadeExistente = pokemonRepository.countByJogadorId_JogadorId(jogadorId);
            if (quantidadeExistente >= 2) {
                throw new IllegalArgumentException("Jogador com ID " + jogadorId + " já possuí dois pokémon");
            }

            Pokemon pokemon = new Pokemon();
            BeanUtils.copyProperties(escolhido, pokemon);
            pokemon.setJogadorId(jogador);
            pokemonRepository.save(pokemon);
        }
    }

    private Jogador getJogadorLogado() {
        return (Jogador) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}



