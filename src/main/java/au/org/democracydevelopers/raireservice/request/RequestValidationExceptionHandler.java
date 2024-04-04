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

package au.org.democracydevelopers.raireservice.request;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Exception handler for the bad request types common to all Contest Requests,
 * including GenerateAssertionsRequest and GetAssertionsRequest.
 * Based on Solution 3 in <a href="https://www.baeldung.com/exception-handling-for-rest-with-spring">...</a>
 * Other errors (the ones specific to particular tasks) will be dealt with using
 * ResponseStatusException.
 */
@ControllerAdvice
public class RequestValidationExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = { RequestValidationException.class })
    protected ResponseEntity<Object> handleConflict(
        RequestValidationException ex, WebRequest request) {
      String bodyOfResponse = "Invalid request";
      return handleExceptionInternal(ex, bodyOfResponse,
          new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    /* Note some simply have very simple bodies, e.g.
        @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<String> handleInvalidRequestException(InvalidRequestException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    which looks better.
     */
}
