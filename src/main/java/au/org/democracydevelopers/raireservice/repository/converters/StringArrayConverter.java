/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * 
 * @created Aug 26, 2017
 * 
 * @copyright 2017 Colorado Department of State
 * 
 * @license SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * @creator Daniel M. Zimmerman <dmz@freeandfair.us>
 * 
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package au.org.democracydevelopers.raireservice.repository.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.lang.reflect.Type;

/**
 * Note this is copied from Free & Fair's converter of the same name for colorado-rla.
 * A converter between lists of Strings and JSON representations of such lists,
 * for database efficiency.
 * @author Daniel M. Zimmerman <dmz@freeandfair.us>
 * @version 1.0.0
 */
@Converter(autoApply = true)
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class StringArrayConverter implements AttributeConverter<String[], String> {
  /**
   * The type information for a list of String.
   */
  private static final Type STRING_ARRAY = new TypeToken<String[]>() {
  }.getType();

  /**
   * Our Gson instance, which does not do pretty-printing (unlike the global one
   * defined in Main).
   */
  // private static final Gson GSON =
  // new GsonBuilder().serializeNulls().create();
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

  /**
   * Converts the specified list of Strings to a database column entry.
   * 
   * @param the_list The list of Strings.
   */
  @Override
  public String convertToDatabaseColumn(final String[] the_list) {
    return GSON.toJson(the_list);
  }

  /**
   * Converts the specified database column entry to a list of strings.
   * 
   * @param the_column The column entry.
   */
  @Override
  public String[] convertToEntityAttribute(final String the_column) {
    System.out.println("Converting "+the_column);
    return GSON.fromJson(the_column, STRING_ARRAY);
  }
}
