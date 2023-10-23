package com.example.demo;

import java.util.*;

import com.mashape.unirest.http.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

@Controller
public class CartesController {

    @Autowired
    private PokemonService pokemonService;

    @GetMapping("/mescartes")
    public String showCartes(Model model) {

        CartesSingleton cartesData = CartesSingleton.getInstance();
        ArrayList<JSONObject> communesBW = cartesData.getCommunesBW();
        ArrayList<JSONObject> raresBW = cartesData.getRaresBW();
        ArrayList<JSONObject> communesBase = cartesData.getCommunesBase();
        ArrayList<JSONObject> raresBase = cartesData.getRaresBase();

        model.addAttribute("pokemonCardsCommonBW", communesBW);
        model.addAttribute("pokemonCardsRareBW", raresBW);
        model.addAttribute("pokemonCardsCommonBase", communesBase);
        model.addAttribute("pokemonCardsRareBase", raresBase);

        return "mescartes";
    }

}