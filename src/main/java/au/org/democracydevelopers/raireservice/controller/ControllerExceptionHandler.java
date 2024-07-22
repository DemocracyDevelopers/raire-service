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
import au.org.democracydevelopers.raireservice.service.RaireServiceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static au.org.democracydevelopers.raireservice.service.RaireServiceException.ERROR_CODE_KEY;

/**
 * Exception handler for all exceptions thrown by the AssertionController endpoints.
 */
@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle RequestValidationExceptions, which can arise during generate or get assertion API requests.
     * The error message will be short and is intended to be shown to the user. These errors are the
     * fault of the caller.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<String> handleInvalidRequestException(RequestValidationException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle RaireServiceExceptions that arise during generate or get assertion API requests.
     * This will return the custom code in the headers, so that colorado-rla can interpret it.
     * The error message will be short and is intended to be shown to the user. These errors
     * may be a consequence of the data (e.g. tied winners) or of a server error.
     * @param ex the RaireException.
     * @return the error message and error code.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(RaireServiceException.class)
    public ResponseEntity<String> handleRaireException(RaireServiceException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(ERROR_CODE_KEY, String.valueOf(ex.errorCode));

        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Generic exception handling. This indicates an unexpected error.
     * Exception-handlers are called in order of specificity, so this one will be last.
     * @return the error message as an API response.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
