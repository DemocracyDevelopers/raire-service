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

package au.org.democracydevelopers.raireservice.controller;

import au.org.democracydevelopers.raireservice.request.RequestValidationException;
import au.org.democracydevelopers.raireservice.service.GenerateAssertionsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Exception handler for the bad request types common to all Contest Requests,
 * including GenerateAssertionsRequest and GetAssertionsRequest.
 * Other errors (the ones specific to particular tasks) will be dealt with using
 * ResponseStatusException.
 */
@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<String> handleInvalidRequestException(RequestValidationException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle exceptions that arise during assertion generation. This will return the custom code
     * in the headers, so that colorado-rla can interpret it. The error message will be short and
     * is intended to be shown to the user. Note that errors may be a consequence of the data
     * (e.g. tied winners) or of a server error.
     * @param ex the GenerateAssertionsException.
     * @return
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(GenerateAssertionsException.class)
    public ResponseEntity<String> handleErrorResponseException(GenerateAssertionsException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("code", String.valueOf(ex.errorCode));

        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.NO_CONTENT);
    }
}
