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
 *
 **/
@JsonSerialize(using= GetAssertionError.GetAssertionsErrorSerializer.class)
public abstract class GetAssertionError {
    public static class NoAssertions extends GetAssertionError {}
    public static class ErrorRetrievingAssertions extends GetAssertionError {}
    public static class InvalidRequest extends GetAssertionError {}

    /** Custom JSON serializer for Jackson */
    public static class GetAssertionsErrorSerializer extends StdSerializer<GetAssertionError> {

        public GetAssertionsErrorSerializer() { this(null); }
        public GetAssertionsErrorSerializer(Class<GetAssertionError> t) { super(t); }

        @Override
        public void serialize(GetAssertionError getAssertionError, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

            if (getAssertionError instanceof NoAssertions) {
                jsonGenerator.writeString("NoAssertionsForThisContest");
            }
            else if (getAssertionError instanceof ErrorRetrievingAssertions) {
                jsonGenerator.writeString("ErrorRetrievingAssertions");
            }
            else if (getAssertionError instanceof InvalidRequest) {
                jsonGenerator.writeString("InvalidGetAssertionRequest");
            }
            else {
                throw new IOException("Do not understand RaireError "+getAssertionError);
            }
        }
    }
}
