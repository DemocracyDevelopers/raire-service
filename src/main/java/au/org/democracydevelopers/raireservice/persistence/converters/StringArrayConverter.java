/*
Copyright 2024 Democracy Developers

The Raire Service is designed to connect colorado-rla and its associated database to
the raire assertion generation engine (https://github.com/DemocracyDevelopers/raire-java).

This file is part of raire-service.

raire-service is free software: you can redistribute it and/or modify it under the terms
of the GNU Affero General Public License as published by the Free Software Foundation, either
version 3 of the License, or (at your option) any later version.

raire-service is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with
raire-service. If not, see <https://www.gnu.org/licenses/>.
*/

package au.org.democracydevelopers.raireservice.persistence.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class converts between an array of String and a JSON representation of that
 * string array as a String. This converter will be used to translate ranked choices
 * stored in the colorodo-rla database as a single String into an array of String,
 * one String for each choice. The counterpart in colorado-rla is a converter between
 * String and List<String>. We convert choices to an array of String for easier
 * interaction with raire-java. We have also replaced the use of GSON with Jackson.
 */
@Converter(autoApply = true)
public class StringArrayConverter implements AttributeConverter<String[], String> {

  private final Logger logger = LoggerFactory.getLogger(StringArrayConverter.class);

  /**
   * The type information for an array of String.
   */
  private static final TypeReference<String[]> STRING_ARRAY = new TypeReference<>() {};

  /**
   * An ObjectMapper  used to convert an array of String to a JSON representation (a single String)
   * and vice versa. We throw an exception when the converter is trying to convert a null/blank
   * entry to a JSON string, or a null/blank entry to an array of strings. In the context with
   * which this converter is being used by raire-service, the first would add invalid data to the
   * database, and the second would indicate that invalid data is present in the database. Note
   * that in colorado-rla their converter (using GSON) disables HTML escaping.
   */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Converts the specified array of String to a single String database column entry.
   *
   * @param arrayOfString The list of String to be converted into a single String.
   * @throws RuntimeException when a null/blank field would be stored in place of a JSON
   * representation of a list, or the given String array could not be converted to JSON.
   */
  @Override
  public String convertToDatabaseColumn(final String[] arrayOfString) {
    final String prefix = "[convertToDatabaseColumn]";
    if(arrayOfString == null){
      final String msg = String.format("%s Attempt to store a null value in place of a JSON list.",
          prefix);
      logger.error(msg);
      throw new RuntimeException(msg);
    }
    try {
      return objectMapper.writeValueAsString(arrayOfString);
    } catch (JsonProcessingException e) {
      final String msg = String.format("%s Problem in converting %s to a JSON string: %s.", prefix,
          Arrays.toString(arrayOfString), e.getMessage());
      logger.error(msg);
      throw new RuntimeException(msg);
    }
  }

  /**
   * Converts the specified single-String database column entry to an array of String. If the
   * column entry is null or an empty string, this method will return 'null'. If the column
   * entry is a non-empty string, and not in the JSON format of a list, this method will throw a
   * RuntimeException. We also throw this exception if the caller is trying to convert a null
   * string, or an empty string, to an array of strings. This indicates a problem in the data
   * present in the database.
   *
   * @param arrayAsString The column entry.
   * @throws RuntimeException when the database column entry being converted to an array of
   * strings is not in the correct JSON format, or is blank.
   */
  @Override
  public String[] convertToEntityAttribute(final String arrayAsString) {
    final String prefix = "[convertToEntityAttribute]";
    if(arrayAsString.isBlank()){
      final String msg = String.format("%s A null/blank entry is present in the database in place "
              + "of a JSON list. Error in attempting to convert \"%s\" to a list.", prefix,
              arrayAsString);
      logger.error(msg);
      throw new RuntimeException(msg);
    }
    try {
      return objectMapper.readValue(arrayAsString, STRING_ARRAY);
    } catch (JsonProcessingException e) {
      final String msg = String.format("%s Problem in converting \"%s\" to an array of strings: %s",
          prefix, arrayAsString, e.getMessage());
      logger.error(msg);
      throw new RuntimeException(e);
    }
  }
}
