/*
  Copyright 2023 Democracy Developers

 */

package au.org.democracydevelopers.raireservice.response;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Everything that could go wrong when retrieving assertions.
 * FIXME - needs to deal with RAIRE generation errors, as well as errors associated with invalid input.
 *
 **/
@JsonSerialize(using= GenerateAssertionsError.GetAssertionsErrorSerializer.class)
public abstract class GenerateAssertionsError {
    public static class PlaceholderError extends GenerateAssertionsError {}

    /** Custom JSON serializer for Jackson */
    public static class GetAssertionsErrorSerializer extends StdSerializer<GenerateAssertionsError> {

        public GetAssertionsErrorSerializer() { this(null); }
        public GetAssertionsErrorSerializer(Class<GenerateAssertionsError> t) { super(t); }

        @Override
        public void serialize(GenerateAssertionsError getAssertionError, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

            if (getAssertionError instanceof PlaceholderError) {
                jsonGenerator.writeString("PlaceholderError");
            }
            else {
                throw new IOException("Do not understand RaireError "+getAssertionError);
            }
        }
    }
}
