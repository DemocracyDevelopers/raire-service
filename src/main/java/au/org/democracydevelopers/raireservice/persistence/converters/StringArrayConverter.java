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
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.lang.reflect.Type;

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

  /**
   * The type information for an array of String.
   */
  private static final Type STRING_ARRAY = new TypeToken<String[]>() {}.getType();

  /**
   * A GSON instance used to convert an array of String to a JSON representation (a single String)
   * and vice versa. The colorado-rla convention is to retain HTML characters when serialising and
   * to serialise null fields.
   */
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

  /**
   * Converts the specified array of String to a single String database column entry.
   *
   * @param arrayOfString The list of String to be converted into a single String.
   */
  @Override
  public String convertToDatabaseColumn(final String[] arrayOfString) {
    return GSON.toJson(arrayOfString);
  }

  /**
   * Converts the specified single-String database column entry to an array of String.
   *
   * @param arrayAsString The column entry.
   */
  @Override
  public String[] convertToEntityAttribute(final String arrayAsString) {
    return GSON.fromJson(arrayAsString, STRING_ARRAY);
  }
}
