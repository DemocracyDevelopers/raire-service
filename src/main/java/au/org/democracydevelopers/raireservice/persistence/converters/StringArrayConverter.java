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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.lang.reflect.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class converts between an array of String and a JSON representation of that
 * string array as a String. This converter will be used to translate ranked choices
 * stored in the colorodo-rla database as a single String into an array of String,
 * one String for each choice. The counterpart in colorado-rla is a converter between
 * String and List<String>. We convert choices to an array of String for easier
 * interaction with raire-java.
 */
@Converter(autoApply = true)
public class StringArrayConverter implements AttributeConverter<String[], String> {

  protected final Logger logger = LoggerFactory.getLogger(StringArrayConverter.class);

  /**
   * The type information for an array of String.
   */
  private static final Type STRING_ARRAY = new TypeToken<String[]>() {}.getType();

  /**
   * A GSON instance used to convert an array of String to a JSON representation (a single String)
   * and vice versa. The colorado-rla convention is to retain HTML characters when serialising and
   * to serialise null fields. Note, however, that we throw an exception when the converter is
   * trying to convert a null/blank entry to a JSON string, or a null/blank entry to an array of
   * strings. In the context with which this converter is being used by raire-service, the first
   * would add invalid data to the database, and the second would indicate that invalid data is
   * present in the database.
   */
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

  /**
   * Converts the specified array of String to a single String database column entry.
   *
   * @param arrayOfString The list of String to be converted into a single String.
   * @throws JsonSyntaxException when a null/blank field would be stored in place of a JSON
   * representation of a list.
   */
  @Override
  public String convertToDatabaseColumn(final String[] arrayOfString) {
    if(arrayOfString == null){
      final String msg = "Attempt to store a null value in place of a JSON list.";
      logger.error(msg);
      throw new JsonSyntaxException(msg);
    }
    return GSON.toJson(arrayOfString);
  }

  /**
   * Converts the specified single-String database column entry to an array of String. If the
   * column entry is null or an empty string, this method will return 'null'. If the column
   * entry is a non-empty string, and not in the JSON format of a list, this method will throw a
   * Json Parse/Syntax Exception. We also throw this exception if the caller is trying to
   * convert a null string, or an empty string, to an array of strings. This indicates a problem
   * in the data present in the database.
   *
   * @param arrayAsString The column entry.
   * @throws JsonSyntaxException when the database column entry being converted to an array of
   * strings is not in the correct JSON format, or is blank.
   */
  @Override
  public String[] convertToEntityAttribute(final String arrayAsString) {
      if(arrayAsString.isBlank()){
        final String msg = "A null/blank entry is present in the database in place of a JSON list.";
        logger.error(msg);
        throw new JsonSyntaxException(msg);
      }
      return GSON.fromJson(arrayAsString, STRING_ARRAY);
  }
}
