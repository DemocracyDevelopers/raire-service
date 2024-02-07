/*
  Copyright 2023 Democracy Developers

 */

package au.org.democracydevelopers.raireservice.response;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.stream.StreamSupport;

/**
 * Everything that could go wrong when retrieving assertions.
 *
 **/
@JsonSerialize(using= GetAssertionError.GetAssertionsErrorSerializer.class)
public abstract class GetAssertionError {
    public static class NoAssertions extends GetAssertionError {}
    public static class ErrorRetrievingAssertions extends GetAssertionError {}

    /** Custom JSON serializer for Jackson */
    public static class GetAssertionsErrorSerializer extends StdSerializer<GetAssertionError> {

        public GetAssertionsErrorSerializer() { this(null); }
        public GetAssertionsErrorSerializer(Class<GetAssertionError> t) { super(t); }

        private void writeIntArray(JsonGenerator jsonGenerator,String fieldName,int[]array) throws IOException {
            jsonGenerator.writeFieldName(fieldName);
            jsonGenerator.writeArray(array,0,array.length);
        }

        @Override
        public void serialize(GetAssertionError getAssertionError, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            // first consider the errors that are serialized as a simple string
            if (getAssertionError instanceof NoAssertions) {
                jsonGenerator.writeString("NoAssertionsForThisContest");
            }
            else if (getAssertionError instanceof ErrorRetrievingAssertions) {
                jsonGenerator.writeString("ErrorRetrievingAssertions");
            }
            else {
                throw new IOException("Do not understand RaireError "+getAssertionError);
            }
            jsonGenerator.writeEndObject();
        }
    }
}
