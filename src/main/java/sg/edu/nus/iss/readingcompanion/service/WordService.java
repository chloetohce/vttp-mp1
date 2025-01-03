package sg.edu.nus.iss.readingcompanion.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import sg.edu.nus.iss.readingcompanion.model.Word;
import sg.edu.nus.iss.readingcompanion.utilities.URL;

@Service
public class WordService {
    private RestTemplate restTemplate = new RestTemplate();

    public List<Word> queryWord(String word) throws Exception {
        String uri = UriComponentsBuilder.fromUriString(URL.WORDSAPI)
            .pathSegment(word)
            .toUriString();
        RequestEntity<Void> request = RequestEntity.get(uri).build();

        try {
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            
            JsonReader reader = Json.createReader(new StringReader(response.getBody()));
            JsonArray groups = reader.readArray();
            
            List<Word> words = new ArrayList<>();

            for (int i = 0; i < groups.size(); i++) {
                JsonObject group = groups.getJsonObject(i);
                JsonArray parts = group.getJsonArray("meanings");
                Map<String, List<String>> partMap = new HashMap<>();

                for (int j = 0; j < parts.size(); j++) {
                    JsonObject part = parts.getJsonObject(j);
                    String partOfSpeech = part.getString("partOfSpeech");
                    JsonArray definitions = part.getJsonArray("definitions");
                    List<String> definitionsList = new ArrayList<>();

                    for (int k = 0; k < definitions.size(); k++) {
                        String definition = definitions.getJsonObject(k).getString("definition");
                        definitionsList.add(definition);
                    }
                    partMap.put(partOfSpeech, definitionsList);
                }
                words.add(new Word(word, partMap));
            }

            return words;
            
        } catch (HttpClientErrorException e) {
            throw new Exception("Word not found.");
        }
    }

    public boolean saveWord(String username, String bookId, String word) {
        try {
            List<Word> words = queryWord(word);
            String uri = UriComponentsBuilder.fromUriString(URL.API_WORD)
                .pathSegment("add")
                .queryParam("username", username)
                .queryParam("bookId", bookId)
                .toUriString();

            for (Word w : words) {
                JsonObject requestBody = Json.createObjectBuilder()
                    .add("word", Json.createReader(new StringReader(w.serialize())).readObject())
                    .build();
                RequestEntity<String> request = RequestEntity.post(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody.toString());
                restTemplate.exchange(request, String.class);

            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Word> getWordsForBook(String username, String bookId) {
        String uri = UriComponentsBuilder.fromUriString(URL.API_WORD)
            .queryParam("username", username)
            .queryParam("bookId", bookId)
            .toUriString();
        RequestEntity<Void> request = RequestEntity.get(uri).build();
        try {
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            JsonReader reader = Json.createReader(new StringReader(response.getBody()));
            JsonArray wordsArr = reader.readArray();
    
            List<Word> words = new ArrayList<>();
            for (int i = 0; i < wordsArr.size(); i++) {
                JsonObject wordJson = wordsArr.getJsonObject(i);
                Word w = Word.deserialize(wordJson.toString());
                words.add(w);
            }
    
            return words;
        } catch (HttpClientErrorException e) {
            return new ArrayList<>();
        }
    }

    public void deleteWord(String username, String bookId, String word) {
        JsonObject requestJson = Json.createObjectBuilder()
            .add("word", word)
            .build();
        String uri = UriComponentsBuilder.fromUriString(URL.API_WORD)
                .pathSegment("delete")
                .queryParam("username", username)
                .queryParam("bookId", bookId)
                .toUriString();
        RequestEntity<String> request = RequestEntity.post(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestJson.toString());
        restTemplate.exchange(request, String.class);
    }

}
