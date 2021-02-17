/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.bulkinfo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.cnr.bulkinfo.BulkInfoImpl.FieldProperty;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Classe di utilita' per tradurre pojo BulkInfo in Json
 * In prospettiva sara' in grado di capire la versione del BulkInfo e
 * tradurre in maniera diversa di conseguenza.
 * 
 * Una differenza importante rispetto all'ftl e' che le properita' nel Json 
 * restituito sono ordinate alfabeticamente (ftl da' semplicemente una stringa,
 * mentre qui costruiamo un oggetto Json)
 * 
 * TODO pulizia generale
 * 
 * @author marcin
 */
public abstract class BulkInfoSerializer {

  private Gson gson = new Gson();

  // EXTENSION POINTS
  protected abstract void putCustomProperties(JsonObject result, Map<String, Object> model);
  protected abstract void putValue(JsonObject fpJson, FieldProperty fieldProperty, Map<String, Object> model);


  public JsonObject serialize(Map<String, Object> model) {
    JsonObject result = new JsonObject();

    BulkInfo bulkInfo = (BulkInfo) model.get("bulkInfo");

    result.addProperty("id", Optional.ofNullable(bulkInfo).map(BulkInfo::getId).orElse(""));

    // EXTENSION POINT
    // implement the protected putCustomProperties() method in sublcasses
    // base Bulkinfo properties
    putCustomProperties(result, model);

    putForms(result, model, (String) model.get("formName"));
    putColumnSets(result, model, (String) model.get("columnSetName"));
    putFreeSearchSets(result, model, (String) model.get("freeSearchSetName"));

    return result;
  }

  /**
   * @param result
   * @param bulkInfo
   * @param cmisObject
   * @param freeSearchSetName
   */
  private void putFreeSearchSets(JsonObject result,
      Map<String, Object> model, String freeSearchSetName) {

    BulkInfo bulkInfo = (BulkInfo) model.get("bulkInfo");

    if (freeSearchSetName != null) {

      JsonArray freeSearchSetsNamesJson = new JsonArray();
      if(freeSearchSetsNamesJson.equals("all")) {
        for(String freeSearchSet : bulkInfo.getFreeSearchSets().keySet()) {
          freeSearchSetsNamesJson.add(gson.toJsonTree(freeSearchSet));
        }
      } else {
        for(String freeSearchSet : freeSearchSetName.split(",")) {
          freeSearchSetsNamesJson.add(gson.toJsonTree(freeSearchSet));
        }
      }
      result.add("freeSearchSets", freeSearchSetsNamesJson);

      JsonArray freeSearchSets = result.getAsJsonArray("freeSearchSets");
      for(int i = 0; i < freeSearchSets.size(); i++) {
        String curFreeSearchSet = freeSearchSets.get(i).getAsString();
        JsonObject freeSearchSetJson = new JsonObject() ;

        Collection<FieldProperty> fieldProperties = bulkInfo.getFreeSearchSet(curFreeSearchSet);
        Iterator<FieldProperty> iterator = fieldProperties.iterator();
        while(iterator.hasNext()) {
          FieldProperty fieldProperty = iterator.next();
          putField(freeSearchSetJson, fieldProperty, model);
        }
        result.add(curFreeSearchSet, freeSearchSetJson);
      }
    }
  }

  /**
   * @param result
   * @param bulkInfo
   * @param cmisObject
   * @param columnSetName
   */
  private void putColumnSets(JsonObject result, Map<String, Object> model, String columnSetName) {
    BulkInfo bulkInfo = (BulkInfo) model.get("bulkInfo");

    if (columnSetName != null) {

      JsonArray columnNamesJson = new JsonArray();
      if(columnSetName.equals("all")) {
        for(String column : bulkInfo.getColumnSets().keySet()) {
          columnNamesJson.add(gson.toJsonTree(column));
        }
      } else {
        for(String column : columnSetName.split(",")) {
          columnNamesJson.add(gson.toJsonTree(column));
        }
      }
      result.add("columnSets", columnNamesJson);


      for(int i = 0; i < columnNamesJson.size(); i++) {
        String curColumnSet = columnNamesJson.get(i).getAsString();
        JsonObject columnSetJson = new JsonObject() ;

        Collection<FieldProperty> fieldProperties = bulkInfo.getColumnSet(curColumnSet);
        Iterator<FieldProperty> iterator = fieldProperties.iterator();
        while(iterator.hasNext()) {
          FieldProperty fieldProperty = iterator.next();
          putField(columnSetJson, fieldProperty, model);
        }
        result.add(curColumnSet, columnSetJson);
      }

    }
  }

  /**
   * @param result
   * @param bulkInfo
   * @param cmisObject
   * @param formName
   */
  private void putForms(JsonObject result, Map<String, Object> model, String formName) {
    BulkInfo bulkInfo = (BulkInfo) model.get("bulkInfo");


    if(formName != null) {

      JsonArray formNamesJson = new JsonArray();
      if(formName.equals("all")) {
        for(String form : bulkInfo.getForms().keySet()) {
          formNamesJson.add(gson.toJsonTree(form));
        }
      } else {
        for(String form : formName.split(",")) {
          formNamesJson.add(gson.toJsonTree(form));
        }
      }
      result.add("forms", formNamesJson);

      JsonArray forms = result.getAsJsonArray("forms");
      for(int i = 0; i < forms.size(); i++) {
        String curName = forms.get(i).getAsString();
        JsonObject formJson = new JsonObject();

        Collection<FieldProperty> fieldProperties = bulkInfo.getForm(curName);
        Iterator<FieldProperty> iterator = fieldProperties.iterator();
        while(iterator.hasNext()) {
          FieldProperty fieldProperty = iterator.next();
          putField(formJson, fieldProperty, model);
        }
        result.add(curName, formJson);
      }		
    }
  }

  private void putField(JsonObject parentJson, FieldProperty fieldProperty, Map<String, Object> model) {
    putField(parentJson, fieldProperty, model, true);
  }

  /**
   * Questo metodo aggiunge le field properties.
   * Se e' la versione due, lo fa in maniera ricorsiva
   * doPutValue va impostato a true solo per le proprieta' di primo livello, 
   * e non per le subFieldProperties
   * 
   * @param parentJson
   * @param fieldProperty
   * @param model
   * @param doPutValue
   */
  private void putField(JsonObject parentJson, FieldProperty fieldProperty, Map<String, Object> model, boolean doPutValue) {
    JsonObject fpJson = new JsonObject();

    for(String key : fieldProperty.getAttributes().keySet()) {
      String value = fieldProperty.getAttribute(key);
      if(value != null /*&& !value.equals("")*/) {
        // TODO Temporary fix, sara' sostituito con i 2.0
        if(key.equals("jsonlist")) {
          JsonParser parser = new JsonParser();
          fpJson.add(key, parser.parse(value));
        } else if(key.startsWith("json")) {
          JsonParser parser = new JsonParser();
          fpJson.add(key, parser.parse(value));

        } else {
          putPropertyTypeSafe(fpJson, key, value);
        }
      }
    }

    // EXTENSION POINT
    // implement the protected putValue() method in subclasses
    if(doPutValue) {
      putValue(fpJson, fieldProperty, model);
    }
    //fpJson.add("val", gson.toJsonTree(getValue(fieldProperty, model)) );


    // TODO subProperties 2.0

    // if the element is an array, build the jsonarray, else build like a normal object recursively
    if(!fieldProperty.getListElements().isEmpty()) {
      JsonArray array = new JsonArray();

      for(FieldProperty arrayElement : fieldProperty.getListElements()) {
        JsonObject jsonArrayElement = new JsonObject();
        for(String key : arrayElement.getAttributes().keySet()) {
          String value = arrayElement.getAttribute(key);
          putPropertyTypeSafe(jsonArrayElement, key, value);
        }
        array.add(jsonArrayElement);
      }

      parentJson.add(fieldProperty.getElementName(), array);

    } else {
      for(String key : fieldProperty.getSubProperties().keySet()) {
        FieldProperty subFieldProperty = fieldProperty.getSubProperty(key);
        putField(fpJson, subFieldProperty, model, false);
      }

      // this is necessary for subproperties
      String name = fieldProperty.getName();
      if(name == null || name.equals("")) {
        name = fieldProperty.getElementName();
      }
      parentJson.add(name, fpJson);
    }
  }
  /**
   * @param fpJson
   * @param key
   * @param value
   */
  private void putPropertyTypeSafe(JsonObject fpJson, String key, String value) {

    // caso speciale: "key" è un campo che può assumere diversi valori, sia stringhe che interi che booleani
    // Se stiamo trattando una "key" restituiamo il valore cos' com'e'
    if("key".equals(key)) {
      fpJson.add(key, gson.toJsonTree(value));

    // fine caso speciale, torniamo a 
    } else { 
      if(value.startsWith("[")) {
        String values = value.substring(1, value.length()-1);
        String[] stringArray = values.split(",");
        fpJson.add(key, gson.toJsonTree(stringArray));
      } else if(value.equals("true") || value.equals("false")) {
        fpJson.add(key, gson.toJsonTree(Boolean.parseBoolean(value)));
      } else if(value.matches("^-?[\\d.]+(?:e-?\\d+)?$")) {
        try{
          Number numberValue = NumberFormat.getInstance().parse(value);
          fpJson.add(key, gson.toJsonTree(numberValue));
        } catch(ParseException exp) {
          // e' una stringa inserisci cosi' com'e'
          fpJson.add(key, gson.toJsonTree(value));
        } 
      } else {
        fpJson.add(key, gson.toJsonTree(value));
      }
    }
  }

}
