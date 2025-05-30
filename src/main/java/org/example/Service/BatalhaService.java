package org.example.Service;

import org.example.Domain.Batalha;
import org.example.Domain.Jogador;
import org.example.Domain.Pokemon;
import org.example.Repository.BatalhaRepository;
import org.example.dto.BatalhaDTO;
import org.example.dto.BatalhaVencedorDTO;
import org.example.dto.JogadorBatalhaDTO;
import org.example.dto.PokemonDTO;
import org.example.exceptions.BatalhaDuplicadoException;
import org.example.exceptions.BatalhaNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BatalhaService {
    @Autowired
    BatalhaRepository batalhaRepository;

    public BatalhaDTO getBatalha(Long id) {
        Batalha batalha = batalhaRepository.findById(id).orElseThrow(()-> new BatalhaNotFoundException("Batalha não existe."));

        JogadorBatalhaDTO jogadorBatalhaDTO = montarJogadorDTO(batalha.getJogador1());
        JogadorBatalhaDTO jogadorBatalhaDTO2 = montarJogadorDTO(batalha.getJogador2());

        return new BatalhaDTO(
                jogadorBatalhaDTO,
                jogadorBatalhaDTO2,
                LocalDate.now(),
                batalha.getEstado()
        );
    }



    public BatalhaDTO cadastrarBatalha(Batalha batalha,Long idBatalha) {
        batalhaRepository.findById(idBatalha)
                .ifPresent(batalhaExistente -> {
                    throw new BatalhaDuplicadoException("Batalha já cadastrada com este "+ idBatalha +" .");
                });

        Batalha novaBatalha = new Batalha(
                batalha.getJogador1(),
                batalha.getJogador2(),
                batalha.getEstado(),
                batalha.getDataCriacao()
        );
        batalhaRepository.save(novaBatalha);

        JogadorBatalhaDTO jogador1DTO = montarJogadorDTO(batalha.getJogador1());
        JogadorBatalhaDTO jogador2DTO = montarJogadorDTO(batalha.getJogador2());

        return new BatalhaDTO(jogador1DTO, jogador2DTO, novaBatalha.getDataCriacao(), novaBatalha.getEstado());
    }

    public BatalhaVencedorDTO getBatalhaVencedor(Long id) {
        Batalha batalha = batalhaRepository.findById(id)
                .orElseThrow(() -> new BatalhaNotFoundException("Batalha com " + id + " não existe."));

        List<Pokemon> pokemonsJogador1 = batalha.getJogador1().getPokemon();
        List<Pokemon> pokemonsJogador2 = batalha.getJogador2().getPokemon();

        if (pokemonsJogador1.size() < 2 || pokemonsJogador2.size() < 2) {
            throw new IllegalArgumentException("Ambos os jogadores devem ter pelo menos 2 Pokémon.");
        }

        int derrotasJogador1 = contarDerrotas(pokemonsJogador1, pokemonsJogador2);
        int derrotasJogador2 = contarDerrotas(pokemonsJogador2, pokemonsJogador1);

        String vencedor = determinarVencedor(batalha, derrotasJogador1, derrotasJogador2);

        return new BatalhaVencedorDTO(vencedor);
    }

    public void deletarBatalha(Long id) {
        if (batalhaRepository.existsById(id)) {
            batalhaRepository.deleteById(id);
        } else {
            throw new BatalhaNotFoundException("Batalha não existe.");
        }

    }

    public JogadorBatalhaDTO montarJogadorDTO(Jogador jogador) {
        List<PokemonDTO> pokemons = jogador.getPokemon().
                stream().
                map(pokemon ->
                        new PokemonDTO(pokemon.getNome(),pokemon.getTipo(), pokemon.getNivel(), pokemon.getHp(), pokemon.getAtaque(), pokemon.getDefesa())).
                collect(Collectors.toList());

        return new JogadorBatalhaDTO(pokemons, jogador.getNome());
    }

    public int simularLutar(Pokemon pokemon1, Pokemon pokemon2) {
        int hp1 = pokemon1.getHp();
        int hp2 = pokemon2.getHp();

        int dano1 = Math.max(1, pokemon1.getAtaque() - pokemon2.getDefesa());
        int dano2 = Math.max(1, pokemon2.getAtaque() - pokemon1.getDefesa());

        while (hp1 > 0 && hp2 > 0) {
            hp2 -= dano1;
            if (hp2 <= 0) break;

            hp1 -= dano2;
        }

        if (hp1 > 0) return 1;
        else if (hp2 > 0) return 2;
        else return 0;
    }

    private int contarDerrotas(List<Pokemon> meusPokemons, List<Pokemon> pokemonsOponentes) {
        int derrotas = 0;
        for (int contador = 0; contador < 2; contador++) {
            int resultado = simularLutar(meusPokemons.get(contador), pokemonsOponentes.get(contador));
            if (resultado == 2) {
                derrotas++;
            }
        }
        return derrotas;
    }

    private String determinarVencedor(Batalha batalha, int derrotasJogador1, int derrotasJogador2) {
        if (derrotasJogador1 == 2) {
            return batalha.getJogador2().getNome();
        } else if (derrotasJogador2 == 2) {
            return batalha.getJogador1().getNome();
        }
        return "Empate";
    }

}
