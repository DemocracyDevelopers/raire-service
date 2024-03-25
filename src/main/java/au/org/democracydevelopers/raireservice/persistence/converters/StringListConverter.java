/*
  This file has been copied and modified from the class of the same name and function in
  colorado-rla (Free & Fair Colorado RLA System). The following is the copyright notice
  of that file. We have updated the commenting associated with that file, and changed parameter
  names to follow a camel case convention.

  Free & Fair Colorado RLA System

  @title ColoradoRLA

  @created Aug 26, 2017

  @copyright 2017 Colorado Department of State

  @license SPDX-License-Identifier: AGPL-3.0-or-later

  @creator Daniel M. Zimmerman <dmz@freeandfair.us>

  @description A system to assist in conducting statewide risk-limiting audits.
 */

package au.org.democracydevelopers.raireservice.persistence.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Note this is class has been copied from Free & Fair's converter of the same name for
 * colorado-rla. It converts between a list of String and a JSON representation of that
 * string list as a String. The original author of this class was Daniel M. Zimmerman
 * <dmz@freeandfair.us>. Note that we have updated the commenting and made modifications to
 * parameter names.
 *
 * This class (version 1.0.0) has been copied from colorado-rla to ensure consistency in the
 * way String array attributes in database columns are interpreted between colorado-rla and
 * raire-service.
 */
@Converter(autoApply = true)
public class StringListConverter implements AttributeConverter<List<String>, String> {

  /**
   * The type information for a list of String.
   */
  private static final Type STRING_LIST = new TypeToken<List<String>>() {}.getType();

  /**
   * A GSON instance used to convert a list of String to a JSON representation (a single String)
   * and vice versa. The colorado-rla convention is to retain HTML characters when serialising and
   * to serialise null fields.
   */
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

  /**
   * Converts the specified list of String to a single String database column entry.
   *
   * @param listOfString The list of String to be converted into a single String.
   */
  @Override
  public String convertToDatabaseColumn(final List<String> listOfString) {
    return GSON.toJson(listOfString);
  }

  /**
   * Converts the specified single-String database column entry to a list of String.
   *
   * @param listAsString The column entry.
   */
  @Override
  public List<String> convertToEntityAttribute(final String listAsString) {
    return GSON.fromJson(listAsString, STRING_LIST);
  }
}
