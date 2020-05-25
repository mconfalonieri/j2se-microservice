/*
 * J2SE Microservice - Application starter.
 * 
 * Copyright (c) 2020 Marco Confalonieri
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package it.marcoconfalonieri.j2semicroservice.app;

import io.undertow.Undertow;
import io.undertow.util.Headers;

/**
 * The AppStarter class is the main application class. It contains the main method
 * and sets up the web server.
 */
public class AppStarter {
    /** Default listener port. */
    public static final int DEFAULT_HTTP_PORT = 8080;
    /** Default listener address. */
    public static final String DEFAULT_HTTP_ADDRESS = "0.0.0.0";

    /** String returned when an unknown path is requested. */
    private static final String NOT_FOUND_STRING = "{\"error\": \"resource not found\"}";

    /**
     * Exception thrown upon errors in the parameters.
     */
    private static class CLIParamsException extends Exception {
        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = -193764702458078L;

        public CLIParamsException(String msg) {
            super(msg);
        }

        public CLIParamsException(String msg, Throwable cause) {
            super(msg, cause);
        }
    };

    /**
     * CLIParams is used to store command line parameters.
     */
    private static class CLIParams {
        /** Listener port. */
        private int port = DEFAULT_HTTP_PORT;
        /** Listener address. */
        private String address = DEFAULT_HTTP_ADDRESS;
    
        /**
         * @return the HTTP port
         */
        public int getPort() {
            return port;
        }

        /**
         * @return the listener address
         */
        public String getAddress() {
            return address;
        }

        /**
         * Parses the arguments.
         * 
         * @param args command line arguments
         * @throws CLIParamsException if there are errors in the specified arguments.
         */
        public void parse(final String[] args) throws CLIParamsException {
            try {
                for (int i = 0; i < args.length; i++) {
                    String arg = args[i];
                    switch (arg) {
                    case "--port":
                        port = readPortValue(args[i++]);
                        break;
                    case "--host":
                        address = args[i++];
                        break;
                    default:
                        String msg = String.format("Cannot parse argument \"%s\"", arg);
                        throw new CLIParamsException(msg);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new CLIParamsException("Missing command line argument");
            }
        }

        /**
         * Parses the port value.
         * 
         * @param arg the argument to parse
         * @return the parsed port
         * @throws CLIParamsException if the port specified is invalid.
         */
        protected int readPortValue(String arg) throws CLIParamsException {
            int p;
            try {
                p = Integer.parseInt(arg);

                if (p < 0 || p > 65535) {
                    String msg = String.format("Invalid port %d specified.", p);
                    throw new CLIParamsException(msg);
                }
            } catch (NumberFormatException ex) {
                String msg = String.format("Specified port %s is not a number.", arg);
                throw new CLIParamsException(msg, ex);
            }
            return p;
        }

    };

    /**
     * Main method. It sets up the listener.
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        CLIParams params = new CLIParams();
        try {
            params.parse(args);
        } catch (CLIParamsException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }

        // Builds the web server and sets up a default handler.
        Undertow server = Undertow.builder()
        .addHttpListener(params.getPort(), params.getAddress())
        .setHandler(
            exchange -> {
                exchange.setStatusCode(404);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(NOT_FOUND_STRING);
            }
        ).build();
        server.start();
    }

}