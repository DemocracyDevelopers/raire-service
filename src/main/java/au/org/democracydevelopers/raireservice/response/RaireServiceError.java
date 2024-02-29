/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

package au.org.democracydevelopers.raireservice.response;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Everything that could go wrong in RaireService. Typically this will be returned as a thrown RaireServiceException with this as its argument.
 *
 * It is implemented as a class rather than an Exception hierarchy to facilitate detailed error serialization.
 **/
@JsonSerialize(using= RaireServiceError.RaireServiceErrorSerializer.class)
@JsonDeserialize(using = RaireServiceError.RaireServiceErrorDeserializer.class)
public abstract class RaireServiceError {
    /**
     * Temporary, for the prototype to deal generically with errors we haven't dealt with properly yet.
     */
    public static class PlaceholderError extends RaireServiceError {}

    /**
     * The initial request was invalid.
     */
    public static class InvalidRequest extends RaireServiceError  {
        final String message;
        public InvalidRequest(String message) {
            this.message = message;
        }
    }

    /**
     * And internal error caused by a coding error. These indicate things that are not supposed to happen,
     * either because they are irrelevant to the Colorado case or because they are not supposed to happen
     * inside raire-java either.
     */
    public static class InternalError extends RaireServiceError {}

    /** The RAIRE algorithm is given a time limit in seconds to limit its runtime.
     * If a negative or NaN value is provided as the time limit, then the InvalidTimout
     * error is generated. */
    public static class InvalidTimeout extends RaireServiceError {}

    /** Carry-over of raire-java error.
     * If assertion generation (usually the slowest of the three stages of computation)
     * does not complete within the specified time limit, the TimeoutFindingAssertions error
     * will be generated. All three stages must be completed within the specified time limit
     * or a relevant timeout error will be generated. */
    public static class TimeoutFindingAssertions extends RaireServiceError { final double difficultyAtTimeOfStopping;
        public TimeoutFindingAssertions(double difficultyAtTimeOfStopping) {
            this.difficultyAtTimeOfStopping = difficultyAtTimeOfStopping;
        }
    }

    /** Carry-over of raire-java error.
     * After generating assertions, a filtering stage will occur in which redundant
     * assertions are removed from the final set. This stage is usually reasonably fast.
     * However, if this stage does not complete within the specified time limit, the
     * TimeoutTrimmingAssertions error will be generated. All three stages must be completed
     * within the specified time limit or a relevant timeout error will be generated.*/
    public static class TimeoutTrimmingAssertions extends RaireServiceError {}

    /** If RAIRE determines that the contest has multiple possible winners consistent with
     * the rules of IRV (i.e. there is a tie) then the TiedWinners error will be generated.
     * While the particular legislation governing the contest may have unambiguous tie
     * resolution rules, there is no way that an RLA could be helpful if the contest comes
     * down to a tie resolution. */
    public static class TiedWinners extends RaireServiceError { final List<String> expected;
        public TiedWinners(List<String> expected) {
            this.expected = expected;
        }
    }
    public static class NoAssertions extends RaireServiceError {}
    public static class ErrorRetrievingAssertions extends RaireServiceError {}

    /** Custom JSON serializer for Jackson */
    public static class RaireServiceErrorSerializer extends StdSerializer<RaireServiceError> {

        public RaireServiceErrorSerializer() { this(null); }
        public RaireServiceErrorSerializer(Class<RaireServiceError> t) { super(t); }

        private void writeIntArray(JsonGenerator jsonGenerator,String fieldName,int[]array) throws IOException {
            jsonGenerator.writeFieldName(fieldName);
            jsonGenerator.writeArray(array,0,array.length);
        }

        /**
         * TODO. Include all types.
         * @param raireServiceError
         * @param jsonGenerator
         * @param serializerProvider
         * @throws IOException
         */
        @Override
        public void serialize(RaireServiceError raireServiceError, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            switch(raireServiceError) {
                case InvalidTimeout e -> jsonGenerator.writeString("This is a test"+e.toString());
                case TiedWinners e ->jsonGenerator.writeString("TiedWinners: "+ e.expected);
                case NoAssertions e -> jsonGenerator.writeString("NoAssertionsForThisContest");
                case ErrorRetrievingAssertions e -> jsonGenerator.writeString("Error retrieving assertions");
                case InvalidRequest e -> jsonGenerator.writeString("Invalid request: "+e.message);
                default -> jsonGenerator.writeString("test");
            }
        }
    }

    public static class RaireServiceErrorDeserializer extends StdDeserializer<RaireServiceError> {
        public RaireServiceErrorDeserializer() { this(null); }
        public RaireServiceErrorDeserializer(Class<?> vc) { super(vc); }

        private int[] getIntArray(JsonNode node) {
            return StreamSupport.stream(node.spliterator(), false).mapToInt(JsonNode::asInt).toArray();
        }
        private List<String> getStringList(JsonNode node) {
            return StreamSupport.stream(node.spliterator(), false).map(JsonNode::asText).toList();
        }

        /**
         * TODO. I'm not sure that we need this. Either cover all the cases, or delete.
         * @param jsonParser
         * @param deserializationContext
         * @return
         * @throws IOException
         */
        @Override
        public RaireServiceError deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            if (node.isTextual()) {
                String text = node.asText();
                switch (text) {
                    case "TimeoutTrimmingAssertions" : return new RaireServiceError.TimeoutTrimmingAssertions();
                    case "InternalError" : return new RaireServiceError.InternalError();
                    case "PlaceholderError" : return new RaireServiceError.PlaceholderError();
                }
            } else if (node.isObject()) {
                if (node.has("TimeoutFindingAssertions")) return new RaireServiceError.TimeoutFindingAssertions(node.get("TimeoutFindingAssertions").doubleValue());
                else if (node.has("TiedWinners")) return new RaireServiceError.TiedWinners(getStringList(node.get("TiedWinners")));
            }
            throw new IOException("Could not understand "+node);
        }
    }
}
