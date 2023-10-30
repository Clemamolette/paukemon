package com.example.demo.Service;

import com.example.demo.Model.Carte;
import com.example.demo.Model.Pokemon;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.*;

@Service @SessionScope
public class PokemonService {

    private String apiURLBW1 = "https://api.pokemontcg.io/v2/cards?select=id,name,hp,types,nationalPokedexNumbers,images,rarity,supertype&q=set.id:bw1";
    private String apiURLBW2 = "https://api.pokemontcg.io/v2/cards?select=id,name,hp,types,nationalPokedexNumbers,images,rarity,supertype&q=set.id:bw2";
    private String apiURLBase1 = "https://api.pokemontcg.io/v2/cards?select=id,name,hp,types,nationalPokedexNumbers,images,rarity,supertype&q=set.id:base1";
    private String apiURLBase2 = "https://api.pokemontcg.io/v2/cards?select=id,name,hp,types,nationalPokedexNumbers,images,rarity,supertype&q=set.id:base2";
    private String apiKEY = "e030d28b-a210-4552-bec5-3b63be6b970b";
    private ArrayList<ArrayList<String>> data;

    protected boolean initialized = false;
    protected ArrayList<JSONObject> communesBWData = new ArrayList<>();
    protected ArrayList<JSONObject> raresBWData = new ArrayList<>();
    protected ArrayList<JSONObject> communesBaseData = new ArrayList<>();
    protected ArrayList<JSONObject> raresBaseData = new ArrayList<>();

    public void initialize() {
        if (!(initialized)) {
            data = getCards();

            JSONObject json;
            // chaque carte est un string, on les transforme en objets JSON

            ArrayList<String> communesBWSTR = data.get(0);
            formatage(communesBWSTR, communesBWData);
            addToDataBase(communesBWData);

            ArrayList<String> raresBWSTR = data.get(1);
            formatage(raresBWSTR, raresBWData);
            addToDataBase(raresBWData);

            ArrayList<String> communesBaseSTR = data.get(2);
            formatage(communesBaseSTR, communesBaseData);
            addToDataBase(communesBaseData);

            ArrayList<String> raresBaseSTR = data.get(3);
            formatage(raresBaseSTR, raresBaseData);
            addToDataBase(raresBaseData);

            initialized = true;
        }
    }

    private void formatage(ArrayList<String> serieSTR, ArrayList<JSONObject> serieData) {
        JSONObject json;
        for (int i = 0; i<serieSTR.size(); i++) {
            json = new JSONObject(serieSTR.get(i));
            JSONObject image = (JSONObject) json.get("images");
            json.put("images",image.get("large"));
            serieData.add(json);
        }
    }

    public ArrayList<ArrayList<String>> getCards() {
        HttpResponse<JsonNode> jsonResponse;
        {
            ArrayList<ArrayList<String>> data;
            List<List<Pokemon>> pokemonList;
            try {
                // première requête avec une extension de carte
                jsonResponse = Unirest.get(apiURLBW1)
                        .header("X-Api-Key", apiKEY)
                        .asJson();

                // création des tableaux pour contenir les cartes rares et communes
                data = new ArrayList<>();
                ArrayList<String> communesBW = new ArrayList<String>();
                ArrayList<String> raresBW = new ArrayList<String>();

                //récupération des données de la réponse et classement dans les deux tableaux selon la rareté
                JSONArray data_json = (JSONArray) jsonResponse.getBody().getObject().get("data");
                int len = data_json.length();
                dispatchRarity(data_json, communesBW, raresBW, len);


                // Deuxième requête avec une autre extension de cartes
                jsonResponse = Unirest.get(apiURLBW2)
                        .header("X-Api-Key", apiKEY)
                        .asJson();
                data_json = (JSONArray) jsonResponse.getBody().getObject().get("data");
                len = data_json.length();
                dispatchRarity(data_json, communesBW, raresBW, len);


                ArrayList<String> communesBase = new ArrayList<String>();
                ArrayList<String> raresBase = new ArrayList<String>();
                // Troisième requête
                jsonResponse = Unirest.get(apiURLBase1)
                        .header("X-Api-Key", apiKEY)
                        .asJson();
                data_json = (JSONArray) jsonResponse.getBody().getObject().get("data");
                len = data_json.length();
                dispatchRarity(data_json, communesBase, raresBase, len);


                // Quatrième requête
                jsonResponse = Unirest.get(apiURLBase2)
                        .header("X-Api-Key", apiKEY)
                        .asJson();
                data_json = (JSONArray) jsonResponse.getBody().getObject().get("data");
                len = data_json.length();
                dispatchRarity(data_json, communesBase, raresBase, len);


                // on regroupe les quatres listes dans une même autre liste
                data.add(communesBW);
                data.add(raresBW);
                data.add(communesBase);
                data.add(raresBase);
            } catch (UnirestException e) {
                throw new RuntimeException(e);
            }
            return data;
        }
    }

    private void addToDataBase(ArrayList<JSONObject> data) {
        Carte c = new Carte();
        for (JSONObject carte : data) {
            c.setId(carte.get("id").toString());
            c.setHp(carte.get("hp").toString());
            c.setName(carte.get("name").toString());
            c.setImages(carte.get("images").toString());
            c.setRarity(carte.get("rarity").toString());
            c.setQuantity(0);
            c.setType(carte.get("types").toString());
            c.setAcquired(false);
        }
    }

    private void dispatchRarity(JSONArray data_json, ArrayList<String> communes, ArrayList<String> rares, int len) {
        for (int i=0;i<len;i++){
            JSONObject card = (JSONObject) data_json.get(i);
            if (card.get("supertype").toString().contains("mon")) {
                if (card.get("rarity").toString().contains("ommon")) {
                    communes.add(card.toString());
                } else {
                    rares.add(card.toString());
                }
            }
        }
    }
    public ArrayList<JSONObject> getCommunesBW() {
        initialize();
        return communesBWData;
    }

    public ArrayList<JSONObject> getRaresBW() {
        initialize();
        return raresBWData;
    }

    public ArrayList<JSONObject> getCommunesBase() {
        initialize();
        return communesBaseData;
    }

    public ArrayList<JSONObject> getRaresBase() {
        initialize();
        return raresBaseData;
    }
}
